package dtri.com.tw.service;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dtri.com.tw.mssql.entity.Bomtd;
import dtri.com.tw.mssql.entity.Bomtf;
import dtri.com.tw.mssql.entity.Copth;
import dtri.com.tw.mssql.entity.Invta;
import dtri.com.tw.mssql.entity.Invtg;
import dtri.com.tw.mssql.entity.Invth;
import dtri.com.tw.mssql.entity.Mocta;
import dtri.com.tw.mssql.entity.MoctaScheduleOutsourcer;
import dtri.com.tw.mssql.entity.Mocte;
import dtri.com.tw.mssql.entity.Moctf;
import dtri.com.tw.mssql.entity.Mocth;
import dtri.com.tw.mssql.entity.Purth;
import dtri.com.tw.pgsql.entity.BasicCommandList;
import dtri.com.tw.pgsql.entity.BasicIncomingList;
import dtri.com.tw.pgsql.entity.BasicShippingList;
import dtri.com.tw.pgsql.entity.ScheduleOutsourcer;
import dtri.com.tw.pgsql.entity.WarehouseArea;
import dtri.com.tw.pgsql.entity.WarehouseKeeper;
import dtri.com.tw.pgsql.entity.WarehouseTypeFilter;
import dtri.com.tw.shared.Fm_T;

@Service
public class ERPToCloudService {

	// 指令類-轉換
	public BasicCommandList commandOne(BasicCommandList o, Mocta m, String checkSum) {

		// 資料匹配
		o.setChecksum(checkSum);
		o.setBclfuser("");
		o.setBclproduct(m.getTa006());// 成品號
		// 單頭
		o.setBclclass(m.getTa001_ta002().split("-")[0]);// 製令單[別]
		o.setBclsn(m.getTa001_ta002().split("-")[1]);// 製令單[號]
		o.setBcltype(m.getTk000());// 製令單
		o.setBclnb(m.getBslnb());// 序列號
		o.setBclcheckin(1);// 0=未核單 1=已核單
		o.setBclacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
		o.setBclmodel(m.getMa003());// 機型
		o.setBclcustomer(m.getTa050());// --訂單生產加工包裝資訊(客戶資訊)
		// 單身
		o.setBclpnumber(m.getMb001());// 物料號品號
		o.setBclpname(m.getMb002());// 品名
		o.setBclpspecification(m.getMb003());// 規格
		o.setBclpnqty(m.getTb004());// 需領用
		o.setBclpnaqty(m.getTb005());// 已領用
		o.setBclfromcommand("[" + m.getTa026_ta027_ta028() + "_訂單]");// 單據指令-來源 訂單
		o.setBcltocommand("[_]");// 單據指令-對象
		o.setBcltowho("[_]");// 目的對象
		o.setBclfromwho("[" + m.getMb017() + "_" + m.getMc002() + "]");// 目的來源-[倉別代號+倉別名稱]
		o.setBclerpcuser(m.getCreator());// 開單人
		if (!m.getTb015().equals("")) {
			o.setBcledate(Fm_T.toYMDate(m.getTb015()));// 預計領料日
		}
		o.setSysstatus(0);// 未完成
		o.setSysmdate(new Date());// 日期
		return o;
	}

	// ============ A341 國內進貨單/ A342 國外進貨單/ A343 台北進貨單/ A345 無採購進貨單 ============
	// 入料類-轉換(Purth)
	public BasicIncomingList incomingOnePurth(BasicIncomingList o, Purth m, String checkSum, //
			Map<String, WarehouseTypeFilter> wTFs, TreeMap<String, WarehouseKeeper> wKs,
			Map<String, WarehouseArea> wAs) {
		// 是否結單?
		int sysstatus = 0;
		if (m.getTh030().equals("Y")) {
			sysstatus = 1;
//			if (o.getBilpngqty().equals(0)) {
//				o.setBilpngqty(m.getTh007());// 已入庫量
//			}
		}
		// 資料匹配
		o.setChecksum(checkSum);
		o.setBilfuser("");
		o.setBilclass(m.getTh001_th002().split("-")[0]);// 入庫單[別]
		o.setBilsn(m.getTh001_th002().split("-")[1]);// 入庫單[號]
		o.setBilnb(m.getTh003());// 序號
		o.setBiltype(m.getTk000());// 入庫單
		o.setBilcheckin(sysstatus);// 0=未核單 1=已核單
		o.setBilacceptance(m.getTh028().equals("1") ? 0 : 1);// 0=未檢驗 1=已檢驗 2=異常
		o.setBilpnumber(m.getMb001());// 物料號品號
		o.setBilpname(m.getMb002());// 品名
		o.setBilpspecification(m.getMb003());// 規格
		o.setBilpnqty(m.getTh007());// 需入庫量
		o.setBiledate(new Date());// 預計入料日(今天)
		o.setSysstatus(0);// 未完成
		o.setSysmdate(new Date());
		o.setBilerpcuser(m.getCreator());// 開單人
		// 而外匹配 [單別]
		String bilfromcommand = "_採購單";
		if (wTFs.containsKey(o.getBilclass())) {
			bilfromcommand = "_" + wTFs.get(o.getBilclass()).getWtfname();
			// 單據急迫性
			o.setBilstatus(wTFs.get(o.getBilclass()).getWtfurgency());
		}
		o.setBilfromcommand("[" + m.getTh011_th012_th013() + bilfromcommand + "]");// 單據來源 [_採購單]
		o.setBiltocommand("[_]");
		// 而外匹配 [倉別代號+倉別名稱+位置]
		String biltowho = m.getTh009() + "_" + m.getTh009() + "_FF-FF-FF-FF";
		String wAsKey = m.getTh009() + "_" + m.getMb001();
		if (wAs.containsKey(wAsKey)) {
			biltowho = m.getTh009() + "_" + wAs.get(wAsKey).getWaaname() + "_" + wAs.get(wAsKey).getWaslocation();
		}
		o.setBiltowho("[" + biltowho + "]");
		o.setBilfromwho("[" + m.getMb032() + "_" + m.getMa002() + "]");// 目的來源[供應商]
		// 而外匹配 [儲位負責]
		String bilmuser = "";
		for (String wkey : wKs.keySet()) {
			String wkeyAccount = wkey.split("_")[0];
			String wkeyWarehouse = wkey.split("_")[1];
			String wkeyLocal = wkey.split("_")[2];
			if (!wkey.equals("") && biltowho.indexOf(wkeyLocal) >= 0//
					&& wkeyWarehouse.equals(m.getTh009())) {
				bilmuser += wkeyAccount + "_";
				// break;
			}
		}
		o.setBilmuser(bilmuser);
		return o;
	}

