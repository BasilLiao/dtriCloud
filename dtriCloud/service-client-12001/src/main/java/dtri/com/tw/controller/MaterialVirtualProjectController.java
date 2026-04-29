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
public class MaterialVirtualProjectController extends AbstractController {

    @Autowired
    private MaterialServiceFeign serviceFeign;

    @Autowired
    private PackageService packageService;

    // ====== 初始化 / 查詢 ======
    @ResponseBody
    @RequestMapping(value = { "/ajax/material_virtual_project.basil" }, method = {
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

            packageBean = serviceFeign.getMaterialVirtualProjectSearch(packageService.beanToJson(packageBean));
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

    // ====== 搜尋 ======
    @ResponseBody
    @RequestMapping(value = { "/ajax/material_virtual_project.basil.AR" }, method = {
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

            packageBean = serviceFeign.getMaterialVirtualProjectSearch(packageService.beanToJson(packageBean));
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
    @ResponseBody
    @RequestMapping(value = { "/ajax/material_virtual_project.basil.AC" }, method = {
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

            packageBean = serviceFeign.setMaterialVirtualProjectAdd(packageService.beanToJson(packageBean));
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
    @ResponseBody
    @RequestMapping(value = { "/ajax/material_virtual_project.basil.AU" }, method = {
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

            packageBean = serviceFeign.setMaterialVirtualProjectModify(packageService.beanToJson(packageBean));
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

    // ====== 作廢 ======
    @Override
    String invalid(@RequestBody String jsonObject) {
        return null;
    }

    // ====== 刪除 ======
    @ResponseBody
    @RequestMapping(value = { "/ajax/material_virtual_project.basil.AD" }, method = {
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

            packageBean = serviceFeign.setMaterialVirtualProjectDelete(packageService.beanToJson(packageBean));
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

    // ====== BOM 展開分析報表 ======
    @ResponseBody
    @RequestMapping(value = { "/ajax/material_virtual_project.basil.expand" }, method = {
            RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    String report(@RequestBody String jsonObject) {
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

            packageBean = serviceFeign.getMaterialVirtualProjectReport(packageService.beanToJson(packageBean));
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

    @ResponseBody
    @RequestMapping(value = { "/ajax/material_virtual_project.basil.simulate" }, method = {
            RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    String simulateMvp(@RequestBody String jsonObject) {
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

            packageBean = serviceFeign.simulateMaterialVirtualProject(packageService.beanToJson(packageBean));
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

    // 取得其他使用者專案 API 代理
    @ResponseBody
    @RequestMapping(value = { "/ajax/material_virtual_project.basil.otherProjects" }, method = {
            RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    String otherProjects(@RequestBody String jsonObject) {
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

            packageBean = serviceFeign.getMaterialVirtualProjectOtherProjects(packageService.beanToJson(packageBean));
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

    // ====== 取得所有庫存料號字典 (Feature 10: Client-side Search Caching) ======
    @ResponseBody
    @RequestMapping(value = { "/ajax/material_virtual_project.basil.getInitSearchData" }, method = {
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

    // ====== 取得指定料號清單的即時庫存 (方案 B: 開啟即刷新) ======
    @ResponseBody
    @RequestMapping(value = { "/ajax/material_virtual_project.basil.getStocks" }, method = {
            RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    String getStocks(@RequestBody String jsonObject) {
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

            // 轉發給物料服務查詢即時庫存
            packageBean = serviceFeign.getMaterialShortageItemStock(packageService.beanToJson(packageBean));
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
