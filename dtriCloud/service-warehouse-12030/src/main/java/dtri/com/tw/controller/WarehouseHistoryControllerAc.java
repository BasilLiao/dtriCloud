package dtri.com.tw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonObject;

import dtri.com.tw.service.WarehouseHistoryServiceAc;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;

@RestController
public class WarehouseHistoryControllerAc extends AbstractControllerAc {

	@Autowired
	private PackageService packageService;
	@Autowired
	private WarehouseHistoryServiceAc serviceAc;

	@RequestMapping(value = { "/warehouseHistory/getSearch" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	PackageBean getSearch(@RequestBody String jsonObject) {
		// 顯示方法
		sysFunction(new Object() {
		}.getClass().getEnclosingMethod().getName());
		// Step0.資料準備
		PackageBean packageBean = new PackageBean();

		try {
			// Step1.解包=>(String 轉換 JSON)=>(JSON 轉換 PackageBean)=> 檢查 => Pass
			JsonObject packageObject = packageService.StringToJson(jsonObject);
			packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);
			// Step2.執行=>服務
			packageBean = serviceAc.getSearch(packageBean);
		} catch (JsonProcessingException e) {
			// StepX-1. 已知-故障回報
			e.printStackTrace();
			loggerWarn(e.toString());
		} catch (CloudExceptionService e) {
			// StepX-2. 已知-故障回報
			e.printStackTrace();
			loggerWarn(e.toString());
		} catch (Exception e) {
			// StepX-3. 未知-故障回報
			loggerWarn(e.toString());
			e.printStackTrace();
			packageBean.setInfo(CloudExceptionService.W0000_en_US);
			packageBean.setInfoColor(CloudExceptionService.ErColor.danger + "");
		}
		return packageBean;
	}

	@RequestMapping(value = { "/warehouseHistory/getReport" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	PackageBean getReport(@RequestBody String jsonObject) {
		// 顯示方法
		sysFunction(new Object() {
		}.getClass().getEnclosingMethod().getName());
		// Step0.資料準備
		PackageBean packageBean = new PackageBean();

		try {
			// Step1.解包=>(String 轉換 JSON)=>(JSON 轉換 PackageBean)=> 檢查 => Pass
			JsonObject packageObject = packageService.StringToJson(jsonObject);
			packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);
			// Step2.執行=>服務
			packageBean = serviceAc.getReport(packageBean);
		} catch (JsonProcessingException e) {
			// StepX-1. 已知-故障回報
			e.printStackTrace();
			loggerWarn(e.toString());
		} catch (CloudExceptionService e) {
			// StepX-2. 已知-故障回報
			e.printStackTrace();
			loggerWarn(e.toString());
		} catch (Exception e) {
			// StepX-3. 未知-故障回報
			loggerWarn(e.toString());
			e.printStackTrace();
			packageBean.setInfo(CloudExceptionService.W0000_en_US);
			packageBean.setInfoColor(CloudExceptionService.ErColor.danger + "");
		}
		return packageBean;
	}

	@RequestMapping(value = { "/warehouseHistory/setModify" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	PackageBean setModify(@RequestBody String jsonObject) {
		// 顯示方法
		sysFunction(new Object() {
		}.getClass().getEnclosingMethod().getName());
		// Step0.資料準備
		PackageBean packageBean = new PackageBean();

		try {
			// Step1.解包=>(String 轉換 JSON)=>(JSON 轉換 PackageBean)=> 檢查 => Pass
			JsonObject packageObject = packageService.StringToJson(jsonObject);
			packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);
			// Step2.執行=>服務
			packageBean = serviceAc.setModify(packageBean);
		} catch (JsonProcessingException e) {
			// StepX-1. 已知-故障回報
			e.printStackTrace();
			loggerWarn(e.toString());
		} catch (CloudExceptionService e) {
			// StepX-2. 已知-故障回報
			e.printStackTrace();
			loggerWarn(e.toString());
		} catch (Exception e) {
			// StepX-3. 未知-故障回報
			loggerWarn(e.toString());
			e.printStackTrace();
			packageBean.setInfo(CloudExceptionService.W0000_en_US);
			packageBean.setInfoColor(CloudExceptionService.ErColor.danger + "");
		}
		return packageBean;
	}

	@RequestMapping(value = { "/warehouseHistory/setAdd" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	PackageBean setAdd(@RequestBody String jsonObject) {
		// 顯示方法
		sysFunction(new Object() {
		}.getClass().getEnclosingMethod().getName());
		// Step0.資料準備
		PackageBean packageBean = new PackageBean();

		try {
			// Step1.解包=>(String 轉換 JSON)=>(JSON 轉換 PackageBean)=> 檢查 => Pass
			JsonObject packageObject = packageService.StringToJson(jsonObject);
			packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);
			// Step2.執行=>服務
			packageBean = serviceAc.setAdd(packageBean);
		} catch (JsonProcessingException e) {
			// StepX-1. 已知-故障回報
			e.printStackTrace();
			loggerWarn(e.toString());
		} catch (CloudExceptionService e) {
			// StepX-2. 已知-故障回報
			e.printStackTrace();
			loggerWarn(e.toString());
		} catch (Exception e) {
			// StepX-3. 未知-故障回報
			loggerWarn(e.toString());
			e.printStackTrace();
			packageBean.setInfo(CloudExceptionService.W0000_en_US);
			packageBean.setInfoColor(CloudExceptionService.ErColor.danger + "");
		}
		return packageBean;
	}

	@RequestMapping(value = { "/warehouseHistory/setInvalid" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	PackageBean setInvalid(@RequestBody String jsonObject) {
		// 顯示方法
		sysFunction(new Object() {
		}.getClass().getEnclosingMethod().getName());
		// Step0.資料準備
		PackageBean packageBean = new PackageBean();

		try {
			// Step1.解包=>(String 轉換 JSON)=>(JSON 轉換 PackageBean)=> 檢查 => Pass
			JsonObject packageObject = packageService.StringToJson(jsonObject);
			packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);
			// Step2.執行=>服務
			packageBean = serviceAc.setInvalid(packageBean);
		} catch (JsonProcessingException e) {
			// StepX-1. 已知-故障回報
			e.printStackTrace();
			loggerWarn(e.toString());
		} catch (CloudExceptionService e) {
			// StepX-2. 已知-故障回報
			e.printStackTrace();
			loggerWarn(e.toString());
		} catch (Exception e) {
			// StepX-3. 未知-故障回報
			loggerWarn(e.toString());
			e.printStackTrace();
			packageBean.setInfo(CloudExceptionService.W0000_en_US);
			packageBean.setInfoColor(CloudExceptionService.ErColor.danger + "");
		}
		return packageBean;
	}

	@RequestMapping(value = { "/warehouseHistory/setDetele" }, method = { RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	PackageBean setDetele(@RequestBody String jsonObject) {
		// 顯示方法
		sysFunction(new Object() {
		}.getClass().getEnclosingMethod().getName());
		// Step0.資料準備
		PackageBean packageBean = new PackageBean();

		try {
			// Step1.解包=>(String 轉換 JSON)=>(JSON 轉換 PackageBean)=> 檢查 => Pass
			JsonObject packageObject = packageService.StringToJson(jsonObject);
			packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);
			// Step2.執行=>服務
			packageBean = serviceAc.setDetele(packageBean);
		} catch (JsonProcessingException e) {
			// StepX-1. 已知-故障回報
			e.printStackTrace();
			loggerWarn(e.toString());
		} catch (CloudExceptionService e) {
			// StepX-2. 已知-故障回報
			e.printStackTrace();
			loggerWarn(e.toString());
		} catch (Exception e) {
			// StepX-3. 未知-故障回報
			loggerWarn(e.toString());
			e.printStackTrace();
			packageBean.setInfo(CloudExceptionService.W0000_en_US);
			packageBean.setInfoColor(CloudExceptionService.ErColor.danger + "");
		}
		return packageBean;
	}
}