	// ============ A541 廠內領料單/ A542 補料單/ A551 委外領料單/ A561 廠內退料單/ A571 委外退料單
	// 入料類-轉換(Mocte)
	public BasicIncomingList incomingOneMocte(BasicIncomingList o, Mocte m, String checkSum, //
			Map<String, WarehouseTypeFilter> wTFs, TreeMap<String, WarehouseKeeper> wKs,
			Map<String, WarehouseArea> wAs) {
		// 是否結單?
		int sysstatus = 0;
		if (m.getTe019().equals("Y")) {
			sysstatus = 1;
//			if (o.getBilpngqty().equals(0)) {
//				o.setBilpngqty(m.getTb004());// 已入庫量
//			}
		}
		// 資料匹配
		o.setChecksum(checkSum);
		o.setBilfuser("");
		o.setBilclass(m.getTa026_ta027_ta028().split("-")[0]);// 入庫單[別]
		o.setBilsn(m.getTa026_ta027_ta028().split("-")[1]);// 入庫單[號]
		o.setBilnb(m.getTa026_ta027_ta028().split("-")[2]);// 序號
		o.setBiltype(m.getTk000());// 入庫單
		o.setBilcheckin(sysstatus);// 0=未核單 1=已核單
		o.setBilacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
		o.setBilpnumber(m.getMb001());// 物料號品號
		o.setBilpname(m.getMb002());// 品名
		o.setBilpspecification(m.getMb003());// 規格
		o.setBilpnqty(m.getTb004());// 數量
		o.setBilpnerpqty(m.getTe005());// 數量(領退料數量ERP)
		o.setSysstatus(0);// 未完成
		o.setSysnote(m.getTe014());// 備註
		o.setSysmdate(new Date());
		o.setSysnote(m.getTe014());
		o.setBilerpcuser(m.getCreator());// 開單人
		o.setSyshnote(m.getTc007());// 單頭備註
		// 預計入料日
		if (m.getTa009() != null) {
			o.setBiledate(Fm_T.toYMDate(m.getTa009()));
		}

		// 而外匹配 [單別]
		String bilfromcommand = "_製令單";
		if (wTFs.containsKey(o.getBilclass())) {
			bilfromcommand = "_" + wTFs.get(o.getBilclass()).getWtfname();
			// 單據急迫性
			o.setBilstatus(wTFs.get(o.getBilclass()).getWtfurgency());
		}
		o.setBilfromcommand("[" + m.getTa001_ta002() + bilfromcommand + "]");// 單據來源 [_製令單]
		o.setBiltocommand("[_]");

		// 而外匹配 [倉別代號+倉別名稱+位置]
		String biltowho = m.getTb009() + "_" + m.getTb009() + "_FF-FF-FF-FF";
		String wAsKey = m.getTb009() + "_" + m.getMb001();
		if (wAs.containsKey(wAsKey)) {
			biltowho = m.getTb009() + "_" + wAs.get(wAsKey).getWaaname() + "_" + wAs.get(wAsKey).getWaslocation();
		}
		o.setBiltowho("[" + biltowho + "]");
		o.setBilfromwho("[_" + m.getTa021() + "]");// 目的來源[_生產線]
		// 而外匹配 [儲位負責]
		String bilmuser = "";
		for (String wkey : wKs.keySet()) {
			String wkeyAccount = wkey.split("_")[0];
			String wkeyWarehouse = wkey.split("_")[1];
			String wkeyLocal = wkey.split("_")[2];
			if (!wkey.equals("") && biltowho.indexOf(wkeyLocal) >= 0//
					&& wkeyWarehouse.equals(m.getTb009())) {
				bilmuser += wkeyAccount + "_";
				// break;
			}
		}
		o.setBilmuser(bilmuser);
		return o;
	}

	// 領料類-轉換(Mocte)
	public BasicShippingList shippingOneMocte(BasicShippingList o, Mocte m, String checkSum, //
			Map<String, WarehouseTypeFilter> wTFs, TreeMap<String, WarehouseKeeper> wKs,
			Map<String, WarehouseArea> wAs) {
		// 是否結單?
		int sysstatus = 0;
		if (m.getTe019().equals("Y")) {
			sysstatus = 1;
//			if (o.getBslpngqty().equals(0)) {
//				o.setBslpngqty(m.getTb004());// 已領庫量
//			}
		}
		// 資料匹配
		o.setChecksum(checkSum);
		o.setBslfuser("");
		o.setBslclass(m.getTa026_ta027_ta028().split("-")[0]);// 入庫單[別]
		o.setBslsn(m.getTa026_ta027_ta028().split("-")[1]);// 入庫單[號]
		o.setBslnb(m.getTa026_ta027_ta028().split("-")[2]);// 序號
		o.setBsltype(m.getTk000());// 入庫單
		o.setBslcheckin(sysstatus);// 0=未核單 1=已核單
		o.setBslacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
		o.setBslpnumber(m.getMb001());// 物料號品號
		o.setBslpname(m.getMb002());// 品名
		o.setBslpspecification(m.getMb003());// 規格
		o.setBslpnqty(m.getTb004());// 數量
		o.setBslpnerpqty(m.getTe005());// 數量(領退料數量ERP)
		o.setSysstatus(sysstatus);// 0=尚未結束,1=結案
		o.setSysnote(m.getTe014());// 備註
		o.setBslerpcuser(m.getCreator());// 開單人
		o.setSyshnote(m.getTc007());// 單據備註
		// 單據急迫性
		if (wTFs.containsKey(o.getBslclass())) {
			o.setBslstatus(wTFs.get(o.getBslclass()).getWtfurgency());
		}

		// 預計領料日
		if (m.getTa009() != null) {
			o.setBsledate(Fm_T.toYMDate(m.getTa009()));
		}

		// 而外匹配 [單別]
		o.setBslfromcommand("[" + m.getTa001_ta002() + "*" + m.getTa006() + "*" + m.getTa015() + "]");// 製令單
		o.setBsltocommand("[_]");
		// 而外匹配 [倉別代號+倉別名稱+位置]
		String bilfromwho = m.getTb009() + "_" + m.getTb009() + "_FF-FF-FF-FF";
		String wAsKey = m.getTb009() + "_" + m.getMb001();
		if (wAs.containsKey(wAsKey)) {
			bilfromwho = m.getTb009() + "_" + wAs.get(wAsKey).getWaaname() + "_" + wAs.get(wAsKey).getWaslocation();
		}
		o.setBsltowho("[_" + m.getTa021() + "]");// 目的[_生產線]
		o.setBslfromwho("[" + bilfromwho + "]");// 目的來源[_倉庫]
		// 而外匹配 [儲位負責]
		String bilmuser = "";
		for (String wkey : wKs.keySet()) {
			String wkeyAccount = wkey.split("_")[0];
			String wkeyWarehouse = wkey.split("_")[1];
			String wkeyLocal = wkey.split("_")[2];
			if (!wkey.equals("") && bilfromwho.indexOf(wkeyLocal) >= 0//
					&& wkeyWarehouse.equals(m.getTb009())) {
				bilmuser += wkeyAccount + "_";
				// break;
			}
		}
		o.setBslmuser(bilmuser);
		return o;
	}

