package dtri.com.tw.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import dtri.com.tw.websocket.ScheduleOutsourcerWebSocket;

@Controller
public class ScheduleOutsourcerControllerAC extends AbstractController {

	//@Autowired
	//private ScheduleOutsourcerWebSocket webSocket;

	// 廣播同步使用
	@RequestMapping(value = { "/clinet/schedule_outsourcer_synchronize_cell" }, method = {
			RequestMethod.POST }, produces = "application/json;charset=UTF-8")
	@ResponseBody
	String OutsourcerSynchronize(@RequestBody String jsonObject) {
		// 顯示方法
		String funName = new Object() {
		}.getClass().getEnclosingMethod().getName();
		sysFunction(funName);
		//
		System.out.println("" + jsonObject);
		boolean isOk = false;
		
		// 進行呼叫廣播給所有人更新資料
		if (jsonObject.equals("sendAllUsers")) {
//			try {
//				webSocket.onMessage(jsonObject);
//			} catch (IOException e) {
//				e.printStackTrace();
//				loggerWarn(eStktToSg(e), loginUser().getUsername());
//			}
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
