package dtri.com.tw.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import dtri.com.tw.pgsql.dao.MaterialReplacementGroupDao;
import dtri.com.tw.pgsql.dao.MaterialShortageDao;
import dtri.com.tw.pgsql.dao.MusUserSearchDao;
import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.dao.WarehouseConfigDao;
import dtri.com.tw.pgsql.dao.WarehouseInventoryDao;
import dtri.com.tw.pgsql.dao.WarehouseMaterialDao;
import dtri.com.tw.pgsql.dto.MaterialShortageDto;
import dtri.com.tw.pgsql.entity.MaterialReplacementGroup;
import dtri.com.tw.pgsql.entity.MaterialReplacementItem;
import dtri.com.tw.pgsql.entity.MusUserSearch;
import dtri.com.tw.pgsql.entity.SystemLanguageCell;
import dtri.com.tw.pgsql.entity.WarehouseConfig;
import dtri.com.tw.pgsql.entity.WarehouseInventory;
import dtri.com.tw.pgsql.entity.WarehouseMaterial;
import dtri.com.tw.shared.ExcelTool;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能模組: 通用-缺料預計
 */
@Service
public class MaterialShortageService {

    @Autowired
    private PackageService packageService;

    @Autowired
    private MusUserSearchDao musUserSearchDao;

    @Autowired
    private SystemLanguageCellDao languageDao;

    @Autowired
    private MaterialShortageDao shortageDao;

    @Autowired
    private MaterialReplacementGroupDao materialReplacementGroupDao;

    @Autowired
    private WarehouseConfigDao warehouseConfigDao;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WarehouseInventoryDao warehouseInventoryDao;

    @Autowired
    private WarehouseMaterialDao warehouseMaterialDao;

    private Gson gson = new Gson();