	// ============A581 生產入庫單 ============
	// 入料類-轉換(Mocte)
	public BasicIncomingList incomingOneMoctf(BasicIncomingList o, Moctf m, String checkSum, //
			Map<String, WarehouseTypeFilter> wTFs, TreeMap<String, WarehouseKeeper> wKs,
			Map<String, WarehouseArea> wAs) {
		// 是否結單?
		int sysstatus = 0;
		if (m.getTg022().equals("Y")) {
			sysstatus = 1;
//			if (o.getBilpngqty().equals(0)) {
//				o.setBilpngqty(m.getTg011());// 已入庫量
//			}
		}
		// 資料匹配
		o.setChecksum(checkSum);
		o.setBilfuser("");
		o.setBilclass(m.getTg001_tg002_tg003().split("-")[0]);// 入庫單[別]
		o.setBilsn(m.getTg001_tg002_tg003().split("-")[1]);// 入庫單[號]
		o.setBilnb(m.getTg001_tg002_tg003().split("-")[2]);// 序號
		o.setBiltype(m.getTk000());// 入庫單
		o.setBilcheckin(sysstatus);// 0=未核單 1=已核單
		o.setBilacceptance(m.getTg016().equals("1") ? 0 : 1);// 0=未檢驗 1=已檢驗 2=異常
		o.setBilpnumber(m.getMb001());// 物料號品號
		o.setBilpname(m.getMb002());// 品名
		o.setBilpspecification(m.getMb003());// 規格
		o.setBilpnqty(m.getTg011());// 需入庫量
		o.setSysstatus(0);// 未完成
		o.setSysmdate(new Date());
		o.setSysnote(m.getTg020());// 備註
		o.setBilerpcuser(m.getCreator());// 開單人
		o.setSyshnote(m.getTf005());// 單頭備註

		// 而外匹配 [單別]
		String bilfromcommand = "_製令單";
		if (wTFs.containsKey(o.getBilclass())) {
			bilfromcommand = "_" + wTFs.get(o.getBilclass()).getWtfname();
			// 單據急迫性
			o.setBilstatus(wTFs.get(o.getBilclass()).getWtfurgency());
		}
		o.setBilfromcommand("[" + m.getTg014_tg015() + bilfromcommand + "]");// 單據來源 [_製令單]
		o.setBiltocommand("[_]");
		// 而外匹配 [倉別代號+倉別名稱+位置]
		String biltowho = m.getTg010() + "_" + m.getTg010() + "_FF-FF-FF-FF";
		String wAsKey = m.getTg010() + "_" + m.getMb001();
		if (wAs.containsKey(wAsKey)) {
			biltowho = m.getTg010() + "_" + wAs.get(wAsKey).getWaaname() + "_" + wAs.get(wAsKey).getWaslocation();
		}
		o.setBiltowho("[" + biltowho + "]");
		o.setBilfromwho("[_生產線]");// 目的來源[_生產線]
		// 而外匹配 [儲位負責]
		String bilmuser = "";
		for (String wkey : wKs.keySet()) {
			String wkeyAccount = wkey.split("_")[0];
			String wkeyWarehouse = wkey.split("_")[1];
			String wkeyLocal = wkey.split("_")[2];
			if (!wkey.equals("") && biltowho.indexOf(wkeyLocal) >= 0//
					&& wkeyWarehouse.equals(m.getTg010())) {
				bilmuser += wkeyAccount + "_";
				// break;
			}
		}
		o.setBilmuser(bilmuser);
		return o;
	}

	// ============ A591 委外進貨單 ============
	// 入料類-轉換(Mocth)
	public BasicIncomingList incomingOneMocth(BasicIncomingList o, Mocth m, String checkSum, //
			Map<String, WarehouseTypeFilter> wTFs, TreeMap<String, WarehouseKeeper> wKs,
			Map<String, WarehouseArea> wAs) {
		// 是否結單?
		int sysstatus = 0;
		if (m.getTi037().equals("Y")) {
			sysstatus = 1;
//			if (o.getBilpngqty().equals(0)) {
//				o.setBilpngqty(m.getTi007());// 已入庫量
//			}
		}
		// 資料匹配
		o.setChecksum(checkSum);
		o.setBilfuser("");
		o.setBilclass(m.getTi001_ti002_ti003().split("-")[0]);// 入庫單[別]
		o.setBilsn(m.getTi001_ti002_ti003().split("-")[1]);// 入庫單[號]
		o.setBilnb(m.getTi001_ti002_ti003().split("-")[2]);// 序號
		o.setBiltype(m.getTk000());// 入庫單
		o.setBilcheckin(sysstatus);// 0=未核單 1=已核單
		o.setBilacceptance(m.getTi035().equals("1") ? 0 : 1);// 0=未檢驗 1=已檢驗 2=異常
		o.setBilpnumber(m.getMb001());// 物料號品號
		o.setBilpname(m.getMb002());// 品名
		o.setBilpspecification(m.getMb003());// 規格
		o.setBilpnqty(m.getTi007());// 需入庫量
		o.setBiledate(new Date());// 預計入料日
		o.setSysstatus(0);// 未完成
		o.setSysmdate(new Date());
		o.setBilerpcuser(m.getCreator());// 開單人
		// 測試用
//		if (m.getTi001_ti002_ti003().equals("A591-231201002-0001")) {
//			System.out.println(m.getTi001_ti002_ti003());
//		}
		// 而外匹配 [單別]
		String bilfromcommand = "_製令單";
		if (wTFs.containsKey(o.getBilclass())) {
			bilfromcommand = "_" + wTFs.get(o.getBilclass()).getWtfname();
			// 單據急迫性
			o.setBilstatus(wTFs.get(o.getBilclass()).getWtfurgency());
		}
		o.setBilfromcommand("[" + m.getTi013_ti014() + bilfromcommand + "]");// 單據來源 [_製令單]
		o.setBiltocommand("[_]");
		// 而外匹配 [倉別代號+倉別名稱+位置]
		String biltowho = m.getTi009() + "_" + m.getTi009() + "_FF-FF-FF-FF";
		String wAsKey = m.getTi009() + "_" + m.getMb001();
		if (wAs.containsKey(wAsKey)) {
			biltowho = m.getTi009() + "_" + wAs.get(wAsKey).getWaaname() + "_" + wAs.get(wAsKey).getWaslocation();
		}
		o.setBiltowho("[" + biltowho + "]");
		o.setBilfromwho("[_委外生產線]");// 目的來源[_生產線]
		// 而外匹配 [儲位負責]
		String bilmuser = "";
		for (String wkey : wKs.keySet()) {
			String wkeyAccount = wkey.split("_")[0];
			String wkeyWarehouse = wkey.split("_")[1];
			String wkeyLocal = wkey.split("_")[2];
			if (!wkey.equals("") && biltowho.indexOf(wkeyLocal) >= 0//
					&& wkeyWarehouse.equals(m.getTi009())) {
				bilmuser += wkeyAccount + "_";
				// break;
			}
		}
		o.setBilmuser(bilmuser);
		return o;
	}

	// ============ A131 庫存借出單/ A141 庫存借入單 ============
	// 入料類-轉換(Invtg)
	public BasicIncomingList incomingOneInvtg(BasicIncomingList o, Invtg m, String checkSum, //
			Map<String, WarehouseTypeFilter> wTFs, TreeMap<String, WarehouseKeeper> wKs,
			Map<String, WarehouseArea> wAs) {
		// 是否結單?
		int sysstatus = 0;
		if (m.getTg022().equals("Y")) {
			sysstatus = 1;
//			if (o.getBilpngqty().equals(0)) {
//				o.setBilpngqty(m.getTg009());// 已入庫量
//			}
		}
		// 資料匹配
		o.setChecksum(checkSum);
		o.setBilfuser("");
		o.setBilclass(m.getTg001_tg002_tg003().split("-")[0].replaceAll("\\s", ""));// 借入庫單[別]
		o.setBilsn(m.getTg001_tg002_tg003().split("-")[1].replaceAll("\\s", ""));// 借入庫單[號]
		o.setBilnb(m.getTg001_tg002_tg003().split("-")[2].replaceAll("\\s", ""));// 序號
		o.setBiltype(m.getTk000());// 入庫單
		o.setBilcheckin(sysstatus);// 0=未核單 1=已核單
		o.setBilacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
		o.setBilpnumber(m.getMb001());// 物料號品號
		o.setBilpname(m.getMb002());// 品名
		o.setBilpspecification(m.getMb003());// 規格
		o.setBilpnqty(m.getTg009());// 數量
		o.setBiledate(new Date());// 預計入料日
		o.setSysstatus(0);// 未完成
		o.setSysmdate(new Date());
		o.setBilerpcuser(m.getCreator());// 開單人
		if (wTFs.containsKey(o.getBilclass())) {
			// 單據急迫性
			o.setBilstatus(wTFs.get(o.getBilclass()).getWtfurgency());
		}
		// 而外匹配 [單別]
		o.setBilfromcommand("[_]");// 單據來源 [_]
		o.setBiltocommand("[_]");// 單據對象 [_]
		// 而外匹配 [倉別代號+倉別名稱+位置]
		String biltowho = m.getTg008() + "_" + m.getTg008() + "_FF-FF-FF-FF";
		String wAsKey = m.getTg008() + "_" + m.getMb001();
		if (wAs.containsKey(wAsKey)) {
			biltowho = m.getTg008() + "_" + wAs.get(wAsKey).getWaaname() + "_" + wAs.get(wAsKey).getWaslocation();
		}
		o.setBiltowho("[" + biltowho + "]");
		o.setBilfromwho("[" + m.getMb032() + "_" + m.getMa002() + "]");// 目的來源[供應商]
		// 而外匹配 [儲位負責]
		String bilmuser = "";
		for (String wkey : wKs.keySet()) {
			String wkeyAccount = wkey.split("_")[0];
			String wkeyWarehouse = wkey.split("_")[1];
			String wkeyLocal = wkey.split("_")[2];
			if (!wkey.equals("") && biltowho.indexOf(wkeyLocal) >= 0//
					&& wkeyWarehouse.equals(m.getTg008())) {
				bilmuser += wkeyAccount + "_";
				// break;
			}
		}
		o.setBilmuser(bilmuser);
		return o;
	}

