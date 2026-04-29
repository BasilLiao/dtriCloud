package dtri.com.tw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonObject;

import dtri.com.tw.service.MaterialShortageService;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;

@RestController
/** 功能模組: 通用-缺料預計 */
public class MaterialShortageControllerAc extends AbstractControllerAc {

    @Autowired
    private MaterialShortageService materialShortageService;

    @Autowired
    private PackageService packageService;

    @RequestMapping(value = { "/materialShortage/getSearch" }, method = {
            RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    PackageBean getSearch(@RequestBody String jsonObject) {
        // 顯示方法
        String funName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sysFunction(funName);
        // Step0.資料準備
        PackageBean packageBean = new PackageBean();

        try {
            long startTime = System.currentTimeMillis();
            
            // Step1.解包=>(String 轉換 JSON)=>(JSON 轉換 PackageBean)=> 檢查 => Pass
            long t1 = System.currentTimeMillis();
            JsonObject packageObject = packageService.StringToJson(jsonObject);
            packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);
            long t2 = System.currentTimeMillis();
            loggerInf(String.format("⏱️ [Material Phase 1] JSON Parser Cost: %d ms", (t2 - t1)), packageBean.getUserAccount());

            // Step2.執行=>服務
            loggerInf(funName + "[Start]", packageBean.getUserAccount());
            
            long t3 = System.currentTimeMillis();
            if ("getItemStock".equals(packageBean.getEntityDetailIKeyGKey())) {
                packageBean = materialShortageService.getItemStock(packageBean);
            } else {
                packageBean = materialShortageService.getSearch(packageBean);
            }
            long t4 = System.currentTimeMillis();
            loggerInf(String.format("⏱️ [Material Phase 2] Service/DB Logic Cost: %d ms", (t4 - t3)), packageBean.getUserAccount());
            
            // 計算最終轉出的大小 (為了回傳給 Feign，最後這裡的 PackageBean 會被 Spring/Tomcat 轉譯為字串)
            try {
                long t5 = System.currentTimeMillis();
                String outJson = packageService.beanToJson(packageBean);
                long t6 = System.currentTimeMillis();
                double sizeMB = outJson.length() / (1024.0 * 1024.0);
                loggerInf(String.format("⏱️ [Material Phase 3] Final Output JSON Build Cost (Estimated): %d ms", (t6 - t5)), packageBean.getUserAccount());
                loggerInf(String.format("📦 [Material Data Size] Outbound Response Size: %.2f MB", sizeMB), packageBean.getUserAccount());
            } catch (Exception e) {
                e.printStackTrace();
            }

            loggerInf(funName + "[End]", packageBean.getUserAccount());
            
            long endTime = System.currentTimeMillis();
            loggerInf(String.format(" [Material Total] API Total Cost: %d ms", (endTime - startTime)), packageBean.getUserAccount());
            
        } catch (JsonProcessingException e) {
            // StepX-1. 已知-故障回報
            e.printStackTrace();
            loggerWarn(eStktToSg(e), packageBean.getUserAccount());
        } catch (CloudExceptionService e) {
            // StepX-2. 已知-故障回報
            e.printStackTrace();
            loggerInf(e.toString(), packageBean.getUserAccount());
        } catch (Exception e) {
            // StepX-3. 未知-故障回報
            e.printStackTrace();
            loggerWarn(eStktToSg(e), packageBean.getUserAccount());
            packageBean.setInfo(CloudExceptionService.W0000_en_US);
            packageBean.setInfoColor(CloudExceptionService.ErColor.danger + "");
        }
        return packageBean;
    }

    @RequestMapping(value = { "/materialShortage/getItemStock" }, method = {
            RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    PackageBean getItemStock(@RequestBody String jsonObject) {
        String funName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sysFunction(funName);
        PackageBean packageBean = new PackageBean();
        try {
            JsonObject packageObject = packageService.StringToJson(jsonObject);
            packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);
            packageBean = materialShortageService.getItemStock(packageBean);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
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
