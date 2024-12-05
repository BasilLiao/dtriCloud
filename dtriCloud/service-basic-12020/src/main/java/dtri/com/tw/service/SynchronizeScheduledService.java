package dtri.com.tw.service;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
	private ScheduleOutsourcerDao scheduleOutsourcerDao;
	@Autowired
	private MoctaScheduleOutsourcerDao erpOutsourcerDao;
	@Autowired
	private ScheduleShortageNotificationDao notificationDao;
	@Autowired
	private ScheduleShortageListDao shortageListDao;
	@Autowired
	private BasicNotificationMailDao notificationMailDao;
	@Autowired
	private ScheduleOutsourcerDao outsourcerDao;

	@Autowired
	private ERPToCloudService erpToCloudService;
	@Autowired
	private PackageService packageService;
	@Resource
	private ClientServiceFeign serviceFeign;

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
				null, nf_pageable);

		// Step2. 取得須寄信清單(缺料清單)
		List<Order> orders = new ArrayList<>();
		orders.add(new Order(Direction.ASC, "sslbslsnnb"));// 單別_單號_單序
		PageRequest pageable = PageRequest.of(0, 9999, Sort.by(orders));
		ArrayList<ScheduleShortageList> shortageLists = shortageListDao.findAllBySearch("A541", null, 0, null,
				pageable);
		ArrayList<ScheduleShortageList> shortageListSaves = new ArrayList<ScheduleShortageList>();
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

			// 建立信件->寄信對象必須要大於1位&& 且不是空的
			if (mainUsers.size() > 0 && !mainUsers.get(0).equals("")) {
				BasicNotificationMail readyNeedMail = new BasicNotificationMail();
				readyNeedMail.setBnmkind("Production");
				readyNeedMail.setBnmmail(mainUsers + "");
				readyNeedMail.setBnmmailcc(secondaryUsers + "");
				// 標題
				readyNeedMail.setBnmtitle("[" + Fm_T.to_y_M_d(new Date()) + "][" + k + "]"//
						+ "[" + erpcuser + "]"//
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
					String mo = fromcommand.length > 0 ? fromcommand[0] : "";// 製令單號
					String pqty = fromcommand.length > 2 ? fromcommand[1] + "*" + fromcommand[2] : "";// 產品品號*數量
					bnmcontent += "<tr>"//
							+ "<td>" + mo + "</td>"//
							+ "<td>" + pqty + "</td>"//
							+ "<td>" + ssl.getSslbslsnnb() + "</td>"//
							+ "<td>" + ssl.getSslpnumber() + "</td>"//
							+ "<td>" + ssl.getSslpname() + "</td>"//
							+ "<td>" + ssl.getSslpnlqty() + "</td>"//
							+ "<td>" + ssl.getSyshnote() + "</td>"//
							+ "<td>" + ssl.getSysnote() + "</td>"//
							+ "<td>" + ssl.getSslerpcuser() + "</td>"//
							+ "</tr>";
					shortageListSaves.add(ssl);
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
		shortageListSaves.forEach(e -> {
			e.setSysstatus(1);
		});
		shortageListDao.saveAll(shortageListSaves);
	}

	// ============ 外包排程通知() ============
	public void scheduleOutNotification() throws Exception {
		// Step1. 取得寄信人
		List<Order> nf_orders = new ArrayList<>();
		nf_orders.add(new Order(Direction.ASC, "ssnsuname"));// 關聯帳號名稱
		nf_orders.add(new Order(Direction.ASC, "ssnsnotice"));// 缺料通知
		PageRequest nf_pageable = PageRequest.of(0, 9999, Sort.by(nf_orders));
		ArrayList<ScheduleShortageNotification> notifications = notificationDao.findAllBySearch(null, null, 0, null,
				true, nf_pageable);
		// Step2. 取得須寄信清單(外包排程資料)

		List<Order> os_orders = new ArrayList<>();
		os_orders.add(new Order(Direction.ASC, "sofdate"));// 預計完工日
		os_orders.add(new Order(Direction.ASC, "somcdate"));// 預計其料日
		os_orders.add(new Order(Direction.ASC, "sonb"));// 製令單
		PageRequest os_pageable = PageRequest.of(0, 9999, Sort.by(os_orders));
		ArrayList<ScheduleOutsourcer> outsourcers = outsourcerDao.findAllBySearch(null, null, null, null, os_pageable);

		// 整理資料
		outsourcers.forEach(o -> {
			// ====物控備註====
			String somcnoteDiv = "<div>";
			JsonArray somcnotes = JsonParser.parseString(o.getSomcnote()).getAsJsonArray();
			// 避免沒資料
			if (somcnotes.size() > 0) {
				JsonObject somcnote = somcnotes.get(somcnotes.size() - 1).getAsJsonObject();
				somcnoteDiv += "<div>" + somcnote.get("date").getAsString() + "/" + somcnote.get("user").getAsString()
						+ "</div>";
				somcnoteDiv += "<div>" + somcnote.get("content").getAsString() + "</div>";
			}
			somcnoteDiv += "</div>";
			o.setSomcnote(somcnoteDiv);
			// ====生管備註====
			String soscnoteDiv = "<div>";
			JsonArray soscnotes = JsonParser.parseString(o.getSoscnote()).getAsJsonArray();
			// 避免沒資料
			if (soscnotes.size() > 0) {
				JsonObject soscnote = soscnotes.get(soscnotes.size() - 1).getAsJsonObject();
				soscnoteDiv += "<div>" + soscnote.get("date").getAsString() + "/" + soscnote.get("user").getAsString()
						+ "</div>";
				soscnoteDiv += "<div>" + soscnote.get("content").getAsString() + "</div>";
			}
			soscnoteDiv += "</div>";
			o.setSoscnote(soscnoteDiv);
			// ====製造備註====
			String sompnoteDiv = "<div>";
			JsonArray sompnotes = JsonParser.parseString(o.getSompnote()).getAsJsonArray();
			// 避免沒資料
			if (sompnotes.size() > 0) {
				JsonObject sompnote = sompnotes.get(sompnotes.size() - 1).getAsJsonObject();
				sompnoteDiv += "<div>" + sompnote.get("date").getAsString() + "/" + sompnote.get("user").getAsString()
						+ "</div>";
				sompnoteDiv += "<div>" + sompnote.get("content").getAsString() + "</div>";
			}
			sompnoteDiv += "</div>";
			o.setSompnote(sompnoteDiv);
			// ====倉庫備註====
			String sowmnoteDiv = "<div>";
			JsonArray sowmnotes = JsonParser.parseString(o.getSowmnote()).getAsJsonArray();
			// 避免沒資料
			if (sowmnotes.size() > 0) {
				JsonObject sowmnote = sowmnotes.get(sowmnotes.size() - 1).getAsJsonObject();
				sowmnoteDiv += "<div>" + sowmnote.get("date").getAsString() + "/" + sowmnote.get("user").getAsString()
						+ "</div>";
				sowmnoteDiv += "<div>" + sowmnote.get("content").getAsString() + "</div>";
			}
			sowmnoteDiv += "</div>";
			o.setSowmnote(sowmnoteDiv);
			// 製令單狀態修正
			switch (o.getSostatus()) {
			case "0":
				o.setSostatus("暫停中");
				break;
			case "1":
				o.setSostatus("未生產");
				break;
			case "2":
				o.setSostatus("已發料");
				break;
			case "3":
				o.setSostatus("生產中");
				break;
			case "Y":
				o.setSostatus("已完工");
				break;
			case "y":
				o.setSostatus("指定完工");
				break;
			case "V":
				o.setSostatus("已作廢");
				break;
			}

		});

		// Step3. 取得寄信模塊
		// 寄信件對象
		ArrayList<String> mainUsers = new ArrayList<String>();
		ArrayList<String> secondaryUsers = new ArrayList<String>();
		// 寄信對象條件
		notifications.forEach(r -> {// 沒有設置=全寄信
			// 主要?次要?
			if (r.getSsnprimary() == 0) {
				mainUsers.add(r.getSsnsumail());
			} else {
				secondaryUsers.add(r.getSsnsumail());
			}
		});// 建立信件->寄信對象必須要大於1位&& 且不是空的
		if (mainUsers.size() > 0 && !mainUsers.get(0).equals("")) {
			BasicNotificationMail readyNeedMail = new BasicNotificationMail();
			readyNeedMail.setBnmkind("Production");
			readyNeedMail.setBnmmail(mainUsers + "");
			readyNeedMail.setBnmmailcc(secondaryUsers + "");// 標題
			readyNeedMail.setBnmtitle("[" + Fm_T.to_y_M_d(new Date()) + "]"//
					+ "Cloud system PCB outsourcing schedule notification!");
			// 內容
			String bnmcontent = "<table border='1' cellpadding='10' cellspacing='0' style='font-size: 12px;'>"//
					+ "<thead><tr style= 'background-color: aliceblue;'>"//
					+ "<th>項次</th>"//
					+ "<th>加工廠代號(代號)</th>"//
					+ "<th>製令單號</th>"//
					+ "<th>產品品號</th>"//
					+ "<th>產品品名</th>"//
					+ "<th>產品規格</th>"//
					+ "<th style='min-width: 65px;'>預計-生產數</th>"//
					+ "<th style='min-width: 65px;'>完成-生產數</th>"//
					+ "<th style='min-width: 65px;'>預計-齊料日</th>"//
					+ "<th style='min-width: 220px;'>物控備註</th>"//
					+ "<th style='min-width: 65px;'>加工廠-開工日</th>"//
					+ "<th style='min-width: 65px;'>預計-完工日</th>"//
					+ "<th style='min-width: 220px;'>生管備註</th>"//
					// + "<th style='min-width: 65px;'>生管狀態</th>"//
					// + "<th style='min-width: 65px;'>物控狀態</th>"//
					+ "<th style='min-width: 65px;'>預計-開工日</th>"//
					// + "<th style='min-width: 220px;'>製令單-備註</th>"//
					// + "<th style='min-width: 65px;'>製令單-負責人</th>"//
					// + "<th style='min-width: 65px;'>加工廠-完工日</th>"//
					// + "<th style='min-width: 65px;'>製令單-狀態</th>"//
					// + "<th style='min-width: 65px;'>YYYY(年)-W00(週期)</th>"//
					+ "</tr></thead>"//
					+ "<tbody>";// 模擬12筆資料
			int r = 1;

			// 創建Excel工作簿
			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet("Mail Data");
			// =================樣式=================
			sheet.setColumnWidth(0, 30 * 256); // 項次
			sheet.setColumnWidth(1, 20 * 256); // 加工廠代號(代號)
			sheet.setColumnWidth(2, 18 * 256); // 製令單號
			sheet.setColumnWidth(3, 18 * 256); // 產品品號
			sheet.setColumnWidth(4, 30 * 256); // 產品品名
			sheet.setColumnWidth(5, 30 * 256); // 產品規格
			//
			sheet.setColumnWidth(6, 15 * 256); // 預計-生產數
			sheet.setColumnWidth(7, 15 * 256); // 完成-生產數
			sheet.setColumnWidth(8, 15 * 256); // 預計-齊料日
			//
			sheet.setColumnWidth(9, 40 * 256); // 物控備註
			sheet.setColumnWidth(10, 18 * 256); // 加工廠-開工日
			sheet.setColumnWidth(11, 15 * 256); // 預計-完工日
			//
			sheet.setColumnWidth(12, 40 * 256); // 生管備註
			sheet.setColumnWidth(13, 15 * 256); // 預計-開工日
			// 創建單元格樣式
			CellStyle wrapTextStyle = workbook.createCellStyle();
			wrapTextStyle.setWrapText(true);
			wrapTextStyle.setAlignment(HorizontalAlignment.LEFT); // 水平靠左
			wrapTextStyle.setVerticalAlignment(VerticalAlignment.TOP); // 垂直靠上
			// 創建一個通用的樣式，設置為靠左和靠上
			CellStyle alignStyle = workbook.createCellStyle();
			alignStyle.setWrapText(true); // 啟用自動換行
			alignStyle.setAlignment(HorizontalAlignment.LEFT); // 水平靠左
			alignStyle.setVerticalAlignment(VerticalAlignment.TOP); // 垂直靠上

			// 創建標題行
			Row header = sheet.createRow(0);
			header.createCell(0).setCellValue("項次");
			header.createCell(1).setCellValue("加工廠代號(代號)");
			header.createCell(2).setCellValue("製令單號");
			header.createCell(3).setCellValue("產品品號");
			header.createCell(4).setCellValue("產品品名");
			header.createCell(5).setCellValue("產品規格");
			header.createCell(6).setCellValue("預計-生產數");
			header.createCell(7).setCellValue("完成-生產數");
			header.createCell(8).setCellValue("預計-齊料日");
			header.createCell(9).setCellValue("物控備註");
			header.createCell(10).setCellValue("加工廠-開工日");
			header.createCell(11).setCellValue("預計-完工日");
			header.createCell(12).setCellValue("生管備註");
			// header.createCell(13).setCellValue("生管狀態");
			// header.createCell(14).setCellValue("物控狀態");
			header.createCell(13).setCellValue("預計-開工日");
			// header.createCell(16).setCellValue("製令單-備註");
			// header.createCell(17).setCellValue("製令單-負責人)");
			// header.createCell(18).setCellValue("加工廠-完工日");
			// header.createCell(19).setCellValue("製令單-狀態");
			// header.createCell(20).setCellValue("YYYY(年)-W00(週期)");
			//
			for (ScheduleOutsourcer oss : outsourcers) {
//				String soscstatus = "";// 生管狀態
//				switch (oss.getSoscstatus()) {
//				case 1:
//					soscstatus = "已發料";
//					break;
//				case 2:
//					soscstatus = "部份發料";
//					break;
//				case 3:
//					soscstatus = "備料中";
//					break;
//				case 4:
//					soscstatus = "未生產";
//					break;
//				case 5:
//					soscstatus = "待打件通知";
//					break;
//				}
//				String somcstatus = "";// 物控狀態
//				switch (oss.getSomcstatus()) {
//				case 0:
//					somcstatus = "未確認";
//					break;
//				case 1:
//					somcstatus = "未齊料";
//					break;
//				case 2:
//					somcstatus = "已齊料";
//					break;
//				}
				// Excel內容
				Row dataRow = sheet.createRow(r);
				dataRow.createCell(0).setCellValue(r);
				dataRow.createCell(1).setCellValue(oss.getSofname());
				dataRow.createCell(2).setCellValue(oss.getSonb());
				dataRow.createCell(3).setCellValue(oss.getSopnb());
				dataRow.createCell(4).setCellValue(oss.getSopname());
				dataRow.createCell(5).setCellValue(oss.getSopspecifications());
				dataRow.createCell(6).setCellValue(oss.getSorqty());
				dataRow.createCell(7).setCellValue(oss.getSookqty());
				dataRow.createCell(8).setCellValue(oss.getSomcdate());
				dataRow.createCell(9)
						.setCellValue(oss.getSomcnote().replaceAll("<div>", "").replaceAll("</div>", "\n"));// 物控備註
				dataRow.createCell(10).setCellValue(oss.getSofodate());
				dataRow.createCell(11).setCellValue(oss.getSofdate());
				dataRow.createCell(12)
						.setCellValue(oss.getSoscnote().replaceAll("<div>", "").replaceAll("</div>", "\n"));// 生管備註
//				dataRow.createCell(13).setCellValue(soscstatus);
//				dataRow.createCell(14).setCellValue(somcstatus);
				dataRow.createCell(13).setCellValue(oss.getSoodate());
//				dataRow.createCell(16).setCellValue(oss.getSonote().replaceAll("<div>", "").replaceAll("</div>", "\n"));// 製令單-備註
//				dataRow.createCell(17).setCellValue(oss.getSouname());
//				dataRow.createCell(18).setCellValue(oss.getSofokdate());
//				dataRow.createCell(19).setCellValue(oss.getSostatus());
//				dataRow.createCell(20).setCellValue(oss.getSoywdate());
				// =================樣式=================
				// 將該樣式應用於每個需要的單元格
				for (int col = 0; col <= 13; col++) {
					dataRow.getCell(col).setCellStyle(alignStyle);
				}
				// 設置每個需要換行的單元格使用該樣式
				dataRow.getCell(9).setCellStyle(wrapTextStyle); // 物控備註
				dataRow.getCell(12).setCellStyle(wrapTextStyle); // 生管備註
				// dataRow.getCell(16).setCellStyle(wrapTextStyle); // 製令單-備註

				// 信件資料結構
				bnmcontent += "<tr>"//
						+ "<td>" + (r++) + "</td>"// 項次
						+ "<td>" + oss.getSofname() + "</td>"// 加工廠代號(代號)
						+ "<td>" + oss.getSonb() + "</td>"// 製令單號
						+ "<td>" + oss.getSopnb() + "</td>"// 產品品號
						+ "<td>" + oss.getSopname() + "</td>"// 產品品名
						+ "<td>" + oss.getSopspecifications() + "</td>"// 產品規格
						+ "<td>" + oss.getSorqty() + "</td>"// 預計-生產數
						+ "<td>" + oss.getSookqty() + "</td>"// 完成-生產數
						+ "<td>" + oss.getSomcdate() + "</td>"// 預計-齊料日
						+ "<td>" + oss.getSomcnote() + "</td>"// 物控備註
						+ "<td>" + oss.getSofodate() + "</td>"// 加工廠-開工日
						+ "<td>" + oss.getSofdate() + "</td>"// 預計-完工日
						+ "<td>" + oss.getSoscnote() + "</td>"// 生管備註
						// + "<td>" + soscstatus + "</td>"// 生管狀態
						// + "<td>" + somcstatus + "</td>"// 物控狀態
						+ "<td>" + oss.getSoodate() + "</td>"// 預計-開工日
						// + "<td>" + oss.getSonote() + "</td>"// 製令單-備註
						// + "<td>" + oss.getSouname() + "</td>"// 製令單-負責人
						// + "<td>" + oss.getSofokdate() + "</td>"// 加工廠-完工日
						// + "<td>" + oss.getSostatus() + "</td>"// 製令單-狀態
						// + "<td>" + oss.getSoywdate() + "</td>"// YYYY(西元年)-W00(週期)
						+ "</tr>";
			}
			bnmcontent += "</tbody></table>";
			readyNeedMail.setBnmcontent(bnmcontent);

			// 輸出到 byte[]
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			workbook.write(outputStream);
			byte[] bytes = outputStream.toByteArray();
			readyNeedMail.setBnmattcontent(bytes);
			readyNeedMail.setBnmattname("[" + Fm_T.to_y_M_d(new Date()) + "]"//
					+ "Cloud system PCB outsourcing schedule.xlsx");

			// 檢查信件(避免重複)
			if (notificationMailDao.findAllBySearch(null, null, null, readyNeedMail.getBnmtitle(), null, null, null)
					.size() == 0) {
				notificationMailDao.save(readyNeedMail);
			}
		}
	}
}