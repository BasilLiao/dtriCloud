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

import com.google.gson.JsonObject;

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
import dtri.com.tw.mssql.entity.Invma;
import dtri.com.tw.mssql.entity.Invta;
import dtri.com.tw.mssql.entity.Invtb;
import dtri.com.tw.mssql.entity.Invtg;
import dtri.com.tw.mssql.entity.Invth;
import dtri.com.tw.mssql.entity.Mocta;
import dtri.com.tw.mssql.entity.MoctaScheduleOutsourcer;
import dtri.com.tw.mssql.entity.Mocte;
import dtri.com.tw.mssql.entity.Moctf;
import dtri.com.tw.mssql.entity.Mocth;
import dtri.com.tw.mssql.entity.Purth;
import dtri.com.tw.pgsql.dao.BasicCommandListDao;
import dtri.com.tw.pgsql.dao.BasicIncomingListDao;
import dtri.com.tw.pgsql.dao.BasicNotificationMailDao;
import dtri.com.tw.pgsql.dao.BasicProductModelDao;
import dtri.com.tw.pgsql.dao.BasicShippingListDao;
import dtri.com.tw.pgsql.dao.BiosPrincipalDao;
import dtri.com.tw.pgsql.dao.BiosVersionDao;
import dtri.com.tw.pgsql.dao.ScheduleOutsourcerDao;
import dtri.com.tw.pgsql.dao.WarehouseAreaDao;
import dtri.com.tw.pgsql.dao.WarehouseConfigDao;
import dtri.com.tw.pgsql.dao.WarehouseKeeperDao;
import dtri.com.tw.pgsql.dao.WarehouseMaterialDao;
import dtri.com.tw.pgsql.dao.WarehouseTypeFilterDao;
import dtri.com.tw.pgsql.entity.BasicCommandList;
import dtri.com.tw.pgsql.entity.BasicIncomingList;
import dtri.com.tw.pgsql.entity.BasicNotificationMail;
import dtri.com.tw.pgsql.entity.BasicProductModel;
import dtri.com.tw.pgsql.entity.BasicShippingList;
import dtri.com.tw.pgsql.entity.BiosPrincipal;
import dtri.com.tw.pgsql.entity.BiosVersion;
import dtri.com.tw.pgsql.entity.ScheduleOutsourcer;
import dtri.com.tw.pgsql.entity.WarehouseArea;
import dtri.com.tw.pgsql.entity.WarehouseConfig;
import dtri.com.tw.pgsql.entity.WarehouseKeeper;
import dtri.com.tw.pgsql.entity.WarehouseMaterial;
import dtri.com.tw.pgsql.entity.WarehouseTypeFilter;
import dtri.com.tw.service.feign.ClientServiceFeign;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.Fm_T;
import dtri.com.tw.shared.PackageService;
import jakarta.annotation.Resource;

@Service
public class ERPSynchronizeService {

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
	BiosVersionDao biosVersionDao;
	@Autowired
	BiosPrincipalDao biosPrincipalDao;
	@Autowired
	BasicNotificationMailDao notificationMailDao;
	@Autowired
	ScheduleOutsourcerDao scheduleOutsourcerDao;

	@Autowired
	ERPToCloudService erpToCloudService;
	@Autowired
	ERPAutoCheckService erpAutoCheckService;
	@Autowired
	ERPAutoRemoveService autoRemoveService;
	@Autowired
	PackageService packageService;

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

	public void initERPSynchronizeService() throws Exception {
		areaDao.findAll().forEach(x -> {
			if (!wAs.containsKey(x.getWaaliasawmpnb())) {
				wAs.put(x.getWaaliasawmpnb(), x);
			}
		});
		// 單別
		filterDao.findAll().forEach(y -> {
			wTFs.put(y.getWtfcode(), y);
		});
		// 負責人
		List<Order> orders = new ArrayList<>();
		orders.add(new Order(Direction.ASC, "wksuaccount"));// 帳號
		orders.add(new Order(Direction.ASC, "wkglist"));// 區域關鍵

		Map<String, WarehouseKeeper> keepers = new HashMap<>();// 負責人
		PageRequest pageable = PageRequest.of(0, 99999, Sort.by(orders));
		keeperDao.findAllBySearch(null, null, pageable).forEach(z -> {
			if (!z.getWksuaccount().equals("") && !z.getWkglist().equals("") && !z.getWkwaslocation().equals("")) {
				keepers.put(z.getWksuaccount() + "_" + z.getWkglist() + "_" + z.getWkwaslocation(), z);
			}
		});
		wKs.putAll(keepers);

		// 倉別
		configDao.findAll().forEach(w -> {
			wCs.put(w.getWcalias(), w);
		});
		// 物料號
		materialDao.findAll().forEach(m -> {
			wMs.put(m.getWmpnb(), m);
		});

		wAsSave = new HashMap<>();// 自動更新清單
	}

