package dtri.com.tw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonObject;

import dtri.com.tw.service.feign.BomServiceFeign;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;
import jakarta.annotation.Resource;

@Controller
public class BomNotificationController extends AbstractController {

	@Autowired
	private PackageService packageService;

	@Resource
	BomServiceFeign serviceFeign;

	@ResponseBody
	@RequestMapping(value = { "/ajax/bom_notification.basil" }, method = {
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

			// Step3.執行=>跨服->務執行
			packageBean = serviceFeign.getBomNotificationSearch(packageService.beanToJson(packageBean));
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
	@RequestMapping(value = { "/ajax/bom_notification.basil.AR" }, method = {
			RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	String search(@RequestBody String jsonObject) {
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

			// Step3.執行=>跨服->務執行
			packageBean = serviceFeign.getBomNotificationSearch(packageService.beanToJson(packageBean));
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
	@RequestMapping(value = { "/ajax/bom_notification.basil.ARR" }, method = {
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

			// Step3.執行=>跨服->務執行
			packageBean = serviceFeign.getBomNotificationReport(packageService.beanToJson(packageBean));
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
	@RequestMapping(value = { "/ajax/bom_notification.basil.AC" }, method = { RequestMethod.POST })
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

			// Step3.執行=>跨服->務執行
			packageBean = serviceFeign.setBomNotificationAdd(packageService.beanToJson(packageBean));
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
	@RequestMapping(value = { "/ajax/bom_notification.basil.AU" }, method = { RequestMethod.PUT })
	String modify(@RequestBody String jsonObject) {
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

			// Step3.執行=>跨服->務執行
			packageBean = serviceFeign.setBomNotificationModify(packageService.beanToJson(packageBean));
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
	@RequestMapping(value = { "/ajax/bom_notification.basil.AD" }, method = { RequestMethod.DELETE })
	String invalid(@RequestBody String jsonObject) {
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

			// Step3.執行=>跨服->務執行
			packageBean = serviceFeign.setBomNotificationInvalid(packageService.beanToJson(packageBean));
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
	@RequestMapping(value = { "/ajax/bom_notification.basil.DD" }, method = { RequestMethod.DELETE })
	String delete(@RequestBody String jsonObject) {
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

			// Step3.執行=>跨服->務執行
			packageBean = serviceFeign.setBomNotificationDetele(packageService.beanToJson(packageBean));
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
}
