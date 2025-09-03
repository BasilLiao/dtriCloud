package dtri.com.tw.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;

import dtri.com.tw.mssql.dao.BommdDao;
import dtri.com.tw.mssql.dao.InvmaDao;
import dtri.com.tw.mssql.entity.Bommd;
import dtri.com.tw.mssql.entity.Invma;
import dtri.com.tw.pgsql.dao.BasicBomIngredientsDao;
import dtri.com.tw.pgsql.dao.BasicNotificationMailDao;
import dtri.com.tw.pgsql.dao.BasicProductModelDao;
import dtri.com.tw.pgsql.dao.BomHistoryDao;
import dtri.com.tw.pgsql.dao.BomNotificationDao;
import dtri.com.tw.pgsql.dao.BomProductManagementDao;
import dtri.com.tw.pgsql.dao.WarehouseMaterialDao;
import dtri.com.tw.pgsql.entity.BasicBomIngredients;
import dtri.com.tw.pgsql.entity.BasicNotificationMail;
import dtri.com.tw.pgsql.entity.BasicProductModel;
import dtri.com.tw.pgsql.entity.BomHistory;
import dtri.com.tw.pgsql.entity.BomNotification;
import dtri.com.tw.pgsql.entity.BomProductManagement;
import dtri.com.tw.pgsql.entity.WarehouseMaterial;
import dtri.com.tw.service.feign.BomServiceFeign;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.Fm_T;
import jakarta.annotation.Resource;

@Service
public class SynchronizeBomService {
	// ERP
	@Autowired
	InvmaDao invmaDao;
	@Autowired
	BommdDao bommdDao;

	// Cloud
	@Autowired
	private BasicProductModelDao modelDao;
	@Autowired
	private BasicBomIngredientsDao basicBomIngredientsDao;
	@Autowired
	private WarehouseMaterialDao materialDao;
	@Autowired
	private BomHistoryDao bomHistoryDao;
	@Autowired
	private BomNotificationDao notificationDao;
	@Autowired
	private BasicNotificationMailDao notificationMailDao;
	@Autowired
	private DiscoveryClient discoveryClient;
	@Autowired
	private BomProductManagementDao managementDao;

	@Autowired
	ERPToCloudService erpToCloudService;

	@Resource
	BomServiceFeign serviceFeign;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	// private static final SimpleDateFormat dateFormat = new
	// SimpleDateFormat("HH:mm:ss");

	// ============ 同步機種別() ============
	public void erpSynchronizeProductModel() throws Exception {
		ArrayList<Invma> invmas = invmaDao.findAllByInvma();
		ArrayList<BasicProductModel> models = modelDao.findAllBySearch(null, null, null);
		ArrayList<BasicProductModel> newModels = new ArrayList<BasicProductModel>();
		// 轉換
		Map<String, BasicProductModel> mapBpms = new HashMap<String, BasicProductModel>();
		models.forEach(y -> {
			mapBpms.put(y.getBpmname(), y);
		});

		// 比對?->如果有->舊的(false)
		invmas.forEach(x -> {
			// Product model
			if (!mapBpms.containsKey(x.getMa003())) {
				BasicProductModel newModel = new BasicProductModel();
				newModel.setBpmid(null);
				newModel.setBpmname(x.getMa003());
				newModels.add(newModel);
			}

		});
		modelDao.saveAll(newModels);
	}

	// ============ 同步BOM() ============
	public static boolean erpSBIWorking = false;

