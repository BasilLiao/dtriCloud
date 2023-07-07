package dtri.com.tw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonObject;

import dtri.com.tw.bean.PackageBean;
import dtri.com.tw.service.CloudExceptionService;
import dtri.com.tw.service.PackageService;
import dtri.com.tw.service.SystemConfigService;

@Controller
public class SystemConfigController extends AbstractController {

	@Autowired
	private PackageService packageService;
	@Autowired
	private SystemConfigService configService;

	@ResponseBody
	@RequestMapping(value = { "/ajax/system_config.basil" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	String access(@RequestBody String jsonObject) {
		// 顯示方法
		sysFunction(new Object() {
		}.getClass().getEnclosingMethod().getName());
		// Step0.資料準備
		String packageJson = "{}";
		PackageBean packageBean = new PackageBean();
		try {
			// Step1.解包=>(String 轉換 JSON)=>(JSON 轉換 PackageBean)=> 檢查 => Pass
			JsonObject packageObject = packageService.StringToJson(jsonObject);
			packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);
			// Step2.執行=>服務項目
			packageBean = configService.getSearch(packageBean, null);
			// Step3.打包=>(轉換 PackageBean)=>包裝=>Json
			packageJson = packageService.beanToJson(packageBean);
		} catch (CloudExceptionService ex) {
			// Step4-1. 已知-故障回報
			loggerWarn(ex.toString());
			ex.getErrorCodeMessage();
		} catch (Exception e) {
			// Step4-2. 未知-故障回報
			loggerWarn(e.toString());
			e.printStackTrace();
			packageBean.setInfo(CloudExceptionService.W0000_en_US);
			packageBean.setInfoColor(CloudExceptionService.ErColor.danger);
		}
		return packageJson;
	}

	@ResponseBody
	@RequestMapping(value = { "/ajax/system_config.basil.AR" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	String search(@RequestBody String jsonObject) {
		// 顯示方法
		sysFunction(new Object() {
		}.getClass().getEnclosingMethod().getName());
		// Step0.資料準備
		String packageJson = "{}";
		PackageBean packageBean = new PackageBean();
		try {
			// Step1.解包=>(String 轉換 JSON)=>(JSON 轉換 PackageBean)=> 檢查 => Pass
			JsonObject packageObject = packageService.StringToJson(jsonObject);
			packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);
			// Step2.執行=>服務項目
			packageBean = configService.getSearch(packageBean, null);
			// Step3.打包=>(轉換 PackageBean)=>包裝=>Json
			packageJson = packageService.beanToJson(packageBean);
		} catch (CloudExceptionService ex) {
			// Step4-1. 已知-故障回報
			loggerWarn(ex.toString());
			ex.getErrorCodeMessage();
		} catch (Exception e) {
			// Step4-2. 未知-故障回報
			loggerWarn(e.toString());
			e.printStackTrace();
			packageBean.setInfo(CloudExceptionService.W0000_en_US);
			packageBean.setInfoColor(CloudExceptionService.ErColor.danger);
		}
		return packageJson;
	}

	@ResponseBody
	@RequestMapping(value = { "/ajax/system_config.basil.AU" }, method = { RequestMethod.PUT })
	String modify(@RequestBody String jsonObject) {
		return null;
	}

	@ResponseBody
	@RequestMapping(value = { "/ajax/system_config.basil.AU" }, method = { RequestMethod.DELETE })
	String delete(@RequestBody String jsonObject) {
		return null;
	}
}
