package dtri.com.tw.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dtri.com.tw.pgsql.dao.WarehouseAreaDao;
import dtri.com.tw.pgsql.dao.WarehouseHistoryDao;
import dtri.com.tw.pgsql.entity.BasicIncomingList;
import dtri.com.tw.pgsql.entity.BasicShippingList;
import dtri.com.tw.pgsql.entity.WarehouseArea;
import dtri.com.tw.pgsql.entity.WarehouseConfig;
import dtri.com.tw.pgsql.entity.WarehouseHistory;
import dtri.com.tw.pgsql.entity.WarehouseMaterial;
import dtri.com.tw.pgsql.entity.WarehouseTypeFilter;

@Service
public class ERPAutoCheckService {
	@Autowired
	private WarehouseAreaDao areaDao;

	@Autowired
	private WarehouseHistoryDao historyDao;

	// 入料類-自動化(歸還)
	public BasicIncomingList incomingAutoRe(BasicIncomingList o, //
			Map<String, Integer> wAsSave, Map<String, WarehouseTypeFilter> wTFs, //
			Map<String, WarehouseConfig> wCs, Map<String, WarehouseMaterial> wMs) {

		// 取得Key
		String wcKey = o.getBiltowho().split("_")[0].replace("[", "").replace("]", "");
		String wAsKey = wcKey + "_" + o.getBilpnumber();
		wAsKey = wAsKey.replaceAll(" ","");
		// 已經有?
		if (wAsSave.containsKey(wAsKey)) {
			int s = wAsSave.get(wAsKey) - o.getBilpnqty();
			wAsSave.put(wAsKey, s);
		} else {
			int s = -o.getBilpnqty();
			wAsSave.put(wAsKey, s);
		}
		return o;
	}

	// 領料類-自動化(歸還)
	public BasicShippingList shippingAutoRe(BasicShippingList o, //
			Map<String, Integer> wAsSave, Map<String, WarehouseTypeFilter> wTFs, //
			Map<String, WarehouseConfig> wCs, Map<String, WarehouseMaterial> wMs) {
		String wcKey = o.getBsltowho().split("_")[0].replace("[", "").replace("]", "");
		String wAsKey = wcKey + "_" + o.getBslpnumber();
		wAsKey = wAsKey.replaceAll(" ","");
		// 已經有?
		if (wAsSave.containsKey(wAsKey)) {
			int s = wAsSave.get(wAsKey) + o.getBslpnqty();
			wAsSave.put(wAsKey, s);
		} else {
			int s = o.getBslpnqty();
			wAsSave.put(wAsKey, s);
		}
		o.setBslfuser("");
		o.setBslcuser("");
		return o;
	}