	public synchronized void erpSynchronizeBomIngredients(boolean synAll) throws Exception {
		try {
			erpSBIWorking = true;
			ArrayList<Bommd> bommds = new ArrayList<Bommd>();
			ArrayList<BasicBomIngredients> boms = new ArrayList<BasicBomIngredients>();
			ArrayList<BasicBomIngredients> bomRemoves = new ArrayList<BasicBomIngredients>();
			ArrayList<BasicBomIngredients> bomNews = new ArrayList<BasicBomIngredients>();
			Map<String, Bommd> erpBommds = new HashMap<String, Bommd>();// ERP整理後資料
			Map<String, WarehouseMaterial> wMs = new HashMap<>();// 物料清單
			List<String> bbisnnb = new ArrayList<String>();
			// 物料號
			materialDao.findAll().forEach(m -> {
				wMs.put(m.getWmpnb(), m);
			});
			// 第一次跑在用 = 沒資料須導入/常態性跑用 = 有資料 區塊性更新
			if (synAll) {
				bommds = bommdDao.findAllByBommdFirst();// 第一次跑在用
			} else {
				bommds = bommdDao.findAllByBommd();// 常態性跑用
			}
			// ERP -> 檢查資料&更正
			bommds.forEach(bommd -> {
				bommd.setMdcdate(bommd.getMdcdate() == null ? "" : bommd.getMdcdate().replaceAll("\\s", ""));
				bommd.setMdcuser(bommd.getMdcuser() == null ? "" : bommd.getMdcuser().replaceAll("\\s", ""));
				bommd.setMdmdate(bommd.getMdmdate() == null ? "" : bommd.getMdmdate().replaceAll("\\s", ""));
				bommd.setMdmuser(bommd.getMdmuser() == null ? "" : bommd.getMdmuser().replaceAll("\\s", ""));
				bommd.setMd001(bommd.getMd001().replaceAll("\\s", ""));
				bommd.setMd002(bommd.getMd002().replaceAll("\\s", ""));
				bommd.setMd003(bommd.getMd003().replaceAll("\\s", ""));
				erpBommds.put(bommd.getMd001() + "-" + bommd.getMd002(), bommd);
				bbisnnb.add(bommd.getMd001() + "-" + bommd.getMd002());
			});
			// 資料回收
			bommds = null;
			// 轉換資料
			if (synAll) {
				boms = basicBomIngredientsDao.findAllByBomListsFirst();// 第一次跑在用
			} else {
				String[] bbisnnbs = bbisnnb.toArray(new String[0]);
				boms = basicBomIngredientsDao.findAllByBomLists(bbisnnbs);// 常態性跑用
			}
			boms.forEach(o -> {
				if (erpBommds.containsKey(o.getBbisnnb())) {
					erpBommds.get(o.getBbisnnb()).setNewone(false);// 標記舊有資料
					String sum = erpBommds.get(o.getBbisnnb()).toString();
					if (!sum.equals(o.getChecksum())) {
						// 更新
						erpToCloudService.bomIngredients(o, erpBommds.get(o.getBbisnnb()), wMs, sum);
						bomNews.add(o);
					}
				} else {
					// 沒比對到?已經移除?
					bomRemoves.add(o);// 第一次跑在用
				}
			});
			// 資料回收
			boms = null;
			// 新增
			erpBommds.forEach((k, n) -> {
				if (n.isNewone()) {
					BasicBomIngredients o = new BasicBomIngredients();
					String sum = n.toString();
					erpToCloudService.bomIngredients(o, n, wMs, sum);
					bomNews.add(o);
				}
			});
			// 存入資料
			basicBomIngredientsDao.saveAll(bomNews);
			basicBomIngredientsDao.deleteAll(bomRemoves);
			System.out.println("---");
			erpSBIWorking = false;

		} catch (Exception e) {
			erpSBIWorking = false;
			throw (e);// 再往外拋
		}
	}

	// ============ BOM是否有異動修正() ============
	public void bomModification() throws Exception {
		// Step0. 準備資料
		ArrayList<BomHistory> hisListSaves = new ArrayList<BomHistory>();
		// 1. 讀取 Excel 檔案
		Workbook workbook = null;
		// 從 classpath 讀取資源
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("90-XXX-XXXXXXX.xlsx")) {
			if (inputStream == null) {
				throw new IllegalArgumentException("找不到資源檔案！");
			}
			System.out.println("檔案已成功讀取。");
			workbook = new XSSFWorkbook(inputStream);
			// 在這裡進行文件處理邏輯
			System.out.println("Excel 總共有 " + workbook.getNumberOfSheets() + " 個工作表");

		} catch (IOException e) {
			e.printStackTrace();
		}

		// Step1. 取得寄信人
		List<Order> nf_orders = new ArrayList<>();
		nf_orders.add(new Order(Direction.ASC, "bnsuname"));// 關聯帳號名稱
		PageRequest nf_pageable = PageRequest.of(0, 9999, Sort.by(nf_orders));
		ArrayList<BomNotification> notificationsAllNew = notificationDao.findAllBySearch(null, null, null, true, 0,
				nf_pageable);
		ArrayList<BomNotification> notificationsUpdate = notificationDao.findAllBySearch(null, null, true, null, 0,
				nf_pageable);

