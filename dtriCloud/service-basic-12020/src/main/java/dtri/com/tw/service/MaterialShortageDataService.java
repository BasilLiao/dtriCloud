package dtri.com.tw.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dtri.com.tw.mssql.dao.CoptdDao;
import dtri.com.tw.mssql.dao.CopthDao;
import dtri.com.tw.mssql.dao.InvmbDao;
import dtri.com.tw.mssql.dao.InvtbDao;
import dtri.com.tw.mssql.dao.MoctaDao;
import dtri.com.tw.mssql.dao.MocteDao;
import dtri.com.tw.mssql.dao.MocthDao;
import dtri.com.tw.mssql.dao.PurtaDao;
import dtri.com.tw.mssql.dao.PurtcDao;
import dtri.com.tw.mssql.dao.PurthDao;
import dtri.com.tw.mssql.dto.MaterialQtyDto;
import dtri.com.tw.mssql.dto.MrpCalcItem;
import dtri.com.tw.mssql.dto.ValidatedCoptdDto;
import dtri.com.tw.mssql.dto.ValidatedInvmbDto;
import dtri.com.tw.mssql.dto.ValidatedMoctaDto;
import dtri.com.tw.mssql.dto.ValidatedMoctabDto;
import dtri.com.tw.mssql.dto.ValidatedPurtaDto;
import dtri.com.tw.mssql.dto.ValidatedPurtcDto;
import dtri.com.tw.mssql.dto.ValidatedPurthDto;
import dtri.com.tw.pgsql.dao.MaterialShortageDao;
import dtri.com.tw.pgsql.entity.MaterialShortage;
// Removed unused JPA imports

/**
 * MRP 物料缺料計算服務
 * <p>
 * 主要職責：
 * 1. 整合 ERP (MSSQL) 之庫存、製令、採購、訂單、驗收等供需數據。
 * 2. 執行 MRP (Material Requirements Planning) 邏輯運算，計算各料號之供需平衡。
 * 3. 產生缺料分析結果並寫入資料庫 (PGSQL)，提供後續報表查詢。
 * <p>
 * 關鍵業務邏輯：
 * 1. [供需配對] 依據日期排序，計算預計餘額 (SYS006) 與可用餘額 (SYS007)。
 * 2. [新採標記] 針對採購單 (SYS009)，若建單日期距今在 7 天內，標記為 "[新採]" 以利識別。
 * 3. [建議請購] 當預計餘額低於安全庫存時，計算建議請購量 (SYS011)。
 * 計算公式：((需求缺口 - MOQ) / MPQ) 無條件進位 * MPQ + MOQ。
 *
 * @author Allen Chen
 * @version 1.0
 */
@Service
public class MaterialShortageDataService {

    // --- 常數定義 ---
    private static final String TYPE_STOCK = "庫存";
    private static final String TYPE_WAIT_INSPECT = "待驗";
    private static final String KEY_INV = "INV";
    private static final String DATE_DEFAULT = "19000101";

    @Autowired
    @Qualifier("mrpExecutor")
    private Executor executor;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private MaterialShortageDataService service;

    @Autowired
    private MaterialShortageDao materialShortageDao;
    @Autowired
    private PurthDao purthDao;
    @Autowired
    private MoctaDao moctaDao;
    @Autowired
    private CopthDao copthDao;
    @Autowired
    private PurtcDao purtcDao;
    @Autowired
    private CoptdDao coptdDao;
    @Autowired
    private PurtaDao purtaDao;
    @Autowired
    private InvmbDao invmbDao;
    @Autowired
    private MocteDao mocteDao;
    @Autowired
    private InvtbDao invtbDao;
    @Autowired
    private MocthDao mocthDao;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${hibernate.jdbc.batch_size:1000}")
    private int batchSize;

    // --- 內部類別：模擬供給池 ---
    private class SupplyPoolItem {
        boolean check;
        boolean isInvmb;
        int quantity;
        int originalQuantity;
        String date;
        String docNo;
        String typeSuffix; // [請], [新採]

        public SupplyPoolItem(boolean isInvmb, int quantity, String date, String docNo, String typeSuffix) {
            this.check = quantity > 0;
            this.isInvmb = isInvmb;
            this.quantity = quantity;
            this.originalQuantity = quantity;
            this.date = date;
            this.docNo = docNo;
            this.typeSuffix = typeSuffix;
        }
    }

    /**
     * MRP 計算主流程入口
     * 流程：平行讀取資料 -> 記憶體內邏輯運算 -> 清空舊表 -> 批次寫入新資料
     */
    public void action() {
        System.out.println("\n=== 料件缺料進貨預計資料導入 MaterialShortage 資料表  Start ===");
        long globalStart = System.currentTimeMillis();

        List<MaterialShortage> finalResults = loadAndCalculateMrp(null, null, null, null);

        // ==========================================
        // Step 3: 資料庫寫入
        // ==========================================
        if (!finalResults.isEmpty()) {
            // --- 3.1 清空舊資料 (Truncate) ---
            // --- 3.2 批次寫入 ---
           service.batchInsert(finalResults);

        } else {
            System.out.println("[資料庫寫入]: 跳過 無資料");
        }

        // ==========================================
        // 總結
        // ==========================================
        long globalEnd = System.currentTimeMillis();
        System.out.printf("總歷程耗時: %d ms (%.2f 秒)\n", (globalEnd - globalStart), (globalEnd - globalStart) / 1000.0);
        System.out.println("==================================================\n");
    }

    /**
     * 即時動態重算缺料 (供 API 呼叫)
     * 
     * @param materialNos 料號清單，格式為逗號分隔字串 (例如 "A,B,C")，null 表示全查
     */
    public List<MaterialShortage> recalculateMrp(List<String> warehouses, List<String> includeTypes,
            Map<String, List<String>> excludePatterns, String materialNos) {
        String warehouseStr = null;
        if (warehouses != null && !warehouses.isEmpty()) {
            warehouseStr = "," + String.join(",", warehouses) + ",";
        }
        // 格式化料號字串，轉換為 CHARINDEX 格式 (例如 ",A,B,C,")
        String materialNosStr = null;
        if (materialNos != null && !materialNos.isBlank()) {
            materialNosStr = "," + materialNos.trim() + ",";
        }
        return loadAndCalculateMrp(warehouseStr, includeTypes, excludePatterns, materialNosStr);
    }

