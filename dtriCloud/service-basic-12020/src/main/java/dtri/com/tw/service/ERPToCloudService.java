package dtri.com.tw.service;

import java.util.Date;
import java.util.Map;

import org.springframework.stereotype.Service;

import dtri.com.tw.mssql.entity.Invta;
import dtri.com.tw.mssql.entity.Invtg;
import dtri.com.tw.mssql.entity.Invth;
import dtri.com.tw.mssql.entity.Mocta;
import dtri.com.tw.mssql.entity.Mocte;
import dtri.com.tw.mssql.entity.Moctf;
import dtri.com.tw.mssql.entity.Mocth;
import dtri.com.tw.mssql.entity.Purth;
import dtri.com.tw.pgsql.entity.BasicCommandList;
import dtri.com.tw.pgsql.entity.BasicIncomingList;
import dtri.com.tw.pgsql.entity.BasicShippingList;
import dtri.com.tw.pgsql.entity.WarehouseArea;
import dtri.com.tw.pgsql.entity.WarehouseKeeper;
import dtri.com.tw.pgsql.entity.WarehouseTypeFilter;
import dtri.com.tw.shared.Fm_T;

@Service
public class ERPToCloudService {

	// 指令類-轉換
	public BasicCommandList commandOne(BasicCommandList e, Mocta m, String checkSum) {

		e.setChecksum(checkSum);
		e.setBclproduct(m.getTa006());
		e.setBclclass(m.getTa001_ta002().split("-")[0]);// 製令單[別]
		e.setBclsn(m.getTa001_ta002().split("-")[1]);// 製令單[號]
		e.setBcltype(m.getTk000());// 製令單
		e.setBclnb(m.getBslnb());// 序列號
		e.setBclcheckin(1);// 0=未核單 1=已核單
		e.setBclacceptance(1);// 0=未檢驗 1=已檢驗 2=異常
		e.setBclpnumber(m.getMb001());// 物料號品號
		e.setBclpname(m.getMb002());// 品名
		e.setBclpspecification(m.getMb003());// 規格
		e.setBclpnqty(m.getTb004());// 需領用
		e.setBclpnaqty(m.getTb005());// 已領用
		e.setBclfromcommand("[" + m.getTa026_ta027_ta028() + "_訂單]");// 單據指令-來源 訂單
		e.setBcltocommand("[_]");// 單據指令-對象
		e.setBcltowho("[_]");// 目的對象
		e.setBclfromwho("[" + m.getMb017() + "_" + m.getMc002() + "]");// 目的來源-[倉別代號+倉別名稱]
		if (!m.getTb015().equals("")) {
			e.setBcledate(Fm_T.toYMDate(m.getTb015()));// 預計領料日
		}
		e.setSysstatus(0);// 未完成
		e.setSysmdate(new Date());// 日期
		return e;
	}

