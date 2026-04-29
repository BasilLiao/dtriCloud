package dtri.com.tw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonObject;

import dtri.com.tw.service.MaterialVirtualProjectServiceAc;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;

@RestController
/** 功能模組: 設置-產品系列主建 (MVP) */
public class MaterialVirtualProjectControllerAc extends AbstractControllerAc {

    @Autowired
    private MaterialVirtualProjectServiceAc materialVirtualProjectService;

    @Autowired
    private PackageService packageService;

    // 初始化 / 查詢
    @RequestMapping(value = { "/materialVirtualProject/getSearch" }, method = {
            RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    PackageBean getSearch(@RequestBody String jsonObject) {
        String funName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sysFunction(funName);
        PackageBean packageBean = new PackageBean();
        try {
            JsonObject packageObject = packageService.StringToJson(jsonObject);
            packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);
            loggerInf(funName + "[Start]", packageBean.getUserAccount());
            materialVirtualProjectService.getSearch(packageBean);
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

    // 新增
    @RequestMapping(value = { "/materialVirtualProject/setAdd" }, method = {
            RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    PackageBean setAdd(@RequestBody String jsonObject) {
        String funName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sysFunction(funName);
        PackageBean packageBean = new PackageBean();
        try {
            JsonObject packageObject = packageService.StringToJson(jsonObject);
            packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);
            loggerInf(funName + "[Start]", packageBean.getUserAccount());
            materialVirtualProjectService.setAdd(packageBean);
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

    // 修改
    @RequestMapping(value = { "/materialVirtualProject/setModify" }, method = {
            RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    PackageBean setModify(@RequestBody String jsonObject) {
        String funName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sysFunction(funName);
        PackageBean packageBean = new PackageBean();
        try {
            JsonObject packageObject = packageService.StringToJson(jsonObject);
            packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);
            loggerInf(funName + "[Start]", packageBean.getUserAccount());
            materialVirtualProjectService.setModify(packageBean);
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

    // 刪除
    @RequestMapping(value = { "/materialVirtualProject/setDelete" }, method = {
            RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    PackageBean setDelete(@RequestBody String jsonObject) {
        String funName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sysFunction(funName);
        PackageBean packageBean = new PackageBean();
        try {
            JsonObject packageObject = packageService.StringToJson(jsonObject);
            packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);
            loggerInf(funName + "[Start]", packageBean.getUserAccount());
            materialVirtualProjectService.setDelete(packageBean);
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

    // BOM 展開分析 (核心功能)
    @RequestMapping(value = { "/materialVirtualProject/getReport" }, method = {
            RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    PackageBean getReport(@RequestBody String jsonObject) {
        String funName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sysFunction(funName);
        PackageBean packageBean = new PackageBean();
        try {
            JsonObject packageObject = packageService.StringToJson(jsonObject);
            packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);
            loggerInf(funName + "[Start]", packageBean.getUserAccount());
            materialVirtualProjectService.getReport(packageBean);
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

    // 取得全部 BOM 與料號字典檔供前端快取搜尋 (Feature 10: Client-side Search Caching)
    @RequestMapping(value = { "/materialVirtualProject/getInitSearchData" }, method = {
            RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    PackageBean getInitSearchData(@RequestBody String jsonObject) {
        String funName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sysFunction(funName);
        PackageBean packageBean = new PackageBean();
        try {
            JsonObject packageObject = packageService.StringToJson(jsonObject);
            packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);
            loggerInf(funName + "[Start]", packageBean.getUserAccount());
            materialVirtualProjectService.getInitSearchData(packageBean);
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

    // 齊套模擬引擎 (Feature 9)
    @RequestMapping(value = { "/materialVirtualProject/simulate" }, method = {
            RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    PackageBean simulateMvp(@RequestBody String jsonObject) {
        String funName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sysFunction(funName);
        PackageBean packageBean = new PackageBean();
        try {
            JsonObject packageObject = packageService.StringToJson(jsonObject);
            packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);
            loggerInf(funName + "[Start]", packageBean.getUserAccount());
            materialVirtualProjectService.simulateMvp(packageBean);
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

    // 取得其他使用者專案清單 (PM Feature 8)
    @RequestMapping(value = { "/materialVirtualProject/otherProjects" }, method = {
            RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    PackageBean otherProjects(@RequestBody String jsonObject) {
        String funName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        sysFunction(funName);
        PackageBean packageBean = new PackageBean();
        try {
            JsonObject packageObject = packageService.StringToJson(jsonObject);
            packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);
            loggerInf(funName + "[Start]", packageBean.getUserAccount());
            materialVirtualProjectService.getOtherProjects(packageBean);
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