    /**
     * 讀取資料與執行 MRP 核心邏輯
     * 
     * @param formattedWarehouses 格式化後的倉別字串 (如 ",W01,W02,")，供 SQL Server CHARINDEX
     *                            判斷。null 代表全取。
     */
    private List<MaterialShortage> loadAndCalculateMrp(String formattedWarehouses, List<String> includeTypes,
            Map<String, List<String>> excludePatterns, String formattedMaterialNos) {
        // ==========================================
        // Step 1: 資料讀取
        // ==========================================
        var taskInvmb = asyncLoad("庫存 Invmb",
                () -> invmbDao.sumStockByMaterial(formattedWarehouses, formattedMaterialNos));
        var taskMocta = asyncLoad("製令 Mocta", () -> shouldSkipMocta(includeTypes) ? new ArrayList<ValidatedMoctaDto>()
                : moctaDao.findAllByValidatedMocta(formattedMaterialNos));
        var taskMoctab = asyncLoad("製令需 Moctab",
                () -> shouldSkipMocta(includeTypes) ? new ArrayList<ValidatedMoctabDto>()
                        : moctaDao.findAllByValidatedMoctab(formattedMaterialNos));
        var taskPurtc = asyncLoad("採購 Purtc", () -> shouldSkip(includeTypes, "採購單") ? new ArrayList<ValidatedPurtcDto>()
                : purtcDao.findAllByValidatedPurtc(formattedMaterialNos));
        var taskPurth = asyncLoad("進貨 Purth", () -> shouldSkip(includeTypes, "進貨單") ? new ArrayList<ValidatedPurthDto>()
                : purthDao.findAllByValidatedPurth(formattedMaterialNos));
        var taskCoptd = asyncLoad("訂單 Coptd", () -> shouldSkip(includeTypes, "客訂單") ? new ArrayList<ValidatedCoptdDto>()
                : coptdDao.findAllByValidatedCoptd(formattedMaterialNos));
        var taskPurta = asyncLoad("請購 Purta", () -> shouldSkip(includeTypes, "請購單") ? new ArrayList<ValidatedPurtaDto>()
                : purtaDao.findAllByValidatedPurta(formattedMaterialNos));
        var taskMocti = asyncLoad("託外進貨 Mocti",
                () -> shouldSkip(includeTypes, "進貨單") ? new ArrayList<dtri.com.tw.mssql.dto.ValidatedMoctiDto>()
                        : mocthDao.findAllByValidatedMocti(formattedMaterialNos));
        var taskAvgUsage = asyncLoad("平均用量", () -> getAvgUsage6M(formattedMaterialNos)); // 子任務內部改為依序執行，避免巢狀非同步導致執行緒池飢餓

        // 等待
        CompletableFuture.allOf(taskInvmb, taskMocta, taskMoctab, taskPurtc, taskPurth, taskCoptd, taskPurta, taskMocti)
                .join();

        // ==========================================
        // Step 2: 邏輯運算
        // ==========================================
        // 準備平均用量 Map
        Map<String, BigDecimal> avgUsageMap = taskAvgUsage.join();
        List<ValidatedInvmbDto.Pojo> invmbPojos = setAvgUsage6M(taskInvmb.join(), avgUsageMap);

        // 執行 MRP 核心計算
        return executeMrpCalculation(invmbPojos, taskPurtc.join(), taskCoptd.join(),
                taskMocta.join(), taskMoctab.join(), taskPurth.join(), taskPurta.join(), taskMocti.join(), avgUsageMap,
                includeTypes,
                excludePatterns);
    }

    // @Transactional
    public List<MaterialShortage> executeMrpCalculation(
            List<ValidatedInvmbDto.Pojo> stocks,
            List<ValidatedPurtcDto> purchases,
            List<ValidatedCoptdDto> orders,
            List<ValidatedMoctaDto> workOrders,
            List<ValidatedMoctabDto> workOrderDemands,
            List<ValidatedPurthDto> inspections,
            List<ValidatedPurtaDto> requisitions,
            List<dtri.com.tw.mssql.dto.ValidatedMoctiDto> outSourcedInspections,
            Map<String, BigDecimal> avgUsageMap,
            List<String> includeTypes,
            Map<String, List<String>> excludePatterns) {

        // 預先計算日期字串，避免迴圈內重複操作
        String sevenDaysAgoStr = LocalDate.now().minusDays(7).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalDate todayDate = LocalDate.now();
        String todayStr = todayDate.format(DateTimeFormatter.BASIC_ISO_DATE); // yyyyMMdd

        // 資料分組 (Grouping By MB001)
        Map<String, List<MrpCalcItem>> groupedData = new HashMap<>();

        workOrders.forEach(x -> {
            MrpCalcItem item = toItemFromMocta(x);
            if ((includeTypes == null || includeTypes.contains(item.getTK000())) && !isExcluded(item, excludePatterns))
                addToMap(groupedData, item);
        });
        orders.forEach(x -> {
            MrpCalcItem item = toItemFromOrder(x);
            if ((includeTypes == null || includeTypes.contains(item.getTK000())) && !isExcluded(item, excludePatterns))
                addToMap(groupedData, item);
        });

        // 僅當父層存在時才加入 (過濾孤兒資料)
        stocks.forEach(x -> addOnlyIfParentExists(groupedData, toItemFromStock(x)));
        workOrderDemands.forEach(x -> {
            MrpCalcItem item = toItemFromMoctab(x);
            if ((includeTypes == null || includeTypes.contains(item.getTK000())) && !isExcluded(item, excludePatterns))
                addOnlyIfParentExists(groupedData, item);
        });
        purchases.forEach(x -> {
            MrpCalcItem item = toItemFromPurchase(x);
            if ((includeTypes == null || includeTypes.contains(item.getTK000())) && !isExcluded(item, excludePatterns))
                addOnlyIfParentExists(groupedData, item);
        });
        requisitions.forEach(x -> {
            MrpCalcItem item = toItemFromRequisition(x);
            if ((includeTypes == null || includeTypes.contains(item.getTK000())) && !isExcluded(item, excludePatterns))
                addOnlyIfParentExists(groupedData, item);
        });
        inspections.forEach(x -> {
            MrpCalcItem item = toItemFromInspection(x);
            // 注意："待驗" 在前端被表示為 "進貨單" 或 "待驗"，所以做雙重比對
            if ((includeTypes == null || includeTypes.contains(item.getTK000()) || includeTypes.contains("進貨單")
                    || includeTypes.contains("託外進貨單"))
                    && !isExcluded(item, excludePatterns))
                addOnlyIfParentExists(groupedData, item);
        });
        outSourcedInspections.forEach(x -> {
            MrpCalcItem item = toItemFromMocti(x);
            // MOCTI 同樣被標記為待驗，所以套用相同雙重比對邏輯
            if ((includeTypes == null || includeTypes.contains(item.getTK000()) || includeTypes.contains("託外進貨單")
                    || includeTypes.contains("進貨單"))
                    && !isExcluded(item, excludePatterns))
                addOnlyIfParentExists(groupedData, item);
        });

        List<MaterialShortage> finalResults = Collections.synchronizedList(new ArrayList<>());

        // 平行流處理每個料號的計算
        groupedData.entrySet().parallelStream().forEach(entry -> {
            String materialKey = entry.getKey();
            List<MrpCalcItem> events = entry.getValue();
            Collections.sort(events); // 按日期排序 權重排序 邏輯在 MrpCalcItem 的 compareTo 中

            BigDecimal avgUsage = avgUsageMap.getOrDefault(materialKey, BigDecimal.ZERO);

            List<MaterialShortage> rows = processMaterialEvents(events, avgUsage, sevenDaysAgoStr, todayDate, todayStr);
            finalResults.addAll(rows);
        });

        // 最後依料號排序 因為前面已經分組已經把每一組都依照日期 權重排序 最後只需用料號
        finalResults.sort(Comparator.comparing(MaterialShortage::getMb001, Comparator.nullsLast(String::compareTo)));
        printStatistics(finalResults);
        return finalResults;
    }