	// ============ A341 國內進貨單/ A342 國外進貨單/ A343 台北進貨單/ A345 無採購進貨單 ============
	// 入料類-轉換(Purth)
	public BasicIncomingList incomingOnePurth(BasicIncomingList o, Purth m, String checkSum, //
			Map<String, WarehouseTypeFilter> wTFs, Map<String, WarehouseKeeper> wKs, Map<String, WarehouseArea> wAs) {
		// 是否結單?
		int sysstatus = 0;
		if (m.getTh030().equals("Y")) {
			sysstatus = 1;
			o.setBilpngqty(m.getTh007());// 已入庫量
			o.setBilcuser("System(ERP_Auto)");
			o.setBilfuser("System(ERP_Auto)");
		}
		// 資料匹配
		o.setChecksum(checkSum);
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

		// 而外匹配 [單別]
		String bilfromcommand = "_採購單";
		String wtfcode = m.getTh001_th002().split("-")[0];
		if (wTFs.containsKey(wtfcode)) {
			bilfromcommand = "_" + wTFs.get(wtfcode).getWtfname();
		}
		o.setBilfromcommand("[" + m.getTh011_th012_th013() + bilfromcommand + "]");// 單據來源 [_採購單]
		o.setBiltocommand("[_]");
		// 而外匹配 [倉別代號+倉別名稱+位置]
		String biltowho = m.getTh009() + "_";
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
			Map<String, WarehouseTypeFilter> wTFs, Map<String, WarehouseKeeper> wKs, Map<String, WarehouseArea> wAs) {
		// 是否結單?
		int sysstatus = 0;
		if (m.getTe019().equals("Y")) {
			sysstatus = 1;
			o.setBilpngqty(m.getTb004());// 已入庫量
			o.setBilcuser("System(ERP_Auto)");
			o.setBilfuser("System(ERP_Auto)");
		}
		// 資料匹配
		o.setChecksum(checkSum);
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
		o.setSysstatus(0);// 未完成
		o.setSysnote(m.getTe014());// 備註
		o.setSysmdate(new Date());
		o.setSysnote(m.getTe014());
		// 預計入料日
		if (m.getTa009() != null) {
			o.setBiledate(Fm_T.toYMDate(m.getTa009()));
		}

		// 而外匹配 [單別]
		String bilfromcommand = "_製令單";
		if (wTFs.containsKey(m.getTa001_ta002().split("-")[0])) {
			bilfromcommand = "_" + wTFs.get(m.getTa001_ta002().split("-")[0]).getWtfname();
		}
		o.setBilfromcommand("[" + m.getTa001_ta002() + bilfromcommand + "]");// 單據來源 [_製令單]
		o.setBiltocommand("[_]");

		// 而外匹配 [倉別代號+倉別名稱+位置]
		String biltowho = m.getTb009() + "_";
		String wAsKey = m.getTb009() + "_" + m.getMb001();
		if (wAs.containsKey(wAsKey)) {
			biltowho = m.getTb009() + "_" + wAs.get(wAsKey).getWaaname() + "_" + wAs.get(wAsKey).getWaslocation();
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
			Map<String, WarehouseTypeFilter> wTFs, Map<String, WarehouseKeeper> wKs, Map<String, WarehouseArea> wAs) {
		// 是否結單?
		int sysstatus = 0;
		if (m.getTe019().equals("Y")) {
			sysstatus = 1;
			o.setBslpngqty(m.getTb004());// 已取庫量
			o.setBslcuser("System(ERP_Auto)");
			o.setBslfuser("System(ERP_Auto)");
		}
		// 資料匹配
		o.setChecksum(checkSum);
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
		o.setSysstatus(sysstatus);// 0=尚未結束,1=結案
		o.setSysnote(m.getTe014());// 備註
		// 預計領料日
		if (m.getTa009() != null) {
			o.setBsledate(Fm_T.toYMDate(m.getTa009()));
		}

		// 而外匹配 [單別]
		o.setBslfromcommand("[" + m.getTa001_ta002() + "*" + m.getTa006() + "*" + m.getTa015() + "]");// 製令單
		o.setBsltocommand("[_]");
		// 而外匹配 [倉別代號+倉別名稱+位置]
		String bilfromwho = m.getTb009() + "_";
		String wAsKey = m.getTb009() + "_" + m.getMb001();
		if (wAs.containsKey(wAsKey)) {
			bilfromwho = m.getTb009() + "_" + wAs.get(wAsKey).getWaaname() + "_" + wAs.get(wAsKey).getWaslocation();
		}
		o.setBsltowho("[_生產線]");// 目的[_生產線]
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
			Map<String, WarehouseTypeFilter> wTFs, Map<String, WarehouseKeeper> wKs, Map<String, WarehouseArea> wAs) {
		// 是否結單?
		int sysstatus = 0;
		if (m.getTg022().equals("Y")) {
			sysstatus = 1;
			o.setBilpngqty(m.getTg011());// 已入庫量
			o.setBilcuser("System(ERP_Auto)");
			o.setBilfuser("System(ERP_Auto)");
		}
		o.setChecksum(checkSum);
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
		// 而外匹配 [單別]
		String bilfromcommand = "_製令單";
		if (wTFs.containsKey(m.getTg014_tg015().split("-")[0])) {
			bilfromcommand = "_" + wTFs.get(m.getTg014_tg015().split("-")[0]).getWtfname();
		}
		o.setBilfromcommand("[" + m.getTg014_tg015() + bilfromcommand + "]");// 單據來源 [_製令單]
		o.setBiltocommand("[_]");
		// 而外匹配 [倉別代號+倉別名稱+位置]
		String biltowho = m.getTg010() + "_";
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
			Map<String, WarehouseTypeFilter> wTFs, Map<String, WarehouseKeeper> wKs, Map<String, WarehouseArea> wAs) {
		// 是否結單?
		int sysstatus = 0;
		if (m.getTi037().equals("Y")) {
			sysstatus = 1;
			o.setBilpngqty(m.getTi007());// 已入庫量
			o.setBilcuser("System(ERP_Auto)");
			o.setBilfuser("System(ERP_Auto)");
		}
		o.setChecksum(checkSum);
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

		// 而外匹配 [單別]
		String bilfromcommand = "_製令單";
		if (wTFs.containsKey(m.getTi013_ti014().split("-")[0])) {
			bilfromcommand = "_" + wTFs.get(m.getTi013_ti014().split("-")[0]).getWtfname();
		}
		o.setBilfromcommand("[" + m.getTi013_ti014() + bilfromcommand + "]");// 單據來源 [_製令單]
		o.setBiltocommand("[_]");
		// 而外匹配 [倉別代號+倉別名稱+位置]
		String biltowho = m.getTi009() + "_";
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
			Map<String, WarehouseTypeFilter> wTFs, Map<String, WarehouseKeeper> wKs, Map<String, WarehouseArea> wAs) {
		// 是否結單?
		int sysstatus = 0;
		if (m.getTg022().equals("Y")) {
			sysstatus = 1;
			o.setBilpngqty(m.getTg009());// 已入庫量
			o.setBilcuser("System(ERP_Auto)");
			o.setBilfuser("System(ERP_Auto)");
		}
		o.setChecksum(checkSum);
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

		// 而外匹配 [單別]
		o.setBilfromcommand("[_]");// 單據來源 [_]
		o.setBiltocommand("[_]");// 單據對象 [_]
		// 而外匹配 [倉別代號+倉別名稱+位置]
		String biltowho = m.getTg008() + "_";
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
			Map<String, WarehouseTypeFilter> wTFs, Map<String, WarehouseKeeper> wKs, Map<String, WarehouseArea> wAs) {
		// 是否結單?
		int sysstatus = 0;
		if (m.getTg022().equals("Y")) {
			sysstatus = 1;
			o.setBslpngqty(m.getTg009());// 已領庫量
			o.setBslcuser("System(ERP_Auto)");
			o.setBslfuser("System(ERP_Auto)");
		}
		o.setChecksum(checkSum);
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

		// 而外匹配 [單別]
		o.setBslfromcommand("[_]");//
		o.setBsltocommand("[_]");//
		// 而外匹配 [倉別代號+倉別名稱+位置]
		String bilfromwho = m.getTg007() + "_";
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
			Map<String, WarehouseTypeFilter> wTFs, Map<String, WarehouseKeeper> wKs, Map<String, WarehouseArea> wAs) {
		// 是否結單?
		int sysstatus = 0;
		if (m.getTi022().equals("3")) {
			sysstatus = 1;
			o.setBilpngqty(m.getTi009());// 已入庫量
			o.setBilcuser("System(ERP_Auto)");
			o.setBilfuser("System(ERP_Auto)");
		}
		o.setChecksum(checkSum);
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

		// 而外匹配 [單別]
		String bilfromcommand = "_借出單";
		if (wTFs.containsKey(m.getTi014_ti015_ti016().split("-")[0])) {
			bilfromcommand = "_" + wTFs.get(m.getTi014_ti015_ti016().split("-")[0]).getWtfname();
		}
		o.setBilfromcommand("[" + m.getTi014_ti015_ti016() + bilfromcommand + "]");// 單據來源 [_借出單]
		o.setBiltocommand("[_]");
		// 而外匹配 [倉別代號+倉別名稱+位置]
		String biltowho = m.getTi008() + "_";
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
			Map<String, WarehouseTypeFilter> wTFs, Map<String, WarehouseKeeper> wKs, Map<String, WarehouseArea> wAs) {
		// 是否結單?
		int sysstatus = 0;
		if (m.getTi022().equals("Y")) {
			sysstatus = 1;
			o.setBslpngqty(m.getTi009());// 已領庫量
			o.setBslcuser("System(ERP_Auto)");
			o.setBslfuser("System(ERP_Auto)");
		}
		o.setChecksum(checkSum);
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

		// 而外匹配 [單別]
		String bilfromcommand = "_借入單";
		if (wTFs.containsKey(m.getTi014_ti015_ti016().split("-")[0])) {
			bilfromcommand = "_" + wTFs.get(m.getTi014_ti015_ti016().split("-")[0]).getWtfname();
		}
		o.setBslfromcommand("[" + m.getTi014_ti015_ti016() + bilfromcommand + "]");// 單據來源 [_借入單]
		o.setBsltocommand("[_]");//
		// 而外匹配 [倉別代號+倉別名稱+位置]
		String bilfromwho = m.getTi007() + "_";
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
			Map<String, WarehouseTypeFilter> wTFs, Map<String, WarehouseKeeper> wKs, Map<String, WarehouseArea> wAs) {
		// 是否結單?
		int sysstatus = 0;
		if (m.getTb018().equals("Y")) {
			sysstatus = 1;
			o.setBilpngqty(m.getTb007());// 已入庫量
			o.setBilcuser("System(ERP_Auto)");
			o.setBilfuser("System(ERP_Auto)");
		}
		o.setChecksum(checkSum);
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

		// 而外匹配 [單別]
		o.setBilfromcommand("[_]");// 單據來源 [_]
		o.setBiltocommand("[_]");
		// 而外匹配 [倉別代號+倉別名稱+位置]
		String biltowho = m.getTb013() + "_";
		String bilfromwho = m.getTb012() + "_";
		String wAsKey = m.getTb013() + "_" + m.getMb001();
		if (wAs.containsKey(wAsKey)) {
			biltowho = m.getTb013() + "_" + wAs.get(wAsKey).getWaaname() + "_" + wAs.get(wAsKey).getWaslocation();
		}
		wAsKey = m.getTb012() + "_" + m.getMb001();
		if (wAs.containsKey(wAsKey)) {
			bilfromwho = m.getTb012() + "_" + wAs.get(wAsKey).getWaaname() + "_" + wAs.get(wAsKey).getWaslocation();
		}
		o.setBiltowho("[" + biltowho + "]");// 目的對象[_倉庫]
		o.setBilfromwho("[" + bilfromwho + "]");// 目的來源[_倉庫]
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
			Map<String, WarehouseTypeFilter> wTFs, Map<String, WarehouseKeeper> wKs, Map<String, WarehouseArea> wAs) {
		// 是否結單?
		int sysstatus = 0;
		if (m.getTb018().equals("Y")) {
			sysstatus = 1;
			o.setBslpngqty(m.getTb007());// 已領庫量
			o.setBslcuser("System(ERP_Auto)");
			o.setBslfuser("System(ERP_Auto)");
		}
		o.setChecksum(checkSum);
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

		// 而外匹配 [單別]
		o.setBslfromcommand("[_]");// 單據來源 [_]
		o.setBsltocommand("[_]");//
		// 而外匹配 [倉別代號+倉別名稱+位置]
		String bilfromwho = m.getTb012() + "_";
		String biltowho = m.getTb013() + "_";
		String wAsKey = m.getTb013() + "_" + m.getMb001();
		if (wAs.containsKey(wAsKey)) {
			biltowho = m.getTb013() + "_" + wAs.get(wAsKey).getWaaname() + "_" + wAs.get(wAsKey).getWaslocation();
		}
		wAsKey = m.getTb012() + "_" + m.getMb001();
		if (wAs.containsKey(wAsKey)) {
			bilfromwho = m.getTb012() + "_" + wAs.get(wAsKey).getWaaname() + "_" + wAs.get(wAsKey).getWaslocation();
		}
		o.setBsltowho("[" + biltowho + "]");// 目的來源[_倉庫]
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
}