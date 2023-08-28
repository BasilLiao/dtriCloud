package dtri.com.tw.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dtri.com.tw.mssql.dao.InvtaDao;
import dtri.com.tw.mssql.dao.InvtbDao;
import dtri.com.tw.mssql.dao.InvtgDao;
import dtri.com.tw.mssql.dao.InvthDao;
import dtri.com.tw.mssql.dao.MoctaDao;
import dtri.com.tw.mssql.dao.MocteDao;
import dtri.com.tw.mssql.dao.MoctfDao;
import dtri.com.tw.mssql.dao.MocthDao;
import dtri.com.tw.mssql.dao.PurthDao;
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
import dtri.com.tw.pgsql.dao.WarehouseMaterialDao;
import dtri.com.tw.pgsql.dao.WarehouseTypeFilterDao;
import dtri.com.tw.pgsql.entity.BasicCommandList;
import dtri.com.tw.pgsql.entity.BasicIncomingList;
import dtri.com.tw.pgsql.entity.BasicShippingList;
import dtri.com.tw.pgsql.entity.WarehouseArea;
import dtri.com.tw.pgsql.entity.WarehouseConfig;
import dtri.com.tw.pgsql.entity.WarehouseMaterial;
import dtri.com.tw.shared.Fm_T;

@Service
public class ERPSynchronizeService {
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

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	/**
	 * KEY: wtf_code單據代號(開頭)Ex:A511 / A521<br>
	 * Value: wtf_type單據類型0=入庫 / 1=出庫 / 2=轉移<br>
	 */
	private Map<String, String> wTypeFilter = new HashMap<>();

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
			m.setNewone(true);
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
			m.setBslnb(String.format("%04d", bslnb));
			m.setTa001_ta002(m.getTa001_ta002().replaceAll("\\s", ""));
			bslnb += 1;
			erpMaps.put(nKey, m);
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
				// System.out.println(nChecksum);
				// System.out.println(o.getChecksum());
				// 內容不同=>更新
				if (!o.getChecksum().equals(nChecksum)) {
					Mocta m = erpMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					o.setChecksum(checkSum);
					o.setBclproduct(m.getTa006());
					o.setBclclass(m.getTa001_ta002().split("-")[0]);// 製令單[別]
					o.setBclsn(m.getTa001_ta002().split("-")[1]);// 製令單[號]
					o.setBcltype(m.getTk000());// 製令單
					o.setBclnb(m.getBslnb());// 序列號
					o.setBclcheckin(1);// 0=未核單 1=已核單
					o.setBclacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
					o.setBclpnumber(m.getMb001());// 物料號品號
					o.setBclpname(m.getMb002());// 品名
					o.setBclpspecification(m.getMb003());// 規格
					o.setBclpnqty(m.getTb004());// 需領用
					o.setBclpnaqty(m.getTb005());// 已領用
					o.setBclfromcommand("[" + m.getTa026_ta027_ta028() + "_訂單" + "]");// 單據指令-來源 訂單
					o.setBcltocommand("[]");// 單據指令-對象
					o.setBcltowho("[]");// 目的對象
					o.setBclfromwho(m.getMb017() + "_" + m.getMc002());// 目的來源-[倉別代號+倉別名稱]
					o.setBcledate(Fm_T.toYMDate(m.getTb015()));// 預計領料日
					o.setSysstatus(0);// 未完成
					o.setSysmdate(new Date());// 日期
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
				n.setChecksum(checkSum);
				n.setBclproduct(v.getTa006());
				n.setBclclass(v.getTa001_ta002().split("-")[0]);// 製令單[別]
				n.setBclsn(v.getTa001_ta002().split("-")[1]);// 製令單[號]
				n.setBcltype(v.getTk000());// 製令單
				n.setBclnb(v.getBslnb());// 序列號
				n.setBclcheckin(1);// 0=未核單 1=已核單
				n.setBclacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
				n.setBclpnumber(v.getMb001());// 物料號品號
				n.setBclpname(v.getMb002());// 品名
				n.setBclpspecification(v.getMb003());// 規格
				n.setBclpnqty(v.getTb004());// 需領用
				n.setBclpnaqty(v.getTb005());// 已領用
				n.setBcltocommand("[]");// 單據指令-對象
				n.setBclfromcommand("[" + v.getTa026_ta027_ta028() + "_訂單" + "]");// 單據指令-來源

				n.setBclfromwho("[" + v.getMb017() + "_" + v.getMc002() + "]");// 目的來源-[倉別代號+倉別名稱]
				n.setBcltowho("[]");// 目的對象-
				n.setBcledate(Fm_T.toYMDate(v.getTb015()));// 預計領料日
				n.setSysstatus(0);// 未完成
				commandLists.add(n);
			}
		});
		// Step4. 存入資料
		commandListDao.saveAll(commandLists);// 330
	}

	// ============ A341 國內進貨單/ A342 國外進貨單/ A343 台北進貨單/ A345 無採購進貨單 ============
	public void erpSynchronizePurth() {
		logger.info("===erpSynchronizePurth: 時間:{}", dateFormat.format(new Date()));
		ArrayList<Purth> erpInEntitys = purthDao.findAllByPurth();
		Map<String, Purth> erpInMaps = new HashMap<>();
		for (Purth m : erpInEntitys) {
			String nKey = m.getTh001_th002() + "-" + m.getTh003() + "-" + m.getMb001();
			nKey = nKey.replaceAll("\\s", "");
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
			m.setTh001_th002(m.getTh001_th002().replaceAll("\\s", ""));
			m.setNewone(true);
			erpInMaps.put(nKey, m);
		}
		// Step2. 取得[Cloud] 有效入料單 資料
		ArrayList<BasicIncomingList> entityOlds = incomingListDao.findAllByStatus(0);
		// 存入資料物件
		ArrayList<BasicIncomingList> saveLists = new ArrayList<BasicIncomingList>();
		// Step3. 資料整理轉換
		entityOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+物料號+單項目號)
			String oKey = o.getBilclass() + "-" + o.getBilsn() + "-" + o.getBilnb() + "-" + o.getBilpnumber();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpInMaps.containsKey(oKey)) {
				String nChecksum = erpInMaps.get(oKey).toString().replaceAll("\\s", "");
				erpInMaps.get(oKey).setNewone(false);// 標記:不是新的
				// System.out.println(nChecksum);
				// System.out.println(o.getChecksum());
				// 內容不同=>更新
				if (!o.getChecksum().equals(nChecksum)) {
					Purth m = erpInMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					o.setChecksum(checkSum);
					o.setBilclass(m.getTh001_th002().split("-")[0].replaceAll("\\s", ""));// 入庫單[別]
					o.setBilsn(m.getTh001_th002().split("-")[1].replaceAll("\\s", ""));// 入庫單[號]
					o.setBilnb(m.getTh003());// 序號
					o.setBiltype(m.getTk000());// 入庫單
					o.setBilcheckin(0);// 0=未核單 1=已核單
					o.setBilacceptance(m.getTh028().equals("1") ? 0 : 1);// 0=未檢驗 1=已檢驗 2=異常
					o.setBilpnumber(m.getMb001().replaceAll("\\s", ""));// 物料號品號
					o.setBilpname(m.getMb002());// 品名
					o.setBilpspecification(m.getMb003());// 規格
					o.setBilpnqty(m.getTh007());// 需入庫量

					o.setBilfromcommand("[" + m.getTh011_th012_th013() + "_採購單" + "]");// 單據來源 [訂單]
					o.setBilfromwho("[" + m.getMb032() + "_" + m.getMa002() + "]");// 目的來源
					o.setBiltowho("[" + m.getMb017() + "_" + m.getMc002() + "]");// 目的對象 [倉別代號+倉別名稱]
					o.setBiledate(new Date());// 預計入料日
					o.setSysstatus(0);// 未完成
					o.setSysmdate(new Date());
					saveLists.add(o);
				}
			}
		});
		// 全新資料?
		erpInMaps.forEach((key, v) -> {
			if (v.isNewone()) {
				BasicIncomingList n = new BasicIncomingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				n.setChecksum(checkSum);
				n.setBilclass(v.getTh001_th002().split("-")[0].replaceAll("\\s", ""));// 製令單[別]
				n.setBilsn(v.getTh001_th002().split("-")[1].replaceAll("\\s", ""));// 製令單[號]
				n.setBilnb(v.getTh003());// 序號
				n.setBiltype(v.getTk000());// 領料單
				n.setBilcheckin(0);// 0=未核單 1=已核單
				n.setBilacceptance(v.getTh028().equals("1") ? 0 : 1);// 0=未檢驗 1=已檢驗 2=異常
				n.setBilpnumber(v.getMb001().replaceAll("\\s", ""));// 物料號品號
				n.setBilpname(v.getMb002());// 品名
				n.setBilpspecification(v.getMb003());// 規格
				n.setBilpnqty(v.getTh007());// 需入量
				n.setBilpnaqty(0);// 提前領用

				n.setBilfromcommand("[" + v.getTh011_th012_th013() + "_採購單" + "]");// 單據來源 採購單
				n.setBilfromwho("[" + v.getMb032() + "_" + v.getMa002() + "]");// 單據對象
				n.setBiltowho("[" + v.getMb017() + "_" + v.getMc002() + "]");// 目的對象 倉別代號+倉別名稱
				n.setBiledate(new Date());// 預計入料日
				n.setSysstatus(0);// 未完成
				saveLists.add(n);
			}
		});
		// Step4. 存入資料
		incomingListDao.saveAll(saveLists);// 1
	}

	// ============ A541 廠內領料單/ A542 補料單/ A551 委外領料單/ A561 廠內退料單/ A571 委外退料單
	public void erpSynchronizeMocte() {
		logger.info("===erpSynchronizeMocte: 時間:{}", dateFormat.format(new Date()));
		ArrayList<Mocte> erpEntitys = mocteDao.findAllByMocte();
		Map<String, Mocte> erpInMaps = new HashMap<>();
		Map<String, Mocte> erpShMaps = new HashMap<>();
		for (Mocte m : erpEntitys) {
			String nKey = m.getTa026_ta027_ta028() + "-" + m.getMb001();
			nKey = nKey.replaceAll("\\s", "");
			m.setNewone(true);
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
			m.setTa026_ta027_ta028(m.getTa026_ta027_ta028().replaceAll("\\s", ""));
			// 單據性質別54.廠內領料,55.託外領料,56.廠內退料,57.託外退料
			if (m.getTc008().equals("54") || m.getTc008().equals("55")) {
				m.setTk000("領料類");
				erpShMaps.put(nKey, m);
			} else {
				m.setTk000("入料類");
				erpInMaps.put(nKey, m);
			}
		}
		// Step2. 取得[Cloud] 有效入料單 資料
		ArrayList<BasicIncomingList> entityInOlds = incomingListDao.findAllByStatus(0);
		ArrayList<BasicShippingList> entityShOlds = shippingListDao.findAllByStatus(0);
		// 存入資料物件
		ArrayList<BasicIncomingList> saveInLists = new ArrayList<BasicIncomingList>();
		ArrayList<BasicShippingList> saveShLists = new ArrayList<BasicShippingList>();
		// Step3. 資料整理轉換
		// 進料
		entityInOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+物料號+單項目號)
			String oKey = o.getBilclass() + "-" + o.getBilsn() + "-" + o.getBilnb() + "-" + o.getBilpnumber();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpInMaps.containsKey(oKey)) {
				String nChecksum = erpInMaps.get(oKey).toString().replaceAll("\\s", "");
				erpInMaps.get(oKey).setNewone(false);// 標記:不是新的
				// System.out.println(nChecksum);
				// System.out.println(o.getChecksum());
				// 內容不同=>更新
				if (!o.getChecksum().equals(nChecksum)) {
					Mocte m = erpInMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					o.setChecksum(checkSum);
					o.setBilclass(m.getTa026_ta027_ta028().split("-")[0].replaceAll("\\s", ""));// 入庫單[別]
					o.setBilsn(m.getTa026_ta027_ta028().split("-")[1].replaceAll("\\s", ""));// 入庫單[號]
					o.setBilnb(m.getTa026_ta027_ta028().split("-")[2].replaceAll("\\s", ""));// 序號
					o.setBiltype(m.getTk000());// 入庫單
					o.setBilcheckin(1);// 0=未核單 1=已核單
					o.setBilacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
					o.setBilpnumber(m.getMb001().replaceAll("\\s", ""));// 物料號品號
					o.setBilpname(m.getMb002());// 品名
					o.setBilpspecification(m.getMb003());// 規格
					o.setBilpnqty(m.getTb004());// 數量

					o.setBilfromcommand("[" + m.getTa001_ta002() + "_製令單" + "]");// 製令單
					o.setBilfromwho("[" + m.getMb032() + "_" + m.getMa002() + "]");
					o.setBiltowho("[" + m.getMb017() + "_" + m.getMc002() + "]");// 倉別代號+倉別名稱
					o.setBiledate(new Date());// 預計入料日
					o.setSysstatus(0);// 未完成
					o.setSysmdate(new Date());
					saveInLists.add(o);
				}
			}
		});
		// 領料
		entityShOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+物料號+單項目號)
			String oKey = o.getBslclass() + "-" + o.getBslsn() + "-" + o.getBslnb() + "-" + o.getBslpnumber();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpShMaps.containsKey(oKey)) {
				String nChecksum = erpShMaps.get(oKey).toString().replaceAll("\\s", "");
				erpShMaps.get(oKey).setNewone(false);// 標記:不是新的
				// System.out.println(nChecksum);
				// System.out.println(o.getChecksum());
				// 內容不同=>更新
				if (!o.getChecksum().equals(nChecksum)) {
					Mocte m = erpShMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					o.setChecksum(checkSum);
					o.setBslclass(m.getTa026_ta027_ta028().split("-")[0].replaceAll("\\s", ""));// 入庫單[別]
					o.setBslsn(m.getTa026_ta027_ta028().split("-")[1].replaceAll("\\s", ""));// 入庫單[號]
					o.setBslnb(m.getTa026_ta027_ta028().split("-")[2].replaceAll("\\s", ""));// 序號
					o.setBsltype(m.getTk000());// 入庫單
					o.setBslcheckin(1);// 0=未核單 1=已核單
					o.setBslacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
					o.setBslpnumber(m.getMb001().replaceAll("\\s", ""));// 物料號品號
					o.setBslpname(m.getMb002());// 品名
					o.setBslpspecification(m.getMb003());// 規格
					o.setBslpnqty(m.getTb004());// 數量

					o.setBslfromcommand("[" + m.getTa001_ta002().replaceAll("\\s", "") + "_製令單" + "]");// 製令單
					o.setBslfromwho("[" + m.getMb017() + "_" + m.getMc002() + "]");// 倉別代號+倉別名稱
					o.setBsltowho("[" + "_生產線" + "]");
					o.setBsledate(new Date());// 預計入料日
					o.setSysstatus(0);// 未完成
					o.setSysmdate(new Date());
					saveShLists.add(o);
				}
			}
		});

		// 全新資料?
		// 入料
		erpInMaps.forEach((key, v) -> {
			if (v.isNewone()) {
				BasicIncomingList n = new BasicIncomingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				n.setChecksum(checkSum);
				n.setBilclass(v.getTa026_ta027_ta028().split("-")[0].replaceAll("\\s", ""));// 入庫單[別]
				n.setBilsn(v.getTa026_ta027_ta028().split("-")[1].replaceAll("\\s", ""));// 入庫單[號]
				n.setBilnb(v.getTa026_ta027_ta028().split("-")[2].replaceAll("\\s", ""));// 序號
				n.setBiltype(v.getTk000());// 領料單
				n.setBilcheckin(1);// 0=未核單 1=已核單
				n.setBilacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
				n.setBilpnumber(v.getMb001().replaceAll("\\s", ""));// 物料號品號
				n.setBilpname(v.getMb002());// 品名
				n.setBilpspecification(v.getMb003());// 規格
				n.setBilpnqty(v.getTb004());// 需入量
				n.setBilpnaqty(0);// 提前領用

				n.setBilfromcommand("[" + v.getTa001_ta002() + "_製令單" + "]");// 製令單
				n.setBilfromwho("[" + "_生產線" + "]");
				n.setBiltowho("[" + v.getMb017() + "_" + v.getMc002() + "]");// 倉別代號+倉別名稱
				n.setBiledate(new Date());// 預計入料日
				n.setSysstatus(0);// 未完成
				saveInLists.add(n);
			}
		});
		// 領料
		erpShMaps.forEach((key, v) -> {
			if (v.isNewone()) {
				BasicShippingList n = new BasicShippingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				n.setChecksum(checkSum);
				n.setBslclass(v.getTa026_ta027_ta028().split("-")[0].replaceAll("\\s", ""));// 入庫單[別]
				n.setBslsn(v.getTa026_ta027_ta028().split("-")[1].replaceAll("\\s", ""));// 入庫單[號]
				n.setBslnb(v.getTa026_ta027_ta028().split("-")[2].replaceAll("\\s", ""));// 序號
				n.setBsltype(v.getTk000());// 領料單
				n.setBslcheckin(1);// 0=未核單 1=已核單
				n.setBslacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
				n.setBslpnumber(v.getMb001().replaceAll("\\s", ""));// 物料號品號
				n.setBslpname(v.getMb002());// 品名
				n.setBslpspecification(v.getMb003());// 規格
				n.setBslpnqty(v.getTb004());// 需入量
				n.setBslpnaqty(0);// 提前領用

				n.setBslfromcommand("[" + v.getTa001_ta002() + "_製令單" + "]");// 製令單
				n.setBslfromwho("[" + v.getMb017() + "_" + v.getMc002() + "]");// 倉別代號+倉別名稱
				n.setBsltowho("[" + "_生產線" + "]");
				n.setBsledate(new Date());// 預計入料日
				n.setSysstatus(0);// 未完成
				saveShLists.add(n);
			}
		});
		// Step4. 存入資料
		incomingListDao.saveAll(saveInLists);// 1
		shippingListDao.saveAll(saveShLists);// 9
	}

	// ============A581 生產入庫單 ============
	public void erpSynchronizeMoctf() {
		logger.info("===erpSynchronizeMoctf: 時間:{}", dateFormat.format(new Date()));
		// Step1. 取得[頂新] 有效單據
		ArrayList<Moctf> erpEntitys = moctfDao.findAllByMoctf();
		Map<String, Moctf> erpInMaps = new HashMap<>();
		for (Moctf m : erpEntitys) {
			String nKey = m.getTg001_tg002_tg003() + "-" + m.getMb001();
			m.setTg001_tg002_tg003(m.getTg001_tg002_tg003().replaceAll("\\s", ""));
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
			nKey = nKey.replaceAll("\\s", "");
			m.setNewone(true);
			erpInMaps.put(nKey, m);
		}
		// Step2. 取得[Cloud] 有效入料單 資料
		ArrayList<BasicIncomingList> entityOlds = incomingListDao.findAllByStatus(0);
		// 存入資料物件
		ArrayList<BasicIncomingList> saveLists = new ArrayList<BasicIncomingList>();
		// Step3. 資料整理轉換
		entityOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+物料號+單項目號)
			String oKey = o.getBilclass() + "-" + o.getBilsn() + "-" + o.getBilnb() + "-" + o.getBilpnumber();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpInMaps.containsKey(oKey)) {
				String nChecksum = erpInMaps.get(oKey).toString().replaceAll("\\s", "");
				erpInMaps.get(oKey).setNewone(false);// 標記:不是新的
				// System.out.println(nChecksum);
				// System.out.println(o.getChecksum());
				// 內容不同=>更新
				if (!o.getChecksum().equals(nChecksum)) {
					Moctf m = erpInMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					o.setChecksum(checkSum);
					o.setBilclass(m.getTg001_tg002_tg003().split("-")[0].replaceAll("\\s", ""));// 入庫單[別]
					o.setBilsn(m.getTg001_tg002_tg003().split("-")[1].replaceAll("\\s", ""));// 入庫單[號]
					o.setBilnb(m.getTg001_tg002_tg003().split("-")[2].replaceAll("\\s", ""));// 序號
					o.setBiltype(m.getTk000());// 入庫單
					o.setBilcheckin(1);// 0=未核單 1=已核單
					o.setBilacceptance(m.getTg016().equals("1") ? 0 : 1);// 0=未檢驗 1=已檢驗 2=異常
					o.setBilpnumber(m.getMb001().replaceAll("\\s", ""));// 物料號品號
					o.setBilpname(m.getMb002());// 品名
					o.setBilpspecification(m.getMb003());// 規格
					o.setBilpnqty(m.getTg011());// 需入庫量

					o.setBilfromcommand("[" + m.getTg014_tg015() + "_製令單" + "]");// 製令單
					o.setBilfromwho("[" + "_生產線" + "]");
					o.setBiltowho("[" + m.getMb017() + "_" + m.getMc002() + "]");// 倉別代號+倉別名稱
					o.setBiledate(new Date());// 預計入料日
					o.setSysstatus(0);// 未完成
					o.setSysmdate(new Date());
					o.setSysnote(m.getTg020());// 備註
					saveLists.add(o);
				}
			}
		});
		// 全新資料?
		erpInMaps.forEach((key, v) -> {
			if (v.isNewone()) {
				BasicIncomingList n = new BasicIncomingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				n.setChecksum(checkSum);
				n.setBilclass(v.getTg001_tg002_tg003().split("-")[0].replaceAll("\\s", ""));// 入料單[別]
				n.setBilsn(v.getTg001_tg002_tg003().split("-")[1].replaceAll("\\s", ""));// 入料單[號]
				n.setBilnb(v.getTg001_tg002_tg003().split("-")[2].replaceAll("\\s", ""));// 序號
				n.setBiltype(v.getTk000());// 領料單
				n.setBilcheckin(v.getTg016().equals("1") ? 0 : 1);// 0=未核單 1=已核單
				n.setBilacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
				n.setBilpnumber(v.getMb001().replaceAll("\\s", ""));// 物料號品號
				n.setBilpname(v.getMb002());// 品名
				n.setBilpspecification(v.getMb003());// 規格
				n.setBilpnqty(v.getTg011());// 需入量
				n.setBilpnaqty(0);// 提前領用

				n.setBilfromcommand("[" + v.getTg014_tg015() + "_製令單" + "]");// 製令單
				n.setBilfromwho("[" + "_生產線" + "]");
				n.setBiltowho("[" + v.getMb017() + "_" + v.getMc002() + "]");// 倉別代號+倉別名稱
				n.setBiledate(new Date());// 預計入料日
				n.setSysstatus(0);// 未完成
				saveLists.add(n);
			}
		});
		// Step4. 存入資料
		incomingListDao.saveAll(saveLists);// 1
	}

	// ============ A591 委外進貨單 ============
	public void erpSynchronizeMocth() {
		logger.info("===erpSynchronizeMocth: 時間:{}", dateFormat.format(new Date()));
		// Step1. 取得[頂新] 有效的入 A591....
		ArrayList<Mocth> erpEntitys = mocthDao.findAllByMocth();
		Map<String, Mocth> erpInMaps = new HashMap<>();
		for (Mocth m : erpEntitys) {
			String nKey = m.getTi001_ti002_ti003() + "-" + m.getMb001();
			nKey = nKey.replaceAll("\\s", "");
			m.setTi001_ti002_ti003(m.getTi001_ti002_ti003().replaceAll("\\s", ""));
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
			m.setNewone(true);
			erpInMaps.put(nKey, m);
		}
		// Step2. 取得[Cloud] 有效 委外入料單 資料
		ArrayList<BasicIncomingList> entityOlds = incomingListDao.findAllByStatus(0);
		// 存入資料物件
		ArrayList<BasicIncomingList> saveLists = new ArrayList<BasicIncomingList>();
		// Step3. 資料整理轉換
		entityOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+物料號+單項目號)
			String oKey = o.getBilclass() + "-" + o.getBilsn() + "-" + o.getBilnb() + "-" + o.getBilpnumber();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpInMaps.containsKey(oKey)) {
				String nChecksum = erpInMaps.get(oKey).toString().replaceAll("\\s", "");
				erpInMaps.get(oKey).setNewone(false);// 標記:不是新的
				// System.out.println(nChecksum);
				// System.out.println(o.getChecksum());
				// 內容不同=>更新
				if (!o.getChecksum().equals(nChecksum)) {
					Mocth m = erpInMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					o.setChecksum(checkSum);
					o.setBilclass(m.getTi001_ti002_ti003().split("-")[0].replaceAll("\\s", ""));// 入庫單[別]
					o.setBilsn(m.getTi001_ti002_ti003().split("-")[1].replaceAll("\\s", ""));// 入庫單[號]
					o.setBilnb(m.getTi001_ti002_ti003().split("-")[2].replaceAll("\\s", ""));// 序號
					o.setBiltype(m.getTk000());// 入庫單
					o.setBilcheckin(1);// 0=未核單 1=已核單
					o.setBilacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
					o.setBilpnumber(m.getMb001().replaceAll("\\s", ""));// 物料號品號
					o.setBilpname(m.getMb002());// 品名
					o.setBilpspecification(m.getMb003());// 規格
					o.setBilpnqty(m.getTi007());// 需入庫量

					o.setBilfromcommand("[" + m.getTi013_ti014() + "_製令單" + "]");// 製令單
					o.setBilfromwho("[" + "_委外生產" + "]");
					o.setBiltowho("[" + m.getMb017() + "_" + m.getMc002() + "]");// 倉別代號+倉別名稱
					o.setBiledate(new Date());// 預計入料日
					o.setSysstatus(0);// 未完成
					o.setSysmdate(new Date());
					saveLists.add(o);
				}
			}
		});
		// 全新資料?
		erpInMaps.forEach((key, v) -> {
			if (v.isNewone()) {
				BasicIncomingList n = new BasicIncomingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				n.setChecksum(checkSum);
				n.setBilclass(v.getTi001_ti002_ti003().split("-")[0].replaceAll("\\s", ""));// 入料單[別]
				n.setBilsn(v.getTi001_ti002_ti003().split("-")[1].replaceAll("\\s", ""));// 入料單[號]
				n.setBilnb(v.getTi001_ti002_ti003().split("-")[2].replaceAll("\\s", ""));// 序號
				n.setBiltype(v.getTk000());// 領料單
				n.setBilcheckin(1);// 0=未核單 1=已核單
				n.setBilacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
				n.setBilpnumber(v.getMb001().replaceAll("\\s", ""));// 物料號品號
				n.setBilpname(v.getMb002());// 品名
				n.setBilpspecification(v.getMb003());// 規格
				n.setBilpnqty(v.getTi007());// 需入量
				n.setBilpnaqty(0);// 提前領用

				n.setBilfromcommand("[" + v.getTi013_ti014() + "_製令單" + "]");// 製令單
				n.setBilfromwho("[" + "_委外生產" + "]");
				n.setBiltowho("[" + v.getMb017() + "_" + v.getMc002() + "]");// 倉別代號+倉別名稱
				n.setBiledate(new Date());// 預計入料日
				n.setSysstatus(0);// 未完成
				saveLists.add(n);
			}
		});
		// Step4. 存入資料
		incomingListDao.saveAll(saveLists);

	}

	// ============ A131 庫存借出單/ A141 庫存借入單 ============
	public void erpSynchronizeInvtg() {
		logger.info("===erpSynchronizeInvtg: 時間:{}", dateFormat.format(new Date()));
		ArrayList<Invtg> erpEntitys = invtgDao.findAllByMocth();
		Map<String, Invtg> erpInMaps = new HashMap<>();
		Map<String, Invtg> erpShMaps = new HashMap<>();
		for (Invtg m : erpEntitys) {
			String nKey = m.getTg001_tg002_tg003() + "-" + m.getMb001();
			m.setTg001_tg002_tg003(m.getTg001_tg002_tg003().replaceAll("\\s", ""));
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
			nKey = nKey.replaceAll("\\s", "");
			m.setNewone(true);
			// 單據性質別:A131 庫存借出單/ A141 庫存借入單
			if (m.getTg001_tg002_tg003().indexOf("A131") >= 0) {
				m.setTk000("領料類");
				erpShMaps.put(nKey, m);
			} else {
				m.setTk000("入料類");
				erpInMaps.put(nKey, m);
			}
		}
		// Step2. 取得[Cloud] 有效入料單 資料
		ArrayList<BasicIncomingList> entityInOlds = incomingListDao.findAllByStatus(0);
		ArrayList<BasicShippingList> entityShOlds = shippingListDao.findAllByStatus(0);
		// 存入資料物件
		ArrayList<BasicIncomingList> saveInLists = new ArrayList<BasicIncomingList>();
		ArrayList<BasicShippingList> saveShLists = new ArrayList<BasicShippingList>();
		// Step3. 資料整理轉換
		// 進料
		entityInOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+物料號+單項目號)
			String oKey = o.getBilclass() + "-" + o.getBilsn() + "-" + o.getBilnb() + "-" + o.getBilpnumber();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpInMaps.containsKey(oKey)) {
				String nChecksum = erpInMaps.get(oKey).toString().replaceAll("\\s", "");
				erpInMaps.get(oKey).setNewone(false);// 標記:不是新的
				// System.out.println(nChecksum);
				// System.out.println(o.getChecksum());
				// 內容不同=>更新
				if (!o.getChecksum().equals(nChecksum)) {
					Invtg m = erpInMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					o.setChecksum(checkSum);
					o.setBilclass(m.getTg001_tg002_tg003().split("-")[0].replaceAll("\\s", ""));// 借入庫單[別]
					o.setBilsn(m.getTg001_tg002_tg003().split("-")[1].replaceAll("\\s", ""));// 借入庫單[號]
					o.setBilnb(m.getTg001_tg002_tg003().split("-")[2].replaceAll("\\s", ""));// 序號
					o.setBiltype(m.getTk000());// 入庫單
					o.setBilcheckin(1);// 0=未核單 1=已核單
					o.setBilacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
					o.setBilpnumber(m.getMb001().replaceAll("\\s", ""));// 物料號品號
					o.setBilpname(m.getMb002());// 品名
					o.setBilpspecification(m.getMb003());// 規格
					o.setBilpnqty(m.getTg009());// 數量

					o.setBilfromcommand("[" + "]");// 製令單
					o.setBilfromwho("[" + "_" + m.getTf015() + "]");// 對象
					o.setBiltowho("[" + m.getMb017() + "_" + m.getMc002() + "]");// 倉別代號+倉別名稱
					o.setBiledate(new Date());// 預計入料日
					o.setSysstatus(0);// 未完成
					o.setSysmdate(new Date());
					saveInLists.add(o);
				}
			}
		});
		// 領料
		entityShOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+物料號+單項目號)
			String oKey = o.getBslclass() + "-" + o.getBslsn() + "-" + o.getBslnb() + "-" + o.getBslpnumber();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpShMaps.containsKey(oKey)) {
				String nChecksum = erpShMaps.get(oKey).toString().replaceAll("\\s", "");
				erpShMaps.get(oKey).setNewone(false);// 標記:不是新的
				// System.out.println(nChecksum);
				// System.out.println(o.getChecksum());
				// 內容不同=>更新
				if (!o.getChecksum().equals(nChecksum)) {
					Invtg m = erpShMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					o.setChecksum(checkSum);
					o.setBslclass(m.getTg001_tg002_tg003().split("-")[0].replaceAll("\\s", ""));// 借出庫單[別]
					o.setBslsn(m.getTg001_tg002_tg003().split("-")[1].replaceAll("\\s", ""));// 借出庫單[號]
					o.setBslnb(m.getTg001_tg002_tg003().split("-")[2].replaceAll("\\s", ""));// 序號
					o.setBsltype(m.getTk000());// 入庫單
					o.setBslcheckin(1);// 0=未核單 1=已核單
					o.setBslacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
					o.setBslpnumber(m.getMb001().replaceAll("\\s", ""));// 物料號品號
					o.setBslpname(m.getMb002());// 品名
					o.setBslpspecification(m.getMb003());// 規格
					o.setBslpnqty(m.getTg009());// 數量

					o.setBslfromcommand("[" + "]");//
					o.setBslfromwho("[" + m.getMb017() + "_" + m.getMc002() + "]");// 倉別代號+倉別名稱
					o.setBsltowho("[" + "_" + m.getTf015() + "]");
					o.setBsledate(new Date());// 預計日
					o.setSysstatus(0);// 未完成
					o.setSysmdate(new Date());
					saveShLists.add(o);
				}
			}
		});

		// 全新資料?
		// 入料
		erpInMaps.forEach((key, v) -> {
			if (v.isNewone()) {
				BasicIncomingList n = new BasicIncomingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				n.setChecksum(checkSum);
				n.setBilclass(v.getTg001_tg002_tg003().split("-")[0].replaceAll("\\s", ""));// 入庫單[別]
				n.setBilsn(v.getTg001_tg002_tg003().split("-")[1].replaceAll("\\s", ""));// 入庫單[號]
				n.setBilnb(v.getTg001_tg002_tg003().split("-")[2].replaceAll("\\s", ""));// 序號
				n.setBiltype(v.getTk000());// 領料單
				n.setBilcheckin(1);// 0=未核單 1=已核單
				n.setBilacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
				n.setBilpnumber(v.getMb001().replaceAll("\\s", ""));// 物料號品號
				n.setBilpname(v.getMb002());// 品名
				n.setBilpspecification(v.getMb003());// 規格
				n.setBilpnqty(v.getTg009());// 需入量
				n.setBilpnaqty(0);// 提前領用

				n.setBilfromcommand("[" + "]");// 製令單
				n.setBilfromwho("[" + "_" + v.getTf015() + "]");
				n.setBiltowho("[" + v.getMb017() + "_" + v.getMc002() + "]");// 倉別代號+倉別名稱
				n.setBiledate(new Date());// 預計入料日
				n.setSysstatus(0);// 未完成
				saveInLists.add(n);
			}
		});
		// 領料
		erpShMaps.forEach((key, v) -> {
			if (v.isNewone()) {
				BasicShippingList n = new BasicShippingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				n.setChecksum(checkSum);
				n.setBslclass(v.getTg001_tg002_tg003().split("-")[0].replaceAll("\\s", ""));// 出庫單[別]
				n.setBslsn(v.getTg001_tg002_tg003().split("-")[1].replaceAll("\\s", ""));// 出庫單[號]
				n.setBslnb(v.getTg001_tg002_tg003().split("-")[2].replaceAll("\\s", ""));// 序號
				n.setBsltype(v.getTk000());// 領料單
				n.setBslcheckin(1);// 0=未核單 1=已核單
				n.setBslacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
				n.setBslpnumber(v.getMb001().replaceAll("\\s", ""));// 物料號品號
				n.setBslpname(v.getMb002());// 品名
				n.setBslpspecification(v.getMb003());// 規格
				n.setBslpnqty(v.getTg009());// 需入量
				n.setBslpnaqty(0);// 提前領用

				n.setBslfromcommand("[" + "]");//
				n.setBslfromwho("[" + v.getMb017() + "_" + v.getMc002() + "]");// 倉別代號+倉別名稱
				n.setBsltowho("[" + "_" + v.getTf015() + "]");
				n.setBsledate(new Date());// 預計入料日
				n.setSysstatus(0);// 未完成
				saveShLists.add(n);
			}
		});

		// Step4. 存入資料
		incomingListDao.saveAll(saveInLists);
		shippingListDao.saveAll(saveShLists);
	}

	// ============ 借出歸還A151/借入歸還單A161 ============
	public void erpSynchronizeInvth() {
		logger.info("===erpSynchronizeInvtg: 時間:{}", dateFormat.format(new Date()));
		ArrayList<Invth> erpEntitys = invthDao.findAllByMocth();
		Map<String, Invth> erpInMaps = new HashMap<>();
		Map<String, Invth> erpShMaps = new HashMap<>();
		for (Invth m : erpEntitys) {
			String nKey = m.getTi001_ti002_ti003() + "-" + m.getMb001();
			m.setTi001_ti002_ti003(m.getTi001_ti002_ti003().replaceAll("\\s", ""));
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
			nKey = nKey.replaceAll("\\s", "");
			m.setNewone(true);
			// 單據性質別:借出歸還A151+借入歸還單A161
			if (m.getTi001_ti002_ti003().indexOf("A161") >= 0) {
				m.setTk000("領料類");
				erpShMaps.put(nKey, m);
			} else {
				m.setTk000("入料類");
				erpInMaps.put(nKey, m);
			}
		}
		// Step2. 取得[Cloud] 有效入料單 資料
		ArrayList<BasicIncomingList> entityInOlds = incomingListDao.findAllByStatus(0);
		ArrayList<BasicShippingList> entityShOlds = shippingListDao.findAllByStatus(0);
		// 存入資料物件
		ArrayList<BasicIncomingList> saveInLists = new ArrayList<BasicIncomingList>();
		ArrayList<BasicShippingList> saveShLists = new ArrayList<BasicShippingList>();
		// Step3. 資料整理轉換
		// 進料
		entityInOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+物料號+單項目號)
			String oKey = o.getBilclass() + "-" + o.getBilsn() + "-" + o.getBilnb() + "-" + o.getBilpnumber();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpInMaps.containsKey(oKey)) {
				String nChecksum = erpInMaps.get(oKey).toString().replaceAll("\\s", "");
				erpInMaps.get(oKey).setNewone(false);// 標記:不是新的
				// System.out.println(nChecksum);
				// System.out.println(o.getChecksum());
				// 內容不同=>更新
				if (!o.getChecksum().equals(nChecksum)) {
					Invth m = erpInMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					o.setChecksum(checkSum);
					o.setBilclass(m.getTi001_ti002_ti003().split("-")[0].replaceAll("\\s", ""));// 借入庫單[別]
					o.setBilsn(m.getTi001_ti002_ti003().split("-")[1].replaceAll("\\s", ""));// 借入庫單[號]
					o.setBilnb(m.getTi001_ti002_ti003().split("-")[2].replaceAll("\\s", ""));// 序號
					o.setBiltype(m.getTk000());// 入庫單
					o.setBilcheckin(1);// 0=未核單 1=已核單
					o.setBilacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
					o.setBilpnumber(m.getMb001().replaceAll("\\s", ""));// 物料號品號
					o.setBilpname(m.getMb002());// 品名
					o.setBilpspecification(m.getMb003());// 規格
					o.setBilpnqty(m.getTi009());// 數量

					o.setBilfromcommand("[" + m.getTi014_ti015_ti016() + "]");// 指令來源
					o.setBilfromwho("[" + "_" + m.getTh006() + "]");// 對象
					o.setBiltowho("[" + m.getMb017() + "_" + m.getMc002() + "]");// 倉別代號+倉別名稱
					o.setBiledate(new Date());// 預計入料日
					o.setSysstatus(0);// 未完成
					o.setSysmdate(new Date());
					saveInLists.add(o);
				}
			}
		});
		// 領料
		entityShOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+物料號+單項目號)
			String oKey = o.getBslclass() + "-" + o.getBslsn() + "-" + o.getBslnb() + "-" + o.getBslpnumber();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpShMaps.containsKey(oKey)) {
				String nChecksum = erpShMaps.get(oKey).toString().replaceAll("\\s", "");
				erpShMaps.get(oKey).setNewone(false);// 標記:不是新的
				// System.out.println(nChecksum);
				// System.out.println(o.getChecksum());
				// 內容不同=>更新
				if (!o.getChecksum().equals(nChecksum)) {
					Invth m = erpShMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					o.setChecksum(checkSum);
					o.setBslclass(m.getTi001_ti002_ti003().split("-")[0].replaceAll("\\s", ""));// 借出庫單[別]
					o.setBslsn(m.getTi001_ti002_ti003().split("-")[1].replaceAll("\\s", ""));// 借出庫單[號]
					o.setBslnb(m.getTi001_ti002_ti003().split("-")[2].replaceAll("\\s", ""));// 序號
					o.setBsltype(m.getTk000());// 入庫單
					o.setBslcheckin(1);// 0=未核單 1=已核單
					o.setBslacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
					o.setBslpnumber(m.getMb001().replaceAll("\\s", ""));// 物料號品號
					o.setBslpname(m.getMb002());// 品名
					o.setBslpspecification(m.getMb003());// 規格
					o.setBslpnqty(m.getTi009());// 數量

					o.setBslfromcommand("[" + m.getTi014_ti015_ti016() + "]");//
					o.setBslfromwho("[" + m.getMb017() + "_" + m.getMc002() + "]");// 倉別代號+倉別名稱
					o.setBsltowho("[" + "_" + m.getTh006() + "]");
					o.setBsledate(new Date());// 預計日
					o.setSysstatus(0);// 未完成
					o.setSysmdate(new Date());
					saveShLists.add(o);
				}
			}
		});

		// 全新資料?
		// 入料
		erpInMaps.forEach((key, v) -> {
			if (v.isNewone()) {
				BasicIncomingList n = new BasicIncomingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				n.setChecksum(checkSum);
				n.setBilclass(v.getTi001_ti002_ti003().split("-")[0].replaceAll("\\s", ""));// 入庫單[別]
				n.setBilsn(v.getTi001_ti002_ti003().split("-")[1].replaceAll("\\s", ""));// 入庫單[號]
				n.setBilnb(v.getTi001_ti002_ti003().split("-")[2].replaceAll("\\s", ""));// 序號
				n.setBiltype(v.getTk000());// 領料單
				n.setBilcheckin(1);// 0=未核單 1=已核單
				n.setBilacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
				n.setBilpnumber(v.getMb001().replaceAll("\\s", ""));// 物料號品號
				n.setBilpname(v.getMb002());// 品名
				n.setBilpspecification(v.getMb003());// 規格
				n.setBilpnqty(v.getTi009());// 需入量
				n.setBilpnaqty(0);// 提前領用

				n.setBilfromcommand("[" + v.getTi014_ti015_ti016() + "]");// 指令來源
				n.setBilfromwho("[" + "_" + v.getTh006() + "]");
				n.setBiltowho("[" + v.getMb017() + "_" + v.getMc002() + "]");// 倉別代號+倉別名稱
				n.setBiledate(new Date());// 預計入料日
				n.setSysstatus(0);// 未完成
				saveInLists.add(n);
			}
		});
		// 領料
		erpShMaps.forEach((key, v) -> {
			if (v.isNewone()) {
				BasicShippingList n = new BasicShippingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				n.setChecksum(checkSum);
				n.setBslclass(v.getTi001_ti002_ti003().split("-")[0].replaceAll("\\s", ""));// 出庫單[別]
				n.setBslsn(v.getTi001_ti002_ti003().split("-")[1].replaceAll("\\s", ""));// 出庫單[號]
				n.setBslnb(v.getTi001_ti002_ti003().split("-")[2].replaceAll("\\s", ""));// 序號
				n.setBsltype(v.getTk000());// 領料單
				n.setBslcheckin(1);// 0=未核單 1=已核單
				n.setBslacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
				n.setBslpnumber(v.getMb001().replaceAll("\\s", ""));// 物料號品號
				n.setBslpname(v.getMb002());// 品名
				n.setBslpspecification(v.getMb003());// 規格
				n.setBslpnqty(v.getTi009());// 需入量
				n.setBslpnaqty(0);// 提前領用

				n.setBslfromcommand("[" + v.getTi014_ti015_ti016() + "]");//
				n.setBslfromwho("[" + v.getMb017() + "_" + v.getMc002() + "]");// 倉別代號+倉別名稱
				n.setBsltowho("[" + "_" + v.getTh006() + "]");
				n.setBsledate(new Date());// 預計入料日
				n.setSysstatus(0);// 未完成
				saveShLists.add(n);
			}
		});

		// Step4. 存入資料
		incomingListDao.saveAll(saveInLists);
		shippingListDao.saveAll(saveShLists);
	}

	// ============ A111 費用領料單/ A112 費用退料單/ A119 料號調整單/ A121 倉庫調撥單 ============
	public void erpSynchronizeInvta() {
		logger.info("===erpSynchronizeInvtg: 時間:{}", dateFormat.format(new Date()));
		ArrayList<Invta> erpEntitys = invtaDao.findAllByMocta();
		Map<String, Invta> erpInMaps = new HashMap<>();
		Map<String, Invta> erpShMaps = new HashMap<>();
		for (Invta m : erpEntitys) {
			String nKey = m.getTb001_tb002_tb003() + "-" + m.getMb001();
			m.setTb001_tb002_tb003(m.getTb001_tb002_tb003().replaceAll("\\s", ""));
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
			nKey = nKey.replaceAll("\\s", "");
			m.setNewone(true);
			// 單據性質別:借出歸還A151+借入歸還單A161
			if (m.getTb001_tb002_tb003().indexOf("A111") >= 0) {
				m.setTk000("領料類");
				erpShMaps.put(nKey, m);
			} else if (m.getTb001_tb002_tb003().indexOf("A112") >= 0) {
				m.setTk000("入料類");
				erpInMaps.put(nKey, m);
			} else if (m.getTb001_tb002_tb003().indexOf("A121") >= 0) {// 轉
				m.setTk000("入料類");
				erpInMaps.put(nKey, m);
				m.setTk000("領料類");
				erpShMaps.put(nKey, m);
			}
		}
		// Step2. 取得[Cloud] 有效入料單 資料
		ArrayList<BasicIncomingList> entityInOlds = incomingListDao.findAllByStatus(0);
		ArrayList<BasicShippingList> entityShOlds = shippingListDao.findAllByStatus(0);
		// 存入資料物件
		ArrayList<BasicIncomingList> saveInLists = new ArrayList<BasicIncomingList>();
		ArrayList<BasicShippingList> saveShLists = new ArrayList<BasicShippingList>();
		// Step3. 資料整理轉換
		// 進料
		entityInOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+物料號+單項目號)
			String oKey = o.getBilclass() + "-" + o.getBilsn() + "-" + o.getBilnb() + "-" + o.getBilpnumber();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpInMaps.containsKey(oKey)) {
				String nChecksum = erpInMaps.get(oKey).toString().replaceAll("\\s", "");
				erpInMaps.get(oKey).setNewone(false);// 標記:不是新的
				// System.out.println(nChecksum);
				// System.out.println(o.getChecksum());
				// 內容不同=>更新
				if (!o.getChecksum().equals(nChecksum)) {
					Invta m = erpInMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					o.setChecksum(checkSum);
					o.setBilclass(m.getTb001_tb002_tb003().split("-")[0].replaceAll("\\s", ""));// 借入庫單[別]
					o.setBilsn(m.getTb001_tb002_tb003().split("-")[1].replaceAll("\\s", ""));// 借入庫單[號]
					o.setBilnb(m.getTb001_tb002_tb003().split("-")[2].replaceAll("\\s", ""));// 序號
					o.setBiltype(m.getTk000());// 入庫單
					o.setBilcheckin(1);// 0=未核單 1=已核單
					o.setBilacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
					o.setBilpnumber(m.getMb001().replaceAll("\\s", ""));// 物料號品號
					o.setBilpname(m.getMb002());// 品名
					o.setBilpspecification(m.getMb003());// 規格
					o.setBilpnqty(m.getTb007());// 數量

					o.setBilfromcommand("[" + "]");// 指令來源
					o.setBilfromwho("[" + "_" + m.getTb012() + "]");// 對象
					o.setBiltowho("[" + "_" + m.getTb013() + "]");// 倉別代號+倉別名稱
					o.setBiledate(new Date());// 預計入料日
					o.setSysstatus(0);// 未完成
					o.setSysmdate(new Date());
					saveInLists.add(o);
				}
			}
		});
		// 領料
		entityShOlds.forEach(o -> {
			// 基本資料準備:檢碼(單類別+單序號+物料號+單項目號)
			String oKey = o.getBslclass() + "-" + o.getBslsn() + "-" + o.getBslnb() + "-" + o.getBslpnumber();
			oKey = oKey.replaceAll("\\s", "");
			// 同一筆資料?
			if (erpShMaps.containsKey(oKey)) {
				String nChecksum = erpShMaps.get(oKey).toString().replaceAll("\\s", "");
				erpShMaps.get(oKey).setNewone(false);// 標記:不是新的
				// System.out.println(nChecksum);
				// System.out.println(o.getChecksum());
				// 內容不同=>更新
				if (!o.getChecksum().equals(nChecksum)) {
					Invta m = erpShMaps.get(oKey);
					String checkSum = m.toString().replaceAll("\\s", "");
					o.setChecksum(checkSum);
					o.setBslclass(m.getTb001_tb002_tb003().split("-")[0].replaceAll("\\s", ""));// 借出庫單[別]
					o.setBslsn(m.getTb001_tb002_tb003().split("-")[1].replaceAll("\\s", ""));// 借出庫單[號]
					o.setBslnb(m.getTb001_tb002_tb003().split("-")[2].replaceAll("\\s", ""));// 序號
					o.setBsltype(m.getTk000());// 入庫單
					o.setBslcheckin(1);// 0=未核單 1=已核單
					o.setBslacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
					o.setBslpnumber(m.getMb001().replaceAll("\\s", ""));// 物料號品號
					o.setBslpname(m.getMb002());// 品名
					o.setBslpspecification(m.getMb003());// 規格
					o.setBslpnqty(m.getTb007());// 數量

					o.setBslfromcommand("[" + "]");//
					o.setBslfromwho("[" + "_" + m.getTb012() + "]");// 倉別代號+倉別名稱
					o.setBsltowho("[" + "_" + m.getTb013() + "]");
					o.setBsledate(new Date());// 預計日
					o.setSysstatus(0);// 未完成
					o.setSysmdate(new Date());
					saveShLists.add(o);
				}
			}
		});

		// 全新資料?
		// 入料
		erpInMaps.forEach((key, v) -> {
			if (v.isNewone()) {
				BasicIncomingList n = new BasicIncomingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				n.setChecksum(checkSum);
				n.setBilclass(v.getTb001_tb002_tb003().split("-")[0].replaceAll("\\s", ""));// 入庫單[別]
				n.setBilsn(v.getTb001_tb002_tb003().split("-")[1].replaceAll("\\s", ""));// 入庫單[號]
				n.setBilnb(v.getTb001_tb002_tb003().split("-")[2].replaceAll("\\s", ""));// 序號
				n.setBiltype(v.getTk000());// 領料單
				n.setBilcheckin(1);// 0=未核單 1=已核單
				n.setBilacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
				n.setBilpnumber(v.getMb001().replaceAll("\\s", ""));// 物料號品號
				n.setBilpname(v.getMb002());// 品名
				n.setBilpspecification(v.getMb003());// 規格
				n.setBilpnqty(v.getTb007());// 需入量
				n.setBilpnaqty(0);// 提前領用

				n.setBilfromcommand("[" + "]");// 指令來源
				n.setBilfromwho("[" + "_" + v.getTb012() + "]");
				n.setBiltowho("[" + "_" + v.getTb013() + "]");// 倉別代號+倉別名稱
				n.setBiledate(new Date());// 預計入料日
				n.setSysstatus(0);// 未完成
				saveInLists.add(n);
			}
		});
		// 領料
		erpShMaps.forEach((key, v) -> {
			if (v.isNewone()) {
				BasicShippingList n = new BasicShippingList();
				String checkSum = v.toString().replaceAll("\\s", "");
				n.setChecksum(checkSum);
				n.setBslclass(v.getTb001_tb002_tb003().split("-")[0].replaceAll("\\s", ""));// 出庫單[別]
				n.setBslsn(v.getTb001_tb002_tb003().split("-")[1].replaceAll("\\s", ""));// 出庫單[號]
				n.setBslnb(v.getTb001_tb002_tb003().split("-")[2].replaceAll("\\s", ""));// 序號
				n.setBsltype(v.getTk000());// 領料單
				n.setBslcheckin(1);// 0=未核單 1=已核單
				n.setBslacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
				n.setBslpnumber(v.getMb001().replaceAll("\\s", ""));// 物料號品號
				n.setBslpname(v.getMb002());// 品名
				n.setBslpspecification(v.getMb003());// 規格
				n.setBslpnqty(v.getTb007());// 需入量
				n.setBslpnaqty(0);// 提前領用

				n.setBslfromcommand("[" + "]");//
				n.setBslfromwho("[" + "_" + v.getTb012() + "]");// 倉別代號+倉別名稱
				n.setBsltowho("[" + "_" + v.getTb013() + "]");
				n.setBsledate(new Date());// 預計入料日
				n.setSysstatus(0);// 未完成
				saveShLists.add(n);
			}
		});

		// Step4. 存入資料
		incomingListDao.saveAll(saveInLists);
		shippingListDao.saveAll(saveShLists);

	}

	// ============ 物料+儲位同步 ============
	public void erpSynchronizeInvtb() {
		logger.info("===erpSynchronizeInvtb: 時間:{}", dateFormat.format(new Date()));

		// Step1. 取得[頂新] 有效的物料....
		ArrayList<Invtb> erpEntitys = invtbDao.findAllByMoctb();
		Map<String, Invtb> erpListMaps = new HashMap<>();
		Map<String, Invtb> erpItemMaps = new HashMap<>();
		Map<String, String> erpConfigMaps = new HashMap<>();// A1000+原物料倉
		String checkSame = "";
		for (Invtb m : erpEntitys) {
			// 物料號+倉別號+位置
			String nKey = m.getMb001() + "_" + m.getMc002() + '_' + m.getMc003();
			nKey = nKey.replaceAll("\\s", "");
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
			m.setMb001(m.getMb001().replaceAll("\\s", ""));
			m.setMb002(m.getMb002().replaceAll("\\s", ""));
			m.setMc002(m.getMc002().replaceAll("\\s", ""));
			m.setNewone(true);
			// list(只有唯一物料)
			if (!checkSame.equals(m.getMb001())) {
				checkSame = m.getMb001();
				erpListMaps.put(m.getMb001(), m);
			}
			// item(所有項目)
			erpItemMaps.put(nKey, m);
			// config(倉別清單)
			erpConfigMaps.put(m.getMc002(), m.getCmc002());
		}

		// Step2. 取得[Cloud] 有效 委外入料單 資料
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
		areaOlds.forEach(a -> {
			String aKey = a.getWawmpnb() + "_" + a.getWaalias() + "_" + a.getWaslocation();
			// 同一筆?
			if (erpItemMaps.containsKey(aKey)) {
				erpItemMaps.get(aKey).setNewone(false);// 標記:不是新的
				Invtb av = erpItemMaps.get(aKey);
				String checkSum = av.toString().replaceAll("\\s", "");
				if (!checkSum.equals(a.getChecksum())) {
					// 正則表達式:FF-FF-FF-FF
					Boolean checkloc = av.getMc003().matches("[0-9A-Z]{2}-[0-9A-Z]{2}-[0-9A-Z]{2}-[0-9A-Z]{2}");
					a.setWawmpnb(av.getMb001());// 物料號
					a.setWaalias(av.getMc002());// 倉庫別
					a.setWawmpnbalias(av.getMb001() + "_" + av.getMc002());// 物料號+倉庫別
					a.setWaslocation(checkloc ? av.getMc003() : a.getWaslocation());// 物料位置
					a.setWaaname(av.getCmc002());// 倉庫名稱
					a.setWaerptqty(av.getMc007());// 倉儲數量
					a.setMaterial(materialDao.findAllByWmpnb(av.getMb001()).get(0));
					a.setChecksum(checkSum);
					saveItems.add(a);
				}
			}
		});

		// Step4-2. [物料位置] 全新資料?
		erpItemMaps.forEach((key, v) -> {
			if (v.isNewone()) {
				// 可能重複?
				ArrayList<WarehouseArea> areaSame = areaDao.findAllByWawmpnbalias(v.getMb001() + "_" + v.getMc002());
				if (areaSame.size() > 0) {
					System.out.println(areaSame.get(0).getWawmpnb());
				} else {
					// 正則表達式:FF-FF-FF-FF
					Boolean checkloc = v.getMc003().matches("[0-9A-Z]{2}-[0-9A-Z]{2}-[0-9A-Z]{2}-[0-9A-Z]{2}");
					WarehouseArea n = new WarehouseArea();
					String checkSum = v.toString().replaceAll("\\s", "");
					n.setChecksum(checkSum);
					n.setWawmpnb(v.getMb001());// 物料號
					n.setWaalias(v.getMc002());// 倉庫別
					n.setWawmpnbalias(v.getMb001() + "_" + v.getMc002());// 物料號+倉庫別
					n.setWaslocation(checkloc ? v.getMc003() : "FF-FF-FF-FF");// 物料位置
					n.setWaaname(v.getCmc002());// 倉庫名稱
					n.setWaerptqty(v.getMc007());// 倉儲數量
					n.setWatqty(0);// (實際)倉儲數量
					n.setMaterial(materialDao.findAllByWmpnb(v.getMb001()).get(0));
					saveItems.add(n);
				}
			}
		});
		areaDao.saveAll(saveItems);
		// Step5 儲位設定 全新資料?
		erpSynchronizeWconfig(erpConfigMaps, configOlds);

		// Step6 去除掉數量為0的儲位
		List<WarehouseArea> areaRemove = areaDao.findAllByWaerptqty(0);
		areaDao.deleteAll(areaRemove);
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
			if (checkNew) {
				WarehouseConfig newC = new WarehouseConfig();
				newC.setWcalias(key);
				newC.setWcwkaname(v);
				saveConfig.add(newC);
			}
		});
		configDao.saveAll(saveConfig);
	}

	// ============ 單據過濾設定 ============
	public void erpSynchronizeWtypeFilter(Map<String, String> erpConfigMaps, List<WarehouseConfig> configOlds) {
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
			if (checkNew) {
				WarehouseConfig newC = new WarehouseConfig();
				newC.setWcalias(key);
				newC.setWcwkaname(v);
				saveConfig.add(newC);
			}
		});
		configDao.saveAll(saveConfig);
	}

}
