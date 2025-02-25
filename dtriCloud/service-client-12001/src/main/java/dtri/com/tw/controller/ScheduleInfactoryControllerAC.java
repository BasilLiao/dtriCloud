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

import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;
import dtri.com.tw.websocket.ScheduleInfactoryWebSocket;

@Controller
public class ScheduleInfactoryControllerAC extends AbstractController {

	@Autowired
	private ScheduleInfactoryWebSocket webSocket;
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
		// 進行呼叫廣播給所有人更新資料
		if (action.equals("sendAllData")) {
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
	String InfactorySynchronizeDft(@RequestBody String jsonObject) {

		// Step0.資料準備
		String packageJson = "{}";
		PackageBean packageBean = new PackageBean();
		try {
			// Step1.解包=>(String 轉換 JSON)=>(JSON 轉換 PackageBean)=> 檢查 => Pass
			JsonObject packageObject = packageService.StringToJson(jsonObject);
			packageBean = packageService.jsonToBean(packageObject.toString(), PackageBean.class);

			// Step4.取得 被動態標記資料->併入
			packageBean = ScheduleInfactoryWebSocket.getMapInfactoryTag(packageBean);
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
		return "OK";
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
