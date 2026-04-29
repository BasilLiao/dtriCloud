package dtri.com.tw.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dtri.com.tw.pgsql.entity.MaterialShortage;
import dtri.com.tw.service.MaterialShortageDataService;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;

@RestController
public class MaterialShortageDataControllerAc extends AbstractControllerAc {

    @Autowired
    private PackageService packageService;

    @Autowired
    private MaterialShortageDataService serviceAc;

    @RequestMapping(value = { "/materialShortageData/getRecalculate" }, method = {
            RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    PackageBean getRecalculate(@RequestBody String jsonObject) {
        // 顯示方法
        String funName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sysFunction(funName);
        // Step0.資料準備
        PackageBean packageBean = new PackageBean();

        try {
            long startTime = System.currentTimeMillis();
            // Step1.解包=>(String 轉換 JSON)=>(JSON 轉換 PackageBean)=> 檢查 => Pass
            JsonObject packageObject = packageService.StringToJson(jsonObject);
            packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);
            // Step2.執行=>服務
            loggerInf(funName + "[Start]", packageBean.getUserAccount());

            // 自定義邏輯: 從 entityJson 擷取倉別清單與單別清單
            // 預期新格式為: {"warehouses": ["W01", "W02"], "includeTypes": ["採購單", "製令單"]}
            // 相容舊格式為: ["W01", "W02"] (只有倉別)
            List<String> warehouses = new ArrayList<>();
            List<String> includeTypes = null; // null 表示全取
            Map<String, List<String>> excludePatterns = new HashMap<>(); // 單別排除關鍵字
            String materialNos = null; // null 表示全查，非 null 則為逗號分隔字串

            if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().trim().isEmpty()
                    && !packageBean.getEntityJson().trim().equals("[]")) {
                try {
                    String json = packageBean.getEntityJson().trim();
                    if (json.startsWith("[")) {
                        // 舊相容：純陣列代表倉別篩選
                        JsonArray jsonArray = packageService.StringToAJson(json);
                        for (JsonElement element : jsonArray) {
                            warehouses.add(element.getAsString());
                        }
                    } else if (json.startsWith("{")) {
                        // 新格式：物件包含 warehouses, includeTypes 與 excludePatterns
                        JsonObject jsonObj = packageService.StringToJson(json);
                        // 1. 倉別
                        if (jsonObj.has("warehouses") && !jsonObj.get("warehouses").isJsonNull()) {
                            for (JsonElement element : jsonObj.getAsJsonArray("warehouses")) {
                                warehouses.add(element.getAsString());
                            }
                        }
                        // 2. 包含單別
                        if (jsonObj.has("includeTypes") && !jsonObj.get("includeTypes").isJsonNull()) {
                            includeTypes = new ArrayList<>();
                            for (JsonElement element : jsonObj.getAsJsonArray("includeTypes")) {
                                includeTypes.add(element.getAsString());
                            }
                        }
                        // 3. 排除關鍵字 (Map<String, List<String>>)
                        if (jsonObj.has("excludePatterns") && !jsonObj.get("excludePatterns").isJsonNull()) {
                            JsonObject patternsObj = jsonObj.getAsJsonObject("excludePatterns");
                            for (Map.Entry<String, JsonElement> entry : patternsObj.entrySet()) {
                                List<String> pList = new ArrayList<>();
                                for (JsonElement p : entry.getValue().getAsJsonArray()) {
                                    pList.add(p.getAsString());
                                }
                                excludePatterns.put(entry.getKey(), pList);
                            }
                        }
                        // 4. 料號過濾 (多選)
                        if (jsonObj.has("materialNos") && !jsonObj.get("materialNos").isJsonNull()) {
                            materialNos = jsonObj.get("materialNos").getAsString();
                        }
                    }
                } catch (Exception e) {
                    loggerWarn("解析 entityJson 失敗: " + packageBean.getEntityJson(), packageBean.getUserAccount());
                }
            }

            // 呼叫 Service 的重算邏輯 (不寫入 DB)
            List<MaterialShortage> results = serviceAc.recalculateMrp(warehouses, includeTypes, excludePatterns, materialNos);

            // 將結果轉成 JSON 放回 entityJson (改用高效能矩陣格式)
            packageBean.setEntityJson(packageService.beanToMatrixJson(results));

            long endTime = System.currentTimeMillis();
            loggerInf(String.format("🚀 [Backend Cost] Core Calculation: %d ms", (endTime - startTime)), packageBean.getUserAccount());
            loggerInf(funName + "[End]", packageBean.getUserAccount());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            loggerWarn(eStktToSg(e), packageBean.getUserAccount());
        } catch (CloudExceptionService e) {
            e.printStackTrace();
            loggerInf(e.toString(), packageBean.getUserAccount());
        } catch (Exception e) {
            e.printStackTrace();
            loggerWarn(eStktToSg(e), packageBean.getUserAccount());
            packageBean.setInfo(CloudExceptionService.W0000_en_US);
            packageBean.setInfoColor(CloudExceptionService.ErColor.danger + "");
        }
        return packageBean;
    }
}
