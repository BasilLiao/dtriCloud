package dtri.com.tw.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import dtri.com.tw.pgsql.dao.MaterialReplacementGroupDao;
import dtri.com.tw.pgsql.dao.MaterialVirtualProjectDao;
import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.dao.WarehouseInventoryDao;
import dtri.com.tw.pgsql.entity.MaterialReplacementGroup;
import dtri.com.tw.pgsql.entity.MaterialReplacementItem;
import dtri.com.tw.pgsql.entity.MaterialVirtualProject;
import dtri.com.tw.pgsql.entity.SystemLanguageCell;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.CloudExceptionService.ErCode;
import dtri.com.tw.shared.CloudExceptionService.ErColor;
import dtri.com.tw.shared.CloudExceptionService.Lan;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;
import jakarta.persistence.EntityManager;

/**
 * @author Allen Chen
 * @description 產品系列主建 - 服務層 (Material Virtual Project Service)
 *              含 BOM 遞迴展開演算法 & 主/副物料交集分類
 */
/** 功能模組: 設置-產品系列主建 (MVP) */
@Service
public class MaterialVirtualProjectServiceAc {

    @Autowired
    private PackageService packageService;
    @Autowired
    private SystemLanguageCellDao languageDao;
    @Autowired
    private MaterialVirtualProjectDao mvpDao;
    @Autowired
    private MaterialReplacementGroupDao replacementGroupDao;
    @Autowired
    private WarehouseInventoryDao warehouseInventoryDao;
    @Autowired
    private EntityManager em;

    private final Gson gson = new Gson();

    // ======================== 1. 取得資料 (Init + Query) ========================
    @Transactional(readOnly = true)
    public PackageBean getData(PackageBean packageBean) throws Exception {
        // 分頁設定 (備用，未來可用於分頁查詢)
        // JsonObject pageSetJson =
        // JsonParser.parseString(packageBean.getSearchPageSet()).getAsJsonObject();
        // int total = pageSetJson.get("total").getAsInt();
        // int batch = pageSetJson.get("batch").getAsInt();
        // PageRequest pageable = PageRequest.of(batch, total, Sort.by(Direction.DESC,
        // "syscdate"));

        // Init 模式
        if (packageBean.getEntityJson() == null || packageBean.getEntityJson().isEmpty()) {
            packageBean.setEntityJson("[]");
            packageBean.setEntityDetailJson("[]");
            // [統一] 使用共用工具注入語系字典與欄位定義
            injectUiLang(packageBean);
            return packageBean;
        }

        // Query 模式
        ArrayList<MaterialVirtualProject> projects = mvpDao.findAllBySysstatusNot(2);
        injectFlags(projects); // [新增] 注入替代料標記
        packageBean.setEntityJson(packageService.beanToJson(projects));

        // [新增] 注入語系字典
        injectUiLang(packageBean);

        return packageBean;
    }

    // [語系字典 + 欄位定義 注入工具]
    private void injectUiLang(PackageBean packageBean) {
        // == Step 1: 載入所有相關語系設定 ==
        // Type 2 = 欄位定義；Type 1 = 標題/按鈕
        ArrayList<SystemLanguageCell> languages = languageDao.findAllByLanguageCellSame("MaterialVirtualProject", null,
                2);
        languages.addAll(languageDao.findAllByLanguageCellSame("MaterialVirtualProject", null, 1));
        languages.addAll(languageDao.findAllByLanguageCellSame("MaterialFront", null, 1));
        languages.addAll(languageDao.findAllByLanguageCellSame("MaterialFront", null, 2));

        // == Step 2: 建立 uiLang (前端通用字典 Key→翻譯)
        Map<String, String> uiLangMap = new HashMap<>();
        // 建立 mapLanguages (resultSet 用的完整欄位元資料) — 僅放 slclass=2 的欄位定義紀錄
        Map<String, SystemLanguageCell> mapLanguages = new HashMap<>();
        languages.forEach(x -> {
            if (x.getSltarget() != null && !x.getSltarget().isEmpty() && x.getSllanguage() != null) {
                uiLangMap.put(x.getSltarget(), x.getSllanguage());
            }
        });
        // DAO 查詢第三參數 slclass=2 的結果即為欄位定義，直接放入 mapLanguages
        languageDao.findAllByLanguageCellSame("MaterialVirtualProject", null, 2)
                .forEach(x -> mapLanguages.put(x.getSltarget(), x));

        // == Step 3: 產出 resultThead (欄位元資料，供前端動態產生表格欄位)
        // 排除大型 JSON 欄位（不適合直接在表格中逐格顯示）
        ArrayList<String> exception = new ArrayList<>(List.of());
        java.lang.reflect.Field[] fields = PackageService.getEntityFields(MaterialVirtualProject.class);
        JsonObject resultThead = packageService.resultSet(fields, exception, mapLanguages);

        // == Step 4: 注入至 searchSet ==
        JsonObject searchSetJson = new JsonObject();
        searchSetJson.add("resultThead", resultThead);
        Gson tempGson = new Gson();
        searchSetJson.add("uiLang", tempGson.toJsonTree(uiLangMap));
        packageBean.setSearchSet(searchSetJson.toString());
    }

    // ======================== 2. 搜尋 ========================
    @Transactional(readOnly = true)
    public PackageBean getSearch(PackageBean packageBean) throws Exception {
        String keyword = "";
        if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().isEmpty()) {
            JsonObject searchJson = JsonParser.parseString(packageBean.getEntityJson()).getAsJsonObject();
            if (searchJson.has("mvpname")) {
                keyword = searchJson.get("mvpname").getAsString();
            }
        }

        ArrayList<MaterialVirtualProject> projects;
        if (keyword.isEmpty()) {
            projects = mvpDao.findAllBySysstatusNot(2);
        } else {
            projects = mvpDao.findAllByKeyword(keyword);
        }
        injectFlags(projects); // [新增] 注入替代料標記
        packageBean.setEntityJson(packageService.beanToJson(projects));

        // [新增] 注入語系字典
        injectUiLang(packageBean);