	// ============ A511 廠內製令單/A512 委外製令單/A521 廠內重工單/A522 委外領料單 ============
	public void erpSynchronizeMocta() throws Exception {
		logger.info("=== erpSynchronizeMocta: 時間:{}", dateFormat.format(new Date()));
		ArrayList<Mocta> moctas = moctaDao.findAllByMocta();
		Map<String, Mocta> erpMaps = new HashMap<>();
		Map<String, BasicCommandList> commandMaps = new HashMap<>();// BIOS檢查
		ArrayList<BasicCommandList> commandLists = new ArrayList<BasicCommandList>();
		ArrayList<BasicCommandList> removeCommandLists = new ArrayList<BasicCommandList>();// [Cloud]儲存(移除)
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
			m.setTa001_ta002(m.getTa001_ta002().replaceAll("\\s", ""));
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
			String oKey = o.getBclclass() + "-" + o.getBclsn() + "-" + o.getBclpnumber();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpMaps.containsKey(oKey)) {
				String nChecksum = erpMaps.get(oKey).toString().replaceAll("\\s", "");
				erpMaps.get(oKey).setNewone(false);// 標記:不是新的
				// 內容不同=>更新
				if (o.getBclfuser().equals("ERP_Remove(Auto)") || //
						(!o.getChecksum().equals(nChecksum)
								&& (o.getBclfuser().equals("") || o.getBclfuser().indexOf("System") >= 0))) {
					Mocta m = erpMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 資料轉換
					erpToCloudService.commandOne(o, m, checkSum);
					commandLists.add(o);
				}
			} else {
				// 匹配不到->已結案/移除項目->移除
				removeCommandLists.add(o);// 移除資料;
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
		// Step4. mail BIOS 通知?
		// bios
		Map<String, BiosVersion> biosVersionMaps = new HashMap<>();// bios配對?
		biosVersionDao.findAll().forEach(b -> {
			// (機種別_客戶)
			String biosKey = b.getBvmodel() + ")_(" + b.getBvcname();
			// 新 (製令單 與 BIOS)配對上 && 沒登記過
			if (commandMaps.containsKey(biosKey) && !biosVersionMaps.containsKey(biosKey)) {
				biosVersionMaps.put(biosKey, b);
			}
		});

		// BOIS->配對人->寄件登記
		ArrayList<BasicNotificationMail> readyNeedMails = new ArrayList<BasicNotificationMail>();
		biosVersionMaps.forEach((k, v) -> {
			// 如果有客戶
			if (k.split("\\)_\\(").length == 2) {
				// 機種別
				String modelName = k.split("\\)_\\(")[0];// 機種別
				String modelCustomized = k.split("\\)_\\(")[1];// 客戶
				String version = v.getBvversion();// 目前版本

				ArrayList<BiosPrincipal> principals = biosPrincipalDao.findAllBySearch(modelName);
				// 寄信件對象
				ArrayList<String> mainUsers = new ArrayList<String>();
				ArrayList<String> secondaryUsers = new ArrayList<String>();
				principals.forEach(u -> {
					// 主要?次要?+制令單通知
					if (u.getBpprimary() == 0 && u.getBponotice()) {
						mainUsers.add(u.getBpsumail());
					} else if (u.getBpprimary() == 1 && u.getBponotice()) {
						secondaryUsers.add(u.getBpsumail());
					}
				});
				// 建立信件
				BasicNotificationMail readyNeedMail = new BasicNotificationMail();
				readyNeedMail.setBnmtitle("[" + Fm_T.to_y_M_d(new Date()) + "][" + modelName + "][" + version + "]["
						+ modelCustomized + "]"//
						+ "Cloud system BIOS recommended update notification!");
				readyNeedMail.setBnmcontent("Please check the model :[" + modelName + "] BIOS needs to be updated,\n"//
						+ "customized version/customer is :[" + version + " / " + modelCustomized + "]");
				readyNeedMail.setBnmkind("BIOS");
				readyNeedMail.setBnmmail(mainUsers + "");
				readyNeedMail.setBnmmailcc(secondaryUsers + "");
				// 檢查信件(避免重複)
				if (notificationMailDao.findAllByCheck(null, null, null, readyNeedMail.getBnmtitle(), null, null, null)
						.size() == 0) {
					readyNeedMails.add(readyNeedMail);
				}
			}
		});

		// Step4. 存入資料
		commandListDao.saveAll(commandLists);//
		commandListDao.deleteAll(removeCommandLists);
		// notificationMailDao.saveAll(readyNeedMails);

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
		ArrayList<BasicIncomingList> entityOlds = incomingListDao.findAllByStatus(0, bilclass);// [Cloud]資料
		ArrayList<BasicIncomingList> saveLists = new ArrayList<BasicIncomingList>();// [Cloud]儲存
		ArrayList<BasicIncomingList> removeInLists = new ArrayList<BasicIncomingList>();// [Cloud]儲存(移除)
		// Step1.資料整理
		for (Purth m : erpInEntitys) {
			String nKey = m.getTh001_th002() + "-" + m.getTh003();
			nKey = nKey.replaceAll("\\s", "");
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
			m.setTh001_th002(m.getTh001_th002().replaceAll("\\s", ""));
			m.setNewone(true);
			erpInMaps.put(nKey, m);
			wTFsSave.put(m.getTh001_th002().split("-")[0], 0);
		}
		// Step2.[ERP vs Cloud]舊資料匹配
		// 入料
		entityOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+單項目號)
			String oKey = o.getBilclass() + "-" + o.getBilsn() + "-" + o.getBilnb();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpInMaps.containsKey(oKey)) {
				String nChecksum = erpInMaps.get(oKey).toString().replaceAll("\\s", "");
				erpInMaps.get(oKey).setNewone(false);// 標記:不是新的
				// 內容不同=>更新
				if (o.getBilfuser().equals("ERP_Remove(Auto)") || //
						(!o.getChecksum().equals(nChecksum)
								&& (o.getBilfuser().equals("") || o.getBilfuser().indexOf("System") >= 0))) {
					Purth m = erpInMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(入)
					if (o.getBilfuser().indexOf("System") >= 0) {
						erpAutoCheckService.incomingAutoRe(o, wAsSave, wTFs, wCs, wMs);
					}
					// 資料轉換
					o = erpToCloudService.incomingOnePurth(o, m, checkSum, wTFs, wKs, wAs);
					// 自動完成
					o = erpAutoCheckService.incomingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
					saveLists.add(o);
				}
			} else if (Fm_T.to_diff(new Date(), o.getSyscdate()) < 30 && o.getBilfuser().equals("") && //
					(o.getBilclass().equals("A341") || o.getBilclass().equals("A342") || o.getBilclass().equals("A343")
							|| o.getBilclass().equals("A344") || o.getBilclass().equals("A345"))) {
				// 距今日(30天內) /A341 國內進貨單/ A342 國外進貨單/ A343 台北進貨單/A344 模具進貨單/ A345 無採購進貨單
				o = autoRemoveService.incomingAuto(o);
				removeInLists.add(o);// 標記:無此資料
			}
		});
		// Step3.[ERP vs Cloud]全新資料?
		// 入料
		erpInMaps.forEach((key, v) -> {
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

		// Step5. 存入資料
		incomingListDao.saveAll(saveLists);
		incomingListDao.saveAll(removeInLists);

		// Step6. 自動結算
		erpAutoCheckService.settlementAuto(wAsSave);
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
		ArrayList<BasicIncomingList> entityInOlds = incomingListDao.findAllByStatus(0, bilclass);// 取得[Cloud]
		ArrayList<BasicShippingList> entityShOlds = shippingListDao.findAllByStatus(0, bilclass);// 取得[Cloud]
		// 存入資料物件
		ArrayList<BasicIncomingList> saveInLists = new ArrayList<BasicIncomingList>();// [Cloud]儲存
		ArrayList<BasicShippingList> saveShLists = new ArrayList<BasicShippingList>();// [Cloud]儲存
		ArrayList<BasicIncomingList> removeInLists = new ArrayList<BasicIncomingList>();// [Cloud]儲存(移除)
		ArrayList<BasicShippingList> removeShLists = new ArrayList<BasicShippingList>();// [Cloud]儲存(移除)

		// Step1.資料整理
		for (Mocte m : erpEntitys) {
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
			m.setTa026_ta027_ta028(m.getTa026_ta027_ta028().replaceAll("\\s", ""));
			m.setTa001_ta002(m.getTa001_ta002() == null ? "" : m.getTa001_ta002().replaceAll("\\s", ""));
			String nKey = m.getTa026_ta027_ta028();
			// 測試用
//			if(nKey.contains("A542-240314004")) {
//				System.out.println("A542-240314004");
//			}

			m.setNewone(true);
			// 單別性質(退料類 需抓取 物料領退用量)
			String classNb = m.getTa026_ta027_ta028().split("-")[0];
			if (classNb.equals("A543") || classNb.equals("A561") || classNb.equals("A571")) {
				m.setTb004(m.getTe005());
			}
			// 單據性質別54.廠內領料,55.託外領料,56.廠內退料,57.託外退料
			if (m.getTc008().equals("54") || m.getTc008().equals("55")) {
				m.setTk000("領料類");
				if (nKey.indexOf("A542-240529007") >= 0) {
					System.out.println(nKey);
				}
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
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpInMaps.containsKey(oKey)) {
				String nChecksum = erpInMaps.get(oKey).toString().replaceAll("\\s", "");
				erpInMaps.get(oKey).setNewone(false);// 標記:不是新的
				// 內容不同=>更新
				if (o.getBilfuser().equals("ERP_Remove(Auto)") || !o.getChecksum().equals(nChecksum)) {
					Mocte m = erpInMaps.get(oKey);
					// 尚未領料 or 系統標記 可修改
					if (o.getBilfuser().equals("ERP_Remove(Auto)") //
							|| o.getBilfuser().equals("")//
							|| o.getBilfuser().indexOf("System") >= 0) {
						String checkSum = m.toString().replaceAll("\\s", "");
						// 自動恢復(入)
						if (o.getBilfuser().indexOf("System") >= 0) {
							erpAutoCheckService.incomingAutoRe(o, wAsSave, wTFs, wCs, wMs);
						}
						// 資料轉換
						o = erpToCloudService.incomingOneMocte(o, m, checkSum, wTFs, wKs, wAs);
						// 自動完成
						o = erpAutoCheckService.incomingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
						saveInLists.add(o);
					} else if (!o.getBilfuser().equals("") && !o.getBilfuser().contains("✪") && //
							(!o.getBilpnqty().equals(m.getTe005()) || !o.getBilpnumber().equals(m.getMb001()))) {
						// 標記二次修正(數量不同+料號不同)
						o.setBilfuser("✪ " + o.getBilfuser());
					}
				}
			} else if (Fm_T.to_diff(new Date(), o.getSyscdate()) < 30 && o.getBilfuser().equals("") && //
					(o.getBilclass().equals("A561") || o.getBilclass().equals("A571"))) {
				// 同一天 /A561 廠內退料單/ A571 委外退料單
				o = autoRemoveService.incomingAuto(o);
				removeInLists.add(o);// 標記:無此資料
			}
		});
		// 領料
		entityShOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+單項目號)
			String oKey = o.getBslclass() + "-" + o.getBslsn() + "-" + o.getBslnb();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (oKey.indexOf("A542-240529007") >= 0) {
				System.out.println(oKey);
			}
			if (erpShMaps.containsKey(oKey)) {
				// A541-240229002
				String nChecksum = erpShMaps.get(oKey).toString().replaceAll("\\s", "");
				erpShMaps.get(oKey).setNewone(false);// 標記:不是新的

				// 內容不同=>更新
				if (o.getBslfuser().equals("ERP_Remove(Auto)") || !o.getChecksum().equals(nChecksum)) {
					Mocte m = erpShMaps.get(oKey);
					// 尚未領料 or 系統標記 可修改
					if (o.getBslfuser().equals("ERP_Remove(Auto)") //
							|| o.getBslfuser().equals("")//
							|| o.getBslfuser().indexOf("System") >= 0) {
						String checkSum = m.toString().replaceAll("\\s", "");
						// 自動恢復(領)
						if (o.getBslfuser().indexOf("System") >= 0) {
							erpAutoCheckService.shippingAutoRe(o, wAsSave, wTFs, wCs, wMs);
						}
						// 資料轉換
						o = erpToCloudService.shippingOneMocte(o, m, checkSum, wTFs, wKs, wAs);
						// 自動完成
						o = erpAutoCheckService.shippingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
						saveShLists.add(o);

					} else if (!o.getBslfuser().equals("") && !o.getBslfuser().contains("✪") && //
							(!o.getBslpnqty().equals(m.getTe005()) || !o.getBslpnumber().equals(m.getMb001()))) {
						// 標記二次修正(數量不同+料號不同)
						o.setBslfuser("✪ " + o.getBslfuser());
					}

				}
			} else if (Fm_T.to_diff(new Date(), o.getSyscdate()) < 30 && o.getBslfuser().equals("") && //
					(o.getBslclass().equals("A541") || o.getBslclass().equals("A542") || //
							o.getBslclass().equals("A543") || o.getBslclass().equals("A551"))) {
				// 30天/尚未領料 / A541 廠內領料單/ A542 補料單/ A551 委外領料單
				o = autoRemoveService.shippingAuto(o);
				removeShLists.add(o);// 標記:無此資料
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
			// 測試用
//			if(key.indexOf("A541-231122019")>=0) {
//				System.out.println(key);
//			}
			if (v.isNewone() && v.getTk000().equals("領料類") && v.getTe019().equals("N")) {
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
		// Step4. 存入資料
		incomingListDao.saveAll(saveInLists);
		shippingListDao.saveAll(saveShLists);
		incomingListDao.saveAll(removeInLists);
		shippingListDao.saveAll(removeShLists);
		// Step5. 自動結算
		erpAutoCheckService.settlementAuto(wAsSave);
	}

	// ============A581 生產入庫單 ============
	public void erpSynchronizeMoctf() throws Exception {
		logger.info("===erpSynchronizeMoctf: 時間:{}", dateFormat.format(new Date()));
		// Step0.資料準備
		ArrayList<Moctf> erpEntitys = moctfDao.findAllByMoctf();
		Map<String, Moctf> erpInMaps = new HashMap<>();
		List<String> bilclass = new ArrayList<String>();
		bilclass.add("A581");

		ArrayList<BasicIncomingList> entityOlds = incomingListDao.findAllByStatus(0, bilclass);// 取得[Cloud]
		ArrayList<BasicIncomingList> saveLists = new ArrayList<BasicIncomingList>();// [Cloud]儲存
		ArrayList<BasicIncomingList> removeInLists = new ArrayList<BasicIncomingList>();// [Cloud]儲存(移除)

		// Step1.資料整理
		for (Moctf m : erpEntitys) {
			String nKey = m.getTg001_tg002_tg003();
			m.setTg001_tg002_tg003(m.getTg001_tg002_tg003().replaceAll("\\s", ""));
			m.setTg014_tg015(m.getTg014_tg015().replaceAll("\\s", ""));
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
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
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpInMaps.containsKey(oKey)) {
				String nChecksum = erpInMaps.get(oKey).toString().replaceAll("\\s", "");
				erpInMaps.get(oKey).setNewone(false);// 標記:不是新的
				// 內容不同=>更新
				if (o.getBilfuser().equals("ERP_Remove(Auto)") || (!o.getChecksum().equals(nChecksum)
						&& (o.getBilfuser().equals("") || o.getBilfuser().indexOf("System") >= 0))) {
					Moctf m = erpInMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(入)
					if (o.getBilfuser().indexOf("System") >= 0) {
						erpAutoCheckService.incomingAutoRe(o, wAsSave, wTFs, wCs, wMs);
					}
					// 資料轉換
					o = erpToCloudService.incomingOneMoctf(o, m, checkSum, wTFs, wKs, wAs);
					// 自動完成
					o = erpAutoCheckService.incomingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
					saveLists.add(o);
				}
			} else if (Fm_T.to_diff(new Date(), o.getSyscdate()) < 30 && o.getBilfuser().equals("") && //
					(o.getBilclass().equals("A581"))) {
				// A581 生產入庫單
				o = autoRemoveService.incomingAuto(o);
				removeInLists.add(o);// 標記:無此資料
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

		// Step4. 存入資料
		incomingListDao.saveAll(saveLists);
		incomingListDao.saveAll(removeInLists);
		// Step5. 自動結算
		erpAutoCheckService.settlementAuto(wAsSave);
	}

	// ============ A591 委外進貨單 ============
	public void erpSynchronizeMocth() throws Exception {
		logger.info("===erpSynchronizeMocth: 時間:{}", dateFormat.format(new Date()));
		// Step0.資料準備
		ArrayList<Mocth> erpEntitys = mocthDao.findAllByMocth();
		Map<String, Mocth> erpInMaps = new HashMap<>();
		List<String> bilclass = new ArrayList<String>();
		bilclass.add("A591");

		ArrayList<BasicIncomingList> entityOlds = incomingListDao.findAllByStatus(0, bilclass);// 取得[Cloud]
		ArrayList<BasicIncomingList> saveLists = new ArrayList<BasicIncomingList>();// [Cloud]儲存
		ArrayList<BasicIncomingList> removeInLists = new ArrayList<BasicIncomingList>();// [Cloud]儲存(移除)
		// Step1.資料整理
		for (Mocth m : erpEntitys) {
			String nKey = m.getTi001_ti002_ti003();
			nKey = nKey.replaceAll("\\s", "");
			m.setTi001_ti002_ti003(m.getTi001_ti002_ti003().replaceAll("\\s", ""));
			m.setTi013_ti014(m.getTi013_ti014().replaceAll("\\s", ""));
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
			m.setNewone(true);
			erpInMaps.put(nKey, m);
			wTFsSave.put(m.getTi001_ti002_ti003().split("-")[0], 0);
		}
		// Step2.[ERP vs Cloud]舊資料匹配
		// 入料
		entityOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+物料號+單項目號)
			String oKey = o.getBilclass() + "-" + o.getBilsn() + "-" + o.getBilnb();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpInMaps.containsKey(oKey)) {
				String nChecksum = erpInMaps.get(oKey).toString().replaceAll("\\s", "");
				erpInMaps.get(oKey).setNewone(false);// 標記:不是新的
				// 內容不同=>更新
				if (o.getBilfuser().equals("ERP_Remove(Auto)") || //
						(!o.getChecksum().equals(nChecksum)
								&& (o.getBilfuser().equals("") || o.getBilfuser().indexOf("System") >= 0))) {
					Mocth m = erpInMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(入)
					if (o.getBilfuser().indexOf("System") >= 0) {
						erpAutoCheckService.incomingAutoRe(o, wAsSave, wTFs, wCs, wMs);
					}
					// 資料轉換
					o = erpToCloudService.incomingOneMocth(o, m, checkSum, wTFs, wKs, wAs);
					// 自動完成
					o = erpAutoCheckService.incomingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
					saveLists.add(o);
				}
			} else if (Fm_T.to_diff(new Date(), o.getSyscdate()) < 30 && o.getBilfuser().equals("") && //
					(o.getBilclass().equals("A591"))) {
				// A591 委外進貨單
				o = autoRemoveService.incomingAuto(o);
				removeInLists.add(o);// 標記:無此資料
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

		// Step4. 存入資料
		incomingListDao.saveAll(saveLists);
		incomingListDao.saveAll(removeInLists);
		// Step5. 自動結算
		erpAutoCheckService.settlementAuto(wAsSave);
	}

	// ============ A131 庫存借出單/ A141 庫存借入單 ============
	public void erpSynchronizeInvtg() throws Exception {
		logger.info("===erpSynchronizeInvtg: 時間:{}", dateFormat.format(new Date()));
		// Step0.資料準備
		ArrayList<Invtg> erpEntitys = invtgDao.findAllByMocth();
		Map<String, Invtg> erpInMaps = new HashMap<>();
		Map<String, Invtg> erpShMaps = new HashMap<>();
		List<String> bilclass = new ArrayList<String>();
		bilclass.add("A131");
		bilclass.add("A141");
		ArrayList<BasicIncomingList> entityInOlds = incomingListDao.findAllByStatus(0, bilclass);// 取得[Cloud]
		ArrayList<BasicShippingList> entityShOlds = shippingListDao.findAllByStatus(0, bilclass);// 取得[Cloud]
		// 存入資料物件
		ArrayList<BasicIncomingList> saveInLists = new ArrayList<BasicIncomingList>();// [Cloud]儲存
		ArrayList<BasicShippingList> saveShLists = new ArrayList<BasicShippingList>();// [Cloud]儲存
		ArrayList<BasicIncomingList> removeInLists = new ArrayList<BasicIncomingList>();// [Cloud]儲存(移除)
		ArrayList<BasicShippingList> removeShLists = new ArrayList<BasicShippingList>();// [Cloud]儲存(移除)
		// Step1.資料整理
		for (Invtg m : erpEntitys) {
			String nKey = m.getTg001_tg002_tg003();
			m.setTg001_tg002_tg003(m.getTg001_tg002_tg003().replaceAll("\\s", ""));
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
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
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpInMaps.containsKey(oKey)) {
				String nChecksum = erpInMaps.get(oKey).toString().replaceAll("\\s", "");
				erpInMaps.get(oKey).setNewone(false);// 標記:不是新的
				// 內容不同=>更新
				if (o.getBilfuser().equals("ERP_Remove(Auto)") || //
						(!o.getChecksum().equals(nChecksum)
								&& (o.getBilfuser().equals("") || o.getBilfuser().indexOf("System") >= 0))) {
					Invtg m = erpInMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(入)
					if (o.getBilfuser().indexOf("System") >= 0) {
						erpAutoCheckService.incomingAutoRe(o, wAsSave, wTFs, wCs, wMs);
					}
					// 資料轉換
					o = erpToCloudService.incomingOneInvtg(o, m, checkSum, wTFs, wKs, wAs);
					// 自動完成
					o = erpAutoCheckService.incomingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
					saveInLists.add(o);
				}
			} else if (Fm_T.to_diff(new Date(), o.getSyscdate()) < 30 && o.getBilfuser().equals("") && //
					(o.getBilclass().equals("A141"))) {
				// A141 庫存借入單
				o = autoRemoveService.incomingAuto(o);
				removeInLists.add(o);// 標記:無此資料
			}
		});
		// 領料
		entityShOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+物料號+單項目號)
			String oKey = o.getBslclass() + "-" + o.getBslsn() + "-" + o.getBslnb();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpShMaps.containsKey(oKey)) {
				String nChecksum = erpShMaps.get(oKey).toString().replaceAll("\\s", "");
				erpShMaps.get(oKey).setNewone(false);// 標記:不是新的
				// 內容不同=>更新
				if (o.getBslfuser().equals("ERP_Remove(Auto)") || //
						(!o.getChecksum().equals(nChecksum)
								&& (o.getBslfuser().equals("") || o.getBslfuser().indexOf("System") >= 0))) {
					Invtg m = erpShMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(領)
					if (o.getBslfuser().indexOf("System") >= 0) {
						erpAutoCheckService.shippingAutoRe(o, wAsSave, wTFs, wCs, wMs);
					}
					// 資料轉換
					o = erpToCloudService.shippingOneInvtg(o, m, checkSum, wTFs, wKs, wAs);
					// 自動完成
					o = erpAutoCheckService.shippingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
					saveShLists.add(o);
				}
			} else if (Fm_T.to_diff(new Date(), o.getSyscdate()) < 30 && o.getBslfuser().equals("") && //
					(o.getBslclass().equals("A131"))) {
				// A131 庫存借出單
				o = autoRemoveService.shippingAuto(o);
				removeShLists.add(o);// 標記:無此資料
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
			if (v.isNewone() && v.getTk000().equals("領料類") && v.getTg022().equals("N")) {
				BasicShippingList n = new BasicShippingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				// 資料轉換
				erpToCloudService.shippingOneInvtg(n, v, checkSum, wTFs, wKs, wAs);
				// 自動完成
				erpAutoCheckService.shippingAuto(n, wAsSave, wTFs, wCs, wMs, wAs);
				saveShLists.add(n);
			}
		});
		// Step4. 存入資料
		incomingListDao.saveAll(saveInLists);
		shippingListDao.saveAll(saveShLists);
		incomingListDao.saveAll(removeInLists);
		shippingListDao.saveAll(removeShLists);
		// Step5. 自動結算
		erpAutoCheckService.settlementAuto(wAsSave);
	}

	// ============ 借出歸還A151/借入歸還單A161 ============
	public void erpSynchronizeInvth() throws Exception {
		logger.info("===erpSynchronizeInvtg: 時間:{}", dateFormat.format(new Date()));
		// Step0.資料準備
		ArrayList<Invth> erpEntitys = invthDao.findAllByMocth();
		Map<String, Invth> erpInMaps = new HashMap<>();
		Map<String, Invth> erpShMaps = new HashMap<>();
		List<String> bilclass = new ArrayList<String>();
		bilclass.add("A151");
		bilclass.add("A161");
		ArrayList<BasicIncomingList> entityInOlds = incomingListDao.findAllByStatus(0, bilclass);
		ArrayList<BasicShippingList> entityShOlds = shippingListDao.findAllByStatus(0, bilclass);
		// 存入資料物件
		ArrayList<BasicIncomingList> saveInLists = new ArrayList<BasicIncomingList>();
		ArrayList<BasicShippingList> saveShLists = new ArrayList<BasicShippingList>();
		ArrayList<BasicIncomingList> removeInLists = new ArrayList<BasicIncomingList>();// [Cloud]儲存(移除)
		ArrayList<BasicShippingList> removeShLists = new ArrayList<BasicShippingList>();// [Cloud]儲存(移除)
		// Step1.資料整理
		for (Invth m : erpEntitys) {
			String nKey = m.getTi001_ti002_ti003();
			m.setTi001_ti002_ti003(m.getTi001_ti002_ti003().replaceAll("\\s", ""));
			m.setTi014_ti015_ti016(m.getTi014_ti015_ti016().replaceAll("\\s", ""));
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
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
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpInMaps.containsKey(oKey)) {
				String nChecksum = erpInMaps.get(oKey).toString().replaceAll("\\s", "");
				erpInMaps.get(oKey).setNewone(false);// 標記:不是新的
				// 內容不同=>更新
				if (o.getBilfuser().equals("ERP_Remove(Auto)") || //
						(!o.getChecksum().equals(nChecksum)
								&& (o.getBilfuser().equals("") || o.getBilfuser().indexOf("System") >= 0))) {
					Invth m = erpInMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(入)
					if (o.getBilfuser().indexOf("System") >= 0) {
						erpAutoCheckService.incomingAutoRe(o, wAsSave, wTFs, wCs, wMs);
					}
					// 資料轉換
					o = erpToCloudService.incomingOneInvth(o, m, checkSum, wTFs, wKs, wAs);
					// 自動完成
					o = erpAutoCheckService.incomingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
					saveInLists.add(o);
				}
			} else if (Fm_T.to_diff(new Date(), o.getSyscdate()) < 30 && o.getBilfuser().equals("") && //
					(o.getBilclass().equals("A151"))) {
				// 借出歸還A151
				o = autoRemoveService.incomingAuto(o);
				removeInLists.add(o);// 標記:無此資料
			}
		});
		// 領料
		entityShOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+單項目號)
			String oKey = o.getBslclass() + "-" + o.getBslsn() + "-" + o.getBslnb();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpShMaps.containsKey(oKey)) {
				String nChecksum = erpShMaps.get(oKey).toString().replaceAll("\\s", "");
				erpShMaps.get(oKey).setNewone(false);// 標記:不是新的
				// 內容不同=>更新
				if (o.getBslfuser().equals("ERP_Remove(Auto)") || //
						(!o.getChecksum().equals(nChecksum)
								&& (o.getBslfuser().equals("") || o.getBslfuser().indexOf("System") >= 0))) {
					Invth m = erpShMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(領)
					if (o.getBslfuser().indexOf("System") >= 0) {
						erpAutoCheckService.shippingAutoRe(o, wAsSave, wTFs, wCs, wMs);
					}
					// 資料轉換
					o = erpToCloudService.shippingOneInvth(o, m, checkSum, wTFs, wKs, wAs);
					// 自動完成
					o = erpAutoCheckService.shippingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
					saveShLists.add(o);
				}
			} else if (Fm_T.to_diff(new Date(), o.getSyscdate()) < 30 && o.getBslfuser().equals("") && //
					(o.getBslclass().equals("A161"))) {
				// A161 庫存借出單
				o = autoRemoveService.shippingAuto(o);
				removeShLists.add(o);// 標記:無此資料
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
		// Step4. 存入資料
		incomingListDao.saveAll(saveInLists);
		shippingListDao.saveAll(saveShLists);
		incomingListDao.saveAll(removeInLists);
		shippingListDao.saveAll(removeShLists);
		// Step5. 自動結算
		erpAutoCheckService.settlementAuto(wAsSave);
	}

	// ============ A111 費用領料單/ A112 費用退料單/A115/ A119 料號調整單/ A121 倉庫調撥單 ============
	public void erpSynchronizeInvta() throws Exception {
		logger.info("===erpSynchronizeInvtg: 時間:{}", dateFormat.format(new Date()));
		// Step0.資料準備
		ArrayList<Invta> erpEntitys = invtaDao.findAllByMocta();
		Map<String, Invta> erpInMaps = new HashMap<>();
		Map<String, Invta> erpShMaps = new HashMap<>();
		List<String> bilclass = new ArrayList<String>();
		bilclass.add("A111");
		bilclass.add("A112");
		bilclass.add("A115");
		bilclass.add("A119");
		bilclass.add("A121");
		ArrayList<BasicIncomingList> entityInOlds = incomingListDao.findAllByStatus(0, bilclass);
		ArrayList<BasicShippingList> entityShOlds = shippingListDao.findAllByStatus(0, bilclass);
		// 存入資料物件
		ArrayList<BasicIncomingList> saveInLists = new ArrayList<BasicIncomingList>();
		ArrayList<BasicShippingList> saveShLists = new ArrayList<BasicShippingList>();
		ArrayList<BasicIncomingList> removeInLists = new ArrayList<BasicIncomingList>();// [Cloud]儲存(移除)
		ArrayList<BasicShippingList> removeShLists = new ArrayList<BasicShippingList>();// [Cloud]儲存(移除)
		// Step1.資料整理
		for (Invta m : erpEntitys) {
			String nKey = m.getTb001_tb002_tb003();
			m.setTb001_tb002_tb003(m.getTb001_tb002_tb003().replaceAll("\\s", ""));
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
			nKey = nKey.replaceAll("\\s", "");
			m.setNewone(true);
			// 測試用
//			if(nKey.indexOf("A121-231122005-0001")>=0) {
//				System.out.println(nKey);
//			}
			// 單據性質別:借出歸還A151+借入歸還單A161
			if (m.getTb001_tb002_tb003().indexOf("A111") >= 0) {
				m.setTk000("領料類");
				erpShMaps.put(nKey, m);
				wTFsSave.put(m.getTb001_tb002_tb003().split("-")[0], 1);
			} else if (m.getTb001_tb002_tb003().indexOf("A112") >= 0) {
				m.setTk000("入料類");
				m.setTb013(m.getTb012());
				erpInMaps.put(nKey, m);
				wTFsSave.put(m.getTb001_tb002_tb003().split("-")[0], 0);
			} else if (m.getTb001_tb002_tb003().indexOf("A121") >= 0) {
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
			} else if (m.getTb001_tb002_tb003().indexOf("A119") >= 0) {// 料號調整單
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
			}
		}

		// Step2.[ERP vs Cloud]舊資料匹配
		// 進料
		entityInOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+物料號+單項目號)
			String oKey = o.getBilclass() + "-" + o.getBilsn() + "-" + o.getBilnb();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpInMaps.containsKey(oKey)) {
				String nChecksum = erpInMaps.get(oKey).toString().replaceAll("\\s", "");
				erpInMaps.get(oKey).setNewone(false);// 標記:不是新的
				// 內容不同=>更新
				if (o.getBilfuser().equals("ERP_Remove(Auto)") || //
						(!o.getChecksum().equals(nChecksum)
								&& (o.getBilfuser().equals("") || o.getBilfuser().indexOf("System") >= 0))) {
					Invta m = erpInMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(入)
					if (o.getBilfuser().indexOf("System") >= 0) {
						erpAutoCheckService.incomingAutoRe(o, wAsSave, wTFs, wCs, wMs);
					}
					// 資料轉換
					o = erpToCloudService.incomingOneInvta(o, m, checkSum, wTFs, wKs, wAs);
					// 自動完成
					o = erpAutoCheckService.incomingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
					saveInLists.add(o);
				}
			} else if (Fm_T.to_diff(new Date(), o.getSyscdate()) < 30 && o.getBilfuser().equals("") && //
					(o.getBilclass().equals("A111") || //
							o.getBilclass().equals("A112") || //
							o.getBilclass().equals("A119") || //
							o.getBilclass().equals("A121"))) {
				// A111 費用領料單/ A112 費用退料單/ A119 料號調整單/ A121 倉庫調撥單
				o = autoRemoveService.incomingAuto(o);
				removeInLists.add(o);// 標記:無此資料
			}

		});
		// 領料
		entityShOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+物料號+單項目號)
			String oKey = o.getBslclass() + "-" + o.getBslsn() + "-" + o.getBslnb();
			oKey = oKey.replaceAll("\\s", "");

			// 同一筆資料?
			if (erpShMaps.containsKey(oKey)) {
				String nChecksum = erpShMaps.get(oKey).toString().replaceAll("\\s", "");
				erpShMaps.get(oKey).setNewone(false);// 標記:不是新的
				// 內容不同=>更新
				if (o.getBslfuser().equals("ERP_Remove(Auto)") || //
						(!o.getChecksum().equals(nChecksum)
								&& (o.getBslfuser().equals("") || o.getBslfuser().indexOf("System") >= 0))) {
					Invta m = erpShMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(領)
					if (o.getBslfuser().indexOf("System") >= 0) {
						erpAutoCheckService.shippingAutoRe(o, wAsSave, wTFs, wCs, wMs);
					}
					// 資料轉換
					o = erpToCloudService.shippingOneInvta(o, m, checkSum, wTFs, wKs, wAs);
					// 自動完成
					o = erpAutoCheckService.shippingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
					saveShLists.add(o);
				}
			} else if (Fm_T.to_diff(new Date(), o.getSyscdate()) < 30 && o.getBslfuser().equals("") && //
					(o.getBslclass().equals("A111") || //
							o.getBslclass().equals("A112") || //
							o.getBslclass().equals("A119") || //
							o.getBslclass().equals("A121"))) {
				// A111 費用領料單/ A112 費用退料單/ A119 料號調整單/ A121 倉庫調撥單
				o = autoRemoveService.shippingAuto(o);
				removeShLists.add(o);// 標記:無此資料
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
		// Step4. 存入資料
		incomingListDao.saveAll(saveInLists);
		shippingListDao.saveAll(saveShLists);
		incomingListDao.saveAll(removeInLists);
		shippingListDao.saveAll(removeShLists);
		// Step5. 自動結算
		erpAutoCheckService.settlementAuto(wAsSave);
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
		ArrayList<BasicIncomingList> entityInOlds = incomingListDao.findAllByStatus(0, bilclass);// 取得[Cloud]
		ArrayList<BasicShippingList> entityShOlds = shippingListDao.findAllByStatus(0, bilclass);// 取得[Cloud]
		// 存入資料物件
		ArrayList<BasicIncomingList> saveInLists = new ArrayList<BasicIncomingList>();// [Cloud]儲存
		ArrayList<BasicShippingList> saveShLists = new ArrayList<BasicShippingList>();// [Cloud]儲存
		ArrayList<BasicIncomingList> removeInLists = new ArrayList<BasicIncomingList>();// [Cloud]儲存(移除)
		ArrayList<BasicShippingList> removeShLists = new ArrayList<BasicShippingList>();// [Cloud]儲存(移除)
		// Step1.資料整理
		String nKeyCheckSame = "";// 同一張單?
		for (Bomtd m : erpEntitys) {
			String nKey = m.getTe001_te002_te003();
			m.setTe001_te002_te003(m.getTe001_te002_te003().replaceAll("\\s", ""));
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
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
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpInMaps.containsKey(oKey)) {
				String nChecksum = erpInMaps.get(oKey).toString().replaceAll("\\s", "");
				erpInMaps.get(oKey).setNewone(false);// 標記:不是新的
				// 內容不同=>更新
				if (o.getBilfuser().equals("ERP_Remove(Auto)") || //
						(!o.getChecksum().equals(nChecksum)
								&& (o.getBilfuser().equals("") || o.getBilfuser().indexOf("System") >= 0))) {
					Bomtd m = erpInMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(入)
					if (o.getBilfuser().indexOf("System") >= 0) {
						erpAutoCheckService.incomingAutoRe(o, wAsSave, wTFs, wCs, wMs);
					}
					// 資料轉換
					o = erpToCloudService.incomingOneBomtd(o, m, checkSum, wTFs, wKs, wAs);
					// 自動完成
					o = erpAutoCheckService.incomingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
					saveInLists.add(o);
				}
			} else if (Fm_T.to_diff(new Date(), o.getSyscdate()) < 30 && o.getBilfuser().equals("") && //
					(o.getBilclass().equals("A421"))) {
				// A421 組合單
				o = autoRemoveService.incomingAuto(o);
				removeInLists.add(o);// 標記:無此資料
			}
		});
		// 領料
		entityShOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+物料號+單項目號)
			String oKey = o.getBslclass() + "-" + o.getBslsn() + "-" + o.getBslnb();
			oKey = oKey.replaceAll("\\s", "");
			// 測試用
