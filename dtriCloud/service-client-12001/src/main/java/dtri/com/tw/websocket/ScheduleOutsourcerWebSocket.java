package dtri.com.tw.websocket;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dtri.com.tw.pgsql.entity.ScheduleOutsourcer;
import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

/**
 * https://springdoc.cn/spring-boot-websocket/ </br>
 * 教學說明</br>
 **/
@Component
@ServerEndpoint(value = "/websocket/schedule_outsourcer/echo")
public class ScheduleOutsourcerWebSocket implements ApplicationContextAware {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleOutsourcerWebSocket.class);

	// 存入所有用戶
	private static Map<String, Session> sessionMap = new HashMap<>();
	// 暫存資料
	private static Map<String, ScheduleOutsourcer> mapOutsourcer = new HashMap<String, ScheduleOutsourcer>();

	// 全域靜態變量，保存 ApplicationContext
	private static ApplicationContext applicationContext;

	// 收到訊息
	/**
	 * @param messageJson {"user":"system","action":"leave/sendAllData/sendAllLock/sendAllUnlock","update":""}
	 * 
	 * 
	 **/
	@OnMessage
	public void onMessage(Session session, String messageJson) throws IOException {

		LOGGER.info("[websocket] 收到訊息：id={}，message={}", session.getId(), messageJson);

		// 資料解析
		JsonObject dataJson = new JsonObject();
		try {
			dataJson = JsonParser.parseString(messageJson).getAsJsonObject();
		} catch (Exception e) {
			return;
		}
		String userAcc = (String) session.getRequestParameterMap().get("userId").get(0);
		// 檢測是否沒有站存資料?
		if (mapOutsourcer.size() == 0) {

		}

		// 離開指令
		if (messageJson.equalsIgnoreCase("leave")) {
			// 由伺服器主動關閉連線。 狀態碼為 NORMAL_CLOSURE（正常關閉）。
			session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "leave...OK"));
			return;
		}

		// 鎖定指令->sendAllLock
		if (dataJson.get("action").getAsString().equalsIgnoreCase("sendAllLock")) {

		}

		// 解鎖指令->sendAllUnlock
		if (dataJson.get("action").getAsString().equalsIgnoreCase("sendAllUnlock")) {

		}

		// 更新暫存+同步指令->sendAllData
		if (dataJson.get("action").getAsString().equalsIgnoreCase("sendAllData")) {
			// 抓取資料庫
			

			// 更新暫存

			// 刷新所有人
			sessionMap.forEach((sessionAcc, v) -> {
				LOGGER.info("[websocket] 傳送訊息：Acc={}，message={}", sessionAcc, messageJson);
				v.getAsyncRemote().sendText("[" + Instant.now().toEpochMilli() + "] Hello " + messageJson);
			});
			return;
		}
	}

	// 連線開啟
	@OnOpen
	public void onOpen(Session session) {
		// 儲存 session 到對象
		String userAcc = (String) session.getRequestParameterMap().get("userId").get(0);
		sessionMap.put(userAcc, session);
		LOGGER.info("[websocket] 新的連線：id={}", session.getId() + ":" + userAcc);
	}

	// 連線關閉
	@OnClose
	public void onClose(Session session) throws IOException {
		String userId = (String) session.getUserProperties().get("userId");
		sessionMap.remove(userId);
		session.close();
		LOGGER.info("[websocket] 連線中斷：id={}", session.getId());
	}

	// 連線異常
	@OnError
	public void onError(Throwable throwable, Session session) throws IOException {
		LOGGER.info("[websocket] 連線例外：id={}，throwable={}", session.getId(), throwable.getMessage());
		// 關閉連線。 狀態碼為 UNEXPECTED_CONDITION（意料之外的異常）
		session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, throwable.getMessage()));
	}

	// 將 Spring 注入的 ApplicationContext 保存到靜態變數
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		ScheduleOutsourcerWebSocket.applicationContext = applicationContext;

	}
}