        return packageBean;
    }

    // ======================== 3. 新增 ========================
    public PackageBean setAdd(PackageBean packageBean) throws Exception {
        MaterialVirtualProject mvp = gson.fromJson(packageBean.getEntityJson(), MaterialVirtualProject.class);
        mvp.setMvpid(null);
        mvp.setSyscdate(new Date());
        mvp.setSyscuser(packageBean.getUserAccount());
        mvp.setSysmdate(new Date());
        mvp.setSysmuser(packageBean.getUserAccount());
        mvp.setSysodate(new Date());
        mvp.setSysouser(packageBean.getUserAccount());
        mvp.setSysheader(false);
        mvp = mvpDao.save(mvp); // 儲存後取回含 ID 的實體
        packageBean.setEntityJson(packageService.beanToJson(mvp)); // 回傳給前端，讓前端取得新的 mvpId
        return packageBean;
    }

    // ======================== 4. 修改 ========================
    public PackageBean setModify(PackageBean packageBean) throws Exception {
        MaterialVirtualProject mvp = gson.fromJson(packageBean.getEntityJson(), MaterialVirtualProject.class);
        MaterialVirtualProject mvpDB = mvpDao.findById(mvp.getMvpid()).orElseThrow(
                () -> new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1000, Lan.zh_TW, null));

        // 保留原有的建立資訊與狀態
        mvp.setSyscdate(mvpDB.getSyscdate());
        mvp.setSyscuser(mvpDB.getSyscuser());
        mvp.setSysstatus(mvpDB.getSysstatus());
        mvp.setSysheader(mvpDB.getSysheader());

        // 更新修改資訊
        mvp.setSysmdate(new Date());
        mvp.setSysmuser(packageBean.getUserAccount());
        mvp.setSysodate(new Date());
        mvp.setSysouser(packageBean.getUserAccount());

        mvpDao.save(mvp);
        return packageBean;
    }

    // ======================== 5. 刪除 ========================
    @SuppressWarnings("null")
    public PackageBean setDelete(PackageBean packageBean) throws Exception {
        MaterialVirtualProject mvp = gson.fromJson(packageBean.getEntityJson(), MaterialVirtualProject.class);
        if (mvp.getMvpid() != null) {
            mvpDao.deleteById(mvp.getMvpid());
        }
        return packageBean;
    }

    /**
     * [輔助] 為專案清單中的物料 JSON 注入 hasReplacement 標記，確保按鈕顯示一致。
     */
    private void injectFlags(List<MaterialVirtualProject> projects) {
        if (projects == null || projects.isEmpty())
            return;

        // [修正] 預取所有具有替代規則的料號，不論是 SOURCE 或 TARGET 角色皆納入
        // 之前只查 role='TARGET'，導致 Sources 看不到替代圖示
        String replSql = "SELECT DISTINCT i.mrnb FROM material_replacement_item i " +
                "JOIN material_replacement_group g ON i.mrg_id = g.mrg_id " +
                "WHERE g.sys_status = 0";
        @SuppressWarnings("unchecked")
        List<String> replItems = em.createNativeQuery(replSql).getResultList();
        java.util.Set<String> replSet = new java.util.HashSet<>(replItems);

        for (MaterialVirtualProject p : projects) {
            p.setMvpmainmaterials(processJson(p.getMvpmainmaterials(), replSet));
            p.setMvpsubmaterials(processJson(p.getMvpsubmaterials(), replSet));
        }
    }

    private String processJson(String json, java.util.Set<String> replSet) {
        if (json == null || json.isEmpty() || json.equals("[]"))
            return json;
        try {
            List<Map<String, Object>> list = gson.fromJson(json, new TypeToken<List<Map<String, Object>>>() {
            }.getType());
            for (Map<String, Object> item : list) {
                String itemNo = (String) item.get("itemno");
                // 更新 hasReplacement 標記
                item.put("hasreplacement", replSet.contains(itemNo));
                // [修正] 保留既有的 _replacementRules 欄位（含使用者勾選狀態 _selected）
                // 之前此欄位在每次 getData/getSearch 時被覆蓋而遺失
                // 現在只在其不存在時才初始化，保留使用者已選的值
                // (前端從 row._replacementRules 讀取，重載後仍可還原勾選)
            }
            return gson.toJson(list);
        } catch (Exception e) {
            return json;
        }
    }

    // ======================== 6. BOM 展開與分析 (核心演算法) ========================
    /**
     * 接收前端傳入的 BOM 清單，遞迴展開並分類主/副物料
     * 
     * @param bomSnList       複數 90BOM 號碼 (動態參數)
     * @param excludePrefixes 要排除的物料前綴 (例如 ["81-"])
     * @param includeSns      排除後仍要強制抓取的物料號
     * @param maxDepth        最大展開階層數 (預設 5)
     * @return Map 包含 "mainMaterials" 和 "subMaterials" 兩個 List
     */
    @Transactional(readOnly = true)
    public Map<String, Object> analyzeBom(List<String> bomSnList, List<String> excludePrefixes,
            List<String> includeSns, List<String> warehouses, int maxDepth) {

        int bomCount = bomSnList.size();
        // [修正] 強制抓取名單標準化 (Trim + UpperCase)，避免大小寫或空白導致比對失敗
        java.util.Set<String> includeSet = new java.util.HashSet<>();
        if (includeSns != null) {
            for (String s : includeSns) {
                if (s != null)
                    includeSet.add(s.trim().toUpperCase());
            }
        }
        if (bomCount == 0 && includeSet.isEmpty()) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("mainMaterials", new ArrayList<>());
            empty.put("subMaterials", new ArrayList<>());
            return empty;
        }

        // 使用遞迴 CTE 展開 BOM (SQL 邏輯已搬移至 DAO 層)
        List<Object[]> rowsData = mvpDao.analyzeBomTree(bomSnList, maxDepth);
        List<Object[]> rows = new java.util.ArrayList<>(rowsData);

        // [新增] 預取所有具有替代規則的目標料號 (以 HashSet 加速比對)
        String replSql = "SELECT DISTINCT i.mrnb FROM material_replacement_item i " +
                "JOIN material_replacement_group g ON i.mrg_id = g.mrg_id " +
                "WHERE i.role = 'TARGET' AND g.sys_status = 0";
        @SuppressWarnings("unchecked")
        List<String> replItems = em.createNativeQuery(replSql).getResultList();
        java.util.Set<String> replSet = new java.util.HashSet<>(replItems);

        // [新增] 處理不在 BOM 階層內的 "特定物料" (includeSns)
        // 這些物料是使用者手動點選加入的，即使不在 90BOM 展開後的清單中，也應參與模擬
        if (!includeSet.isEmpty()) {
            java.util.Set<String> existingInBom = new java.util.HashSet<>();
            for (Object[] row : rows) {
                existingInBom.add(((String) row[0]).trim());
            }

            List<String> missingSns = new java.util.ArrayList<>();
            for (String sn : includeSns) {
                if (!existingInBom.contains(sn.trim())) {
                    missingSns.add(sn.trim());
                }
            }

            if (!missingSns.isEmpty()) {
                // [優化] 從物料主檔抓取這些外部物料的基本資訊 (Name/Spec)，且同步轉大寫
                List<Object[]> mRows = mvpDao.getMissingMaterialsInfo(missingSns);

                java.util.Set<String> added = new java.util.HashSet<>();
                for (Object[] mRow : mRows) {
                    String sn = ((String) mRow[0]).trim();
                    if (!added.contains(sn)) {
                        added.add(sn);
                        // 轉換為與 BOM 展開結果相同的格式: [itemno, itemname, itemspec, inbomcount, totalqty,
                        // mindepth, isleaf]
                        // inbomcount=0 (不在BOM內), totalqty=1 (手動加入預設數量為1), mindepth=0, isleaf=true
                        rows.add(new Object[] { mRow[0], mRow[1], mRow[2], 0, 1, 0, true });
                    }
                }
            }
        }

        // [新增] 收集所有未被排除的料號，準備批次查詢真實庫存
        List<String> validItemNos = new ArrayList<>();
        for (Object[] row : rows) {
            String itemNo = ((String) row[0]).trim();
            boolean excluded = false;
            if (excludePrefixes != null) {
                for (String prefix : excludePrefixes) {
                    if (itemNo.startsWith(prefix)) {
                        excluded = true;
                        break;
                    }
                }
            }
            // 如果是手動加入的料號 (includeSet)，不論是否匹配排除規則，都強制取消排除
            // [修正] 轉大寫後比對 includeSet
            if (excluded && includeSet.contains(itemNo.toUpperCase())) {
                excluded = false;
            }
            // [新增過濾]：如果料號就是當前選擇的「生產標的 (Root BOM)」，則絕對不列入零件清單，避免自己包含自己
            if (bomSnList.contains(itemNo)) {
                excluded = true;
            }

            if (!excluded) {
                validItemNos.add(itemNo);
            }
        }

        // 預設有效倉別 (如果傳入 null 則報錯)
        if (warehouses == null || warehouses.isEmpty()) {
            warehouses = java.util.Collections.singletonList("__NONE__");
        }
        List<String> activeWhs = warehouses;

        Map<String, Double> stockMap = new HashMap<>();
        if (!validItemNos.isEmpty()) {
            int chunkSize = 1000;
            for (int i = 0; i < validItemNos.size(); i += chunkSize) {
                List<String> chunk = validItemNos.subList(i, Math.min(validItemNos.size(), i + chunkSize));
                // 使用 UPPER(TRIM) 確保精準匹配，並加入 [倉別過濾]
                List<Object[]> stockRows = mvpDao.getInventoryQuantities(chunk, activeWhs);
                for (Object[] sr : stockRows) {
                    stockMap.put((String) sr[0], ((Number) sr[1]).doubleValue());
                }
            }
        }

        // 分類：主物料 vs 副物料
        List<Map<String, Object>> mainMaterials = new ArrayList<>();
        List<Map<String, Object>> subMaterials = new ArrayList<>();

        for (Object[] row : rows) {
            String itemNo = ((String) row[0]).trim();
            String itemName = (row[1] != null && !row[1].toString().isEmpty()) ? row[1].toString() : "未知物料";
            String itemSpec = (row[2] != null) ? row[2].toString() : "";
            int inBomCount = ((Number) row[3]).intValue();
            int totalQty = ((Number) row[4]).intValue();
            int minDepth = ((Number) row[5]).intValue();
            boolean isLeaf = (Boolean) row[6];

            if (!validItemNos.contains(itemNo)) {
                continue;
            }

            Map<String, Object> item = new HashMap<>();
            item.put("itemno", itemNo);
            item.put("itemname", itemName);
            item.put("itemspec", itemSpec);
            item.put("inbomcount", inBomCount);
            item.put("totalqty", totalQty);
            item.put("mindepth", minDepth);
            item.put("isleaf", isLeaf);

            // [再次確認] 排除當前選擇的目標成品
            if (bomSnList.contains(itemNo)) {
                continue;
            }

            item.put("hasreplacement", replSet.contains(itemNo));
            // 查表時也轉大寫
            item.put("currentstock", stockMap.getOrDefault(itemNo.toUpperCase(), 0.0));

            // 分類邏輯：
            // 1. 出現次數 >= BOM 總數 (代表每個BOM都有) -> 主物料 (Main)
            // 2. 其餘情況（包含手動加入、或是只出現在部分 BOM 內的料號） -> 副物料 (Sub)
            if (inBomCount >= bomCount && bomCount > 0) {
                mainMaterials.add(item);
            } else {
                subMaterials.add(item);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("mainmaterials", mainMaterials);
        result.put("submaterials", subMaterials);
        result.put("bomcount", bomCount);
        result.put("maincount", mainMaterials.size());
        result.put("subcount", subMaterials.size());
        return result;
    }

    // ======================== 7-2. 取得全域 BOM 與料號字典 (供前端 Client-side Caching 搜尋使用)
    // ========================
    @Transactional(readOnly = true)
    public PackageBean getInitSearchData(PackageBean packageBean) throws Exception {
        Map<String, Object> finalResult = new HashMap<>();

        // 1. 取得所有獨立的 BOM
        String sqlBoms = "SELECT DISTINCT TRIM(bbi_sn) AS bbisn, TRIM(bbi_name) AS bbiname, TRIM(bbi_specification) AS bbispec "
                + "FROM basic_bom_ingredients "
                + "WHERE bbi_sn IS NOT NULL AND TRIM(bbi_sn) != '' "
                + "ORDER BY bbisn";
        jakarta.persistence.Query queryBoms = em.createNativeQuery(sqlBoms);
        @SuppressWarnings("unchecked")
        List<Object[]> rowsBoms = queryBoms.getResultList();

        List<Map<String, String>> allBoms = new ArrayList<>(rowsBoms.size());
        for (Object[] row : rowsBoms) {
            Map<String, String> item = new HashMap<>();
            item.put("bbisn", row[0] != null ? row[0].toString() : "");
            item.put("bbiname", row[1] != null ? row[1].toString() : "");
            item.put("bbispec", row[2] != null ? row[2].toString() : "");
            allBoms.add(item);
        }
        finalResult.put("allboms", allBoms);

        // 2. 取得所有獨立的料號 (改從物料主檔 warehouse_material 抓取，排除 BOM 標題等雜訊)
        String sqlItems = "SELECT TRIM(wm_p_nb) AS itemno, TRIM(wm_name) AS itemname, TRIM(wm_specification) AS itemspec "
                + "FROM warehouse_material "
                + "WHERE sys_status = 0 "
                + "ORDER BY wm_p_nb";
        jakarta.persistence.Query queryItems = em.createNativeQuery(sqlItems);
        @SuppressWarnings("unchecked")
        List<Object[]> rowsItems = queryItems.getResultList();

        List<Map<String, String>> allItems = new ArrayList<>(rowsItems.size());
        for (Object[] row : rowsItems) {
            Map<String, String> item = new HashMap<>();
            item.put("itemno", row[0] != null ? row[0].toString() : "");
            item.put("itemname", row[1] != null ? row[1].toString() : "");
            item.put("itemspec", row[2] != null ? row[2].toString() : "");
            allItems.add(item);
        }
        finalResult.put("allitems", allItems);

        // 3. 取得所有獨立的倉別 (用於過濾)
        String sqlWhs = "SELECT DISTINCT wa_alias, wa_a_name FROM warehouse_area WHERE wa_alias IS NOT NULL AND wa_alias != '' ORDER BY wa_alias";
        jakarta.persistence.Query queryWhs = em.createNativeQuery(sqlWhs);
        @SuppressWarnings("unchecked")
        List<Object[]> rowsWhs = queryWhs.getResultList();
        List<Map<String, String>> allWarehouses = new ArrayList<>(rowsWhs.size());
        for (Object[] row : rowsWhs) {
            Map<String, String> item = new HashMap<>();
            item.put("alias", row[0] != null ? row[0].toString() : "");
            item.put("name", row[1] != null ? row[1].toString() : "");
            allWarehouses.add(item);
        }
        finalResult.put("allwarehouses", allWarehouses);

        packageBean.setEntityJson(packageService.beanToJson(finalResult));

        // [新增] 注入語系字典
        injectUiLang(packageBean);

        return packageBean;
    }

    @Transactional(readOnly = true)
    public PackageBean getReport(PackageBean packageBean) throws Exception {
        JsonObject reqJson = JsonParser.parseString(packageBean.getEntityJson()).getAsJsonObject();

        // 解析前端傳入的參數
        List<String> bomSnList = gson.fromJson(reqJson.get("bomList"), new TypeToken<List<String>>() {
        }.getType());
        List<String> excludePrefixes = reqJson.has("excludePrefixes") && !reqJson.get("excludePrefixes").isJsonNull()
                ? gson.fromJson(reqJson.get("excludePrefixes"), new TypeToken<List<String>>() {
                }.getType())
                : null;
        List<String> includeSns = reqJson.has("includeSns") && !reqJson.get("includeSns").isJsonNull()
                ? gson.fromJson(reqJson.get("includeSns"), new TypeToken<List<String>>() {
                }.getType())
                : null;

        int maxDepth = reqJson.has("maxDepth") && !reqJson.get("maxDepth").isJsonNull()
                ? reqJson.get("maxDepth").getAsInt()
                : 5;

        if (!reqJson.has("warehouses") || reqJson.get("warehouses").isJsonNull()) {
            throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1003, Lan.zh_TW,
                    new String[] { "缺少參數：請選擇計算倉別！" });
        }
        List<String> warehouses = new ArrayList<>();
        JsonArray whAry = reqJson.getAsJsonArray("warehouses");
        for (JsonElement we : whAry) {
            if (we.isJsonObject()) {
                warehouses.add(we.getAsJsonObject().get("value").getAsString());
            } else if (we.isJsonPrimitive()) {
                warehouses.add(we.getAsString());
            }
        }

        // 執行 BOM 展開分析
        Map<String, Object> analysisResult = analyzeBom(bomSnList, excludePrefixes, includeSns, warehouses, maxDepth);

        // 回傳結果
        packageBean.setEntityJson(packageService.beanToJson(analysisResult));
        return packageBean;
    }

    // ======================== 8-1. 取得其他使用者專案清單 (輕量版，不含大 JSON)
    // ========================
    @Transactional(readOnly = true)
    public PackageBean getOtherProjects(PackageBean packageBean) throws Exception {
        JsonObject reqJson = JsonParser.parseString(packageBean.getEntityJson()).getAsJsonObject();
        Long currentMvpId = reqJson.has("currentmvpid") && !reqJson.get("currentmvpid").isJsonNull()
                ? reqJson.get("currentmvpid").getAsLong()
                : 0L;

        ArrayList<MaterialVirtualProject> projects = mvpDao.findOtherProjects(currentMvpId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (MaterialVirtualProject p : projects) {
            Map<String, Object> summary = new HashMap<>();
            summary.put("mvpid", p.getMvpid());
            summary.put("mvpname", p.getMvpname());
            summary.put("creator", p.getSyscuser());
            summary.put("moddate", p.getSysmdate() != null ? p.getSysmdate().toString() : "");
            // 解析 simTime 取出 virtualQty / virtualDate
            String simTimeStr = p.getMvpsimtime();
            int vQty = 0;
            String vDate = "";
            if (simTimeStr != null && !simTimeStr.isEmpty() && !simTimeStr.equals("{}")) {
                try {
                    JsonObject st = JsonParser.parseString(simTimeStr).getAsJsonObject();
                    vQty = st.has("virtualqty") ? st.get("virtualqty").getAsInt() : 0;
                    vDate = st.has("virtualdate") ? st.get("virtualdate").getAsString() : "";
                } catch (Exception ignored) {
                }
            }
            summary.put("virtualqty", vQty);
            summary.put("virtualdate", vDate);
            result.add(summary);
        }
        packageBean.setEntityJson(packageService.beanToJson(result));
        return packageBean;
    }

    // ======================== 9. 齊套模擬引擎 API (供 Controller 呼叫)
    // ========================
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    /**
     * 暫存模擬分析用之上下文物件
     */
    private static class SimContext {
        int virtualqty = 0;
        String virtualdate = "2099-12-31";
        List<Map<String, Object>> allItems = new ArrayList<>();
        Map<String, Double> extDemandByItem = new java.util.HashMap<>();
        Map<String, List<Map<String, Object>>> extDemandDetailByItem = new java.util.HashMap<>();
        // 替代料：{ targetItemNo -> { ruleId, mrAction, sources:[{mrnb,qty}],
        // targets:[{mrnb,qty}] } }
        List<Map<String, Object>> selectedReplacementRules = new ArrayList<>();
        List<String> selectedWarehouses = new ArrayList<>();
        // [新增] 是否啟用替代料計算全域開關 (對應前端 mvp_use_replacement checkbox)
        boolean useReplacement = false;
    }

    /**
     * 執行虛擬專案物料缺口模擬計算。
     * (已優化：提取私有方法、解決 N+1 查詢效能問題)
     *
     * @param packageBean 前端傳遞的 Request 本體
     * @return 包含模擬細節、BOM 樹結構與最終瓶頸結果的 PackageBean
     */
    public PackageBean simulateMvp(PackageBean packageBean) throws Exception {
        JsonObject reqJson = JsonParser.parseString(packageBean.getEntityJson()).getAsJsonObject();

        // 1. 解析前端參數
        SimContext ctx = parseSimulationRequest(reqJson);
        if (ctx.virtualqty <= 0) {
            throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1008, Lan.en_US,
                    new String[] { "Effective target quantity [virtualqty] not set" });
        }
        if (ctx.allItems.isEmpty()) {
            throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1003, Lan.en_US,
                    new String[] { "No materials in project, simulation cannot proceed" });
        }

        // 2. 提取前端選定的倉別 (必須在 SQL 執行前設定)
        if (!reqJson.has("warehouses")) {
            throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1003, Lan.en_US,
                    new String[] { "Missing parameter: warehouses" });
        }
        List<String> selectedWarehouses = new ArrayList<>();
        JsonArray whAry = reqJson.getAsJsonArray("warehouses");
        for (JsonElement we : whAry) {
            selectedWarehouses.add(we.getAsString());
        }

        // 如果空陣列傳入 SQL IN 可能會報錯或撈到全部，改傳入 dummy 值 "__NONE__"
        if (selectedWarehouses.isEmpty()) {
            selectedWarehouses = java.util.Collections.singletonList("__NONE__");
        }
        ctx.selectedWarehouses = selectedWarehouses;

        // 提取 mvpsimorders 並預先獲取 extEntities，供 DAO 處理外部需求競爭
        String otherProjectsStr = reqJson.has("mvpsimorders") && !reqJson.get("mvpsimorders").isJsonNull()
                ? reqJson.get("mvpsimorders").getAsString()
                : "[]";
        List<Map<String, Object>> otherProjects = gson.fromJson(otherProjectsStr,
                new TypeToken<List<Map<String, Object>>>() {
                }.getType());
        List<MaterialVirtualProject> extEntities = new ArrayList<>();
        if (otherProjects != null && !otherProjects.isEmpty()) {
            List<Long> extMvpIds = new ArrayList<>();
            for (Map<String, Object> extProj : otherProjects) {
                Long extMvpId = extProj.get("mvpid") instanceof Number ? ((Number) extProj.get("mvpid")).longValue()
                        : 0L;
                int extQty = extProj.get("virtualqty") instanceof Number
                        ? ((Number) extProj.get("virtualqty")).intValue()
                        : 0;
                if (extMvpId > 0 && extQty > 0)
                    extMvpIds.add(extMvpId);
            }
            if (!extMvpIds.isEmpty()) {
                extEntities = mvpDao.findAllById(extMvpIds);
            }
        }

        // 3 & 4. 執行 SQL 並打平資料結構 (原本在 Service 內的 SQL 邏輯已搬移至 DaoImpl)
        List<Object[]> rawSimRows = mvpDao.executeSimulationSql(
                ctx.virtualdate, ctx.virtualqty, ctx.selectedWarehouses,
                ctx.allItems, otherProjects, extEntities,
                ctx.extDemandByItem, ctx.extDemandDetailByItem);
        List<Map<String, Object>> simResults = mapSimResults(rawSimRows, ctx);

        // 5. 執行母件庫存沖銷 (Netting Logic) - 使用批次查詢解決 N+1
        applyNettingLogic(simResults, ctx.virtualqty);

        // 4.5 執行替代料沖銷 (Replacement Netting - 第二階段)
        applyReplacementLogic(simResults, ctx);

        // 5. 整理 BOM 樹狀結構用於前端顯示
        List<Map<String, Object>> bomEdges = buildBomEdges(simResults);

        // 6. 計算專案最終瓶頸 (Max Sets)
        int minMaxSets = Integer.MAX_VALUE;
        int minMaxSetsPhysical = Integer.MAX_VALUE;
        for (Map<String, Object> simRow : simResults) {
            int maxSetsVal = ((Number) simRow.getOrDefault("maxsets", Integer.MAX_VALUE)).intValue();
            int maxSetsPhyVal = ((Number) simRow.getOrDefault("maxsetsphysical", Integer.MAX_VALUE)).intValue();
            if (maxSetsVal < minMaxSets)
                minMaxSets = maxSetsVal;
            if (maxSetsPhyVal < minMaxSetsPhysical)
                minMaxSetsPhysical = maxSetsPhyVal;
        }

        // 7. 回傳結果
        Map<String, Object> finalResponse = new java.util.HashMap<>();
        finalResponse.put("simdetails", simResults);
        finalResponse.put("bomedges", bomEdges);
        finalResponse.put("projectmaxsets", minMaxSets == Integer.MAX_VALUE ? ctx.virtualqty : minMaxSets);
        finalResponse.put("projectmaxsetsphysical",
                minMaxSetsPhysical == Integer.MAX_VALUE ? ctx.virtualqty : minMaxSetsPhysical);
        finalResponse.put("virtualqty", ctx.virtualqty);
        finalResponse.put("virtualdate", ctx.virtualdate);

        packageBean.setEntityJson(packageService.beanToJson(finalResponse));
        return packageBean;
    }

    /**
     * 步驟 1：解析 JSON 參數，整理出所有參與模擬的物料清單。
     */
    private SimContext parseSimulationRequest(JsonObject reqJson) {
        SimContext ctx = new SimContext();

        String mvpMainStr = reqJson.has("mvpmainmaterials") && !reqJson.get("mvpmainmaterials").isJsonNull()
                ? reqJson.get("mvpmainmaterials").getAsString()
                : "[]";
        String mvpSubStr = reqJson.has("mvpsubmaterials") && !reqJson.get("mvpsubmaterials").isJsonNull()
                ? reqJson.get("mvpsubmaterials").getAsString()
                : "[]";
        String mvpSimTimeStr = reqJson.has("mvpsimtime") && !reqJson.get("mvpsimtime").isJsonNull()
                ? reqJson.get("mvpsimtime").getAsString()
                : "{}";

        List<Map<String, Object>> mainMaterials = gson.fromJson(mvpMainStr, new TypeToken<List<Map<String, Object>>>() {
        }.getType());
        List<Map<String, Object>> subMaterials = gson.fromJson(mvpSubStr, new TypeToken<List<Map<String, Object>>>() {
        }.getType());
        JsonObject simTime = mvpSimTimeStr != null && !mvpSimTimeStr.isEmpty() && !mvpSimTimeStr.equals("{}")
                ? JsonParser.parseString(mvpSimTimeStr).getAsJsonObject()
                : new JsonObject();

        ctx.virtualqty = simTime.has("virtualqty") ? simTime.get("virtualqty").getAsInt() : 0;
        ctx.virtualdate = simTime.has("virtualdate") && !simTime.get("virtualdate").getAsString().isEmpty()
                ? simTime.get("virtualdate").getAsString()
                : "2099-12-31";

        if (mainMaterials != null) {
            mainMaterials.forEach(m -> {
                m.put("itemtype", "主料");
                m.put("itemcategory", (String) m.getOrDefault("category", ""));
            });
            ctx.allItems.addAll(mainMaterials);
        }
        if (subMaterials != null) {
            subMaterials.forEach(m -> {
                m.put("itemtype", "副料");
                m.put("itemcategory", (String) m.getOrDefault("category", ""));
            });
            ctx.allItems.addAll(subMaterials);
        }

        // 解析前端選定的替代料規則 [ { mrgId, targetItemNo } ]
        String replacementRulesStr = reqJson.has("selectedreplacementrules")
                && !reqJson.get("selectedreplacementrules").isJsonNull()
                        ? reqJson.get("selectedreplacementrules").getAsString()
                        : "[]";
        if (!replacementRulesStr.isEmpty() && !replacementRulesStr.equals("[]")) {
            JsonArray rulesArr = JsonParser.parseString(replacementRulesStr).getAsJsonArray();
            for (JsonElement el : rulesArr) {
                JsonObject ruleObj = el.getAsJsonObject();
                Map<String, Object> rule = new java.util.HashMap<>();
                rule.put("mrgid", ruleObj.has("mrgid") ? ruleObj.get("mrgid").getAsLong() : null);
                rule.put("targetitemno", ruleObj.has("targetitemno") ? ruleObj.get("targetitemno").getAsString() : "");
                ctx.selectedReplacementRules.add(rule);
            }
        }

        // [新增] 解析全域開關：mvpUseReplacement (前端 checkbox 傳入)
        ctx.useReplacement = reqJson.has("mvpusereplacement")
                && !reqJson.get("mvpusereplacement").isJsonNull()
                && reqJson.get("mvpusereplacement").getAsBoolean();

        return ctx;
    }

    // [提示]: buildSimulationSql, buildMvpItemsCte, buildCustomOrdersSql,
    // buildExternalDemandSql
    // 已整體遷移到 MaterialVirtualProjectDaoImpl.java 中，以遵循架構分層並避免 SQL 污染業務邏輯層。

    /**
     * 步驟 3 & 4：將 SQL 結果映射到 Java Map 中，組合基本物料屬性。
     */
    private List<Map<String, Object>> mapSimResults(List<Object[]> rows, SimContext ctx) {
        List<Map<String, Object>> simResults = new ArrayList<>();

        for (Object[] row : rows) {
            String itemNo = (String) row[0];
            double qtyMultiplier = ((Number) row[1]).doubleValue(); // 1.0 default
            double bomUnitQty = ((Number) row[2]).doubleValue(); // 35.0 original
            double balanceBefore = ((Number) row[3]).doubleValue();
            double virtualDemand = ((Number) row[4]).doubleValue();
            double balanceAfter = ((Number) row[5]).doubleValue();
            int delayDays = ((Number) row[6]).intValue();
            int maxSets = ((Number) row[7]).intValue();
            double currentStock = ((Number) row[8]).doubleValue();
            int maxSetsPhysical = ((Number) row[9]).intValue();

            // 從 ctx 同步品名規格等基礎屬性
            String itemName = "";
            String itemSpec = "";
            String itemType = "";
            String itemCategory = "";
            boolean isLeaf = true;
            int minDepth = 99;
            for (Map<String, Object> mat : ctx.allItems) {
                if (((String) mat.get("itemno")).trim().equals(itemNo.trim())) {
                    itemName = (String) mat.getOrDefault("itemname", "");
                    itemSpec = (String) mat.getOrDefault("itemspec", "");
                    itemType = (String) mat.getOrDefault("itemtype", "");
                    itemCategory = (String) mat.getOrDefault("itemcategory", "");
                    isLeaf = (Boolean) mat.getOrDefault("isleaf", true);
                    minDepth = mat.get("mindepth") instanceof Number ? ((Number) mat.get("mindepth")).intValue() : 99;
                    break;
                }
            }

            Map<String, Object> simRow = new java.util.HashMap<>();
            simRow.put("itemno", itemNo);
            simRow.put("itemname", itemName);
            simRow.put("itemspec", itemSpec);
            simRow.put("itemtype", itemType);
            simRow.put("itemcategory", itemCategory);
            simRow.put("qtymultiplier", qtyMultiplier);
            simRow.put("bomunitqty", bomUnitQty);
            simRow.put("qtyperset", bomUnitQty); // Keep for UI display of unit qty
            simRow.put("balancebefore", balanceBefore);
            simRow.put("virtualdemand", virtualDemand);
            simRow.put("balanceafter", balanceAfter);
            simRow.put("delaydays", delayDays);
            simRow.put("maxsets", maxSets);
            simRow.put("currentstock", currentStock);
            simRow.put("maxsetsphysical", maxSetsPhysical);
            simRow.put("shortageqty", balanceAfter < 0 ? Math.abs(balanceAfter) : 0);
            simRow.put("isleaf", isLeaf);
            simRow.put("mindepth", minDepth);
            simRow.put("extdemandqty", ctx.extDemandByItem.getOrDefault(itemNo, 0.0));
            simRow.put("extdemanddetails", ctx.extDemandDetailByItem.getOrDefault(itemNo, new ArrayList<>()));
            simRow.put("replacementsupply", 0.0);
            simRow.put("replacementconsumed", 0.0);
            simRow.put("replacementlogs", "");
            simRow.put("replacementtrace", new ArrayList<>());

            simResults.add(simRow);
        }
        return simResults;
    }

    /**
     * 步驟 4：執行母件庫存沖銷 (Netting Logic)。
     * 若某組件(非底層)有庫存，其子件的虛擬需求應等比例減少。
     * (已優化：引入分批次查詢 Chunking，徹底解決迴圈 N+1 且防止資料庫 IN 上限爆發)
     */
    private void applyNettingLogic(List<Map<String, Object>> simResults, int virtualqty) {
        // 1. 過濾出有需要沖銷的父層組件
        List<Map<String, Object>> parentCandidates = new ArrayList<>();
        List<String> validParentItemSos = new ArrayList<>();

        for (Map<String, Object> simRow : simResults) {
            boolean rowIsLeaf = (Boolean) simRow.getOrDefault("isleaf", true);
            if (rowIsLeaf)
                continue; // 底層零件不需要沖銷

            double parentStock = ((Number) simRow.getOrDefault("currentstock", 0.0)).doubleValue();
            double parentQtyPerSet = ((Number) simRow.getOrDefault("qtyperset", 0.0)).doubleValue();
            if (parentStock <= 0 || parentQtyPerSet <= 0)
                continue;

            parentCandidates.add(simRow);
            validParentItemSos.add((String) simRow.get("itemno"));
        }

        if (validParentItemSos.isEmpty())
            return;

        // 按 minDepth 由淺到深排序，確保先處理上層組件
        parentCandidates.sort((a, b) -> {
            int da = a.get("mindepth") instanceof Number ? ((Number) a.get("mindepth")).intValue() : 99;
            int db = b.get("mindepth") instanceof Number ? ((Number) b.get("mindepth")).intValue() : 99;
            return Integer.compare(da, db);
        });

        // 2. 批次抓取父子關係 (防止 IN > 1000 回報錯誤, 這裡手動每 500 個切一刀)
        Map<String, List<Object[]>> bomHierarchyMap = new java.util.HashMap<>();
        int chunkSize = 500;
        for (int i = 0; i < validParentItemSos.size(); i += chunkSize) {
            List<String> chunk = validParentItemSos.subList(i, Math.min(validParentItemSos.size(), i + chunkSize));
            List<Object[]> childRows = mvpDao.getBomChildren(chunk);
            for (Object[] row : childRows) {
                String pSn = (String) row[0];
                bomHierarchyMap.computeIfAbsent(pSn, k -> new ArrayList<>()).add(row);
            }
        }

        // 3. 開始計算沖銷
        for (Map<String, Object> parentRow : parentCandidates) {
            String parentItemNo = (String) parentRow.get("itemno");
            double parentStock = ((Number) parentRow.getOrDefault("currentstock", 0.0)).doubleValue();
            double parentQtyPerSet = ((Number) parentRow.getOrDefault("qtyperset", 0.0)).doubleValue();

            int coverableSets = (int) Math.floor(parentStock / parentQtyPerSet);
            if (coverableSets <= 0)
                continue;
            if (coverableSets > virtualqty)
                coverableSets = virtualqty;

            List<Object[]> children = bomHierarchyMap.getOrDefault(parentItemNo.trim(),
                    java.util.Collections.emptyList());
            for (Object[] childData : children) {
                String childItemNo = (String) childData[1];
                double childQtyPerUnit = ((Number) childData[2]).doubleValue();
                double reduction = coverableSets * childQtyPerUnit;

                for (Map<String, Object> childSim : simResults) {
                    if (childItemNo.equals(((String) childSim.get("itemno")).trim())) {
                        double origDemand = ((Number) childSim.getOrDefault("virtualdemand", 0.0)).doubleValue();
                        double newDemand = Math.max(0, origDemand - reduction);
                        double origBalance = ((Number) childSim.getOrDefault("balanceafter", 0.0)).doubleValue();
                        double newBalance = origBalance + (origDemand - newDemand);

                        childSim.put("demandbeforenetting", origDemand);
                        childSim.put("virtualdemand", newDemand);
                        childSim.put("balanceafter", newBalance);
                        childSim.put("shortageqty", newBalance < 0 ? Math.abs(newBalance) : 0);

                        double childQtyPerSet = ((Number) childSim.getOrDefault("qtyperset", 0.0)).doubleValue();
                        double childBalanceBefore = ((Number) childSim.getOrDefault("balancebefore", 0.0))
                                .doubleValue();
                        if (childQtyPerSet > 0) {
                            int newMaxSets = childBalanceBefore <= 0 ? 0
                                    : (int) Math.floor(childBalanceBefore / childQtyPerSet);
                            childSim.put("maxsets", newMaxSets);
                        }
                        break;
                    }
                }
            }
            parentRow.put("nettedsets", coverableSets);
        }
    }

    /**
     * 動態加載外部即時庫存
     */
    private double fetchExternalInventory(String itemNo, List<String> warehouses) {
        if (itemNo == null || itemNo.isEmpty() || warehouses == null || warehouses.isEmpty())
            return 0.0;
        try {
            List<dtri.com.tw.pgsql.entity.WarehouseInventory> detailList = warehouseInventoryDao
                    .findAllByWiwmpnbAndWiwaaliasIn(itemNo.trim(), warehouses);
            double total = 0;
            for (dtri.com.tw.pgsql.entity.WarehouseInventory wi : detailList) {
                total += (wi.getWinqty() != null ? wi.getWinqty() : 0);
                total += (wi.getWitqty() != null ? wi.getWitqty() : 0); // 包含待驗
            }
            return total;
        } catch (Exception e) {
            System.err
                    .println("[Virtual Project] Fetch external inventory failed for " + itemNo + ": " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * 步驟 4.5：執行替代料沖銷 (Two-Pass Replacement Netting)。
     *
     * 策略驅動的雙向 N:N 替代料對沖引擎
     *
     * 舊邏輯：硬寫「Sources 救 Targets」，無法處理反向（Target 缺料借用 Source）
     * 新邏輯：動態識別觸發者 (triggerItemNo) 的角色 (SOURCE/TARGET)，
     * 再根據策略 (EQUIVALENT/RUN_DOWN) 決定可用的救援方向：
     * - 觸發者是 SOURCE -> 永遠可以借用 TARGETS (正向，兩種策略皆支援)
     * - 觸發者是 TARGET -> 只有 EQUIVALENT 策略才可以反向借用 SOURCES
     * - RUN_DOWN 策略：只有 SOURCE 觸發時才能起動，TARGET 不能反向借用
     *
     * N:N 比例計算：ratio = helper.qty / trigger.qty
     */
    private void applyReplacementLogic(List<Map<String, Object>> simResults, SimContext ctx) {
        // [區塊 0]：全域開關防呆 — 若使用者未勾選「啟用替代料計算」，直接跳過整個沖銷邏輯
        if (!ctx.useReplacement)
            return;

        // [區塊 1]：基本防呆，若無選擇任何規則則直接返回
        if (ctx.selectedReplacementRules.isEmpty())
            return;

        // [區塊 2]：建立 itemno -> simRow 快速查找表 (本專案 BOM 表內物料)
        Map<String, Map<String, Object>> simMap = new java.util.HashMap<>();
        for (Map<String, Object> row : simResults) {
            simMap.put(((String) row.get("itemno")).trim(), row);
        }

        // [區塊 3]：建立外部庫存緩存表 (不在 BOM 表內的 Helpers 或 Partners)
        Map<String, Double> extInvMap = new java.util.HashMap<>();

        // [區塊 4]：預先批次載入所有需要的規則群組，避免迴圈中的 N+1 DB 查詢
        java.util.Set<Long> neededMrgIds = new java.util.HashSet<>();
        for (Map<String, Object> ruleRef : ctx.selectedReplacementRules) {
            Long mrgId = ruleRef.get("mrgid") != null ? ((Number) ruleRef.get("mrgid")).longValue() : null;
            if (mrgId != null)
                neededMrgIds.add(mrgId);
        }
        Map<Long, MaterialReplacementGroup> groupCache = new java.util.HashMap<>();
        if (!neededMrgIds.isEmpty()) {
            replacementGroupDao.findAllById(neededMrgIds)
                    .forEach(g -> groupCache.put(g.getMrgid(), g));
        }

        // [區塊 5]：遍歷使用者選擇的替代規則
        for (Map<String, Object> ruleRef : ctx.selectedReplacementRules) {
            Long mrgId = ruleRef.get("mrgid") != null ? ((Number) ruleRef.get("mrgid")).longValue() : null;
            String triggerItemNo = ((String) ruleRef.getOrDefault("targetitemno", "")).trim();
            if (mrgId == null || triggerItemNo.isEmpty())
                continue;

            // [區塊 6]：從快取取得規則詳情並過濾無效規則
            MaterialReplacementGroup group = groupCache.get(mrgId);
            if (group == null)
                continue;
            String mrgName = group.getMrgnb() != null ? group.getMrgnb() : "Rule #" + mrgId;

            List<MaterialReplacementItem> sourcesInGroup = group.getItems().stream()
                    .filter(i -> "SOURCE".equals(i.getRole()))
                    .collect(java.util.stream.Collectors.toList());
            List<MaterialReplacementItem> targetsInGroup = group.getItems().stream()
                    .filter(i -> "TARGET".equals(i.getRole()))
                    .collect(java.util.stream.Collectors.toList());

            if (sourcesInGroup.isEmpty() || targetsInGroup.isEmpty())
                continue;

            // [區塊 7]：策略與角色辨識 (辨識是 Source 缺料還是 Target 缺料)
            // DB 儲存值：EQUIVALENT = 雙向替代；RUN_DOWN = 單向取代
            String policy = group.getPolicy() != null ? group.getPolicy().toUpperCase() : "RUN_DOWN";

            // ===== 動態角色識別 =====
            boolean triggerIsSource = sourcesInGroup.stream()
                    .anyMatch(i -> triggerItemNo.equals(i.getMrnb() == null ? "" : i.getMrnb().trim()));
            boolean triggerIsTarget = targetsInGroup.stream()
                    .anyMatch(i -> triggerItemNo.equals(i.getMrnb() == null ? "" : i.getMrnb().trim()));

            MaterialReplacementItem triggerItem = null;
            List<MaterialReplacementItem> helperItems;
            List<MaterialReplacementItem> partnerItems = new ArrayList<>();
            String directionLabel;

            // [區塊 8]：根據觸發者角色分配「救援料 (Helpers)」與「夥伴料 (Partners)」
            if (triggerIsSource) {
                // 正向：Source 缺料 → Target 救援
                triggerItem = sourcesInGroup.stream()
                        .filter(i -> triggerItemNo.equals(i.getMrnb().trim()))
                        .findFirst().orElse(null);
                helperItems = targetsInGroup;
                // 找出同為 Source 但非 Trigger 的料作為 Partners
                partnerItems = sourcesInGroup.stream()
                        .filter(i -> !triggerItemNo.equals(i.getMrnb().trim()))
                        .collect(java.util.stream.Collectors.toList());
                directionLabel = "Forward (Source shortage -> Target donor)";
            } else if (triggerIsTarget && "EQUIVALENT".equalsIgnoreCase(policy)) {
                // 反向：Target 缺料 → Source 救援 (僅 EQUIVALENT 策略)
                triggerItem = targetsInGroup.stream()
                        .filter(i -> triggerItemNo.equals(i.getMrnb().trim()))
                        .findFirst().orElse(null);
                helperItems = sourcesInGroup;
                // 反向時，同為 Target 的其他料作為 Partners
                partnerItems = targetsInGroup.stream()
                        .filter(i -> !triggerItemNo.equals(i.getMrnb().trim()))
                        .collect(java.util.stream.Collectors.toList());
                directionLabel = "Reverse (Target shortage -> Source donor, EQUIVALENT)";
            } else {
                continue;
            }

            if (triggerItem == null)
                continue;

            // [區塊 9]：定出觸發料在模擬結果中的 row 及其定義數量
            Map<String, Object> triggerRow = simMap.get(triggerItemNo);
            if (triggerRow == null)
                continue;

            double triggerQtyDef = triggerItem.getQty() == null || triggerItem.getQty() == 0 ? 1.0
                    : triggerItem.getQty();

            // 使用通俗用語：替代 (EQUIVALENT) / 取代 (RUN_DOWN)
            String policyLabel = "EQUIVALENT".equalsIgnoreCase(policy) ? "[Replacement]" : "[Substitution]";

            // [區塊 10]：計算觸發料的實際缺口 (neededQty)
            // ===== 決定需救援數量 =====
            double triggerShortage = ((Number) triggerRow.getOrDefault("shortageqty", 0.0)).doubleValue();
            double triggerDemand = ((Number) triggerRow.getOrDefault("virtualdemand", 0.0)).doubleValue();
            double triggerMultiplier = ((Number) triggerRow.getOrDefault("qtymultiplier", 1.0)).doubleValue();
            double totalDemand = triggerDemand * triggerMultiplier;
            double currentSupplied = ((Number) triggerRow.getOrDefault("replacementsupply", 0.0)).doubleValue();

            double neededQty;
            if ("EQUIVALENT".equals(policy)) {
                if (totalDemand <= currentSupplied) {
                    Map<String, Object> trace = new HashMap<>();
                    trace.put("mrgid", mrgId);
                    trace.put("mrgname", mrgName);
                    trace.put("policy", policy);
                    trace.put("status", "SATIATED");
                    addReplacementTrace(triggerRow, trace);

                    appendReplacementLog(triggerRow,
                            String.format("● %s (Rule#%d)\n  ✅ Demand satisfied by previous rules.",
                                    policyLabel, mrgId));
                    continue;
                }
                neededQty = totalDemand - currentSupplied;
            } else {
                if (triggerShortage <= 0) {
                    Map<String, Object> trace = new HashMap<>();
                    trace.put("mrgid", mrgId);
                    trace.put("mrgname", mrgName);
                    trace.put("policy", policy);
                    trace.put("status", "SATIATED");
                    addReplacementTrace(triggerRow, trace);

                    appendReplacementLog(triggerRow,
                            String.format("● %s (Rule#%d)\n  ✅ Sufficient stock, no replacement needed.",
                                    policyLabel, mrgId));
                    continue;
                }
                neededQty = triggerShortage;
            }

            double neededUnit = neededQty / triggerQtyDef;

            // [區塊 11]：計算所有救援料 (Helpers) 的可用庫存換算 Unit
            // ===== 計算救援方可提供的最大 Unit 數 =====
            double availableUnit = Double.MAX_VALUE;
            StringBuilder helperLog = new StringBuilder();
            for (MaterialReplacementItem helper : helperItems) {
                String helperNo = helper.getMrnb() == null ? "" : helper.getMrnb().trim();
                double helperQtyDef = helper.getQty() == null || helper.getQty() == 0 ? 1.0 : helper.getQty();
                double ratio = helperQtyDef / triggerQtyDef;

                double helperBalance = 0.0;
                Map<String, Object> helperRow = simMap.get(helperNo);
                if (helperRow != null) {
                    helperBalance = ((Number) helperRow.getOrDefault("balanceafter", 0.0)).doubleValue();
                } else {
                    // JIT Fetch 外部庫存
                    if (!extInvMap.containsKey(helperNo)) {
                        double extStock = fetchExternalInventory(helperNo, ctx.selectedWarehouses);
                        extInvMap.put(helperNo, extStock);
                        helperLog.append(String.format("  🔍 [EXT-STOCK] %s = %.0f\n", helperNo, extStock));
                    }
                    helperBalance = extInvMap.get(helperNo);
                }

                double unitsFromHelper = (ratio > 0 && helperBalance > 0) ? helperBalance / helperQtyDef : 0.0;
                availableUnit = Math.min(availableUnit, unitsFromHelper);

                // 只有比例不是 1:1 時才標註比例
                String ratioInfo = (Math.abs(ratio - 1.0) < 0.0001) ? "" : String.format(" (Ratio 1:%.2f)", ratio);
                helperLog.append(String.format("  From %s support (Balance: %.0f%s)\n",
                        helperNo, helperBalance, ratioInfo));
            }

            // [區塊 12]：初始化紀錄標頭
            StringBuilder logEntry = new StringBuilder();
            logEntry.append(String.format("● %s (Rule#%d)\n", policyLabel, mrgId));
            logEntry.append(String.format("  Target material: %s (Need %.0f)\n",
                    triggerItemNo, neededQty));
            logEntry.append(helperLog);

            // 預先取得供前端顯示的 Helpers 清單
            List<Map<String, Object>> helperDetails = new ArrayList<>();
            for (MaterialReplacementItem h : helperItems) {
                Map<String, Object> hm = new HashMap<>();
                hm.put("itemno", h.getMrnb() == null ? "" : h.getMrnb().trim());
                hm.put("qtydef", h.getQty() == null ? 1.0 : h.getQty());
                helperDetails.add(hm);
            }

            // [區塊 13]：若無可用救援量則中止
            if (availableUnit <= 0 || availableUnit == Double.MAX_VALUE) {
                Map<String, Object> trace = new HashMap<>();
                trace.put("mrgid", mrgId);
                trace.put("mrgname", mrgName);
                trace.put("policy", policy);
                trace.put("status", "FAIL_STOCK");
                trace.put("suppliedqty", 0.0);
                trace.put("helpers", helperDetails);
                addReplacementTrace(triggerRow, trace);

                logEntry.append("  ❌ Result: Insufficient donor stock.");
                appendReplacementLog(triggerRow, logEntry.toString());
                continue;
            }

            // [區塊 14]：計算最終移轉量 (Units 與 Qty)
            double transferUnit = Math.min(availableUnit, neededUnit);
            if (transferUnit <= 0) {
                Map<String, Object> trace = new HashMap<>();
                trace.put("mrgid", mrgId);
                trace.put("mrgname", mrgName);
                trace.put("policy", policy);
                trace.put("status", "FAIL_STOCK");
                trace.put("suppliedqty", 0.0);
                trace.put("helpers", helperDetails);
                addReplacementTrace(triggerRow, trace);

                logEntry.append("  ❌ Result: Zero transfer, insufficient stock.");
                appendReplacementLog(triggerRow, logEntry.toString());
                continue;
            }

            double suppliedQty = transferUnit * triggerQtyDef;
            double remaining = neededQty - suppliedQty;
            String resultStatus = remaining > 0.5 ? "PARTIAL" : "SUCCESS";

            Map<String, Object> successTrace = new HashMap<>();
            successTrace.put("mrgid", mrgId);
            successTrace.put("mrgname", mrgName);
            successTrace.put("policy", policy);
            successTrace.put("status", resultStatus);
            successTrace.put("suppliedqty", suppliedQty);
            successTrace.put("neededqty", neededQty);

            // 記錄 Helpers 詳情到 Trace 中
            successTrace.put("helpers", helperDetails);
            addReplacementTrace(triggerRow, successTrace);

            // [Phase 2] 同步將追蹤紀錄也塞給所有支援者 (Helpers)，讓雙方都能點開明細
            for (MaterialReplacementItem h : helperItems) {
                Map<String, Object> hRow = simMap.get(h.getMrnb().trim());
                if (hRow != null) {
                    addReplacementTrace(hRow, successTrace);
                }
            }

            if (remaining > 0.5) {
                logEntry.append(String.format("  ⚠️ Result: PARTIAL satisfied (+%.0f), %.0f remaining.", suppliedQty,
                        remaining));
            } else {
                logEntry.append(String.format("  ✅ Result: FULLY satisfied (+%.0f).", suppliedQty));
            }

            // [區塊 15]：執行虛擬庫存移轉 (虛擬庫存計算核心)
            // ===== 執行虛擬庫存移轉 =====

            // 1. 觸發料 (A) 缺口縮減
            double triggerBalanceNow = ((Number) triggerRow.getOrDefault("balanceafter", 0.0)).doubleValue();
            double triggerNewBalance = triggerBalanceNow + suppliedQty;
            triggerRow.put("balanceafter", triggerNewBalance);
            triggerRow.put("shortageqty", triggerNewBalance < 0 ? Math.abs(triggerNewBalance) : 0.0);
            double prevReplSupply = ((Number) triggerRow.getOrDefault("replacementsupply", 0.0)).doubleValue();
            triggerRow.put("replacementsupply", prevReplSupply + suppliedQty);
            triggerRow.put("replacementruleid", mrgId);

            double trigQtyPerSet = ((Number) triggerRow.getOrDefault("qtyperset", 1.0)).doubleValue();
            if (trigQtyPerSet > 0) {
                double trigBalanceBefore = ((Number) triggerRow.getOrDefault("balancebefore", 0.0)).doubleValue();
                int newMaxSets = triggerNewBalance <= 0 ? 0
                        : (int) Math.floor((trigBalanceBefore + suppliedQty) / trigQtyPerSet);
                newMaxSets = Math.min(newMaxSets, ctx.virtualqty);
                triggerRow.put("maxsets", newMaxSets);
            }

            // 2. 隊友連動補充 (Partners B) (N:N 邏輯核心)
            for (MaterialReplacementItem partner : partnerItems) {
                String partnerNo = partner.getMrnb() == null ? "" : partner.getMrnb().trim();
                double partnerQtyDef = partner.getQty() == null || partner.getQty() == 0 ? 1.0 : partner.getQty();
                double partnerSuppliedQty = transferUnit * partnerQtyDef;

                Map<String, Object> partnerRow = simMap.get(partnerNo);
                if (partnerRow != null) {
                    double pBalanceNow = ((Number) partnerRow.getOrDefault("balanceafter", 0.0)).doubleValue();
                    double pNewBalance = pBalanceNow + partnerSuppliedQty;
                    partnerRow.put("balanceafter", pNewBalance);
                    partnerRow.put("shortageqty", pNewBalance < 0 ? Math.abs(pNewBalance) : 0.0);

                    double pPrevReplSupply = ((Number) partnerRow.getOrDefault("replacementsupply", 0.0)).doubleValue();
                    partnerRow.put("replacementsupply", pPrevReplSupply + partnerSuppliedQty);

                    double pQtyPerSet = ((Number) partnerRow.getOrDefault("qtyperset", 1.0)).doubleValue();
                    if (pQtyPerSet > 0) {
                        double pBalanceBefore = ((Number) partnerRow.getOrDefault("balancebefore", 0.0)).doubleValue();
                        int pNewMaxSets = pNewBalance <= 0 ? 0
                                : (int) Math.floor((pBalanceBefore + partnerSuppliedQty) / pQtyPerSet);
                        pNewMaxSets = Math.min(pNewMaxSets, ctx.virtualqty);
                        partnerRow.put("maxsets", pNewMaxSets);
                    }
                    appendReplacementLog(partnerRow,
                            String.format("[Rule#%d - Synergy Supply] Linked with %s, supplied %.0f pcs.", mrgId,
                                    triggerItemNo, partnerSuppliedQty));
                    logEntry.append(String.format("\n  🤝 Synergy: Partner %s supplied %.0f pcs.", partnerNo,
                            partnerSuppliedQty));
                }
            }

            // 寫入 Trigger 的最終 Log
            appendReplacementLog(triggerRow, logEntry.toString());

            // 3. 救援料 (Helpers C, D) 餘額扣減
            for (MaterialReplacementItem helper : helperItems) {
                String helperNo = helper.getMrnb() == null ? "" : helper.getMrnb().trim();
                double helperQtyDef = helper.getQty() == null || helper.getQty() == 0 ? 1.0 : helper.getQty();
                double deduction = transferUnit * helperQtyDef;

                Map<String, Object> helperRow = simMap.get(helperNo);
                if (helperRow != null) {
                    // 原專案內 Helper，正常扣減
                    double helperCurrentBalance = ((Number) helperRow.getOrDefault("balanceafter", 0.0)).doubleValue();
                    double helperNewBalance = helperCurrentBalance - deduction;
                    helperRow.put("balanceafter", helperNewBalance);
                    helperRow.put("shortageqty", helperNewBalance < 0 ? Math.abs(helperNewBalance) : 0.0);

                    double prevReplConsume = ((Number) helperRow.getOrDefault("replacementconsumed", 0.0))
                            .doubleValue();
                    helperRow.put("replacementconsumed", prevReplConsume + deduction);
                    appendReplacementLog(helperRow,
                            String.format("● [%s - Inventory Deduction] Supporting target %s, consumed %.0f pcs.",
                                    policyLabel, triggerItemNo, deduction));

                    double helperQtyPerSet = ((Number) helperRow.getOrDefault("qtyperset", 1.0)).doubleValue();
                    if (helperQtyPerSet > 0) {
                        double helperBalanceBefore = ((Number) helperRow.getOrDefault("balancebefore", 0.0))
                                .doubleValue();
                        int newMaxSets = (int) Math.floor((helperBalanceBefore - deduction) / helperQtyPerSet);
                        newMaxSets = Math.min(Math.max(0, newMaxSets), ctx.virtualqty);
                        helperRow.put("maxsets", newMaxSets);
                    }
                } else if (extInvMap.containsKey(helperNo)) {
                    // 外部庫存 Helper，更新 extInvMap 供後續規則使用
                    double extStock = extInvMap.get(helperNo);
                }
            }
        }
    }

    /** 將替代執行日誌追加到目標物料 row，多筆規則換行分隔 */
    private void appendReplacementLog(Map<String, Object> targetRow, String logEntry) {
        Object existing = targetRow.get("replacementlogs");
        if (existing == null || existing.toString().isEmpty()) {
            targetRow.put("replacementlogs", logEntry);
        } else {
            targetRow.put("replacementlogs", existing + "\n─────────────\n" + logEntry);
        }
    }

    /** 將結構化日誌記錄到目標物料 row */
    private void addReplacementTrace(Map<String, Object> targetRow, Map<String, Object> traceEntry) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> traces = (List<Map<String, Object>>) targetRow.get("replacementtrace");
        if (traces == null) {
            traces = new ArrayList<>();
            targetRow.put("replacementtrace", traces);
        }
        traces.add(traceEntry);
    }

    /**
     * 步驟 5：取得 BOM 父子關係，供前端繪製樹狀圖。
     *
     * 修復說明：原本用 WHERE parent IN (:chunk) AND child IN (:chunk) 的做法，
     * 會導致 parent 和 child 落在不同 chunk 時找不到關係。
     * 現在改為：只用 parent 分批查，child 在 Java 端用完整的 simItemNos Set 過濾。
     */
    private List<Map<String, Object>> buildBomEdges(List<Map<String, Object>> simResults) {
        Set<String> simItemNos = new java.util.HashSet<>();
        for (Map<String, Object> sr : simResults) {
            simItemNos.add(((String) sr.get("itemno")).trim());
        }

        List<Map<String, Object>> bomEdges = new ArrayList<>();
        // 用 LinkedHashSet 去重，防止重複 edge
        Set<String> edgeKeys = new java.util.LinkedHashSet<>();

        if (!simItemNos.isEmpty()) {
            List<String> itemsList = new ArrayList<>(simItemNos);
            int chunkSize = 1000; // 分批查詢，every chunk queries by parent only
            for (int i = 0; i < itemsList.size(); i += chunkSize) {
                List<String> chunk = itemsList.subList(i, Math.min(itemsList.size(), i + chunkSize));
                // 只用 parent 做 IN 查詢，不限制 child 必須在同一批 (SQL 已搬移至 DAO)
                List<Object[]> edgeRows = mvpDao.getBomEdges(chunk);
                for (Object[] er : edgeRows) {
                    String parentNo = ((String) er[0]).trim();
                    String childNo = ((String) er[1]).trim();
                    // 在 Java 端過濾：child 必須也在模擬清單中
                    if (!simItemNos.contains(childNo))
                        continue;
                    // 去重
                    String edgeKey = parentNo + "→" + childNo;
                    if (!edgeKeys.add(edgeKey))
                        continue;

                    Map<String, Object> edge = new java.util.HashMap<>();
                    edge.put("parent", parentNo);
                    edge.put("child", childNo);
                    bomEdges.add(edge);
                }
            }
        }
        return bomEdges;
    }
}