	// 入料類-自動化
	public BasicIncomingList incomingAuto(BasicIncomingList o, //
			Map<String, Integer> wAsSave, Map<String, WarehouseTypeFilter> wTFs, //
			Map<String, WarehouseConfig> wCs, Map<String, WarehouseMaterial> wMs, //
			Map<String, WarehouseArea> wAs) {// 庫別

		// 是否在檢測下一層(單據->倉別->物料)
		boolean wTFsCheck = true;// 單據自動(true->繼續/false->停止)
		boolean wCsCheck = true;// 倉別自動

		// boolean wMsCheck = true;// 物料自動
		// 測試用
//		if ((o.getBilclass() + "-" + o.getBilsn()).equals("A121-231117002")) {
//			System.out.println("A541-240419004-0001");
//		}
		// 如果是0則直接完成
		if (o.getBilpnqty().equals(0)) {
			o.setBilcuser("System(Zero)");
			o.setBilfuser("System(Zero)");
		}

		// 單據自動?
		// Step1. 必須有匹配該單據設定
		if (wTFs.containsKey(o.getBilclass())) {
			/*
			 * System.out.println(o.getBilclass() + " " // +
			 * wTFs.get(o.getBilclass()).getWtfaiqty() + " " // +
			 * wTFs.get(o.getBilclass()).getWtfmrcheck() + " "// +
			 * wTFs.get(o.getBilclass()).getWtfsepncheck());
			 */
			// +自動添加
			if (wTFs.get(o.getBilclass()).getWtfaiqty()) {
				String wcKey = o.getBiltowho().split("_")[0].replace("[", "").replace("]", "");
				String wAsKey = wcKey + "_" + o.getBilpnumber();
				wAsKey = wAsKey.replaceAll(" ","");
				o.setBilcuser("System(Type_Auto)");
				o.setBilfuser("System(Type_Auto)");
				o.setBilpngqty(o.getBilpnqty());// 數量
				// 已經有?
				if (wAsSave.containsKey(wAsKey)) {
					wAsSave.put(wAsKey, wAsSave.get(wAsKey) + o.getBilpnqty());
				} else {
					wAsSave.put(wAsKey, o.getBilpnqty());
				}
				wTFsCheck = false;
			}
			// 單據管理人(攔截)->沒有勾起來 自動Pass
			if (!wTFs.get(o.getBilclass()).getWtfmrcheck() && o.getBilcuser().equals("")) {
				o.setBilcuser("System(Type_Pass)");
			}
			// 儲位負責人(攔截)->沒有勾起來 自動Pass
			if (!wTFs.get(o.getBilclass()).getWtfsepncheck() && o.getBilfuser().equals("")) {
				o.setBilfuser("System(Type_Pass)");
			}
			// 都沒勾
			if (!wTFs.get(o.getBilclass()).getWtfmrcheck() && !wTFs.get(o.getBilclass()).getWtfsepncheck()) {
				wTFsCheck = false;
			}
		}

		// 倉別自動?
		// Step2. 必須標準格式 Ex:[A0002_原物料倉_2F-B1-06-01]
		if (o.getBiltowho().split("_").length == 3 && wTFsCheck) {
			String wcKey = o.getBiltowho().split("_")[0].replace("[", "").replace("]", "");
			String wAsKey = wcKey + "_" + o.getBilpnumber();
			wAsKey = wAsKey.replaceAll(" ","");
			// 測試用
//			if(wAsKey.equals("A0041_81-105-38210G")) {
//				System.out.println(wAsKey);
//			}

			// 此倉儲+自動添加
			if (wCs.containsKey(wcKey)) {
				if (wCs.get(wcKey).getWcaiqty()) {
					o.setBilcuser("System(Config_Auto)");
					o.setBilfuser("System(Config_Auto)");
					o.setBilpngqty(o.getBilpnqty());// 數量
					// 已經有?
					if (wAsSave.containsKey(wAsKey)) {
						wAsSave.put(wAsKey, wAsSave.get(wAsKey) + o.getBilpnqty());
					} else {
						wAsSave.put(wAsKey, o.getBilpnqty());
					}
					wCsCheck = false;
				} else {
					// 單據管理人(攔截)->沒有勾起來 自動Pass
					if (!wCs.get(wcKey).getWcmrcheck() && o.getBilcuser().equals("")) {
						o.setBilcuser("System(Config_Pass)");
					}
					// 欄位管理人(攔截)->沒有勾起來 自動Pass
					if (!wCs.get(wcKey).getWcsepncheck() && o.getBilfuser().equals("")) {
						o.setBilfuser("System(Config_Pass)");
					}
					// 都沒勾
					if (!wCs.get(wcKey).getWcmrcheck() && !wCs.get(wcKey).getWcsepncheck()) {
						wCsCheck = false;
					}
				}
			}
		}
		// 物料自動?
		// Step3. 必須標準格式 Ex:[A0002_原物料倉_2F-B1-06-01]
		if (wMs.containsKey(o.getBilpnumber()) && wCsCheck) {
			String wcKey = o.getBiltowho().split("_")[0].replace("[", "").replace("]", "");
			String wAsKey = wcKey + "_" + o.getBilpnumber();
			wAsKey = wAsKey.replaceAll(" ","");
			if (wMs.get(o.getBilpnumber()).getWmaiqty()) {
				o.setBilcuser("System(Material_Auto)");
				o.setBilfuser("System(Material_Auto)");
				o.setBilpngqty(o.getBilpnqty());// 數量
				// 已經有?
				if (wAsSave.containsKey(wAsKey)) {
					wAsSave.put(wAsKey, wAsSave.get(wAsKey) + o.getBilpnqty());
				} else {
					wAsSave.put(wAsKey, o.getBilpnqty());
				}
			} else {
				// 物料管理人(攔截)->沒有勾起來 自動Pass
				if (!wMs.get(o.getBilpnumber()).getWmmrcheck() && o.getBilcuser().equals("")) {
					o.setBilcuser("System(Material_Pass)");
				}
				// 物料管理人(攔截)->沒有勾起來 自動Pass
				if (!wMs.get(o.getBilpnumber()).getWmsepncheck() && o.getBilfuser().equals("")) {
					o.setBilfuser("System(Material_Pass)");
				}
			}
		}
		// 自動化 物料異動紀錄
		if (o.getBilfuser().contains("System")) {
			// 紀錄更新
			String wcKey = o.getBiltowho().split("_")[0].replace("[", "").replace("]", "");
			String wAsKey = wcKey + "_" + o.getBilpnumber();
			wAsKey = wAsKey.replaceAll(" ","");
			WarehouseArea area = wAs.get(wAsKey);
			WarehouseHistory history = new WarehouseHistory();
			history.setWhtype("入料(" + o.getBilfuser() + ")");
			history.setWhwmslocation(o.getBiltowho());
			history.setWhcontent(//
					o.getBilclass() + "-" + o.getBilsn() + "-" + //
							o.getBilnb() + "*" + o.getBilpnqty());
			history.setWhwmpnb(o.getBilpnumber());
			history.setWhfuser(o.getBilfuser());
			history.setWheqty(area != null ? area.getWaerptqty() : 0);
			history.setWhcqty(area != null ? area.getWatqty() : 0);
			history.setWhcheckin(o.getBilcheckin() == 0 ? "未核單" : "已核單");
			historyDao.save(history);
		}
		return o;
	}