    // =================================================================
    // 核心業務邏輯
    // =================================================================
    private List<MaterialShortage> processMaterialEvents(List<MrpCalcItem> events, BigDecimal avgUsage,
            String sevenDaysAgoStr, LocalDate todayDate, String todayStr) {
        List<MaterialShortage> resultRows = new ArrayList<>();

        // 1. 取得 主資料 (取第一筆有效的非庫存資料作為主檔參考)
        MrpCalcItem effectiveMaster = findEffectiveMasterData(events);
        MrpCalcItem stockItemData = findStockItem(events);

        BigDecimal lt = new BigDecimal(
                effectiveMaster != null && effectiveMaster.getMB036() != null ? effectiveMaster.getMB036() : 0);

        // 2. 計算安全庫存量 (SYS010)
        // 公式：((LT/30) + 0.25) * 平均月用量，無條件進位
        int safeStockLevel = 0;
        if (avgUsage.compareTo(BigDecimal.ZERO) > 0) {
            double safeQty = ((lt.doubleValue() / 30.0) + 0.25) * avgUsage.doubleValue();
            safeStockLevel = (int) Math.ceil(safeQty);
        }

        // 3. 建立供給池 (Supply Pool for SYS008/SYS009)
        List<SupplyPoolItem> supplyPool = new ArrayList<>();

        int invQty = 0;
        int inspectQty = 0;
        if (stockItemData != null) {
            invQty = stockItemData.getQTY().intValue();
        }
        for (MrpCalcItem e : events) {
            if (e.getSYS_SY005() != null)
                inspectQty += e.getSYS_SY005().intValue();
        }

        // 3.1 加入庫存+待驗
        if ((invQty + inspectQty) > 0) {
            supplyPool.add(new SupplyPoolItem(true, invQty + inspectQty, "", "", ""));
        } else {
            supplyPool.add(new SupplyPoolItem(false, 0, "", "", ""));
        }

        // 3.2 供給池填充 (採購單/請購單)
        for (MrpCalcItem e : events) {
            if (e.getQTY().compareTo(BigDecimal.ZERO) > 0 && !TYPE_STOCK.equals(e.getTK000())) {
                String typeSuffix = "";

                if ("請購單".equals(e.getTK000())) {
                    typeSuffix = " [請]";
                }
                // [新採] 判斷邏輯：採購單 && (建單日 >= 7天前)
                // 優化：使用 String.compareTo 避免 LocalDate.parse
                else if ("採購單".equals(e.getTK000()) && e.getCREATE_DATE() != null
                        && e.getCREATE_DATE().compareTo(sevenDaysAgoStr) >= 0) {
                    typeSuffix = " [新採]";
                }

                supplyPool
                        .add(new SupplyPoolItem(false, e.getQTY().intValue(), e.getTK002(), e.getTK001(), typeSuffix));
            }
        }

        // 4. 執行計算迴圈
        int sysSy001 = 0; // 缺料累計
        int sysSy002 = 0; // 供給累計

        for (int i = 0; i < events.size(); i++) {
            MrpCalcItem event = events.get(i);

            if (TYPE_STOCK.equals(event.getTK000()))
                continue;
            if (isSkipEvent(event))
                continue;

            MaterialShortage row = new MaterialShortage();
            fillBasicInfo(row, event, effectiveMaster, stockItemData, invQty, inspectQty, avgUsage);

            int qty = event.getQTY().intValue();

            // 累計供給與需求
            if (qty > 0) {
                if (!TYPE_WAIT_INSPECT.equals(event.getTK000())) {
                    sysSy002 += qty;
                }
            } else {
                sysSy001 += Math.abs(qty);
            }

            // 計算當前狀態
            row.setSyssy001(sysSy001); // 累計缺
            row.setSyssy002(sysSy002); // 累計補
            row.setSyssy006(invQty - sysSy001); // 庫存餘量(當日) (只扣不補)

            // 預計餘額 (考量供給)
            int currentAvailable = invQty + inspectQty + sysSy002 - sysSy001;
            row.setSyssy007(currentAvailable);

            // --- SYS008 配給(最後 預交日) & SYS009 配給(預交日*未交量)[新單]:單號 邏輯 ---
            if (qty < 0) {
                int demandQty = Math.abs(qty);
                StringBuilder sb008 = new StringBuilder();
                StringBuilder sb009 = new StringBuilder();

                if (supplyPool == null || (supplyPool.size() == 1 && !supplyPool.get(0).check)) {
                    sb008.append("採購量不足");
                    sb009.append("採購量不足");
                } else {
                    for (int p = 0; p < supplyPool.size(); p++) {
                        SupplyPoolItem poolItem = supplyPool.get(p);
                        if (!poolItem.check)
                            continue;

                        if (demandQty > 0) {
                            int consume = 0;
                            if (poolItem.quantity - demandQty > 0) {
                                consume = demandQty;
                                poolItem.quantity -= demandQty;
                                demandQty = 0;
                            } else {
                                consume = poolItem.quantity;
                                demandQty -= poolItem.quantity;
                                poolItem.quantity = 0;
                                poolItem.check = false;
                            }

                            if (consume > 0 || poolItem.isInvmb) {
                                if (sb008.length() > 0) {
                                    sb008.append("\n");
                                    sb009.append("\n");
                                }
                                String dateStr = poolItem.isInvmb ? "" : formatDate(poolItem.date);
                                sb008.append(dateStr);
                                if (!poolItem.isInvmb) {
                                    sb009.append(dateStr).append(" * ")
                                            .append(poolItem.originalQuantity).append(poolItem.typeSuffix)
                                            .append(" : ").append(poolItem.docNo);
                                }
                            }
                            if (demandQty == 0)
                                break;
                        }
                    }
                    if (demandQty > 0) {
                        if (sb008.length() > 0) {
                            sb008.append("\n");
                            sb009.append("\n");
                        }
                        sb008.append("採購量不足");
                        sb009.append("採購量不足");
                    }
                }
                row.setSyssy008(sb008.toString().trim());
                row.setSyssy009(sb009.toString().trim());
            }

            // --- SYS011 建議請購量邏輯 ---
            BigDecimal mb040 = effectiveMaster != null && effectiveMaster.getMB040() != null
                    ? effectiveMaster.getMB040()
                    : BigDecimal.ZERO;

            if (safeStockLevel > 0 && mb040.compareTo(BigDecimal.ZERO) > 0 && isValid(event.getTK002())) {

                // 計算未來餘額
                int projectedBalance = currentAvailable;
                for (int j = i + 1; j < events.size(); j++) {
                    MrpCalcItem futureEvent = events.get(j);
                    if (TYPE_STOCK.equals(futureEvent.getTK000()))
                        continue;

                    if (futureEvent.getQTY().compareTo(BigDecimal.ZERO) > 0) {
                        if (!TYPE_WAIT_INSPECT.equals(futureEvent.getTK000())) {
                            projectedBalance += futureEvent.getQTY().intValue();
                        }
                    }
                }

                // 若低於安全庫存，觸發建議請購
                if (safeStockLevel >= projectedBalance) {
                    String suggestDate = calculateSuggestDate(event.getTK002(), lt.intValue(), todayDate, todayStr);
                    int suggestQty = safeStockLevel - projectedBalance;

                    int mb039 = effectiveMaster != null && effectiveMaster.getMB039() != null
                            ? effectiveMaster.getMB039().intValue()
                            : 0;
                    int mpq = mb040.intValue(); // MOQ/MPQ

                    // 整數運算進位公式
                    if (suggestQty <= mb039) {
                        suggestQty = mb039;
                    } else {
                        int remain = suggestQty - mb039;
                        int packs = (remain + mpq - 1) / mpq;
                        suggestQty = (packs * mpq) + mb039;
                    }

                    row.setSyssy011(formatDate(suggestDate) + " * " + suggestQty);
                }
            } else {
                row.setSyssy011("基本資料異常");
            }

            resultRows.add(row);
        }

        return resultRows;
    }

