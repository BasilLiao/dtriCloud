package dtri.com.tw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonObject;

import dtri.com.tw.service.feign.BasicServiceFeign;
import dtri.com.tw.service.feign.MaterialServiceFeign;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;
import jakarta.annotation.Resource;

@Controller
public class MaterialShortageController extends AbstractController {

    @Autowired
    private PackageService packageService;

    @Resource
    MaterialServiceFeign serviceFeign;

    @Resource
    BasicServiceFeign basicServiceFeign;

    @ResponseBody
    @RequestMapping(value = { "/ajax/material_shortage.basil" }, method = {
            RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    String access(@RequestBody String jsonObject) {
        // 計時器起點
        long startTime = System.currentTimeMillis();

        // 顯示方法
        String funName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sysFunction(funName);

        // Step0.資料準備
        String packageJson = "{}";
        PackageBean packageBean = new PackageBean();

        try {
            loggerInf(funName + "[Start]" + jsonObject, loginUser().getUsername());

            // --- ⏱ 階段 1: JSON 解析 (解包) ---
            long t1 = System.currentTimeMillis();

            // Step1.解包=>(String 轉換 JSON)=>(JSON 轉換 PackageBean)=> 檢查 => Pass
            JsonObject packageObject = packageService.StringToJson(jsonObject);
            packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);

            // Step2.基礎資料整理
            packageBean.setUserAccount(loginUser().getSystemUser().getSuaccount());
            packageBean.setUserLanguaue(loginUser().getSystemUser().getSulanguage());
            packageBean.setUserAgentAccount(loginUser().getSystemUser().getSuaaccount());
            packageBean.setEntityIKeyGKey(String.valueOf(loginUser().getSystemUser().getSuid()));

            // --- ⏱ 階段 2: 呼叫微服務 (Feign Client) ---
            String reqJson = packageService.beanToJson(packageBean);

            long t3 = System.currentTimeMillis();

            // Step3.執行=>跨服務執行
            // 判斷指令
            if ("getItemStock".equals(packageBean.getEntityDetailIKeyGKey())) {
                packageBean = serviceFeign.getMaterialShortageItemStock(reqJson);
            } else {
                packageBean = serviceFeign.getMaterialShortageSearch(reqJson);
            }

            long t4 = System.currentTimeMillis();
            loggerInf(String.format("⏱️ [Phase 2] Feign Service Cost (DB Query): %d ms", (t4 - t3)),
                    loginUser().getUsername());

            // --- ⏱ 階段 3: 資料打包 (Serialization) ---
            loggerInf(funName + "[End]", loginUser().getUsername());

        } catch (Exception e) {
            // StepX-2. 未知-故障回報
            e.printStackTrace();
            loggerWarn(eStktToSg(e), loginUser().getUsername());
            packageBean.setInfo(CloudExceptionService.W0000_en_US);
            packageBean.setInfoColor(CloudExceptionService.ErColor.danger + "");
        }

        // Step4.打包=>(轉換 PackageBean)=>包裝=>Json
        try {
            long t5 = System.currentTimeMillis();

            // 如果回傳資料有 8 萬筆，這裡轉 JSON 也會很久
            packageJson = packageService.beanToJson(packageBean);

            long t6 = System.currentTimeMillis();
            loggerInf(String.format("⏱️ [Phase 3] Final JSON Build Cost: %d ms", (t6 - t5)), loginUser().getUsername());

            // 檢查產生的 JSON 大小 (以 MB 為單位)
            double sizeMB = packageJson.length() / (1024.0 * 1024.0);
            loggerInf(String.format("📦 [Data Size] Response Size: %.2f MB", sizeMB), loginUser().getUsername());

        } catch (Exception e) {
            e.printStackTrace();
            loggerWarn(eStktToSg(e), loginUser().getUsername());
        }

        long endTime = System.currentTimeMillis();
        loggerInf(String.format(" [Total] API Total Cost: %d ms", (endTime - startTime)), loginUser().getUsername());

        return packageJson;
    }

    @ResponseBody
    @RequestMapping(value = { "/ajax/material_shortage.basil.AR" }, method = {
            RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    String search(@RequestBody String jsonObject) {
        // 顯示方法
        String funName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sysFunction(funName);

        // Step0.資料準備
        String packageJson = "{}";
        PackageBean packageBean = new PackageBean();
        long startTime = System.currentTimeMillis();
        try {
            loggerInf(funName + "[Start]", loginUser().getUsername());

            // --- ⏱ 階段 1: JSON 解析 (解包) ---
            long t1 = System.currentTimeMillis();
            JsonObject packageObject = packageService.StringToJson(jsonObject);
            packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);

            // Step2.基礎資料整理
            packageBean.setUserAccount(loginUser().getSystemUser().getSuaccount());// 使用者
            packageBean.setUserLanguaue(loginUser().getSystemUser().getSulanguage());// 語言
            packageBean.setUserAgentAccount(loginUser().getSystemUser().getSuaaccount());// 使用者(代理)
            packageBean.setEntityIKeyGKey(String.valueOf(loginUser().getSystemUser().getSuid()));
            long t2 = System.currentTimeMillis();
            loggerInf(String.format("⏱️ [Phase 1] JSON Parser Cost: %d ms", (t2 - t1)), loginUser().getUsername());

            // --- ⏱ 階段 2: 呼叫微服務 (Feign Client) ---
            long t3 = System.currentTimeMillis();
            String reqJson = packageService.beanToJson(packageBean);
            packageBean = serviceFeign.getMaterialShortageSearch(reqJson);
            long t4 = System.currentTimeMillis();
            loggerInf(String.format("⏱️ [Phase 2] Feign Service Cost (Include Material DB Query): %d ms", (t4 - t3)),
                    loginUser().getUsername());

            loggerInf(funName + "[End]", loginUser().getUsername());

        } catch (Exception e) {
            // StepX-2. 未知-故障回報
            e.printStackTrace();
            loggerWarn(eStktToSg(e), loginUser().getUsername());
            packageBean.setInfo(CloudExceptionService.W0000_en_US);
            packageBean.setInfoColor(CloudExceptionService.ErColor.danger + "");
        }

        // Step4.打包=>(轉換 PackageBean)=>包裝=>Json
        try {
            long t5 = System.currentTimeMillis();
            packageJson = packageService.beanToJson(packageBean);
            long t6 = System.currentTimeMillis();
            loggerInf(String.format("⏱️ [Phase 3] Final JSON Build Cost: %d ms", (t6 - t5)), loginUser().getUsername());
        } catch (Exception e) {
            e.printStackTrace();
            loggerWarn(eStktToSg(e), loginUser().getUsername());
        }

        long endTime = System.currentTimeMillis();
        loggerInf(String.format(" [Total] API Total Cost: %d ms", (endTime - startTime)), loginUser().getUsername());
        return packageJson;
    }

    @ResponseBody
    @RequestMapping(value = { "/ajax/material_shortage.basil.RC" }, method = {
            RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    String recalculate(@RequestBody String jsonObject) {
        // 計時器起點
        long startTime = System.currentTimeMillis();
        // 顯示方法
        String funName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sysFunction(funName);

        // Step0.資料準備
        String packageJson = "{}";
        PackageBean packageBean = new PackageBean();
        try {
            loggerInf(funName + "[Start]" + jsonObject, loginUser().getUsername());

            // --- ⏱ 階段 1: JSON 解析 (解包) ---
            long t1 = System.currentTimeMillis();
            // Step1.解包=>(String 轉換 JSON)=>(JSON 轉換 PackageBean)=> 檢查 => Pass
            JsonObject packageObject = packageService.StringToJson(jsonObject);
            packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);

            // Step2.基礎資料整理
            packageBean.setUserAccount(loginUser().getSystemUser().getSuaccount());// 使用者
            packageBean.setUserLanguaue(loginUser().getSystemUser().getSulanguage());// 語言
            packageBean.setUserAgentAccount(loginUser().getSystemUser().getSuaaccount());// 使用者(代理)
            packageBean.setEntityIKeyGKey(String.valueOf(loginUser().getSystemUser().getSuid()));

            long t2 = System.currentTimeMillis();
            loggerInf(String.format("⏱️ [Phase 1] JSON Parser Cost: %d ms", (t2 - t1)), loginUser().getUsername());

            // --- ⏱ 階段 2: 呼叫微服務 (Feign Client) ---
            long t3 = System.currentTimeMillis();
            // Step3.執行=>跨服->務執行
            packageBean = basicServiceFeign.getMaterialShortageRecalculate(packageService.beanToJson(packageBean));
            long t4 = System.currentTimeMillis();
            loggerInf(String.format("⏱️ [Phase 2] Feign Service Cost: %d ms", (t4 - t3)), loginUser().getUsername());

            loggerInf(funName + "[End]", loginUser().getUsername());

        } catch (Exception e) {
            // StepX-2. 未知-故障回報
            e.printStackTrace();
            loggerWarn(eStktToSg(e), loginUser().getUsername());
            packageBean.setInfo(CloudExceptionService.W0000_en_US);
            packageBean.setInfoColor(CloudExceptionService.ErColor.danger + "");
        }

        // Step4.打包=>(轉換 PackageBean)=>包裝=>Json
        try {
            long t5 = System.currentTimeMillis();
            packageJson = packageService.beanToJson(packageBean);
            long t6 = System.currentTimeMillis();
            loggerInf(String.format("⏱️ [Phase 3] Final JSON Build Cost: %d ms", (t6 - t5)), loginUser().getUsername());
        } catch (Exception e) {
            e.printStackTrace();
            loggerWarn(eStktToSg(e), loginUser().getUsername());
        }

        long endTime = System.currentTimeMillis();
        loggerInf(String.format(" [Total] Recalculate API Total Cost: %d ms", (endTime - startTime)),
                loginUser().getUsername());
        return packageJson;
    }

    @Override
    String add(String jsonObject) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'add'");
    }

    @Override
    String modify(String jsonObject) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'modify'");
    }

    @Override
    String invalid(String jsonObject) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'invalid'");
    }

    @Override
    String delete(String jsonObject) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }
}
