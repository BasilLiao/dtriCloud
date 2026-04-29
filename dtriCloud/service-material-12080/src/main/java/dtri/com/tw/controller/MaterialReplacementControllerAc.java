package dtri.com.tw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonObject;

import dtri.com.tw.service.MaterialReplacementServiceAc;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
/** 功能模組: 通用-替代料關聯 */
public class MaterialReplacementControllerAc extends AbstractControllerAc {

	@Autowired
	private PackageService packageService;
	@Autowired
	private MaterialReplacementServiceAc serviceAc;

	@RequestMapping(value = { "/materialReplacement/getSearch" }, method = {
			RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	PackageBean getSearch(@RequestBody String jsonObject) {
		// 1. 顯示方法名稱 (Log用)
		String funName = new Object() {
		}.getClass().getEnclosingMethod().getName();
		sysFunction(funName);

		// 2. 資料準備
		PackageBean packageBean = new PackageBean();
		try {
			// 3. 解包
			JsonObject packageObject = packageService.StringToJson(jsonObject);
			packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);

			// 4. 執行服務
			loggerInf(funName + "[Start]", packageBean.getUserAccount());
			if ("other_search".equals(packageBean.getCallBackFunction())) {
				packageBean = serviceAc.getData(packageBean);
			} else if ("other_preload".equals(packageBean.getCallBackFunction())) {
				packageBean = serviceAc.getPreloadData(packageBean);
			} else {
				packageBean = serviceAc.getSearch(packageBean);
			}

			loggerInf(funName + "[End]", packageBean.getUserAccount());

		} catch (JsonProcessingException e) {
			e.printStackTrace();
			loggerWarn(eStktToSg(e), packageBean.getUserAccount());
		} catch (CloudExceptionService e) {
			e.printStackTrace();
			loggerInf(e.toString(), packageBean.getUserAccount());
		} catch (Exception e) {
			e.printStackTrace();
			loggerWarn(eStktToSg(e), packageBean.getUserAccount());
			packageBean.setInfo(CloudExceptionService.W0000_en_US);
			packageBean.setInfoColor(CloudExceptionService.ErColor.danger + "");
		}

		return packageBean;
	}

	@RequestMapping(value = { "/materialReplacement/setAdd", "/materialReplacement/setModify" }, method = {
			RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	PackageBean setData(@RequestBody String jsonObject) {
		String funName = new Object() {
		}.getClass().getEnclosingMethod().getName();
		sysFunction(funName);
		PackageBean packageBean = new PackageBean();
		try {
			// 3. 解包
			JsonObject packageObject = packageService.StringToJson(jsonObject);
			packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);

			// 4. 執行服務
			loggerInf(funName + "[Start]", packageBean.getUserAccount());
			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
					.getRequest();
			String uri = request.getRequestURI();
			if ("other_search".equals(packageBean.getCallBackFunction())) {
				packageBean = serviceAc.doSave(packageBean);
			} else {
				if (uri.endsWith("/setAdd")) {
					packageBean = serviceAc.setAdd(packageBean);
				} else {
					packageBean = serviceAc.setModify(packageBean);
				}

			}
			loggerInf(funName + "[End]", packageBean.getUserAccount());

		} catch (JsonProcessingException e) {
			e.printStackTrace();
			loggerWarn(eStktToSg(e), packageBean.getUserAccount());
		} catch (CloudExceptionService e) {
			e.printStackTrace();
			loggerInf(e.toString(), packageBean.getUserAccount());
		} catch (Exception e) {
			e.printStackTrace();
			loggerWarn(eStktToSg(e), packageBean.getUserAccount());
			packageBean.setInfo(CloudExceptionService.W0000_en_US);
			packageBean.setInfoColor(CloudExceptionService.ErColor.danger + "");
		}

		return packageBean;
	}

	@RequestMapping(value = { "/materialReplacement/getReport" }, method = {
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

	@RequestMapping(value = { "/materialReplacement/setInvalid" }, method = {
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

	@RequestMapping(value = { "/materialReplacement/setDetele" }, method = {
			RequestMethod.POST }, produces = "application/json;charset=UTF-8")
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