	// 領料類-轉換(Invtg)
	public BasicShippingList shippingOneInvtg(BasicShippingList o, Invtg m, String checkSum, //
			Map<String, WarehouseTypeFilter> wTFs, TreeMap<String, WarehouseKeeper> wKs,
			Map<String, WarehouseArea> wAs) {
		// 是否結單?
		int sysstatus = 0;
		if (m.getTg022().equals("Y")) {
			sysstatus = 1;
//			if (o.getBslpngqty().equals(0)) {
//				o.setBslpngqty(m.getTg009());// 已領庫量
//			}
		}
		// 資料匹配
		o.setChecksum(checkSum);
		o.setBslfuser("");
		o.setBslclass(m.getTg001_tg002_tg003().split("-")[0]);// 借出庫單[別]
		o.setBslsn(m.getTg001_tg002_tg003().split("-")[1]);// 借出庫單[號]
		o.setBslnb(m.getTg001_tg002_tg003().split("-")[2]);// 序號
		o.setBsltype(m.getTk000());// 入庫單
		o.setBslcheckin(sysstatus);// 0=未核單 1=已核單
		o.setBslacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
		o.setBslpnumber(m.getMb001());// 物料號品號
		o.setBslpname(m.getMb002());// 品名
		o.setBslpspecification(m.getMb003());// 規格
		o.setBslpnqty(m.getTg009());// 數量
		o.setBsledate(new Date());// 預計日
		o.setSysstatus(sysstatus);// 未完成
		o.setSysmdate(new Date());
		o.setBslerpcuser(m.getCreator());// 開單人

		// 單據急迫性
		if (wTFs.containsKey(o.getBslclass())) {
			o.setBslstatus(wTFs.get(o.getBslclass()).getWtfurgency());
		}
		// 而外匹配 [單別]
		o.setBslfromcommand("[_]");//
		o.setBsltocommand("[_]");//
		// 而外匹配 [倉別代號+倉別名稱+位置]
		String bilfromwho = m.getTg007() + "_" + m.getTg007() + "_FF-FF-FF-FF";
		String wAsKey = m.getTg007() + "_" + m.getMb001();
		if (wAs.containsKey(wAsKey)) {
			bilfromwho = m.getTg007() + "_" + wAs.get(wAsKey).getWaaname() + "_" + wAs.get(wAsKey).getWaslocation();
		}
		o.setBsltowho("[_" + m.getTf015() + "]");// 目的[_借出對象]
		o.setBslfromwho("[" + bilfromwho + "]");// 目的來源[_倉庫]
		// 而外匹配 [儲位負責]
		String bilmuser = "";
		for (String wkey : wKs.keySet()) {
			String wkeyAccount = wkey.split("_")[0];
			String wkeyWarehouse = wkey.split("_")[1];
			String wkeyLocal = wkey.split("_")[2];
			if (!wkey.equals("") && bilfromwho.indexOf(wkeyLocal) >= 0//
					&& wkeyWarehouse.equals(m.getTg007())) {
				bilmuser += wkeyAccount + "_";
				// break;
			}
		}
		o.setBslmuser(bilmuser);
		return o;
	}

	// ============ 借出歸還A151/借入歸還單A161 ============
	// 入料類-轉換(Invth)
	public BasicIncomingList incomingOneInvth(BasicIncomingList o, Invth m, String checkSum, //
			Map<String, WarehouseTypeFilter> wTFs, TreeMap<String, WarehouseKeeper> wKs,
			Map<String, WarehouseArea> wAs) {
		// 是否結單?
		int sysstatus = 0;
		if (m.getTh027().equals("N") || m.getTh027().equals("3")) {
			sysstatus = 1;
//			if (o.getBilpngqty().equals(0)) {
//				o.setBilpngqty(m.getTi009());// 已入庫量
//			}
		}
		// 資料匹配
		o.setChecksum(checkSum);
		o.setBilfuser("");
		o.setBilclass(m.getTi001_ti002_ti003().split("-")[0]);// 借入庫單[別]
		o.setBilsn(m.getTi001_ti002_ti003().split("-")[1]);// 借入庫單[號]
		o.setBilnb(m.getTi001_ti002_ti003().split("-")[2]);// 序號
		o.setBiltype(m.getTk000());// 入庫單
		o.setBilcheckin(sysstatus);// 0=未核單 1=已核單
		o.setBilacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
		o.setBilpnumber(m.getMb001());// 物料號品號
		o.setBilpname(m.getMb002());// 品名
		o.setBilpspecification(m.getMb003());// 規格
		o.setBilpnqty(m.getTi009());// 數量
		o.setBiledate(new Date());// 預計入料日
		o.setSysstatus(0);// 未完成
		o.setSysmdate(new Date());
		o.setBilerpcuser(m.getCreator());// 開單人

		// 而外匹配 [單別]
		String bilfromcommand = "_借出單";
		if (wTFs.containsKey(o.getBilclass())) {
			bilfromcommand = "_" + wTFs.get(o.getBilclass()).getWtfname();
			// 單據急迫性
			o.setBilstatus(wTFs.get(o.getBilclass()).getWtfurgency());
		}
		o.setBilfromcommand("[" + m.getTi014_ti015_ti016() + bilfromcommand + "]");// 單據來源 [_借出單]
		o.setBiltocommand("[_]");
		// 而外匹配 [倉別代號+倉別名稱+位置]
		String biltowho = m.getTi008() + "_" + m.getTi008() + "_FF-FF-FF-FF";
		String wAsKey = m.getTi008() + "_" + m.getMb001();
		if (wAs.containsKey(wAsKey)) {
			biltowho = m.getTi008() + "_" + wAs.get(wAsKey).getWaaname() + "_" + wAs.get(wAsKey).getWaslocation();
		}
		o.setBiltowho("[" + biltowho + "]");
		o.setBilfromwho("[" + m.getMb032() + "_" + m.getMa002() + "]");// 目的來源[供應商]
		// 而外匹配 [儲位負責]
		String bilmuser = "";
		for (String wkey : wKs.keySet()) {
			String wkeyAccount = wkey.split("_")[0];
			String wkeyWarehouse = wkey.split("_")[1];
			String wkeyLocal = wkey.split("_")[2];
			if (!wkey.equals("") && biltowho.indexOf(wkeyLocal) >= 0//
					&& wkeyWarehouse.equals(m.getTi008())) {
				bilmuser += wkeyAccount + "_";
				// break;
			}
		}
		o.setBilmuser(bilmuser);
		return o;
	}

