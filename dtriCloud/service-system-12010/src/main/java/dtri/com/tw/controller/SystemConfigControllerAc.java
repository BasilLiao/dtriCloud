package dtri.com.tw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonObject;

import dtri.com.tw.service.SystemConfigServiceAc;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;

@RestController
public class SystemConfigControllerAc extends AbstractControllerAc {

	@Autowired
	private PackageService packageService;
	@Autowired
	private SystemConfigServiceAc serviceAc;

	@RequestMapping(value = { "/systemConfig/getSearch" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
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

	@RequestMapping(value = { "/systemConfig/getReport" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
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

	@RequestMapping(value = { "/systemConfig/setModify" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	PackageBean setModify(@RequestBody String jsonObject) {
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

	@RequestMapping(value = { "/systemConfig/setAdd" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	PackageBean setAdd(@RequestBody String jsonObject) {
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

	@RequestMapping(value = { "/systemConfig/setInvalid" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
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

	@RequestMapping(value = { "/systemConfig/setDetele" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	PackageBean setDetele(@RequestBody String jsonObject) {
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
