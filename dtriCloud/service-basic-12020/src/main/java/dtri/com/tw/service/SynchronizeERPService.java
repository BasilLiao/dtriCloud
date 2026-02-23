package dtri.com.tw.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import dtri.com.tw.mssql.dao.BommdDao;
import dtri.com.tw.mssql.dao.BomtdDao;
import dtri.com.tw.mssql.dao.BomtfDao;
import dtri.com.tw.mssql.dao.CopthDao;
import dtri.com.tw.mssql.dao.InvmaDao;
import dtri.com.tw.mssql.dao.InvtaDao;
import dtri.com.tw.mssql.dao.InvtbDao;
import dtri.com.tw.mssql.dao.InvtgDao;
import dtri.com.tw.mssql.dao.InvthDao;
import dtri.com.tw.mssql.dao.MoctaDao;
import dtri.com.tw.mssql.dao.MoctaScheduleOutsourcerDao;
import dtri.com.tw.mssql.dao.MocteDao;
import dtri.com.tw.mssql.dao.MoctfDao;
import dtri.com.tw.mssql.dao.MocthDao;
import dtri.com.tw.mssql.dao.PurthDao;
import dtri.com.tw.mssql.entity.Bomtd;
import dtri.com.tw.mssql.entity.Bomtf;
import dtri.com.tw.mssql.entity.Copth;
import dtri.com.tw.mssql.entity.Invta;
import dtri.com.tw.mssql.entity.Invtb;
import dtri.com.tw.mssql.entity.Invtg;
import dtri.com.tw.mssql.entity.Invth;
import dtri.com.tw.mssql.entity.Mocta;
import dtri.com.tw.mssql.entity.Mocte;
import dtri.com.tw.mssql.entity.Moctf;
import dtri.com.tw.mssql.entity.Mocth;
import dtri.com.tw.mssql.entity.Purth;
import dtri.com.tw.pgsql.dao.BasicBomIngredientsDao;
import dtri.com.tw.pgsql.dao.BasicCommandListDao;
import dtri.com.tw.pgsql.dao.BasicIncomingListDao;
import dtri.com.tw.pgsql.dao.BasicNotificationMailDao;
import dtri.com.tw.pgsql.dao.BasicProductModelDao;
import dtri.com.tw.pgsql.dao.BasicShippingListDao;
import dtri.com.tw.pgsql.dao.WarehouseAreaDao;
import dtri.com.tw.pgsql.dao.WarehouseConfigDao;
import dtri.com.tw.pgsql.dao.WarehouseKeeperDao;
import dtri.com.tw.pgsql.dao.WarehouseMaterialDao;
import dtri.com.tw.pgsql.dao.WarehouseTypeFilterDao;
import dtri.com.tw.pgsql.entity.BasicCommandList;
import dtri.com.tw.pgsql.entity.BasicIncomingList;
import dtri.com.tw.pgsql.entity.BasicShippingList;
import dtri.com.tw.pgsql.entity.WarehouseArea;
import dtri.com.tw.pgsql.entity.WarehouseConfig;
import dtri.com.tw.pgsql.entity.WarehouseKeeper;
import dtri.com.tw.pgsql.entity.WarehouseMaterial;
import dtri.com.tw.pgsql.entity.WarehouseTypeFilter;
import dtri.com.tw.service.feign.ClientServiceFeign;
import dtri.com.tw.shared.Fm_T;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class SynchronizeERPService {

	@Autowired
	BommdDao bommdDao;
	@Autowired
	BomtdDao bomtdDao;
	@Autowired
	BomtfDao bomtfDao;
	@Autowired
	InvtaDao invtaDao;
	@Autowired
	InvtbDao invtbDao;
	@Autowired
	InvtgDao invtgDao;
	@Autowired
	InvthDao invthDao;
	@Autowired
	MoctaDao moctaDao;
	@Autowired
	MoctaScheduleOutsourcerDao erpOutsourcerDao;
	@Autowired
	MocteDao mocteDao;
	@Autowired
	MoctfDao moctfDao;
	@Autowired
	MocthDao mocthDao;
	@Autowired
	PurthDao purthDao;
	@Autowired
	InvmaDao invmaDao;
	@Autowired
	CopthDao copthDao;

	// Cloud
	@Autowired
	BasicIncomingListDao incomingListDao;
	@Autowired
	BasicShippingListDao shippingListDao;
	@Autowired
	BasicCommandListDao commandListDao;
	@Autowired
	WarehouseAreaDao areaDao;
	@Autowired
	WarehouseConfigDao configDao;
	@Autowired
	WarehouseMaterialDao materialDao;
	@Autowired
	WarehouseTypeFilterDao filterDao;
	@Autowired
	WarehouseKeeperDao keeperDao;
	@Autowired
	BasicProductModelDao modelDao;

	@Autowired
	BasicNotificationMailDao notificationMailDao;

	@Autowired
	BasicBomIngredientsDao basicBomIngredientsDao;

	@Autowired
	ERPToCloudService erpToCloudService;
	@Autowired
	ERPAutoCheckService erpAutoCheckService;
	@Autowired
	ERPAutoRemoveService autoRemoveService;
	@Autowired
	SynchronizeBiosService biosService;

	@Resource
	ClientServiceFeign serviceFeign;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	/**
	 * KEY: wtf_code單據代號(開頭)Ex:A511 / A521<br>
	 * Value: wtf_type單據類型0=入庫 / 1=出庫 / 2=轉移/3=無作用(製令單?)<br>
	 */
	private Map<String, Integer> wTFsSave = new HashMap<>();
	// 事先準備匹配
	private Map<String, WarehouseArea> wAs = new HashMap<>();// 庫別清單
	private Map<String, WarehouseTypeFilter> wTFs = new HashMap<>();// 單別清單
	private Map<String, WarehouseMaterial> wMs = new HashMap<>();// 物料清單
	private TreeMap<String, WarehouseKeeper> wKs = new TreeMap<>();// 負責人
	private Map<String, WarehouseConfig> wCs = new HashMap<>();// 倉別

	// 物料自動化 修正(單據/倉別/物料)<wa_alias_wmpnb,wa_t_qty>
	private Map<String, Integer> wAsSave = new HashMap<>();// 自動更新清單
	// 全新物料 如果有進料的?(ERP新物料 & Cloud 新物料)<倉別_物料號,數量>
	private Map<String, Integer> wAsAllNewSave = new HashMap<String, Integer>();

	public void initERPSynchronizeService() throws Exception {
		// 區域變數（暫存用）
		Map<String, WarehouseArea> wAsTemp = new HashMap<>();
		Map<String, WarehouseTypeFilter> wTFsTemp = new HashMap<>();
		Map<String, WarehouseMaterial> wMsTemp = new HashMap<>();
		TreeMap<String, WarehouseKeeper> wKsTemp = new TreeMap<>();
		Map<String, WarehouseConfig> wCsTemp = new HashMap<>();
		Map<String, Integer> wAsSaveTemp = new HashMap<>();
		Map<String, Integer> wAsAllNewSaveTemp = new HashMap<>();

		// ====== 資料初始化過程 ======

		// 庫別清單
		areaDao.findAll().forEach(x -> {
			if (!wAsTemp.containsKey(x.getWaaliasawmpnb())) {
				wAsTemp.put(x.getWaaliasawmpnb(), x);
			}
		});

		// 單別清單
		filterDao.findAll().forEach(y -> {
			wTFsTemp.put(y.getWtfcode(), y);
		});

		// 負責人（排序條件）
		List<Order> orders = new ArrayList<>();
		orders.add(new Order(Direction.ASC, "wksuaccount"));
		orders.add(new Order(Direction.ASC, "wkglist"));
		PageRequest pageable = PageRequest.of(0, 99999, Sort.by(orders));

		Map<String, WarehouseKeeper> keepersTemp = new HashMap<>();
		keeperDao.findAllBySearch(null, null, pageable).forEach(z -> {
			if (!z.getWksuaccount().isEmpty() && !z.getWkglist().isEmpty() && !z.getWkwaslocation().isEmpty()) {
				String key = z.getWksuaccount() + "_" + z.getWkglist() + "_" + z.getWkwaslocation();
				keepersTemp.put(key, z);
			}
		});
		wKsTemp.putAll(keepersTemp); // TreeMap 排序版

		// 倉別清單
		configDao.findAll().forEach(w -> {
			wCsTemp.put(w.getWcalias(), w);
		});

		// 物料清單
		materialDao.findAll().forEach(m -> {
			wMsTemp.put(m.getWmpnb(), m);
		});

		// 初始化空的自動更新與新物料清單
		wAsSaveTemp = new HashMap<>();
		wAsAllNewSaveTemp = new HashMap<>();

		// ====== 一次性安全指派 ======
		this.wAs = wAsTemp;
		this.wTFs = wTFsTemp;
		this.wMs = wMsTemp;
		this.wKs = wKsTemp;
		this.wCs = wCsTemp;
		this.wAsSave = wAsSaveTemp;
		this.wAsAllNewSave = wAsAllNewSaveTemp;
	}

	// ============ A511 廠內製令單/A512 委外製令單/A521 廠內重工單/A522 委外領料單 ============
	public void erpSynchronizeMocta() throws Exception {
		logger.info("=== erpSynchronizeMocta: 時間:{}", dateFormat.format(new Date()));
		ArrayList<Mocta> moctas = moctaDao.findAllByMocta();
		Map<String, Mocta> erpMaps = new HashMap<>();
		Map<String, BasicCommandList> commandMaps = new HashMap<>();// BIOS檢查
		ArrayList<BasicCommandList> commandLists = new ArrayList<BasicCommandList>();
		Map<String, BasicCommandList> removeCommandMap = new TreeMap<String, BasicCommandList>();// [Cloud]儲存(移除)
		int bslnb = 1;
		String Ta001_ta002 = "";
		for (Mocta m : moctas) {
			if (!Ta001_ta002.equals(m.getTa001_ta002())) {
				Ta001_ta002 = m.getTa001_ta002();
				bslnb = 1;
			}
			// 建立序號
			String nKey = m.getTa001_ta002() + "-" + m.getMb001();
			nKey = nKey.replaceAll("\\s", "");
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
			m.setTd004(m.getTd004() == null ? "" : m.getTd004().replaceAll("\\s", ""));
			m.setTa001_ta002(m.getTa001_ta002().replaceAll("\\s", ""));
			m.setMb002(m.getMb002().replaceAll("\\s+$", ""));// 去除結尾空格
			m.setMb003(m.getMb003().replaceAll("\\s+$", ""));// 去除結尾空格
			m.setBslnb(String.format("%04d", bslnb));
			m.setNewone(true);
			bslnb += 1;
			erpMaps.put(nKey, m);
			wTFsSave.put(m.getTa001_ta002().split("-")[0], 3);
		}
		// Step2. 取得[Cloud] 有效製令單 資料
		ArrayList<BasicCommandList> entityOlds = commandListDao.findAllByStatus(0);
		// 存入資料物件
		// Step3. 資料整理轉換
		entityOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+物料號)
			String oKeyClassSn = o.getBclclass() + "-" + o.getBclsn();
			String oKey = (oKeyClassSn + "-" + o.getBclpnumber()).replaceAll("\\s", "");
			// 同一筆資料?
			if (erpMaps.containsKey(oKey)) {
				erpMaps.get(oKey).setNewone(false);// 標記:不是新的
				String nChecksum = erpMaps.get(oKey).toString().replaceAll("\\s", "");
				boolean checksum = !o.getChecksum().equals(nChecksum);// 內容物是否有不同?
				// 內容不同=>更新
				if (checksum) {
					Mocta m = erpMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 資料轉換
					erpToCloudService.commandOne(o, m, checkSum);
					commandLists.add(o);
				}
			} else {
				// 可能被移除了?或是被完結了?
				removeCommandMap.put(oKeyClassSn + "-" + o.getBclpnumber(), o);
			}
		});
		// 全新資料?
		erpMaps.forEach((key, v) -> {
			if (v.isNewone()) {
				BasicCommandList n = new BasicCommandList();
				String checkSum = v.toString().replaceAll("\\s", "");
				// 資料轉換
				commandLists.add(erpToCloudService.commandOne(n, v, checkSum));
				// 判斷BIOS通知?(機種別_客戶)
				String biosKey = v.getMa003() + "_" + v.getTa050();
				if (!commandMaps.containsKey(biosKey)) {
					commandMaps.put(biosKey, n);
				}
			}
		});
		// Step4.確認是否完結 or 被移除?
		int batchSize = 1000;
		List<String> removeCommandMapList = new ArrayList<>(removeCommandMap.keySet());
		int totalSize = removeCommandMapList.size();
		// 批次檢查
		for (int i = 0; i < totalSize; i += batchSize) {
			// System.out.println("S:" + i + ":" + Fm_T.to_yMd_Hm(new Date()));
			// 取得當前批次
			List<String> batchList = removeCommandMapList.subList(i, Math.min(i + batchSize, totalSize));
			// 執行 JPA 查詢
			List<Mocta> removeCheck = moctaDao.findAllByMocta(batchList);

			// 處理結果
			removeCheck.forEach(r -> {
				String nKey = r.getTa001_ta002() + "-" + r.getMb001();
				nKey = nKey.replaceAll("\\s", "");
				if ("Y".equalsIgnoreCase(r.getTa011()) && removeCommandMap.containsKey(nKey)) { // 忽略大小寫
					// 納入完結更新 -> 不需移除
					BasicCommandList ok = removeCommandMap.get(nKey);
					if (ok != null) {
						// System.out.println(nKey);
						ok.setSysstatus(1);
						ok.setBclerpconfirm("Y".equals(r.getTa011()) ? "已完工" : "y".equals(r.getTa011()) ? "指定完工" : // 不區分大小寫
								"未知狀態");
						ok.setBcltrqty(r.getTa017());
						commandLists.add(ok);
					}
				}
				removeCommandMap.remove(nKey);
			});
		}
		ArrayList<BasicCommandList> removeCommandList = new ArrayList<>(removeCommandMap.values());

		// Step5. 存入資料
		commandListDao.deleteAll(removeCommandList);
		commandListDao.saveAll(commandLists);//
		// 檢查新的致令單BIOS?
		biosService.biosNewOrderCheck(commandMaps);

	}

	// ============ A341 國內進貨單/ A342 國外進貨單/ A343 台北進貨單/A344 模具進貨單 / A345 無採購進貨單
	// ============
	public void erpSynchronizePurth() throws Exception {
		logger.info("===erpSynchronizePurth: 時間:{}", dateFormat.format(new Date()));
		// Step0.資料準備
		ArrayList<Purth> erpInEntitys = purthDao.findAllByPurth();// [ERP]資料
		Map<String, Purth> erpInMaps = new HashMap<>();// [ERP]資料
		List<String> bilclass = new ArrayList<String>();
		bilclass.add("A341");
		bilclass.add("A342");
		bilclass.add("A343");
		bilclass.add("A344");
		bilclass.add("A345");
		ArrayList<BasicIncomingList> entityOlds = incomingListDao.findAllByStatus(null, bilclass);// [Cloud]資料
		ArrayList<BasicIncomingList> saveLists = new ArrayList<BasicIncomingList>();// [Cloud]儲存
		Map<String, BasicIncomingList> removeInMap = new TreeMap<String, BasicIncomingList>();// [Cloud]儲存(移除)
		// Step1.資料整理
		for (Purth m : erpInEntitys) {
			String nKey = m.getTh001_th002() + "-" + m.getTh003();
			nKey = nKey.replaceAll("\\s", "");
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
			m.setTh001_th002(m.getTh001_th002().replaceAll("\\s", ""));
			m.setMb002(m.getMb002().replaceAll("\\s+$", ""));// 去除結尾空格
			m.setMb003(m.getMb003().replaceAll("\\s+$", ""));// 去除結尾空格
			m.setNewone(true);
			erpInMaps.put(nKey, m);
			wTFsSave.put(m.getTh001_th002().split("-")[0], 0);
			// 測試用
			// if (m.getTh001_th002().equals("A345-2512160001")) {
			// System.out.println(new Date() + ":" + nKey);
			// }
		}
		// Step2.[ERP vs Cloud]舊資料匹配
		// 入料
		entityOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+單項目號)
			String oKey = o.getBilclass() + "-" + o.getBilsn() + "-" + o.getBilnb();
			String bilfuser = o.getBilfuser();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpInMaps.containsKey(oKey)) {
				String nChecksum = erpInMaps.get(oKey).toString().replaceAll("\\s", "");
				erpInMaps.get(oKey).setNewone(false);// 標記:不是新的
				boolean erp_remove = bilfuser.equals("ERP_Remove(Auto)");
				boolean checksum = !o.getChecksum().equals(nChecksum);
				boolean checkbslfuser = (bilfuser.equals("") || bilfuser.contains("System"));// 尚未異動 or 有系統自動化?
				// 內容不同=>更新
				if (erp_remove || (checksum && checkbslfuser)) {
					Purth m = erpInMaps.get(oKey);
					// 尚未入料 or 系統標記 可修改
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(入)
					if (o.getBilfuser().indexOf("System") >= 0) {
						erpAutoCheckService.incomingAutoRe(o, wAsSave, wTFs, wCs, wMs, wAs);
					}
					// 資料轉換
					o = erpToCloudService.incomingOnePurth(o, m, checkSum, wTFs, wKs, wAs);
					// 自動完成
					o = erpAutoCheckService.incomingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
					saveLists.add(o);
				}
			} else if (bilfuser.equals("")) {
				// 可能移除? 結單?
				removeInMap.put(oKey, o);
			}
		});
		// Step3.[ERP vs Cloud]全新資料?
		// 入料
		erpInMaps.forEach((key, v) -> {
			// 測試用
//			if (key.equals("A345-2512160001-0001")) {
//				System.out.println(new Date() + ":" + key);
//			}
			if (v.isNewone()) {
				BasicIncomingList n = new BasicIncomingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				// 資料轉換
				n = erpToCloudService.incomingOnePurth(n, v, checkSum, wTFs, wKs, wAs);
				// 自動完成
				n = erpAutoCheckService.incomingAuto(n, wAsSave, wTFs, wCs, wMs, wAs);
				saveLists.add(n);
			}
		});
		// Step4.確認是否完結 or 被移除?
		List<String> removeInMapList = new ArrayList<>(removeInMap.keySet());
		int batchSize = 1500;
		int totalInSize = removeInMapList.size();
		// 批次檢查-進料類
		for (int i = 0; i < totalInSize; i += batchSize) {
			// 取得當前批次
			List<String> batchList = removeInMapList.subList(i, Math.min(i + batchSize, totalInSize));
			// 執行 JPA 查詢
			List<Purth> removeInCheck = purthDao.findAllByPurth60(batchList);
			// 處理結果
			removeInCheck.forEach(r -> {
				// 移除標記
				String nKey = (r.getTh001_th002() + "-" + r.getTh003()).replaceAll("\\s", "");
				// 已經完成->標記更新
				if ("Y".equals(r.getTh030()) && removeInMap.containsKey(nKey)) {
					BasicIncomingList o = removeInMap.get(nKey);
					o.setSysmuser("system");
					o.setSysstatus(1);// 完成
					o.setBilcheckin(1);
					saveLists.add(o);
				}
				removeInMap.remove(nKey);
			});
		}
		ArrayList<BasicIncomingList> removeInLists = new ArrayList<>(removeInMap.values());
		// 添加:移除標記
		removeInLists.forEach(r -> {
			// 排除已經有User
			if (r.getBilfuser().equals("")) {
				r = autoRemoveService.incomingAuto(r);
			} else if (r.getBilfuser().contains("System")) {
				erpAutoCheckService.incomingAutoRe(r, wAsSave, wTFs, wCs, wMs, wAs);
				r = autoRemoveService.incomingAuto(r);
			}
		});

		// Step5. 存入資料
		incomingListDao.saveAll(saveLists);
		incomingListDao.saveAll(removeInLists);

		// Step6. 自動結算
		erpAutoCheckService.settlementAuto(wAsSave, wAsAllNewSave);

		// Step7.重新更新庫存
		wAsSave = new HashMap<>();
		// 庫別清單
		areaDao.findAll().forEach(x -> {
			if (!wAs.containsKey(x.getWaaliasawmpnb())) {
				wAs.put(x.getWaaliasawmpnb(), x);
			}
		});
	}

	// ============ A541 廠內領料單/ A542 補料單/(A543 超領單)/ A551 委外領料單/ A561 廠內退料單/ A571/
	// 委外退料單
	public void erpSynchronizeMocte() throws Exception {
		logger.info("===erpSynchronizeMocte: 時間:{}", dateFormat.format(new Date()));
		// Step0.資料準備
		ArrayList<Mocte> erpEntitys = mocteDao.findAllByMocte();
		Map<String, Mocte> erpInMaps = new HashMap<>();
		Map<String, Mocte> erpShMaps = new HashMap<>();
		List<String> bilclass = new ArrayList<String>();
		bilclass.add("A541");
		bilclass.add("A542");
		bilclass.add("A543");
		bilclass.add("A551");
		bilclass.add("A561");
		bilclass.add("A571");
		ArrayList<BasicIncomingList> entityInOlds = incomingListDao.findAllByStatus(null, bilclass);// 取得[Cloud]
		ArrayList<BasicShippingList> entityShOlds = shippingListDao.findAllByStatus(null, bilclass);// 取得[Cloud]
		// 存入資料物件
		ArrayList<BasicIncomingList> saveInLists = new ArrayList<BasicIncomingList>();// [Cloud]儲存
		ArrayList<BasicShippingList> saveShLists = new ArrayList<BasicShippingList>();// [Cloud]儲存
		Map<String, BasicIncomingList> removeInMap = new TreeMap<String, BasicIncomingList>();// [Cloud]儲存(移除)
		Map<String, BasicShippingList> removeShMap = new TreeMap<String, BasicShippingList>();// [Cloud]儲存(移除)

		// Step1.資料整理
		for (Mocte m : erpEntitys) {
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
			m.setMb002(m.getMb002().replaceAll("\\s+$", ""));// 去除結尾空格
			m.setMb003(m.getMb003().replaceAll("\\s+$", ""));// 去除結尾空格
			m.setTe014(m.getTe014().replaceAll("\\s+$", ""));// 去除結尾空格
			m.setTa026_ta027_ta028(m.getTa026_ta027_ta028().replaceAll("\\s", ""));
			m.setTa001_ta002(m.getTa001_ta002() == null ? "" : m.getTa001_ta002().replaceAll("\\s", ""));
			String nKey = m.getTa026_ta027_ta028();
			// 測試用
			if (nKey.contains("A542-260204001")) {
				System.out.println(nKey);
			}
			m.setNewone(true);
			// 單別性質(退料類 需抓取 物料領退用量)
			String classNb = m.getTa026_ta027_ta028().split("-")[0];
			if (classNb.equals("A543") || classNb.equals("A561") || classNb.equals("A571")) {
				m.setTb004(m.getTe005());
			}
			// 單據性質別54.廠內領料,55.託外領料,56.廠內退料,57.託外退料
			if (m.getTc008().equals("54") || m.getTc008().equals("55")) {
				m.setTk000("領料類");
				erpShMaps.put(nKey, m);
				wTFsSave.put(m.getTa026_ta027_ta028().split("-")[0], 1);
			} else {
				m.setTk000("入料類");
				erpInMaps.put(nKey, m);
				wTFsSave.put(m.getTa026_ta027_ta028().split("-")[0], 0);
			}
			// 可能臨時修改
			if (m.getTb004() == null) {
				m.setTb004(0);
			}
		}
		// Step2.[ERP vs Cloud]舊資料匹配
		// 入料
		entityInOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+單項目號)
			String oKey = o.getBilclass() + "-" + o.getBilsn() + "-" + o.getBilnb();
			String bilfuser = o.getBilfuser();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpInMaps.containsKey(oKey)) {
				String nChecksum = erpInMaps.get(oKey).toString().replaceAll("\\s", "");
				erpInMaps.get(oKey).setNewone(false);// 標記:不是新的
				Mocte m = erpInMaps.get(oKey);
				boolean erp_remove = bilfuser.equals("ERP_Remove(Auto)");
				boolean checksum = !o.getChecksum().equals(nChecksum);
				boolean checkbslfuser = (bilfuser.equals("") || bilfuser.contains("System"));// 尚未異動 or 有系統自動化?
				boolean checkUpdated = !bilfuser.equals("") && !bilfuser.contains("✪") && // 已異動+尚未標記
						(!o.getBilpnqty().equals(m.getTb005()) || !o.getBilpnumber().equals(m.getMb001())
								|| !o.getBiltowho().contains(m.getTb009()));// 數量變化?+物料變化?+庫別變化?
				// 內容不同=>更新
				if (erp_remove || (checksum && checkbslfuser)) {
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(入)
					if (bilfuser.contains("System")) {
						erpAutoCheckService.incomingAutoRe(o, wAsSave, wTFs, wCs, wMs, wAs);
					}
					// 資料轉換
					o = erpToCloudService.incomingOneMocte(o, m, checkSum, wTFs, wKs, wAs);
					// 自動完成
					o = erpAutoCheckService.incomingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
					saveInLists.add(o);
				} else if (checkUpdated) {
					// 標記二次修正(數量不同+料號不同+庫別不同)
					o.setBilfuser("✪ " + bilfuser);
				}
			} else if (bilfuser.equals("")) {// 入料類限定
				// 可能移除? 結單?
				removeInMap.put(oKey, o);
			}
		});
		// 領料
		entityShOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+單項目號)
			String oKey = o.getBslclass() + "-" + o.getBslsn() + "-" + o.getBslnb();
			String bslfuser = o.getBslfuser();
			oKey = oKey.replaceAll("\\s", "");
			// 測試用
			if ("A542-260204001-0004".equals(oKey)) {
				System.out.println("A542-260204001-0004");
			}
			// 比對同一筆資料?->修正
			if (erpShMaps.containsKey(oKey)) {
				String nChecksum = erpShMaps.get(oKey).toString().replaceAll("\\s", "");// ERP檢查碼
				erpShMaps.get(oKey).setNewone(false);// 標記:不是新的
				Mocte m = erpShMaps.get(oKey);
				boolean erp_remove = bslfuser.equals("ERP_Remove(Auto)");// 被移除的項目?
				boolean checksum = !o.getChecksum().equals(nChecksum);// 內容物是否有不同?
				boolean checkbslfuser = (bslfuser.equals("") || bslfuser.contains("System"));// 尚未異動 or 有系統自動化?
				boolean checkUpdated = !bslfuser.equals("") && !bslfuser.contains("✪")// 已異動+尚未標記
						&& (!o.getBslpnqty().equals(m.getTb004()) || !o.getBslpnumber().equals(m.getMb001())
								|| !o.getBslfromwho().contains(m.getTb009()));// 數量變化?+物料變化?+庫別變化?
				// 內容不同=>更新
				if (erp_remove || (checksum && checkbslfuser)) {
					// 尚未領料 or 系統標記 可修改
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(領)
					if (bslfuser.contains("System")) {
						erpAutoCheckService.shippingAutoRe(o, wAsSave, wTFs, wCs, wMs, wAs);
					}
					// 資料轉換
					o = erpToCloudService.shippingOneMocte(o, m, checkSum, wTFs, wKs, wAs);
					// 自動完成
					o = erpAutoCheckService.shippingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
					saveShLists.add(o);
				} else if (checkUpdated) {
					// 標記二次修正(數量不同+料號不同)
					o.setBslfuser("✪ " + bslfuser);
				}
			} else if (o.getSysstatus() == 0) {// 領料類限定
				// 可能移除? 結單?
				removeShMap.put(oKey, o);
			}
		});
		// Step3.[ERP vs Cloud] 全新資料?
		// 入料
		erpInMaps.forEach((key, v) -> {
			if (v.isNewone() && v.getTk000().equals("入料類")) {
				BasicIncomingList n = new BasicIncomingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				// 資料轉換
				n = erpToCloudService.incomingOneMocte(n, v, checkSum, wTFs, wKs, wAs);
				// 自動完成
				n = erpAutoCheckService.incomingAuto(n, wAsSave, wTFs, wCs, wMs, wAs);
				saveInLists.add(n);
			}
		});
		// 領料
		erpShMaps.forEach((key, v) -> {
			if (v.isNewone() && v.getTk000().equals("領料類") && v.getTc009().equals("N")) {
				BasicShippingList n = new BasicShippingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				n.setChecksum(checkSum);
				// 資料轉換
				n = erpToCloudService.shippingOneMocte(n, v, checkSum, wTFs, wKs, wAs);
				// 自動完成
				n = erpAutoCheckService.shippingAuto(n, wAsSave, wTFs, wCs, wMs, wAs);
				saveShLists.add(n);
			}
		});
		// Step4.確認是否完結 or 被移除?
		List<String> removeInMapList = new ArrayList<>(removeInMap.keySet());
		List<String> removeShMapList = new ArrayList<>(removeShMap.keySet());
		int batchSize = 1500;
		int totalInSize = removeInMapList.size();
		int totalShSize = removeShMapList.size();
		// 批次檢查-進料類
		for (int i = 0; i < totalInSize; i += batchSize) {
			// 取得當前批次
			List<String> batchList = removeInMapList.subList(i, Math.min(i + batchSize, totalInSize));
			// 執行 JPA 查詢
			List<Mocte> removeInCheck = mocteDao.findAllByMocte60(batchList);
			// 處理結果
			removeInCheck.forEach(r -> {
				// 移除標記
				String nKey = r.getTa026_ta027_ta028().replaceAll("\\s", "");
				// 已經完成->標記更新
				if ("Y".equals(r.getTc009()) && removeInMap.containsKey(nKey)) {
					BasicIncomingList o = removeInMap.get(nKey);
					o.setSysmuser("system");
					o.setSysstatus(1);// 完成
					o.setBilcheckin(1);
					saveInLists.add(o);
				}
				removeInMap.remove(nKey);
			});
		}
		// 批次檢查-領料類
		for (int i = 0; i < totalShSize; i += batchSize) {
			// 取得當前批次
			List<String> batchList = removeShMapList.subList(i, Math.min(i + batchSize, totalShSize));
			//測試用
//			boolean exists = batchList.stream().anyMatch("A542-260204001-0004"::equals);
//			if (exists) {
//			    System.out.println("Stream 比對成功");
//			}
			
			// 執行 JPA 查詢
			List<Mocte> removeShCheck = mocteDao.findAllByMocte60(batchList);
			// 處理結果
			removeShCheck.forEach(r -> {
				// 移除標記
				String nKey = r.getTa026_ta027_ta028().replaceAll("\\s", "");
				// 測試用
//				if ("A542-260204001-0004".equals(nKey)) {
//					System.out.println("A542-260204001-0004");
//				}
				// 已經完成->標記更新
				if ("Y".equals(r.getTc009()) && removeShMap.containsKey(nKey)) {
					BasicShippingList o = removeShMap.get(nKey);
					o.setSysmuser("system");
					o.setSysstatus(1);// 完成
					o.setBslcheckin(1);
					saveShLists.add(o);
				}
				removeShMap.remove(nKey);
			});
		}
		ArrayList<BasicIncomingList> removeInLists = new ArrayList<>(removeInMap.values());
		ArrayList<BasicShippingList> removeShLists = new ArrayList<>(removeShMap.values());
		// 添加:移除標記
		removeInLists.forEach(r -> {
			// 排除已經有User
			if (r.getBilfuser().equals("")) {
				r = autoRemoveService.incomingAuto(r);
			} else if (r.getBilfuser().contains("System")) {
				erpAutoCheckService.incomingAutoRe(r, wAsSave, wTFs, wCs, wMs, wAs);
				r = autoRemoveService.incomingAuto(r);
			}
		});
		removeShLists.forEach(r -> {
			// 排除已經有User
			if (r.getBslfuser().equals("")) {
				r = autoRemoveService.shippingAuto(r);
			} else if (r.getBslfuser().contains("System")) {
				// 如果是 有System自動 則須歸還
				erpAutoCheckService.shippingAutoRe(r, wAsSave, wTFs, wCs, wMs, wAs);
				r = autoRemoveService.shippingAuto(r);
			}
		});

		// Step5. 存入資料
		incomingListDao.saveAll(saveInLists);
		incomingListDao.flush();
		shippingListDao.saveAll(saveShLists);
		shippingListDao.flush();
		incomingListDao.saveAll(removeInLists);
		shippingListDao.saveAll(removeShLists);
		// Step6. 自動結算
		erpAutoCheckService.settlementAuto(wAsSave, wAsAllNewSave);
		// Step7.重新更新庫存
		wAsSave = new HashMap<>();
		// 庫別清單
		areaDao.findAll().forEach(x -> {
			if (!wAs.containsKey(x.getWaaliasawmpnb())) {
				wAs.put(x.getWaaliasawmpnb(), x);
			}
		});
	}

	// ============ /(A543 超領單)/ A561 廠內退料單 / A571 委外退料單
	public void erpSynchronizeMocteOnlyA543A561A571() throws Exception {
		logger.info("===erpSynchronizeMocte: 時間:{}", dateFormat.format(new Date()));
		// Step0.資料準備
		ArrayList<Mocte> erpEntitys = mocteDao.findAllByMocteOnlyA543A561A571();
		Map<String, Mocte> erpInMaps = new HashMap<>();
		Map<String, Mocte> erpShMaps = new HashMap<>();
		List<String> bilclass = new ArrayList<String>();
		bilclass.add("A543");
		bilclass.add("A561");
		bilclass.add("A571");
		ArrayList<BasicIncomingList> entityInOlds = incomingListDao.findAllByStatus(null, bilclass);// 取得[Cloud]
		ArrayList<BasicShippingList> entityShOlds = shippingListDao.findAllByStatus(null, bilclass);// 取得[Cloud]
		// 存入資料物件
		ArrayList<BasicIncomingList> saveInLists = new ArrayList<BasicIncomingList>();// [Cloud]儲存
		ArrayList<BasicShippingList> saveShLists = new ArrayList<BasicShippingList>();// [Cloud]儲存
		Map<String, BasicIncomingList> removeInMap = new TreeMap<String, BasicIncomingList>();// [Cloud]儲存(移除)
		Map<String, BasicShippingList> removeShMap = new TreeMap<String, BasicShippingList>();// [Cloud]儲存(移除)

		// Step1.資料整理
		for (Mocte m : erpEntitys) {
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
			m.setMb002(m.getMb002().replaceAll("\\s+$", ""));// 去除結尾空格
			m.setMb003(m.getMb003().replaceAll("\\s+$", ""));// 去除結尾空格
			m.setTa026_ta027_ta028(m.getTa026_ta027_ta028().replaceAll("\\s", ""));
			m.setTa001_ta002(m.getTa001_ta002() == null ? "" : m.getTa001_ta002().replaceAll("\\s", ""));
			String nKey = m.getTa026_ta027_ta028();
			//
			m.setNewone(true);
			// 單別性質(退料類 需抓取 物料領退用量)
			String classNb = m.getTa026_ta027_ta028().split("-")[0];
			if (classNb.equals("A543") || classNb.equals("A561") || classNb.equals("A571")) {
				m.setTb004(m.getTe005());
			}
			// 單據性質別54.廠內領料,55.託外領料,56.廠內退料,57.託外退料
			if (m.getTc008().equals("54") || m.getTc008().equals("55")) {
				m.setTk000("領料類");
				erpShMaps.put(nKey, m);
				wTFsSave.put(m.getTa026_ta027_ta028().split("-")[0], 1);
			} else {
				m.setTk000("入料類");
				erpInMaps.put(nKey, m);
				wTFsSave.put(m.getTa026_ta027_ta028().split("-")[0], 0);
			}
			// 可能臨時修改
			if (m.getTb004() == null) {
				m.setTb004(0);
			}
		}
		// Step2.[ERP vs Cloud]舊資料匹配
		// 入料
		entityInOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+單項目號)
			String oKey = o.getBilclass() + "-" + o.getBilsn() + "-" + o.getBilnb();
			String bilfuser = o.getBilfuser();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpInMaps.containsKey(oKey)) {
				String nChecksum = erpInMaps.get(oKey).toString().replaceAll("\\s", "");
				erpInMaps.get(oKey).setNewone(false);// 標記:不是新的
				Mocte m = erpInMaps.get(oKey);
				boolean erp_remove = bilfuser.equals("ERP_Remove(Auto)");
				boolean checksum = !o.getChecksum().equals(nChecksum);
				boolean checkbslfuser = (bilfuser.equals("") || bilfuser.contains("System"));// 尚未異動 or 有系統自動化?
				boolean checkUpdated = !bilfuser.equals("") && !bilfuser.contains("✪") && // 已異動+尚未標記
						(!o.getBilpnqty().equals(m.getTb005()) || !o.getBilpnumber().equals(m.getMb001())
								|| !o.getBiltowho().contains(m.getTb009()));// 數量變化?+物料變化?+庫別變化?
				// 內容不同=>更新
				if (erp_remove || (checksum && checkbslfuser)) {
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(入)
					if (bilfuser.contains("System")) {
						erpAutoCheckService.incomingAutoRe(o, wAsSave, wTFs, wCs, wMs, wAs);
					}
					// 資料轉換
					o = erpToCloudService.incomingOneMocte(o, m, checkSum, wTFs, wKs, wAs);
					// 自動完成
					o = erpAutoCheckService.incomingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
					saveInLists.add(o);
				} else if (checkUpdated) {// 庫別變化?
					// 標記二次修正(數量不同+料號不同+庫別不同)
					o.setBilfuser("✪ " + bilfuser);
				}
			} else if (bilfuser.equals("")) {
				// 可能移除? 結單?
				removeInMap.put(oKey, o);
			}
		});
		// 領料
		entityShOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+單項目號)
			String oKey = o.getBslclass() + "-" + o.getBslsn() + "-" + o.getBslnb();
			String bslfuser = o.getBslfuser();
			oKey = oKey.replaceAll("\\s", "");
			// 比對同一筆資料?->修正
			if (erpShMaps.containsKey(oKey)) {
				String nChecksum = erpShMaps.get(oKey).toString().replaceAll("\\s", "");// ERP檢查碼
				erpShMaps.get(oKey).setNewone(false);// 標記:不是新的
				Mocte m = erpShMaps.get(oKey);
				boolean erp_remove = bslfuser.equals("ERP_Remove(Auto)");// 被移除的項目?
				boolean checksum = !o.getChecksum().equals(nChecksum);// 內容物是否有不同?
				boolean checkbslfuser = (bslfuser.equals("") || bslfuser.contains("System"));// 尚未異動 or 有系統自動化?
				boolean checkUpdated = !bslfuser.equals("") && !bslfuser.contains("✪")// 已異動+尚未標記
						&& (!o.getBslpnqty().equals(m.getTb004()) || !o.getBslpnumber().equals(m.getMb001())
								|| !o.getBslfromwho().contains(m.getTb009()));// 數量變化?+物料變化?+庫別變化?
				// 內容不同=>更新
				if (erp_remove || (checksum && checkbslfuser)) {
					// 尚未領料 or 系統標記 可修改
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(領)
					if (bslfuser.contains("System")) {
						erpAutoCheckService.shippingAutoRe(o, wAsSave, wTFs, wCs, wMs, wAs);
					}
					// 資料轉換
					o = erpToCloudService.shippingOneMocte(o, m, checkSum, wTFs, wKs, wAs);
					// 自動完成
					o = erpAutoCheckService.shippingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
					saveShLists.add(o);
				} else if (checkUpdated) {
					// 標記二次修正(數量不同+料號不同)
					o.setBslfuser("✪ " + bslfuser);
				}
			} else if (o.getSysstatus() == 0) {
				// 可能移除? 結單?
				removeShMap.put(oKey, o);
			}
		});
		// Step3.[ERP vs Cloud] 全新資料?
		// 入料
		erpInMaps.forEach((key, v) -> {
			if (v.isNewone() && v.getTk000().equals("入料類")) {
				BasicIncomingList n = new BasicIncomingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				// 資料轉換
				n = erpToCloudService.incomingOneMocte(n, v, checkSum, wTFs, wKs, wAs);
				// 自動完成
				n = erpAutoCheckService.incomingAuto(n, wAsSave, wTFs, wCs, wMs, wAs);
				saveInLists.add(n);
			}
		});
		// 領料
		erpShMaps.forEach((key, v) -> {
			if (v.isNewone() && v.getTk000().equals("領料類") && v.getTc016().equals("N")) {
				BasicShippingList n = new BasicShippingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				n.setChecksum(checkSum);
				// 資料轉換
				n = erpToCloudService.shippingOneMocte(n, v, checkSum, wTFs, wKs, wAs);
				// 自動完成
				n = erpAutoCheckService.shippingAuto(n, wAsSave, wTFs, wCs, wMs, wAs);
				saveShLists.add(n);
			}
		});
		// Step4.確認是否完結 or 被移除?
		List<String> removeInMapList = new ArrayList<>(removeInMap.keySet());
		List<String> removeShMapList = new ArrayList<>(removeShMap.keySet());
		int batchSize = 1500;
		int totalInSize = removeInMapList.size();
		int totalShSize = removeShMapList.size();
		// 批次檢查-進料類
		for (int i = 0; i < totalInSize; i += batchSize) {
			// 取得當前批次
			List<String> batchList = removeInMapList.subList(i, Math.min(i + batchSize, totalInSize));
			// 執行 JPA 查詢
			List<Mocte> removeInCheck = mocteDao.findAllByMocte60(batchList);
			// 處理結果
			removeInCheck.forEach(r -> {
				// 移除標記
				String nKey = r.getTa026_ta027_ta028().replaceAll("\\s", "");
				// 已經完成->標記更新
				if (("3".equals(r.getTc016()) || "N".equals(r.getTc016())) && removeInMap.containsKey(nKey)) {
					BasicIncomingList o = removeInMap.get(nKey);
					o.setSysmuser("system");
					o.setSysstatus(1);// 完成
					o.setBilcheckin(1);
					saveInLists.add(o);
				}
				removeInMap.remove(nKey);
			});
		}
		// 批次檢查-領料類
		for (int i = 0; i < totalShSize; i += batchSize) {
			// 取得當前批次
			List<String> batchList = removeShMapList.subList(i, Math.min(i + batchSize, totalShSize));
			// 執行 JPA 查詢
			List<Mocte> removeShCheck = mocteDao.findAllByMocte60(batchList);
			// 處理結果
			removeShCheck.forEach(r -> {
				// 移除標記
				String nKey = r.getTa026_ta027_ta028().replaceAll("\\s", "");
				// 已經完成->標記更新
				if (("3".equals(r.getTc016()) || "N".equals(r.getTc016())) && removeShMap.containsKey(nKey)) {
					BasicShippingList o = removeShMap.get(nKey);
					o.setSysmuser("system");
					o.setSysstatus(1);// 完成
					o.setBslcheckin(1);
					saveShLists.add(o);
				}
				removeShMap.remove(nKey);
			});
		}
		ArrayList<BasicIncomingList> removeInLists = new ArrayList<>(removeInMap.values());
		ArrayList<BasicShippingList> removeShLists = new ArrayList<>(removeShMap.values());
		// 添加:移除標記
		removeInLists.forEach(r -> {
			// 排除已經有User
			if (r.getBilfuser().equals("")) {
				r = autoRemoveService.incomingAuto(r);
			} else if (r.getBilfuser().contains("System")) {
				erpAutoCheckService.incomingAutoRe(r, wAsSave, wTFs, wCs, wMs, wAs);
				r = autoRemoveService.incomingAuto(r);
			}
		});
		removeShLists.forEach(r -> {
			// 排除已經有User
			if (r.getBslfuser().equals("")) {
				r = autoRemoveService.shippingAuto(r);
			} else if (r.getBslfuser().contains("System")) {
				// 如果是 有System自動 則須歸還
				erpAutoCheckService.shippingAutoRe(r, wAsSave, wTFs, wCs, wMs, wAs);
				r = autoRemoveService.shippingAuto(r);
			}
		});

		// Step5. 存入資料
		incomingListDao.saveAll(saveInLists);
		shippingListDao.saveAll(saveShLists);
		incomingListDao.saveAll(removeInLists);
		shippingListDao.saveAll(removeShLists);
		// Step6. 自動結算
		erpAutoCheckService.settlementAuto(wAsSave, wAsAllNewSave);
		// Step7.重新更新庫存
		wAsSave = new HashMap<>();
		// 庫別清單
		areaDao.findAll().forEach(x -> {
			if (!wAs.containsKey(x.getWaaliasawmpnb())) {
				wAs.put(x.getWaaliasawmpnb(), x);
			}
		});
	}

	// ============ 不帶入自動化處理/(A543 超領單)/ A561 廠內退料單 / A571 委外退料單
	public void erpSynchronizeMocteOnlyA543A561A571NotAuto() throws Exception {
		logger.info("===erpSynchronizeMocte: 時間:{}", dateFormat.format(new Date()));
		// Step0.資料準備
		ArrayList<Mocte> erpEntitys = mocteDao.findAllByMocteOnlyA543A561A571();
		Map<String, Mocte> erpInMaps = new HashMap<>();
		Map<String, Mocte> erpShMaps = new HashMap<>();
		List<String> bilclass = new ArrayList<String>();
		bilclass.add("A543");
		bilclass.add("A561");
		bilclass.add("A571");
		ArrayList<BasicIncomingList> entityInOlds = incomingListDao.findAllByStatus(null, bilclass);// 取得[Cloud]
		ArrayList<BasicShippingList> entityShOlds = shippingListDao.findAllByStatus(null, bilclass);// 取得[Cloud]
		// 存入資料物件
		ArrayList<BasicIncomingList> saveInLists = new ArrayList<BasicIncomingList>();// [Cloud]儲存
		ArrayList<BasicShippingList> saveShLists = new ArrayList<BasicShippingList>();// [Cloud]儲存
		Map<String, BasicIncomingList> removeInMap = new TreeMap<String, BasicIncomingList>();// [Cloud]儲存(移除)
		Map<String, BasicShippingList> removeShMap = new TreeMap<String, BasicShippingList>();// [Cloud]儲存(移除)

		// Step1.資料整理
		for (Mocte m : erpEntitys) {
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
			m.setMb002(m.getMb002().replaceAll("\\s+$", ""));// 去除結尾空格
			m.setMb003(m.getMb003().replaceAll("\\s+$", ""));// 去除結尾空格
			m.setTa026_ta027_ta028(m.getTa026_ta027_ta028().replaceAll("\\s", ""));
			m.setTa001_ta002(m.getTa001_ta002() == null ? "" : m.getTa001_ta002().replaceAll("\\s", ""));
			String nKey = m.getTa026_ta027_ta028();
			//
			m.setNewone(true);
			// 單別性質(退料類 需抓取 物料領退用量)
			String classNb = m.getTa026_ta027_ta028().split("-")[0];
			if (classNb.equals("A543") || classNb.equals("A561") || classNb.equals("A571")) {
				m.setTb004(m.getTe005());
			}
			// 單據性質別54.廠內領料,55.託外領料,56.廠內退料,57.託外退料
			if (m.getTc008().equals("54") || m.getTc008().equals("55")) {
				m.setTk000("領料類");
				erpShMaps.put(nKey, m);
				wTFsSave.put(m.getTa026_ta027_ta028().split("-")[0], 1);
			} else {
				m.setTk000("入料類");
				erpInMaps.put(nKey, m);
				wTFsSave.put(m.getTa026_ta027_ta028().split("-")[0], 0);
			}
			// 可能臨時修改
			if (m.getTb004() == null) {
				m.setTb004(0);
			}
		}
		// Step2.[ERP vs Cloud]舊資料匹配
		// 入料
		entityInOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+單項目號)
			String oKey = o.getBilclass() + "-" + o.getBilsn() + "-" + o.getBilnb();
			String bilfuser = o.getBilfuser();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpInMaps.containsKey(oKey)) {
				String nChecksum = erpInMaps.get(oKey).toString().replaceAll("\\s", "");
				erpInMaps.get(oKey).setNewone(false);// 標記:不是新的
				Mocte m = erpInMaps.get(oKey);
				boolean erp_remove = bilfuser.equals("ERP_Remove(Auto)");
				boolean checksum = !o.getChecksum().equals(nChecksum);
				boolean checkbslfuser = (bilfuser.equals("") || bilfuser.contains("System"));// 尚未異動 or 有系統自動化?
				boolean checkUpdated = !bilfuser.equals("") && !bilfuser.contains("✪") && // 已異動+尚未標記
						(!o.getBilpnqty().equals(m.getTb005()) || !o.getBilpnumber().equals(m.getMb001())
								|| !o.getBiltowho().contains(m.getTb009()));// 數量變化?+物料變化?+庫別變化?
				// 內容不同=>更新
				if (erp_remove || (checksum && checkbslfuser)) {
					String checkSum = m.toString().replaceAll("\\s", "");
					// 資料轉換
					o = erpToCloudService.incomingOneMocte(o, m, checkSum, wTFs, wKs, wAs);
					saveInLists.add(o);
				} else if (checkUpdated) {// 庫別變化?
					// 標記二次修正(數量不同+料號不同+庫別不同)
					o.setBilfuser("✪ " + bilfuser);
				}
			} else if (bilfuser.equals("")) {
				// 可能移除? 結單?
				removeInMap.put(oKey, o);
			}
		});
		// 領料
		entityShOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+單項目號)
			String oKey = o.getBslclass() + "-" + o.getBslsn() + "-" + o.getBslnb();
			String bslfuser = o.getBslfuser();
			oKey = oKey.replaceAll("\\s", "");
			// 比對同一筆資料?->修正
			if (erpShMaps.containsKey(oKey)) {
				String nChecksum = erpShMaps.get(oKey).toString().replaceAll("\\s", "");// ERP檢查碼
				erpShMaps.get(oKey).setNewone(false);// 標記:不是新的
				Mocte m = erpShMaps.get(oKey);
				boolean erp_remove = bslfuser.equals("ERP_Remove(Auto)");// 被移除的項目?
				boolean checksum = !o.getChecksum().equals(nChecksum);// 內容物是否有不同?
				boolean checkbslfuser = (bslfuser.equals("") || bslfuser.contains("System"));// 尚未異動 or 有系統自動化?
				boolean checkUpdated = !bslfuser.equals("") && !bslfuser.contains("✪")// 已異動+尚未標記
						&& (!o.getBslpnqty().equals(m.getTb004()) || !o.getBslpnumber().equals(m.getMb001())
								|| !o.getBslfromwho().contains(m.getTb009()));// 數量變化?+物料變化?+庫別變化?
				// 內容不同=>更新
				if (erp_remove || (checksum && checkbslfuser)) {
					// 尚未領料 or 系統標記 可修改
					String checkSum = m.toString().replaceAll("\\s", "");
					// 資料轉換
					o = erpToCloudService.shippingOneMocte(o, m, checkSum, wTFs, wKs, wAs);
					saveShLists.add(o);
				} else if (checkUpdated) {
					// 標記二次修正(數量不同+料號不同)
					o.setBslfuser("✪ " + bslfuser);
				}
			} else if (o.getSysstatus() == 0) {
				// 可能移除? 結單?
				removeShMap.put(oKey, o);
			}
		});
		// Step3.[ERP vs Cloud] 全新資料?
		// 入料
		erpInMaps.forEach((key, v) -> {
			if (v.isNewone() && v.getTk000().equals("入料類")) {
				BasicIncomingList n = new BasicIncomingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				// 資料轉換
				n = erpToCloudService.incomingOneMocte(n, v, checkSum, wTFs, wKs, wAs);
				n.setBilcuser("System(Type_Pass)");
				saveInLists.add(n);
			}
		});
		// 領料
		erpShMaps.forEach((key, v) -> {
			if (v.isNewone() && v.getTk000().equals("領料類") && v.getTc016().equals("N")) {
				BasicShippingList n = new BasicShippingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				n.setChecksum(checkSum);
				// 資料轉換
				n = erpToCloudService.shippingOneMocte(n, v, checkSum, wTFs, wKs, wAs);
				n.setBslcuser("System(Type_Pass)");
				saveShLists.add(n);
			}
		});
		// Step4.確認是否完結 or 被移除?
		List<String> removeInMapList = new ArrayList<>(removeInMap.keySet());
		List<String> removeShMapList = new ArrayList<>(removeShMap.keySet());
		int batchSize = 1500;
		int totalInSize = removeInMapList.size();
		int totalShSize = removeShMapList.size();
		// 批次檢查-進料類
		for (int i = 0; i < totalInSize; i += batchSize) {
			// 取得當前批次
			List<String> batchList = removeInMapList.subList(i, Math.min(i + batchSize, totalInSize));
			// 執行 JPA 查詢
			List<Mocte> removeInCheck = mocteDao.findAllByMocte60(batchList);
			// 處理結果
			removeInCheck.forEach(r -> {
				// 移除標記
				String nKey = r.getTa026_ta027_ta028().replaceAll("\\s", "");
				// 已經完成->標記更新
				if (("3".equals(r.getTc016()) || "N".equals(r.getTc016())) && removeInMap.containsKey(nKey)) {
					BasicIncomingList o = removeInMap.get(nKey);
					o.setSysmuser("system");
					o.setSysstatus(1);// 完成
					o.setBilcheckin(1);
					saveInLists.add(o);
				}
				removeInMap.remove(nKey);
			});
		}
		// 批次檢查-領料類
		for (int i = 0; i < totalShSize; i += batchSize) {
			// 取得當前批次
			List<String> batchList = removeShMapList.subList(i, Math.min(i + batchSize, totalShSize));
			// 執行 JPA 查詢
			List<Mocte> removeShCheck = mocteDao.findAllByMocte60(batchList);
			// 處理結果
			removeShCheck.forEach(r -> {
				// 移除標記
				String nKey = r.getTa026_ta027_ta028().replaceAll("\\s", "");
				// 已經完成->標記更新
				if (("3".equals(r.getTc016()) || "N".equals(r.getTc016())) && removeShMap.containsKey(nKey)) {
					BasicShippingList o = removeShMap.get(nKey);
					o.setSysmuser("system");
					o.setSysstatus(1);// 完成
					o.setBslcheckin(1);
					saveShLists.add(o);
				}
				removeShMap.remove(nKey);
			});
		}
		ArrayList<BasicIncomingList> removeInLists = new ArrayList<>(removeInMap.values());
		ArrayList<BasicShippingList> removeShLists = new ArrayList<>(removeShMap.values());
		// 添加:移除標記
		removeInLists.forEach(r -> {
			// 排除已經有User
			if (r.getBilfuser().equals("")) {
				r = autoRemoveService.incomingAuto(r);
			} else if (r.getBilfuser().contains("System")) {
				erpAutoCheckService.incomingAutoRe(r, wAsSave, wTFs, wCs, wMs, wAs);
				r = autoRemoveService.incomingAuto(r);
			}
		});
		removeShLists.forEach(r -> {
			// 排除已經有User
			if (r.getBslfuser().equals("")) {
				r = autoRemoveService.shippingAuto(r);
			} else if (r.getBslfuser().contains("System")) {
				// 如果是 有System自動 則須歸還
				erpAutoCheckService.shippingAutoRe(r, wAsSave, wTFs, wCs, wMs, wAs);
				r = autoRemoveService.shippingAuto(r);
			}
		});

		// Step5. 存入資料
		incomingListDao.saveAll(saveInLists);
		shippingListDao.saveAll(saveShLists);
		incomingListDao.saveAll(removeInLists);
		shippingListDao.saveAll(removeShLists);

	}

	// ============A581 生產入庫單 ============
	public void erpSynchronizeMoctf() throws Exception {
		logger.info("===erpSynchronizeMoctf: 時間:{}", dateFormat.format(new Date()));
		// Step0.資料準備
		ArrayList<Moctf> erpEntitys = moctfDao.findAllByMoctf();
		Map<String, Moctf> erpInMaps = new HashMap<>();
		List<String> bilclass = new ArrayList<String>();
		bilclass.add("A581");

		ArrayList<BasicIncomingList> entityOlds = incomingListDao.findAllByStatus(null, bilclass);// 取得[Cloud]
		ArrayList<BasicIncomingList> saveLists = new ArrayList<BasicIncomingList>();// [Cloud]儲存
		Map<String, BasicIncomingList> removeInMap = new TreeMap<String, BasicIncomingList>();// [Cloud]儲存(移除)

		// Step1.資料整理
		for (Moctf m : erpEntitys) {
			String nKey = m.getTg001_tg002_tg003();
			m.setTg001_tg002_tg003(m.getTg001_tg002_tg003().replaceAll("\\s", ""));
			m.setTg014_tg015(m.getTg014_tg015().replaceAll("\\s", ""));
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
			m.setMb002(m.getMb002().replaceAll("\\s+$", ""));// 去除結尾空格
			m.setMb003(m.getMb003().replaceAll("\\s+$", ""));// 去除結尾空格
			nKey = nKey.replaceAll("\\s", "");
			m.setNewone(true);
			erpInMaps.put(nKey, m);
			wTFsSave.put(m.getTg001_tg002_tg003().replaceAll("\\s", "").split("-")[0], 0);
		}
		// Step2.[ERP vs Cloud]舊資料匹配
		// 入料
		entityOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+物料號+單項目號)
			String oKey = o.getBilclass() + "-" + o.getBilsn() + "-" + o.getBilnb();
			String bilfuser = o.getBilfuser();
			oKey = oKey.replaceAll("\\s", "");
			// 測試
