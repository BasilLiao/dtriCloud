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

import dtri.com.tw.websocket.ScheduleOutsourcerWebSocket;

@Controller
public class ScheduleOutsourcerControllerAC extends AbstractController {

	@Autowired
	private ScheduleOutsourcerWebSocket webSocket;

	// Service-呼叫用
	@RequestMapping(value = { "/websocket/schedule_outsourcer_service" }, method = {
			RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	@ResponseBody
	String OutsourcerSynchronize(@RequestBody String jsonObject) {
		// 顯示方法
		String funName = new Object() {
		}.getClass().getEnclosingMethod().getName();
		sysFunction(funName);
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
				loggerInf(funName + "[Start]" + jsonObject, "system");
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
