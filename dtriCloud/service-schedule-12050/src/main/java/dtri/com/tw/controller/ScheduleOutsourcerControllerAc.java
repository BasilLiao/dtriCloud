package dtri.com.tw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonObject;

import dtri.com.tw.service.ScheduleOutsourcerServiceAc;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;

@RestController
public class ScheduleOutsourcerControllerAc extends AbstractControllerAc {

	@Autowired
	private PackageService packageService;
	@Autowired
	private ScheduleOutsourcerServiceAc serviceAc;

	@RequestMapping(value = { "/scheduleOutsourcer/getSearch" }, method = {
			RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	PackageBean getSearch(@RequestBody String jsonObject) {
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

	@ResponseBody
	@RequestMapping(value = { "/scheduleOutsourcer/setModifySc" }, method = {
			RequestMethod.PUT }, produces = "application/json;charset=UTF-8")
	PackageBean setModifySc(@RequestBody String jsonObject) {// 生管
		// 顯示方法
		String funName = new Object() {
		}.getClass().getEnclosingMethod().getName();
		sysFunction(funName);

		// Step0.資料準備
		String packageJson = "{}";
		PackageBean packageBean = new PackageBean();
		try {
			// Step1.解包=>(String 轉換 JSON)=>(JSON 轉換 PackageBean)=> 檢查 => Pass
			JsonObject packageObject = packageService.StringToJson(jsonObject);
			packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);
			// Step2.執行=>服務
			loggerInf(funName + "[Start]", packageBean.getUserAccount());
			packageBean = serviceAc.setModify(packageBean, "sc");
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

	@ResponseBody
	@RequestMapping(value = { "/scheduleOutsourcer/setModifyMc" }, method = {
			RequestMethod.PUT }, produces = "application/json;charset=UTF-8")
	PackageBean setModifyMc(@RequestBody String jsonObject) {// 物控
		// 顯示方法
		String funName = new Object() {
		}.getClass().getEnclosingMethod().getName();
		sysFunction(funName);

		// Step0.資料準備
		String packageJson = "{}";
		PackageBean packageBean = new PackageBean();
		try {
			// Step1.解包=>(String 轉換 JSON)=>(JSON 轉換 PackageBean)=> 檢查 => Pass
			JsonObject packageObject = packageService.StringToJson(jsonObject);
			packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);
			// Step2.執行=>服務
			loggerInf(funName + "[Start]", packageBean.getUserAccount());
			packageBean = serviceAc.setModify(packageBean, "mc");
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

	@ResponseBody
	@RequestMapping(value = { "/scheduleOutsourcer/setModifyWm" }, method = {
			RequestMethod.PUT }, produces = "application/json;charset=UTF-8")
	PackageBean setModifyWm(@RequestBody String jsonObject) {// 倉儲
		// 顯示方法
		String funName = new Object() {
		}.getClass().getEnclosingMethod().getName();
		sysFunction(funName);

		// Step0.資料準備
		String packageJson = "{}";
		PackageBean packageBean = new PackageBean();
		try {
			// Step1.解包=>(String 轉換 JSON)=>(JSON 轉換 PackageBean)=> 檢查 => Pass
			JsonObject packageObject = packageService.StringToJson(jsonObject);
			packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);
			// Step2.執行=>服務
			loggerInf(funName + "[Start]", packageBean.getUserAccount());
			packageBean = serviceAc.setModify(packageBean, "wm");
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

	@ResponseBody
	@RequestMapping(value = { "/scheduleOutsourcer/setModifyMp" }, method = {
			RequestMethod.PUT }, produces = "application/json;charset=UTF-8")
	PackageBean setModifyMp(@RequestBody String jsonObject) {// 製造
		// 顯示方法
		String funName = new Object() {
		}.getClass().getEnclosingMethod().getName();
		sysFunction(funName);

		// Step0.資料準備
		String packageJson = "{}";
		PackageBean packageBean = new PackageBean();
		try {
			// Step1.解包=>(String 轉換 JSON)=>(JSON 轉換 PackageBean)=> 檢查 => Pass
			JsonObject packageObject = packageService.StringToJson(jsonObject);
			packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);
			// Step2.執行=>服務
			loggerInf(funName + "[Start]", packageBean.getUserAccount());
			packageBean = serviceAc.setModify(packageBean, "mp");
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

	@RequestMapping(value = { "/scheduleOutsourcer/getReport" }, method = {
			RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	PackageBean getReport(@RequestBody String jsonObject) {
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

	@RequestMapping(value = { "/scheduleOutsourcer/setInvalid" }, method = {
			RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	PackageBean setInvalid(@RequestBody String jsonObject) {
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

	@ResponseBody
	@RequestMapping(value = { "/scheduleOutsourcer/setDelete" }, method = {
			RequestMethod.DELETE }, produces = "application/json;charset=UTF-8")
	String setDelete(@RequestBody String jsonObject) {
		// 顯示方法
		String funName = new Object() {
		}.getClass().getEnclosingMethod().getName();
		sysFunction(funName);

		// Step0.資料準備
		String packageJson = "{}";
		return packageJson;
	}
}
