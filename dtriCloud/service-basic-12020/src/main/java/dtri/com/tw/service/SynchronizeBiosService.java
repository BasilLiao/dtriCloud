package dtri.com.tw.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import dtri.com.tw.pgsql.dao.BasicNotificationMailDao;
import dtri.com.tw.pgsql.dao.BasicProductModelDao;
import dtri.com.tw.pgsql.dao.BiosPrincipalDao;
import dtri.com.tw.pgsql.dao.BiosVersionDao;
import dtri.com.tw.pgsql.entity.BasicCommandList;
import dtri.com.tw.pgsql.entity.BasicNotificationMail;
import dtri.com.tw.pgsql.entity.BasicProductModel;
import dtri.com.tw.pgsql.entity.BiosPrincipal;
import dtri.com.tw.pgsql.entity.BiosVersion;
import dtri.com.tw.shared.Fm_T;

@Service
public class SynchronizeBiosService {

	// Cloud
	@Autowired
	BasicProductModelDao modelDao;
	@Autowired
	BiosVersionDao biosVersionDao;
	@Autowired
	BiosPrincipalDao biosPrincipalDao;
	@Autowired
	BasicNotificationMailDao notificationMailDao;

	// 檢查是否有新的製令單
	public void biosNewOrderCheck(Map<String, BasicCommandList> commandMaps) throws Exception {
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

	}

	// 檢查是否有N+1版本過時
	public void versionCheckBios() throws Exception {
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
		notificationMailDao.saveAll(readyNeedMails);
	}

	// ============ 同步機種別toBios() ============
	public void erpSynchronizeProductModeltoBios() throws Exception {
		ArrayList<BasicProductModel> models = modelDao.findAllBySearch(null, null, null);
		ArrayList<BiosVersion> biosVers = biosVersionDao.findAllBySearch(null, null, null, null, null, null);
		ArrayList<BiosVersion> newBiosVers = new ArrayList<BiosVersion>();

		// 轉換
		Map<String, BiosVersion> mapBvms = new HashMap<String, BiosVersion>();
		biosVers.forEach(y -> {
			mapBvms.put(y.getBvmodel(), y);
		});
		// 比對?->如果有->舊的(false)
		models.forEach(x -> {
			// Bios
			if (!mapBvms.containsKey(x.getBpmname())) {
				BiosVersion biosVer = new BiosVersion();
				biosVer.setBvid(null);
				biosVer.setBvmodel(x.getBpmname());
				newBiosVers.add(biosVer);
			}
		});
		biosVersionDao.saveAll(newBiosVers);
	}
}