//			if("A581-260102055-0001".equals(oKey)) {
//				System.out.println(oKey);
//			}

			// 同一筆資料?
			if (erpInMaps.containsKey(oKey)) {
				String nChecksum = erpInMaps.get(oKey).toString().replaceAll("\\s", "");
				erpInMaps.get(oKey).setNewone(false);// 標記:不是新的
				boolean erp_remove = bilfuser.equals("ERP_Remove(Auto)");
				boolean checksum = !o.getChecksum().equals(nChecksum);
				boolean checkbslfuser = (bilfuser.equals("") || bilfuser.contains("System"));// 尚未異動 or 有系統自動化?
				// 內容不同=>更新
				if (erp_remove || (checksum && checkbslfuser)) {
					// 尚未入料 or 系統標記 可修改
					Moctf m = erpInMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(入)
					if (o.getBilfuser().indexOf("System") >= 0) {
						erpAutoCheckService.incomingAutoRe(o, wAsSave, wTFs, wCs, wMs, wAs);
					}
					// 資料轉換
					o = erpToCloudService.incomingOneMoctf(o, m, checkSum, wTFs, wKs, wAs);
					// 自動完成
					o = erpAutoCheckService.incomingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
					saveLists.add(o);
				}
			} else if (bilfuser.equals("")) {
				// 可能移除? 結單?
				removeInMap.put(oKey, o);
			}
		});
		// Step3.[ERP vs Cloud]全新資料?
		// 入料
		erpInMaps.forEach((key, v) -> {
			if (v.isNewone()) {
				BasicIncomingList n = new BasicIncomingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				// 資料轉換
				n = erpToCloudService.incomingOneMoctf(n, v, checkSum, wTFs, wKs, wAs);
				// 自動完成
				n = erpAutoCheckService.incomingAuto(n, wAsSave, wTFs, wCs, wMs, wAs);
				saveLists.add(n);
			}
		});
		// Step4.確認是否完結 or 被移除?
		List<String> removeInMapList = new ArrayList<>(removeInMap.keySet());
		int batchSize = 1500;
		int totalInSize = removeInMapList.size();
		// 批次檢查-進料類
		for (int i = 0; i < totalInSize; i += batchSize) {
			// 取得當前批次
			List<String> batchList = removeInMapList.subList(i, Math.min(i + batchSize, totalInSize));
			// 執行 JPA 查詢
			List<Moctf> removeInCheck = moctfDao.findAllByMoctf60(batchList);
			// 處理結果
			removeInCheck.forEach(r -> {
				// 移除標記
				String nKey = r.getTg001_tg002_tg003().replaceAll("\\s", "");
				// 已經完成->標記更新
				if ("Y".equals(r.getTg022()) && removeInMap.containsKey(nKey)) {
					BasicIncomingList o = removeInMap.get(nKey);
					o.setSysmuser("system");
					o.setSysstatus(1);// 完成
					o.setBilcheckin(1);
					saveLists.add(o);
				}
				removeInMap.remove(nKey);
			});
		}
		ArrayList<BasicIncomingList> removeInLists = new ArrayList<>(removeInMap.values());
		// 添加:移除標記
		removeInLists.forEach(r -> {
			// 排除已經有User
			if (r.getBilfuser().equals("")) {
				r = autoRemoveService.incomingAuto(r);
			} else if (r.getBilfuser().contains("System")) {
				erpAutoCheckService.incomingAutoRe(r, wAsSave, wTFs, wCs, wMs, wAs);
				r = autoRemoveService.incomingAuto(r);
			}
		});

		// Step5. 存入資料
		incomingListDao.saveAll(saveLists);
		incomingListDao.saveAll(removeInLists);
		// Step6. 自動結算
		erpAutoCheckService.settlementAuto(wAsSave, wAsAllNewSave);
		// Step7.重新更新庫存
		wAsSave = new HashMap<>();
		// 庫別清單
		areaDao.findAll().forEach(x -> {
			if (!wAs.containsKey(x.getWaaliasawmpnb())) {
				wAs.put(x.getWaaliasawmpnb(), x);
			}
		});
	}

	// ============不執行自動 A581 生產入庫單 ============
	public void erpSynchronizeMoctfNotAuto() throws Exception {
		logger.info("===erpSynchronizeMoctf: 時間:{}", dateFormat.format(new Date()));
		// Step0.資料準備
		ArrayList<Moctf> erpEntitys = moctfDao.findAllByMoctf();
		Map<String, Moctf> erpInMaps = new HashMap<>();
		List<String> bilclass = new ArrayList<String>();
		bilclass.add("A581");

		ArrayList<BasicIncomingList> entityOlds = incomingListDao.findAllByStatus(null, bilclass);// 取得[Cloud]
		ArrayList<BasicIncomingList> saveLists = new ArrayList<BasicIncomingList>();// [Cloud]儲存
		Map<String, BasicIncomingList> removeInMap = new TreeMap<String, BasicIncomingList>();// [Cloud]儲存(移除)

		// Step1.資料整理
		for (Moctf m : erpEntitys) {
			String nKey = m.getTg001_tg002_tg003();
			m.setTg001_tg002_tg003(m.getTg001_tg002_tg003().replaceAll("\\s", ""));
			m.setTg014_tg015(m.getTg014_tg015().replaceAll("\\s", ""));
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
			m.setMb002(m.getMb002().replaceAll("\\s+$", ""));// 去除結尾空格
			m.setMb003(m.getMb003().replaceAll("\\s+$", ""));// 去除結尾空格
			nKey = nKey.replaceAll("\\s", "");
			m.setNewone(true);
			erpInMaps.put(nKey, m);
			wTFsSave.put(m.getTg001_tg002_tg003().replaceAll("\\s", "").split("-")[0], 0);
		}
		// Step2.[ERP vs Cloud]舊資料匹配
		// 入料
		entityOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+物料號+單項目號)
			String oKey = o.getBilclass() + "-" + o.getBilsn() + "-" + o.getBilnb();
			String bilfuser = o.getBilfuser();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpInMaps.containsKey(oKey)) {
				String nChecksum = erpInMaps.get(oKey).toString().replaceAll("\\s", "");
				erpInMaps.get(oKey).setNewone(false);// 標記:不是新的
				boolean erp_remove = bilfuser.equals("ERP_Remove(Auto)");
				boolean checksum = !o.getChecksum().equals(nChecksum);
				boolean checkbslfuser = (bilfuser.equals("") || bilfuser.contains("System"));// 尚未異動 or 有系統自動化?
				// 內容不同=>更新
				if (erp_remove || (checksum && checkbslfuser)) {
					// 尚未入料 or 系統標記 可修改
					Moctf m = erpInMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 資料轉換
					o = erpToCloudService.incomingOneMoctf(o, m, checkSum, wTFs, wKs, wAs);
					saveLists.add(o);
				}
			} else if (bilfuser.equals("")) {
				// 可能移除? 結單?
				removeInMap.put(oKey, o);
			}
		});
		// Step3.[ERP vs Cloud]全新資料?
		// 入料
		erpInMaps.forEach((key, v) -> {
			if (v.isNewone()) {
				BasicIncomingList n = new BasicIncomingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				// 資料轉換
				n = erpToCloudService.incomingOneMoctf(n, v, checkSum, wTFs, wKs, wAs);
				n.setBilcuser("System(Type_Pass)");
				saveLists.add(n);
			}
		});
		// Step4.確認是否完結 or 被移除?
		List<String> removeInMapList = new ArrayList<>(removeInMap.keySet());
		int batchSize = 1500;
		int totalInSize = removeInMapList.size();
		// 批次檢查-進料類
		for (int i = 0; i < totalInSize; i += batchSize) {
			// 取得當前批次
			List<String> batchList = removeInMapList.subList(i, Math.min(i + batchSize, totalInSize));
			// 執行 JPA 查詢
			List<Moctf> removeInCheck = moctfDao.findAllByMoctf60(batchList);
			// 處理結果
			removeInCheck.forEach(r -> {
				// 移除標記
				String nKey = r.getTg001_tg002_tg003().replaceAll("\\s", "");
				// 已經完成->標記更新
				if ("Y".equals(r.getTg022()) && removeInMap.containsKey(nKey)) {
					BasicIncomingList o = removeInMap.get(nKey);
					o.setSysmuser("system");
					o.setSysstatus(1);// 完成
					o.setBilcheckin(1);
					saveLists.add(o);
				}
				removeInMap.remove(nKey);
			});
		}
		ArrayList<BasicIncomingList> removeInLists = new ArrayList<>(removeInMap.values());
		// 添加:移除標記
		removeInLists.forEach(r -> {
			// 排除已經有User
			if (r.getBilfuser().equals("")) {
				r = autoRemoveService.incomingAuto(r);
			} else if (r.getBilfuser().contains("System")) {
				erpAutoCheckService.incomingAutoRe(r, wAsSave, wTFs, wCs, wMs, wAs);
				r = autoRemoveService.incomingAuto(r);
			}
		});

		// Step5. 存入資料
		incomingListDao.saveAll(saveLists);
		incomingListDao.saveAll(removeInLists);
	}

	// ============ A591 委外進貨單 ============
	public void erpSynchronizeMocth() throws Exception {
		logger.info("===erpSynchronizeMocth: 時間:{}", dateFormat.format(new Date()));
		// Step0.資料準備
		ArrayList<Mocth> erpEntitys = mocthDao.findAllByMocth();
		Map<String, Mocth> erpInMaps = new HashMap<>();
		List<String> bilclass = new ArrayList<String>();
		bilclass.add("A591");

		ArrayList<BasicIncomingList> entityOlds = incomingListDao.findAllByStatus(null, bilclass);// 取得[Cloud]
		ArrayList<BasicIncomingList> saveLists = new ArrayList<BasicIncomingList>();// [Cloud]儲存
		Map<String, BasicIncomingList> removeInMap = new TreeMap<String, BasicIncomingList>();// [Cloud]儲存(移除)
		// Step1.資料整理
		for (Mocth m : erpEntitys) {
			String nKey = m.getTi001_ti002_ti003();
			nKey = nKey.replaceAll("\\s", "");
			m.setTi001_ti002_ti003(m.getTi001_ti002_ti003().replaceAll("\\s", ""));
			m.setTi013_ti014(m.getTi013_ti014().replaceAll("\\s", ""));
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
			m.setMb002(m.getMb002().replaceAll("\\s+$", ""));// 去除結尾空格
			m.setMb003(m.getMb003().replaceAll("\\s+$", ""));// 去除結尾空格
			m.setNewone(true);
			erpInMaps.put(nKey, m);
			wTFsSave.put(m.getTi001_ti002_ti003().split("-")[0], 0);
		}
		// Step2.[ERP vs Cloud]舊資料匹配
		// 入料
		entityOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+物料號+單項目號)
			String oKey = o.getBilclass() + "-" + o.getBilsn() + "-" + o.getBilnb();
			String bilfuser = o.getBilfuser();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpInMaps.containsKey(oKey)) {
				String nChecksum = erpInMaps.get(oKey).toString().replaceAll("\\s", "");
				erpInMaps.get(oKey).setNewone(false);// 標記:不是新的
				boolean erp_remove = bilfuser.equals("ERP_Remove(Auto)");
				boolean checksum = !o.getChecksum().equals(nChecksum);
				boolean checkbslfuser = (bilfuser.equals("") || bilfuser.contains("System"));// 尚未異動 or 有系統自動化?
				// 內容不同=>更新
				if (erp_remove || (checksum && checkbslfuser)) {
					Mocth m = erpInMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(入)
					if (o.getBilfuser().indexOf("System") >= 0) {
						erpAutoCheckService.incomingAutoRe(o, wAsSave, wTFs, wCs, wMs, wAs);
					}
					// 資料轉換
					o = erpToCloudService.incomingOneMocth(o, m, checkSum, wTFs, wKs, wAs);
					// 自動完成
					o = erpAutoCheckService.incomingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
					saveLists.add(o);
				}
			} else if (bilfuser.equals("")) {
				// 可能移除? 結單?
				removeInMap.put(oKey, o);
			}
		});
		// Step3.[ERP vs Cloud]全新資料?
		// 入料
		erpInMaps.forEach((key, v) -> {
			if (v.isNewone()) {
				BasicIncomingList n = new BasicIncomingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				n.setChecksum(checkSum);
				// 資料轉換
				n = erpToCloudService.incomingOneMocth(n, v, checkSum, wTFs, wKs, wAs);
				// 自動完成
				n = erpAutoCheckService.incomingAuto(n, wAsSave, wTFs, wCs, wMs, wAs);
				saveLists.add(n);
			}
		});
		// Step4.確認是否完結 or 被移除?
		List<String> removeInMapList = new ArrayList<>(removeInMap.keySet());
		int batchSize = 1500;
		int totalInSize = removeInMapList.size();
		// 批次檢查-進料類
		for (int i = 0; i < totalInSize; i += batchSize) {
			// 取得當前批次
			List<String> batchList = removeInMapList.subList(i, Math.min(i + batchSize, totalInSize));
			// 執行 JPA 查詢
			List<Mocth> removeInCheck = mocthDao.findAllByMocth80(batchList);
			// 處理結果
			removeInCheck.forEach(r -> {
				// 移除標記
				String nKey = r.getTi001_ti002_ti003().replaceAll("\\s", "");
				// 已經完成->標記更新
				if ("Y".equals(r.getTi037()) && removeInMap.containsKey(nKey)) {
					BasicIncomingList o = removeInMap.get(nKey);
					o.setSysmuser("system");
					o.setSysstatus(1);// 完成
					o.setBilcheckin(1);
					saveLists.add(o);
				}
				removeInMap.remove(nKey);
			});
		}
		ArrayList<BasicIncomingList> removeInLists = new ArrayList<>(removeInMap.values());
		// 添加:移除標記
		removeInLists.forEach(r -> {
			// 排除已經有User
			if (r.getBilfuser().equals("")) {
				r = autoRemoveService.incomingAuto(r);
			} else if (r.getBilfuser().contains("System")) {
				erpAutoCheckService.incomingAutoRe(r, wAsSave, wTFs, wCs, wMs, wAs);
				r = autoRemoveService.incomingAuto(r);
			}
		});

		// Step5. 存入資料
		incomingListDao.saveAll(saveLists);
		incomingListDao.saveAll(removeInLists);
		// Step6. 自動結算
		erpAutoCheckService.settlementAuto(wAsSave, wAsAllNewSave);
		// Step7.重新更新庫存
		wAsSave = new HashMap<>();
		// 庫別清單
		areaDao.findAll().forEach(x -> {
			if (!wAs.containsKey(x.getWaaliasawmpnb())) {
				wAs.put(x.getWaaliasawmpnb(), x);
			}
		});
	}

	// ============ A131 庫存借出單/ A141 庫存借入單 ============
	public void erpSynchronizeInvtg() throws Exception {
		logger.info("===erpSynchronizeInvtg: 時間:{}", dateFormat.format(new Date()));
		// Step0.資料準備
		ArrayList<Invtg> erpEntitys = invtgDao.findAllByInvtg();
		Map<String, Invtg> erpInMaps = new HashMap<>();
		Map<String, Invtg> erpShMaps = new HashMap<>();
		List<String> bilclass = new ArrayList<String>();
		bilclass.add("A131");
		bilclass.add("A141");
		ArrayList<BasicIncomingList> entityInOlds = incomingListDao.findAllByStatus(null, bilclass);// 取得[Cloud]
		ArrayList<BasicShippingList> entityShOlds = shippingListDao.findAllByStatus(null, bilclass);// 取得[Cloud]
		// 存入資料物件
		ArrayList<BasicIncomingList> saveInLists = new ArrayList<BasicIncomingList>();// [Cloud]儲存
		ArrayList<BasicShippingList> saveShLists = new ArrayList<BasicShippingList>();// [Cloud]儲存

		Map<String, BasicIncomingList> removeInMap = new TreeMap<String, BasicIncomingList>();// [Cloud]儲存(移除)
		Map<String, BasicShippingList> removeShMap = new TreeMap<String, BasicShippingList>();// [Cloud]儲存(移除)
		// Step1.資料整理
		for (Invtg m : erpEntitys) {
			String nKey = m.getTg001_tg002_tg003();
			m.setTg001_tg002_tg003(m.getTg001_tg002_tg003().replaceAll("\\s", ""));
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
			m.setMb002(m.getMb002().replaceAll("\\s+$", ""));// 去除結尾空格
			m.setMb003(m.getMb003().replaceAll("\\s+$", ""));// 去除結尾空格
			nKey = nKey.replaceAll("\\s", "");
			m.setNewone(true);
			// 單據性質別:A131 庫存借出單/ A141 庫存借入單
			if (m.getTg001_tg002_tg003().indexOf("A131") >= 0) {
				m.setTk000("領料類");
				erpShMaps.put(nKey, m);
				wTFsSave.put(m.getTg001_tg002_tg003().replaceAll("\\s", "").split("-")[0], 1);
			} else {
				m.setTk000("入料類");
				erpInMaps.put(nKey, m);
				wTFsSave.put(m.getTg001_tg002_tg003().replaceAll("\\s", "").split("-")[0], 0);
			}
		}
		// Step2.[ERP vs Cloud]舊資料匹配
		// 入料
		entityInOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+物料號+單項目號)
			String oKey = o.getBilclass() + "-" + o.getBilsn() + "-" + o.getBilnb();
			String bilfuser = o.getBilfuser();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpInMaps.containsKey(oKey)) {
				String nChecksum = erpInMaps.get(oKey).toString().replaceAll("\\s", "");
				erpInMaps.get(oKey).setNewone(false);// 標記:不是新的
				boolean erp_remove = bilfuser.equals("ERP_Remove(Auto)");
				boolean checksum = !o.getChecksum().equals(nChecksum);
				boolean checkbslfuser = (bilfuser.equals("") || bilfuser.contains("System"));// 尚未異動 or 有系統自動化?
				// 內容不同=>更新
				if (erp_remove || (checksum && checkbslfuser)) {
					Invtg m = erpInMaps.get(oKey);
					// 尚未入料 or 系統標記 可修改
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(入)
					if (o.getBilfuser().indexOf("System") >= 0) {
						erpAutoCheckService.incomingAutoRe(o, wAsSave, wTFs, wCs, wMs, wAs);
					}
					// 資料轉換
					o = erpToCloudService.incomingOneInvtg(o, m, checkSum, wTFs, wKs, wAs);
					// 自動完成
					o = erpAutoCheckService.incomingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
					saveInLists.add(o);
				}
			} else if (bilfuser.equals("")) {
				// 可能移除? 結單?
				removeInMap.put(oKey, o);
			}
		});
		// 領料
		entityShOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+物料號+單項目號)
			String oKey = o.getBslclass() + "-" + o.getBslsn() + "-" + o.getBslnb();
			String bslfuser = o.getBslfuser();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpShMaps.containsKey(oKey)) {
				String nChecksum = erpShMaps.get(oKey).toString().replaceAll("\\s", "");
				erpShMaps.get(oKey).setNewone(false);// 標記:不是新的
				boolean erp_remove = bslfuser.equals("ERP_Remove(Auto)");// 被移除的項目?
				boolean checksum = !o.getChecksum().equals(nChecksum);// 內容物是否有不同?
				boolean checkbslfuser = (bslfuser.equals("") || bslfuser.contains("System"));// 尚未異動 or 有系統自動化?
				// 內容不同=>更新
				if (erp_remove || (checksum && checkbslfuser)) {
					Invtg m = erpShMaps.get(oKey);
					// 尚未領料 or 系統標記 可修改
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(領)
					if (o.getBslfuser().indexOf("System") >= 0) {
						erpAutoCheckService.shippingAutoRe(o, wAsSave, wTFs, wCs, wMs, wAs);
					}
					// 資料轉換
					o = erpToCloudService.shippingOneInvtg(o, m, checkSum, wTFs, wKs, wAs);
					// 自動完成
					o = erpAutoCheckService.shippingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
					saveShLists.add(o);
				}
			} else if (o.getSysstatus() == 0) {
				// 可能移除? 結單?
				removeShMap.put(oKey, o);
			}
		});
		// Step3.[ERP vs Cloud]全新資料?
		// 入料
		erpInMaps.forEach((key, v) -> {
			if (v.isNewone() && v.getTk000().equals("入料類")) {
				BasicIncomingList n = new BasicIncomingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				// 資料轉換
				n = erpToCloudService.incomingOneInvtg(n, v, checkSum, wTFs, wKs, wAs);
				// 自動完成
				erpAutoCheckService.incomingAuto(n, wAsSave, wTFs, wCs, wMs, wAs);
				saveInLists.add(n);
			}
		});
		// 領料
		erpShMaps.forEach((key, v) -> {
			if (v.isNewone() && v.getTk000().equals("領料類") && v.getTf028().equals("1")) {
				BasicShippingList n = new BasicShippingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				// 資料轉換
				erpToCloudService.shippingOneInvtg(n, v, checkSum, wTFs, wKs, wAs);
				// 自動完成
				erpAutoCheckService.shippingAuto(n, wAsSave, wTFs, wCs, wMs, wAs);
				saveShLists.add(n);
			}
		});
		// Step4.確認是否完結 or 被移除?
		List<String> removeInMapList = new ArrayList<>(removeInMap.keySet());
		List<String> removeShMapList = new ArrayList<>(removeShMap.keySet());
		int batchSize = 1500;
		int totalInSize = removeInMapList.size();
		int totalShSize = removeShMapList.size();
		// 批次檢查-進料類
		for (int i = 0; i < totalInSize; i += batchSize) {
			// 取得當前批次
			List<String> batchList = removeInMapList.subList(i, Math.min(i + batchSize, totalInSize));
			// 執行 JPA 查詢
			List<Invtg> removeInCheck = invtgDao.findAllByInvtg80(batchList);
			// 處理結果
			removeInCheck.forEach(r -> {
				// 移除標記
				String nKey = r.getTg001_tg002_tg003().replaceAll("\\s", "");
				// 已經完成->標記更新
				if ("3".equals(r.getTf028()) && removeInMap.containsKey(nKey)) {
					BasicIncomingList o = removeInMap.get(nKey);
					o.setSysmuser("system");
					o.setSysstatus(1);// 完成
					o.setBilcheckin(1);
					saveInLists.add(o);
				}
				removeInMap.remove(nKey);

			});
		}
		// 批次檢查-領料類
		for (int i = 0; i < totalShSize; i += batchSize) {
			// 取得當前批次
			List<String> batchList = removeShMapList.subList(i, Math.min(i + batchSize, totalShSize));
			// 執行 JPA 查詢
			List<Invtg> removeShCheck = invtgDao.findAllByInvtg80(batchList);
			// 處理結果
			removeShCheck.forEach(r -> {
				// 移除標記
				String nKey = r.getTg001_tg002_tg003().replaceAll("\\s", "");
				// 已經完成->標記更新
				if ("3".equals(r.getTf028()) && removeShMap.containsKey(nKey)) {
					BasicShippingList o = removeShMap.get(nKey);
					o.setSysmuser("system");
					o.setSysstatus(1);// 完成
					o.setBslcheckin(1);
					saveShLists.add(o);
				}
				removeShMap.remove(nKey);
			});
		}
		ArrayList<BasicIncomingList> removeInLists = new ArrayList<>(removeInMap.values());
		ArrayList<BasicShippingList> removeShLists = new ArrayList<>(removeShMap.values());
		// 添加:移除標記
		removeInLists.forEach(r -> {
			// 排除已經有User
			if (r.getBilfuser().equals("")) {
				r = autoRemoveService.incomingAuto(r);
			} else if (r.getBilfuser().contains("System")) {
				erpAutoCheckService.incomingAutoRe(r, wAsSave, wTFs, wCs, wMs, wAs);
				r = autoRemoveService.incomingAuto(r);
			}
		});
		removeShLists.forEach(r -> {
			// 排除已經有User
			if (r.getBslfuser().equals("")) {
				r = autoRemoveService.shippingAuto(r);
			} else if (r.getBslfuser().contains("System")) {
				// 如果是 有System自動 則須歸還
				erpAutoCheckService.shippingAutoRe(r, wAsSave, wTFs, wCs, wMs, wAs);
				r = autoRemoveService.shippingAuto(r);
			}
		});

		// Step5. 存入資料
		incomingListDao.saveAll(saveInLists);
		shippingListDao.saveAll(saveShLists);
		incomingListDao.saveAll(removeInLists);
		shippingListDao.saveAll(removeShLists);
		// Step6. 自動結算
		erpAutoCheckService.settlementAuto(wAsSave, wAsAllNewSave);
		// Step7.重新更新庫存
		wAsSave = new HashMap<>();
		// 庫別清單
		areaDao.findAll().forEach(x -> {
			if (!wAs.containsKey(x.getWaaliasawmpnb())) {
				wAs.put(x.getWaaliasawmpnb(), x);
			}
		});
	}

	// ============ 借出歸還A151/借入歸還單A161 ============
	public void erpSynchronizeInvth() throws Exception {
		logger.info("===erpSynchronizeInvtg: 時間:{}", dateFormat.format(new Date()));
		// Step0.資料準備
		ArrayList<Invth> erpEntitys = invthDao.findAllByInvth();
		Map<String, Invth> erpInMaps = new HashMap<>();
		Map<String, Invth> erpShMaps = new HashMap<>();
		List<String> bilclass = new ArrayList<String>();
		bilclass.add("A151");
		bilclass.add("A161");
		ArrayList<BasicIncomingList> entityInOlds = incomingListDao.findAllByStatus(null, bilclass);
		ArrayList<BasicShippingList> entityShOlds = shippingListDao.findAllByStatus(null, bilclass);
		// 存入資料物件
		ArrayList<BasicIncomingList> saveInLists = new ArrayList<BasicIncomingList>();
		ArrayList<BasicShippingList> saveShLists = new ArrayList<BasicShippingList>();
		Map<String, BasicIncomingList> removeInMap = new TreeMap<String, BasicIncomingList>();// [Cloud]儲存(移除)
		Map<String, BasicShippingList> removeShMap = new TreeMap<String, BasicShippingList>();// [Cloud]儲存(移除)
		// Step1.資料整理
		for (Invth m : erpEntitys) {
			String nKey = m.getTi001_ti002_ti003();
			m.setTi001_ti002_ti003(m.getTi001_ti002_ti003().replaceAll("\\s", ""));
			m.setTi014_ti015_ti016(m.getTi014_ti015_ti016().replaceAll("\\s", ""));
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
			m.setMb002(m.getMb002().replaceAll("\\s+$", ""));// 去除結尾空格
			m.setMb003(m.getMb003().replaceAll("\\s+$", ""));// 去除結尾空格
			nKey = nKey.replaceAll("\\s", "");
			m.setNewone(true);
			// 單據性質別:借出歸還A151+借入歸還單A161
			if (m.getTi001_ti002_ti003().indexOf("A161") >= 0) {
				m.setTk000("領料類");
				erpShMaps.put(nKey, m);
				wTFsSave.put(m.getTi001_ti002_ti003().replaceAll("\\s", "").split("-")[0], 1);
			} else {
				m.setTk000("入料類");
				erpInMaps.put(nKey, m);
				wTFsSave.put(m.getTi001_ti002_ti003().replaceAll("\\s", "").split("-")[0], 0);
			}
		}
		// Step2.[ERP vs Cloud]舊資料匹配
		// 入料
		entityInOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+物料號+單項目號)
			String oKey = o.getBilclass() + "-" + o.getBilsn() + "-" + o.getBilnb();
			// 測試用
