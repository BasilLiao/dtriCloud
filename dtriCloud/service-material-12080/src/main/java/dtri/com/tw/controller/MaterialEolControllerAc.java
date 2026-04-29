package dtri.com.tw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonObject;

import dtri.com.tw.service.MaterialEolService;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;

@RestController
/** 功能模組: 通用-停損物料估算 */
public class MaterialEolControllerAc extends AbstractControllerAc {

    @Autowired
    private MaterialEolService materialEolService;

    @Autowired
    private PackageService packageService;

    @RequestMapping(value = { "/materialEol/getSearch" }, method = {
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
            packageBean = materialEolService.getSearch(packageBean);
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

    @RequestMapping(value = { "/materialEol/getReport" }, method = {
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
            packageBean = materialEolService.getReport(packageBean);
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
