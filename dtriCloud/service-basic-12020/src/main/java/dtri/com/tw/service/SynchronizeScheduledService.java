package dtri.com.tw.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;

import dtri.com.tw.mssql.dao.MoctaScheduleOutsourcerDao;
import dtri.com.tw.mssql.entity.MoctaScheduleOutsourcer;
import dtri.com.tw.pgsql.dao.BasicNotificationMailDao;
import dtri.com.tw.pgsql.dao.ScheduleOutsourcerDao;
import dtri.com.tw.pgsql.dao.ScheduleShortageListDao;
import dtri.com.tw.pgsql.dao.ScheduleShortageNotificationDao;
import dtri.com.tw.pgsql.entity.BasicNotificationMail;
import dtri.com.tw.pgsql.entity.ScheduleOutsourcer;
import dtri.com.tw.pgsql.entity.ScheduleShortageList;
import dtri.com.tw.pgsql.entity.ScheduleShortageNotification;
import dtri.com.tw.service.feign.ClientServiceFeign;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.Fm_T;
import dtri.com.tw.shared.PackageService;
import jakarta.annotation.Resource;

@Service
public class SynchronizeScheduledService {

	// Cloud

	@Autowired
	ScheduleOutsourcerDao scheduleOutsourcerDao;
	@Autowired
	MoctaScheduleOutsourcerDao erpOutsourcerDao;
	@Autowired
	ScheduleShortageNotificationDao notificationDao;
	@Autowired
	ScheduleShortageListDao shortageListDao;
	@Autowired
	BasicNotificationMailDao notificationMailDao;

	@Autowired
	private ERPToCloudService erpToCloudService;
	@Autowired
	private PackageService packageService;
	@Resource
	ClientServiceFeign serviceFeign;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	// ============ 同步外包生管平台() ============
	public void erpSynchronizeScheduleOutsourcer() throws Exception {
		ArrayList<MoctaScheduleOutsourcer> erpOutsourcers = erpOutsourcerDao.findAllByMocta(null, "Y");// 目前ERP有的資料
		Map<String, MoctaScheduleOutsourcer> erpMapOutsourcers = new HashMap<String, MoctaScheduleOutsourcer>();// ERP整理後資料
		ArrayList<ScheduleOutsourcer> scheduleOutsourcers = scheduleOutsourcerDao.findAllByNotFinish(null);// 尚未結束的
		ArrayList<ScheduleOutsourcer> newScheduleOutsourcers = new ArrayList<ScheduleOutsourcer>();// 要更新的
		// 資料整理
		for (MoctaScheduleOutsourcer one : erpOutsourcers) {
			// 避免時間-問題
			if (one.getTa009() != null && !one.getTa009().equals(""))
				one.setNewone(true);
			erpMapOutsourcers.put(one.getTa001_ta002(), one);
//				//測試用
//				if(one.getTa001_ta002().equals("A512-240311001")) {
//					System.out.println(one.getTa001_ta002());
//				}
		}

		// 比對資料?
		scheduleOutsourcers.forEach(o -> {
//				//測試用
//				if(o.getSonb().equals("A512-240311001")) {
//					System.out.println(o.getSonb());
//				}
			// 有抓取到同樣單據
			if (erpMapOutsourcers.containsKey(o.getSonb())) {
				erpMapOutsourcers.get(o.getSonb()).setNewone(false);
				// sum不同->更新
				String sum = erpMapOutsourcers.get(o.getSonb()).toString();
				if (!sum.equals(o.getSosum())) {
					erpToCloudService.scheduleOutsourcerOne(o, erpMapOutsourcers.get(o.getSonb()), sum);
					newScheduleOutsourcers.add(o);
				}
			} else {
				ArrayList<MoctaScheduleOutsourcer> erpOutsourcersEnd = erpOutsourcerDao.findAllByMocta(o.getSonb(),
						null);
				if (erpOutsourcersEnd.size() == 1) {
					// 更新最後一次?
					o = erpToCloudService.scheduleOutsourcerOne(o, erpOutsourcersEnd.get(0),
							erpOutsourcersEnd.get(0).toString());
				}
				// 沒比對到?移除?完成?
				o.setSysstatus(2);
				newScheduleOutsourcers.add(o);
			}
		});
		// 新增?
		erpMapOutsourcers.forEach((k, n) -> {
			ArrayList<ScheduleOutsourcer> OldEndOne = scheduleOutsourcerDao.findAllByFinish(k, null);
			if (n.isNewone()) {
				ScheduleOutsourcer outsourcer = new ScheduleOutsourcer();
				// 檢查是否有舊資料?
				if (OldEndOne.size() > 0) {
					outsourcer = OldEndOne.get(0);
					outsourcer.setSysstatus(0);// 開啟
				}
				outsourcer = erpToCloudService.scheduleOutsourcerOne(outsourcer, n, n.toString());
				newScheduleOutsourcers.add(outsourcer);
			}
		});

		// 更新資料+建立新資料
		scheduleOutsourcerDao.saveAll(newScheduleOutsourcers);
		String update = packageService.beanToJson(newScheduleOutsourcers);
		JsonObject sendAllData = new JsonObject();
		sendAllData.addProperty("update", update);
		sendAllData.addProperty("action", "sendAllData");
		// 測試 通知Client->Websocket(sendAllUsers)
		OutsourcerSynchronizeCell sendTo = new OutsourcerSynchronizeCell();
		sendTo.setSendAllData(sendAllData.toString());
		sendTo.run();
	}

	// 而外執行(外包生管同步)
	public class OutsourcerSynchronizeCell implements Runnable {
		private String sendAllData;

		@Override
		public void run() {
			try {
				serviceFeign.setOutsourcerSynchronizeCell(sendAllData);
			} catch (Exception e) {
				logger.warn(CloudExceptionService.eStktToSg(e));
			}
		}