//			if (oKey.indexOf("-240507024") >= 0) {
//				System.out.println(oKey);
//			}
			// 同一筆資料?
			if (erpShMaps.containsKey(oKey)) {
				String nChecksum = erpShMaps.get(oKey).toString().replaceAll("\\s", "");
				erpShMaps.get(oKey).setNewone(false);// 標記:不是新的
				// 內容不同=>更新
				if (o.getBslfuser().equals("ERP_Remove(Auto)") || //
						(!o.getChecksum().equals(nChecksum)
								&& (o.getBslfuser().equals("") || o.getBslfuser().indexOf("System") >= 0))) {
					Bomtd m = erpShMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(領)
					if (o.getBslfuser().indexOf("System") >= 0) {
						erpAutoCheckService.shippingAutoRe(o, wAsSave, wTFs, wCs, wMs);
					}
					// 資料轉換
					o = erpToCloudService.shippingOneBomtd(o, m, checkSum, wTFs, wKs, wAs);
					// 自動完成
					o = erpAutoCheckService.shippingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
					saveShLists.add(o);
				}
			} else if (Fm_T.to_diff(new Date(), o.getSyscdate()) < 30 && o.getBslfuser().equals("") && //
					(o.getBslclass().equals("A421"))) {
				// A421
				o = autoRemoveService.shippingAuto(o);
				removeShLists.add(o);// 標記:無此資料
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
		// Step4. 存入資料
		incomingListDao.saveAll(saveInLists);
		shippingListDao.saveAll(saveShLists);
		incomingListDao.saveAll(removeInLists);
		shippingListDao.saveAll(removeShLists);
		// Step5. 自動結算
		erpAutoCheckService.settlementAuto(wAsSave);
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
		ArrayList<BasicIncomingList> entityInOlds = incomingListDao.findAllByStatus(0, bilclass);// 取得[Cloud]
		ArrayList<BasicShippingList> entityShOlds = shippingListDao.findAllByStatus(0, bilclass);// 取得[Cloud]
		// 存入資料物件
		ArrayList<BasicIncomingList> saveInLists = new ArrayList<BasicIncomingList>();// [Cloud]儲存
		ArrayList<BasicShippingList> saveShLists = new ArrayList<BasicShippingList>();// [Cloud]儲存
		ArrayList<BasicIncomingList> removeInLists = new ArrayList<BasicIncomingList>();// [Cloud]儲存(移除)
		ArrayList<BasicShippingList> removeShLists = new ArrayList<BasicShippingList>();// [Cloud]儲存(移除)
		// Step1.資料整理
		String nKeyCheckSame = "";// 同一張單?
		for (Bomtf m : erpEntitys) {
			String nKey = m.getTg001_tg002_tg003();
			m.setTg001_tg002_tg003(m.getTg001_tg002_tg003().replaceAll("\\s", ""));
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
			nKey = nKey.replaceAll("\\s", "");
			m.setNewone(true);
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
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpInMaps.containsKey(oKey)) {
				String nChecksum = erpInMaps.get(oKey).toString().replaceAll("\\s", "");
				erpInMaps.get(oKey).setNewone(false);// 標記:不是新的
				// 內容不同=>更新
				if (o.getBilfuser().equals("ERP_Remove(Auto)") || //
						(!o.getChecksum().equals(nChecksum)
								&& (o.getBilfuser().equals("") || o.getBilfuser().indexOf("System") >= 0))) {
					Bomtf m = erpInMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(入)
					if (o.getBilfuser().indexOf("System") >= 0) {
						erpAutoCheckService.incomingAutoRe(o, wAsSave, wTFs, wCs, wMs);
					}
					// 資料轉換
					o = erpToCloudService.incomingOneBomtf(o, m, checkSum, wTFs, wKs, wAs);
					// 自動完成
					o = erpAutoCheckService.incomingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
					saveInLists.add(o);
				}
			} else if (Fm_T.to_diff(new Date(), o.getSyscdate()) < 30 && o.getBilfuser().equals("") && //
					(o.getBilclass().equals("A431"))) {
				// A431 拆解
				o = autoRemoveService.incomingAuto(o);
				removeInLists.add(o);// 標記:無此資料
			}
		});
		// 領料
		entityShOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+物料號+單項目號)
			String oKey = o.getBslclass() + "-" + o.getBslsn() + "-" + o.getBslnb();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpShMaps.containsKey(oKey)) {
				String nChecksum = erpShMaps.get(oKey).toString().replaceAll("\\s", "");
				erpShMaps.get(oKey).setNewone(false);// 標記:不是新的
				// 內容不同=>更新
				if (o.getBslfuser().equals("ERP_Remove(Auto)") || //
						(!o.getChecksum().equals(nChecksum)
								&& (o.getBslfuser().equals("") || o.getBslfuser().indexOf("System") >= 0))) {
					Bomtf m = erpShMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(領)
					if (o.getBslfuser().indexOf("System") >= 0) {
						erpAutoCheckService.shippingAutoRe(o, wAsSave, wTFs, wCs, wMs);
					}
					// 資料轉換
					o = erpToCloudService.shippingOneBomtf(o, m, checkSum, wTFs, wKs, wAs);
					// 自動完成
					o = erpAutoCheckService.shippingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
					saveShLists.add(o);
				}
			} else if (Fm_T.to_diff(new Date(), o.getSyscdate()) < 30 && o.getBslfuser().equals("") && //
					(o.getBslclass().equals("A431"))) {
				// A431
				o = autoRemoveService.shippingAuto(o);
				removeShLists.add(o);// 標記:無此資料
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
				// 如果已經是完成(則不寫入)
				if (!n.getSysstatus().equals(1)) {
					// 自動完成
					erpAutoCheckService.incomingAuto(n, wAsSave, wTFs, wCs, wMs, wAs);
					saveInLists.add(n);
				}
			}
		});
		// 領料
		erpShMaps.forEach((key, v) -> {
			if (v.isNewone() && v.getTk000().equals("領料類")) {
				BasicShippingList n = new BasicShippingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				// 資料轉換
				erpToCloudService.shippingOneBomtf(n, v, checkSum, wTFs, wKs, wAs);
				// 如果已經是完成(則不寫入)
				if (!n.getSysstatus().equals(1)) {
					// 自動完成
					erpAutoCheckService.shippingAuto(n, wAsSave, wTFs, wCs, wMs, wAs);
					saveShLists.add(n);
				}
			}
		});
		// Step4. 存入資料
		incomingListDao.saveAll(saveInLists);
		shippingListDao.saveAll(saveShLists);
		incomingListDao.saveAll(removeInLists);
		shippingListDao.saveAll(removeShLists);
		// Step5. 自動結算
		erpAutoCheckService.settlementAuto(wAsSave);
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
		ArrayList<BasicShippingList> entityShOlds = shippingListDao.findAllByStatus(0, bilclass);// 取得[Cloud]
		// 存入資料物件
		ArrayList<BasicShippingList> saveShLists = new ArrayList<BasicShippingList>();// [Cloud]儲存
		ArrayList<BasicShippingList> removeShLists = new ArrayList<BasicShippingList>();// [Cloud]儲存(移除)

		// Step1.資料整理
		for (Copth m : erpEntitys) {
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
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
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpShMaps.containsKey(oKey)) {
				String nChecksum = erpShMaps.get(oKey).toString().replaceAll("\\s", "");
				erpShMaps.get(oKey).setNewone(false);// 標記:不是新的
				// 內容不同=>更新
				if (o.getBslfuser().equals("ERP_Remove(Auto)") || //
						(!o.getChecksum().equals(nChecksum)
								&& (o.getBslfuser().equals("") || o.getBslfuser().indexOf("System") >= 0))) {
					Copth m = erpShMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(領)
					if (o.getBslfuser().indexOf("System") >= 0) {
						erpAutoCheckService.shippingAutoRe(o, wAsSave, wTFs, wCs, wMs);
					}
					// 資料轉換
					o = erpToCloudService.shippingOneCopth(o, m, checkSum, wTFs, wKs, wAs);
					// 自動完成
					o = erpAutoCheckService.shippingAuto(o, wAsSave, wTFs, wCs, wMs, wAs);
					saveShLists.add(o);
				}
			} else if (Fm_T.to_diff(new Date(), o.getSyscdate()) < 30 && o.getBslfuser().equals("") && //
					(o.getBslclass().equals("A231") || o.getBslclass().equals("A232"))) {
				// 30天/尚未領料 / A231/A232 銷貨單
				o = autoRemoveService.shippingAuto(o);
				removeShLists.add(o);// 標記:無此資料
			}
		});
		// Step3.[ERP vs Cloud] 全新資料?
		// 領料
		erpShMaps.forEach((key, v) -> {
			// 測試用
//				if(key.indexOf("A541-231122019")>=0) {
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
		// Step4. 存入資料
		shippingListDao.saveAll(saveShLists);
		shippingListDao.saveAll(removeShLists);
		// Step5. 自動結算
		erpAutoCheckService.settlementAuto(wAsSave);
	}

	// ============ 物料+儲位同步 ============
	public void erpSynchronizeInvtb() throws Exception {
		logger.info("===erpSynchronizeInvtb: 時間:{}", dateFormat.format(new Date()));
		// Step0.資料準備
		// 取得[頂新] 有效的物料....
		ArrayList<Invtb> erpEntitys = invtbDao.findAllByMoctb();
		Map<String, Invtb> erpListMaps = new HashMap<>();
		Map<String, Invtb> erpItemMaps = new HashMap<>();
		Map<String, String> erpConfigMaps = new HashMap<>();// A1000+原物料倉
		String checkSame = "";
		for (Invtb m : erpEntitys) {
			// 測試用
//			if(m.getMb001().replaceAll("\\s", "").equals("81-105-38210G") && m.getMb017().equals("A0041")) {
//				System.out.println(m.getMb001());
//			}
			// 物料號+倉別號+位置
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
			m.setMb002(m.getMb002().replaceAll("\\s", ""));
			m.setMb003(m.getMb003().replaceAll("\\s", ""));
			m.setNewone(true);
			// ERP 倉別異常Null
			if (m.getMc002() == null) {
				m.setMc002(m.getMb017());// --倉別代號
				m.setCmc002(erpConfigMaps.get(m.getMb017()));// --倉別名稱
				m.setMc003("FF-FF-FF-FF");// --儲位
				m.setMc007(0);// 數量
			} else {
				m.setMc002(m.getMc002().replaceAll("\\s", ""));// --倉別代號
			}

			// list(物料清單)-物料
			if (!checkSame.equals(m.getMb001())) {
				checkSame = m.getMb001();
				erpListMaps.put(m.getMb001(), m);
			}

			// item(區域清單)-物料儲位
			String nKey = m.getMc002() + "_" + m.getMb001();
			nKey = nKey.replaceAll("\\s", "");
			try {
				Invtb cloneM;
				cloneM = (Invtb) m.clone();
				erpItemMaps.put(nKey, cloneM);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}

			// config(倉別清單)-儲位
			if (m.getMc002() != null && !m.getMc002().equals("")) {
				erpConfigMaps.put(m.getMc002(), m.getCmc002());
			}
		}

		// Step1. 取得[Cloud] 有效 物料+區域+倉儲設定
		List<WarehouseMaterial> listOlds = materialDao.findAll();
		List<WarehouseArea> areaOlds = areaDao.findAll();
		List<WarehouseConfig> configOlds = configDao.findAll();
		// 存入資料物件
		ArrayList<WarehouseMaterial> saveLists = new ArrayList<WarehouseMaterial>();
		ArrayList<WarehouseArea> saveItems = new ArrayList<WarehouseArea>();

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
					o.setChecksum(checkSum);
					saveLists.add(o);
				}
			}
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
				saveLists.add(n);
			}
		});
		materialDao.saveAll(saveLists);

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
						incomingListDao.findAllBySearch(null, null, areaOld.getWawmpnb(), null).forEach(in -> {
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
						shippingListDao.findAllBySearch(null, null, areaOld.getWawmpnb(), null).forEach(sh -> {
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
					n.setWatqty(v.getMc007());// (實際)倉儲數量
					saveItems.add(n);
				}
			}
		});
		areaDao.saveAll(saveItems);
		incomingListDao.saveAll(incomingLists);
		shippingListDao.saveAll(shippingLists);
		// Step5 儲位設定 全新資料?
		erpSynchronizeWconfig(erpConfigMaps, configOlds);

		// Step6 去除掉數量為0的儲位
		// List<WarehouseArea> areaRemove = areaDao.findAllByWaerptqty(0);
		// areaDao.deleteAll(areaRemove);
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

	// ============ 單據移除(120天以前資料) ============
	public void remove120DayData() throws Exception {
		Date countD = Fm_T.to_count(-120, new Date());
		incomingListDao.deleteAll(incomingListDao.findAllBySyscdateRemove(countD));
		shippingListDao.deleteAll(shippingListDao.findAllBySyscdateRemove(countD));
		commandListDao.deleteAll(commandListDao.findAllBySyscdateRemove(countD));
	}

	// ============ 同步機種別() ============
	public void erpSynchronizeProductModel() throws Exception {
		ArrayList<Invma> invmas = invmaDao.findAllByInvma();
		ArrayList<BasicProductModel> models = modelDao.findAllBySearch(null, null, null);
		ArrayList<BasicProductModel> newModels = new ArrayList<BasicProductModel>();
		ArrayList<BiosVersion> biosVers = biosVersionDao.findAllBySearch(null, null, null, null, null, null);
		ArrayList<BiosVersion> newBiosVers = new ArrayList<BiosVersion>();

		// 轉換
		Map<String, BasicProductModel> mapBpms = new HashMap<String, BasicProductModel>();
		models.forEach(y -> {
			mapBpms.put(y.getBpmname(), y);
		});
		Map<String, BiosVersion> mapBvms = new HashMap<String, BiosVersion>();
		biosVers.forEach(y -> {
			mapBvms.put(y.getBvmodel(), y);
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
			// Bios
			if (!mapBvms.containsKey(x.getMa003())) {
				BiosVersion biosVer = new BiosVersion();
				biosVer.setBvid(null);
				biosVer.setBvmodel(x.getMa003());
				newBiosVers.add(biosVer);
			}
		});

		modelDao.saveAll(newModels);
		biosVersionDao.saveAll(newBiosVers);
	}

	// 檢查是否有N+1版本過時
	public void biosVersionCheck() throws Exception {
		//
		Map<String, BiosVersion> bversionDef = new HashMap<String, BiosVersion>();
		Map<String, BiosVersion> bversionCust = new HashMap<String, BiosVersion>();
		ArrayList<BasicNotificationMail> readyNeedMails = new ArrayList<BasicNotificationMail>();

		List<Order> orders = new ArrayList<>();
		orders.add(new Order(Direction.ASC, "bvmodel"));// 機種別
		orders.add(new Order(Direction.DESC, "bvaversion"));// 自動版本BIOS版本
		PageRequest pageable = PageRequest.of(0, 999999, Sort.by(orders));
		List<BiosVersion> biosVers = biosVersionDao.findAll(pageable).getContent();

		// Step1. 取得 每個機種別<最大版本>
		biosVers.forEach(b -> {
			String key = b.getBvmodel();
			// 測試用
//			if (key.equals("DT301CY")) {
//				System.out.println(key);
//			}

			// 一般主要
			if (b.getBvcname().equals("")) {
				// 如果沒有重複
				if (!bversionDef.containsKey(key)) {
					bversionDef.put(key, b);
				} else {
					int bDef = bversionDef.get(key).getBvaversion();
					if (bDef < b.getBvaversion()) {
						// 如果有重複?&新的比較大?
						bversionDef.put(key, b);
					}
				}
			} else {
				// 客製化
				key += ")_(" + b.getBvcname();
				// 如果沒有重複
				if (!bversionCust.containsKey(key)) {
					bversionCust.put(key, b);
				} else {
					int bDef = bversionCust.get(key).getBvaversion();
					if (bDef < b.getBvaversion()) {
						// 如果有重複?&新的比較大?
						bversionCust.put(key, b);
					}
				}
			}
		});
		// Step2.檢查那些客製化落後?+匹配對象?
		bversionCust.forEach((k, v) -> {
			String modelName = k.split("\\)_\\(")[0];
			int bvaversionCust = v.getBvaversion();
			Boolean lockCust = v.getBvclock();
			// 有比對到機種別 & 版本比較
			if (bversionDef.containsKey(modelName)) {
				int bvaversionDef = bversionDef.get(modelName).getBvaversion();
				ArrayList<BiosPrincipal> principals = biosPrincipalDao.findAllBySearch(modelName);
				// 相差n+1版本以上+排除鎖定
				if (bvaversionDef - bvaversionCust >= 2 && principals.size() > 0 && !lockCust) {
					String versionDefName = bversionDef.get(modelName).getBvversion();
					String versionCustomizedName = v.getBvversion();

					// 寄信件對象
					ArrayList<String> mainUsers = new ArrayList<String>();
					ArrayList<String> secondaryUsers = new ArrayList<String>();
					principals.forEach(u -> {
						// 主要?次要?+有勾選 版本檢查通知
						if (u.getBpprimary() == 0 && u.getBpmnotice()) {
							mainUsers.add(u.getBpsumail());
						} else if (u.getBpprimary() == 1 && u.getBpmnotice()) {
							secondaryUsers.add(u.getBpsumail());
						}
					});
					// 建立信件
					BasicNotificationMail readyNeedMail = new BasicNotificationMail();
					readyNeedMail.setBnmtitle(
							"[" + Fm_T.to_y_M_d(new Date()) + "][" + modelName + "][" + bvaversionCust + "]"//
									+ "Cloud system BIOS recommended update notification!");
					readyNeedMail
							.setBnmcontent("Please check the model :[" + modelName + "] BIOS needs to be updated,\n"//
									+ "default/customized version is :[" + versionDefName + " / "
									+ versionCustomizedName + "]");
					readyNeedMail.setBnmkind("BIOS");
					readyNeedMail.setBnmmail(mainUsers + "");
					readyNeedMail.setBnmmailcc(secondaryUsers + "");
					// 檢查信件(避免重複)
					if (notificationMailDao
							.findAllByCheck(null, null, null, readyNeedMail.getBnmtitle(), null, null, null)
							.size() == 0) {
						readyNeedMails.add(readyNeedMail);
					}
				}
			}
		});
		// Step3.登記寄信件
		// System.out.println(readyNeedMails);
		notificationMailDao.saveAll(readyNeedMails);
	}

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
//			//測試用
//			if(one.getTa001_ta002().equals("A512-240311001")) {
//				System.out.println(one.getTa001_ta002());
//			}
		}

		// 比對資料?
		scheduleOutsourcers.forEach(o -> {
//			//測試用
//			if(o.getSonb().equals("A512-240311001")) {
//				System.out.println(o.getSonb());
//			}
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

	// 而外執行
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
}