	// 領料類-轉換(Invth)
	public BasicShippingList shippingOneInvth(BasicShippingList o, Invth m, String checkSum, //
			Map<String, WarehouseTypeFilter> wTFs, TreeMap<String, WarehouseKeeper> wKs,
			Map<String, WarehouseArea> wAs) {
		// 是否結單?
		int sysstatus = 0;
		if (m.getTi022().equals("Y")) {
			sysstatus = 1;
//			if (o.getBslpngqty().equals(0)) {
//				o.setBslpngqty(m.getTi009());// 已領庫量
//			}
		}
		// 資料匹配
		o.setChecksum(checkSum);
		o.setBslfuser("");
		o.setBslclass(m.getTi001_ti002_ti003().split("-")[0].replaceAll("\\s", ""));// 借出庫單[別]
		o.setBslsn(m.getTi001_ti002_ti003().split("-")[1].replaceAll("\\s", ""));// 借出庫單[號]
		o.setBslnb(m.getTi001_ti002_ti003().split("-")[2].replaceAll("\\s", ""));// 序號
		o.setBsltype(m.getTk000());// 入庫單
		o.setBslcheckin(sysstatus);// 0=未核單 1=已核單
		o.setBslacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
		o.setBslpnumber(m.getMb001());// 物料號品號
		o.setBslpname(m.getMb002());// 品名
		o.setBslpspecification(m.getMb003());// 規格
		o.setBslpnqty(m.getTi009());// 數量
		o.setBsledate(new Date());// 預計日
		o.setSysstatus(sysstatus);// 未完成
		o.setSysmdate(new Date());
		o.setBslerpcuser(m.getCreator());// 開單人

		// 而外匹配 [單別]
		String bilfromcommand = "_借入單";
		if (wTFs.containsKey(o.getBslclass())) {
			bilfromcommand = "_" + wTFs.get(o.getBslclass()).getWtfname();
			// 單據急迫性
			o.setBslstatus(wTFs.get(o.getBslclass()).getWtfurgency());
		}
		o.setBslfromcommand("[" + m.getTi014_ti015_ti016() + bilfromcommand + "]");// 單據來源 [_借入單]
		o.setBsltocommand("[_]");//
		// 而外匹配 [倉別代號+倉別名稱+位置]
		String bilfromwho = m.getTi007() + "_" + m.getTi007() + "_FF-FF-FF-FF";
		String wAsKey = m.getTi007() + "_" + m.getMb001();
		if (wAs.containsKey(wAsKey)) {
			bilfromwho = m.getTi007() + "_" + wAs.get(wAsKey).getWaaname() + "_" + wAs.get(wAsKey).getWaslocation();
		}
		o.setBsltowho("[" + "_" + m.getTh006() + "]");// 目的[_歸還對象]
		o.setBslfromwho("[" + bilfromwho + "]");// 目的來源[_倉庫]
		// 而外匹配 [儲位負責]
		String bilmuser = "";
		for (String wkey : wKs.keySet()) {
			String wkeyAccount = wkey.split("_")[0];
			String wkeyWarehouse = wkey.split("_")[1];
			String wkeyLocal = wkey.split("_")[2];
			if (!wkey.equals("") && bilfromwho.indexOf(wkeyLocal) >= 0//
					&& wkeyWarehouse.equals(m.getTi007())) {
				bilmuser += wkeyAccount + "_";
				// break;
			}
		}
		o.setBslmuser(bilmuser);
		return o;
	}

	// ============ A111 費用領料單/ A112 費用退料單/ A119 料號調整單/ A121 倉庫調撥單 ============
	// 入料類-轉換(Invta)
	public BasicIncomingList incomingOneInvta(BasicIncomingList o, Invta m, String checkSum, //
			Map<String, WarehouseTypeFilter> wTFs, TreeMap<String, WarehouseKeeper> wKs,
			Map<String, WarehouseArea> wAs) {
		// 是否結單?
		int sysstatus = 0;
		if (m.getTb018().equals("Y")) {
			sysstatus = 1;
//			if (o.getBilpngqty().equals(0)) {
//				o.setBilpngqty(m.getTb007());// 已入庫量
//			}
		}
		// 資料匹配
		o.setChecksum(checkSum);
		o.setBilfuser("");
		o.setBilclass(m.getTb001_tb002_tb003().split("-")[0].replaceAll("\\s", ""));// 借入庫單[別]
		o.setBilsn(m.getTb001_tb002_tb003().split("-")[1].replaceAll("\\s", ""));// 借入庫單[號]
		o.setBilnb(m.getTb001_tb002_tb003().split("-")[2].replaceAll("\\s", ""));// 序號
		o.setBiltype(m.getTk000());// 入庫單
		o.setBilcheckin(sysstatus);// 0=未核單 1=已核單
		o.setBilacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
		o.setBilpnumber(m.getMb001().replaceAll("\\s", ""));// 物料號品號
		o.setBilpname(m.getMb002());// 品名
		o.setBilpspecification(m.getMb003());// 規格
		o.setBilpnqty(m.getTb007());// 數量
		o.setBiledate(new Date());// 預計入料日
		o.setSysstatus(0);// 未完成
		o.setSysmdate(new Date());
		o.setSyshnote(m.getTa005());// 單頭備註
		o.setBilerpcuser(m.getCreator());// 開單人
		// 單據急迫性
		if (wTFs.containsKey(o.getBilclass())) {
			o.setBilstatus(wTFs.get(o.getBilclass()).getWtfurgency());
		}
		// 而外匹配 [單別]
		o.setBilfromcommand("[_]");// 單據來源 [_]
		o.setBiltocommand("[_]");
		// 而外匹配 [倉別代號+倉別名稱+位置]
		String biltowho = m.getTb013() + "_" + m.getTb013() + "_FF-FF-FF-FF";
		// String bilfromwho = m.getTb012() + "_unfound_FF-FF-FF-FF";
		String wAsKey = m.getTb013() + "_" + m.getMb001();
		// 測試用
//		if (m.getTb001_tb002_tb003().equals("A119-231130001-0002")) {
//			System.out.println("A119-231130001-002");
//		}
		// 對象
		if (wAs.containsKey(wAsKey)) {
			biltowho = m.getTb013() + "_" + wAs.get(wAsKey).getWaaname() + "_" + wAs.get(wAsKey).getWaslocation();
		}
		// 來源
//		wAsKey = m.getTb012() + "_" + m.getMb001();
//		if (wAs.containsKey(wAsKey)) {
//			bilfromwho = m.getTb012() + "_" + wAs.get(wAsKey).getWaaname() + "_" + wAs.get(wAsKey).getWaslocation();
//		}
		o.setBiltowho("[" + biltowho + "]");// 目的對象[_倉庫]
		o.setBilfromwho("[_]");// 目的來源[_倉庫]
		// 而外匹配 [儲位負責]
		String bilmuser = "";
		for (String wkey : wKs.keySet()) {
			String wkeyAccount = wkey.split("_")[0];
			String wkeyWarehouse = wkey.split("_")[1];
			String wkeyLocal = wkey.split("_")[2];
			if (!wkey.equals("") && biltowho.indexOf(wkeyLocal) >= 0//
					&& wkeyWarehouse.equals(m.getTb013())) {
				bilmuser += wkeyAccount + "_";
				// break;
			}
		}
		o.setBilmuser(bilmuser);
		return o;
	}