		public String getSendAllData() {
			return sendAllData;
		}

		public void setSendAllData(String sendAllData) {
			this.sendAllData = sendAllData;
		}
	}

	// ============ 補料清單檢查() ============
	public void scheduleShortageNotification() throws Exception {
		ArrayList<BasicNotificationMail> readyNeedMails = new ArrayList<BasicNotificationMail>();
		// Step1. 取得寄信人
		List<Order> nf_orders = new ArrayList<>();
		nf_orders.add(new Order(Direction.ASC, "ssnsuname"));// 關聯帳號名稱
		nf_orders.add(new Order(Direction.ASC, "ssnsnotice"));// 缺料通知
		PageRequest nf_pageable = PageRequest.of(0, 9999, Sort.by(nf_orders));
		ArrayList<ScheduleShortageNotification> notifications = notificationDao.findAllBySearch(null, null, 0, true,
				nf_pageable);

		// Step2. 取得須寄信清單(缺料清單)
		List<Order> orders = new ArrayList<>();
		orders.add(new Order(Direction.ASC, "sslbslsnnb"));// 單別_單號_單序
		PageRequest pageable = PageRequest.of(0, 9999, Sort.by(orders));
		ArrayList<ScheduleShortageList> shortageLists = shortageListDao.findAllBySearch(null, null, 0, null, pageable);
		// 分類<工單,缺料清單>
		Map<String, ArrayList<ScheduleShortageList>> shortageListGroups = new HashMap<String, ArrayList<ScheduleShortageList>>();
		shortageLists.forEach(r -> {
			// 有匹配到
			String key = r.getSslbslsnnb().split("-")[0] + "-" + r.getSslbslsnnb().split("-")[1];
			ArrayList<ScheduleShortageList> newadd = new ArrayList<ScheduleShortageList>();
			if (shortageListGroups.containsKey(key)) {
				newadd = shortageListGroups.get(key);
				newadd.add(r);
				shortageListGroups.put(key, newadd);
			} else {
				newadd.add(r);
				shortageListGroups.put(key, newadd);
			}
		});

		// Step3. 取得寄信模塊
		shortageListGroups.forEach((k, y) -> {
			// 寄信件對象
			ArrayList<String> mainUsers = new ArrayList<String>();
			ArrayList<String> secondaryUsers = new ArrayList<String>();
			String erpcuser = y.get(0).getSslerpcuser();// ERP的開單人
			// 寄信對象條件
			notifications.forEach(r -> {
				// 沒有設置=全寄信
				if (r.getSsnsslerpcuser().equals("")) {
					// 主要?次要?
					if (r.getSsnprimary() == 0) {
						mainUsers.add(r.getSsnsumail());
					} else {
						secondaryUsers.add(r.getSsnsumail());
					}
				} else if (r.getSsnsslerpcuser().equals(erpcuser)) {
					// 有匹配?
					// 主要?次要?
					if (r.getSsnprimary() == 0) {
						mainUsers.add(r.getSsnsumail());
					} else {
						secondaryUsers.add(r.getSsnsumail());
					}
				}
			});

			// 建立信件->寄信對象必須要大於1位
			if (mainUsers.size() > 0) {
				BasicNotificationMail readyNeedMail = new BasicNotificationMail();
				readyNeedMail.setBnmkind("Production");
				readyNeedMail.setBnmmail(mainUsers + "");
				readyNeedMail.setBnmmailcc(secondaryUsers + "");
				// 標題
				readyNeedMail.setBnmtitle("[" + Fm_T.to_y_M_d(new Date()) + "][" + k + "]"//
						+ "Cloud system Production management material shortage notification!");
				// 內容
				String bnmcontent = "<table border='1' cellpadding='10' cellspacing='0'>"//
						+ "<thead><tr>"//
						+ "<th>製令單號</th>"//
						+ "<th>產品品號*數量</th>"//
						+ "<th>領料單號-序號</th>"//
						+ "<th>缺料料號</th>"//
						+ "<th>料號品名</th>"//
						+ "<th>缺料數</th>"//
						+ "<th>單據備註</th>"//
						+ "<th>物料備註</th>"//
						+ "<th>建單人</th>"//
						+ "</tr></thead>"//
						+ "<tbody>";
				// 模擬12筆資料
				for (ScheduleShortageList ssl : y) {
					String fromcommand[] = ssl.getSslfromcommand().replaceAll("\\[|\\]", "").split("\\*");
					bnmcontent += "<tr>"//
							+ "<td>" + fromcommand[0] + "</td>"//
							+ "<td>" + fromcommand[1] + "*" + fromcommand[2] + "</td>"//
							+ "<td>" + ssl.getSslbslsnnb() + "</td>"//
							+ "<td>" + ssl.getSslpnumber() + "</td>"//
							+ "<td>" + ssl.getSslpname() + "</td>"//
							+ "<td>" + ssl.getSslpnlqty() + "</td>"//
							+ "<td>" + ssl.getSyshnote() + "</td>"//
							+ "<td>" + ssl.getSysnote() + "</td>"//
							+ "<td>" + ssl.getSslerpcuser() + "</td>"//
							+ "</tr>";
				}
				bnmcontent += "</tbody></table>";
				readyNeedMail.setBnmcontent(bnmcontent);
				// 檢查信件(避免重複)
				if (notificationMailDao.findAllBySearch(null, null, null, k, null, null, null).size() == 0) {
					readyNeedMails.add(readyNeedMail);
				}
			}
		});
		notificationMailDao.saveAll(readyNeedMails);
		// Step4. 修正資料
		shortageLists.forEach(e -> {
			e.setSysstatus(1);
		});
		shortageListDao.saveAll(shortageLists);
	}
}