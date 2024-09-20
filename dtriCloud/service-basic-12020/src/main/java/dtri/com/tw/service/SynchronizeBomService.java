package dtri.com.tw.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;

import dtri.com.tw.mssql.dao.BommdDao;
import dtri.com.tw.mssql.dao.InvmaDao;
import dtri.com.tw.mssql.entity.Bommd;
import dtri.com.tw.mssql.entity.Invma;
import dtri.com.tw.pgsql.dao.BasicBomIngredientsDao;
import dtri.com.tw.pgsql.dao.BasicProductModelDao;
import dtri.com.tw.pgsql.dao.WarehouseMaterialDao;
import dtri.com.tw.pgsql.entity.BasicBomIngredients;
import dtri.com.tw.pgsql.entity.BasicProductModel;
import dtri.com.tw.pgsql.entity.WarehouseMaterial;
import dtri.com.tw.service.SynchronizeERPService.OutsourcerSynchronizeCell;
import dtri.com.tw.service.feign.BomServiceFeign;
import dtri.com.tw.shared.CloudExceptionService;
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
	BasicProductModelDao modelDao;
	@Autowired
	BasicBomIngredientsDao basicBomIngredientsDao;
	@Autowired
	WarehouseMaterialDao materialDao;
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
	public void erpSynchronizeBomIngredients() throws Exception {
		ArrayList<Bommd> bommds = new ArrayList<Bommd>();
		ArrayList<BasicBomIngredients> boms = basicBomIngredientsDao.findAllByBomList(null, null, null, null, null);
		ArrayList<BasicBomIngredients> bomRemoves = new ArrayList<BasicBomIngredients>();
		ArrayList<BasicBomIngredients> bomNews = new ArrayList<BasicBomIngredients>();
		Map<String, Bommd> erpBommds = new HashMap<String, Bommd>();// ERP整理後資料
		Map<String, WarehouseMaterial> wMs = new HashMap<>();// 物料清單
		// 物料號
		materialDao.findAll().forEach(m -> {
			wMs.put(m.getWmpnb(), m);
		});

		bommds = bommdDao.findAllByBommdFirst();
		// 檢查資料&更正
		for (Bommd bommd : bommds) {
			bommd.setMdcdate(bommd.getMdcdate() == null ? "" : bommd.getMdcdate().replaceAll("\\s", ""));
			bommd.setMdcuser(bommd.getMdcuser() == null ? "" : bommd.getMdcuser().replaceAll("\\s", ""));
			bommd.setMdmdate(bommd.getMdmdate() == null ? "" : bommd.getMdmdate().replaceAll("\\s", ""));
			bommd.setMdmuser(bommd.getMdmuser() == null ? "" : bommd.getMdmuser().replaceAll("\\s", ""));
			bommd.setMd001(bommd.getMd001().replaceAll("\\s", ""));
			bommd.setMd002(bommd.getMd002().replaceAll("\\s", ""));
			bommd.setMd003(bommd.getMd003().replaceAll("\\s", ""));
			erpBommds.put(bommd.getMd001() + "-" + bommd.getMd002(), bommd);
		}
		// 轉換資料
		boms.forEach(o -> {
			if (erpBommds.containsKey(o.getBbisnnb())) {
				erpBommds.get(o.getBbisnnb()).setNewone(false);// 標記舊有資料
				String sum = erpBommds.get(o.getBbisnnb()).toString();
				if (!sum.equals(o.getChecksum())) {
					erpToCloudService.bomIngredients(o, erpBommds.get(o.getBbisnnb()), wMs, sum);
					bomNews.add(o);
				}
			} else {
				// 沒比對到?已經移除?
				bomRemoves.add(o);
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
		// 存入資料
		basicBomIngredientsDao.saveAll(bomNews);
		basicBomIngredientsDao.deleteAll(bomRemoves);
		System.out.println("---");
	}

	// 而外執行(BOM規則同步)
	public void autoBISF() {

		JsonObject sendAllData = new JsonObject();
		sendAllData.addProperty("update", "checkUpdate");
		sendAllData.addProperty("action", "sendAllData");
		// 測試 通知Client->autoSearchTestAndUpdate(BOM 規格檢查)
		AutoBomItemSpecifications specifications = new AutoBomItemSpecifications();
		specifications.setSendAllData(sendAllData.toString());
		specifications.run();
	}

	// 而外執行(BOM規則同步)
	public class AutoBomItemSpecifications implements Runnable {
		private String sendAllData;

		@Override
		public void run() {
			try {
				serviceFeign.autoSearchTestAndUpdate(sendAllData);
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