	// 領料類-轉換(Invta)
	public BasicShippingList shippingOneInvta(BasicShippingList o, Invta m, String checkSum, //
			Map<String, WarehouseTypeFilter> wTFs, TreeMap<String, WarehouseKeeper> wKs,
			Map<String, WarehouseArea> wAs) {
		// 是否結單?
		int sysstatus = 0;
		if (m.getTb018().equals("Y")) {
			sysstatus = 1;
//			if (o.getBslpngqty().equals(0)) {
//				o.setBslpngqty(m.getTb007());// 已領庫量
//			}
		}
		// 資料匹配
		o.setChecksum(checkSum);
		o.setBslfuser("");
		o.setBslclass(m.getTb001_tb002_tb003().split("-")[0].replaceAll("\\s", ""));// 借出庫單[別]
		o.setBslsn(m.getTb001_tb002_tb003().split("-")[1].replaceAll("\\s", ""));// 借出庫單[號]
		o.setBslnb(m.getTb001_tb002_tb003().split("-")[2].replaceAll("\\s", ""));// 序號
		o.setBsltype(m.getTk000());// 入庫單
		o.setBslcheckin(sysstatus);// 0=未核單 1=已核單
		o.setBslacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
		o.setBslpnumber(m.getMb001().replaceAll("\\s", ""));// 物料號品號
		o.setBslpname(m.getMb002());// 品名
		o.setBslpspecification(m.getMb003());// 規格
		o.setBslpnqty(m.getTb007());// 數量
		o.setSysstatus(0);// 未完成
		o.setSysmdate(new Date());
		o.setSyshnote(m.getTa005());// 單頭備註
		o.setBslerpcuser(m.getCreator());// 開單人
		// 單據急迫性
		if (wTFs.containsKey(o.getBslclass())) {
			o.setBslstatus(wTFs.get(o.getBslclass()).getWtfurgency());
		}
		// 而外匹配 [單別]
		o.setBslfromcommand("[_]");// 單據來源 [_]
		o.setBsltocommand("[_]");//
		// 而外匹配 [倉別代號+倉別名稱+位置]
		String bilfromwho = m.getTb012() + "_" + m.getTb012() + "_FF-FF-FF-FF";
		// String biltowho = m.getTb013() + "_unfound_FF-FF-FF-FF";
		String wAsKey = m.getTb013() + "_" + m.getMb001();
//		if (wAs.containsKey(wAsKey)) {
//			biltowho = m.getTb013() + "_" + wAs.get(wAsKey).getWaaname() + "_" + wAs.get(wAsKey).getWaslocation();
//		}
		wAsKey = m.getTb012() + "_" + m.getMb001();
		if (wAs.containsKey(wAsKey)) {
			bilfromwho = m.getTb012() + "_" + wAs.get(wAsKey).getWaaname() + "_" + wAs.get(wAsKey).getWaslocation();
		}
		o.setBsltowho("[_]");// 目的來源[_倉庫]
		o.setBslfromwho("[" + bilfromwho + "]");// 目的來源[_倉庫]
		// 而外匹配 [儲位負責]
		String bilmuser = "";
		for (String wkey : wKs.keySet()) {
			String wkeyAccount = wkey.split("_")[0];
			String wkeyWarehouse = wkey.split("_")[1];
			String wkeyLocal = wkey.split("_")[2];
			if (!wkey.equals("") && bilfromwho.indexOf(wkeyLocal) >= 0//
					&& wkeyWarehouse.equals(m.getTb012())) {
				bilmuser += wkeyAccount + "_";
				// break;
			}
		}
		o.setBslmuser(bilmuser);
		return o;
	}

	// ============ -OK 組合單/A421 ============
	// 入料類-轉換(Bomtd)
	public BasicIncomingList incomingOneBomtd(BasicIncomingList o, Bomtd m, String checkSum, //
			Map<String, WarehouseTypeFilter> wTFs, TreeMap<String, WarehouseKeeper> wKs,
			Map<String, WarehouseArea> wAs) {
		// 是否結單?
		int sysstatus = 0;
		if (m.getTd016().equals("Y")) {
			sysstatus = 1;
//			if (o.getBilpngqty().equals(0)) {
//				o.setBilpngqty(m.getTd007());// 已入庫量
//			}
		}
		// 資料匹配
		o.setChecksum(checkSum);
		o.setBilfuser("");
		o.setBilclass(m.getTe001_te002_te003().split("-")[0].replaceAll("\\s", ""));// 借入庫單[別]
		o.setBilsn(m.getTe001_te002_te003().split("-")[1].replaceAll("\\s", ""));// 借入庫單[號]
		o.setBilnb(m.getTe001_te002_te003().split("-")[2].replaceAll("\\s", ""));// 序號
		o.setBiltype(m.getTk000());// 入庫單
		o.setBilcheckin(sysstatus);// 0=未核單 1=已核單
		o.setBilacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
		o.setBilpnumber(m.getMb001());// 物料號品號
		o.setBilpname(m.getMb002());// 品名
		o.setBilpspecification(m.getMb003());// 規格
		o.setBilpnqty(m.getTd007());// 數量
		o.setBiledate(new Date());// 預計入料日
		o.setSysstatus(0);// 未完成
		o.setSysmdate(new Date());
		o.setBilerpcuser(m.getCreator());// 開單人
		if (wTFs.containsKey(o.getBilclass())) {
			// 單據急迫性
			o.setBilstatus(wTFs.get(o.getBilclass()).getWtfurgency());
		}
		// 而外匹配 [單別]
		o.setBilfromcommand("[_]");// 單據來源 [_]
		o.setBiltocommand("[_]");// 單據對象 [_]
		// 而外匹配 [倉別代號+倉別名稱+位置]
		String biltowho = m.getTd010() + "_" + m.getTd010() + "_FF-FF-FF-FF";
		String wAsKey = m.getTd010() + "_" + m.getMb001();
		if (wAs.containsKey(wAsKey)) {
			biltowho = m.getTd010() + "_" + wAs.get(wAsKey).getWaaname() + "_" + wAs.get(wAsKey).getWaslocation();
		}
		o.setBiltowho("[" + biltowho + "]");
		o.setBilfromwho("[" + m.getMb032() + "_" + m.getMa002() + "]");// 目的來源[供應商]
		// 而外匹配 [儲位負責]
		String bilmuser = "";
		for (String wkey : wKs.keySet()) {
			String wkeyAccount = wkey.split("_")[0];
			String wkeyWarehouse = wkey.split("_")[1];
			String wkeyLocal = wkey.split("_")[2];
			if (!wkey.equals("") && biltowho.indexOf(wkeyLocal) >= 0//
					&& wkeyWarehouse.equals(m.getTd010())) {
				bilmuser += wkeyAccount + "_";
				// break;
			}
		}
		o.setBilmuser(bilmuser);
		return o;
	}

