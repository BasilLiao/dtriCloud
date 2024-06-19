package dtri.com.tw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonObject;

import dtri.com.tw.service.feign.BasicServiceFeign;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;
import jakarta.annotation.Resource;

@Controller
public class BasicProductModelController extends AbstractController {

	@Autowired
	private PackageService packageService;

	@Resource
	BasicServiceFeign serviceFeign;

	@ResponseBody
	@RequestMapping(value = { "/ajax/basic_product_model.basil" }, method = {
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
			packageBean = serviceFeign.getProductModelSearch(packageService.beanToJson(packageBean));
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
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			loggerWarn(eStktToSg(e), loginUser().getUsername());
		}
		return packageJson;
	}

	@ResponseBody
	@RequestMapping(value = { "/ajax/basic_product_model.basil.AR" }, method = {
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
			packageBean = serviceFeign.getProductModelSearch(packageService.beanToJson(packageBean));
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
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			loggerWarn(eStktToSg(e), loginUser().getUsername());
		}
		return packageJson;
	}

	@ResponseBody
	@RequestMapping(value = { "/ajax/basic_product_model.basil.ARR" }, method = {
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
			packageBean = serviceFeign.getProductModelReport(packageService.beanToJson(packageBean));
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
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			loggerWarn(eStktToSg(e), loginUser().getUsername());
		}
		return packageJson;
	}

	@ResponseBody
	@RequestMapping(value = { "/ajax/basic_product_model.basil.AC" }, method = { RequestMethod.POST })
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
			packageBean = serviceFeign.setProductModelAdd(packageService.beanToJson(packageBean));
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
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			loggerWarn(eStktToSg(e), loginUser().getUsername());
		}
		return packageJson;
	}

	@ResponseBody
	@RequestMapping(value = { "/ajax/basic_product_model.basil.AU" }, method = { RequestMethod.PUT })
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
			packageBean = serviceFeign.setProductModelModify(packageService.beanToJson(packageBean));
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
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			loggerWarn(eStktToSg(e), loginUser().getUsername());
		}
		return packageJson;
	}

	@ResponseBody
	@RequestMapping(value = { "/ajax/basic_product_model.basil.AD" }, method = { RequestMethod.DELETE })
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
			packageBean = serviceFeign.setProductModelInvalid(packageService.beanToJson(packageBean));
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
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			loggerWarn(eStktToSg(e), loginUser().getUsername());
		}
		return packageJson;
	}

	@ResponseBody
	@RequestMapping(value = { "/ajax/basic_product_model.basil.DD" }, method = { RequestMethod.DELETE })
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
			packageBean = serviceFeign.setProductModelDetele(packageService.beanToJson(packageBean));
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
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			loggerWarn(eStktToSg(e), loginUser().getUsername());
		}
		return packageJson;
	}

}
