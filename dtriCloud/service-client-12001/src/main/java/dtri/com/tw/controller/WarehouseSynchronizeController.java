package dtri.com.tw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonObject;

import dtri.com.tw.service.feign.WarehouseServiceFeign;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;
import jakarta.annotation.Resource;

@Controller
public class WarehouseSynchronizeController extends AbstractController {

	@Autowired
	private PackageService packageService;

	@Resource
	WarehouseServiceFeign serviceFeign;

	@ResponseBody
	@RequestMapping(value = { "/ajax/warehouse_synchronize.basil" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
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
			packageBean.setUserLanguaue(loginUser().getSystemUser().getSulanguage());// 語言
			packageBean.setUserAgentAccount(loginUser().getSystemUser().getSuaaccount());// 使用者(代理)

			// Step3.執行=>跨服->務執行
			packageBean = serviceFeign.getSynchronizeSearch(packageService.beanToJson(packageBean));

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
	@RequestMapping(value = { "/ajax/warehouse_synchronize.basil.AR" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
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
			packageBean.setUserLanguaue(loginUser().getSystemUser().getSulanguage());// 語言
			packageBean.setUserAgentAccount(loginUser().getSystemUser().getSuaaccount());// 使用者(代理)

			// Step3.執行=>跨服->務執行
			packageBean = serviceFeign.getSynchronizeSearch(packageService.beanToJson(packageBean));

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
	@RequestMapping(value = { "/ajax/warehouse_synchronize.basil.ARR" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	String report(@RequestBody String jsonObject) {
		// 顯示方法
		sysFunction(new Object() {
		}.getClass().getEnclosingMethod().getName());

		// Step0.資料準備
		String packageJson = "{}";
		return packageJson;
	}

	@ResponseBody
	@RequestMapping(value = { "/ajax/warehouse_synchronize.basil.AC" }, method = { RequestMethod.POST })
	String add(@RequestBody String jsonObject) {
		// 顯示方法
		sysFunction(new Object() {
		}.getClass().getEnclosingMethod().getName());

		// Step0.資料準備
		String packageJson = "{}";
		return packageJson;
	}

	@ResponseBody
	@RequestMapping(value = { "/ajax/warehouse_synchronize.basil.AU" }, method = { RequestMethod.PUT })
	String modify(@RequestBody String jsonObject) {
		// 顯示方法
		sysFunction(new Object() {
		}.getClass().getEnclosingMethod().getName());
		// Step0.資料準備
		String packageJson = "{}";
		return packageJson;
	}

	@ResponseBody
	@RequestMapping(value = { "/ajax/warehouse_synchronize.basil.S1" }, method = { RequestMethod.PUT })
	String modifySynchronizeQty(@RequestBody String jsonObject) {
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
			packageBean.setUserLanguaue(loginUser().getSystemUser().getSulanguage());// 語言
			packageBean.setUserAgentAccount(loginUser().getSystemUser().getSuaaccount());// 使用者(代理)

			// Step3.執行=>跨服->務執行
			packageBean = serviceFeign.setModifySynchronizeQty(packageService.beanToJson(packageBean));

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
	@RequestMapping(value = { "/ajax/warehouse_synchronize.basil.S2" }, method = { RequestMethod.PUT })
	String modifySynchronizeItem(@RequestBody String jsonObject) {
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
			packageBean.setUserLanguaue(loginUser().getSystemUser().getSulanguage());// 語言
			packageBean.setUserAgentAccount(loginUser().getSystemUser().getSuaaccount());// 使用者(代理)

			// Step3.執行=>跨服->務執行
			packageBean = serviceFeign.setModifySynchronizeItem(packageService.beanToJson(packageBean));

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
	@RequestMapping(value = { "/ajax/warehouse_synchronize.basil.S3" }, method = { RequestMethod.PUT })
	String modifySynchronizeDeleteAll(@RequestBody String jsonObject) {
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
			packageBean.setUserLanguaue(loginUser().getSystemUser().getSulanguage());// 語言
			packageBean.setUserAgentAccount(loginUser().getSystemUser().getSuaaccount());// 使用者(代理)

			// Step3.執行=>跨服->務執行
			packageBean = serviceFeign.setModifySynchronizeRemove(packageService.beanToJson(packageBean));

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
	@RequestMapping(value = { "/ajax/warehouse_synchronize.basil.AD" }, method = { RequestMethod.DELETE })
	String invalid(@RequestBody String jsonObject) {
		// 顯示方法
		sysFunction(new Object() {
		}.getClass().getEnclosingMethod().getName());
		// Step0.資料準備
		String packageJson = "{}";
		return packageJson;
	}

	@ResponseBody
	@RequestMapping(value = { "/ajax/warehouse_synchronize.basil.DD" }, method = { RequestMethod.DELETE })
	String delete(@RequestBody String jsonObject) {
		// 顯示方法
		sysFunction(new Object() {
		}.getClass().getEnclosingMethod().getName());
		// Step0.資料準備
		String packageJson = "{}";
		return packageJson;
	}

}