	// 領料類-自動化
	public BasicShippingList shippingAuto(BasicShippingList o, //
			Map<String, Integer> wAsSave, Map<String, WarehouseTypeFilter> wTFs, //
			Map<String, WarehouseConfig> wCs, Map<String, WarehouseMaterial> wMs, //
			Map<String, WarehouseArea> wAs) {// 庫存

		// 是否在檢測下一層(單據->倉別->物料)
		boolean wTFsCheck = true;// 單據自動(true->繼續/false->停止)
		boolean wCsCheck = true;// 倉別自動
		// boolean wMsCheck = true;// 物料自動
		// 測試用
//		if ((o.getBslclass() + "-" + o.getBslsn()).equals("A541-240418031")) {
//			System.out.println("A541-240418031"+o.getBslpnumber());
//		}
		// 如果是0則直接完成
		if (o.getBslpnqty().equals(0)) {
			o.setBslcuser("System(Zero)");
			o.setBslfuser("System(Zero)");
			o.setBslsmuser("System(Zero)");
		}

		// 單據自動?
		// Step1. 必須有匹配該單據設定+自動減少
		if (wTFs.containsKey(o.getBslclass())) {
			/*
			 * System.out.println(o.getBslclass() + " " // +
			 * wTFs.get(o.getBslclass()).getWtfaiqty() + " " // +
			 * wTFs.get(o.getBslclass()).getWtfmrcheck() + " "// +
			 * wTFs.get(o.getBslclass()).getWtfsepncheck());
			 */
			// 自動減少?
			String wcKey = o.getBslfromwho().split("_")[0].replace("[", "").replace("]", "");
			String wAsKey = wcKey + "_" + o.getBslpnumber();
			wAsKey = wAsKey.replaceAll(" ","");
			if (wTFs.get(o.getBslclass()).getWtfaiqty()) {
				// 檢查數量是否-充足?
				boolean checkQty = false;
				if (wAs.containsKey(wAsKey) && wAsSave.containsKey(wAsKey)) {
					// 已經有 登記過?(取得目前倉庫數量+((負數)累計扣數量 - (正數)需用數量))
					checkQty = wAs.get(wAsKey).getWatqty() + (wAsSave.get(wAsKey) - o.getBslpnqty()) >= 0;
				} else if (wAs.containsKey(wAsKey) && !wAsSave.containsKey(wAsKey)) {
					// 還沒有 登記過?
					checkQty = wAs.get(wAsKey).getWatqty() - o.getBslpnqty() >= 0;
				} else {
					// 沒配到
					checkQty = false;
				}
				// 動作
				o.setBslcuser("System(Type_Auto)");
				if (checkQty) {
					o.setBslfuser("System(Type_Auto)");
					o.setBslpngqty(o.getBslpnqty());// 數量
					// 已經有?
					if (wAsSave.containsKey(wAsKey)) {
						wAsSave.put(wAsKey, wAsSave.get(wAsKey) - o.getBslpnqty());
					} else {
						wAsSave.put(wAsKey, -o.getBslpnqty());
					}
				}
				wTFsCheck = false;// 停止
			} else {
				// 單據攔截
				// 單據管理人(攔截)->沒有勾起來 自動Pass
				if (!wTFs.get(o.getBslclass()).getWtfmrcheck() && o.getBslcuser().equals("")) {
					o.setBslcuser("System(Type_Pass)");
				}
				// 欄位管理人(攔截)->沒有勾起來 自動Pass
				if (!wTFs.get(o.getBslclass()).getWtfsepncheck() && o.getBslfuser().equals("")) {
					o.setBslfuser("System(Type_Pass)");
				}
				// 都沒勾起[單據管理人(攔截)/欄位管理人(攔截)]
				if (!wTFs.get(o.getBslclass()).getWtfmrcheck() && !wTFs.get(o.getBslclass()).getWtfsepncheck()) {
					wTFsCheck = false;// 停止
				}
			}
		}
		// 倉別自動?
		// Step2. 必須標準格式 Ex:[A0002_原物料倉_2F-B1-06-01]
		if (o.getBslfromwho().split("_").length == 3 && wTFsCheck) {
			String wcKey = o.getBslfromwho().split("_")[0].replace("[", "").replace("]", "");
			String wAsKey = wcKey + "_" + o.getBslpnumber();
			wAsKey = wAsKey.replaceAll(" ","");
			// 取得倉別
			if (wCs.containsKey(wcKey)) {
				// 自動減少?
				if (wCs.get(wcKey).getWcaiqty()) {
					// 檢查數量是否-充足?
					boolean checkQty = false;
					if (wAs.containsKey(wAsKey) && wAsSave.containsKey(wAsKey)) {
						// 已經有 登記過?(取得目前倉庫數量+((負數)累計扣數量 - (正數)需用數量))
						checkQty = wAs.get(wAsKey).getWatqty() + (wAsSave.get(wAsKey) - o.getBslpnqty()) >= 0;
					} else if (wAs.containsKey(wAsKey) && !wAsSave.containsKey(wAsKey)) {
						// 還沒有 登記過?
						checkQty = wAs.get(wAsKey).getWatqty() - o.getBslpnqty() >= 0;
					} else {
						// 沒配到
						checkQty = false;
					}
					// 動作
					o.setBslcuser("System(Config_Auto)");
					if (checkQty) {
						o.setBslfuser("System(Config_Auto)");
						o.setBslpngqty(o.getBslpnqty());// 數量
						// 已經有?
						if (wAsSave.containsKey(wAsKey)) {
							wAsSave.put(wAsKey, wAsSave.get(wAsKey) - o.getBslpnqty());
						} else {
							wAsSave.put(wAsKey, -o.getBslpnqty());
						}
					}

					wCsCheck = false;
				} else {
					// 倉別管理人(攔截)->沒有勾起來 自動Pass
					if (!wCs.get(wcKey).getWcmrcheck() && o.getBslcuser().equals("")) {
						o.setBslcuser("System(Config_Pass)");
					}
					// 倉別管理人(攔截)->沒有勾起來 自動Pass
					if (!wCs.get(wcKey).getWcsepncheck() && o.getBslfuser().equals("")) {
						o.setBslfuser("System(Config_Pass)");
					}
					if (!wCs.get(wcKey).getWcmrcheck() && !wCs.get(wcKey).getWcsepncheck()) {
						wCsCheck = false;
					}
				}
			}
		}
		// 物料自動?
		// Step3. 必須標準格式 Ex:[A0002_原物料倉_2F-B1-06-01]
		if (wMs.containsKey(o.getBslpnumber()) && wCsCheck) {
			// 自動減少?
			String wcKey = o.getBslfromwho().split("_")[0].replace("[", "").replace("]", "");
			String wAsKey = wcKey + "_" + o.getBslpnumber();
			wAsKey = wAsKey.replaceAll(" ","");
			if (wMs.get(o.getBslpnumber()).getWmaiqty()) {
				// 檢查數量是否-充足?
				boolean checkQty = false;
				if (wAs.containsKey(wAsKey) && wAsSave.containsKey(wAsKey)) {
					// 已經有 登記過?(取得目前倉庫數量+((負數)累計扣數量 - (正數)需用數量))
					checkQty = wAs.get(wAsKey).getWatqty() + (wAsSave.get(wAsKey) - o.getBslpnqty()) >= 0;
				} else if (wAs.containsKey(wAsKey) && !wAsSave.containsKey(wAsKey)) {
					// 還沒有 登記過?
					checkQty = wAs.get(wAsKey).getWatqty() - o.getBslpnqty() >= 0;
				} else {
					// 沒配到
					checkQty = false;
				}
				// 動作
				o.setBslcuser("System(Material_Auto)");
				if (checkQty) {
					o.setBslfuser("System(Material_Auto)");
					o.setBslpngqty(o.getBslpnqty());// 數量
					// 已經有?
					if (wAsSave.containsKey(wAsKey)) {
						wAsSave.put(wAsKey, wAsSave.get(wAsKey) - o.getBslpnqty());
					} else {
						wAsSave.put(wAsKey, -o.getBslpnqty());
					}
				}
			} else {
				// 物料管理人(攔截)->沒有勾起來 自動Pass
				if (!wMs.get(o.getBslpnumber()).getWmmrcheck() && o.getBslcuser().equals("")) {
					o.setBslcuser("System(Material_Pass)");
				}
				// 物料管理人(攔截)->沒有勾起來 自動Pass
				if (!wMs.get(o.getBslpnumber()).getWmsepncheck() && o.getBslfuser().equals("")) {
					o.setBslfuser("System(Material_Pass)");
				}
			}
		}
		// 自動化 物料異動紀錄
		if (o.getBslfuser().contains("System")) {
			// 紀錄更新
			String wcKey = o.getBslfromwho().split("_")[0].replace("[", "").replace("]", "");
			String wAsKey = wcKey + "_" + o.getBslpnumber();
			wAsKey = wAsKey.replaceAll(" ","");
			WarehouseArea area = wAs.get(wAsKey);
			WarehouseHistory history = new WarehouseHistory();
			history.setWhtype("領料(" + o.getBslfuser() + ")");
			history.setWhwmslocation(o.getBslfromwho());
			history.setWhcontent(//
					o.getBslclass() + "-" + o.getBslsn() + "-" + //
							o.getBslnb() + "*" + o.getBslpnqty());
			history.setWhwmpnb(o.getBslpnumber());
			history.setWhfuser(o.getBslfuser());
			history.setWheqty(area != null ? area.getWaerptqty() : 0);
			history.setWhcqty(area != null ? area.getWatqty() : 0);
			history.setWhcheckin(o.getBslcheckin() == 0 ? "未核單" : "已核單");
			historyDao.save(history);
		}
		return o;
	}

	// Step3. 立即結算
	public Map<String, Integer> settlementAuto(Map<String, Integer> wAsSave) {
		ArrayList<WarehouseArea> arraySave = new ArrayList<>();
		wAsSave.forEach((key, val) -> {
			// 測試用
//			if(key.equals("A0041_81-105-38210G")) {
//				System.out.println(key);
//			}
			ArrayList<WarehouseArea> arrayList = areaDao.findAllByWaaliasawmpnb(key);
			if (arrayList.size() > 0) {
				val = arrayList.get(0).getWatqty() + val;
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
