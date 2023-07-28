package dtri.com.tw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonObject;

import dtri.com.tw.service.feign.SystemServiceFeign;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;
import jakarta.annotation.Resource;

@Controller
public class SystemGroupController extends AbstractController {

	@Autowired
	private PackageService packageService;

	@Resource
	SystemServiceFeign systemServiceFeign;

	@ResponseBody
	@RequestMapping(value = { "/ajax/system_group.basil" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
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

			// Step2.基礎資料整理
			packageBean.setUserAccount(loginUser().getSystemUser().getSuaccount());// 使用者
			packageBean.setDetailMode(true);// 細節模式?

			// Step3.執行=>跨服->務執行
			packageBean = systemServiceFeign.getGroupSearch(packageService.beanToJson(packageBean));
			
		} catch (Exception e) {
			// StepX-2. 未知-故障回報
			loggerWarn(e.toString());
			e.printStackTrace();
			packageBean.setInfo(CloudExceptionService.W0000_en_US);
			packageBean.setInfoColor(CloudExceptionService.ErColor.danger + "");
		}

		// Step4.打包=>(轉換 PackageBean)=>包裝=>Json
		try {
			packageJson = packageService.beanToJson(packageBean);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			loggerWarn(e.toString());
		}
		return packageJson;
	}

	@ResponseBody
	@RequestMapping(value = { "/ajax/system_group.basil.AR" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
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

			// Step2.基礎資料整理
			packageBean.setUserAccount(loginUser().getSystemUser().getSuaccount());// 使用者
			packageBean.setDetailMode(true);// 細節模式?

			// Step3.執行=>跨服->務執行
			packageBean = systemServiceFeign.getGroupSearch(packageService.beanToJson(packageBean));
			
		} catch (Exception e) {
			// StepX-2. 未知-故障回報
			loggerWarn(e.toString());
			e.printStackTrace();
			packageBean.setInfo(CloudExceptionService.W0000_en_US);
			packageBean.setInfoColor(CloudExceptionService.ErColor.danger + "");
		}

		// Step4.打包=>(轉換 PackageBean)=>包裝=>Json
		try {
			packageJson = packageService.beanToJson(packageBean);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			loggerWarn(e.toString());
		}
		return packageJson;
	}

	@ResponseBody
	@RequestMapping(value = { "/ajax/system_group.basil.ARR" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	String report(@RequestBody String jsonObject) {
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

			// Step2.基礎資料整理
			packageBean.setUserAccount(loginUser().getSystemUser().getSuaccount());// 使用者
			packageBean.setDetailMode(true);// 細節模式?

			// Step3.執行=>跨服->務執行
			packageBean = systemServiceFeign.getGroupReport(packageService.beanToJson(packageBean));
			
		} catch (Exception e) {
			// StepX-2. 未知-故障回報
			loggerWarn(e.toString());
			e.printStackTrace();
			packageBean.setInfo(CloudExceptionService.W0000_en_US);
			packageBean.setInfoColor(CloudExceptionService.ErColor.danger + "");
		}

		// Step4.打包=>(轉換 PackageBean)=>包裝=>Json
		try {
			packageJson = packageService.beanToJson(packageBean);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			loggerWarn(e.toString());
		}
		return packageJson;
	}

	@ResponseBody
	@RequestMapping(value = { "/ajax/system_group.basil.AC" }, method = { RequestMethod.POST })
	String add(@RequestBody String jsonObject) {
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

			// Step2.基礎資料整理
			packageBean.setUserAccount(loginUser().getSystemUser().getSuaccount());// 使用者
			packageBean.setDetailMode(true);// 細節模式?

			// Step3.執行=>跨服->務執行
			packageBean = systemServiceFeign.setGroupAdd(packageService.beanToJson(packageBean));
		} catch (Exception e) {
			// StepX-2. 未知-故障回報
			loggerWarn(e.toString());
			e.printStackTrace();
			packageBean.setInfo(CloudExceptionService.W0000_en_US);
			packageBean.setInfoColor(CloudExceptionService.ErColor.danger + "");
		}

		// Step4.打包=>(轉換 PackageBean)=>包裝=>Json
		try {
			packageJson = packageService.beanToJson(packageBean);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			loggerWarn(e.toString());
		}
		return packageJson;
	}

	@ResponseBody
	@RequestMapping(value = { "/ajax/system_group.basil.AU" }, method = { RequestMethod.PUT })
	String modify(@RequestBody String jsonObject) {
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

			// Step2.基礎資料整理
			packageBean.setUserAccount(loginUser().getSystemUser().getSuaccount());// 使用者
			packageBean.setDetailMode(true);// 細節模式?

			// Step3.執行=>跨服->務執行
			packageBean = systemServiceFeign.setGroupModify(packageService.beanToJson(packageBean));
			
		} catch (Exception e) {
			// StepX-2. 未知-故障回報
			loggerWarn(e.toString());
			e.printStackTrace();
			packageBean.setInfo(CloudExceptionService.W0000_en_US);
			packageBean.setInfoColor(CloudExceptionService.ErColor.danger + "");
		}

		// Step4.打包=>(轉換 PackageBean)=>包裝=>Json
		try {
			packageJson = packageService.beanToJson(packageBean);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			loggerWarn(e.toString());
		}
		return packageJson;
	}

	@ResponseBody
	@RequestMapping(value = { "/ajax/system_group.basil.AD" }, method = { RequestMethod.DELETE })
	String invalid(@RequestBody String jsonObject) {
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

			// Step2.基礎資料整理
			packageBean.setUserAccount(loginUser().getSystemUser().getSuaccount());// 使用者
			packageBean.setDetailMode(true);// 細節模式?

			// Step3.執行=>跨服->務執行
			packageBean = systemServiceFeign.setGroupInvalid(packageService.beanToJson(packageBean));
			
		} catch (Exception e) {
			// StepX-2. 未知-故障回報
			loggerWarn(e.toString());
			e.printStackTrace();
			packageBean.setInfo(CloudExceptionService.W0000_en_US);
			packageBean.setInfoColor(CloudExceptionService.ErColor.danger + "");
		}

		// Step4.打包=>(轉換 PackageBean)=>包裝=>Json
		try {
			packageJson = packageService.beanToJson(packageBean);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			loggerWarn(e.toString());
		}
		return packageJson;
	}

	@ResponseBody
	@RequestMapping(value = { "/ajax/system_group.basil.DD" }, method = { RequestMethod.DELETE })
	String delete(@RequestBody String jsonObject) {
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

			// Step2.基礎資料整理
			packageBean.setUserAccount(loginUser().getSystemUser().getSuaccount());// 使用者
			packageBean.setDetailMode(true);// 細節模式?

			// Step3.執行=>跨服->務執行
			packageBean = systemServiceFeign.setGroupDetele(packageService.beanToJson(packageBean));
			
		} catch (Exception e) {
			// StepX-2. 未知-故障回報
			loggerWarn(e.toString());
			e.printStackTrace();
			packageBean.setInfo(CloudExceptionService.W0000_en_US);
			packageBean.setInfoColor(CloudExceptionService.ErColor.danger + "");
		}

		// Step4.打包=>(轉換 PackageBean)=>包裝=>Json
		try {
			packageJson = packageService.beanToJson(packageBean);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			loggerWarn(e.toString());
		}
		return packageJson;
	}

}
