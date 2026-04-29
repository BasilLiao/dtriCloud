package dtri.com.tw.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

import dtri.com.tw.pgsql.dao.MaterialEolDao;
import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.dto.MaterialEolDto;
import dtri.com.tw.pgsql.entity.SystemLanguageCell;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.CloudExceptionService.ErCode;
import dtri.com.tw.shared.CloudExceptionService.ErColor;
import dtri.com.tw.shared.CloudExceptionService.Lan;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;

/**
 * 停產物料估算
 */
/** 功能模組: 通用-停損物料估算 */
@Service
public class MaterialEolService {

    @Autowired
    private MaterialEolDao materialEolDao;

    @Autowired
    private PackageService packageService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SystemLanguageCellDao languageDao;

    /**
     * 初始化載入 (Search)
     * 取得系統所有可用倉別 (大於 0 的庫存倉別) 與定義匯出表頭
     */
    @Transactional(readOnly = true)
    public PackageBean getSearch(PackageBean packageBean) throws Exception {
        // 從資料庫取得所有非空庫存的倉別清單
        List<Object[]> warehouses = materialEolDao.getAvailableWarehouses();
        List<Map<String, String>> whList = new ArrayList<>();

        for (Object[] wh : warehouses) {
            Map<String, String> map = new HashMap<>();
            map.put("alias", (String) wh[0]);
            map.put("name", (String) wh[1]);
            whList.add(map);
        }

        // 2. 語系資料整合 (由 MaterialFront 統一管理)
        ArrayList<SystemLanguageCell> languages = languageDao.findAllByLanguageCellSame("MaterialFront", null, 1);
        languages.addAll(languageDao.findAllByLanguageCellSame("MaterialFront", null, 2));

        Map<String, String> uiLangMap = new HashMap<>();
        languages.forEach(x -> {
            if (x.getSltarget() != null && !x.getSltarget().isEmpty()) {
                if (x.getSllanguage() != null && !x.getSllanguage().isEmpty()) {
                    uiLangMap.put(x.getSltarget(), x.getSllanguage());
                }
            }
        });

        // 3. 設定前端顯示所需的語系與工具
        Map<String, Object> searchSetMap = new HashMap<>();
        Map<String, Object> resultThead = new LinkedHashMap<>(); // EOL 目前主要由前端定義 Cell，但保留 Thead 結構
        searchSetMap.put("uiLang", uiLangMap);
        searchSetMap.put("resultThead", resultThead);

        packageBean.setEntityJson(objectMapper.writeValueAsString(whList));
        packageBean.setSearchSet(objectMapper.writeValueAsString(searchSetMap));

        return packageBean;
    }

    /**
     * 計算客製化報表
     * 接收 90BOM、階層、勾選倉別，回傳計算結果
     */
    @Transactional(readOnly = true)
    public PackageBean getReport(PackageBean packageBean) throws Exception {
        JsonObject reqJson;
        try {
            reqJson = packageService.StringToJson(packageBean.getEntityJson());
        } catch (Exception e) {
            throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1004, Lan.zh_TW,
                    new String[] { "JSON 解析失敗" });
        }

        // 1. BOM參數 (支援單選或多選)
        List<String> bomNoList = new ArrayList<>();
        if (reqJson.has("bomnolist") && !reqJson.get("bomnolist").isJsonNull()) {
            bomNoList = objectMapper.readValue(reqJson.get("bomnolist").toString(),
                    new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {
                    });
        }
        // 反向相容舊版單一輸入
        if (bomNoList.isEmpty() && reqJson.has("bomno") && !reqJson.get("bomno").isJsonNull()) {
            String bomNo = reqJson.get("bomno").getAsString().trim();
            if (!bomNo.isEmpty()) {
                bomNoList.add(bomNo);
            }
        }

