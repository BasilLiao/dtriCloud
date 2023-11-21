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

import dtri.com.tw.mssql.dao.BomtdDao;
import dtri.com.tw.mssql.dao.BomtfDao;
import dtri.com.tw.mssql.dao.InvtaDao;
import dtri.com.tw.mssql.dao.InvtbDao;
import dtri.com.tw.mssql.dao.InvtgDao;
import dtri.com.tw.mssql.dao.InvthDao;
import dtri.com.tw.mssql.dao.MoctaDao;
import dtri.com.tw.mssql.dao.MocteDao;
import dtri.com.tw.mssql.dao.MoctfDao;
import dtri.com.tw.mssql.dao.MocthDao;
import dtri.com.tw.mssql.dao.PurthDao;
import dtri.com.tw.mssql.entity.Bomtd;
import dtri.com.tw.mssql.entity.Bomtf;
import dtri.com.tw.mssql.entity.Invta;
import dtri.com.tw.mssql.entity.Invtb;
import dtri.com.tw.mssql.entity.Invtg;
import dtri.com.tw.mssql.entity.Invth;
import dtri.com.tw.mssql.entity.Mocta;
import dtri.com.tw.mssql.entity.Mocte;
import dtri.com.tw.mssql.entity.Moctf;
import dtri.com.tw.mssql.entity.Mocth;
import dtri.com.tw.mssql.entity.Purth;
import dtri.com.tw.pgsql.dao.BasicCommandListDao;
import dtri.com.tw.pgsql.dao.BasicIncomingListDao;
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
import dtri.com.tw.shared.Fm_T;

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
	MocteDao mocteDao;
	@Autowired
	MoctfDao moctfDao;
	@Autowired
	MocthDao mocthDao;
	@Autowired
	PurthDao purthDao;

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
	ERPToCloudService erpToCloudService;
	@Autowired
	ERPAutoCheckService erpAutoCheckService;
	@Autowired
	ERPAutoRemoveService autoRemoveService;

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

	public void initERPSynchronizeService() {
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
	public void erpSynchronizeMocta() {
		logger.info("=== erpSynchronizeMocta: 時間:{}", dateFormat.format(new Date()));
		ArrayList<Mocta> moctas = moctaDao.findAllByMocta();
		Map<String, Mocta> erpMaps = new HashMap<>();
		int bslnb = 1;
		String Ta001_ta002 = "";
		for (Mocta m : moctas) {
			if (!Ta001_ta002.equals(m.getTa001_ta002())) {
				Ta001_ta002 = m.getTa001_ta002();
				bslnb = 1;
			}
			// 建立序號
			String nKey = m.getTa001_ta002() + "-" + String.format("%04d", bslnb) + "-" + m.getMb001();
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
		ArrayList<BasicCommandList> commandLists = new ArrayList<BasicCommandList>();
		// Step3. 資料整理轉換
		entityOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+單項目號+物料號)
			String oKey = o.getBclclass() + "-" + o.getBclsn() + "-" + o.getBclnb() + "-" + o.getBclpnumber();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpMaps.containsKey(oKey)) {
				String nChecksum = erpMaps.get(oKey).toString().replaceAll("\\s", "");
				erpMaps.get(oKey).setNewone(false);// 標記:不是新的
				// 內容不同=>更新
				if (!o.getChecksum().equals(nChecksum) && (o.getBclfuser().equals("") || o.getBclfuser().equals("ERP_Remove(Auto)"))) {
					Mocta m = erpMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 資料轉換
					erpToCloudService.commandOne(o, m, checkSum);
					commandLists.add(o);
				}
			} else {
				// 匹配不到->已結案
				o.setSysstatus(1);
				o.setSysmdate(new Date());
				commandLists.add(o);
			}
		});
		// 全新資料?
		erpMaps.forEach((key, v) -> {
			if (v.isNewone()) {
				BasicCommandList n = new BasicCommandList();
				String checkSum = v.toString().replaceAll("\\s", "");
				// 資料轉換
				commandLists.add(erpToCloudService.commandOne(n, v, checkSum));
			}
		});
		// Step4. 存入資料
		commandListDao.saveAll(commandLists);//
	}

	// ============ A341 國內進貨單/ A342 國外進貨單/ A343 台北進貨單/ A345 無採購進貨單 ============
	public void erpSynchronizePurth() {
		logger.info("===erpSynchronizePurth: 時間:{}", dateFormat.format(new Date()));
		// Step0.資料準備
		ArrayList<Purth> erpInEntitys = purthDao.findAllByPurth();// [ERP]資料
		Map<String, Purth> erpInMaps = new HashMap<>();// [ERP]資料
		ArrayList<BasicIncomingList> entityOlds = incomingListDao.findAllByStatus(0);// [Cloud]資料
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
				if (!o.getChecksum().equals(nChecksum) && //
						(o.getBilfuser().equals("") || o.getBilfuser().equals("ERP_Remove(Auto)") || o.getBilfuser().indexOf("System") >= 0)) {
					Purth m = erpInMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(入)
					if (o.getBilfuser().indexOf("System") >= 0) {
						erpAutoCheckService.incomingAutoRe(o, wAsSave, wTFs, wCs, wMs);
					}

					// 資料轉換
					o = erpToCloudService.incomingOnePurth(o, m, checkSum, wTFs, wKs, wAs);
					saveLists.add(o);
				}
			} else if (Fm_T.to_y_M_d(o.getSyscdate()).equals(Fm_T.to_y_M_d(new Date())) && //
					(o.getBilclass().equals("A341") || o.getBilclass().equals("A342") || o.getBilclass().equals("A343")
							|| o.getBilclass().equals("A345"))) {
				// 同一天 /A341 國內進貨單/ A342 國外進貨單/ A343 台北進貨單/ A345 無採購進貨單
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
				// 自動扣除
				n = erpAutoCheckService.incomingAuto(n, wAsSave, wTFs, wCs, wMs);
				saveLists.add(n);
			}
		});

		// Step5. 存入資料
		incomingListDao.saveAll(saveLists);
		incomingListDao.saveAll(removeInLists);

		// Step6. 自動結算
		erpAutoCheckService.settlementAuto(wAsSave);
	}

	// ============ A541 廠內領料單/ A542 補料單/(A543 超領單)/ A551 委外領料單/ A561 廠內退料單/ A571
	// 委外退料單
	public void erpSynchronizeMocte() {
		logger.info("===erpSynchronizeMocte: 時間:{}", dateFormat.format(new Date()));
		// Step0.資料準備
		ArrayList<Mocte> erpEntitys = mocteDao.findAllByMocte();
		ArrayList<Mocte> erpoEntitys = mocteDao.findAllByMocteo();
		// 放入內容
		erpoEntitys.forEach(teo -> {
			erpEntitys.add(teo);
		});
		Map<String, Mocte> erpInMaps = new HashMap<>();
		Map<String, Mocte> erpShMaps = new HashMap<>();
		ArrayList<BasicIncomingList> entityInOlds = incomingListDao.findAllByStatus(0);// 取得[Cloud]
		ArrayList<BasicShippingList> entityShOlds = shippingListDao.findAllByStatus(0);// 取得[Cloud]
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
			m.setNewone(true);
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
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpInMaps.containsKey(oKey)) {
				String nChecksum = erpInMaps.get(oKey).toString().replaceAll("\\s", "");
				erpInMaps.get(oKey).setNewone(false);// 標記:不是新的
				// 內容不同=>更新
				if (!o.getChecksum().equals(nChecksum)
						&& (o.getBilfuser().equals("") || o.getBilfuser().equals("ERP_Remove(Auto)") || o.getBilfuser().indexOf("System") >= 0)) {
					Mocte m = erpInMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(入)
					if (o.getBilfuser().indexOf("System") >= 0) {
						erpAutoCheckService.incomingAutoRe(o, wAsSave, wTFs, wCs, wMs);
					}
					// 資料轉換
					o = erpToCloudService.incomingOneMocte(o, m, checkSum, wTFs, wKs, wAs);
					saveInLists.add(o);
				}
			} else if (Fm_T.to_y_M_d(o.getSyscdate()).equals(Fm_T.to_y_M_d(new Date())) && //
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
			if (erpShMaps.containsKey(oKey)) {
				String nChecksum = erpShMaps.get(oKey).toString().replaceAll("\\s", "");
				erpShMaps.get(oKey).setNewone(false);// 標記:不是新的
				// 內容不同=>更新
				if (!o.getChecksum().equals(nChecksum) && //
						(o.getBslfuser().equals("") || o.getBslfuser().equals("ERP_Remove(Auto)") || o.getBslfuser().indexOf("System") >= 0)) {
					Mocte m = erpShMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(領)
					if (o.getBslfuser().indexOf("System") >= 0) {
						erpAutoCheckService.shippingAutoRe(o, wAsSave, wTFs, wCs, wMs);
					}
					// 資料轉換
					o = erpToCloudService.shippingOneMocte(o, m, checkSum, wTFs, wKs, wAs);
					saveShLists.add(o);
				}
			} else if (Fm_T.to_y_M_d(o.getSyscdate()).equals(Fm_T.to_y_M_d(new Date())) && //
					(o.getBslclass().equals("A541") || o.getBslclass().equals("A542") || //
							o.getBslclass().equals("A543") || o.getBslclass().equals("A551"))) {
				// 同一天 / A541 廠內領料單/ A542 補料單/ A551 委外領料單
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
				// 自動扣除
				n = erpAutoCheckService.incomingAuto(n, wAsSave, wTFs, wCs, wMs);
				saveInLists.add(n);
			}
		});
		// 領料
		erpShMaps.forEach((key, v) -> {
			if (v.isNewone() && v.getTk000().equals("領料類") && v.getTe019().equals("N")) {
				BasicShippingList n = new BasicShippingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				n.setChecksum(checkSum);
				// 資料轉換
				n = erpToCloudService.shippingOneMocte(n, v, checkSum, wTFs, wKs, wAs);
				// 自動扣除
				n = erpAutoCheckService.shippingAuto(n, wAsSave, wTFs, wCs, wMs);
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
	public void erpSynchronizeMoctf() {
		logger.info("===erpSynchronizeMoctf: 時間:{}", dateFormat.format(new Date()));
		// Step0.資料準備
		ArrayList<Moctf> erpEntitys = moctfDao.findAllByMoctf();
		Map<String, Moctf> erpInMaps = new HashMap<>();
		ArrayList<BasicIncomingList> entityOlds = incomingListDao.findAllByStatus(0);// 取得[Cloud]
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
				if (!o.getChecksum().equals(nChecksum) && //
						(o.getBilfuser().equals("") || o.getBilfuser().equals("ERP_Remove(Auto)") || o.getBilfuser().indexOf("System") >= 0)) {
					Moctf m = erpInMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(入)
					if (o.getBilfuser().indexOf("System") >= 0) {
						erpAutoCheckService.incomingAutoRe(o, wAsSave, wTFs, wCs, wMs);
					}
					// 資料轉換
					o = erpToCloudService.incomingOneMoctf(o, m, checkSum, wTFs, wKs, wAs);
					saveLists.add(o);
				}
			} else if (Fm_T.to_y_M_d(o.getSyscdate()).equals(Fm_T.to_y_M_d(new Date())) && //
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
				// 自動扣除
				n = erpAutoCheckService.incomingAuto(n, wAsSave, wTFs, wCs, wMs);
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
	public void erpSynchronizeMocth() {
		logger.info("===erpSynchronizeMocth: 時間:{}", dateFormat.format(new Date()));
		// Step0.資料準備
		ArrayList<Mocth> erpEntitys = mocthDao.findAllByMocth();
		Map<String, Mocth> erpInMaps = new HashMap<>();
		ArrayList<BasicIncomingList> entityOlds = incomingListDao.findAllByStatus(0);// 取得[Cloud]
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
				if (!o.getChecksum().equals(nChecksum) && //
						(o.getBilfuser().equals("") || o.getBilfuser().equals("ERP_Remove(Auto)") || o.getBilfuser().indexOf("System") >= 0)) {
					Mocth m = erpInMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(入)
					if (o.getBilfuser().indexOf("System") >= 0) {
						erpAutoCheckService.incomingAutoRe(o, wAsSave, wTFs, wCs, wMs);
					}
					// 資料轉換
					o = erpToCloudService.incomingOneMocth(o, m, checkSum, wTFs, wKs, wAs);
					saveLists.add(o);
				}
			} else if (Fm_T.to_y_M_d(o.getSyscdate()).equals(Fm_T.to_y_M_d(new Date())) && //
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
				// 自動扣除
				n = erpAutoCheckService.incomingAuto(n, wAsSave, wTFs, wCs, wMs);
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
	public void erpSynchronizeInvtg() {
		logger.info("===erpSynchronizeInvtg: 時間:{}", dateFormat.format(new Date()));
		// Step0.資料準備
		ArrayList<Invtg> erpEntitys = invtgDao.findAllByMocth();
		Map<String, Invtg> erpInMaps = new HashMap<>();
		Map<String, Invtg> erpShMaps = new HashMap<>();
		ArrayList<BasicIncomingList> entityInOlds = incomingListDao.findAllByStatus(0);// 取得[Cloud]
		ArrayList<BasicShippingList> entityShOlds = shippingListDao.findAllByStatus(0);// 取得[Cloud]
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
				if (!o.getChecksum().equals(nChecksum) && //
						(o.getBilfuser().equals("") || o.getBilfuser().equals("ERP_Remove(Auto)") || o.getBilfuser().indexOf("System") >= 0)) {
					Invtg m = erpInMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(入)
					if (o.getBilfuser().indexOf("System") >= 0) {
						erpAutoCheckService.incomingAutoRe(o, wAsSave, wTFs, wCs, wMs);
					}
					// 資料轉換
					erpToCloudService.incomingOneInvtg(o, m, checkSum, wTFs, wKs, wAs);
					saveInLists.add(o);
				}
			} else if (Fm_T.to_y_M_d(o.getSyscdate()).equals(Fm_T.to_y_M_d(new Date())) && //
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
				if (!o.getChecksum().equals(nChecksum) && //
						(o.getBslfuser().equals("") || o.getBslfuser().equals("ERP_Remove(Auto)") || o.getBslfuser().indexOf("System") >= 0)) {
					Invtg m = erpShMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(領)
					if (o.getBslfuser().indexOf("System") >= 0) {
						erpAutoCheckService.shippingAutoRe(o, wAsSave, wTFs, wCs, wMs);
					}
					// 資料轉換
					erpToCloudService.shippingOneInvtg(o, m, checkSum, wTFs, wKs, wAs);
					saveShLists.add(o);
				}
			} else if (Fm_T.to_y_M_d(o.getSyscdate()).equals(Fm_T.to_y_M_d(new Date())) && //
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
				erpAutoCheckService.incomingAuto(n, wAsSave, wTFs, wCs, wMs);
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
				erpAutoCheckService.shippingAuto(n, wAsSave, wTFs, wCs, wMs);
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
	public void erpSynchronizeInvth() {
		logger.info("===erpSynchronizeInvtg: 時間:{}", dateFormat.format(new Date()));
		// Step0.資料準備
		ArrayList<Invth> erpEntitys = invthDao.findAllByMocth();
		Map<String, Invth> erpInMaps = new HashMap<>();
		Map<String, Invth> erpShMaps = new HashMap<>();
		ArrayList<BasicIncomingList> entityInOlds = incomingListDao.findAllByStatus(0);
		ArrayList<BasicShippingList> entityShOlds = shippingListDao.findAllByStatus(0);
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
				if (!o.getChecksum().equals(nChecksum) && //
						(o.getBilfuser().equals("") || o.getBilfuser().equals("ERP_Remove(Auto)") || o.getBilfuser().indexOf("System") >= 0)) {
					Invth m = erpInMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(入)
					if (o.getBilfuser().indexOf("System") >= 0) {
						erpAutoCheckService.incomingAutoRe(o, wAsSave, wTFs, wCs, wMs);
					}
					// 資料轉換
					erpToCloudService.incomingOneInvth(o, m, checkSum, wTFs, wKs, wAs);
					saveInLists.add(o);
				}
			} else if (Fm_T.to_y_M_d(o.getSyscdate()).equals(Fm_T.to_y_M_d(new Date())) && //
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
				if (!o.getChecksum().equals(nChecksum) && //
						(o.getBslfuser().equals("") || o.getBslfuser().equals("ERP_Remove(Auto)") || o.getBslfuser().indexOf("System") >= 0)) {
					Invth m = erpShMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(領)
					if (o.getBslfuser().indexOf("System") >= 0) {
						erpAutoCheckService.shippingAutoRe(o, wAsSave, wTFs, wCs, wMs);
					}
					// 資料轉換
					erpToCloudService.shippingOneInvth(o, m, checkSum, wTFs, wKs, wAs);
					saveShLists.add(o);
				}
			} else if (Fm_T.to_y_M_d(o.getSyscdate()).equals(Fm_T.to_y_M_d(new Date())) && //
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
				n = erpAutoCheckService.incomingAuto(n, wAsSave, wTFs, wCs, wMs);
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
				n = erpAutoCheckService.shippingAuto(n, wAsSave, wTFs, wCs, wMs);
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

	// ============ A111 費用領料單/ A112 費用退料單/ A119 料號調整單/ A121 倉庫調撥單 ============
	public void erpSynchronizeInvta() {
		logger.info("===erpSynchronizeInvtg: 時間:{}", dateFormat.format(new Date()));
		// Step0.資料準備
		ArrayList<Invta> erpEntitys = invtaDao.findAllByMocta();
		Map<String, Invta> erpInMaps = new HashMap<>();
		Map<String, Invta> erpShMaps = new HashMap<>();
		ArrayList<BasicIncomingList> entityInOlds = incomingListDao.findAllByStatus(0);
		ArrayList<BasicShippingList> entityShOlds = shippingListDao.findAllByStatus(0);
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
			// 單據性質別:借出歸還A151+借入歸還單A161
			if (m.getTb001_tb002_tb003().indexOf("A111") >= 0) {
				m.setTk000("領料類");
				erpShMaps.put(nKey, m);
				wTFsSave.put(m.getTb001_tb002_tb003().split("-")[0], 1);
			} else if (m.getTb001_tb002_tb003().indexOf("A112") >= 0) {
				m.setTk000("入料類");
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
			} else if (m.getTb001_tb002_tb003().indexOf("A119") >= 0) {
				// 轉
				if (m.getTb007() < 0) {
					// 領
					m.setTk000("領料類");
					m.setTb007(Math.abs(m.getTb007()));
				} else if (m.getTb007() > 0) {
					// 入
					m.setTk000("入料類");
				}
				wTFsSave.put(m.getTb001_tb002_tb003().split("-")[0], 2);
				erpShMaps.put(nKey, m);
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
				if (!o.getChecksum().equals(nChecksum) && //
						(o.getBilfuser().equals("") || o.getBilfuser().equals("ERP_Remove(Auto)") || o.getBilfuser().indexOf("System") >= 0)) {
					Invta m = erpInMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(入)
					if (o.getBilfuser().indexOf("System") >= 0) {
						erpAutoCheckService.incomingAutoRe(o, wAsSave, wTFs, wCs, wMs);
					}
					// 資料轉換
					erpToCloudService.incomingOneInvta(o, m, checkSum, wTFs, wKs, wAs);
					saveInLists.add(o);
				}
			} else if (Fm_T.to_y_M_d(o.getSyscdate()).equals(Fm_T.to_y_M_d(new Date())) && //
					(o.getBilclass().equals("A112") || o.getBilclass().equals("A119") || o.getBilclass().equals("A121"))) {
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
				if (!o.getChecksum().equals(nChecksum) && //
						(o.getBslfuser().equals("") || o.getBslfuser().equals("ERP_Remove(Auto)") || o.getBslfuser().indexOf("System") >= 0)) {
					Invta m = erpShMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(領)
					if (o.getBslfuser().indexOf("System") >= 0) {
						erpAutoCheckService.shippingAutoRe(o, wAsSave, wTFs, wCs, wMs);
					}
					// 資料轉換
					erpToCloudService.shippingOneInvta(o, m, checkSum, wTFs, wKs, wAs);
					saveShLists.add(o);
				}
			} else if (Fm_T.to_y_M_d(o.getSyscdate()).equals(Fm_T.to_y_M_d(new Date())) && //
					(o.getBslclass().equals("A111") || o.getBslclass().equals("A119") || o.getBslclass().equals("A121"))) {
				// A111 費用領料單/ A112 費用退料單/ A119 料號調整單/ A121 倉庫調撥單
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
				erpToCloudService.incomingOneInvta(n, v, checkSum, wTFs, wKs, wAs);
				// 自動完成
				n = erpAutoCheckService.incomingAuto(n, wAsSave, wTFs, wCs, wMs);
				saveInLists.add(n);
			}
		});
		// 領料
		erpShMaps.forEach((key, v) -> {
			if (v.isNewone() && v.getTk000().equals("領料類") && v.getTb018().equals("N")) {
				BasicShippingList n = new BasicShippingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				// 資料轉換
				erpToCloudService.shippingOneInvta(n, v, checkSum, wTFs, wKs, wAs);
				// 自動完成
				n = erpAutoCheckService.shippingAuto(n, wAsSave, wTFs, wCs, wMs);
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
	public void erpSynchronizeBomtd() {
		logger.info("===erpSynchronizeBomtd: 時間:{}", dateFormat.format(new Date()));
		// Step0.資料準備
		ArrayList<Bomtd> erpEntitys = bomtdDao.findAllByBomtd();
		Map<String, Bomtd> erpInMaps = new HashMap<>();
		Map<String, Bomtd> erpShMaps = new HashMap<>();
		ArrayList<BasicIncomingList> entityInOlds = incomingListDao.findAllByStatus(0);// 取得[Cloud]
		ArrayList<BasicShippingList> entityShOlds = shippingListDao.findAllByStatus(0);// 取得[Cloud]
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
				erpShMaps.put(nKey, m);
				wTFsSave.put(m.getTe001_te002_te003().replaceAll("\\s", "").split("-")[0], 2);
			} else {
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
				if (!o.getChecksum().equals(nChecksum) && //
						(o.getBilfuser().equals("") || o.getBilfuser().equals("ERP_Remove(Auto)") || o.getBilfuser().indexOf("System") >= 0)) {
					Bomtd m = erpInMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(入)
					if (o.getBilfuser().indexOf("System") >= 0) {
						erpAutoCheckService.incomingAutoRe(o, wAsSave, wTFs, wCs, wMs);
					}
					// 資料轉換
					erpToCloudService.incomingOneBomtd(o, m, checkSum, wTFs, wKs, wAs);
					saveInLists.add(o);
				}
			} else if (Fm_T.to_y_M_d(o.getSyscdate()).equals(Fm_T.to_y_M_d(new Date())) && //
					(o.getBilclass().equals("A421"))) {
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
				if (!o.getChecksum().equals(nChecksum) && //
						(o.getBslfuser().equals("") || o.getBslfuser().equals("ERP_Remove(Auto)") || o.getBslfuser().indexOf("System") >= 0)) {
					Bomtd m = erpShMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(領)
					if (o.getBslfuser().indexOf("System") >= 0) {
						erpAutoCheckService.shippingAutoRe(o, wAsSave, wTFs, wCs, wMs);
					}
					// 資料轉換
					erpToCloudService.shippingOneBomtd(o, m, checkSum, wTFs, wKs, wAs);
					saveShLists.add(o);
				}
			} else if (Fm_T.to_y_M_d(o.getSyscdate()).equals(Fm_T.to_y_M_d(new Date())) && //
					(o.getBslclass().equals("A421"))) {
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
				n = erpToCloudService.incomingOneBomtd(n, v, checkSum, wTFs, wKs, wAs);
				// 自動完成
				erpAutoCheckService.incomingAuto(n, wAsSave, wTFs, wCs, wMs);
				saveInLists.add(n);
			}
		});
		// 領料
		erpShMaps.forEach((key, v) -> {
			if (v.isNewone() && v.getTk000().equals("領料類")) {
				BasicShippingList n = new BasicShippingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				// 資料轉換
				erpToCloudService.shippingOneBomtd(n, v, checkSum, wTFs, wKs, wAs);
				// 自動完成
				erpAutoCheckService.shippingAuto(n, wAsSave, wTFs, wCs, wMs);
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
	public void erpSynchronizeBomtf() {
		logger.info("===erpSynchronizeBomtf: 時間:{}", dateFormat.format(new Date()));
		// Step0.資料準備
		ArrayList<Bomtf> erpEntitys = bomtfDao.findAllByBomtf();
		Map<String, Bomtf> erpInMaps = new HashMap<>();
		Map<String, Bomtf> erpShMaps = new HashMap<>();
		ArrayList<BasicIncomingList> entityInOlds = incomingListDao.findAllByStatus(0);// 取得[Cloud]
		ArrayList<BasicShippingList> entityShOlds = shippingListDao.findAllByStatus(0);// 取得[Cloud]
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
				if (!o.getChecksum().equals(nChecksum) && //
						(o.getBilfuser().equals("") || o.getBilfuser().equals("ERP_Remove(Auto)") || o.getBilfuser().indexOf("System") >= 0)) {
					Bomtf m = erpInMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(入)
					if (o.getBilfuser().indexOf("System") >= 0) {
						erpAutoCheckService.incomingAutoRe(o, wAsSave, wTFs, wCs, wMs);
					}
					// 資料轉換
					erpToCloudService.incomingOneBomtf(o, m, checkSum, wTFs, wKs, wAs);
					saveInLists.add(o);
				}
			} else if (Fm_T.to_y_M_d(o.getSyscdate()).equals(Fm_T.to_y_M_d(new Date())) && //
					(o.getBilclass().equals("A431"))) {
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
				if (!o.getChecksum().equals(nChecksum) && //
						(o.getBslfuser().equals("") || o.getBslfuser().equals("ERP_Remove(Auto)") || o.getBslfuser().indexOf("System") >= 0)) {
					Bomtf m = erpShMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					// 自動恢復(領)
					if (o.getBslfuser().indexOf("System") >= 0) {
						erpAutoCheckService.shippingAutoRe(o, wAsSave, wTFs, wCs, wMs);
					}
					// 資料轉換
					erpToCloudService.shippingOneBomtf(o, m, checkSum, wTFs, wKs, wAs);
					saveShLists.add(o);
				}
			} else if (Fm_T.to_y_M_d(o.getSyscdate()).equals(Fm_T.to_y_M_d(new Date())) && //
					(o.getBslclass().equals("A421"))) {
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
				n = erpToCloudService.incomingOneBomtf(n, v, checkSum, wTFs, wKs, wAs);
				// 自動完成
				erpAutoCheckService.incomingAuto(n, wAsSave, wTFs, wCs, wMs);
				saveInLists.add(n);
			}
		});
		// 領料
		erpShMaps.forEach((key, v) -> {
			if (v.isNewone() && v.getTk000().equals("領料類")) {
				BasicShippingList n = new BasicShippingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				// 資料轉換
				erpToCloudService.shippingOneBomtf(n, v, checkSum, wTFs, wKs, wAs);
				// 自動完成
				erpAutoCheckService.shippingAuto(n, wAsSave, wTFs, wCs, wMs);
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

	// ============ 物料+儲位同步 ============
	public void erpSynchronizeInvtb() {
		logger.info("===erpSynchronizeInvtb: 時間:{}", dateFormat.format(new Date()));
		// Step0.資料準備
		// 取得[頂新] 有效的物料....
		ArrayList<Invtb> erpEntitys = invtbDao.findAllByMoctb();
		Map<String, Invtb> erpListMaps = new HashMap<>();
		Map<String, Invtb> erpItemMaps = new HashMap<>();
		Map<String, String> erpConfigMaps = new HashMap<>();// A1000+原物料倉
		String checkSame = "";
		for (Invtb m : erpEntitys) {
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
		areaOlds.forEach(a -> {// 區域庫別代號_物料號_
			String aKey = a.getWaaliasawmpnb();
			// 同一筆?
			if (erpItemMaps.containsKey(aKey)) {
				// 測試用
//				if (aKey.equals("A0002_81-105-361134")) {
//					System.out.println(aKey);
//				}
				erpItemMaps.get(aKey).setNewone(false);// 標記:不是新的
				Invtb av = erpItemMaps.get(aKey);
				String checkSum = av.toString().replaceAll("\\s", "");
				if (!checkSum.equals(a.getChecksum())) {
					// 正則表達式:FF-FF-FF-FF
					Boolean checkloc = av.getMc003().matches("[0-9A-Z]{2}-[0-9A-Z]{2}-[0-9A-Z]{2}-[0-9A-Z]{2}");
					a.setWawmpnb(av.getMb001());// 物料號
					a.setWaalias(av.getMc002());// 倉庫別
					a.setWaaliasawmpnb(av.getMc002() + "_" + av.getMb001());// 倉庫別+物料號
					a.setWaslocation(checkloc ? av.getMc003() : a.getWaslocation());// 物料位置
					a.setWaaname(av.getCmc002() == null ? "" : av.getCmc002());// 倉庫名稱
					a.setWaerptqty(av.getMc007());// 倉儲數量
					a.setChecksum(checkSum);
					a.setSysmdate(new Date());
					a.setSysmuser("system");
					saveItems.add(a);
				}
			}
			areaSameMap.put(a.getWaaliasawmpnb(), a);
		});

		// Step4-2. [物料位置] 全新資料?
		erpItemMaps.forEach((key, v) -> {
			// 測試用
			if (key.equals("A0002_81-105-361134")) {
				System.out.println(key);
			}
			if (v.isNewone()) {
				// 可能重複?
				if (areaSameMap.containsKey(v.getMc002() + "_" + v.getMb001())) {
					System.out.println(v.getMc002() + "_" + v.getMb001());
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
		// Step5 儲位設定 全新資料?
		erpSynchronizeWconfig(erpConfigMaps, configOlds);

		// Step6 去除掉數量為0的儲位
		// List<WarehouseArea> areaRemove = areaDao.findAllByWaerptqty(0);
		// areaDao.deleteAll(areaRemove);
	}

	// ============ 儲位過濾設定 ============
	public void erpSynchronizeWconfig(Map<String, String> erpConfigMaps, List<WarehouseConfig> configOlds) {
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
	public void erpSynchronizeWtypeFilter() {
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
}