	// 領料類-轉換(Bomtd)
	public BasicShippingList shippingOneBomtd(BasicShippingList o, Bomtd m, String checkSum, //
			Map<String, WarehouseTypeFilter> wTFs, TreeMap<String, WarehouseKeeper> wKs,
			Map<String, WarehouseArea> wAs) {
		// 是否結單?
		int sysstatus = 0;
		if (m.getTe010().equals("Y")) {
			sysstatus = 1;
//			if (o.getBslpngqty().equals(0)) {
//				o.setBslpngqty(m.getTe008());// 已領庫量
//			}
		}
		// 資料匹配
		o.setChecksum(checkSum);
		o.setBslfuser("");
		o.setBslclass(m.getTe001_te002_te003().split("-")[0]);// 借出庫單[別]
		o.setBslsn(m.getTe001_te002_te003().split("-")[1]);// 借出庫單[號]
		o.setBslnb(m.getTe001_te002_te003().split("-")[2]);// 序號
		o.setBsltype(m.getTk000());// 入庫單
		o.setBslcheckin(sysstatus);// 0=未核單 1=已核單
		o.setBslacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
		o.setBslpnumber(m.getMb001());// 物料號品號
		o.setBslpname(m.getMb002());// 品名
		o.setBslpspecification(m.getMb003());// 規格
		o.setBslpnqty(m.getTe008());// 數量
		o.setBsledate(new Date());// 預計日
		o.setSysstatus(sysstatus);// 未完成
		o.setSysmdate(new Date());
		o.setBslerpcuser(m.getCreator());// 開單人

		// 單據急迫性
		if (wTFs.containsKey(o.getBslclass())) {
			o.setBslstatus(wTFs.get(o.getBslclass()).getWtfurgency());
		}
		// 而外匹配 [單別]
		o.setBslfromcommand("[_]");//
		o.setBsltocommand("[_]");//
		// 而外匹配 [倉別代號+倉別名稱+位置]
		String bilfromwho = m.getTe007() + "_" + m.getTe007() + "_FF-FF-FF-FF";
		String wAsKey = m.getTe007() + "_" + m.getMb001();
		if (wAs.containsKey(wAsKey)) {
			bilfromwho = m.getTe007() + "_" + wAs.get(wAsKey).getWaaname() + "_" + wAs.get(wAsKey).getWaslocation();
		}
		o.setBsltowho("[_" + m.getTd004() + "]");// 目的[_對象]
		o.setBslfromwho("[" + bilfromwho + "]");// 目的來源[_倉庫]
		// 而外匹配 [儲位負責]
		String bilmuser = "";
		for (String wkey : wKs.keySet()) {
			String wkeyAccount = wkey.split("_")[0];
			String wkeyWarehouse = wkey.split("_")[1];
			String wkeyLocal = wkey.split("_")[2];
			if (!wkey.equals("") && bilfromwho.indexOf(wkeyLocal) >= 0//
					&& wkeyWarehouse.equals(m.getTe007())) {
				bilmuser += wkeyAccount + "_";
				// break;
			}
		}
		o.setBslmuser(bilmuser);
		return o;
	}

	// ============ -OK 拆解單/A431 ============
	// 入料類-轉換(Bomtf)
	public BasicIncomingList incomingOneBomtf(BasicIncomingList o, Bomtf m, String checkSum, //
			Map<String, WarehouseTypeFilter> wTFs, TreeMap<String, WarehouseKeeper> wKs,
			Map<String, WarehouseArea> wAs) {
		// 是否結單?
		int sysstatus = 0;
		if (m.getTg010().equals("Y")) {
			sysstatus = 1;
//			if (o.getBilpngqty().equals(0)) {
//				o.setBilpngqty(m.getTg008());// 已入庫量
//			}
		}
		// 資料匹配
		o.setChecksum(checkSum);
		o.setBilfuser("");
		o.setBilclass(m.getTg001_tg002_tg003().split("-")[0].replaceAll("\\s", ""));// 借入庫單[別]
		o.setBilsn(m.getTg001_tg002_tg003().split("-")[1].replaceAll("\\s", ""));// 借入庫單[號]
		o.setBilnb(m.getTg001_tg002_tg003().split("-")[2].replaceAll("\\s", ""));// 序號
		o.setBiltype(m.getTk000());// 入庫單
		o.setBilcheckin(sysstatus);// 0=未核單 1=已核單
		o.setBilacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
		o.setBilpnumber(m.getMb001());// 物料號品號
		o.setBilpname(m.getMb002());// 品名
		o.setBilpspecification(m.getMb003());// 規格
		o.setBilpnqty(m.getTg008());// 數量
		o.setBiledate(new Date());// 預計入料日
		o.setSysstatus(0);// 未完成
		o.setSysmdate(new Date());
		o.setBilerpcuser(m.getCreator());// 開單人
		if (wTFs.containsKey(o.getBilclass())) {
			// 單據急迫性
			o.setBilstatus(wTFs.get(o.getBilclass()).getWtfurgency());
		}
		// 而外匹配 [單別]
		o.setBilfromcommand("[_]");// 單據來源 [_]
		o.setBiltocommand("[_]");// 單據對象 [_]
		// 而外匹配 [倉別代號+倉別名稱+位置]
		String biltowho = m.getTg007() + "_" + m.getTg007() + "_FF-FF-FF-FF";
		String wAsKey = m.getTg007() + "_" + m.getMb001();
		if (wAs.containsKey(wAsKey)) {
			biltowho = m.getTg007() + "_" + wAs.get(wAsKey).getWaaname() + "_" + wAs.get(wAsKey).getWaslocation();
		}
		o.setBiltowho("[" + biltowho + "]");
		o.setBilfromwho("[" + m.getMb032() + "_" + m.getMa002() + "]");// 目的來源[供應商]
		// 而外匹配 [儲位負責]
		String bilmuser = "";
		for (String wkey : wKs.keySet()) {
			String wkeyAccount = wkey.split("_")[0];
			String wkeyWarehouse = wkey.split("_")[1];
			String wkeyLocal = wkey.split("_")[2];
			if (!wkey.equals("") && biltowho.indexOf(wkeyLocal) >= 0//
					&& wkeyWarehouse.equals(m.getTg007())) {
				bilmuser += wkeyAccount + "_";
				// break;
			}
		}
		o.setBilmuser(bilmuser);
		return o;
	}

	// ============ -OK 銷貨單 A231/A232 ============
	// 領料類-轉換(Mocte)
	public BasicShippingList shippingOneCopth(BasicShippingList o, Copth m, String checkSum, //
			Map<String, WarehouseTypeFilter> wTFs, TreeMap<String, WarehouseKeeper> wKs,
			Map<String, WarehouseArea> wAs) {
		// 是否結單?
		int sysstatus = 0;
		if (m.getTg047().equals("Y")) {
			sysstatus = 1;
//			if (o.getBslpngqty().equals(0)) {
//				o.setBslpngqty(m.getTb004());// 已領庫量
//			}
		}
		// 資料匹配
		o.setChecksum(checkSum);
		o.setBslfuser("");
		o.setBslclass(m.getTh001_th002_th003().split("-")[0]);// 入庫單[別]
		o.setBslsn(m.getTh001_th002_th003().split("-")[1]);// 入庫單[號]
		o.setBslnb(m.getTh001_th002_th003().split("-")[2]);// 序號
		o.setBsltype(m.getTk000());// 入庫單
		o.setBslcheckin(sysstatus);// 0=未核單 1=已核單
		o.setBslacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
		o.setBslpnumber(m.getMb001());// 物料號品號
		o.setBslpname(m.getMb002());// 品名
		o.setBslpspecification(m.getMb003());// 規格
		o.setBslpnqty(m.getTh008());// 數量
		o.setBslpnerpqty(0);// 數量(領退料數量ERP)
		o.setSysstatus(sysstatus);// 0=尚未結束,1=結案
		o.setSysnote(m.getTh018());// 備註
		o.setBslerpcuser(m.getCreator());// 開單人
		o.setSyshnote(m.getTg020());// 單據備註
		// 單據急迫性
		if (wTFs.containsKey(o.getBslclass())) {
			o.setBslstatus(wTFs.get(o.getBslclass()).getWtfurgency());
		}

		// 預計領料日
		if (m.getTg042() != null) {
			o.setBsledate(Fm_T.toYMDate(m.getTg042()));
		}

		// 而外匹配 [單別]
		o.setBslfromcommand("[_]");//
		o.setBsltocommand("[_]");
		// 而外匹配 [倉別代號+倉別名稱+位置]
		String bilfromwho = m.getTh007() + "_" + m.getTh007() + "_FF-FF-FF-FF";
		String wAsKey = m.getTh007() + "_" + m.getMb001();
		if (wAs.containsKey(wAsKey)) {
			bilfromwho = m.getTh007() + "_" + wAs.get(wAsKey).getWaaname() + "_" + wAs.get(wAsKey).getWaslocation();
		}
		o.setBsltowho("[_" + m.getTg007() + "]");// 目的[_]
		o.setBslfromwho("[" + bilfromwho + "]");// 目的來源[_倉庫]
		// 而外匹配 [儲位負責]
		String bilmuser = "";
		for (String wkey : wKs.keySet()) {
			String wkeyAccount = wkey.split("_")[0];
			String wkeyWarehouse = wkey.split("_")[1];
			String wkeyLocal = wkey.split("_")[2];
			if (!wkey.equals("") && bilfromwho.indexOf(wkeyLocal) >= 0//
					&& wkeyWarehouse.equals(m.getTh007())) {
				bilmuser += wkeyAccount + "_";
				// break;
			}
		}
		o.setBslmuser(bilmuser);
		return o;
	}

