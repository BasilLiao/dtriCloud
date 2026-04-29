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

@Controller
public class MaterialProcessController extends AbstractController {

    @Autowired
    private MaterialServiceFeign serviceFeign;

    @Autowired
    private PackageService packageService;

    // ====== 進入/初始化 ======
    @ResponseBody
    @RequestMapping(value = { "/ajax/material_process.basil" }, method = {
            RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    String access(@RequestBody String jsonObject) {
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

            packageBean = serviceFeign.getMaterialProcessSearch(packageService.beanToJson(packageBean));
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
            loggerWarn(eStktToSg(e), loginUser().getUsername());
        }
        return packageJson;
    }

    // ====== 查詢 ======
    @ResponseBody
    @RequestMapping(value = { "/ajax/material_process.basil.AR" }, method = {
            RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    String search(@RequestBody String jsonObject) {
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

            packageBean = serviceFeign.getMaterialProcessSearch(packageService.beanToJson(packageBean));
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
            loggerWarn(eStktToSg(e), loginUser().getUsername());
        }
        return packageJson;
    }

    // ====== 新增 ======
    @Override
    @ResponseBody
    @RequestMapping(value = { "/ajax/material_process.basil.AC" }, method = {
            RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    String add(@RequestBody String jsonObject) {
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

            // 新增 = 透過 setModify 觸發後端 upsert 邏輯
            packageBean = serviceFeign.setMaterialProcessModify(packageService.beanToJson(packageBean));
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
            loggerWarn(eStktToSg(e), loginUser().getUsername());
        }
        return packageJson;
    }

    // ====== 修改 ======
    @Override
    @ResponseBody
    @RequestMapping(value = { "/ajax/material_process.basil.AU" }, method = {
            RequestMethod.PUT }, produces = "application/json;charset=UTF-8")
    String modify(@RequestBody String jsonObject) {
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

            packageBean = serviceFeign.setMaterialProcessModify(packageService.beanToJson(packageBean));
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
            loggerWarn(eStktToSg(e), loginUser().getUsername());
        }
        return packageJson;
    }

    // ====== 刪除 ======
    @Override
    @ResponseBody
    @RequestMapping(value = { "/ajax/material_process.basil.AD" }, method = {
            RequestMethod.DELETE }, produces = "application/json;charset=UTF-8")
    String delete(@RequestBody String jsonObject) {
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

            packageBean = serviceFeign.setMaterialProcessDelete(packageService.beanToJson(packageBean));
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
            loggerWarn(eStktToSg(e), loginUser().getUsername());
        }
        return packageJson;
    }

    // ====== 作廢 (此模組不使用) ======
    @Override
    String invalid(String jsonObject) {
        throw new UnsupportedOperationException("Unimplemented method 'invalid'");
    }
}
