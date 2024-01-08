package dtri.com.tw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonObject;

import dtri.com.tw.service.BiosVersionServiceAc;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;

@RestController
public class BiosVersionControllerAc extends AbstractControllerAc {

	@Autowired
	private PackageService packageService;
	@Autowired
	private BiosVersionServiceAc serviceAc;

	@RequestMapping(value = { "/biosVersion/getSearch" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	PackageBean getBiosVersionSearch(@RequestBody String jsonObject) {
		// 顯示方法
		String funName = new Object() {
		}.getClass().getEnclosingMethod().getName();
		sysFunction(funName);
		// Step0.資料準備
		PackageBean packageBean = new PackageBean();

		try {
			// Step1.解包=>(String 轉換 JSON)=>(JSON 轉換 PackageBean)=> 檢查 => Pass
			JsonObject packageObject = packageService.StringToJson(jsonObject);
			packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);
			// Step2.執行=>服務
			loggerInf(funName + "[Start]", packageBean.getUserAccount());
			packageBean = serviceAc.getSearch(packageBean);
			loggerInf(funName + "[End]", packageBean.getUserAccount());
		} catch (JsonProcessingException e) {
			// StepX-1. 已知-故障回報
			e.printStackTrace();
			loggerWarn(eStktToSg(e), packageBean.getUserAccount());
		} catch (CloudExceptionService e) {
			// StepX-2. 已知-故障回報
			e.printStackTrace();
			loggerInf(e.toString(), packageBean.getUserAccount());
		} catch (Exception e) {
			// StepX-3. 未知-故障回報
			e.printStackTrace();
			loggerWarn(eStktToSg(e), packageBean.getUserAccount());
			packageBean.setInfo(CloudExceptionService.W0000_en_US);
			packageBean.setInfoColor(CloudExceptionService.ErColor.danger + "");
		}
		return packageBean;
	}

	@RequestMapping(value = { "/biosVersion/getReport" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	PackageBean getBiosVersionReport(@RequestBody String jsonObject) {
		// 顯示方法
		String funName = new Object() {
		}.getClass().getEnclosingMethod().getName();
		sysFunction(funName);
		// Step0.資料準備
		PackageBean packageBean = new PackageBean();

		try {
			// Step1.解包=>(String 轉換 JSON)=>(JSON 轉換 PackageBean)=> 檢查 => Pass
			JsonObject packageObject = packageService.StringToJson(jsonObject);
			packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);
			// Step2.執行=>服務
			loggerInf(funName + "[Start]", packageBean.getUserAccount());
			packageBean = serviceAc.getReport(packageBean);
			loggerInf(funName + "[End]", packageBean.getUserAccount());
		} catch (JsonProcessingException e) {
			// StepX-1. 已知-故障回報
			e.printStackTrace();
			loggerWarn(eStktToSg(e), packageBean.getUserAccount());
		} catch (CloudExceptionService e) {
			// StepX-2. 已知-故障回報
			e.printStackTrace();
			loggerInf(e.toString(), packageBean.getUserAccount());
		} catch (Exception e) {
			// StepX-3. 未知-故障回報
			e.printStackTrace();
			loggerWarn(eStktToSg(e), packageBean.getUserAccount());
			packageBean.setInfo(CloudExceptionService.W0000_en_US);
			packageBean.setInfoColor(CloudExceptionService.ErColor.danger + "");
		}
		return packageBean;
	}

	@RequestMapping(value = { "/biosVersion/setInvalid" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	PackageBean setBiosVersionInvalid(@RequestBody String jsonObject) {
		// 顯示方法
		String funName = new Object() {
		}.getClass().getEnclosingMethod().getName();
		sysFunction(funName);
		// Step0.資料準備
		PackageBean packageBean = new PackageBean();

		try {
			// Step1.解包=>(String 轉換 JSON)=>(JSON 轉換 PackageBean)=> 檢查 => Pass
			JsonObject packageObject = packageService.StringToJson(jsonObject);
			packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);
			// Step2.執行=>服務
			loggerInf(funName + "[Start]", packageBean.getUserAccount());
			packageBean = serviceAc.setInvalid(packageBean);
			loggerInf(funName + "[End]", packageBean.getUserAccount());
		} catch (JsonProcessingException e) {
			// StepX-1. 已知-故障回報
			e.printStackTrace();
			loggerWarn(eStktToSg(e), packageBean.getUserAccount());
		} catch (CloudExceptionService e) {
			// StepX-2. 已知-故障回報
			e.printStackTrace();
			loggerInf(e.toString(), packageBean.getUserAccount());
		} catch (Exception e) {
			// StepX-3. 未知-故障回報
			e.printStackTrace();
			loggerWarn(eStktToSg(e), packageBean.getUserAccount());
			packageBean.setInfo(CloudExceptionService.W0000_en_US);
			packageBean.setInfoColor(CloudExceptionService.ErColor.danger + "");
		}
		return packageBean;
	}
	@RequestMapping(value = { "/biosVersion/setModify" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	PackageBean setBiosVersionModify(@RequestBody String jsonObject) {
		// 顯示方法
		String funName = new Object() {
		}.getClass().getEnclosingMethod().getName();
		sysFunction(funName);
		// Step0.資料準備
		PackageBean packageBean = new PackageBean();

		try {
			// Step1.解包=>(String 轉換 JSON)=>(JSON 轉換 PackageBean)=> 檢查 => Pass
			JsonObject packageObject = packageService.StringToJson(jsonObject);
			packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);
			// Step2.執行=>服務
			loggerInf(funName + "[Start]", packageBean.getUserAccount());
			packageBean = serviceAc.setModify(packageBean);
			loggerInf(funName + "[End]", packageBean.getUserAccount());
		} catch (JsonProcessingException e) {
			// StepX-1. 已知-故障回報
			e.printStackTrace();
			loggerWarn(eStktToSg(e), packageBean.getUserAccount());
		} catch (CloudExceptionService e) {
			// StepX-2. 已知-故障回報
			e.printStackTrace();
			loggerInf(e.toString(), packageBean.getUserAccount());
		} catch (Exception e) {
			// StepX-3. 未知-故障回報
			e.printStackTrace();
			loggerWarn(eStktToSg(e), packageBean.getUserAccount());
			packageBean.setInfo(CloudExceptionService.W0000_en_US);
			packageBean.setInfoColor(CloudExceptionService.ErColor.danger + "");
		}
		return packageBean;
	}

	@RequestMapping(value = { "/biosVersion/setAdd" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	PackageBean setBiosVersionAdd(@RequestBody String jsonObject) {
		// 顯示方法
		String funName = new Object() {
		}.getClass().getEnclosingMethod().getName();
		sysFunction(funName);
		// Step0.資料準備
		PackageBean packageBean = new PackageBean();

		try {
			// Step1.解包=>(String 轉換 JSON)=>(JSON 轉換 PackageBean)=> 檢查 => Pass
			JsonObject packageObject = packageService.StringToJson(jsonObject);
			packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);
			// Step2.執行=>服務
			loggerInf(funName + "[Start]", packageBean.getUserAccount());
			packageBean = serviceAc.setAdd(packageBean);
			loggerInf(funName + "[End]", packageBean.getUserAccount());
		} catch (JsonProcessingException e) {
			// StepX-1. 已知-故障回報
			e.printStackTrace();
			loggerWarn(eStktToSg(e), packageBean.getUserAccount());
		} catch (CloudExceptionService e) {
			// StepX-2. 已知-故障回報
			e.printStackTrace();
			loggerInf(e.toString(), packageBean.getUserAccount());
		} catch (Exception e) {
			// StepX-3. 未知-故障回報
			e.printStackTrace();
			loggerWarn(eStktToSg(e), packageBean.getUserAccount());
			packageBean.setInfo(CloudExceptionService.W0000_en_US);
			packageBean.setInfoColor(CloudExceptionService.ErColor.danger + "");
		}
		return packageBean;
	}


	@RequestMapping(value = { "/biosVersion/setDetele" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	PackageBean setBiosVersionDetele(@RequestBody String jsonObject) {
		// 顯示方法
		String funName = new Object() {
		}.getClass().getEnclosingMethod().getName();
		sysFunction(funName);
		// Step0.資料準備
		PackageBean packageBean = new PackageBean();

		try {
			// Step1.解包=>(String 轉換 JSON)=>(JSON 轉換 PackageBean)=> 檢查 => Pass
			JsonObject packageObject = packageService.StringToJson(jsonObject);
			packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);
			// Step2.執行=>服務
			loggerInf(funName + "[Start]", packageBean.getUserAccount());
			packageBean = serviceAc.setDetele(packageBean);
			loggerInf(funName + "[End]", packageBean.getUserAccount());
		} catch (JsonProcessingException e) {
			// StepX-1. 已知-故障回報
			e.printStackTrace();
			loggerWarn(eStktToSg(e), packageBean.getUserAccount());
		} catch (CloudExceptionService e) {
			// StepX-2. 已知-故障回報
			e.printStackTrace();
			loggerInf(e.toString(), packageBean.getUserAccount());
		} catch (Exception e) {
			// StepX-3. 未知-故障回報
			e.printStackTrace();
			loggerWarn(eStktToSg(e), packageBean.getUserAccount());
			packageBean.setInfo(CloudExceptionService.W0000_en_US);
			packageBean.setInfoColor(CloudExceptionService.ErColor.danger + "");
		}
		return packageBean;
	}
}
