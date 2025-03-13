package dtri.com.tw.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dtri.com.tw.service.feign.ScheduleServiceFeign;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;
import dtri.com.tw.websocket.ScheduleInfactoryWebSocket;
import jakarta.annotation.Resource;

@Controller
public class ScheduleInfactoryControllerAC extends AbstractController {

	@Autowired
	private ScheduleInfactoryWebSocket webSocket;
	@Resource
	ScheduleServiceFeign serviceFeign;
	@Autowired
	private PackageService packageService;

	// Service-呼叫用
	@RequestMapping(value = { "/websocket/schedule_infactory_service" }, method = {
			RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	@ResponseBody
	String InfactorySynchronize(@RequestBody String jsonObject) {
		// 顯示方法(資料量太大會卡)
		/*
		 * String funName = new Object() { }.getClass().getEnclosingMethod().getName();
		 * sysFunction(funName);
		 */
		// System.out.println("" + jsonObject);
		// 資料解析準備
		boolean isOk = false;
		JsonObject getJson = new JsonObject();
		getJson = (JsonObject) JsonParser.parseString(jsonObject);
		String action = getJson.get("action").getAsString();
		String update = getJson.get("update").getAsString();
		// 進行呼叫廣播給所有人更新資料(傳給所有人/清除所有標記/更新倉庫資訊)
		if (action.equals("sendAllData") || action.equals("sendAllClearShow")|| action.equals("sendWarehouseUpdate")) {
			try {
				// loggerInf(funName + "[Start]" + jsonObject, "system");
				// {"user":"system","action":"leave/sendAllData/sendAllLock/sendAllUnlock","update":""}
				JsonObject sendMessage = new JsonObject();
				sendMessage.addProperty("user", "system");
				sendMessage.addProperty("action", action);
				sendMessage.addProperty("update", update);

				webSocket.onMessage(null, sendMessage.toString());
			} catch (IOException e) {
				e.printStackTrace();
				loggerWarn(eStktToSg(e), "system");
			}
			isOk = true;
		}
		//

		return "" + isOk;
	}

	// Service-呼叫用
	@RequestMapping(value = { "/websocket/schedule_infactory_dft_service" }, method = {
			RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	@ResponseBody
	PackageBean InfactorySynchronizeDft(@RequestBody String jsonObject) {

		// Step0.資料準備
		PackageBean packageBean = new PackageBean();
		try {
			// Step2.基礎資料整理
			packageBean.setUserAccount("System");// 使用者
			packageBean.setUserLanguaue("TW");// 語言
			packageBean.setUserAgentAccount("System");// 使用者(代理)
			// Step3.執行=>跨服->務執行
			packageBean = serviceFeign.getScheduleInfactorySearch(packageService.beanToJson(packageBean));
			// Step4.取得 被動態標記資料->併入
			packageBean = ScheduleInfactoryWebSocket.getMapInfactoryOnlyTag(packageBean);
		} catch (Exception e) {
			// StepX-2. 未知-故障回報
			e.printStackTrace();
			loggerWarn(eStktToSg(e), loginUser().getUsername());
			packageBean.setInfo(CloudExceptionService.W0000_en_US);
			packageBean.setInfoColor(CloudExceptionService.ErColor.danger + "");
		}
		return packageBean;
	}

	@Override
	String access(String jsonObject) {
		return null;
	}

	@Override
	String search(String jsonObject) {
		return null;
	}

	@Override
	String add(String jsonObject) {
		return null;
	}

	@Override
	String modify(String jsonObject) {
		return null;
	}

	@Override
	String invalid(String jsonObject) {
		return null;
	}

	@Override
	String delete(String jsonObject) {
		return null;
	}

}