		// Step2. 取得須寄信清單(產品異動通知)
		List<Order> os_orders = new ArrayList<>();
		os_orders.add(new Order(Direction.ASC, "bhnb"));// 成品BOM號
		os_orders.add(new Order(Direction.ASC, "bhpnb"));// 成品BOM號-part 物料
		PageRequest os_pageable = PageRequest.of(0, 9999, Sort.by(os_orders));
		ArrayList<BomHistory> outsourcers = bomHistoryDao.findAllBySearch(null, null, null, 1, null,
				Fm_T.to_count(-1, new Date()), Fm_T.to_count(0, new Date()), os_pageable);// 今天改今天送
		// Step2-1.整理資料(每一張BOM 一封信)
		Map<String, ArrayList<BomHistory>> outsourcersMapAllNew = new HashMap<String, ArrayList<BomHistory>>();
		Map<String, ArrayList<BomHistory>> outsourcersMapUpdate = new HashMap<String, ArrayList<BomHistory>>();
		for (BomHistory bomHistory : outsourcers) {
			// 有比對到?
			if (bomHistory.getBhatype().equals("All New")) {
				// 新增
				if (outsourcersMapAllNew.containsKey(bomHistory.getBhnb())) {
					ArrayList<BomHistory> oldHis = outsourcersMapAllNew.get(bomHistory.getBhnb());
					oldHis.add(bomHistory);
					outsourcersMapAllNew.put(bomHistory.getBhnb(), oldHis);
				} else {
					// 沒比對到?
					ArrayList<BomHistory> newHis = new ArrayList<BomHistory>();
					newHis.add(bomHistory);
					outsourcersMapAllNew.put(bomHistory.getBhnb(), newHis);
				}
			} else {
				// 更新?
				if (outsourcersMapUpdate.containsKey(bomHistory.getBhnb())) {
					ArrayList<BomHistory> oldHis = outsourcersMapUpdate.get(bomHistory.getBhnb());
					oldHis.add(bomHistory);
					outsourcersMapUpdate.put(bomHistory.getBhnb(), oldHis);
				} else {
					// 沒比對到?
					ArrayList<BomHistory> newHis = new ArrayList<BomHistory>();
					newHis.add(bomHistory);
					outsourcersMapUpdate.put(bomHistory.getBhnb(), newHis);
				}
			}
		}

