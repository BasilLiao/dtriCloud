package dtri.com.tw.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.diff.StringsComparator;
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
import dtri.com.tw.pgsql.dao.ScheduleInfactoryDao;
import dtri.com.tw.pgsql.dao.WarehouseMaterialDao;
import dtri.com.tw.pgsql.entity.BasicBomIngredients;
import dtri.com.tw.pgsql.entity.BasicNotificationMail;
import dtri.com.tw.pgsql.entity.BasicProductModel;
import dtri.com.tw.pgsql.entity.BomHistory;
import dtri.com.tw.pgsql.entity.BomNotification;
import dtri.com.tw.pgsql.entity.BomProductManagement;
import dtri.com.tw.pgsql.entity.ScheduleInfactory;
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
	private ScheduleInfactoryDao infactoryDao;

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

			ArrayList<BasicBomIngredients> bomRemoves = new ArrayList<BasicBomIngredients>();
			ArrayList<BasicBomIngredients> bomNews = new ArrayList<BasicBomIngredients>();
			Map<String, WarehouseMaterial> wMs = new HashMap<>();// 物料清單
			List<String> bbisnnb = new ArrayList<String>();
			// 物料號
			materialDao.findAll().forEach(m -> {
				wMs.put(m.getWmpnb(), m);
			});
			// 第一次跑在用 = 沒資料須導入/常態性跑用 = 有資料 區塊性更新
			if (synAll) {
				ArrayList<BasicBomIngredients> boms = new ArrayList<BasicBomIngredients>();
				boms = basicBomIngredientsDao.findAllByBomListsFirst();// 常態性跑用
				long pageSize = 50000; // 每批 5 萬
				long offset = 0;
				while (true) {
					ArrayList<Bommd> bommds = new ArrayList<Bommd>();
					bommds = bommdDao.findAllByBommdFirst(offset, pageSize);// 第一次跑在用
					if (bommds.isEmpty()) {
						break; // 沒資料就結束
					}
					// 🔹 在這裡處理這批資料
					System.out.println("處理筆數: " + offset + ":" + pageSize + ":" + bommds.size());

					Map<String, Bommd> erpBommds = new HashMap<String, Bommd>();// ERP整理後資料
					// ERP -> 檢查資料&更正
					bommds.forEach(bommd -> {
						bommd.setMdcdate(bommd.getMdcdate() == null ? "" : bommd.getMdcdate().replaceAll("\\s", ""));
						bommd.setMdcuser(bommd.getMdcuser() == null ? "" : bommd.getMdcuser().replaceAll("\\s", ""));
						bommd.setMdmdate(bommd.getMdmdate() == null ? "" : bommd.getMdmdate().replaceAll("\\s", ""));
						bommd.setMdmuser(bommd.getMdmuser() == null ? "" : bommd.getMdmuser().replaceAll("\\s", ""));
						bommd.setMd001(bommd.getMd001().replaceAll("\\s", ""));
						bommd.setMd002(bommd.getMd002().replaceAll("\\s", ""));
						bommd.setMd003(bommd.getMd003().replaceAll("\\s", ""));
						bommd.setCmb009(bommd.getCmb009().trim());
						bommd.setMb009(bommd.getMb009().trim());
						erpBommds.put(bommd.getMd001() + "-" + bommd.getMd002(), bommd);
						bbisnnb.add(bommd.getMd001() + "-" + bommd.getMd002());// 成品號-序號
						// 測試
						if ((bommd.getMd001() + "-" + bommd.getMd002()).equals("89-300-0000000-0010")) {
							System.out.println("81-105-30211G-0010");
						}
					});
					// Cloud 取出比對
					boms.forEach(o -> {
						// 測試
//						if (o.getBbisnnb().equals("89-300-0000000-0010")) {
//							System.out.println(o.getBbisnnb());
//						}
						if (erpBommds.containsKey(o.getBbisnnb())) {
							erpBommds.get(o.getBbisnnb()).setNewone(false);// 標記舊有資料
							String sum = erpBommds.get(o.getBbisnnb()).toString();
							if (!sum.equals(o.getChecksum())) {
								// 更新
								erpToCloudService.bomIngredients(o, erpBommds.get(o.getBbisnnb()), wMs, sum);
								o.setSysmdate(new Date());
								o.setSysmuser("system");
								bomNews.add(o);
							}
							o.setSysstatus(1);
						} else {
							// 沒比對到?已經移除?
							if (o.getSysstatus() != 1) {
								o.setSysstatus(2);
							}
						}
					});
					// 新增
					erpBommds.forEach((k, n) -> {
						if (n.isNewone()) {
							BasicBomIngredients o = new BasicBomIngredients();
							String sum = n.toString();
							erpToCloudService.bomIngredients(o, n, wMs, sum);
							bomNews.add(o);
						}
					});

					if (bommds.size() < pageSize) {
						break; // 最後一批
					}
					// 更新下一批範圍
					offset += pageSize; // 下一批
				}
				// 移除
				boms.forEach(r -> {
					if (r.getSysstatus() == 2) {
						bomRemoves.add(r);
					}
					r.setSysstatus(0);
				});
			} else {
				ArrayList<Bommd> bommds = new ArrayList<Bommd>();
				bommds = bommdDao.findAllByBommd();// 常態性跑用
				Map<String, Bommd> erpBommds = new HashMap<String, Bommd>();// ERP整理後資料
				// ERP -> 檢查資料&更正
				bommds.forEach(bommd -> {
					bommd.setMdcdate(bommd.getMdcdate() == null ? "" : bommd.getMdcdate().replaceAll("\\s", ""));
					bommd.setMdcuser(bommd.getMdcuser() == null ? "" : bommd.getMdcuser().replaceAll("\\s", ""));
					bommd.setMdmdate(bommd.getMdmdate() == null ? "" : bommd.getMdmdate().replaceAll("\\s", ""));
					bommd.setMdmuser(bommd.getMdmuser() == null ? "" : bommd.getMdmuser().replaceAll("\\s", ""));
					bommd.setMd001(bommd.getMd001().replaceAll("\\s", ""));
					bommd.setMd002(bommd.getMd002().replaceAll("\\s", ""));
					bommd.setMd003(bommd.getMd003().replaceAll("\\s", ""));
					bommd.setCmb009(bommd.getCmb009().trim());
					bommd.setMb009(bommd.getMb009().trim());
					// 測試
//					if ((bommd.getMd001() + "-" + bommd.getMd002()).equals("81-105-30211G-0010")) {
//						System.out.println((bommd.getMd001() + "-" + bommd.getMd002()));
//					}
					erpBommds.put(bommd.getMd001() + "-" + bommd.getMd002(), bommd);
					bbisnnb.add(bommd.getMd001() + "-" + bommd.getMd002());// 成品號-序號
				});
				// 轉換資料
				String[] bbisnnbs = bbisnnb.toArray(new String[0]);
				ArrayList<BasicBomIngredients> boms = new ArrayList<BasicBomIngredients>();
				boms = basicBomIngredientsDao.findAllByBomLists(bbisnnbs);// 常態性跑用
				boms.forEach(o -> {
					// 測試
//					if (o.getBbisnnb().equals("90-340-T12RA03-0040")) {
//						System.out.println(o.getBbisnnb());
//					}
					if (erpBommds.containsKey(o.getBbisnnb())) {
						erpBommds.get(o.getBbisnnb()).setNewone(false);// 標記舊有資料
						String sum = erpBommds.get(o.getBbisnnb()).toString();
						if (!sum.equals(o.getChecksum())) {
							// 更新
							erpToCloudService.bomIngredients(o, erpBommds.get(o.getBbisnnb()), wMs, sum);
							o.setSysmdate(new Date());
							o.setSysmuser("system");
							bomNews.add(o);
						}
						o.setSysstatus(1);
					} else {
						// 沒比對到?已經移除?
						if (o.getSysstatus() != 1) {
							o.setSysstatus(2);
						}
					}
				});
				// 新增
				erpBommds.forEach((k, n) -> {
					if (n.isNewone()) {
						BasicBomIngredients o = new BasicBomIngredients();
						String sum = n.toString();
						erpToCloudService.bomIngredients(o, n, wMs, sum);
						bomNews.add(o);
					}
				});
				// 移除
				boms.forEach(r -> {
					if (r.getSysstatus() == 2) {
						bomRemoves.add(r);
					}
					r.setSysstatus(0);
				});
			}
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
		Map<String, WarehouseMaterial> wMs = new HashMap<>();// 物料清單
		// List<String> bbisnnb = new ArrayList<String>();
		// 物料號
		materialDao.findAll().forEach(m -> {
			wMs.put(m.getWmpnb(), m);
		});

		ArrayList<BomHistory> hisListSaves = new ArrayList<BomHistory>();
		// Step1. 取得寄信人
		List<Order> nf_orders = new ArrayList<>();
		nf_orders.add(new Order(Direction.ASC, "bnsuname"));// 關聯帳號名稱
		PageRequest nf_pageable = PageRequest.of(0, 9999, Sort.by(nf_orders));
		ArrayList<BomNotification> notificationsUpdate = notificationDao.findAllBySearch(null, null, true, null, null,
				null, 0, nf_pageable);// 必須要有勾一個(更新)
		ArrayList<BomNotification> notificationsAllNew = notificationDao.findAllBySearch(null, null, null, true, null,
				null, 0, nf_pageable);// 必須要有勾一個(新增)
		ArrayList<BomNotification> notificationsDelete = notificationDao.findAllBySearch(null, null, null, null, true,
				null, 0, nf_pageable);// 必須要有勾一個(移除)
		ArrayList<BomNotification> notificationsIp = notificationDao.findAllBySearch(null, null, null, null, null, true,
				0, nf_pageable);// 必須要有勾一個(立即導入)

		// Step2. 取得須寄信清單(產品異動通知)
		List<Order> os_orders = new ArrayList<>();
		os_orders.add(new Order(Direction.ASC, "bhnb"));// 成品BOM號
		os_orders.add(new Order(Direction.ASC, "bhpnb"));// 成品BOM號-part 物料
		PageRequest os_pageable = PageRequest.of(0, 9999, Sort.by(os_orders));
		ArrayList<BomHistory> outsourcers = bomHistoryDao.findAllBySearch(null, null, null, 1, null,
				Fm_T.to_count(-1, new Date()), Fm_T.to_count(0, new Date()), os_pageable);// 今天改今天送
		// Step2-1.整理資料(每一張BOM 一封信)
		Map<String, ArrayList<BomHistory>> outsourcersMapAllNew = new HashMap<String, ArrayList<BomHistory>>();// BOM號_新增時間->全新
		Map<String, ArrayList<BomHistory>> outsourcersMapUpdate = new HashMap<String, ArrayList<BomHistory>>();// BOM號_新增時間->更新
		Map<String, ArrayList<BomHistory>> outsourcersMapDelete = new HashMap<String, ArrayList<BomHistory>>();// BOM號_新增時間->移除

		// : 內容
		for (BomHistory bomHistory : outsourcers) {
			// 有比對到?
			if (bomHistory.getBhatype().equals("All New")) {
				// 新增
				if (outsourcersMapAllNew
						.containsKey(bomHistory.getBhnb() + "_" + Fm_T.to_yMd_Hms(bomHistory.getSyscdate()))) {
					ArrayList<BomHistory> oldHis = outsourcersMapAllNew
							.get(bomHistory.getBhnb() + "_" + Fm_T.to_yMd_Hms(bomHistory.getSyscdate()));
					oldHis.add(bomHistory);
					outsourcersMapAllNew.put(bomHistory.getBhnb() + "_" + Fm_T.to_yMd_Hms(bomHistory.getSyscdate()),
							oldHis);
				} else {
					// 沒比對到?
					ArrayList<BomHistory> newHis = new ArrayList<BomHistory>();
					newHis.add(bomHistory);
					outsourcersMapAllNew.put(bomHistory.getBhnb() + "_" + Fm_T.to_yMd_Hms(bomHistory.getSyscdate()),
							newHis);
				}
			} else if (bomHistory.getBhatype().equals("All Delete")) {
				// 移除
				if (outsourcersMapDelete
						.containsKey(bomHistory.getBhnb() + "_" + Fm_T.to_yMd_Hms(bomHistory.getSyscdate()))) {
					ArrayList<BomHistory> oldHis = outsourcersMapDelete
							.get(bomHistory.getBhnb() + "_" + Fm_T.to_yMd_Hms(bomHistory.getSyscdate()));
					oldHis.add(bomHistory);
					outsourcersMapDelete.put(bomHistory.getBhnb() + "_" + Fm_T.to_yMd_Hms(bomHistory.getSyscdate()),
							oldHis);
				} else {
					// 沒比對到?
					ArrayList<BomHistory> newHis = new ArrayList<BomHistory>();
					newHis.add(bomHistory);
					outsourcersMapDelete.put(bomHistory.getBhnb() + "_" + Fm_T.to_yMd_Hms(bomHistory.getSyscdate()),
							newHis);
				}
			} else {
				// 更新?
				if (outsourcersMapUpdate
						.containsKey(bomHistory.getBhnb() + "_" + Fm_T.to_yMd_Hms(bomHistory.getSyscdate()))) {
					ArrayList<BomHistory> oldHis = outsourcersMapUpdate
							.get(bomHistory.getBhnb() + "_" + Fm_T.to_yMd_Hms(bomHistory.getSyscdate()));
					oldHis.add(bomHistory);
					outsourcersMapUpdate.put(bomHistory.getBhnb() + "_" + Fm_T.to_yMd_Hms(bomHistory.getSyscdate()),
							oldHis);
				} else {
					// 沒比對到?
					ArrayList<BomHistory> newHis = new ArrayList<BomHistory>();
					newHis.add(bomHistory);
					outsourcersMapUpdate.put(bomHistory.getBhnb() + "_" + Fm_T.to_yMd_Hms(bomHistory.getSyscdate()),
							newHis);
				}
			}
		}

		// Step3. 取得寄信模塊(更新)
		outsourcersMapUpdate.forEach((mk, mv) -> {
			String bhnb = mk.split("_")[0];
			Boolean bhinproduction = mv.get(0).getBhinproduction();// 緊急
			// 1. 讀取 Excel 檔案
			Workbook workbooks = null;
			// 從 classpath 讀取資源
			try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("90-XXX-XXXXXXX.xlsx")) {
				if (inputStream == null) {
					throw new IllegalArgumentException("找不到資源檔案！");
				}
				System.out.println("檔案已成功讀取。");
				workbooks = new XSSFWorkbook(inputStream);
				// 在這裡進行文件處理邏輯
				System.out.println("Excel 總共有 " + workbooks.getNumberOfSheets() + " 個工作表");

			} catch (IOException e) {
				e.printStackTrace();
			}

			// 寄信件對象
			LinkedHashSet<String> mainUsers = new LinkedHashSet<String>();
			LinkedHashSet<String> secondaryUsers = new LinkedHashSet<String>();
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
				} else if (r.getBnnb().equals("") && r.getBnmodel().equals("")) {
					// 如果都沒有過濾(留空白)-> 主要?次要?
					if (r.getBnprimary() == 0) {
						mainUsers.add(r.getBnsumail());
					} else {
						secondaryUsers.add(r.getBnsumail());
					}
				}
			});
			// 可能是急單
			if (bhinproduction) {
				notificationsIp.forEach(r -> {// 沒有設置=全寄信
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
					} else if (r.getBnnb().equals("") && r.getBnmodel().equals("")) {
						// 如果都沒有過濾(留空白)-> 主要?次要?
						if (r.getBnprimary() == 0) {
							mainUsers.add(r.getBnsumail());
						} else {
							secondaryUsers.add(r.getBnsumail());
						}
					}
				});
			}

			// 建立信件
			if (mainUsers.size() > 0 && !mainUsers.iterator().next().equals("")) {
				// 時間配置
				String dateTime = Fm_T.to_y_M_d(new Date()) + "";
				if (mv.size() > 0) {
					dateTime = Fm_T.to_yMd_Hms(mv.get(0).getSyscdate()) + "";
				}
				// 取得BOM資訊(PM備註)
				String sysnote = "";
				String sysnoteOld = "";
				ArrayList<BomProductManagement> bomProductManagements = managementDao.findAllByCheck(bhnb, null, null);
				if (bomProductManagements.size() == 1) {
					sysnote += "☑Product Model : " + bomProductManagements.get(0).getBpmmodel();
					sysnote += bomProductManagements.get(0).getSysnote();
					sysnote += "(" + bomProductManagements.get(0).getSysmuser() + ")";
					sysnote = sysnote.replaceAll("\n", "<br>");

					sysnoteOld += "☑Product Model : " + bomProductManagements.get(0).getBpmmodel();
					sysnoteOld += mv.get(0).getSysnote();
					sysnoteOld += "(" + bomProductManagements.get(0).getSysmuser() + ")";
					sysnoteOld = sysnoteOld.replaceAll("\n", "<br>");
				}
				String newDiff = highlightDiff(sysnoteOld, sysnote);
				// 取得目前"規格BOM"資訊
				BasicNotificationMail readyNeedMail = new BasicNotificationMail();
				String hs = bhinproduction ? "[急]" : "";
				readyNeedMail.setBnmkind("BOM");
				readyNeedMail.setBnmmail(new ArrayList<>(mainUsers) + "");
				readyNeedMail.setBnmmailcc(new ArrayList<>(secondaryUsers) + "");// 標題
				readyNeedMail.setBnmtitle(hs + "[Update][" + bhnb + "][" + dateTime + "]"//
						+ " Cloud system BOM notification!");
				// 內容
				String bnmcontent = "<table border='1' cellpadding='10' cellspacing='0' style='font-size: 12px;'>"//
						+ "<thead><tr style= 'background-color: aliceblue;'>"//
						+ "<th>新舊</th>"//
						+ "<th>品規修改</th>"//
						+ "</tr></thead>"//
						+ "<tbody>"// 模擬12筆資料
						+ "<tr><td>New</td><td>" + newDiff + "</td><tr>"// 新備註
						+ "<tr><td>Old</td><td>" + sysnoteOld + "</td><tr>"// 舊備註
						+ "</tbody></table>"//
						+ "<br>" //
						+ "<table border='1' cellpadding='10' cellspacing='0' style='font-size: 12px;'>"//
						+ "<thead><tr style= 'background-color: aliceblue;'>"//
						+ "<th>項次</th>"//
						+ "<th>產品號</th>"//
						+ "<th>產品型號</th>"//
						+ "<th>異動類型</th>"//
						+ "<th>組成-物料號</th>"//
						+ "<th>組成-物料名</th>"//
						+ "<th>組成-物料規格</th>"//
						+ "<th>組成-製成</th>"//
						+ "<th>組成-數量</th>"//
						+ "</tr></thead>"//
						+ "<tbody>";// 模擬12筆資料
				int r = 0;
				// 標記是否為 需要寄信 可能沒有改資料
				Boolean checkNeedSend = false;
				//
				for (BomHistory oss : mv) {
					// 有修改內容
					if ((!checkNeedSend) && !oss.getBhatype().equals("")) {
						checkNeedSend = true;
					}

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
					// 抓取規格(去除空格)
					String getWmname = wMs.containsKey(oss.getBhpnb().trim())
							? wMs.get(oss.getBhpnb().trim()).getWmname()
							: "";
					String getWmspecification = wMs.containsKey(oss.getBhpnb().trim())
							? wMs.get(oss.getBhpnb().trim()).getWmspecification()
							: "";
					// 信件資料結構
					bnmcontent += "<tr>"//
							+ "<td>" + (checkX ? "X" : r) + "</td>"// 項次
							+ "<td>" + oss.getBhnb() + "</td>"// 產品號
							+ "<td>" + oss.getBhmodel() + "</td>"// 產品型號
							+ "<td>" + oss.getBhatype() + "</td>"// 異動類型
							+ "<td>" + oss.getBhpnb() + "</td>"// 組成-物料號
							+ "<td>" + getWmname + "</td>"// 組成-物料名
							+ "<td>" + getWmspecification + "</td>"// 組成-物料規格
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
				bnmcontent += "<br>";

				// 如果是急單 查出在途製令單
				if (bhinproduction) {
					// Step3-1.取得資料(一般/細節)
					ArrayList<ScheduleInfactory> entitys = infactoryDao.findAllByNotFinish(bhnb, null);
					if (entitys.size() > 0) {
						bnmcontent += "<table border='1' cellpadding='10' cellspacing='0' style='font-size: 12px;'>"//
								+ "<thead><tr style= 'background-color: aliceblue;'>"//
								+ "<th>預計開工</th>"//
								+ "<th>預計完工</th>"//
								+ "<th>製令單備註(客/國/訂/其)</th>"//
								+ "<th>製令單號</th>"//
								+ "<th>產品品號</th>"//
								+ "<th>產品品名</th>"//
								+ "<th>產品規格</th>"//
								+ "<th>預計-生產數</th>"//
								+ "<th>製令單-負責人</th>"//
								+ "</tr></thead>"//
								+ "<tbody>";// 模擬12筆資料

						for (ScheduleInfactory entityOne : entitys) {

							// 信件資料結構
							bnmcontent += "<tr>"//
									+ "<td>" + entityOne.getSiodate() + "</td>"// 預計開工
									+ "<td>" + entityOne.getSifdate() + "</td>"// 預計完工
									+ "<td>" + entityOne.getSinote() + "</td>"// 製令單備註(客/國/訂/其)
									+ "<td>" + entityOne.getSinb() + "</td>"// 製令單號
									+ "<td>" + entityOne.getSipnb() + "</td>"// 產品品號
									+ "<td>" + entityOne.getSipname() + "</td>"// 產品品名
									+ "<td>" + entityOne.getSipspecifications() + "</td>"// 產品規格
									+ "<td>" + entityOne.getSirqty() + "</td>"// 預計-生產數
									+ "<td>" + entityOne.getSiuname() + "</td>"// 製令單-負責人
									+ "</tr>";

						}
						bnmcontent += "</tbody></table>";
					}
				}

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
				readyNeedMail.setBnmattname(bhnb + ".xlsx");

				// 取消-檢查信件(避免重複)
				if (checkNeedSend) {
					notificationMailDao.save(readyNeedMail);
				}
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
			String bhnb = mk.split("_")[0];
			// 1. 讀取 Excel 檔案
			Workbook workbooks = null;
			// 從 classpath 讀取資源
			try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("90-XXX-XXXXXXX.xlsx")) {
				if (inputStream == null) {
					throw new IllegalArgumentException("找不到資源檔案！");
				}
				System.out.println("檔案已成功讀取。");
				workbooks = new XSSFWorkbook(inputStream);
				// 在這裡進行文件處理邏輯
				System.out.println("Excel 總共有 " + workbooks.getNumberOfSheets() + " 個工作表");

			} catch (IOException e) {
				e.printStackTrace();
			}
			// 寄信件對象
			LinkedHashSet<String> mainUsers = new LinkedHashSet<String>();
			LinkedHashSet<String> secondaryUsers = new LinkedHashSet<String>();
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
				} else if (r.getBnnb().equals("") && r.getBnmodel().equals("")) {
					// 如果都沒有過濾(留空白)-> 主要?次要?
					if (r.getBnprimary() == 0) {
						mainUsers.add(r.getBnsumail());
					} else {
						secondaryUsers.add(r.getBnsumail());
					}
				}
			});

			// 建立信件
			if (mainUsers.size() > 0 && !mainUsers.iterator().next().equals("")) {
				// 時間配置
				String dateTime = Fm_T.to_y_M_d(new Date()) + "";
				if (mv.size() > 0) {
					dateTime = Fm_T.to_yMd_Hms(mv.get(0).getSyscdate()) + "";
				}

				// 取得BOM資訊(PM備註)
				String sysnote = "";
				ArrayList<BomProductManagement> bomProductManagements = managementDao.findAllByCheck(bhnb, null, null);
				if (bomProductManagements.size() == 1) {
					sysnote += bomProductManagements.get(0).getBpmmodel() + " & ";
					sysnote += bomProductManagements.get(0).getSysnote();
					sysnote = sysnote.replaceAll("\n", "<br>");
				}

				//
				BasicNotificationMail readyNeedMail = new BasicNotificationMail();
				readyNeedMail.setBnmkind("BOM");
				readyNeedMail.setBnmmail(new ArrayList<>(mainUsers) + "");
				readyNeedMail.setBnmmailcc(new ArrayList<>(secondaryUsers) + "");// 標題
				Boolean checkImport = basicBomIngredientsDao.findAllByCheck(bhnb, null, null, null).size() > 0;//
				// 可能是ERP 導入
				if (checkImport) {
					readyNeedMail.setBnmtitle("[Import][" + bhnb + "][" + dateTime + "]"//
							+ " Cloud system BOM notification!");
				} else {
					// 可能是全新資料
					readyNeedMail.setBnmtitle("[All New][" + bhnb + "][" + dateTime + "]"//
							+ " Cloud system BOM notification!");
				}
				// 內容
				String bnmcontent = "<table border='1' cellpadding='10' cellspacing='0' style='font-size: 12px;'>"//
						+ "<thead><tr style= 'background-color: aliceblue;'>"//
						+ "<th>新舊</th>"//
						+ "<th>品規修改</th>"//
						+ "</tr></thead>"//
						+ "<tbody>"// 模擬12筆資料
						+ "<tr><td>New</td><td>" + sysnote + "</td><tr>"// 新備註
						+ "</tbody></table>"//
						+ "<br>"//
						+ "<table border='1' cellpadding='10' cellspacing='0' style='font-size: 12px;'>"//
						+ "<thead><tr style= 'background-color: aliceblue;'>"//
						+ "<th>項次</th>"//
						+ "<th>產品號</th>"//
						+ "<th>產品型號</th>"//
						+ "<th>異動類型</th>"//
						+ "<th>組成-物料號</th>"//
						+ "<th>組成-物料名</th>"//
						+ "<th>組成-物料規格</th>"//
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
					// 抓取規格(去除空格)
					String getWmname = wMs.containsKey(oss.getBhpnb().trim())
							? wMs.get(oss.getBhpnb().trim()).getWmname()
							: "";
					String getWmspecification = wMs.containsKey(oss.getBhpnb().trim())
							? wMs.get(oss.getBhpnb().trim()).getWmspecification()
							: "";
					// 信件資料結構
					bnmcontent += "<tr>"//
							+ "<td>" + (checkX ? "X" : r) + "</td>"// 項次
							+ "<td>" + oss.getBhnb() + "</td>"// 產品號
							+ "<td>" + oss.getBhmodel() + "</td>"// 產品型號
							+ "<td>" + oss.getBhatype() + "</td>"// 異動類型
							+ "<td>" + oss.getBhpnb() + "</td>"// 組成-物料號
							+ "<td>" + getWmname + "</td>"// 組成-物料名
							+ "<td>" + getWmspecification + "</td>"// 組成-物料規格
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
				readyNeedMail.setBnmattname(bhnb + ".xlsx");

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
		// Step3. 取得寄信模塊(移除)
		outsourcersMapDelete.forEach((mk, mv) -> {
			String bhnb = mk.split("_")[0];
			// 1. 讀取 Excel 檔案
			Workbook workbooks = null;
			// 從 classpath 讀取資源
			try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("90-XXX-XXXXXXX.xlsx")) {
				if (inputStream == null) {
					throw new IllegalArgumentException("找不到資源檔案！");
				}
				System.out.println("檔案已成功讀取。");
				workbooks = new XSSFWorkbook(inputStream);
				// 在這裡進行文件處理邏輯
				System.out.println("Excel 總共有 " + workbooks.getNumberOfSheets() + " 個工作表");

			} catch (IOException e) {
				e.printStackTrace();
			}
			// 寄信件對象
			LinkedHashSet<String> mainUsers = new LinkedHashSet<String>();
			LinkedHashSet<String> secondaryUsers = new LinkedHashSet<String>();
			// 寄信對象條件
			notificationsDelete.forEach(r -> {// 沒有設置=全寄信
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
				} else if (r.getBnnb().equals("") && r.getBnmodel().equals("")) {
					// 如果都沒有過濾(留空白)-> 主要?次要?
					if (r.getBnprimary() == 0) {
						mainUsers.add(r.getBnsumail());
					} else {
						secondaryUsers.add(r.getBnsumail());
					}
				}
			});
			// 建立信件
			if (mainUsers.size() > 0 && !mainUsers.iterator().next().equals("")) {
				// 時間配置
				String dateTime = Fm_T.to_y_M_d(new Date()) + "";
				if (mv.size() > 0) {
					dateTime = Fm_T.to_yMd_Hms(mv.get(0).getSyscdate()) + "";
				}
				// 取得BOM資訊(PM備註)
				String sysnoteOld = "";
				sysnoteOld += mv.get(0).getSysnote();
				sysnoteOld = sysnoteOld.replaceAll("\n", "<br>");

				//
				BasicNotificationMail readyNeedMail = new BasicNotificationMail();
				readyNeedMail.setBnmkind("BOM");
				readyNeedMail.setBnmmail(new ArrayList<>(mainUsers) + "");
				readyNeedMail.setBnmmailcc(new ArrayList<>(secondaryUsers) + "");// 標題
				readyNeedMail.setBnmtitle("[All Delete][" + bhnb + "][" + dateTime + "]"//
						+ " Cloud system BOM notification!");
				// 內容
				String bnmcontent = "<table border='1' cellpadding='10' cellspacing='0' style='font-size: 12px;'>"//
						+ "<thead><tr style= 'background-color: aliceblue;'>"//
						+ "<th>新舊</th>"//
						+ "<th>品規修改</th>"//
						+ "</tr></thead>"//
						+ "<tbody>"// 模擬12筆資料
						+ "<tr><td>Delete</td><td>" + sysnoteOld + "</td><tr>"// 備註
						+ "</tbody></table>"//
						+ "<br>"//
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
				readyNeedMail.setBnmattname(bhnb + ".xlsx");

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
		AutoBomItemSpecifications taskSpecifications = new AutoBomItemSpecifications();
		taskSpecifications.setSendAllData(sendAllData.toString());

		// ★ 改成丟給新執行緒跑，不要直接呼叫 run()
		Thread t = new Thread(taskSpecifications, "getAutoSearchTestAndUpdate-Thread");
		t.start();
	}

	// 而外執行(BOM規則同步)
	private class AutoBomItemSpecifications implements Runnable {
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

		public void setSendAllData(String sendAllData) {
			this.sendAllData = sendAllData;
		}
	}

	/**
	 * 取得兩段文字差異，並在不同處以黃色背景標記
	 * 
	 * @param oldText 舊文字
	 * @param newText 新文字
	 * @return 含HTML標記的比對結果
	 */
	public static String highlightDiff(String oldText, String newText) {
		StringsComparator comparator = new StringsComparator(oldText, newText);
		StringBuilder result = new StringBuilder();
		comparator.getScript().visit(new org.apache.commons.text.diff.CommandVisitor<Character>() {
			@Override
			public void visitInsertCommand(Character c) {
				// 🔸【新增字元】：
				// 表示這個字元 c 是新字串中出現、但在舊字串中沒有的內容
				// 通常代表「新增的部分」。
				//
				// 這裡我們用 <span> 包住該字元，
				// 並以黃色背景（background-color:yellow）凸顯它是新加入的。
				result.append("<span style='background-color:yellow;'>").append(c).append("</span>");
			}

			@Override
			public void visitDeleteCommand(Character c) {
				// 可選：顯示刪除內容（例如紅色刪除線）
				// 🔸【刪除字元】：
				// 表示這個字元 c 是舊字串中有、但在新字串中被刪除的內容
				// 通常代表「被移除的部分」。
				//
				// 為了讓視覺上更清楚，我們使用粉紅底（#ffc0cb）
				// 並加上刪除線（text-decoration:line-through）
				// 以提示這個部分是舊內容、已被移除。
				result.append("<span style='background-color:#ffc0cb;text-decoration:line-through;'>").append(c)
						.append("</span>");
			}

			@Override
			public void visitKeepCommand(Character c) {
				// 🔸【保留字元】：
				// 表示這個字元 c 在舊字串與新字串中都存在，
				// 沒有變化，因此不需要上色或刪除線。
				//
				// 直接將字元原樣加入結果即可。
				result.append(c);
			}
		});
		return result.toString();
	}

	// ============ 規格BOM更正 & 修正規格名稱() ============
	public void bomRevisedSpecifications() {

		JsonObject sendAllData = new JsonObject();
		AutogetSynBomAll taskAutogetSynBomAll = new AutogetSynBomAll();
		taskAutogetSynBomAll.setSendAllData(sendAllData.toString());

		// ★ 改成丟給新執行緒跑，不要直接呼叫 run()
		Thread t = new Thread(taskAutogetSynBomAll, "AutogetSynBomAll-Thread");
		t.start();
	}

	// 而外執行(規格BOM更正 & 修正規格名稱)
	private class AutogetSynBomAll implements Runnable {
		private String sendAllData;

		@Override
		public void run() {
			try {
				List<ServiceInstance> instances = discoveryClient.getInstances("service-bom");
				boolean check = instances != null && !instances.isEmpty();
				if (check) {// 有再傳送
					serviceFeign.autogetSynBomAll(sendAllData);
				}
			} catch (Exception e) {
				logger.warn(CloudExceptionService.eStktToSg(e));
			}
		}

		public void setSendAllData(String sendAllData) {
			this.sendAllData = sendAllData;
		}
	}
}