    @Transactional(readOnly = true)
    public PackageBean getSearch(PackageBean packageBean) throws Exception {
        // [Time] 計時開始
        long startTotal = System.currentTimeMillis();
        StringBuilder timeLog = new StringBuilder();

        try {
            // --- 1. DB 查詢 ---
            long t1 = System.currentTimeMillis();
            List<MaterialShortageDto> allData = shortageDao.findAllData();
            timeLog.append("DB:").append(System.currentTimeMillis() - t1).append("ms | ");

            // --- 2. 準備規則 Map (N對N 邏輯核心) ---
            long t2 = System.currentTimeMillis();
            Map<String, List<ReplacementRuleDto>> ruleMap = prepareReplacementRules(packageBean.getUserLanguaue());
            timeLog.append("RulePrep:").append(System.currentTimeMillis() - t2).append("ms | ");

            // --- 3. 業務邏輯 ---
            long t3 = System.currentTimeMillis();
            for (MaterialShortageDto dto : allData) {
                // 只要 Map 裡有這個料號，就代表它有替代規則 (無論是 Trigger 還是 Partner)
                // Fix: 加上 trim() 以防資料庫有空白
                String mb001 = dto.getMb001() == null ? "" : dto.getMb001().trim();
                if (ruleMap.containsKey(mb001)) {
                    dto.setHasreplacement(true);
                }
            }
            timeLog.append("Logic:").append(System.currentTimeMillis() - t3).append("ms | ");

            // --- 4. 轉 JSON ---
            long t4 = System.currentTimeMillis();
            String json = packageService.beanToMatrixJson(allData);
            timeLog.append("JSON:").append(System.currentTimeMillis() - t4).append("ms | ");

            // --- 5. 封裝其他資料 ---
            long t5 = System.currentTimeMillis();

            // --- 5.5. 動態語系 (i18n) 字典與欄位寬度 ---
            Map<String, String> uiLangMap = new HashMap<>();
            Map<String, Integer> widthMap = new HashMap<>();
            List<SystemLanguageCell> languages = languageDao.findAllByLanguageCellSame("MaterialShortage", null, 2);
            // 也包含標題與按鈕的 Type 1
            languages.addAll(languageDao.findAllByLanguageCellSame("MaterialShortage", null, 1));
            // 補齊 MaterialFront 掃描出來的新語系
            languages.addAll(languageDao.findAllByLanguageCellSame("MaterialFront", null, 1));
            languages.addAll(languageDao.findAllByLanguageCellSame("MaterialFront", null, 2));

            languages.forEach(x -> {
                if (x.getSltarget() != null && !x.getSltarget().isEmpty()) {
                    if (x.getSllanguage() != null && !x.getSllanguage().isEmpty()) {
                        uiLangMap.put(x.getSltarget(), x.getSllanguage());
                    }
                    if (x.getSlcwidth() != null && x.getSlcwidth() > 0) {
                        widthMap.put(x.getSltarget(), x.getSlcwidth());
                    }
                }
            });

            Map<String, Object> resultThead = new LinkedHashMap<>();
            setupResultThead(resultThead, widthMap, uiLangMap, packageBean.getUserLanguaue());
            List<Map<String, Object>> searchInputs = new ArrayList<>();
            Map<String, Object> searchSetMap = new HashMap<>();
            searchSetMap.put("resultThead", resultThead);
            searchSetMap.put("searchSet", searchInputs);
            searchSetMap.put("uiLang", uiLangMap);

            Map<String, Object> otherSetMap = new HashMap<>();
            Optional<MusUserSearch> motorMemory = musUserSearchDao.findByMusuid(
                    packageBean.getEntityIKeyGKey() == null || packageBean.getEntityIKeyGKey().isEmpty()
                            ? null
                            : Long.valueOf(packageBean.getEntityIKeyGKey()));

            if (motorMemory.isPresent()) {
                otherSetMap.put("userMemory", motorMemory.get().getMussearch());
            }
            // 將 N對N 的規則 Map 放入 response
            otherSetMap.put("replacementRules", ruleMap);

            // 將動態倉別放進 response 讓前端抓取
            List<WarehouseConfig> warehouses = warehouseConfigDao.findAllWarehouses();
            List<Map<String, String>> whList = new ArrayList<>();
            for (WarehouseConfig wh : warehouses) {
                Map<String, String> whMap = new HashMap<>();
                whMap.put("alias", wh.getWcalias());
                whMap.put("name", wh.getWcwkaname());
                whList.add(whMap);
            }
            otherSetMap.put("warehouses", whList);

            packageBean.setEntityDetailJson(json);
            packageBean.setSearchSet(objectMapper.writeValueAsString(searchSetMap));
            packageBean.setOtherSet(objectMapper.writeValueAsString(otherSetMap));

            timeLog.append("Final:").append(System.currentTimeMillis() - t5).append("ms");

            // --- 輸出時間監控 ---
            long totalTime = System.currentTimeMillis() - startTotal;
            System.err.println("\n>>> [List Version] Total: " + totalTime + "ms >>> [" + timeLog.toString() + "]");

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return packageBean;
    }

    private void setupResultThead(Map<String, Object> resultThead, Map<String, Integer> widthMap,
            Map<String, String> uiLangMap, String lang) {
        String[] fields = { "mb001", "mb002", "mb003", "tk002", "invmbmc007", "syssy003", "syssy004", "syssy005",
                "syssy001", "syssy002",
                "syssy006", "syssy007", "tk000", "tk001", "tk003", "ta032", "syssy008", "syssy009", "mc004", "mb036",
                "mb039",
                "mb040", "mb032", "ma002", "mb017", "mc002", "tc004", "copma002", "syssy011", "hasreplacement",
                "simadvice" };

        Map<String, String> defaultNotes = new HashMap<>();
        if ("en_US".equals(lang)) {
            defaultNotes.put("syssy006", "Formula: Current Stock - Pending Delivery Qty");
            defaultNotes.put("syssy007",
                    "Formula: Current Stock + Pending Inspect + Pending Purchase - Pending Delivery");
            defaultNotes.put("mc004", "Avg Monthly Usage (6 months)");
            defaultNotes.put("mb036", "Fixed Lead Time Days");
            defaultNotes.put("mb039", "Min Order Qty");
            defaultNotes.put("mb040", "Min Package Qty");
            defaultNotes.put("syssy011", "(Rec) Purchase Date * Qty");
        } else {
            defaultNotes.put("syssy006", "公式：目前庫存 - 累計未領量");
            defaultNotes.put("syssy007", "公式：目前庫存 + 待驗 + 累計未交 - 累計未領");
            defaultNotes.put("mc004", "平均量六個月均用量");
            defaultNotes.put("mb036", "固定前置天數");
            defaultNotes.put("mb039", "最低補量");
            defaultNotes.put("mb040", "最小包裝量");
            defaultNotes.put("syssy011", "(推薦)請購日*數量");
        }

        int i = 1;
        for (String field : fields) {
            String order = String.format("%02d", i++) + "_";
            int defaultWidth = 150;
            if (field.equals("tk000"))
                defaultWidth = 90;
            if (field.equals("invmbmc007"))
                defaultWidth = 130;

            ExcelTool tool = ExcelTool.def(field, field).width(widthMap.getOrDefault(field, defaultWidth));
            if (defaultNotes.containsKey(field)) {
                tool.note(defaultNotes.get(field));
            }

            Map<String, Object> col = tool.build();
            // 如果資料庫有翻譯，直接覆蓋！(這才是真正的動態語系)
            if (uiLangMap.containsKey(field)) {
                col.put("cellLanguage", uiLangMap.get(field));
            }

            if (field.equals("mb040")) {
                resultToHead(order + field, col, resultThead);
            } else {
                resultThead.put(order + field, col);
            }
        }
    }

    private void resultToHead(String key, Object val, Map<String, Object> target) {
        target.put(key, val);
    }

    /**
     * 隨選即查 (JIT) 庫存補全邏輯
     * 根據前端傳入的料號列表與選定倉庫，回傳加總後的即時庫存
     */
    @Transactional(readOnly = true)
    public PackageBean getItemStock(PackageBean packageBean) throws Exception {
        try {
            // [Log Interceptor] Raw SearchSet:
            System.out.println("[JIT Interceptor] Raw SearchSet: " + packageBean.getSearchSet());

            // 指定預設補查倉別 (全勾選範圍)
            List<String> DEFAULT_WHS = java.util.Arrays.asList("A0001", "A0002", "A00021", "A0003", "A0005", "A0009",
                    "A0015", "A0019", "A0027", "A0029", "A0030", "A0032", "A0041", "A10011", "A2001", "B0001", "B0002",
                    "D0045");

            // 從 searchSet 取得參數
            JsonObject reqJson = packageService.StringToJson(packageBean.getSearchSet());
            List<String> mb001s_raw = gson.fromJson(reqJson.get("mb001s"), new TypeToken<List<String>>() {
            }.getType());
            List<String> warehouses_raw = gson.fromJson(reqJson.get("warehouses"), new TypeToken<List<String>>() {
            }.getType());

            // 確保去除空白
            List<String> mb001s = mb001s_raw.stream().map(String::trim).toList();
            List<String> warehouses = (warehouses_raw != null)
                    ? new ArrayList<>(warehouses_raw.stream().map(String::trim).toList())
                    : new ArrayList<>();

            // 如果前端沒傳倉庫 (例如 Modal 被重置)，則代入全公司常用倉別
            if (warehouses.isEmpty()) {
                System.out.println("[JIT] Warehouses is empty, using DEFAULT list.");
                warehouses.addAll(DEFAULT_WHS);
            }

            System.out.println("[JIT Interceptor] mb001s (Size:" + mb001s.size() + "): " + mb001s);
            System.out.println("[JIT Interceptor] warehouses (Size:" + warehouses.size() + "): " + warehouses);

            if (mb001s == null || warehouses == null || warehouses.isEmpty()) {
                System.out.println("[JIT Interceptor] WARNING: Warehouses list is EMPTY!");
            }

            List<Map<String, Object>> results = new ArrayList<>();
            for (String mb001 : mb001s) {
                // 1. 取得基本物料資訊 (從 WarehouseMaterial)
                ArrayList<WarehouseMaterial> wmList = warehouseMaterialDao.findAllByWmpnb(mb001);
                String name = "";
                String spec = "";
                if (!wmList.isEmpty()) {
                    name = wmList.get(0).getWmname();
                    spec = wmList.get(0).getWmspecification();
                }

                // 2. 取得明細庫存資訊 (含中文倉別名稱)
                List<WarehouseInventory> detailList = warehouseInventoryDao.findAllByWiwmpnbAndWiwaaliasIn(mb001,
                        warehouses);

                int totalInv = 0;
                int totalTransit = 0;
                StringBuilder detailSb = new StringBuilder();

                for (WarehouseInventory wi : detailList) {
                    totalInv += wi.getWinqty();
                    totalTransit += wi.getWitqty();
                    if (wi.getWinqty() > 0 || wi.getWitqty() > 0) {
                        detailSb.append(wi.getWiwaaliasname())
                                .append("(").append(wi.getWiwaalias()).append("): ")
                                .append(wi.getWinqty()).append(" ");
                        if (wi.getWitqty() > 0) {
                            String transitLabel = "en_US".equals(packageBean.getUserLanguaue()) ? "[T" : "[待";
                            detailSb.append(transitLabel).append(wi.getWitqty()).append("] ");
                        }
                        detailSb.append("| ");
                    }
                }

                System.out.println("[Backend JIT Debug] Part: " + mb001 + ", sumInv: " + totalInv + ", sumTransit: "
                        + totalTransit);

                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("mb001", mb001);
                itemMap.put("mb002", name);
                itemMap.put("mb003", spec);
                itemMap.put("invmbmc007", totalInv); // 對應實體庫存 (INV_MB_MC007)
                itemMap.put("syssy005", totalTransit); // 對應待驗量 (SYS_SY005)
                itemMap.put("inventorydetail", detailSb.toString()); // 中文細項
                results.add(itemMap);
            }

            packageBean.setEntityDetailJson(objectMapper.writeValueAsString(results));

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return packageBean;
    }

    /**
     * 支援 N對N 連動邏輯 + 雙向判斷
     * 1. 撈出所有啟用規則 (FETCH JOIN)
     * 2. 正向：Source -> Target (永遠執行)
     * 3. 反向：Target -> Source (只有 EQUIVALENT 時執行)
     */
    private Map<String, List<ReplacementRuleDto>> prepareReplacementRules(String lang) {
        List<MaterialReplacementGroup> activeGroups = materialReplacementGroupDao.findAllActiveRules();
        Map<String, List<ReplacementRuleDto>> resultMap = new HashMap<>();

        for (MaterialReplacementGroup group : activeGroups) {

            // 取得策略 (若無則預設為雙向 EQUIVALENT)
            String policy = group.getPolicy() != null ? group.getPolicy() : "EQUIVALENT";

            // 1. 分類 Source (舊料) 與 Target (新料)
            List<MaterialReplacementItem> allSources = group.getItems().stream()
                    .filter(i -> "SOURCE".equals(i.getRole())).collect(Collectors.toList());
            List<MaterialReplacementItem> allTargets = group.getItems().stream()
                    .filter(i -> "TARGET".equals(i.getRole())).collect(Collectors.toList());

            if (allSources.isEmpty() || allTargets.isEmpty())
                continue;

            // =================================================================
            // 情境一：正向替代 (Source -> Target)
            // 邏輯：缺舊料(Source)，去消耗新料(Target)
            // 限制：無 (正向永遠成立)
            // =================================================================
            for (MaterialReplacementItem sourceItem : allSources) {
                String currentPn = sourceItem.getMrnb() == null ? "" : sourceItem.getMrnb().trim();
                Double baseQty = sourceItem.getQty(); // 基準分母
                if (baseQty == null || baseQty == 0)
                    continue; // 防呆: 避免除以零 or NPE

                ReplacementRuleDto ruleDto = new ReplacementRuleDto();
                ruleDto.setRuleid(group.getMrgid());
                ruleDto.setRulename(group.getMrgnb());
                ruleDto.setScopetype(group.getScopetype());
                ruleDto.setScopeval(group.getScopeval());
                ruleDto.setTriggerpart(currentPn); // 觸發者是 Source

                // 隊友：其他 Sources
                List<ReplacementPartRatioDto> partners = allSources.stream()
                        .filter(s -> !s.getMrnb().trim().equals(currentPn))
                        .map(s -> new ReplacementPartRatioDto(s.getMrnb().trim(), s.getQty() / baseQty))
                        .collect(Collectors.toList());
                ruleDto.setPartners(partners);

                // 消耗：所有的 Targets
                List<ReplacementPartRatioDto> targets = allTargets.stream()
                        .map(t -> new ReplacementPartRatioDto(t.getMrnb().trim(), t.getQty() / baseQty))
                        .collect(Collectors.toList());
                ruleDto.setTargets(targets);

                resultMap.computeIfAbsent(currentPn, k -> new ArrayList<>()).add(ruleDto);
            }

            // =================================================================
            // 情境二：反向替代 (Target -> Source)
            // 邏輯：缺新料(Target)，去消耗舊料(Source)
            // 限制：只有 "EQUIVALENT" (雙向) 才可以！
            // =================================================================
            if ("EQUIVALENT".equalsIgnoreCase(policy)) {
                for (MaterialReplacementItem targetItem : allTargets) {
                    String currentPn = targetItem.getMrnb() == null ? "" : targetItem.getMrnb().trim();
                    Double baseQty = targetItem.getQty(); // 基準分母 (這次換 Target 當分母)
                    if (baseQty == null || baseQty == 0)
                        continue; // 防呆: 避免除以零 or NPE

                    ReplacementRuleDto ruleDto = new ReplacementRuleDto();
                    ruleDto.setRuleid(group.getMrgid());
                    String reverseLabel = "en_US".equals(lang) ? " (Reverse)" : " (反向)";
                    ruleDto.setRulename(group.getMrgnb() + reverseLabel);
                    ruleDto.setScopetype(group.getScopetype());
                    ruleDto.setScopeval(group.getScopeval());
                    ruleDto.setTriggerpart(currentPn); // 觸發者是 Target

                    // 隊友：其他 Targets
                    List<ReplacementPartRatioDto> partners = allTargets.stream()
                            .filter(t -> !t.getMrnb().trim().equals(currentPn))
                            .map(t -> new ReplacementPartRatioDto(t.getMrnb().trim(), t.getQty() / baseQty))
                            .collect(Collectors.toList());
                    ruleDto.setPartners(partners);

                    // 消耗：所有的 Sources (Source 變成被消耗品)
                    List<ReplacementPartRatioDto> targets = allSources.stream()
                            .map(s -> new ReplacementPartRatioDto(s.getMrnb().trim(), s.getQty() / baseQty))
                            .collect(Collectors.toList());
                    ruleDto.setTargets(targets);

                    resultMap.computeIfAbsent(currentPn, k -> new ArrayList<>()).add(ruleDto);
                }
            }
        }
        return resultMap;
    }

    // --- Inner DTOs ---

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReplacementRuleDto {
        private Long ruleid;
        private String rulename;
        private Integer scopetype;
        private String scopeval;

        // 讓前端知道這條規則是誰觸發的
        private String triggerpart;

        // 隊友列表 (B料)
        private List<ReplacementPartRatioDto> partners;

        // 目標列表 (C, D料)
        private List<ReplacementPartRatioDto> targets;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReplacementPartRatioDto {
        private String pn; // 料號
        private Double ratio; // 相對比例
    }

}