		Workbook workbooks = workbook;// 為了下方邏輯
		// Step3. 取得寄信模塊(更新)
		outsourcersMapUpdate.forEach((mk, mv) -> {
			// 寄信件對象
			ArrayList<String> mainUsers = new ArrayList<String>();
			ArrayList<String> secondaryUsers = new ArrayList<String>();
			// 寄信對象條件
			notificationsUpdate.forEach(r -> {// 沒有設置=全寄信
				// 如果有機型?
				if (!r.getBnmodel().equals("") && mv.get(0).getBhmodel().contains(r.getBnmodel())) {
					// 主要?次要?
					if (r.getBnprimary() == 0) {
						mainUsers.add(r.getBnsumail());
					} else {
						secondaryUsers.add(r.getBnsumail());
					}
				} // 如果有成品號?
				else if (!r.getBnnb().equals("") && mv.get(0).getBhnb().contains(r.getBnnb())) {
					// 主要?次要?
					if (r.getBnprimary() == 0) {
						mainUsers.add(r.getBnsumail());
					} else {
						secondaryUsers.add(r.getBnsumail());
					}
				} else {
					// 如果都沒有過濾(留空白)-> 主要?次要?
					if (r.getBnprimary() == 0) {
						mainUsers.add(r.getBnsumail());
					} else {
						secondaryUsers.add(r.getBnsumail());
					}
				}
			});
			// 建立信件
			if (mainUsers.size() > 0 && !mainUsers.get(0).equals("")) {
				// 取得BOM資訊(PM備註)
				String sysnote = "";
				ArrayList<BomProductManagement> bomProductManagements = managementDao.findAllByCheck(mk, null, null);
				if (bomProductManagements.size() == 1) {
					sysnote += bomProductManagements.get(0).getBpmmodel() + " & ";
					sysnote += bomProductManagements.get(0).getSysnote();
				}

				// 取得目前"規格BOM"資訊
				BasicNotificationMail readyNeedMail = new BasicNotificationMail();
				readyNeedMail.setBnmkind("BOM");
				readyNeedMail.setBnmmail(mainUsers + "");
				readyNeedMail.setBnmmailcc(secondaryUsers + "");// 標題
				readyNeedMail.setBnmtitle("[" + Fm_T.to_y_M_d(new Date()) + "]"//
						+ "Cloud system BOM [" + mk + "] modification notification!");
				// 內容
				String bnmcontent = "<div>" + sysnote + "</div>"
						+ "<table border='1' cellpadding='10' cellspacing='0' style='font-size: 12px;'>"//
						+ "<thead><tr style= 'background-color: aliceblue;'>"//
						+ "<th>項次</th>"//
						+ "<th>產品號</th>"//
						+ "<th>產品型號</th>"//
						+ "<th>異動類型</th>"//
						+ "<th>組成-物料號</th>"//
						+ "<th>組成-製成</th>"//
						+ "<th>組成-數量</th>"//
						+ "</tr></thead>"//
						+ "<tbody>";// 模擬12筆資料
				int r = 0;
				for (BomHistory oss : mv) {
					// 移除的不能算
					Boolean checkX = oss.getBhatype().equals("Delete") || oss.getBhatype().equals("Old");
					if (!checkX) {
						r += 1;
						// Excel
						Sheet sheet = workbooks.getSheetAt(0); // 獲取第一個工作表
						// 2. 修改 Excel 資料（這裡假設在第一行添加一行資料）
						Row dataRow = sheet.createRow(r);
						dataRow.createCell(0).setCellValue(r);
						dataRow.createCell(1).setCellValue(oss.getBhpnb());
						dataRow.createCell(2).setCellValue(oss.getBhpqty());
						dataRow.createCell(8).setCellValue(oss.getBhpprocess());
					}

					// 信件資料結構
					bnmcontent += "<tr>"//
							+ "<td>" + (checkX ? "X" : r) + "</td>"// 項次
							+ "<td>" + oss.getBhnb() + "</td>"// 產品號
							+ "<td>" + oss.getBhmodel() + "</td>"// 產品型號
							+ "<td>" + oss.getBhatype() + "</td>"// 異動類型
							+ "<td>" + oss.getBhpnb() + "</td>"// 組成-物料號
							+ "<td>" + oss.getBhpprocess() + "</td>"// 組成-製成
							+ "<td>" + oss.getBhpqty() + "</td>"// 組成-數量
							+ "</tr>";
					// 有登記的
					hisListSaves.add(oss);
				}
				bnmcontent += "</tbody></table>";
				bnmcontent += "<div>Old=原先舊[物料]/Update=更新後[物料]/";
				bnmcontent += "<br>Delete=已被移除[物料]/New=新增加[物料]/";
				bnmcontent += "<br>All New=新增[BOM]產品/All Delete=移除[BOM]產品</div>";

				readyNeedMail.setBnmcontent(bnmcontent);

				// 輸出到 byte[]
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				try {
					workbooks.write(outputStream);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				byte[] bytes = outputStream.toByteArray();
				readyNeedMail.setBnmattcontent(bytes);
				readyNeedMail.setBnmattname(mk + ".xlsx");

				// 取消-檢查信件(避免重複)
				notificationMailDao.save(readyNeedMail);
				// Step4. 修正資料
				hisListSaves.forEach(e -> {
					e.setSysstatus(1);
					e.setBhnotification(true);
				});
				bomHistoryDao.saveAll(hisListSaves);
			}
		});
		// Step3. 取得寄信模塊(新增)
		outsourcersMapAllNew.forEach((mk, mv) -> {
			// 寄信件對象
			ArrayList<String> mainUsers = new ArrayList<String>();
			ArrayList<String> secondaryUsers = new ArrayList<String>();
			// 寄信對象條件
			notificationsAllNew.forEach(r -> {// 沒有設置=全寄信
				// 如果有機型?
				if (!r.getBnmodel().equals("") && mv.get(0).getBhmodel().contains(r.getBnmodel())) {
					// 主要?次要?
					if (r.getBnprimary() == 0) {
						mainUsers.add(r.getBnsumail());
					} else {
						secondaryUsers.add(r.getBnsumail());
					}
				} // 如果有成品號?
				else if (!r.getBnnb().equals("") && mv.get(0).getBhnb().contains(r.getBnnb())) {
					// 主要?次要?
					if (r.getBnprimary() == 0) {
						mainUsers.add(r.getBnsumail());
					} else {
						secondaryUsers.add(r.getBnsumail());
					}
				} else {
					// 如果都沒有過濾(留空白)-> 主要?次要?
					if (r.getBnprimary() == 0) {
						mainUsers.add(r.getBnsumail());
					} else {
						secondaryUsers.add(r.getBnsumail());
					}
				}
			});
			// 建立信件
			if (mainUsers.size() > 0 && !mainUsers.get(0).equals("")) {
				// 取得BOM資訊(PM備註)
				String sysnote = "";
				ArrayList<BomProductManagement> bomProductManagements = managementDao.findAllByCheck(mk, null, null);
				if (bomProductManagements.size() == 1) {
					sysnote += bomProductManagements.get(0).getBpmmodel() + " & ";
					sysnote += bomProductManagements.get(0).getSysnote();
				}

				//
				BasicNotificationMail readyNeedMail = new BasicNotificationMail();
				readyNeedMail.setBnmkind("BOM");
				readyNeedMail.setBnmmail(mainUsers + "");
				readyNeedMail.setBnmmailcc(secondaryUsers + "");// 標題
				readyNeedMail.setBnmtitle("[" + Fm_T.to_y_M_d(new Date()) + "]"//
						+ "Cloud system BOM [" + mk + "] all new notification!");
				// 內容
				String bnmcontent = "<div>" + sysnote + "</div>"
						+ "<table border='1' cellpadding='10' cellspacing='0' style='font-size: 12px;'>"//
						+ "<thead><tr style= 'background-color: aliceblue;'>"//
						+ "<th>項次</th>"//
						+ "<th>產品號</th>"//
						+ "<th>產品型號</th>"//
						+ "<th>異動類型</th>"//
						+ "<th>組成-物料號</th>"//
						+ "<th>組成-製成</th>"//
						+ "<th>組成-數量</th>"//
						+ "</tr></thead>"//
						+ "<tbody>";// 模擬12筆資料
				int r = 0;
				for (BomHistory oss : mv) {
					// 移除的不能算
					Boolean checkX = oss.getBhatype().equals("Delete") || oss.getBhatype().equals("Old");
					if (!checkX) {
						r += 1;
						// Excel
						Sheet sheet = workbooks.getSheetAt(0); // 獲取第一個工作表
						// 2. 修改 Excel 資料（這裡假設在第一行添加一行資料）
						Row dataRow = sheet.createRow(r);
						dataRow.createCell(0).setCellValue(r);
						dataRow.createCell(1).setCellValue(oss.getBhpnb());
						dataRow.createCell(2).setCellValue(oss.getBhpqty());
						dataRow.createCell(8).setCellValue(oss.getBhpprocess());
					}

					// 信件資料結構
					bnmcontent += "<tr>"//
							+ "<td>" + (checkX ? "X" : r) + "</td>"// 項次
							+ "<td>" + oss.getBhnb() + "</td>"// 產品號
							+ "<td>" + oss.getBhmodel() + "</td>"// 產品型號
							+ "<td>" + oss.getBhatype() + "</td>"// 異動類型
							+ "<td>" + oss.getBhpnb() + "</td>"// 組成-物料號
							+ "<td>" + oss.getBhpprocess() + "</td>"// 組成-製成
							+ "<td>" + oss.getBhpqty() + "</td>"// 組成-數量
							+ "</tr>";
					// 有登記的
					hisListSaves.add(oss);
				}
				bnmcontent += "</tbody></table>";
				bnmcontent += "<div>Old=原先舊[物料]/Update=更新後[物料]/";
				bnmcontent += "<br>Delete=已被移除[物料]/New=新增加[物料]/";
				bnmcontent += "<br>All New=新增[BOM]產品/All Delete=移除[BOM]產品</div>";

				readyNeedMail.setBnmcontent(bnmcontent);

				// 輸出到 byte[]
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				try {
					workbooks.write(outputStream);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				byte[] bytes = outputStream.toByteArray();
				readyNeedMail.setBnmattcontent(bytes);
				readyNeedMail.setBnmattname(mk + ".xlsx");

				// 取消-檢查信件(避免重複)
				notificationMailDao.save(readyNeedMail);
				// Step4. 修正資料
				hisListSaves.forEach(e -> {
					e.setSysstatus(1);
					e.setBhnotification(true);
				});
				bomHistoryDao.saveAll(hisListSaves);
			}
		});
	}

	// 而外執行(BOM規則同步)
	public void autoBISF() {
		JsonObject sendAllData = new JsonObject();
		sendAllData.addProperty("update", "checkUpdate");
		sendAllData.addProperty("action", "sendAllData");
		// 測試 通知Client->autoSearchTestAndUpdate(BOM 規格檢查)
		AutoBomItemSpecifications specifications = new AutoBomItemSpecifications();
		specifications.setSendAllData(sendAllData.toString());
		specifications.run();
	}

	// 而外執行(BOM規則同步)
	public class AutoBomItemSpecifications implements Runnable {
		private String sendAllData;

		@Override
		public void run() {
			try {
				List<ServiceInstance> instances = discoveryClient.getInstances("service-bom");
				boolean check = instances != null && !instances.isEmpty();
				if (check) {// 有再傳送
					serviceFeign.autoSearchTestAndUpdate(sendAllData);
				}
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
}