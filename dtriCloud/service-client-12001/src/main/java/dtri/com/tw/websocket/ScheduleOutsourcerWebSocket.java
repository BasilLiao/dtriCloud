package dtri.com.tw.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dtri.com.tw.pgsql.dao.ScheduleOutsourcerDao;
import dtri.com.tw.pgsql.entity.ScheduleOutsourcer;
import dtri.com.tw.shared.Fm_T;
import dtri.com.tw.shared.PackageService;
import jakarta.websocket.CloseReason;
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
@ServerEndpoint(value = "/websocket/schedule_outsourcer_client/echo")
public class ScheduleOutsourcerWebSocket implements ApplicationContextAware {

	private static ScheduleOutsourcerDao scheduleOutsourcerDao;

	private static PackageService packageService;

	private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleOutsourcerWebSocket.class);

	private static ApplicationContext applicationContext;// 為了讓Bean 物建化

	// 存入所有用戶
	private static Map<String, Session> sessionMap = new HashMap<>();
	// 暫存資料
	private static Map<Long, ScheduleOutsourcer> mapOutsourcer = new LinkedHashMap<Long, ScheduleOutsourcer>();
	private static Map<Long, JsonObject> mapOutsourcerTag = new HashMap<Long, JsonObject>();

	// 收到訊息
	/**
	 * @param messageJson {"user":"system","action":"leave/sendAllData/sendOnlyData/sendAllLock/sendAllUnlock",<br>
	 *                    "update":ScheduleOutsourcer(物件)<br>
	 *                    "status":成功失敗?<br>
	 *                    }<br>
	 **/
	@OnMessage
	public void onMessage(Session session, String messageJson) throws IOException {
		if (scheduleOutsourcerDao == null) {
			scheduleOutsourcerDao = getBean(ScheduleOutsourcerDao.class);
		}
		if (packageService == null) {
			packageService = getBean(PackageService.class);
		}
		// Step0.資料準備
		ArrayList<String> leaves = new ArrayList<String>();// 可能要離開或是異常的Client
		ArrayList<ScheduleOutsourcer> entityDatas = new ArrayList<>();// 接收-要更新或是修改資料的資料
		JsonObject dataJson = new JsonObject();
		String action = "";
		String userAcc = "Server";
		String sessionOnlyAcc = "";// 單一?
		try {
			// Step1.資料解析
			dataJson = JsonParser.parseString(messageJson).getAsJsonObject();
			action = dataJson.get("action").getAsString();// 執行類?
			if (!dataJson.get("update").getAsString().equals("")) {
				entityDatas = packageService.jsonToBean(dataJson.get("update").getAsString(),
						new TypeReference<ArrayList<ScheduleOutsourcer>>() {
						});
			}
			// 檢測是否沒有站存資料?
			if (mapOutsourcer.size() == 0) {
				// Step2.排序
				List<Order> orders = new ArrayList<>();
				orders.add(new Order(Direction.ASC, "soodate"));// 預計開工日
				orders.add(new Order(Direction.ASC, "syssort"));// 排序
				orders.add(new Order(Direction.ASC, "sonb"));// 工單
				// 一般模式
				PageRequest pageable = PageRequest.of(0, 1000, Sort.by(orders));
				scheduleOutsourcerDao.findAllBySearch(null, null, null, null, pageable).forEach(o -> {
					if (!mapOutsourcer.containsKey(o.getSoid())) {
						mapOutsourcer.put(o.getSoid(), o);
					}
					JsonObject tagString = new JsonObject();
					tagString.addProperty("all", "");
					// 生管
					tagString.addProperty("sofodate", "");
					tagString.addProperty("sofokdate", "");
					tagString.addProperty("soscstatus", "");
					tagString.addProperty("soscnote", "");
					// 物控
					tagString.addProperty("somcnote", "");
					tagString.addProperty("somcstatus", "");
					tagString.addProperty("somcdate", "");
					// 倉庫
					tagString.addProperty("sowmnote", "");
					tagString.addProperty("sowmprogress", "");
					// 製造
					tagString.addProperty("sompnote", "");
					tagString.addProperty("sompprogress", "");
					// 單據
					tagString.addProperty("sorqty", "");
					tagString.addProperty("sookqty", "");
					tagString.addProperty("sostatus", "");
					tagString.addProperty("sonote", "");
					tagString.addProperty("sofname", "");
					// Locked
					tagString.addProperty("locked", false);
					tagString.addProperty("lockedtime", 0L);
					tagString.addProperty("lockeduser", "");
					mapOutsourcerTag.put(o.getSoid(), tagString);
				});
			}
			// User?
			if (session != null) {
				userAcc = (String) session.getRequestParameterMap().get("userAcc").get(0);
				if (action.equals("sendOnlyData")) {// 單一用戶?
					sessionOnlyAcc = userAcc;
				}
			} else {
				// Server 來的要把資料轉換(生館)
				entityDatas.forEach(n -> {
					if (!n.getSoscnote().equals("[]")) {
						String soscnote = JsonParser.parseString(n.getSoscnote()).getAsJsonArray().get(0)
								.getAsJsonObject().get("content").getAsString();
						n.setSoscnote(soscnote);
					}
				});
			}

			// Step2.執行
			JsonObject dataJsonRe = updateMapOutsourcer(action, entityDatas, userAcc);
			// Step3.回傳 (多人 or 單人)
			if (sessionOnlyAcc.equals("")) {
				sessionMap.forEach((sessionAcc, v) -> {
					try {
						LOGGER.info("[websocket] 傳送訊息：Acc={}", sessionAcc);
						v.getAsyncRemote().sendText(dataJsonRe.toString());
					} catch (Exception e) {
						// SomethingError
						LOGGER.warn("[websocket] 傳送訊息：" + e);
						leaves.add(sessionAcc);
					}
				});
			} else if (!action.equals("leave")) {// 不是離開指令
				try {
					Session only = sessionMap.get(sessionOnlyAcc);
					LOGGER.info("[websocket] 傳送訊息：Acc={}", sessionOnlyAcc);
					only.getAsyncRemote().sendText(dataJsonRe.toString());
				} catch (Exception e) {
					// SomethingError
					LOGGER.warn("[websocket] 傳送訊息：" + e);
					leaves.add(sessionOnlyAcc);
				}
			}
			// step Last. 關閉異常
			leaves.forEach(userAccLeave -> {
				sessionMap.remove(userAccLeave);
			});
		} catch (Exception e) {
			LOGGER.warn("[websocket] 傳送訊息：" + e);
			LOGGER.info("[websocket] 錯誤訊息：message={}", messageJson);
			return;
		}
		return;
	}

	// 連線開啟
	@OnOpen
	public void onOpen(Session session) {
		// 儲存 session 到對象
		String userAcc = (String) session.getRequestParameterMap().get("userAcc").get(0);
		// 重新刷新?
		if (sessionMap.containsKey(userAcc)) {
			sessionMap.remove(userAcc);
		}
		sessionMap.put(userAcc, session);
		LOGGER.info("[websocket] 新的連線：id={}", session.getId() + ":" + userAcc);
	}

	// 連線關閉
	@OnClose
	public void onClose(Session session) throws IOException {
		String userAcc = (String) session.getUserProperties().get("userAcc");
		sessionMap.remove(userAcc);
		session.close();
		LOGGER.info("[websocket] 連線中斷：id={}", session.getId());
	}

	// 連線異常
	@OnError
	public void onError(Throwable throwable, Session session) throws IOException {
		String userAcc = (String) session.getUserProperties().get("userAcc");
		sessionMap.remove(userAcc);
		session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, throwable.getMessage()));
		// 關閉連線。 狀態碼為 UNEXPECTED_CONDITION（意料之外的異常）
		LOGGER.info("[websocket] 連線例外：id={}，throwable={}", session.getId(), throwable.getMessage());
	}

	/**
	 * @param action    sendAllData/sendOnlyData/sendAllLock/sendAllUnlock
	 * @param arrayList 要更新的資料?
	 * @param userAcc   使用者帳號(Key)
	 * @param session
	 **/
	// 修正資料鎖
	private synchronized JsonObject updateMapOutsourcer(String action, ArrayList<ScheduleOutsourcer> arrayList,
			String userAcc) throws Exception {
		Boolean ok = false;
		JsonObject dataJsonRe = new JsonObject();
		ArrayList<ScheduleOutsourcer> entityDatasRe = new ArrayList<>();// 回傳-要更新或是修改資料的資料
		LOGGER.info("[websocket] 收到訊息：Acc={}，action={}", userAcc, action);
		/**
		 * all
		 * 
		 * //生管 sofodate sofokdate soscstatus soscnote
		 * 
		 * //物控 somcnote somcstatus somcdate
		 * 
		 * //倉庫 sowmnote sowmprogress
		 * 
		 * //製造 sompnote sompprogress
		 * 
		 * //單據 sorqty sookqty sostatus sonote sofname
		 * 
		 * //鎖定 locked lockedtime lockeduser (綁定帳號)
		 */
		// 檢查是否標記資料?
		if (arrayList.size() > 0) {
			for (ScheduleOutsourcer n : arrayList) {
				if (!mapOutsourcerTag.containsKey(n.getSoid())) {
					JsonObject tagString = new JsonObject();
					tagString.addProperty("all", Fm_T.to_y_M_d(new Date()));
					// 生管
					tagString.addProperty("sofodate", "");
					tagString.addProperty("sofokdate", "");
					tagString.addProperty("soscstatus", "");
					tagString.addProperty("soscnote", "");
					// 物控
					tagString.addProperty("somcnote", "");
					tagString.addProperty("somcstatus", "");
					tagString.addProperty("somcdate", "");
					// 倉庫
					tagString.addProperty("sowmnote", "");
					tagString.addProperty("sowmprogress", "");
					// 製造
					tagString.addProperty("sompnote", "");
					tagString.addProperty("sompprogress", "");
					// 單據
					tagString.addProperty("sorqty", "");
					tagString.addProperty("sookqty", "");
					tagString.addProperty("sostatus", "");
					tagString.addProperty("sonote", "");
					tagString.addProperty("sofname", "");
					// Locked
					tagString.addProperty("locked", false);
					tagString.addProperty("lockedtime", 0L);
					tagString.addProperty("lockeduser", "");
					mapOutsourcerTag.put(n.getSoid(), tagString);
				}
			}
		}

		switch (action) {
		case "sendAllData":// 用處: 資料庫取出->更新暫存資料->給所有用戶/
			// Step4.更新標記內容
			if (arrayList.size() > 0) {
				JsonObject tagString = new JsonObject();
				ScheduleOutsourcer o = new ScheduleOutsourcer();
				for (ScheduleOutsourcer n : arrayList) {
					tagString = mapOutsourcerTag.get(n.getSoid());
					o = mapOutsourcer.get(n.getSoid());
					// 生管
					if (!n.getSofodate().equals("") && !n.getSofodate().equals(o.getSofodate())) {
						tagString.addProperty("sofodate", Fm_T.to_y_M_d(new Date()));
					}
					if (!n.getSofokdate().equals("") && !n.getSofokdate().equals(o.getSofokdate())) {
						tagString.addProperty("sofokdate", Fm_T.to_y_M_d(new Date()));
					}
					if (!n.getSoscstatus().equals(0) && !n.getSoscstatus().equals(o.getSoscstatus())) {
						tagString.addProperty("soscstatus", Fm_T.to_y_M_d(new Date()));
					}
					if (!n.getSoscnote().equals("[]")) {
						JsonArray soscnotes = JsonParser.parseString(o.getSoscnote()).getAsJsonArray();
						String scnoteNew = n.getSoscnote();
						String scnoteOld = soscnotes.get(0).getAsJsonObject().get("content").toString();
						if (soscnotes.size() > 0 && !scnoteNew.equals(scnoteOld)) {
							tagString.addProperty("soscnote", Fm_T.to_y_M_d(new Date()));
						}
					}
					// 物控
					if (!n.getSomcnote().equals("[]")) {
						JsonArray somcnotes = JsonParser.parseString(o.getSomcnote()).getAsJsonArray();
						if (somcnotes.size() > 0 && !n.getSomcnote()
								.equals(somcnotes.get(0).getAsJsonObject().get("content").toString())) {
							tagString.addProperty("somcnote", Fm_T.to_y_M_d(new Date()));
						}
					}
					if (!n.getSomcstatus().equals(0) && !n.getSomcstatus().equals(o.getSomcstatus())) {
						tagString.addProperty("somcstatus", Fm_T.to_y_M_d(new Date()));
					}
					if (!n.getSomcdate().equals("") && !n.getSomcdate().equals(o.getSomcdate())) {
						tagString.addProperty("somcdate", Fm_T.to_y_M_d(new Date()));
					}
					// 倉庫
					if (!n.getSowmnote().equals("[]")) {
						JsonArray somcnotes = JsonParser.parseString(o.getSowmnote()).getAsJsonArray();
						if (somcnotes.size() > 0 && !n.getSowmnote()
								.equals(somcnotes.get(0).getAsJsonObject().get("content").toString())) {
							tagString.addProperty("sowmnote", Fm_T.to_y_M_d(new Date()));
						}

					}
					if (!n.getSowmprogress().equals("") && !n.getSowmprogress().equals(o.getSowmprogress())) {
						tagString.addProperty("sowmprogress", Fm_T.to_y_M_d(new Date()));
					}
					// 製造
					if (!n.getSompnote().equals("[]") && !n.getSompnote().equals(o.getSompnote())) {
						JsonArray sompnotes = JsonParser.parseString(o.getSompnote()).getAsJsonArray();
						if (sompnotes.size() > 0 && !n.getSompnote()
								.equals(sompnotes.get(0).getAsJsonObject().get("content").toString())) {
							tagString.addProperty("sompnote", Fm_T.to_y_M_d(new Date()));
						}
					}
					if (!n.getSompprogress().equals("") && !n.getSompprogress().equals(o.getSompprogress())) {
						tagString.addProperty("sompprogress", Fm_T.to_y_M_d(new Date()));
					}
					// 單據
					if (!n.getSorqty().equals(0) && !n.getSorqty().equals(o.getSorqty())) {
						tagString.addProperty("sorqty", Fm_T.to_y_M_d(new Date()));
					}
					if (!n.getSookqty().equals(0) && !n.getSookqty().equals(o.getSookqty())) {
						tagString.addProperty("sookqty", Fm_T.to_y_M_d(new Date()));
					}
					if (!n.getSostatus().equals("0") && !n.getSostatus().equals(o.getSostatus())) {
						tagString.addProperty("sostatus", Fm_T.to_y_M_d(new Date()));
					}
					if (!n.getSonote().equals("") && !n.getSonote().equals(o.getSonote())) {
						tagString.addProperty("sonote", Fm_T.to_y_M_d(new Date()));
					}
					if (!n.getSofname().equals("") && !n.getSofname().equals(o.getSofname())) {
						tagString.addProperty("sofname", Fm_T.to_y_M_d(new Date()));
					}
					if (!n.getSoodate().equals("") && !n.getSoodate().equals(o.getSoodate())) {
						tagString.addProperty("soodate", Fm_T.to_y_M_d(new Date()));
					}
					if (!n.getSofdate().equals("") && !n.getSofdate().equals(o.getSofdate())) {
						tagString.addProperty("sofdate", Fm_T.to_y_M_d(new Date()));
					}
					if (!n.getSonb().equals("") && !n.getSonb().equals(o.getSonb())) {
						tagString.addProperty("all", Fm_T.to_y_M_d(new Date()));
					}

					// UnLocked
					// 修改後解鎖?
					if (tagString.get("locked").getAsBoolean()) {
						Long time = (new Date().getTime()) - tagString.get("lockedtime").getAsLong();
						if (tagString.get("lockeduser").getAsString().equals(userAcc) || time > 30000) {// 同一個人或是
																										// 時間大於5分鐘 解鎖
							tagString.addProperty("locked", false);
							tagString.addProperty("lockedtime", 0L);
							tagString.addProperty("lockeduser", "");
						}
						mapOutsourcerTag.put(n.getSoid(), tagString);
					}
				}
			}
			// Step2.排序
			List<Order> orders = new ArrayList<>();
			orders.add(new Order(Direction.ASC, "soodate"));// 預計開工日
			orders.add(new Order(Direction.ASC, "syssort"));// 排序
			orders.add(new Order(Direction.ASC, "sonb"));// 工單
			// 一般模式
			// Step3.資料庫->更新資料->暫存
			PageRequest pageable = PageRequest.of(0, 1000, Sort.by(orders));
			scheduleOutsourcerDao.findAllBySearch(null, null, null, null, pageable).forEach(o -> {
				mapOutsourcer.put(o.getSoid(), o);
			});

			// Step5. 合併
			mapOutsourcer.forEach((k, v) -> {
				if (mapOutsourcerTag.containsKey(k)) {
					v.setTag(mapOutsourcerTag.get(k).toString());
				}
			});

			ok = true;
			break;
		case "sendOnlyData":// 用處: 資料庫取出->更新暫存資料->給單一用戶/

			// UnLocked
			mapOutsourcerTag.forEach((k, tagString) -> {
				if (tagString.get("locked").getAsBoolean()) {
					Long time = (new Date().getTime()) - tagString.get("lockedtime").getAsLong();
					if (time > 30000) {// 時間大於5分鐘 解鎖
						tagString.addProperty("locked", false);
						tagString.addProperty("lockedtime", 0L);
						tagString.addProperty("lockeduser", "");
					}
				}
			});
			// Step5. 合併
			mapOutsourcer.forEach((k, v) -> {
				if (mapOutsourcerTag.containsKey(k)) {
					v.setTag(mapOutsourcerTag.get(k).toString());
				}
			});

			ok = true;
			break;
		case "sendAllLock":// 用處: 暫存檢查鎖定->更新暫存資料->給所有用戶/
			// Step4.更新標記內容
			if (arrayList.size() > 0) {
				// 先檢查
				ok = true;
				JsonObject tagString = new JsonObject();
				for (ScheduleOutsourcer n : arrayList) {
					tagString = mapOutsourcerTag.get(n.getSoid());
					if (tagString.get("locked").getAsBoolean()) { // 有人勾選 ?且沒過期(1000*60*5 = 5分鐘)?同一個人?
						Long time = (new Date().getTime()) - tagString.get("lockedtime").getAsLong();
						if (time < 30000 && !tagString.get("lockeduser").getAsString().equals(userAcc)) {
							ok = false;
						}
						break;
					}
				}

				// 更新
				if (ok) {
					arrayList.forEach(n -> {
						JsonObject tagNew = mapOutsourcerTag.get(n.getSoid());
						// Locked
						tagNew.addProperty("locked", true);
						tagNew.addProperty("lockedtime", new Date().getTime());
						tagNew.addProperty("lockeduser", userAcc);
						mapOutsourcerTag.put(n.getSoid(), tagNew);
					});
					// Step5. 合併
					mapOutsourcer.forEach((k, v) -> {
						if (mapOutsourcerTag.containsKey(k)) {
							v.setTag(mapOutsourcerTag.get(k).toString());
						}
					});
				}
			}
			break;
		case "sendAllUnlock":// 用處: 暫存檢查(解)鎖定->更新暫存資料->給所有用戶/

			arrayList.forEach(n -> {
				JsonObject tagString = mapOutsourcerTag.get(n.getSoid());
				// UnLocked
				if (tagString.get("lockeduser").getAsString().equals(userAcc)) {
					tagString.addProperty("locked", false);
					tagString.addProperty("lockedtime", 0L);
					tagString.addProperty("lockeduser", "");
					mapOutsourcerTag.put(n.getSoid(), tagString);
				}
			});
			// Step5. 合併
			mapOutsourcer.forEach((k, v) -> {
				if (mapOutsourcerTag.containsKey(k)) {
					v.setTag(mapOutsourcerTag.get(k).toString());
				}
			});
			ok = true;
			break;
		case "leave":// 用處: 解除連接->移除session用戶/
			Session session = sessionMap.get(userAcc);
			session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "leave...OK"));
			sessionMap.remove(userAcc);
			ok = true;
			break;
		case "sendAllClearShow":// 用處: 清除所有顯示
			arrayList.forEach(br -> {
				if (mapOutsourcerTag.containsKey(br.getSoid())) {
					JsonObject tagString = mapOutsourcerTag.get(br.getSoid());
					tagString.addProperty("all", "");
					// 生管
					tagString.addProperty("sofodate", "");
					tagString.addProperty("sofokdate", "");
					tagString.addProperty("soscstatus", "");
					tagString.addProperty("soscnote", "");
					// 物控
					tagString.addProperty("somcnote", "");
					tagString.addProperty("somcstatus", "");
					tagString.addProperty("somcdate", "");
					// 倉庫
					tagString.addProperty("sowmnote", "");
					tagString.addProperty("sowmprogress", "");
					// 製造
					tagString.addProperty("sompnote", "");
					tagString.addProperty("sompprogress", "");
					// 單據
					tagString.addProperty("sorqty", "");
					tagString.addProperty("sookqty", "");
					tagString.addProperty("sostatus", "");
					tagString.addProperty("sonote", "");
					tagString.addProperty("sofname", "");
					mapOutsourcerTag.put(br.getSoid(), tagString);
				}
			});
			// Step5. 合併
			mapOutsourcer.forEach((k, v) -> {
				if (mapOutsourcerTag.containsKey(k)) {
					v.setTag(mapOutsourcerTag.get(k).toString());
				}
			});
			ok = true;
			break;
		default:
			ok = false;
			break;
		}

		// 包裝整理
		mapOutsourcer.forEach((k, v) -> {
			entityDatasRe.add(v);
		});
		dataJsonRe.addProperty("update", packageService.beanToJson(entityDatasRe));
		dataJsonRe.addProperty("status", ok);
		dataJsonRe.addProperty("action", action);
		return dataJsonRe;
	}

	// 將 Spring 注入的 ApplicationContext 保存到靜態變數?
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		ScheduleOutsourcerWebSocket.applicationContext = applicationContext;
	}

	// 為了 取得在Spring 在ApplicationContext Bean
	public static <T> T getBean(Class<T> type) {
		try {
			return applicationContext.getBean(type);

		} catch (NoUniqueBeanDefinitionException e) {
			LOGGER.warn(e.toString());
			String beanName = applicationContext.getBeanNamesForType(type)[0];
			return applicationContext.getBean(beanName, type);
		}
	}

}