        if (bomNoList.isEmpty()) {
            throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1003, Lan.zh_TW,
                    new String[] { "BOM 號不可為空" });
        }

        // 2. 最大階層
        int maxLevel = reqJson.has("maxlevel") && !reqJson.get("maxlevel").isJsonNull()
                ? reqJson.get("maxlevel").getAsInt()
                : 10;

        // 3. 勾選倉別清單
        List<String> warehouseList = new ArrayList<>();
        if (reqJson.has("warehouselist") && !reqJson.get("warehouselist").isJsonNull()) {
            warehouseList = objectMapper.readValue(reqJson.get("warehouselist").toString(),
                    new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {
                    });
        }

        // 若未勾選則預設帶入成品倉 (明天修改在前端 預設checkbox 不能小於1) 2026/03/02
        if (warehouseList.isEmpty()) {
            warehouseList.add("A0001");
        }

        // 4. 每月需求套數
        int monthlyDemand = reqJson.has("monthlydemand") && !reqJson.get("monthlydemand").isJsonNull()
                ? reqJson.get("monthlydemand").getAsInt()
                : 0;

        // 呼叫 DAO 執行
        List<MaterialEolDto> resultList = materialEolDao.getEstimatedMaterials(bomNoList, maxLevel, warehouseList);

        // 如果查無資料，傳回警告訊息與空陣列
        if (resultList == null || resultList.isEmpty()) {
            packageBean.setEntityJson("[]");
            throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1000, Lan.zh_TW, null);
        }

        // --- 聚合邏輯 (Group by partNo) ---
        // key: partNo, value: aggregated Map
        Map<String, Map<String, Object>> aggregatedMap = new LinkedHashMap<>();

        for (MaterialEolDto dto : resultList) {
            String partNo = dto.getPartno();

            if (aggregatedMap.containsKey(partNo)) {
                // 已存在，累加 qtyPerSet 並記錄來源 BOM
                Map<String, Object> existing = aggregatedMap.get(partNo);

                // 累加數量
                double currentQty = (double) existing.get("qtyperset");
                existing.put("qtyperset", currentQty + dto.getQtyperset());

                // 記錄來源 BOM (去重)
                String currentRoots = (String) existing.get("rootboms");
                if (dto.getRootbom() != null && !currentRoots.contains(dto.getRootbom())) {
                    existing.put("rootboms", currentRoots + ", " + dto.getRootbom());
                }

                // 維護最小階層 (選階層最高的數字，因為 1 是最高階，數字越大階層越深。通常顯示最淺的階層)
                int currentLevel = (int) existing.get("bomlevel");
                if (dto.getBomlevel() < currentLevel) {
                    existing.put("bomlevel", dto.getBomlevel());
                }

            } else {
                // 第一次遇到此料號
                Map<String, Object> map = new HashMap<>();
                map.put("rootboms", dto.getRootbom() != null ? dto.getRootbom() : "");
                map.put("partno", dto.getPartno());
                map.put("partname", dto.getPartname());
                map.put("partspec", dto.getPartspec());
                map.put("remark", dto.getRemark());
                map.put("bomlevel", dto.getBomlevel());
                map.put("qtyperset", dto.getQtyperset());
                map.put("warehousestock", dto.getWarehousestock());
                aggregatedMap.put(partNo, map);
            }
        }

        // 轉換為 List 並計算最終的 availableSets 和 supportMonths
        List<Map<String, Object>> finalResult = new ArrayList<>();
        for (Map<String, Object> map : aggregatedMap.values()) {
            double totalQtyPerSet = (double) map.get("qtyperset");
            int warehouseStock = (int) map.get("warehousestock");

            // 重新計算可做套數 (加總後的組合)
            int availableSets = 0;
            if (totalQtyPerSet > 0) {
                availableSets = (int) Math.floor(warehouseStock / totalQtyPerSet);
            }
            map.put("availablesets", availableSets);

            // 估計可支撐月數
            if (monthlyDemand > 0) {
                double months = (double) availableSets / monthlyDemand;
                map.put("supportmonths", Math.round(months * 10.0) / 10.0);
            } else {
                map.put("supportmonths", "-");
            }

            // 四捨五入 qtyPerSet 以防浮點數精度問題
            map.put("qtyperset", Math.round(totalQtyPerSet * 10000.0) / 10000.0);

            finalResult.add(map);
        }

        // 統一把結果寫入 EntityJson 回傳
        packageBean.setEntityJson(objectMapper.writeValueAsString(finalResult));

        return packageBean;
    }
}