    // =================================================================
    // 輔助方法
    // =================================================================

    /**
     * 開啟異步線程載入資料
     * 使用自定義的 mrpExecutor 避免與 CommonPool 競爭或死結。
     */
    private <T> CompletableFuture<T> asyncLoad(String taskName, java.util.function.Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                System.err.printf("   [SQL讀取失敗] %-10s\n", taskName);
                throw e;
            }
        }, executor);
    }

    // 格式化日期調整：yyyy/MM/dd (避免 substring 產生大量垃圾物件)
    private String formatDate(String yyyyMMdd) {
        if (yyyyMMdd == null || yyyyMMdd.length() != 8)
            return yyyyMMdd;
        StringBuilder sb = new StringBuilder(10);
        sb.append(yyyyMMdd, 0, 4)
                .append('/')
                .append(yyyyMMdd, 4, 6)
                .append('/')
                .append(yyyyMMdd, 6, 8);
        return sb.toString();
    }

    /**
     * 計算建議請購日期 (SYS011 用)
     * <p>
     * 邏輯：目標日期 - LT (前置天數) = 建議請購日。
     * 若計算出的日期早於今天，則強制設定為「今天」，代表需立即請購。
     * * @param targetDateStr 缺料發生的目標日期 (yyyyMMdd)
     * 
     * @param ltDays   採購前置天數 (Lead Time)
     * @param today    今日日期物件
     * @param todayStr 今日日期字串
     * @return 格式化後的建議日期字串
     */
    private String calculateSuggestDate(String targetDateStr, int ltDays, LocalDate today, String todayStr) {
        try {
            if (targetDateStr == null || targetDateStr.length() != 8)
                return targetDateStr;

            LocalDate targetDate = LocalDate.parse(targetDateStr, DateTimeFormatter.BASIC_ISO_DATE);
            LocalDate suggestDate = targetDate.minusDays(ltDays);

            // [業務邏輯] 若建議日期是過去的時間，代表已經來不及了，直接回傳「今天」以警示需急件處理
            if (suggestDate.isBefore(today)) {
                return todayStr;
            }
            return suggestDate.format(DateTimeFormatter.BASIC_ISO_DATE);
        } catch (Exception e) {
            return targetDateStr;
        }
    }

    /**
     * 從事件清單中尋找「庫存」項目
     * 用於取得初始庫存量 (INV_QTY) 與平均用量 (MC004)。
     */
    private MrpCalcItem findStockItem(List<MrpCalcItem> events) {
        for (MrpCalcItem item : events) {
            if (TYPE_STOCK.equals(item.getTK000()))
                return item;
        }
        return null;
    }

    /**
     * 尋找最有效的 Master Data (主檔資料)
     * <p>
     * 優先順序：
     * 1. 優先找「非庫存」且「有設定 LT (MB036 > 0)」的單據 (通常採購單上的資訊比主檔準確)。
     * 2. 其次找任意「非庫存」單據。
     * 3. 最後回傳第一筆資料。
     */
    private MrpCalcItem findEffectiveMasterData(List<MrpCalcItem> events) {
        // [業務邏輯] 第一輪篩選：優先抓取有 Lead Time 的交易單據 (如採購單)，視為最新且有效的參數來源
        for (MrpCalcItem item : events) {
            if (!TYPE_STOCK.equals(item.getTK000()) && item.getMB036() != null && item.getMB036() > 0)
                return item;
        }
        // [業務邏輯] 第二輪篩選：若無 LT，則抓取任一交易單據
        for (MrpCalcItem item : events) {
            if (!TYPE_STOCK.equals(item.getTK000()))
                return item;
        }
        return events.isEmpty() ? null : events.get(0);
    }

    /**
     * 填寫 MaterialShortage 基本欄位
     * 包含：料號、品名、規格、單別單號、需求/供給數量拆分、以及主檔參數的回填。
     * * @param row 要填寫的結果物件
     * 
     * @param event      當前處理的事件 (Event)
     * @param master     找到的最有效主檔 (Effective Master)
     * @param stockItem  庫存項目 (用於 fallback 取值)
     * @param invQty     當前庫存量
     * @param inspectQty 待驗量
     * @param avgUsage   6個月平均用量
     */
    private void fillBasicInfo(MaterialShortage row, MrpCalcItem event, MrpCalcItem master, MrpCalcItem stockItem,
            int invQty, int inspectQty, BigDecimal avgUsage) {
        // 1. 基礎欄位複製 (去除多餘空白)
        row.setMb001(event.getMB001() != null ? event.getMB001().trim() : "");
        row.setMb002(event.getMB002() != null ? event.getMB002().trim() : "");
        row.setMb003(event.getMB003() != null ? event.getMB003().trim() : "");
        row.setTk000(event.getTK000() != null ? event.getTK000().trim() : "");
        row.setTk001(event.getTK001() != null ? event.getTK001().trim() : "");
        row.setTk002(event.getTK002() != null ? event.getTK002().trim() : "");
        row.setTa032(event.getTA032() != null ? event.getTA032().trim() : "");
        row.setTc004(event.getTC004() != null ? event.getTC004().trim() : "");
        row.setCopma002(event.getCOPMA002() != null ? event.getCOPMA002().trim() : "");
        row.setTk003(event.getTK003() != null ? event.getTK003().trim() : "");
        // 2. 設定當下庫存狀態
        row.setInvmbmc007(invQty);
        row.setSyssy005(inspectQty);

        // [業務邏輯] 需求與供給拆分：QTY < 0 為需求 (SysSy003)，QTY > 0 為供給 (SysSy004)
        if (event.getQTY().compareTo(BigDecimal.ZERO) < 0) {
            row.setSyssy003(event.getQTY().abs().intValue());
            row.setSyssy004(0);
        } else {
            row.setSyssy003(0);
            row.setSyssy004(event.getQTY().intValue());
        }

        // 3. 填寫主檔屬性 (Brand, Spec 等) - 優先使用 Event 本身資料，若無則查 Master
        row.setMb032(isValid(event.getMB032()) ? event.getMB032().trim()
                : orEmpty(master != null ? master.getMB032() : "").trim());
        row.setMa002(isValid(event.getMB032()) ? event.getMA002().trim()
                : orEmpty(master != null ? master.getMA002() : "").trim());
        row.setMb017(isValid(event.getMB017()) ? event.getMB017().trim()
                : orEmpty(master != null ? master.getMB017() : "").trim());
        row.setMc002(isValid(event.getMB017()) ? event.getMC002().trim()
                : orEmpty(master != null ? master.getMC002() : "").trim());

        // [業務邏輯] 參數優先級：當前單據 > 有效主檔(Master) > 預設值 0
        // LT (前置天數)
        row.setMb036((event.getMB036() != null && event.getMB036() > 0) ? event.getMB036()
                : (master != null && master.getMB036() != null ? master.getMB036() : 0));
        // MOQ (最小訂購量)
        row.setMb039((event.getMB039() != null && event.getMB039().compareTo(BigDecimal.ZERO) > 0)
                ? event.getMB039().intValue()
                : (master != null && master.getMB039() != null ? master.getMB039().intValue() : 0));
        // MPQ (包裝量)
        row.setMb040((event.getMB040() != null && event.getMB040().compareTo(BigDecimal.ZERO) > 0)
                ? event.getMB040().intValue()
                : (master != null && master.getMB040() != null ? master.getMB040().intValue() : 0));

        // 4. 設定平均用量 (優先使用計算出的 avgUsage，若無則嘗試從庫存檔取)
        if (avgUsage != null) {
            row.setMc004(avgUsage.intValue());
        } else if (stockItem != null && stockItem.getMC004() != null) {
            row.setMc004(stockItem.getMC004().intValue());
        } else {
            row.setMc004(0);
        }
    }

    /**
     * 計算 6 個月平均用量 (修正版：移除巢狀非同步以避免 Thread Starvation)
     * 
     * @param formattedMaterialNos 逗號分隔的料號字串 (如 ",A,B,") 提供給 SQL 篩選。null 或空白代表全量。
     */
    private Map<String, BigDecimal> getAvgUsage6M(String formattedMaterialNos) {

        // 1. 依序執行 DAO 查詢 (增加詳細計時日誌以利優化)
        long subStart1 = System.currentTimeMillis();
        List<MaterialQtyDto> listPIM = mocteDao.findMocte005Qty(formattedMaterialNos);
        long subEnd1 = System.currentTimeMillis();
        System.out.println("   [平均用量] Mocte (耗用): " + (subEnd1 - subStart1) + " ms");

        long subStart2 = System.currentTimeMillis();
        List<MaterialQtyDto> listSDM = copthDao.findCopth008Qty(formattedMaterialNos);
        long subEnd2 = System.currentTimeMillis();
        System.out.println("   [平均用量] Copth (銷貨): " + (subEnd2 - subStart2) + " ms");

        long subStart3 = System.currentTimeMillis();
        List<MaterialQtyDto> listSTM = invtbDao.getImvtb007QtyList(formattedMaterialNos);
        long subEnd3 = System.currentTimeMillis();
        System.out.println("   [平均用量] Invtb (異動): " + (subEnd3 - subStart3) + " ms");

        long subStart4 = System.currentTimeMillis();
        List<MaterialQtyDto> listMOM = moctaDao.findMoctb005Qty(formattedMaterialNos);
        long subEnd4 = System.currentTimeMillis();
        System.out.println("   [平均用量] Moctb (製領): " + (subEnd4 - subStart4) + " ms");

        Map<String, BigDecimal> pIM = toMap(listPIM);
        Map<String, BigDecimal> sDM = toMap(listSDM);
        Map<String, BigDecimal> sTM = toMap(listSTM);
        Map<String, BigDecimal> mOM = toMap(listMOM);

        // 2. 合併計算
        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(pIM.keySet());
        allKeys.addAll(sDM.keySet());
        allKeys.addAll(sTM.keySet());
        allKeys.addAll(mOM.keySet());

        Map<String, BigDecimal> resultMap = new HashMap<>();
        BigDecimal divisor = new BigDecimal(6);

        for (String mb001 : allKeys) {
            BigDecimal total = pIM.getOrDefault(mb001, BigDecimal.ZERO)
                    .add(sDM.getOrDefault(mb001, BigDecimal.ZERO))
                    .add(sTM.getOrDefault(mb001, BigDecimal.ZERO))
                    .subtract(mOM.getOrDefault(mb001, BigDecimal.ZERO));

            if (total.compareTo(BigDecimal.ZERO) > 0) {
                resultMap.put(mb001, total.divide(divisor, 0, RoundingMode.DOWN));
            }
        }
        return resultMap;
    }

    private List<ValidatedInvmbDto.Pojo> setAvgUsage6M(List<ValidatedInvmbDto> invmbs,
            Map<String, BigDecimal> avgUsag6M) {
        return invmbs.stream().map(x -> {
            ValidatedInvmbDto.Pojo pojo = new ValidatedInvmbDto.Pojo(x);
            String cleanKey = (x.getMB001() == null) ? "" : x.getMB001().trim();
            BigDecimal safeQty = avgUsag6M.get(cleanKey);
            pojo.setMC004(safeQty != null ? String.valueOf(safeQty.intValue()) : "0");
            return pojo;
        }).collect(Collectors.toList());
    }

    private Map<String, BigDecimal> toMap(List<MaterialQtyDto> list) {
        return list.stream().collect(Collectors.toMap(
                dto -> (dto.getMb001() == null) ? "" : dto.getMb001().trim(),
                MaterialQtyDto::getQty, (v1, v2) -> v1));
    }

    /*
     * 打印每個單據的筆數 跟總筆數 有需要再加到 master
     */
    private void printStatistics(List<MaterialShortage> results) {
        System.out.println("\n=== MRP 運算結果統計筆數 ===");
        System.out.println("總筆數: " + results.size());
        Map<String, Long> stats = results.stream().map(r -> r.getTk000() != null ? r.getTk000() : "(未定義)")
                .collect(Collectors.groupingBy(k -> k, TreeMap::new, Collectors.counting()));
        stats.forEach((k, v) -> System.out.printf(" %-15s : %6d 筆\n", k, v));
        System.out.println("==========================================\n");
    }

    /**
     * 原生批次寫入資料庫 (JdbcTemplate)
     * 繞過 JPA Reflection / Auditing，直接進行最高效能的 Insert，大幅縮短自動同步的耗時。
     */
    @Transactional
    public void batchInsert(List<MaterialShortage> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        long truncateStart = System.currentTimeMillis();
        materialShortageDao.truncateTable();
        System.out.println("[資料庫同步]: Truncate 清空資料表耗時: " + (System.currentTimeMillis() - truncateStart) + " ms");
        long insertStart = System.currentTimeMillis();
        // PostgreSQL 原生 Insert 語法
        String sql = "INSERT INTO material_shortage (" +
                "msl_id, mb001, mb002, mb003, tk002, " + // 1~5
                "invmb_mc007, sys_sy003, sys_sy004, sys_sy005, " + // 6~9
                "sys_sy001, sys_sy002, sys_sy006, sys_sy007, " + // 10~13
                "tk000, tk001, tk003, sys_sy008, sys_sy009, " + // 14~18
                "mc004, sys_sy011, mb036, mb039, mb040, " + // 19~23
                "mb032, ma002, mb017, mc002, ta032, " + // 24~28
                "tc004, copma002, " + // 29~30
                "sys_c_date, sys_c_user, sys_m_date, sys_m_user, " + // 31~34
                "sys_o_date, sys_o_user, sys_header, sys_status, " + // 35~38
                "sys_sort, sys_note" + // 39~40
                ") VALUES (" +
                "NEXTVAL('material_shortage_seq'), ?, ?, ?, ?, " +
                "?, ?, ?, ?, " +
                "?, ?, ?, ?, " +
                "?, ?, ?, ?, ?, " +
                "?, ?, ?, ?, ?, " +
                "?, ?, ?, ?, ?, " +
                "?, ?, " +
                "?, ?, ?, ?, " +
                "?, ?, ?, ?, " +
                "?, ?)";

        // --- 時間戳記物件複用，減少 8.4 萬次物件建立與系統調用 ---
        java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
        final int totalSize = list.size();
        System.out.println("   [寫入細節] 欄位數: 40, 總儲存單元數: " + (totalSize * 40));

        // 採用分段批次寫入 (Chunking) 以提升大數據量下的效能與穩定性
        for (int i = 0; i < totalSize; i += batchSize) {
            final List<MaterialShortage> chunk = list.subList(i, Math.min(i + batchSize, totalSize));

            jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                @Override
                public void setValues(@org.springframework.lang.NonNull java.sql.PreparedStatement ps, int j)
                        throws java.sql.SQLException {
                    MaterialShortage item = chunk.get(j);

                    // --- 業務欄位 ---
                    ps.setString(1, item.getMb001());
                    ps.setString(2, item.getMb002());
                    ps.setString(3, item.getMb003());
                    ps.setString(4, item.getTk002());
                    ps.setObject(5, item.getInvmbmc007(), java.sql.Types.INTEGER);

                    ps.setObject(6, item.getSyssy003(), java.sql.Types.INTEGER);
                    ps.setObject(7, item.getSyssy004(), java.sql.Types.INTEGER);
                    ps.setObject(8, item.getSyssy005(), java.sql.Types.INTEGER);
                    ps.setObject(9, item.getSyssy001(), java.sql.Types.INTEGER);
                    ps.setObject(10, item.getSyssy002(), java.sql.Types.INTEGER);

                    ps.setObject(11, item.getSyssy006(), java.sql.Types.INTEGER);
                    ps.setObject(12, item.getSyssy007(), java.sql.Types.INTEGER);
                    ps.setString(13, item.getTk000());
                    ps.setString(14, item.getTk001());
                    ps.setString(15, item.getTk003());

                    ps.setString(16, item.getSyssy008());
                    ps.setString(17, item.getSyssy009());
                    ps.setObject(18, item.getMc004(), java.sql.Types.INTEGER);
                    ps.setString(19, item.getSyssy011());
                    ps.setObject(20, item.getMb036(), java.sql.Types.INTEGER);

                    ps.setObject(21, item.getMb039(), java.sql.Types.INTEGER);
                    ps.setObject(22, item.getMb040(), java.sql.Types.INTEGER);
                    ps.setString(23, item.getMb032());
                    ps.setString(24, item.getMa002());
                    ps.setString(25, item.getMb017());

                    ps.setString(26, item.getMc002());
                    ps.setString(27, item.getTa032());
                    ps.setString(28, item.getTc004());
                    ps.setString(29, item.getCopma002());

                    // --- 系統欄位 (透過複用的 now 物件提升效能) ---
                    ps.setTimestamp(30, now); // sys_c_date
                    ps.setString(31, "system"); // sys_c_user
                    ps.setTimestamp(32, now); // sys_m_date
                    ps.setString(33, "system"); // sys_m_user
                    ps.setTimestamp(34, now); // sys_o_date
                    ps.setString(35, "system"); // sys_o_user

                    ps.setBoolean(36, false); // sys_header
                    ps.setObject(37, 0, java.sql.Types.INTEGER); // sys_status
                    ps.setObject(38, 0, java.sql.Types.INTEGER); // sys_sort
                    ps.setString(39, ""); // sys_note
                }

                @Override
                public int getBatchSize() {
                    return chunk.size();
                }
            });
        }
        System.out.println("[資料庫同步]: Batch Insert 寫入 " + list.size() + " 筆耗時: "
                + (System.currentTimeMillis() - insertStart) + " ms");
    }

    // --- Utils ---
    private boolean isSkipEvent(MrpCalcItem event) {
        return event.getSYS_NOTE() != null
                && (event.getSYS_NOTE().contains("Source: Purth ") || event.getSYS_NOTE().contains("Source: Mocti "));
    }

    private boolean isValid(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private String orEmpty(String s) {
        return s != null ? s : "";
    }

    private void addToMap(Map<String, List<MrpCalcItem>> map, MrpCalcItem item) {
        if (isValid(item.getMB001())) {
            map.computeIfAbsent(item.getMB001().trim(), k -> new ArrayList<>()).add(item);
        }
    }

    /**
     * [業務邏輯] 僅加入有「父層需求」的資料 (過濾孤兒數據)
     * <p>
     * 邏輯說明：
     * MRP 運算核心是以「製令單(Mocta)」與「訂單(Coptd)」為主驅動。
     * 若某料號(MB001)只有庫存或採購單，但在當前範圍內完全沒有製令或訂單需求，
     * 則視為「無效運算對象」，直接捨棄不處理。
     * <p>
     * 目的：
     * 1. 減少運算雜訊：報表只呈現有實際生產或銷售需求的料件。
     * 2. 提升效能：避免計算那些呆滯料或目前無用的庫存數據。
     */
    private void addOnlyIfParentExists(Map<String, List<MrpCalcItem>> map, MrpCalcItem item) {
        if (isValid(item.getMB001()) && item.getQTY().compareTo(BigDecimal.ZERO) != 0) {
            String key = item.getMB001().trim();
            if (map.containsKey(key))
                map.get(key).add(item);
        }
    }

    /**
     * 判斷是否應該跳過特定類別的資料庫查詢 (根據前端傳入的過濾條件)
     */
    private boolean shouldSkip(List<String> includeTypes, String typeName) {
        if (includeTypes == null)
            return false;
        return !includeTypes.contains(typeName);
    }

    private boolean shouldSkipMocta(List<String> includeTypes) {
        if (includeTypes == null)
            return false;
        return !includeTypes.contains("製令單") &&
                !includeTypes.contains("製令單(退)") &&
                !includeTypes.contains("內製令單");
    }

    /**
     * 檢查單據是否符合排除規則
     */
    private boolean isExcluded(MrpCalcItem item, Map<String, List<String>> excludePatterns) {
        if (excludePatterns == null || excludePatterns.isEmpty() || !isValid(item.getTK001()))
            return false;

        // 取得該單別的排除關鍵字列表
        List<String> patterns = excludePatterns.get(item.getTK000());
        if (patterns == null || patterns.isEmpty())
            return false;

        String docNo = item.getTK001().trim();
        for (String p : patterns) {
            if (p != null && !p.trim().isEmpty() && docNo.startsWith(p.trim())) {
                return true;
            }
        }
        return false;
    }

    // --- DTO Mappers ---

    private MrpCalcItem toItemFromPurchase(ValidatedPurtcDto dto) {
        MrpCalcItem item = new MrpCalcItem();
        populateCommon(item, dto.getMB001(), dto.getMB002(), dto.getMB003(), dto.getTC001_TC002(), dto.getTD012(),
                dto.getMB032(), dto.getMA002(), dto.getMB017(), dto.getMC002(), dto.getMB036(), dto.getMB039(),
                dto.getMB040());
        item.setTK000(dto.getTK000());
        item.setQTY(dto.getTD008_TH007());
        item.setCREATE_DATE(dto.getCreateDate());
        return item;
    }

    private MrpCalcItem toItemFromMocta(ValidatedMoctaDto dto) {
        MrpCalcItem item = new MrpCalcItem();
        populateCommon(item, dto.getMB001(), dto.getMB002(), dto.getMB003(), dto.getTA001_TA002(), dto.getTA009(),
                dto.getMB032(), dto.getMA002(), dto.getMB017(), dto.getMC002(), dto.getMB036(), dto.getMB039(),
                dto.getMB040());

        boolean isReturn = dto.getTB004_TB005().intValue() < 0;
        item.setTK000(dto.getTK000() + (isReturn ? "(退)" : ""));
        item.setQTY(dto.getTB004_TB005().negate());
        item.setTK001(dto.getTA001_TA002());
        item.setTK002(dto.getTA009());
        item.setTK003(dto.getTA006());
        item.setTA032(dto.getTA032()); // 新增加工廠商
        item.setTC004(dto.getTC004()); // 客戶代號
        item.setCOPMA002(dto.getCOPMA002()); // 客戶名稱
        return item;
    }

    private MrpCalcItem toItemFromMoctab(ValidatedMoctabDto dto) {
        MrpCalcItem item = new MrpCalcItem();
        populateCommon(item, dto.getMB001(), dto.getMB002(), dto.getMB003(), dto.getTA001_TA002(), dto.getTA009(),
                dto.getMB032(), dto.getMA002(), dto.getMB017(), dto.getMC002(), dto.getMB036(), dto.getMB039(),
                dto.getMB040());
        boolean isReturn = dto.getTA015_TA017().intValue() < 0;
        item.setTK000(dto.getTK000() + (isReturn ? "(退)" : ""));
        item.setQTY(dto.getTA015_TA017());
        return item;
    }

    private MrpCalcItem toItemFromOrder(ValidatedCoptdDto dto) {
        MrpCalcItem item = new MrpCalcItem();
        populateCommon(item, dto.getMB001(), dto.getMB002(), dto.getMB003(), dto.getTD001_TD002(), dto.getTD013(),
                dto.getMB032(), dto.getMA002(), dto.getMB017(), dto.getMC002(), dto.getMB036(), dto.getMB039(),
                dto.getMB040());
        item.setTK000(dto.getTK000());
        item.setQTY(dto.getTD008_TD009().negate());
        return item;
    }

    private MrpCalcItem toItemFromStock(ValidatedInvmbDto.Pojo dto) {
        MrpCalcItem item = new MrpCalcItem();
        item.setMB001(dto.getMB001() != null ? dto.getMB001().trim() : "");
        item.setTK000(TYPE_STOCK);
        item.setTK001(KEY_INV);
        item.setTK002(DATE_DEFAULT);
        item.setQTY(dto.getMC007());
        item.setMC004(new BigDecimal(dto.getMC004()));
        return item;
    }

    private MrpCalcItem toItemFromInspection(ValidatedPurthDto dto) {
        MrpCalcItem item = new MrpCalcItem();
        item.setMB001(dto.getMB001());
        item.setMB002(dto.getMB002());
        item.setMB003(dto.getMB003());
        item.setTK000(dto.getTK000());
        item.setTK001(dto.getTH001_TH002());
        item.setTK000(TYPE_WAIT_INSPECT);
        item.setTK002(DATE_DEFAULT);
        item.setSYS_SY005(dto.getTH007());
        BigDecimal qty = dto.getTH007() != null ? dto.getTH007() : BigDecimal.ZERO;
        item.setQTY(qty);
        item.setSYS_NOTE("Source: Purth " + dto.getTH001_TH002());
        return item;
    }

    private MrpCalcItem toItemFromMocti(dtri.com.tw.mssql.dto.ValidatedMoctiDto dto) {
        MrpCalcItem item = new MrpCalcItem();
        item.setMB001(dto.getMB001());
        item.setMB002(dto.getMB002());
        item.setMB003(dto.getMB003());
        item.setTK000(dto.getTK000());
        item.setTK001(dto.getTI001_TI002());
        item.setTK000(TYPE_WAIT_INSPECT);
        item.setTK002(DATE_DEFAULT);
        item.setSYS_SY005(dto.getTI007());
        BigDecimal qty = dto.getTI007() != null ? dto.getTI007() : BigDecimal.ZERO;
        item.setQTY(qty);
        item.setSYS_NOTE("Source: Mocti " + dto.getTI001_TI002());
        return item;
    }

    private MrpCalcItem toItemFromRequisition(ValidatedPurtaDto dto) {
        MrpCalcItem item = new MrpCalcItem();
        populateCommon(item, dto.getMB001(), dto.getMB002(), dto.getMB003(), dto.getTB001_TB002(), dto.getTB011(),
                dto.getMB032(), dto.getMA002(), dto.getMB017(), dto.getMC002(), dto.getMB036(), dto.getMB039(),
                dto.getMB040());
        item.setTK000(dto.getTK000());
        item.setQTY(dto.getTB009());
        return item;
    }

    private void populateCommon(MrpCalcItem item, String mb001, String mb002, String mb003, String tk001, String tk002,
            String mb032, String ma002, String mb017, String mc002, Integer mb036, BigDecimal mb039, BigDecimal mb040) {
        item.setMB001(mb001);
        item.setMB002(mb002);
        item.setMB003(mb003);
        item.setTK001(tk001);
        item.setTK002(tk002);
        item.setMB032(mb032);
        item.setMA002(ma002);
        item.setMB017(mb017);
        item.setMC002(mc002);
        item.setMB036(mb036);
        item.setMB039(mb039);
        item.setMB040(mb040);
    }
}
