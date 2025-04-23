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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dtri.com.tw.pgsql.dao.ScheduleInfactoryDao;
import dtri.com.tw.pgsql.entity.ScheduleInfactory;
import dtri.com.tw.shared.Fm_T;
import dtri.com.tw.shared.PackageBean;
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
@ServerEndpoint(value = "/websocket/schedule_infactory_client/echo")
public class ScheduleInfactoryWebSocket implements ApplicationContextAware {

	private static ScheduleInfactoryDao scheduleInfactoryDao;

	private static PackageService packageService;

	private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleInfactoryWebSocket.class);

	private static ApplicationContext applicationContext;// 為了讓Bean 物建化

	// 存入所有用戶
	private static Map<String, Session> sessionMap = new HashMap<>();
	// 暫存資料
	private static Map<Long, ScheduleInfactory> mapInfactory = new LinkedHashMap<Long, ScheduleInfactory>();
	private static Map<Long, JsonObject> mapInfactoryTag = new HashMap<Long, JsonObject>();

	// 給查詢資料取值->且併入包裝內
	public static PackageBean getMapInfactoryTag(PackageBean packageBean) throws Exception {
		// 資料轉換
		ArrayList<ScheduleInfactory> entityDatas = new ArrayList<>();
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<ScheduleInfactory>>() {
					});
			// 標記修正
			entityDatas.forEach(x -> {
				if (mapInfactoryTag.containsKey(x.getSiid())) {
					x.setTag(mapInfactoryTag.get(x.getSiid()).toString());
				}
			});
			packageBean.setEntityJson(packageService.beanToJson(entityDatas));
		}
		return packageBean;
	}

	// 給查詢資料取值->且併入包裝內(只有Tag標記的)
	public static PackageBean getMapInfactoryOnlyTag(PackageBean packageBean) throws Exception {
		// 資料轉換
		ArrayList<ScheduleInfactory> entityDatas = new ArrayList<>();
		ArrayList<ScheduleInfactory> entityOnlyTagDatas = new ArrayList<>();
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// 轉換
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<ScheduleInfactory>>() {
					});
			// 標記修正
			entityDatas.forEach(x -> {
				if (mapInfactoryTag.containsKey(x.getSiid())) {
					JsonObject infactoryTag = mapInfactoryTag.get(x.getSiid());
					// 只登記含有標記的(物控+齊料日)
					String simcdate = infactoryTag.get("simcdate").getAsString();
					String simcnote = infactoryTag.get("simcnote").getAsString();
					if (!simcdate.equals("") || !simcnote.equals("")) {
						x.setTag(mapInfactoryTag.get(x.getSiid()).toString());
						entityOnlyTagDatas.add(x);
					}
				}
			});
			packageBean.setEntityJson(packageService.beanToJson(entityOnlyTagDatas));
		}
		return packageBean;
	}

	// 收到訊息
	/**
	 * @param messageJson {"user":"system","action":"leave/sendAllData/sendOnlyData/sendAllLock/sendAllUnlock",<br>
	 *                    "update":ScheduleInfactory(物件)<br>
	 *                    "status":成功失敗?<br>
	 *                    }<br>
	 **/
	@OnMessage
	public void onMessage(Session session, String messageJson) throws IOException {
		if (scheduleInfactoryDao == null) {
			scheduleInfactoryDao = getBean(ScheduleInfactoryDao.class);
		}
		if (packageService == null) {
			packageService = getBean(PackageService.class);
		}
		// Step0.資料準備
		ArrayList<String> leaves = new ArrayList<String>();// 可能要離開或是異常的Client
		ArrayList<ScheduleInfactory> entityDatas = new ArrayList<>();// 接收-要更新或是修改資料的資料
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
						new TypeReference<ArrayList<ScheduleInfactory>>() {
						});
			}
			// 檢測是否沒有暫存資料?
			if (mapInfactory.size() == 0) {
				reLoad();
			}
			// User?
			if (session != null) {
				userAcc = (String) session.getRequestParameterMap().get("userAcc").get(0);
				if (action.equals("sendOnlyData")) {// 單一用戶?
					sessionOnlyAcc = userAcc;
				}
			} else if (!action.equals("sendAllClearShow")) {// Server 清除燈號例外
				// Server 來的要把資料轉換(生館)
				entityDatas.forEach(n -> {
					// 生管
					if (n.getSiscnote() != null && !n.getSiscnote().equals("[]") && !n.getSiscnote().equals("")) {
						JsonArray siscnotes = JsonParser.parseString(n.getSiscnote()).getAsJsonArray();
						String siscnote = siscnotes.get(siscnotes.size() - 1).getAsJsonObject().get("content")
								.getAsString();
						n.setSiscnote(siscnote);
					} else {
						n.setSiscnote("[]");
					}
					// 物控
					if (n.getSimcnote() != null && !n.getSimcnote().equals("[]") && !n.getSimcnote().equals("")) {
						JsonArray simcnotes = JsonParser.parseString(n.getSimcnote()).getAsJsonArray();
						String simcnote = simcnotes.get(simcnotes.size() - 1).getAsJsonObject().get("content")
								.getAsString();
						n.setSimcnote(simcnote);
					} else {
						n.setSimcnote("[]");
					}
					// 倉儲
					if (n.getSiwmnote() != null && !n.getSiwmnote().equals("[]") && !n.getSiwmnote().equals("")) {
						JsonArray siwmnotes = JsonParser.parseString(n.getSiwmnote()).getAsJsonArray();
						String siwmnote = siwmnotes.get(siwmnotes.size() - 1).getAsJsonObject().get("content")
								.getAsString();
						n.setSiwmnote(siwmnote);
					} else {
						n.setSiwmnote("[]");
					}
					// 製造
					if (n.getSimpnote() != null && !n.getSimpnote().equals("[]") && !n.getSimpnote().equals("")) {
						JsonArray simpnotes = JsonParser.parseString(n.getSimpnote()).getAsJsonArray();
						String simpnote = simpnotes.get(simpnotes.size() - 1).getAsJsonObject().get("content")
								.getAsString();
						n.setSimpnote(simpnote);
					} else {
						n.setSimpnote("[]");
					}
				});
			}

			// Step2.執行
			JsonObject dataJsonRe = updateMapInfactory(action, entityDatas, userAcc);
			// Step3.回傳 (多人 or 單人)
			if (sessionOnlyAcc.equals("")) {
				sessionMap.forEach((sessionAcc, v) -> {
					try {
						LOGGER.info("[websocket] 傳送訊息：Acc={}", sessionAcc);
						v.getAsyncRemote().sendText(dataJsonRe.toString());
					} catch (Exception e) {
						// SimethingError
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
					// SimethingError
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
			// LOGGER.info("[websocket] 錯誤訊息：message={}", messageJson);
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
	private synchronized JsonObject updateMapInfactory(String action, ArrayList<ScheduleInfactory> arrayList,
			String userAcc) throws Exception {
		Boolean ok = false;
		JsonObject dataJsonRe = new JsonObject();
		ArrayList<ScheduleInfactory> entityDatasRe = new ArrayList<>();// 回傳-要更新或是修改資料的資料
		LOGGER.info("[websocket] 收到訊息：Acc={}，action={}", userAcc, action);
		/**
		 * all
		 * 
		 * //生管 sifodate sifokdate siscstatus siscnote
		 * 
		 * //物控 simcnote simcstatus simcdate
		 * 
		 * //倉庫 siwmnote siwmprogress
		 * 
		 * //製造 simpnote simpprogress
		 * 
		 * //單據 sirqty siokqty sistatus sinote sifname
		 * 
		 * //鎖定 locked lockedtime lockeduser (綁定帳號)
		 */
		// 檢查是否標記資料?
		if (arrayList.size() > 0) {
			for (ScheduleInfactory n : arrayList) {
				if (!mapInfactoryTag.containsKey(n.getSiid())) {
					JsonObject tagString = new JsonObject();
					// tagString.addProperty("all", Fm_T.to_y_M_d(new Date()));//--新資料取消顯示標記
					tagString.addProperty("all", "");
					// 生管
					tagString.addProperty("sifodate", "");
					tagString.addProperty("sifokdate", "");
					tagString.addProperty("siscstatus", "");
					tagString.addProperty("siscnote", "");
					// 物控
					tagString.addProperty("simcnote", "");
					tagString.addProperty("simcstatus", "");
					tagString.addProperty("simcdate", "");
					// 倉庫
					tagString.addProperty("siwmnote", "");
					tagString.addProperty("siwmprogress", "");
					// 製造
					tagString.addProperty("simpnote", "");
					tagString.addProperty("simpprogress", "");
					// 單據
					tagString.addProperty("sirqty", "");
					tagString.addProperty("siokqty", "");
					tagString.addProperty("sistatus", "");
					tagString.addProperty("sinote", "");
					tagString.addProperty("sifname", "");
					// Locked
					tagString.addProperty("locked", false);
					tagString.addProperty("lockedtime", 0L);
					tagString.addProperty("lockeduser", "");
					mapInfactoryTag.put(n.getSiid(), tagString);
				}
			}
		}

		switch (action) {
		case "sendAllData":// 用處: 資料庫取出->更新暫存資料->給所有用戶/
			// Step4.更新標記內容
			if (arrayList.size() > 0) {
				JsonObject tagString = new JsonObject();
				ScheduleInfactory o = new ScheduleInfactory();
				for (ScheduleInfactory n : arrayList) {
					tagString = mapInfactoryTag.get(n.getSiid());
					o = mapInfactory.get(n.getSiid());
//					// 生管
//					if (!n.getSifodate().equals("") && !n.getSifodate().equals(o.getSifodate())) {
//						tagString.addProperty("sifodate", Fm_T.to_y_M_d(new Date()));
//					}
//					if (!n.getSifokdate().equals("") && !n.getSifokdate().equals(o.getSifokdate())) {
//						tagString.addProperty("sifokdate", Fm_T.to_y_M_d(new Date()));
//					}
//					if (!n.getSiscstatus().equals(0) && !n.getSiscstatus().equals(o.getSiscstatus())) {
//						tagString.addProperty("siscstatus", Fm_T.to_y_M_d(new Date()));
//					}
//					// 生館
//					if (!n.getSiscnote().equals("[]")) {
//						JsonArray siscnotes = JsonParser.parseString(o.getSiscnote()).getAsJsonArray();
//						String scnoteNew = n.getSiscnote();
//						String scnoteOld = "";
//						// 如果是第一筆資料?
//						if (siscnotes.size() > 0) {
//							scnoteOld = siscnotes.get((siscnotes.size() - 1)).getAsJsonObject().get("content")
//									.getAsString();
//							if (!scnoteNew.equals(scnoteOld) && !scnoteNew.equals("")) {
//								tagString.addProperty("siscnote", Fm_T.to_y_M_d(new Date()));
//							}
//						} else if (!scnoteNew.equals("")) {
//							tagString.addProperty("siscnote", Fm_T.to_y_M_d(new Date()));
//						}
//					}
					// 測試用
					if (o == null) {
						System.out.println("o is null");
					}

					// 物控
					if (o != null && !n.getSimcnote().equals("[]")) {
						JsonArray simcnotes = JsonParser.parseString(o.getSimcnote()).getAsJsonArray();
						String scnoteNew = n.getSimcnote().replaceAll("\n", "");
						// 如果是第一筆資料?
						if (simcnotes.size() == 0 && !scnoteNew.equals("")) {
							tagString.addProperty("simcnote", Fm_T.to_y_M_d(new Date()));
						} else if (simcnotes.size() > 0 && !scnoteNew.equals("")) {
							// 如果多筆資料
							boolean checkNotSame = true;
							for (JsonElement simcnote : simcnotes) {
								String scnoteOld = simcnote.getAsJsonObject().get("content").getAsString()
										.replaceAll("\n", "");
								if (scnoteNew.equals(scnoteOld)) {
									checkNotSame = false;
									break;
								}
							}
							if (checkNotSame) {
								tagString.addProperty("simcnote", Fm_T.to_y_M_d(new Date()));
							}
						}
					}
					if (o != null && n.getSimcstatus() != null && !n.getSimcstatus().equals(0)
							&& !n.getSimcstatus().equals(o.getSimcstatus())) {
						tagString.addProperty("simcstatus", Fm_T.to_y_M_d(new Date()));
					}
					if (o != null && !n.getSimcdate().equals("") && !n.getSimcdate().equals(o.getSimcdate())) {
						tagString.addProperty("simcdate", Fm_T.to_y_M_d(new Date()));
					}
//					// 倉庫
//					if (!n.getSiwmnote().equals("[]")) {
//						JsonArray siwmnotes = JsonParser.parseString(o.getSiwmnote()).getAsJsonArray();
//						String scnoteNew = n.getSiwmnote();
//						String scnoteOld = "";
//						// 如果是第一筆資料?
//						if (siwmnotes.size() > 0) {
//							scnoteOld = siwmnotes.get((siwmnotes.size() - 1)).getAsJsonObject().get("content")
//									.getAsString();
//							if (!scnoteNew.equals(scnoteOld) && !scnoteNew.equals("")) {
//								tagString.addProperty("siwmnote", Fm_T.to_y_M_d(new Date()));
//							}
//						} else if (!scnoteNew.equals("")) {
//							tagString.addProperty("siwmnote", Fm_T.to_y_M_d(new Date()));
//						}
//					}
//					if (!n.getSiwmprogress().equals("") && !n.getSiwmprogress().equals(o.getSiwmprogress())) {
//						tagString.addProperty("siwmprogress", Fm_T.to_y_M_d(new Date()));
//					}
//					// 製造
//					if (!n.getSimpnote().equals("[]") && !n.getSimpnote().equals(o.getSimpnote())) {
//						JsonArray simpnotes = JsonParser.parseString(o.getSimpnote()).getAsJsonArray();
//						String scnoteNew = n.getSimpnote();
//						String scnoteOld = "";
//						// 如果是第一筆資料?
//						if (simpnotes.size() > 0) {
//							scnoteOld = simpnotes.get((simpnotes.size() - 1)).getAsJsonObject().get("content")
//									.getAsString();
//							if (!scnoteNew.equals(scnoteOld) && !scnoteNew.equals("")) {
//								tagString.addProperty("simpnote", Fm_T.to_y_M_d(new Date()));
//							}
//						} else if (!scnoteNew.equals("")) {
//							tagString.addProperty("simpnote", Fm_T.to_y_M_d(new Date()));
//						}
//					}
//					if (!n.getSimpprogress().equals("") && !n.getSimpprogress().equals(o.getSimpprogress())) {
//						tagString.addProperty("simpprogress", Fm_T.to_y_M_d(new Date()));
//					}
//					// 單據
//					if (!n.getSirqty().equals(0) && !n.getSirqty().equals(o.getSirqty())) {
//						tagString.addProperty("sirqty", Fm_T.to_y_M_d(new Date()));
//					}
//					if (!n.getSiokqty().equals(0) && !n.getSiokqty().equals(o.getSiokqty())) {
//						tagString.addProperty("siokqty", Fm_T.to_y_M_d(new Date()));
//					}
//					if (!n.getSistatus().equals("0") && !n.getSistatus().equals(o.getSistatus())) {
//						tagString.addProperty("sistatus", Fm_T.to_y_M_d(new Date()));
//					}
//					if (!n.getSinote().equals("") && !n.getSinote().equals(o.getSinote())) {
//						tagString.addProperty("sinote", Fm_T.to_y_M_d(new Date()));
//					}
//					if (!n.getSifname().equals("") && !n.getSifname().equals(o.getSifname())) {
//						tagString.addProperty("sifname", Fm_T.to_y_M_d(new Date()));
//					}
//					if (!n.getSiodate().equals("") && !n.getSiodate().equals(o.getSiodate())) {
//						tagString.addProperty("soodate", Fm_T.to_y_M_d(new Date()));
//					}
//					if (!n.getSifdate().equals("") && !n.getSifdate().equals(o.getSifdate())) {
//						tagString.addProperty("sofdate", Fm_T.to_y_M_d(new Date()));
//					}
					// 所有
//					if (!n.getSinb().equals("") && !n.getSinb().equals(o.getSinb())) {
//						tagString.addProperty("all", Fm_T.to_y_M_d(new Date()));
//					}

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
						mapInfactoryTag.put(n.getSiid(), tagString);
					}
				}
			}
			// Step2.排序
			List<Order> orders = new ArrayList<>();
			orders.add(new Order(Direction.ASC, "siodate"));// 預計開工日
			orders.add(new Order(Direction.ASC, "sifdate"));// 預計完工日
			orders.add(new Order(Direction.ASC, "sinb"));// 工單
			// 一般模式
			// Step3.資料庫->更新資料->暫存
			mapInfactory = new LinkedHashMap<Long, ScheduleInfactory>();
			PageRequest pageable = PageRequest.of(0, 1000, Sort.by(orders));
			scheduleInfactoryDao.findAllBySearch(null, null, null, null, pageable).forEach(o -> {
				// 測試用
				if (o.getSiid() == 264L) {
					System.out.println(o.getSiid());
				}
				mapInfactory.put(o.getSiid(), o);
			});

			// Step5. 合併
			mapInfactory.forEach((k, v) -> {
				if (mapInfactoryTag.containsKey(k)) {
					v.setTag(mapInfactoryTag.get(k).toString());
				}
			});

			ok = true;
			break;
		case "sendOnlyData":// 用處: 資料庫取出->更新暫存資料->給單一用戶/

			// UnLocked
			mapInfactoryTag.forEach((k, tagString) -> {
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
			mapInfactory.forEach((k, v) -> {
				if (mapInfactoryTag.containsKey(k)) {
					v.setTag(mapInfactoryTag.get(k).toString());
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
				for (ScheduleInfactory n : arrayList) {
					tagString = mapInfactoryTag.get(n.getSiid());
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
						JsonObject tagNew = mapInfactoryTag.get(n.getSiid());
						// Locked
						tagNew.addProperty("locked", true);
						tagNew.addProperty("lockedtime", new Date().getTime());
						tagNew.addProperty("lockeduser", userAcc);
						mapInfactoryTag.put(n.getSiid(), tagNew);
					});
					// Step5. 合併
					mapInfactory.forEach((k, v) -> {
						if (mapInfactoryTag.containsKey(k)) {
							v.setTag(mapInfactoryTag.get(k).toString());
						}
					});
				}
			}
			break;
		case "sendAllUnlock":// 用處: 暫存檢查(解)鎖定->更新暫存資料->給所有用戶/

			arrayList.forEach(n -> {
				JsonObject tagString = mapInfactoryTag.get(n.getSiid());
				// UnLocked
				if (tagString.get("lockeduser").getAsString().equals(userAcc)) {
					tagString.addProperty("locked", false);
					tagString.addProperty("lockedtime", 0L);
					tagString.addProperty("lockeduser", "");
					mapInfactoryTag.put(n.getSiid(), tagString);
				}
			});
			// Step5. 合併
			mapInfactory.forEach((k, v) -> {
				if (mapInfactoryTag.containsKey(k)) {
					v.setTag(mapInfactoryTag.get(k).toString());
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
				if (mapInfactoryTag.containsKey(br.getSiid())) {
					JsonObject tagString = mapInfactoryTag.get(br.getSiid());
					tagString.addProperty("all", "");
					// 生管
					tagString.addProperty("sifodate", "");
					tagString.addProperty("sifokdate", "");
					tagString.addProperty("siscstatus", "");
					tagString.addProperty("siscnote", "");
					// 物控
					tagString.addProperty("simcnote", "");
					tagString.addProperty("simcstatus", "");
					tagString.addProperty("simcdate", "");
					// 倉庫
					tagString.addProperty("siwmnote", "");
					tagString.addProperty("siwmprogress", "");
					// 製造
					tagString.addProperty("simpnote", "");
					tagString.addProperty("simpprogress", "");
					// 單據
					tagString.addProperty("sirqty", "");
					tagString.addProperty("siokqty", "");
					tagString.addProperty("sistatus", "");
					tagString.addProperty("sinote", "");
					tagString.addProperty("sifname", "");
					tagString.addProperty("siodate", "");
					tagString.addProperty("sifdate", "");

					mapInfactoryTag.put(br.getSiid(), tagString);
				}
			});
			// Step5. 合併
			mapInfactory.forEach((k, v) -> {
				if (mapInfactoryTag.containsKey(k)) {
					v.setTag(mapInfactoryTag.get(k).toString());
				}
			});
			ok = true;
			break;
		default:
			ok = false;
			break;
		}

		// 包裝整理
		mapInfactory.forEach((k, v) -> {
			entityDatasRe.add(v);
		});
		dataJsonRe.addProperty("update", packageService.beanToJson(entityDatasRe));
		dataJsonRe.addProperty("status", ok);
		dataJsonRe.addProperty("action", action);
		return dataJsonRe;
	}

	// 資料重新整理?
	public synchronized void reLoad() {
		// Step2.排序
		mapInfactory = new LinkedHashMap<Long, ScheduleInfactory>();
		List<Order> orders = new ArrayList<>();
		orders.add(new Order(Direction.ASC, "siodate"));// 預計開工日
		orders.add(new Order(Direction.ASC, "sifdate"));// 預計完工日
		orders.add(new Order(Direction.ASC, "sinb"));// 工單
		// 一般模式
		PageRequest pageable = PageRequest.of(0, 1000, Sort.by(orders));
		scheduleInfactoryDao.findAllBySearch(null, null, null, null, pageable).forEach(o -> {
			// ===資料登記===
			if (!mapInfactory.containsKey(o.getSiid())) {
				mapInfactory.put(o.getSiid(), o);
			}
			// ===標記登記===
			if (!mapInfactoryTag.containsKey(o.getSiid())) {
				JsonObject tagString = new JsonObject();
				tagString.addProperty("all", "");
				// 生管
				tagString.addProperty("sifodate", "");
				tagString.addProperty("sifokdate", "");
				tagString.addProperty("siscstatus", "");
				tagString.addProperty("siscnote", "");
				// 物控
				tagString.addProperty("simcnote", "");
				tagString.addProperty("simcstatus", "");
				tagString.addProperty("simcdate", "");
				// 倉庫
				tagString.addProperty("siwmnote", "");
				tagString.addProperty("siwmprogress", "");
				// 製造
				tagString.addProperty("simpnote", "");
				tagString.addProperty("simpprogress", "");
				// 單據
				tagString.addProperty("sirqty", "");
				tagString.addProperty("siokqty", "");
				tagString.addProperty("sistatus", "");
				tagString.addProperty("sinote", "");
				tagString.addProperty("sifname", "");
				// Locked
				tagString.addProperty("locked", false);
				tagString.addProperty("lockedtime", 0L);
				tagString.addProperty("lockeduser", "");
				mapInfactoryTag.put(o.getSiid(), tagString);
			}
		});
	}

	// 將 Spring 注入的 ApplicationContext 保存到靜態變數?
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		ScheduleInfactoryWebSocket.applicationContext = applicationContext;
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