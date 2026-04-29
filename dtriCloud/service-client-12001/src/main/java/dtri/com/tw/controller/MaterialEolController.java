package dtri.com.tw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonObject;

import dtri.com.tw.service.feign.MaterialServiceFeign;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;
import jakarta.annotation.Resource;

@Controller
public class MaterialEolController extends AbstractController {

    @Autowired
    private PackageService packageService;

    @Resource
    MaterialServiceFeign serviceFeign;

    @ResponseBody
    @RequestMapping(value = { "/ajax/material_eol.basil" }, method = {
            RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    String access(@RequestBody String jsonObject) {
        // 顯示方法
        String funName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sysFunction(funName);

        // Step0.資料準備
        String packageJson = "{}";
        PackageBean packageBean = new PackageBean();
        try {
            loggerInf(funName + "[Start]" + jsonObject, loginUser().getUsername());
            // Step1.解包=>(String 轉換 JSON)=>(JSON 轉換 PackageBean)=> 檢查 => Pass
            JsonObject packageObject = packageService.StringToJson(jsonObject);
            packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);

            // Step2.基礎資料整理
            packageBean.setUserAccount(loginUser().getSystemUser().getSuaccount());// 使用者
            packageBean.setUserLanguaue(loginUser().getSystemUser().getSulanguage());// 語言
            packageBean.setUserAgentAccount(loginUser().getSystemUser().getSuaaccount());// 使用者(代理)
            packageBean.setEntityIKeyGKey(String.valueOf(loginUser().getSystemUser().getSuid()));

            // Step3.執行=>跨服務執行 (呼叫 Backend ServiceFeign)
            packageBean = serviceFeign.getMaterialEolSearch(packageService.beanToJson(packageBean));
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
            packageJson = packageService.beanToJson(packageBean);
        } catch (Exception e) {
            e.printStackTrace();
            loggerWarn(eStktToSg(e), loginUser().getUsername());
        }
        return packageJson;
    }

    @ResponseBody
    @RequestMapping(value = { "/ajax/material_eol.basil.AR" }, method = {
            RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    String search(@RequestBody String jsonObject) {
        return access(jsonObject);
    }

    @ResponseBody
    @RequestMapping(value = { "/ajax/material_eol.basil.ARR" }, method = {
            RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    String report(@RequestBody String jsonObject) {
        // 顯示方法
        String funName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sysFunction(funName);

        // Step0.資料準備
        String packageJson = "{}";
        PackageBean packageBean = new PackageBean();
        try {
            loggerInf(funName + "[Start]" + jsonObject, loginUser().getUsername());
            // Step1.解包=>(String 轉換 JSON)=>(JSON 轉換 PackageBean)=> 檢查 => Pass
            JsonObject packageObject = packageService.StringToJson(jsonObject);
            packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);

            // Step2.基礎資料整理
            packageBean.setUserAccount(loginUser().getSystemUser().getSuaccount());// 使用者
            packageBean.setUserLanguaue(loginUser().getSystemUser().getSulanguage());// 語言
            packageBean.setUserAgentAccount(loginUser().getSystemUser().getSuaaccount());// 使用者(代理)
            packageBean.setEntityIKeyGKey(String.valueOf(loginUser().getSystemUser().getSuid()));

            // Step3.執行=>跨服務執行 (呼叫 Backend ServiceFeign 報表查詢)
            packageBean = serviceFeign.getMaterialEolReport(packageService.beanToJson(packageBean));
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
            packageJson = packageService.beanToJson(packageBean);
        } catch (Exception e) {
            e.printStackTrace();
            loggerWarn(eStktToSg(e), loginUser().getUsername());
        }
        return packageJson;
    }

    @Override
    @ResponseBody
    @RequestMapping(value = { "/ajax/material_eol.basil.AC" }, method = {
            RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    String add(@RequestBody String jsonObject) {
        throw new UnsupportedOperationException("Unimplemented method 'add'");
    }

    @Override
    @ResponseBody
    @RequestMapping(value = { "/ajax/material_eol.basil.AU" }, method = {
            RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    String modify(@RequestBody String jsonObject) {
        throw new UnsupportedOperationException("Unimplemented method 'modify'");
    }

    @Override
    @ResponseBody
    @RequestMapping(value = { "/ajax/material_eol.basil.DD" }, method = {
            RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    String invalid(@RequestBody String jsonObject) {
        throw new UnsupportedOperationException("Unimplemented method 'invalid'");
    }

    @Override
    @ResponseBody
    @RequestMapping(value = { "/ajax/material_eol.basil.AD" }, method = {
            RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    String delete(@RequestBody String jsonObject) {
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }


    // ====== 取得所有庫存料號字典 (Feature 10: Client-side Search Caching) ======
    @ResponseBody
    @RequestMapping(value = { "/ajax/material_eol.basil.getInitSearchData" }, method = {
            RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    String getInitSearchData(@RequestBody String jsonObject) {
        String funName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sysFunction(funName);

        String packageJson = "{}";
        PackageBean packageBean = new PackageBean();
        try {
            loggerInf(funName + "[Start]" + jsonObject, loginUser().getUsername());
            JsonObject packageObject = packageService.StringToJson(jsonObject);
            packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);

            packageBean.setUserAccount(loginUser().getSystemUser().getSuaccount());
            packageBean.setUserLanguaue(loginUser().getSystemUser().getSulanguage());
            packageBean.setUserAgentAccount(loginUser().getSystemUser().getSuaaccount());

            packageBean = serviceFeign.getMaterialVirtualProjectInitSearchData(packageService.beanToJson(packageBean));
            loggerInf(funName + "[End]", loginUser().getUsername());
        } catch (Exception e) {
            e.printStackTrace();
            loggerWarn(eStktToSg(e), loginUser().getUsername());
            packageBean.setInfo(CloudExceptionService.W0000_en_US);
            packageBean.setInfoColor(CloudExceptionService.ErColor.danger + "");
        }

        try {
            packageJson = packageService.beanToJson(packageBean);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return packageJson;
    }
}
