package dtri.com.tw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;

import dtri.com.tw.service.feign.MaterialServiceFeign;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;

@RestController
public class MusUserSearchController extends AbstractController {

    @Autowired
    private MaterialServiceFeign serviceFeign;

    @Autowired
    private PackageService packageService;

    @Override
    String access(String jsonObject) {

        return null;
    }

    @Override
    String delete(String jsonObject) {

        return null;
    }

    @ResponseBody
    @RequestMapping(value = { "/ajax/mus_user_search.basil" }, method = {
            RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    String add(@RequestBody String jsonObject) {
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
            packageBean.setEntityIKeyGKey(String.valueOf(loginUser().getSystemUser().getSuid())); // userid
            // Step3.執行=>跨服->務執行
            packageBean = serviceFeign.setMusUserSearch(packageService.beanToJson(packageBean));
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
    String invalid(String jsonObject) {

        return null;
    }

    @Override
    String modify(String jsonObject) {

        return null;
    }

    @Override
    String search(String jsonObject) {

        return null;
    }

}