//			if(oKey.equals("A151-250604002-0001")) {
//				System.out.println(oKey);
//			}
			String bilfuser = o.getBilfuser();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpInMaps.containsKey(oKey)) {
				String nChecksum = erpInMaps.get(oKey).toString().replaceAll("\\s", "");
				erpInMaps.get(oKey).setNewone(false);// 標記:不是新的
				boolean erp_remove = bilfuser.equals("ERP_Remove(Auto)");
				boolean checksum = !o.getChecksum().equals(nChecksum);
				boolean checkbslfuser = (bilfuser.equals("") || bilfuser.contains("System"));// 尚未異動 or 有系統自動化?
				// 內容不同=>更新
				if (erp_remove || (checksum && checkbslfuser)) {
					Invth m = erpInMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(入)
					if (o.getBilfuser().indexOf("System") >= 0) {
						erpAutoCheckService.incomingAutoRe(o, wAsSave, wTFs, wCs, wMs, wAs);
					}
					// 資料轉換
					o = erpToCloudService.incomingOneInvth(o, m, checkSum, wTFs, wKs, wAs);
					// 自動完成
					o = erpAutoCheckService.incomingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
					saveInLists.add(o);
				}
			} else if (bilfuser.equals("")) {
				// 可能移除? 結單?
				removeInMap.put(oKey, o);
			}
		});
		// 領料
		entityShOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+單項目號)
			String oKey = o.getBslclass() + "-" + o.getBslsn() + "-" + o.getBslnb();
			String bslfuser = o.getBslfuser();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpShMaps.containsKey(oKey)) {
				String nChecksum = erpShMaps.get(oKey).toString().replaceAll("\\s", "");
				erpShMaps.get(oKey).setNewone(false);// 標記:不是新的
				boolean erp_remove = bslfuser.equals("ERP_Remove(Auto)");// 被移除的項目?
				boolean checksum = !o.getChecksum().equals(nChecksum);// 內容物是否有不同?
				boolean checkbslfuser = (bslfuser.equals("") || bslfuser.contains("System"));// 尚未異動 or 有系統自動化?
				// 內容不同=>更新
				if (erp_remove || (checksum && checkbslfuser)) {
					Invth m = erpShMaps.get(oKey);
					// 尚未領料 or 系統標記 可修改
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(領)
					if (o.getBslfuser().indexOf("System") >= 0) {
						erpAutoCheckService.shippingAutoRe(o, wAsSave, wTFs, wCs, wMs, wAs);
					}
					// 資料轉換
					o = erpToCloudService.shippingOneInvth(o, m, checkSum, wTFs, wKs, wAs);
					// 自動完成
					o = erpAutoCheckService.shippingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
					saveShLists.add(o);
				}
			} else if (o.getSysstatus() == 0) {
				// 可能移除? 結單?
				removeShMap.put(oKey, o);
			}
		});
		// Step3.[ERP vs Cloud]全新資料?
		// 入料
		erpInMaps.forEach((key, v) -> {
			if (v.isNewone() && v.getTk000().equals("入料類")) {
				BasicIncomingList n = new BasicIncomingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				// 資料轉換
				erpToCloudService.incomingOneInvth(n, v, checkSum, wTFs, wKs, wAs);
				// 自動完成
				n = erpAutoCheckService.incomingAuto(n, wAsSave, wTFs, wCs, wMs, wAs);
				saveInLists.add(n);
			}
		});
		// 領料
		erpShMaps.forEach((key, v) -> {
			if (v.isNewone() && v.getTk000().equals("領料類") && v.getTi022().equals("N")) {
				BasicShippingList n = new BasicShippingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				// 資料轉換
				erpToCloudService.shippingOneInvth(n, v, checkSum, wTFs, wKs, wAs);
				// 自動完成
				n = erpAutoCheckService.shippingAuto(n, wAsSave, wTFs, wCs, wMs, wAs);
				saveShLists.add(n);
			}
		});
		// Step4.確認是否完結 or 被移除?
		List<String> removeInMapList = new ArrayList<>(removeInMap.keySet());
		List<String> removeShMapList = new ArrayList<>(removeShMap.keySet());
		int batchSize = 1500;
		int totalInSize = removeInMapList.size();
		int totalShSize = removeShMapList.size();
		// 批次檢查-進料類
		for (int i = 0; i < totalInSize; i += batchSize) {
			// 取得當前批次
			List<String> batchList = removeInMapList.subList(i, Math.min(i + batchSize, totalInSize));
			// 執行 JPA 查詢
			List<Invth> removeInCheck = invthDao.findAllByInvth100(batchList);
			// 處理結果
			removeInCheck.forEach(r -> {
				// 移除標記
				String nKey = r.getTi001_ti002_ti003().replaceAll("\\s", "");
				// 已經完成->標記更新
				if ("Y".equals(r.getTi022()) && removeInMap.containsKey(nKey)) {
					BasicIncomingList o = removeInMap.get(nKey);
					o.setSysmuser("system");
					o.setSysstatus(1);// 完成
					o.setBilcheckin(1);
					saveInLists.add(o);
				}
				removeInMap.remove(nKey);

			});
		}
		// 批次檢查-領料類
		for (int i = 0; i < totalShSize; i += batchSize) {
			// 取得當前批次
			List<String> batchList = removeShMapList.subList(i, Math.min(i + batchSize, totalShSize));
			// 執行 JPA 查詢
			List<Invth> removeShCheck = invthDao.findAllByInvth100(batchList);
			// 處理結果
			removeShCheck.forEach(r -> {
				// 移除標記
				String nKey = r.getTi001_ti002_ti003().replaceAll("\\s", "");
				// 已經完成->標記更新
				if ("Y".equals(r.getTi022()) && removeShMap.containsKey(nKey)) {
					BasicShippingList o = removeShMap.get(nKey);
					o.setSysmuser("system");
					o.setSysstatus(1);// 完成
					o.setBslcheckin(1);
					saveShLists.add(o);
				}
				removeShMap.remove(nKey);
			});
		}
		ArrayList<BasicIncomingList> removeInLists = new ArrayList<>(removeInMap.values());
		ArrayList<BasicShippingList> removeShLists = new ArrayList<>(removeShMap.values());
		// 添加:移除標記
		removeInLists.forEach(r -> {
			// 排除已經有User
			if (r.getBilfuser().equals("")) {
				r = autoRemoveService.incomingAuto(r);
			} else if (r.getBilfuser().contains("System")) {
				erpAutoCheckService.incomingAutoRe(r, wAsSave, wTFs, wCs, wMs, wAs);
				r = autoRemoveService.incomingAuto(r);
			}
		});
		removeShLists.forEach(r -> {
			// 排除已經有User
			if (r.getBslfuser().equals("")) {
				r = autoRemoveService.shippingAuto(r);
			} else if (r.getBslfuser().contains("System")) {
				// 如果是 有System自動 則須歸還
				erpAutoCheckService.shippingAutoRe(r, wAsSave, wTFs, wCs, wMs, wAs);
				r = autoRemoveService.shippingAuto(r);
			}
		});

		// Step5. 存入資料
		incomingListDao.saveAll(saveInLists);
		shippingListDao.saveAll(saveShLists);
		incomingListDao.saveAll(removeInLists);
		shippingListDao.saveAll(removeShLists);
		// Step6. 自動結算
		erpAutoCheckService.settlementAuto(wAsSave, wAsAllNewSave);
		// Step7.重新更新庫存
		wAsSave = new HashMap<>();
		// 庫別清單
		areaDao.findAll().forEach(x -> {
			if (!wAs.containsKey(x.getWaaliasawmpnb())) {
				wAs.put(x.getWaaliasawmpnb(), x);
			}
		});
	}

	// ============ A111 費用領料單/ A112 費用退料單/A115/ A119 料號調整單/ A121 倉庫調撥單 ============
	public void erpSynchronizeInvta() throws Exception {
		logger.info("===erpSynchronizeInvtg: 時間:{}", dateFormat.format(new Date()));
		// Step0.資料準備
		ArrayList<Invta> erpEntitys = invtaDao.findAllByInvta();
		Map<String, Invta> erpInMaps = new HashMap<>();
		Map<String, Invta> erpShMaps = new HashMap<>();
		List<String> bilclass = new ArrayList<String>();
		bilclass.add("A111");
		bilclass.add("A112");
		bilclass.add("A115");
		bilclass.add("A119");
		bilclass.add("A121");
		ArrayList<BasicIncomingList> entityInOlds = incomingListDao.findAllByStatus(null, bilclass);
		ArrayList<BasicShippingList> entityShOlds = shippingListDao.findAllByStatus(null, bilclass);
		// 存入資料物件
		ArrayList<BasicIncomingList> saveInLists = new ArrayList<BasicIncomingList>();
		ArrayList<BasicShippingList> saveShLists = new ArrayList<BasicShippingList>();
		Map<String, BasicIncomingList> removeInMap = new TreeMap<String, BasicIncomingList>();// [Cloud]儲存(移除)
		Map<String, BasicShippingList> removeShMap = new TreeMap<String, BasicShippingList>();// [Cloud]儲存(移除)
		// Step1.資料整理
		for (Invta m : erpEntitys) {
			String nKey = m.getTb001_tb002_tb003();
			m.setTb001_tb002_tb003(m.getTb001_tb002_tb003().replaceAll("\\s", ""));
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
			m.setMb002(m.getMb002().replaceAll("\\s+$", ""));// 去除結尾空格
			m.setMb003(m.getMb003().replaceAll("\\s+$", ""));// 去除結尾空格
			nKey = nKey.replaceAll("\\s", "");
			m.setNewone(true);
			// 測試用
//			if (nKey.contains("A115")) {
//				System.out.println(nKey);
//			}
			// 單據性質別:
			if (m.getTb001_tb002_tb003().contains("A111")) {
				m.setTk000("領料類");
				erpShMaps.put(nKey, m);
				wTFsSave.put(m.getTb001_tb002_tb003().split("-")[0], 1);
			} else if (m.getTb001_tb002_tb003().contains("A112")) {
				m.setTk000("入料類");
				m.setTb013(m.getTb012());
				erpInMaps.put(nKey, m);
				wTFsSave.put(m.getTb001_tb002_tb003().split("-")[0], 0);
			} else if (m.getTb001_tb002_tb003().contains("A121")) {
				// 轉
				Invta mIn = new Invta();
				try {
					mIn = (Invta) m.clone();
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
				mIn.setTk000("入料類");
				erpInMaps.put(nKey, mIn);
				m.setTk000("領料類");
				erpShMaps.put(nKey, m);
				wTFsSave.put(m.getTb001_tb002_tb003().split("-")[0], 2);
			} else if (m.getTb001_tb002_tb003().contains("A119")) {// 料號調整單
				// 轉
				if (m.getTb007() < 0) {
					// 領
					m.setTk000("領料類");
					m.setTb007(Math.abs(m.getTb007()));
					erpShMaps.put(nKey, m);
				} else if (m.getTb007() > 0) {
					// 入
					m.setTk000("入料類");
					m.setTb013(m.getTb012());
					erpInMaps.put(nKey, m);
				}
				wTFsSave.put(m.getTb001_tb002_tb003().split("-")[0], 2);
			} else if (m.getTb001_tb002_tb003().contains("A115")) {
				// RMA領料
				wTFsSave.put(m.getTb001_tb002_tb003().split("-")[0], 1);
				// 領
				m.setTk000("領料類");
				erpShMaps.put(nKey, m);
			}
		}

		// Step2.[ERP vs Cloud]舊資料匹配
		// 進料
		entityInOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+單項目號)
			String oKey = o.getBilclass() + "-" + o.getBilsn() + "-" + o.getBilnb();
			String bilfuser = o.getBilfuser();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpInMaps.containsKey(oKey)) {
				String nChecksum = erpInMaps.get(oKey).toString().replaceAll("\\s", "");
				erpInMaps.get(oKey).setNewone(false);// 標記:不是新的
				boolean erp_remove = bilfuser.equals("ERP_Remove(Auto)");
				boolean checksum = !o.getChecksum().equals(nChecksum);
				boolean checkbilfuser = (bilfuser.equals("") || bilfuser.contains("System"));
				// 內容不同=>更新
				if (erp_remove || (checksum && checkbilfuser)) {
					Invta m = erpInMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(入)
					if (bilfuser.contains("System")) {
						erpAutoCheckService.incomingAutoRe(o, wAsSave, wTFs, wCs, wMs, wAs);
					}
					// 資料轉換
					o = erpToCloudService.incomingOneInvta(o, m, checkSum, wTFs, wKs, wAs);
					// 自動完成
					o = erpAutoCheckService.incomingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
					saveInLists.add(o);
				} else if (checksum) {
					// 只能改單據狀態
					removeInMap.put(oKey, o);
				}
			} else if (bilfuser.equals("")) {
				// 可能移除? 結單?
				removeInMap.put(oKey, o);
			}
		});
		// 領料
		entityShOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+單項目號)
			String oKey = o.getBslclass() + "-" + o.getBslsn() + "-" + o.getBslnb();
			String bslfuser = o.getBslfuser();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpShMaps.containsKey(oKey)) {
				String nChecksum = erpShMaps.get(oKey).toString().replaceAll("\\s", "");
				erpShMaps.get(oKey).setNewone(false);// 標記:不是新的
				boolean erp_remove = bslfuser.equals("ERP_Remove(Auto)");
				boolean checksum = !o.getChecksum().equals(nChecksum);
				boolean checkbslfuser = (bslfuser.equals("") || bslfuser.contains("System"));
				// 內容不同=>更新
				if (erp_remove || (checksum && checkbslfuser)) {
					Invta m = erpShMaps.get(oKey);
					// 尚未領料 or 系統標記 可修改
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(領)
					if (o.getBslfuser().indexOf("System") >= 0) {
						erpAutoCheckService.shippingAutoRe(o, wAsSave, wTFs, wCs, wMs, wAs);
					}
					// 資料轉換
					o = erpToCloudService.shippingOneInvta(o, m, checkSum, wTFs, wKs, wAs);
					// 自動完成
					o = erpAutoCheckService.shippingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
					saveShLists.add(o);
				} else if (checksum) {
					// 只能改單據狀態
					removeShMap.put(oKey, o);
				}
			} else if (bslfuser.equals("")) {
				// 可能移除? 結單?
				removeShMap.put(oKey, o);
			}
		});
		// Step3.[ERP vs Cloud]全新資料?
		// 入料
		erpInMaps.forEach((key, v) -> {
			// 測試用
//			if(key.indexOf("A121-231122005-0001")>=0) {
//				System.out.println(key);
//			}
			if (v.isNewone() && v.getTk000().equals("入料類")) {
				BasicIncomingList n = new BasicIncomingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				// 資料轉換
				erpToCloudService.incomingOneInvta(n, v, checkSum, wTFs, wKs, wAs);
				// 自動完成
				n = erpAutoCheckService.incomingAuto(n, wAsSave, wTFs, wCs, wMs, wAs);
				saveInLists.add(n);
			}
		});
		// 領料
		erpShMaps.forEach((key, v) -> {
			// 測試用
//			if (key.indexOf("A121-231113022-0001") >= 0) {
//				System.out.println(key);
//			}
			if (v.isNewone() && v.getTk000().equals("領料類")) {
				BasicShippingList n = new BasicShippingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				// 資料轉換
				erpToCloudService.shippingOneInvta(n, v, checkSum, wTFs, wKs, wAs);
				// 自動完成
				n = erpAutoCheckService.shippingAuto(n, wAsSave, wTFs, wCs, wMs, wAs);
				saveShLists.add(n);
			}
		});
		// Step4.確認是否完結 or 被移除?
		List<String> removeInMapList = new ArrayList<>(removeInMap.keySet());
		List<String> removeShMapList = new ArrayList<>(removeShMap.keySet());
		int batchSize = 1500;
		int totalInSize = removeInMapList.size();
		int totalShSize = removeShMapList.size();
		// 批次檢查-進料類
		for (int i = 0; i < totalInSize; i += batchSize) {
			// 取得當前批次
			List<String> batchList = removeInMapList.subList(i, Math.min(i + batchSize, totalInSize));
			// 執行 JPA 查詢
			List<Invta> removeInCheck = invtaDao.findAllByInvta60(batchList);
			// 處理結果
			removeInCheck.forEach(r -> {
				// 移除標記
				String nKey = r.getTb001_tb002_tb003().replaceAll("\\s", "");
				// 已經完成->標記更新
				if ("Y".equals(r.getTb018()) && removeInMap.containsKey(nKey)) {
					BasicIncomingList o = removeInMap.get(nKey);
					o.setSysmuser("system");
					o.setSysstatus(1);// 完成
					o.setBilcheckin(1);
					saveInLists.add(o);
				}
				removeInMap.remove(nKey);

			});
		}
		// 批次檢查-領料類
		for (int i = 0; i < totalShSize; i += batchSize) {
			// 取得當前批次
			List<String> batchList = removeShMapList.subList(i, Math.min(i + batchSize, totalShSize));
			// 執行 JPA 查詢
			List<Invta> removeShCheck = invtaDao.findAllByInvta60(batchList);
			// 處理結果
			removeShCheck.forEach(r -> {
				// 移除標記
				String nKey = r.getTb001_tb002_tb003().replaceAll("\\s", "");
				// 已經完成->標記更新
				if ("Y".equals(r.getTb018()) && removeShMap.containsKey(nKey)) {
					BasicShippingList o = removeShMap.get(nKey);
					o.setSysmuser("system");
					o.setSysstatus(1);// 完成
					o.setBslcheckin(1);
					saveShLists.add(o);
				}
				removeShMap.remove(nKey);
			});
		}
		ArrayList<BasicIncomingList> removeInLists = new ArrayList<>(removeInMap.values());
		ArrayList<BasicShippingList> removeShLists = new ArrayList<>(removeShMap.values());
		// 添加:移除標記
		removeInLists.forEach(r -> {
			// 排除已經有User
			if (r.getBilfuser().equals("")) {
				r = autoRemoveService.incomingAuto(r);
			} else if (r.getBilfuser().contains("System")) {
				erpAutoCheckService.incomingAutoRe(r, wAsSave, wTFs, wCs, wMs, wAs);
				r = autoRemoveService.incomingAuto(r);
			}
		});
		removeShLists.forEach(r -> {
			// 排除已經有User
			if (r.getBslfuser().equals("")) {
				r = autoRemoveService.shippingAuto(r);
			} else if (r.getBslfuser().contains("System")) {
				// 如果是 有System自動 則須歸還
				erpAutoCheckService.shippingAutoRe(r, wAsSave, wTFs, wCs, wMs, wAs);
				r = autoRemoveService.shippingAuto(r);
			}
		});

		// Step4. 存入資料
		incomingListDao.saveAll(saveInLists);
		shippingListDao.saveAll(saveShLists);
		incomingListDao.saveAll(removeInLists);
		shippingListDao.saveAll(removeShLists);
		// Step5. 自動結算
		erpAutoCheckService.settlementAuto(wAsSave, wAsAllNewSave);
		// Step7.重新更新庫存
		wAsSave = new HashMap<>();
		// 庫別清單
		areaDao.findAll().forEach(x -> {
			if (!wAs.containsKey(x.getWaaliasawmpnb())) {
				wAs.put(x.getWaaliasawmpnb(), x);
			}
		});
	}

	// ============ 組合單/A421 ============
	public void erpSynchronizeBomtd() throws Exception {
		logger.info("===erpSynchronizeBomtd: 時間:{}", dateFormat.format(new Date()));
		// Step0.資料準備
		ArrayList<Bomtd> erpEntitys = bomtdDao.findAllByBomtd();
		Map<String, Bomtd> erpInMaps = new HashMap<>();
		Map<String, Bomtd> erpShMaps = new HashMap<>();
		List<String> bilclass = new ArrayList<String>();
		bilclass.add("A421");
		ArrayList<BasicIncomingList> entityInOlds = incomingListDao.findAllByStatus(null, bilclass);// 取得[Cloud]
		ArrayList<BasicShippingList> entityShOlds = shippingListDao.findAllByStatus(null, bilclass);// 取得[Cloud]
		// 存入資料物件
		ArrayList<BasicIncomingList> saveInLists = new ArrayList<BasicIncomingList>();// [Cloud]儲存
		ArrayList<BasicShippingList> saveShLists = new ArrayList<BasicShippingList>();// [Cloud]儲存
		Map<String, BasicIncomingList> removeInMap = new TreeMap<String, BasicIncomingList>();// [Cloud]儲存(移除)
		Map<String, BasicShippingList> removeShMap = new TreeMap<String, BasicShippingList>();// [Cloud]儲存(移除)
		// Step1.資料整理
		String nKeyCheckSame = "";// 同一張單?
		for (Bomtd m : erpEntitys) {
			String nKey = m.getTe001_te002_te003();
			m.setTe001_te002_te003(m.getTe001_te002_te003().replaceAll("\\s", ""));
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
			m.setMb002(m.getMb002().replaceAll("\\s+$", ""));// 去除結尾空格
			m.setMb003(m.getMb003().replaceAll("\\s+$", ""));// 去除結尾空格
			nKey = nKey.replaceAll("\\s", "");
			m.setNewone(true);
			// 不同一張工單?
			if (!nKeyCheckSame.equals(nKey.split("-")[0] + "-" + nKey.split("-")[1])) {
				nKeyCheckSame = nKey.split("-")[0] + "-" + nKey.split("-")[1];
				try {
					Bomtd mIn = (Bomtd) m.clone();
					mIn.setTk000("入料類");
					erpInMaps.put(nKey, mIn);
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
				m.setTk000("領料類");
				m.setMb001(m.getTe004());// 領(元件號)
				erpShMaps.put(nKey, m);
				wTFsSave.put(m.getTe001_te002_te003().replaceAll("\\s", "").split("-")[0], 2);
			} else {
				m.setMb001(m.getTe004());// 領(元件號)
				m.setTk000("領料類");
				erpShMaps.put(nKey, m);
			}
		}
		// Step2.[ERP vs Cloud]舊資料匹配
		// 入料
		entityInOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+物料號+單項目號)
			String oKey = o.getBilclass() + "-" + o.getBilsn() + "-" + o.getBilnb();
			String bilfuser = o.getBilfuser();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpInMaps.containsKey(oKey)) {
				String nChecksum = erpInMaps.get(oKey).toString().replaceAll("\\s", "");
				erpInMaps.get(oKey).setNewone(false);// 標記:不是新的
				boolean erp_remove = bilfuser.equals("ERP_Remove(Auto)");
				boolean checksum = !o.getChecksum().equals(nChecksum);
				boolean checkbslfuser = (bilfuser.equals("") || bilfuser.contains("System"));// 尚未異動 or 有系統自動化?
				// 內容不同=>更新
				if (erp_remove || (checksum && checkbslfuser)) {
					Bomtd m = erpInMaps.get(oKey);
					// 尚未入料 or 系統標記 可修改
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(入)
					if (o.getBilfuser().indexOf("System") >= 0) {
						erpAutoCheckService.incomingAutoRe(o, wAsSave, wTFs, wCs, wMs, wAs);
					}
					// 資料轉換
					o = erpToCloudService.incomingOneBomtd(o, m, checkSum, wTFs, wKs, wAs);
					// 自動完成
					o = erpAutoCheckService.incomingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
					saveInLists.add(o);
				}
			} else if (bilfuser.equals("")) {
				// 可能移除? 結單?
				removeInMap.put(oKey, o);
			}
		});
		// 領料
		entityShOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+物料號+單項目號)
			String oKey = o.getBslclass() + "-" + o.getBslsn() + "-" + o.getBslnb();
			String bslfuser = o.getBslfuser();
			oKey = oKey.replaceAll("\\s", "");
			// 測試用