	// 領料類-轉換(Bomtd)
	public BasicShippingList shippingOneBomtf(BasicShippingList o, Bomtf m, String checkSum, //
			Map<String, WarehouseTypeFilter> wTFs, TreeMap<String, WarehouseKeeper> wKs,
			Map<String, WarehouseArea> wAs) {
		// 是否結單?
		int sysstatus = 0;
		if (m.getTg010().equals("Y")) {
			sysstatus = 1;
//			if (o.getBslpngqty().equals(0)) {
//				o.setBslpngqty(m.getTf007());// 已領庫量
//			}
		}
		// 資料匹配
		o.setChecksum(checkSum);
		o.setBslfuser("");
		o.setBslclass(m.getTg001_tg002_tg003().split("-")[0]);// 借出庫單[別]
		o.setBslsn(m.getTg001_tg002_tg003().split("-")[1]);// 借出庫單[號]
		o.setBslnb(m.getTg001_tg002_tg003().split("-")[2]);// 序號
		o.setBsltype(m.getTk000());// 入庫單
		o.setBslcheckin(sysstatus);// 0=未核單 1=已核單
		o.setBslacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
		o.setBslpnumber(m.getMb001());// 物料號品號
		o.setBslpname(m.getMb002());// 品名
		o.setBslpspecification(m.getMb003());// 規格
		o.setBslpnqty(m.getTf007());// 數量
		o.setBsledate(new Date());// 預計日
		o.setSysstatus(sysstatus);// 未完成
		o.setSysmdate(new Date());
		o.setBslerpcuser(m.getCreator());// 開單人

		// 單據急迫性
		if (wTFs.containsKey(o.getBslclass())) {
			o.setBslstatus(wTFs.get(o.getBslclass()).getWtfurgency());
		}
		// 而外匹配 [單別]
		o.setBslfromcommand("[_]");//
		o.setBsltocommand("[_]");//
		// 而外匹配 [倉別代號+倉別名稱+位置]
		String bilfromwho = m.getTf008() + "_" + m.getTf008() + "_FF-FF-FF-FF";
		String wAsKey = m.getTf008() + "_" + m.getMb001();
		if (wAs.containsKey(wAsKey)) {
			bilfromwho = m.getTf008() + "_" + wAs.get(wAsKey).getWaaname() + "_" + wAs.get(wAsKey).getWaslocation();
		}
		o.setBsltowho("[_]");// 目的[_對象]
		o.setBslfromwho("[" + bilfromwho + "]");// 目的來源[_倉庫]
		// 而外匹配 [儲位負責]
		String bilmuser = "";
		for (String wkey : wKs.keySet()) {
			String wkeyAccount = wkey.split("_")[0];
			String wkeyWarehouse = wkey.split("_")[1];
			String wkeyLocal = wkey.split("_")[2];
			if (!wkey.equals("") && bilfromwho.indexOf(wkeyLocal) >= 0//
					&& wkeyWarehouse.equals(m.getTf008())) {
				bilmuser += wkeyAccount + "_";
				// break;
			}
		}
		o.setBslmuser(bilmuser);
		return o;
	}

	// 外包-生產管理
	public ScheduleOutsourcer scheduleOutsourcerOne(ScheduleOutsourcer o, MoctaScheduleOutsourcer m, String checkSum) {
		// 資料匹配

		// 年-週期
		if (m.getTa009() != null || !m.getTa009().equals("")) {
			Date soywdate = Fm_T.toYMDate(m.getTa009());
			String week = String.format("%02d", Fm_T.getWeek(soywdate));
			int year = Fm_T.getYear(soywdate);
			o.setSoywdate(year + "-W" + week);
		} else {
			o.setSoywdate("9999-W99");
		}
		o.setSoodate(m.getTa009() != null ? Fm_T.to_y_M_d(Fm_T.toYMDate(m.getTa009())) : "99991201");// 預計開工時間
		o.setSofdate(m.getTa010() != null ? Fm_T.to_y_M_d(Fm_T.toYMDate(m.getTa010())) : "99991201");// 預計完工時間
		o.setSonb(m.getTa001_ta002());// --製令單
		o.setSopnb(m.getTa006());// --產品品號
		o.setSopname(m.getTa034());// --產品品名
		o.setSopspecifications(m.getTa035());// --產品規格
		o.setSorqty(m.getTa015());// 預計生產
		o.setSookqty(m.getTa017());// 目前生產數
		o.setSostatus(m.getTa011());// --狀態碼1.未生產,2.已發料,3.生產中,Y.已完工,y.指定完工
		o.setSonote(m.getTa029());// 製令備註(客戶/國家/訂單)
		if (!m.getMa002().equals("")) {
			o.setSofname(m.getMa002() + "(" + m.getTa032() + ")");// --加工廠
		}
		o.setSouname(m.getCreator());// 開單人

		JsonArray soscnotes = new JsonArray();
		JsonObject soscnoteOne = new JsonObject();
		// 如果是空的?
		if (o.getSoscnote().equals("[]")) {
			soscnoteOne.addProperty("date", Fm_T.to_yMd_Hms(new Date()));
			soscnoteOne.addProperty("user", m.getCreator());
			soscnoteOne.addProperty("content", m.getTa029());// m.getTa054() 不常使用
			soscnotes.add(soscnoteOne);
			o.setSoscnote(soscnotes.toString());// 生管備註(格式)人+時間+內容
		} else {
			// 不是空的->取出轉換->比對最新資料
			soscnotes = JsonParser.parseString(o.getSoscnote()).getAsJsonArray();

			// 取出先前的-最新資料比對->不同內容->添加新的
			JsonArray soscnoteOld = new JsonArray();
			soscnoteOld = (JsonArray) JsonParser.parseString(o.getSoscnote());
			String contentOld = soscnoteOld.get(0).getAsJsonObject().get("content").getAsString();
			String contentNew = m.getTa029();
			if (!contentOld.equals(contentNew)) {
				soscnoteOne.addProperty("date", Fm_T.to_yMd_Hms(new Date()));
				soscnoteOne.addProperty("user", m.getCreator());
				soscnoteOne.addProperty("content", m.getTa029());// m.getTa054() 不常使用
				soscnotes.add(soscnoteOne);
				o.setSoscnote(soscnotes.toString());// 生管備註(格式)人+時間+內容
			}
		}

		o.setSosum(checkSum);// 檢查/更新相同?資料串比對
		return o;
	}
}