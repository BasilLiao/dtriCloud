package dtri.com.tw.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dtri.com.tw.pgsql.dao.WarehouseAreaDao;
import dtri.com.tw.pgsql.entity.BasicIncomingList;
import dtri.com.tw.pgsql.entity.BasicShippingList;
import dtri.com.tw.pgsql.entity.WarehouseArea;
import dtri.com.tw.pgsql.entity.WarehouseConfig;
import dtri.com.tw.pgsql.entity.WarehouseTypeFilter;

@Service
public class ERPAutoCheckService {
	@Autowired
	WarehouseAreaDao areaDao;

	// 入料類-自動化
	public BasicIncomingList incomingAuto(BasicIncomingList o, //
			Map<String, Integer> wAsSave, Map<String, WarehouseTypeFilter> wTFs, Map<String, WarehouseConfig> wCs) {
		// 單據自動?
		// Step1. 必須有匹配該單據設定
		if (wTFs.containsKey(o.getBilclass())) {
			System.out.println(o.getBilclass() + " " + wTFs.get(o.getBilclass()).getWtfaiqty() + " " //
					+ wTFs.get(o.getBilclass()).getWtfmrcheck() + " "//
					+ wTFs.get(o.getBilclass()).getWtfsepncheck());
			// +自動添加
			if (wTFs.get(o.getBilclass()).getWtfaiqty()) {
				String wcKey = o.getBiltowho().split("_")[0].replace("[", "");
				String wAsKey = wcKey + "_" + o.getBilpnumber();
				o.setSysstatus(1);// 已完成
				o.setBilcuser("System(Type_Auto)");
				o.setBilfuser("System(Type_Auto)");
				o.setBilpngqty(o.getBilpnqty());// 數量
				// 已經有?
				if (wAsSave.containsKey(wAsKey)) {
					wAsSave.put(wAsKey, wAsSave.get(wAsKey) + o.getBilpnqty());
				} else {
					wAsSave.put(wAsKey, o.getBilpnqty());
				}
			}
			// 單據管理人(攔截)->沒有勾起來 自動Pass
			if (!wTFs.get(o.getBilclass()).getWtfmrcheck()) {
				o.setBilcuser("System(Type_Pass)");
			}
			// 儲位負責人(攔截)->沒有勾起來 自動Pass
			if (!wTFs.get(o.getBilclass()).getWtfsepncheck()) {
				o.setBilfuser("System(Type_Pass)");
			}
		}

		// 倉別自動?
		// Step2. 必須標準格式 Ex:[A0002_原物料倉_2F-B1-06-01]
		if (o.getBiltowho().split("_").length == 3) {
			String wcKey = o.getBiltowho().split("_")[0].replace("[", "");
			String wAsKey = wcKey + "_" + o.getBilpnumber();
			// 此倉儲+自動添加
			if (wCs.containsKey(wcKey) && wCs.get(wcKey).getWcaiqty()) {
				o.setBilcuser("System(Config_Auto)");
				o.setBilfuser("System(Config_Auto)");
				o.setBilpngqty(o.getBilpnqty());// 數量
				// 已經有?
				if (wAsSave.containsKey(wAsKey)) {
					wAsSave.put(wAsKey, wAsSave.get(wAsKey) + o.getBilpnqty());
				} else {
					wAsSave.put(wAsKey, o.getBilpnqty());
				}
			}
		}
		return o;
	}

	// 領料類-自動化
	public BasicShippingList shippingAuto(BasicShippingList o, //
			Map<String, Integer> wAsSave, Map<String, WarehouseTypeFilter> wTFs, Map<String, WarehouseConfig> wCs) {
		// 單據自動?
		// Step1. 必須有匹配該單據設定+自動減少
		if (wTFs.containsKey(o.getBslclass())) {
			System.out.println(o.getBslclass() + " " + wTFs.get(o.getBslclass()).getWtfaiqty() + " " //
					+ wTFs.get(o.getBslclass()).getWtfmrcheck() + " "//
					+ wTFs.get(o.getBslclass()).getWtfsepncheck());
			
			if (wTFs.get(o.getBslclass()).getWtfaiqty()) {
				String wcKey = o.getBsltowho().split("_")[0].replace("[", "");
				String wAsKey = wcKey + "_" + o.getBslpnumber();
				o.setBslcuser("System(Type_Auto)");
				o.setBslfuser("System(Type_Auto)");
				o.setBslpngqty(o.getBslpnqty());// 數量
				// 已經有?
				if (wAsSave.containsKey(wAsKey)) {
					wAsSave.put(wAsKey, wAsSave.get(wAsKey) - o.getBslpnqty());
				} else {
					wAsSave.put(wAsKey, o.getBslpnqty());
				}
			}
			// 單據管理人(攔截)->沒有勾起來 自動Pass
			if (!wTFs.get(o.getBslclass()).getWtfmrcheck()) {
				o.setBslcuser("System(Type_Pass)");
			}
			// 欄位管理人(攔截)->沒有勾起來 自動Pass
			if (!wTFs.get(o.getBslclass()).getWtfsepncheck()) {
				o.setBslfuser("System(Type_Pass)");
			}
		}
		// 倉別自動?
		// Step2. 必須標準格式 Ex:[A0002_原物料倉_2F-B1-06-01]
		if (o.getBsltowho().split("_").length == 3) {
			String wcKey = o.getBsltowho().split("_")[0].replace("[", "");
			String wAsKey = wcKey + "_" + o.getBslpnumber();
			// 此倉儲+自動添加
			if (wCs.containsKey(wcKey) && wCs.get(wcKey).getWcaiqty()) {
				o.setBslcuser("System(Config_Auto)");
				o.setBslfuser("System(Config_Auto)");
				o.setBslpngqty(o.getBslpnqty());// 數量
				// 已經有?
				if (wAsSave.containsKey(wAsKey)) {
					wAsSave.put(wAsKey, wAsSave.get(wAsKey) - o.getBslpnqty());
				} else {
					wAsSave.put(wAsKey, -o.getBslpnqty());
				}
			}
		}

		return o;
	}

	// Step3. 立即結算
	public Map<String, Integer> settlementAuto(Map<String, Integer> wAsSave) {
		ArrayList<WarehouseArea> arraySave = new ArrayList<>();
		wAsSave.forEach((key, val) -> {
			ArrayList<WarehouseArea> arrayList = areaDao.findAllByWaaliasawmpnb(key);
			if (arrayList.size() > 0) {
				val = arrayList.get(0).getWatqty() - val;
				val = val < 0 ? 0 : val;
				arrayList.get(0).setWatqty(val);
				arraySave.add(arrayList.get(0));
			}
		});
		areaDao.saveAll(arraySave);
		wAsSave = new HashMap<>();
		return wAsSave;
	}

}