//			if (oKey.indexOf("-240507024") >= 0) {
//				System.out.println(oKey);
//			}
			// 同一筆資料?
			if (erpShMaps.containsKey(oKey)) {
				String nChecksum = erpShMaps.get(oKey).toString().replaceAll("\\s", "");
				erpShMaps.get(oKey).setNewone(false);// 標記:不是新的
				boolean erp_remove = bslfuser.equals("ERP_Remove(Auto)");// 被移除的項目?
				boolean checksum = !o.getChecksum().equals(nChecksum);// 內容物是否有不同?
				boolean checkbslfuser = (bslfuser.equals("") || bslfuser.contains("System"));// 尚未異動 or 有系統自動化?
				// 內容不同=>更新
				if (erp_remove || (checksum && checkbslfuser)) {
					Bomtd m = erpShMaps.get(oKey);
					// 尚未入料 or 系統標記 可修改
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(領)
					if (o.getBslfuser().indexOf("System") >= 0) {
						erpAutoCheckService.shippingAutoRe(o, wAsSave, wTFs, wCs, wMs, wAs);
					}
					// 資料轉換
					o = erpToCloudService.shippingOneBomtd(o, m, checkSum, wTFs, wKs, wAs);
					// 自動完成
					o = erpAutoCheckService.shippingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
					saveShLists.add(o);
				}
			} else if (o.getSysstatus() == 0) {
				// 可能移除? 結單?
				removeShMap.put(oKey, o);
			}
		});
		// Step3.[ERP vs Cloud]全新資料?
		// 入料
		erpInMaps.forEach((key, v) -> {
			if (v.isNewone() && v.getTk000().equals("入料類") && v.getTd010().equals("N")) {
				BasicIncomingList n = new BasicIncomingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				// 資料轉換
				n = erpToCloudService.incomingOneBomtd(n, v, checkSum, wTFs, wKs, wAs);
				// 自動完成
				erpAutoCheckService.incomingAuto(n, wAsSave, wTFs, wCs, wMs, wAs);
				saveInLists.add(n);
			}
		});
		// 領料
		erpShMaps.forEach((key, v) -> {
			if (v.isNewone() && v.getTk000().equals("領料類") && v.getTd010().equals("N")) {
				BasicShippingList n = new BasicShippingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				// 資料轉換
				erpToCloudService.shippingOneBomtd(n, v, checkSum, wTFs, wKs, wAs);
				// 自動完成
				erpAutoCheckService.shippingAuto(n, wAsSave, wTFs, wCs, wMs, wAs);
				saveShLists.add(n);
			}
		});
		// Step4.確認是否完結 or 被移除?
		List<String> removeInMapList = new ArrayList<>(removeInMap.keySet());
		List<String> removeShMapList = new ArrayList<>(removeShMap.keySet());
		int batchSize = 1500;
		int totalInSize = removeInMapList.size();
		int totalShSize = removeShMapList.size();
		// 批次檢查-進料類
		for (int i = 0; i < totalInSize; i += batchSize) {
			// 取得當前批次
			List<String> batchList = removeInMapList.subList(i, Math.min(i + batchSize, totalInSize));
			// 執行 JPA 查詢
			List<Bomtd> removeInCheck = bomtdDao.findAllByBomtd60(batchList);
			// 處理結果
			removeInCheck.forEach(r -> {
				// 移除標記
				String nKey = r.getTe001_te002_te003().replaceAll("\\s", "");
				// 已經完成->標記更新
				if ("Y".equals(r.getTe010()) && removeShMap.containsKey(nKey)) {
					BasicIncomingList o = removeInMap.get(nKey);
					o.setSysmuser("system");
					o.setSysstatus(1);// 完成
					o.setBilcheckin(1);
					saveInLists.add(o);
				}
				removeInMap.remove(nKey);
			});
		}
		// 批次檢查-領料類
		for (int i = 0; i < totalShSize; i += batchSize) {
			// 取得當前批次
			List<String> batchList = removeShMapList.subList(i, Math.min(i + batchSize, totalShSize));
			// 執行 JPA 查詢
			List<Bomtd> removeShCheck = bomtdDao.findAllByBomtd60(batchList);
			// 處理結果
			removeShCheck.forEach(r -> {
				// 移除標記
				String nKey = r.getTe001_te002_te003().replaceAll("\\s", "");
				// 已經完成->標記更新
				if ("Y".equals(r.getTe010()) && removeShMap.containsKey(nKey)) {
					BasicShippingList o = removeShMap.get(nKey);
					o.setSysmuser("system");
					o.setSysstatus(1);// 完成
					o.setBslcheckin(1);
					saveShLists.add(o);
				}
				removeShMap.remove(nKey);
			});
		}
		ArrayList<BasicIncomingList> removeInLists = new ArrayList<>(removeInMap.values());
		ArrayList<BasicShippingList> removeShLists = new ArrayList<>(removeShMap.values());
		// 添加:移除標記
		removeInLists.forEach(r -> {
			// 排除已經有User
			if (r.getBilfuser().equals("")) {
				r = autoRemoveService.incomingAuto(r);
			} else if (r.getBilfuser().contains("System")) {
				erpAutoCheckService.incomingAutoRe(r, wAsSave, wTFs, wCs, wMs, wAs);
				r = autoRemoveService.incomingAuto(r);
			}
		});
		removeShLists.forEach(r -> {
			// 排除已經有User
			if (r.getBslfuser().equals("")) {
				r = autoRemoveService.shippingAuto(r);
			} else if (r.getBslfuser().contains("System")) {
				// 如果是 有System自動 則須歸還
				erpAutoCheckService.shippingAutoRe(r, wAsSave, wTFs, wCs, wMs, wAs);
				r = autoRemoveService.shippingAuto(r);
			}
		});

		// Step5. 存入資料
		incomingListDao.saveAll(saveInLists);
		shippingListDao.saveAll(saveShLists);
		incomingListDao.saveAll(removeInLists);
		shippingListDao.saveAll(removeShLists);
		// Step6. 自動結算
		erpAutoCheckService.settlementAuto(wAsSave, wAsAllNewSave);
		// Step7.重新更新庫存
		wAsSave = new HashMap<>();
		// 庫別清單
		areaDao.findAll().forEach(x -> {
			if (!wAs.containsKey(x.getWaaliasawmpnb())) {
				wAs.put(x.getWaaliasawmpnb(), x);
			}
		});
	}

	// ============ OK 拆解單/A431 ============
	public void erpSynchronizeBomtf() throws Exception {
		logger.info("===erpSynchronizeBomtf: 時間:{}", dateFormat.format(new Date()));
		// Step0.資料準備
		ArrayList<Bomtf> erpEntitys = bomtfDao.findAllByBomtf();
		Map<String, Bomtf> erpInMaps = new HashMap<>();
		Map<String, Bomtf> erpShMaps = new HashMap<>();
		List<String> bilclass = new ArrayList<String>();
		bilclass.add("A431");
		ArrayList<BasicIncomingList> entityInOlds = incomingListDao.findAllByStatus(null, bilclass);// 取得[Cloud]
		ArrayList<BasicShippingList> entityShOlds = shippingListDao.findAllByStatus(null, bilclass);// 取得[Cloud]
		// 存入資料物件
		ArrayList<BasicIncomingList> saveInLists = new ArrayList<BasicIncomingList>();// [Cloud]儲存
		ArrayList<BasicShippingList> saveShLists = new ArrayList<BasicShippingList>();// [Cloud]儲存
		Map<String, BasicIncomingList> removeInMap = new TreeMap<String, BasicIncomingList>();// [Cloud]儲存(移除)
		Map<String, BasicShippingList> removeShMap = new TreeMap<String, BasicShippingList>();// [Cloud]儲存(移除)
		// Step1.資料整理
		String nKeyCheckSame = "";// 同一張單?
		for (Bomtf m : erpEntitys) {
			String nKey = m.getTg001_tg002_tg003();
			m.setTg001_tg002_tg003(m.getTg001_tg002_tg003().replaceAll("\\s", ""));
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
			m.setMb002(m.getMb002().replaceAll("\\s+$", ""));// 去除結尾空格
			m.setMb003(m.getMb003().replaceAll("\\s+$", ""));// 去除結尾空格
			nKey = nKey.replaceAll("\\s", "");
			m.setNewone(true);
			// 測試用
			if (nKey.equals("A431-260130001-0001")) {
				System.out.println(nKey);
			}
			// 不同一張工單?
			if (!nKeyCheckSame.equals(nKey.split("-")[0] + "-" + nKey.split("-")[1])) {
				nKeyCheckSame = nKey.split("-")[0] + "-" + nKey.split("-")[1];
				try {
					Bomtf mSh = (Bomtf) m.clone();
					mSh.setTk000("領料類");
					mSh.setMb001(m.getTe004());
					erpShMaps.put(nKey, mSh);
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
				m.setTk000("入料類");
				erpInMaps.put(nKey, m);
				wTFsSave.put(m.getTg001_tg002_tg003().split("-")[0], 2);
			} else {
				m.setTk000("入料類");
				erpInMaps.put(nKey, m);
			}

		}
		// Step2.[ERP vs Cloud]舊資料匹配
		// 入料
		entityInOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+物料號+單項目號)
			String oKey = o.getBilclass() + "-" + o.getBilsn() + "-" + o.getBilnb();
			String bilfuser = o.getBilfuser();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpInMaps.containsKey(oKey)) {
				String nChecksum = erpInMaps.get(oKey).toString().replaceAll("\\s", "");
				erpInMaps.get(oKey).setNewone(false);// 標記:不是新的
				boolean erp_remove = bilfuser.equals("ERP_Remove(Auto)");
				boolean checksum = !o.getChecksum().equals(nChecksum);
				boolean checkbslfuser = (bilfuser.equals("") || bilfuser.contains("System"));// 尚未異動 or 有系統自動化?
				// 內容不同=>更新
				if (erp_remove || (checksum && checkbslfuser)) {
					Bomtf m = erpInMaps.get(oKey);
					// 尚未入料 or 系統標記 可修改
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(入)
					if (o.getBilfuser().indexOf("System") >= 0) {
						erpAutoCheckService.incomingAutoRe(o, wAsSave, wTFs, wCs, wMs, wAs);
					}
					// 資料轉換
					o = erpToCloudService.incomingOneBomtf(o, m, checkSum, wTFs, wKs, wAs);
					// 自動完成
					o = erpAutoCheckService.incomingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
					saveInLists.add(o);
				}
			} else if (bilfuser.equals("")) {
				// 可能移除? 結單?
				removeInMap.put(oKey, o);
			}
		});
		// 領料
		entityShOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+物料號+單項目號)
			String oKey = o.getBslclass() + "-" + o.getBslsn() + "-" + o.getBslnb();
			String bslfuser = o.getBslfuser();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpShMaps.containsKey(oKey)) {
				String nChecksum = erpShMaps.get(oKey).toString().replaceAll("\\s", "");
				erpShMaps.get(oKey).setNewone(false);// 標記:不是新的
				boolean erp_remove = bslfuser.equals("ERP_Remove(Auto)");// 被移除的項目?
				boolean checksum = !o.getChecksum().equals(nChecksum);// 內容物是否有不同?
				boolean checkbslfuser = (bslfuser.equals("") || bslfuser.contains("System"));// 尚未異動 or 有系統自動化?
				// 內容不同=>更新
				if (erp_remove || (checksum && checkbslfuser)) {
					Bomtf m = erpShMaps.get(oKey);
					// 尚未入料 or 系統標記 可修改
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(領)
					if (o.getBslfuser().indexOf("System") >= 0) {
						erpAutoCheckService.shippingAutoRe(o, wAsSave, wTFs, wCs, wMs, wAs);
					}
					// 資料轉換
					o = erpToCloudService.shippingOneBomtf(o, m, checkSum, wTFs, wKs, wAs);
					// 自動完成
					o = erpAutoCheckService.shippingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
					saveShLists.add(o);
				}
			} else if (o.getSysstatus() == 0) {
				// 可能移除? 結單?
				removeShMap.put(oKey, o);
			}
		});
		// Step3.[ERP vs Cloud]全新資料?
		// 入料
		erpInMaps.forEach((key, v) -> {
			if (v.isNewone() && v.getTk000().equals("入料類")) {
				BasicIncomingList n = new BasicIncomingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				// 資料轉換
				n = erpToCloudService.incomingOneBomtf(n, v, checkSum, wTFs, wKs, wAs);
				// 如果已經是完成(則不寫入)->因太快核單 未核與已核
				// if (!n.getSysstatus().equals(1)) {
				// 自動完成
				erpAutoCheckService.incomingAuto(n, wAsSave, wTFs, wCs, wMs, wAs);
				saveInLists.add(n);
				// }
			}
		});
		// 領料
		erpShMaps.forEach((key, v) -> {
			// 測試用
			if (key.equals("A431-260130001-0001")) {
				System.out.println(key);
			}

			if (v.isNewone() && v.getTk000().equals("領料類")) {
				BasicShippingList n = new BasicShippingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				// 資料轉換
				erpToCloudService.shippingOneBomtf(n, v, checkSum, wTFs, wKs, wAs);
				// 如果已經是完成(則不寫入)->因太快核單 未核與已核
				// if (!n.getSysstatus().equals(1)) {
				// 自動完成
				erpAutoCheckService.shippingAuto(n, wAsSave, wTFs, wCs, wMs, wAs);
				saveShLists.add(n);
				// }
			}
		});
		// Step4.確認是否完結 or 被移除?
		List<String> removeInMapList = new ArrayList<>(removeInMap.keySet());
		List<String> removeShMapList = new ArrayList<>(removeShMap.keySet());
		int batchSize = 1500;
		int totalInSize = removeInMapList.size();
		int totalShSize = removeShMapList.size();
		// 批次檢查-進料類
		for (int i = 0; i < totalInSize; i += batchSize) {
			// 取得當前批次
			List<String> batchList = removeInMapList.subList(i, Math.min(i + batchSize, totalInSize));
			// 執行 JPA 查詢
			List<Bomtf> removeInCheck = bomtfDao.findAllByBomtf60(batchList);
			// 處理結果
			removeInCheck.forEach(r -> {
				// 移除標記
				String nKey = r.getTg001_tg002_tg003().replaceAll("\\s", "");
				// 已經完成->標記更新
				if ("Y".equals(r.getTg010()) && removeShMap.containsKey(nKey)) {
					BasicIncomingList o = removeInMap.get(nKey);
					o.setSysmuser("system");
					o.setSysstatus(1);// 完成
					o.setBilcheckin(1);
					saveInLists.add(o);
				}
				removeInMap.remove(nKey);
			});
		}
		// 批次檢查-領料類
		for (int i = 0; i < totalShSize; i += batchSize) {
			// 取得當前批次
			List<String> batchList = removeShMapList.subList(i, Math.min(i + batchSize, totalShSize));
			// 執行 JPA 查詢
			List<Bomtf> removeShCheck = bomtfDao.findAllByBomtf60(batchList);
			// 處理結果
			removeShCheck.forEach(r -> {
				// 移除標記
				String nKey = r.getTg001_tg002_tg003().replaceAll("\\s", "");
				// 已經完成->標記更新
				if ("Y".equals(r.getTg010()) && removeShMap.containsKey(nKey)) {
					BasicShippingList o = removeShMap.get(nKey);
					o.setSysmuser("system");
					o.setSysstatus(1);// 完成
					o.setBslcheckin(1);
					saveShLists.add(o);
				}
				removeShMap.remove(nKey);
			});
		}
		ArrayList<BasicIncomingList> removeInLists = new ArrayList<>(removeInMap.values());
		ArrayList<BasicShippingList> removeShLists = new ArrayList<>(removeShMap.values());
		// 添加:移除標記
		removeInLists.forEach(r -> {
			// 排除已經有User
			if (r.getBilfuser().equals("")) {
				r = autoRemoveService.incomingAuto(r);
			} else if (r.getBilfuser().contains("System")) {
				erpAutoCheckService.incomingAutoRe(r, wAsSave, wTFs, wCs, wMs, wAs);
				r = autoRemoveService.incomingAuto(r);
			}
		});
		removeShLists.forEach(r -> {
			// 排除已經有User
			if (r.getBslfuser().equals("")) {
				r = autoRemoveService.shippingAuto(r);
			} else if (r.getBslfuser().contains("System")) {
				// 如果是 有System自動 則須歸還
				erpAutoCheckService.shippingAutoRe(r, wAsSave, wTFs, wCs, wMs, wAs);
				r = autoRemoveService.shippingAuto(r);
			}
		});

		// Step5. 存入資料
		incomingListDao.saveAll(saveInLists);
		shippingListDao.saveAll(saveShLists);
		incomingListDao.saveAll(removeInLists);
		shippingListDao.saveAll(removeShLists);
		// Step6. 自動結算
		erpAutoCheckService.settlementAuto(wAsSave, wAsAllNewSave);
		// Step7.重新更新庫存
		wAsSave = new HashMap<>();
		// 庫別清單
		areaDao.findAll().forEach(x -> {
			if (!wAs.containsKey(x.getWaaliasawmpnb())) {
				wAs.put(x.getWaaliasawmpnb(), x);
			}
		});
	}

	// ============ 銷貨單 A231/A232
	public void erpSynchronizeCopth() throws Exception {
		logger.info("===erpSynchronizeCopth: 時間:{}", dateFormat.format(new Date()));
		// Step0.資料準備
		ArrayList<Copth> erpEntitys = copthDao.findAllByCopth();
		Map<String, Copth> erpShMaps = new HashMap<>();
		List<String> bilclass = new ArrayList<String>();
		bilclass.add("A231");
		bilclass.add("A232");
		ArrayList<BasicShippingList> entityShOlds = shippingListDao.findAllByStatus(null, bilclass);// 取得[Cloud]
		// 存入資料物件
		ArrayList<BasicShippingList> saveShLists = new ArrayList<BasicShippingList>();// [Cloud]儲存
		Map<String, BasicShippingList> removeShMap = new TreeMap<String, BasicShippingList>();// [Cloud]儲存(移除)

		// Step1.資料整理
		for (Copth m : erpEntitys) {
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
			m.setMb002(m.getMb002().replaceAll("\\s+$", ""));// 去除結尾空格
			m.setMb003(m.getMb003().replaceAll("\\s+$", ""));// 去除結尾空格
			m.setTh001_th002_th003(m.getTh001_th002_th003().replaceAll("\\s", ""));
			String nKey = m.getTh001_th002_th003();
			m.setNewone(true);
			erpShMaps.put(nKey, m);
			wTFsSave.put(m.getTh001_th002_th003().replaceAll("\\s", "").split("-")[0], 1);
		}
		// Step2.[ERP vs Cloud]舊資料匹配
		// 領料
		entityShOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+單項目號)
			String oKey = o.getBslclass() + "-" + o.getBslsn() + "-" + o.getBslnb();
			String bslfuser = o.getBslfuser();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpShMaps.containsKey(oKey)) {
				String nChecksum = erpShMaps.get(oKey).toString().replaceAll("\\s", "");
				erpShMaps.get(oKey).setNewone(false);// 標記:不是新的
				boolean erp_remove = bslfuser.equals("ERP_Remove(Auto)");// 被移除的項目?
				boolean checksum = !o.getChecksum().equals(nChecksum);// 內容物是否有不同?
				boolean checkbslfuser = (bslfuser.equals("") || bslfuser.contains("System"));// 尚未異動 or 有系統自動化?
				// 內容不同=>更新
				if (erp_remove || (checksum && checkbslfuser)) {
					Copth m = erpShMaps.get(oKey);
					// 尚未領料 or 系統標記 可修改
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(領)
					if (o.getBslfuser().indexOf("System") >= 0) {
						erpAutoCheckService.shippingAutoRe(o, wAsSave, wTFs, wCs, wMs, wAs);
					}
					// 資料轉換
					o = erpToCloudService.shippingOneCopth(o, m, checkSum, wTFs, wKs, wAs);
					// 自動完成
					o = erpAutoCheckService.shippingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
					saveShLists.add(o);
				}
			} else if (o.getSysstatus() == 0) {// 領料類限定
				// 可能移除? 結單?
				removeShMap.put(oKey, o);
			}
		});
		// Step3.[ERP vs Cloud] 全新資料?
		// 領料
		erpShMaps.forEach((key, v) -> {
			// 測試用
//				if(key.indexOf("A232-251030002")>=0) {
//					System.out.println(key);
//				}
			if (v.isNewone() && v.getTk000().equals("領料類") && v.getTh020().equals("N")) {
				BasicShippingList n = new BasicShippingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				n.setChecksum(checkSum);
				// 資料轉換
				n = erpToCloudService.shippingOneCopth(n, v, checkSum, wTFs, wKs, wAs);
				// 自動完成
				n = erpAutoCheckService.shippingAuto(n, wAsSave, wTFs, wCs, wMs, wAs);
				saveShLists.add(n);
			}
		});
		// Step4.確認是否完結 or 被移除?
		List<String> removeShMapList = new ArrayList<>(removeShMap.keySet());
		int batchSize = 1500;
		int totalShSize = removeShMapList.size();
		// 批次檢查-領料類
		for (int i = 0; i < totalShSize; i += batchSize) {
			// 取得當前批次
			List<String> batchList = removeShMapList.subList(i, Math.min(i + batchSize, totalShSize));
			// 執行 JPA 查詢
			List<Copth> removeShCheck = copthDao.findAllByCopth(batchList);
			// 處理結果
			removeShCheck.forEach(r -> {
				// 移除標記
				String nKey = r.getTh001_th002_th003().replaceAll("\\s", "");
				// 已經完成->標記更新
				if ("Y".equals(r.getTh020()) && removeShMap.containsKey(nKey)) {
					BasicShippingList o = removeShMap.get(nKey);
					o.setSysmuser("system");
					o.setSysstatus(1);// 完成
					o.setBslcheckin(1);
					saveShLists.add(o);
				}
				removeShMap.remove(nKey);
			});
		}
		ArrayList<BasicShippingList> removeShLists = new ArrayList<>(removeShMap.values());
		// Step5. 存入資料
		shippingListDao.saveAll(saveShLists);
		shippingListDao.saveAll(removeShLists);
		// Step6. 自動結算
		erpAutoCheckService.settlementAuto(wAsSave, wAsAllNewSave);
		// Step7.重新更新庫存
		wAsSave = new HashMap<>();
		// 庫別清單
		areaDao.findAll().forEach(x -> {
			if (!wAs.containsKey(x.getWaaliasawmpnb())) {
				wAs.put(x.getWaaliasawmpnb(), x);
			}
		});
	}

	// ============ 物料+儲位同步 ============
	public void erpSynchronizeInvtb() throws Exception {
		logger.info("===erpSynchronizeInvtb: 時間:{}", dateFormat.format(new Date()));
		// Step0.資料準備
		// 取得[頂新] 有效的物料....
		ArrayList<Invtb> erpEntitys = invtbDao.findAllByMoctb();
		Map<String, Invtb> erpListMaps = new HashMap<>();
		Map<String, Invtb> erpItemMaps = new HashMap<>();
		Map<String, String> erpConfigMaps = new HashMap<>();// A1500+原物料倉

		erpEntitys.forEach(m -> {
			// 物料號+倉別號+位置
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
			m.setMb002(m.getMb002().replaceAll("[\\n\\r\\t]+", " ").trim());
			m.setMb003(m.getMb003().replaceAll("[\\n\\r\\t]+", " ").trim());
			m.setMb009(m.getMb009().replaceAll("[\\n\\r\\t]+", " ").trim());
			m.setMc002(m.getMc002().trim());
			m.setNewone(true);
			// 測試用
//			if (m.getMb001().equals("81-105-382138")) {
//				System.out.println(m.getMb001() + ":" + m.getMc002());
//			}
			// ERP 倉別異常Null
			if (m.getMc002().equals("")) {
				m.setMc002(m.getMb017());// --倉別代號
				m.setCmc002(erpConfigMaps.get(m.getMb017()));// --倉別名稱
				m.setMc003("FF-FF-FF-FF");// --儲位
				m.setMc007(0);// 數量
			}

			// list(物料清單)-物料
			if (!erpListMaps.containsKey(m.getMb001())) {
				erpListMaps.put(m.getMb001(), m);
			}

			// item(區域清單)-物料儲位
			String nKey = (m.getMc002() + "_" + m.getMb001()).replaceAll("\\s", "");
			try {
				erpItemMaps.put(nKey, (Invtb) m.clone());
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}

			// config(倉別清單)-儲位
			if (m.getMc002() != null && !m.getMc002().equals("")) {
				erpConfigMaps.put(m.getMc002(), m.getCmc002());
			}
		});
		// 資料回收
		erpEntitys = null;

		// Step1. 取得[Cloud] 有效 物料+區域+倉儲設定
		List<WarehouseMaterial> listOlds = materialDao.findAll();
		List<WarehouseArea> areaOlds = areaDao.findAll();
		List<WarehouseConfig> configOlds = configDao.findAll();
		// 存入資料物件
		ArrayList<WarehouseMaterial> saveLists = new ArrayList<WarehouseMaterial>();
		ArrayList<WarehouseArea> saveItems = new ArrayList<WarehouseArea>();
		ArrayList<WarehouseMaterial> removeLists = new ArrayList<WarehouseMaterial>();
		// Step3-1.[物料清單] 資料整理轉換
		listOlds.forEach(o -> {
			String oKey = o.getWmpnb();
			// 同一筆資料?
			if (erpListMaps.containsKey(oKey)) {
				Invtb ov = erpListMaps.get(oKey);
				erpListMaps.get(oKey).setNewone(false);// 標記:不是新的
				// 內容更新(複寫)
				String checkSum = ov.toString().replaceAll("\\s", "");
				if (!checkSum.equals(o.getChecksum())) {
					o.setWmpnb(ov.getMb001());// 物料號
					o.setWmname(ov.getMb002());// 物料名稱
					o.setWmspecification(ov.getMb003());// 物料規格
					o.setWmdescription(ov.getMb009());// 物料敘述
					o.setChecksum(checkSum);
					saveLists.add(o);
				}
				// 標記有對應到->如果沒對應到的將移除
				o.setSysstatus(1);
			}
		});
		// 檢查那些要移除?
		listOlds.forEach(x -> {
			if (x.getSysstatus() == 0) {
				removeLists.add(x);
			}
			x.setSysstatus(0);
		});

		// Step3-2 [物料清單] 全新資料?
		erpListMaps.forEach((key, v) -> {
			if (v.isNewone()) {
				WarehouseMaterial n = new WarehouseMaterial();
				String checkSum = v.toString().replaceAll("\\s", "");
				n.setChecksum(checkSum);
				n.setWmpnb(v.getMb001());// 物料號
				n.setWmname(v.getMb002());// 物料名稱
				n.setWmspecification(v.getMb003());// 物料規格
				n.setWmdescription(v.getMb009());// 物料敘述
				saveLists.add(n);
			}
		});
		materialDao.saveAll(saveLists);
		materialDao.deleteAll(removeLists);
		// 資料回收
		listOlds = null;

		// Step4-1. [物料位置] 資料整理轉換
		Map<String, WarehouseArea> areaSameMap = new HashMap<>();
		ArrayList<BasicIncomingList> incomingLists = new ArrayList<>();
		ArrayList<BasicShippingList> shippingLists = new ArrayList<>();
		areaOlds.forEach(areaOld -> {// 區域庫別代號_物料號_
			String aKey = areaOld.getWaaliasawmpnb();
			// 同一筆?
			if (erpItemMaps.containsKey(aKey)) {
				// 測試用
//				if (aKey.equals("A0002_25-540-080026")) {
//					System.out.println(aKey);
//				}
				erpItemMaps.get(aKey).setNewone(false);// 標記:不是新的
				Invtb areaNew = erpItemMaps.get(aKey);
				String checkSumNew = areaNew.toString().replaceAll("\\s", "");
				String checkSumOld = areaOld.getChecksum();
				if (!checkSumNew.equals(checkSumOld)) {
					// 正則表達式:FF-FF-FF-FF
					Boolean checkloc = areaNew.getMc003().matches("[0-9A-Z]{2}-[0-9A-Z]{2}-[0-9A-Z]{2}-[0-9A-Z]{2}");

					// 新舊儲位不同時?
					if (checkloc && !areaNew.getMc003().equals(areaOld.getWaslocation())) {
						// 檢查單據修正位置(入料)
						incomingListDao.findAllBySearch(null, null, areaOld.getWawmpnb(), null, null).forEach(in -> {
							// 供應對象 要有內容
							if (in.getBiltowho().split("_").length > 1) {
								// 倉儲_物料
								String areaKey = in.getBiltowho().split("_")[0].replace("[", "") + "_"
										+ in.getBilpnumber();
								// [單據]要比對到[區域] 儲位物料
								if (areaKey.contains(areaOld.getWaaliasawmpnb())) {
									String oldLocation = in.getBiltowho().split("_")[2].replace("]", "");
									String newBiltowho = in.getBiltowho().replace(oldLocation,
											areaOld.getWaslocation());
									newBiltowho = newBiltowho.replaceAll(" ", "");
									in.setBiltowho(newBiltowho);
									incomingLists.add(in);
								}
							}
						});
						// 檢查單據修正位置(領料)
						shippingListDao.findAllBySearch(null, null, areaOld.getWawmpnb(), null, null).forEach(sh -> {
							// 供應來源 要有內容
							if (sh.getBslfromwho().split("_").length > 1) {
								// 倉儲_物料
								String areaKey = sh.getBslfromwho().split("_")[0].replace("[", "") + "_"
										+ sh.getBslpnumber();
								// [單據]要比對到[區域] 儲位物料
								if (areaKey.contains(areaOld.getWaaliasawmpnb())) {
									String oldLocation = sh.getBslfromwho().split("_")[2].replace("]", "");
									String newBslfromwho = sh.getBslfromwho().replace(oldLocation,
											areaOld.getWaslocation());
									newBslfromwho = newBslfromwho.replaceAll(" ", "");
									sh.setBslfromwho(newBslfromwho);
									shippingLists.add(sh);
								}
							}
						});
					}
					// 修正資料
					areaOld.setWawmpnb(areaNew.getMb001());// 物料號
					areaOld.setWaalias(areaNew.getMc002());// 倉庫別
					areaOld.setWaaliasawmpnb(areaNew.getMc002() + "_" + areaNew.getMb001());// 倉庫別+物料號
					areaOld.setWaslocation(checkloc ? areaNew.getMc003() : areaOld.getWaslocation());// 物料位置
					areaOld.setWaaname(areaNew.getCmc002() == null ? "" : areaNew.getCmc002());// 倉庫名稱
					areaOld.setWaerptqty(areaNew.getMc007());// 倉儲數量
					areaOld.setChecksum(checkSumNew);
					areaOld.setSysmdate(new Date());
					areaOld.setSysmuser("system");
					saveItems.add(areaOld);
				}
			}
			areaSameMap.put(areaOld.getWaaliasawmpnb(), areaOld);
		});
		// 資料回收
		areaOlds = null;

		// Step4-2. [物料位置] 全新資料?
		erpItemMaps.forEach((key, v) -> {
			// 測試用
//			if (key.equals("A0002_81-105-361134")) {
//				System.out.println(key);
//			}
			if (v.isNewone()) {
				// 可能重複?
				if (areaSameMap.containsKey(v.getMc002() + "_" + v.getMb001())) {
					// System.out.println(v.getMc002() + "_" + v.getMb001());
				} else {
					// 正則表達式:FF-FF-FF-FF
					Boolean checkloc = v.getMc003().matches("[0-9A-Z]{2}-[0-9A-Z]{2}-[0-9A-Z]{2}-[0-9A-Z]{2}");
					WarehouseArea n = new WarehouseArea();
					String checkSum = v.toString().replaceAll("\\s", "");
					n.setChecksum(checkSum);
					n.setWawmpnb(v.getMb001());// 物料號
					n.setWaalias(v.getMc002());// 倉庫別
					n.setWaaliasawmpnb(v.getMc002() + "_" + v.getMb001());// 倉庫別+物料號
					n.setWaslocation(checkloc ? v.getMc003() : "FF-FF-FF-FF");// 物料位置
					n.setWaaname(v.getCmc002() == null ? "" : v.getCmc002());// 倉庫名稱
					n.setWaerptqty(v.getMc007());// 倉儲數量
					///
					n.setWatqty(n.getWaslocation().equals("FF-FF-FF-FF") ? 0 : v.getMc007());// (實際)倉儲數量[如果是FF-FF-FF-FF//
																								// 則實際庫存0]
					saveItems.add(n);
				}
			}
		});
		areaDao.saveAll(saveItems);
		incomingListDao.saveAll(incomingLists);
		shippingListDao.saveAll(shippingLists);
		// Step5 儲位設定 全新資料?
		erpSynchronizeWconfig(erpConfigMaps, configOlds);
	}

	// ============ (全新)物料+儲位同步 ============
	public void erpAllNewAreaSynchronize() throws Exception {
		wAsAllNewSave.forEach((key, v) -> {
			// 必須是正數
			if (v > 0) {
				WarehouseArea areaNew = new WarehouseArea();
				areaNew.setChecksum("");
				areaNew.setWaalias(key.split("_")[0]);// 倉庫別
				areaNew.setWawmpnb(key.split("_")[1]);// 物料號
				areaNew.setWaaliasawmpnb(key);// 倉庫別+物料號
				areaNew.setWaslocation("FF-FF-FF-FF");// 物料位置
				areaNew.setWaaname("");// 倉庫名稱
				areaNew.setWaerptqty(v);// (帳上)倉儲數量
				areaNew.setWatqty(v);// (實際)倉儲數量[如果是FF-FF-FF-FF//
				areaDao.save(areaNew);
			}
		});
	}

	// ============ 儲位過濾設定 ============
	public void erpSynchronizeWconfig(Map<String, String> erpConfigMaps, List<WarehouseConfig> configOlds)
			throws Exception {
		ArrayList<WarehouseConfig> saveConfig = new ArrayList<WarehouseConfig>();
		erpConfigMaps.forEach((key, v) -> {
			boolean checkNew = true;
			// 是否重複?
			for (WarehouseConfig c : configOlds) {
				if (c.getWcalias().equals(key)) {
					checkNew = false;
					break;
				}
			}
			if (checkNew && v != null) {
				WarehouseConfig newC = new WarehouseConfig();
				newC.setWcalias(key);
				newC.setWcwkaname(v);
				saveConfig.add(newC);
			}
		});
		configDao.saveAll(saveConfig);
	}

	// ============ 單據過濾設定 ============
	public void erpSynchronizeWtypeFilter() throws Exception {
		ArrayList<WarehouseTypeFilter> saveFilters = new ArrayList<WarehouseTypeFilter>();
		ArrayList<WarehouseTypeFilter> filters = filterDao.findAllBySearch(null, null, null);
		Map<String, WarehouseTypeFilter> oldEntity = new HashMap<>();
		filters.forEach(f -> {
			oldEntity.put(f.getWtfcode(), f);
		});

		wTFsSave.forEach((key, val) -> {
			// 如過舊資料沒有?->新增
			if (!oldEntity.containsKey(key)) {
				WarehouseTypeFilter newEntity = new WarehouseTypeFilter();
				newEntity.setWtfcode(key);
				newEntity.setWtftype(val);// 單據類型 0=入庫 / 1=出庫 / 2=轉移
				saveFilters.add(newEntity);
			}
		});
		filterDao.saveAll(saveFilters);
	}

	// ============ 單據移除(360天以前資料) ============
	public void remove360DayData() throws Exception {
		Date countD50 = Fm_T.to_count(-50, new Date());
		Date countD100 = Fm_T.to_count(-100, new Date());
		Date countD600 = Fm_T.to_count(-600, new Date());
		// 出
		ArrayList<BasicShippingList> shRemove = shippingListDao.findAllBySyscdateRemove(countD50);
		shippingListDao.deleteAll(shRemove);
		// 進
		ArrayList<BasicIncomingList> inRemove = incomingListDao.findAllBySyscdateRemove(countD100);
		incomingListDao.deleteAll(inRemove);
		// 命令
		ArrayList<BasicCommandList> coRemove = commandListDao.findAllBySyscdateRemove(countD600);
		commandListDao.deleteAll(coRemove);
	}

}