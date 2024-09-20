package dtri.com.tw.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dtri.com.tw.pgsql.dao.BomKeeperDao;
import dtri.com.tw.pgsql.dao.BomProductRuleDao;
import dtri.com.tw.pgsql.entity.BomKeeper;
import dtri.com.tw.pgsql.entity.BomProductRule;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.CloudExceptionService.ErCode;
import dtri.com.tw.shared.CloudExceptionService.ErColor;
import dtri.com.tw.shared.CloudExceptionService.Lan;
import dtri.com.tw.shared.Fm_T;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;

@Service
public class BomProductRuleServiceAc {

	@Autowired
	private PackageService packageService;

	@Autowired
	private BomProductRuleDao productRuleDao;

	@Autowired
	private BomKeeperDao bomKeeperDao;

	@Autowired
	private EntityManager em;

	/** 取得資料 */
	public PackageBean getSearch(PackageBean packageBean) throws Exception {
		// ========================分頁設置========================
		// Step1.批次分頁
		// JsonObject pageSetJson =
		// JsonParser.parseString(packageBean.getSearchPageSet()).getAsJsonObject();
		// int total = pageSetJson.get("total").getAsInt();
		// int batch = pageSetJson.get("batch").getAsInt();

		// Step2.排序
		List<Order> ordersBPR = new ArrayList<>();
		ordersBPR.add(new Order(Direction.ASC, "bprname"));//

		// 一般模式
		PageRequest pageable = PageRequest.of(0, 200, Sort.by(ordersBPR));

		// ========================區分:訪問/查詢========================
		if (packageBean.getEntityJson() == "") {
			// 不會有訪問
		} else {
			// Step4-0.哪一種查詢other_BBI/other_BPR/other_BPM
			String otherType = packageBean.getOtherSet();
			JsonObject other = new JsonObject();
			// Step3-1.取得資料(一般/細節)
			ArrayList<BomProductRule> entitys = new ArrayList<BomProductRule>();

			// Step4-1. 取得資料(一般/細節)
			BomProductRule searchData = packageService.jsonToBean(packageBean.getEntityJson(), BomProductRule.class);
			// BPR BOM規則
			ArrayList<BomProductRule> entityBPR = productRuleDao.findAllBySearch(searchData.getBprname(),
					searchData.getBprbisitem(), null, pageable);
			// 資料放入
			String entityJsonBPR = packageService.beanToJson(entityBPR);
			other.add("BPR", packageService.StringToAJson(entityJsonBPR));

			// 類別(一般模式)
			String entityJson = packageService.beanToJson(entitys);
			// 資料包裝
			packageBean.setEntityJson(entityJson);
			packageBean.setOtherSet(other.toString());
			packageBean.setCallBackValue(otherType);
			// 查不到資料
			if (!(packageBean.getEntityJson().equals("[]") || packageBean.getOtherSet().equals("{}"))) {
				throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1000, Lan.zh_TW, null);
			}

		}
		// ========================配置共用參數========================
		// Step5. 取得資料格式/(主KEY/群組KEY)
		// 資料格式
		String entityFormatBPR = packageService.beanToJson(new BomProductRule());
		JsonObject entityFormats = new JsonObject();
		entityFormats.addProperty("BPR", entityFormatBPR);
		packageBean.setEntityFormatJson(entityFormats.toString());
		// KEY名稱Ikey_Gkey
		JsonObject entityIKeyGKeys = new JsonObject();
		entityIKeyGKeys.addProperty("BPR", "bprid_");
		packageBean.setEntityIKeyGKey(entityIKeyGKeys.toString());
		packageBean.setEntityDateTime(packageBean.getEntityDateTime());
		return packageBean;
	}

	/** 修改資料 */
	@Transactional
	public PackageBean setModify(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<BomProductRule> saveDatasUpdate = new ArrayList<BomProductRule>();
		ArrayList<BomProductRule> entityDatas = new ArrayList<>();
		ArrayList<BomKeeper> bomKeepers = bomKeeperDao.findAllBySearch(packageBean.getUserAccount(), null, null, null);

		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<BomProductRule>>() {
					});

			// Step2.資料檢查
			for (BomProductRule entityData : entityDatas) {
				// 檢查-舊資料-名稱重複(有資料 && 不是同一筆資料)
				ArrayList<BomProductRule> checkDatas = productRuleDao.findAllByCheck(entityData.getBprname(), null);
				for (BomProductRule checkData : checkDatas) {
					// 排除自己
					if (entityData.getBprid() != null && checkData.getBprid().compareTo(entityData.getBprid()) != 0) {
						throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
								new String[] { entityData.getBprname() });
					}
				}
				// Step2-1.檢查-新資料-資料檢查-缺少值?
				if (entityData.getBprmodel() == null || entityData.getBprmodel().equals("") || // 型號
						entityData.getBprname() == null || entityData.getBprname().equals("") || // 規則名稱
						entityData.getBprtype() == null || entityData.getBprtype() < 0 || // 成品類
						entityData.getBprbpsnv() == null || entityData.getBprbpsnv().equals("") || // 參數
						entityData.getBprbisitem() == null || entityData.getBprbisitem().equals("")) {// 物料匹配規格
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1003, Lan.zh_TW,
							new String[] { entityData.getBprname() });
				}
				// Step2-3.檢查匹配權限?
				BomProductRule checkDataOne = productRuleDao.getReferenceById(entityData.getBprid());
				Boolean throwCheck = true;
				String info = checkDataOne.getBprname() + " / " + checkDataOne.getBprmodel();
				for (BomKeeper keeper : bomKeepers) {
					String bkmodel = keeper.getBkmodel();
					String bknb = keeper.getBknb();
					// 2=normal(增改刪)/1=limited(改)/0=disabled(禁用)
					if (keeper.getBktype() == 0) {
						// 沒權限?
						if (!bkmodel.equals("") && (checkDataOne.getBprmodel().contains(bkmodel)
								|| entityData.getBprmodel().contains(bkmodel))) {
							throwCheck = true;
							info = bkmodel;
							break;
						}
						if (!bknb.equals("") && (checkDataOne.getBprname().contains(bknb)
								|| entityData.getBprname().contains(bknb))) {
							throwCheck = true;
							info = bknb;
							break;
						}
					} else if (keeper.getBktype() == 1 || keeper.getBktype() == 2) {
						// 有權限?
						info = bknb + " / " + bkmodel;
						if (!bkmodel.equals("") && (checkDataOne.getBprmodel().contains(bkmodel)
								&& entityData.getBprmodel().contains(bkmodel))) {
							// 有權限
							throwCheck = false;
						} else if (!bknb.equals("") && (checkDataOne.getBprname().contains(bknb)
								&& entityData.getBprname().contains(bknb))) {
							// 有權限
							throwCheck = false;
						}
					}
				}
				// 沒權限?
				if (throwCheck) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1006, Lan.zh_TW,
							new String[] { "This account has no permissions : " + packageBean.getUserAccount() + " : "
									+ info });
				}
			}
		}

		// =======================資料整理=======================
		// Step3.一般資料->寫入
		entityDatas.forEach(c -> {// 要更新的資料
			if (c.getBprid() != null) {
				BomProductRule oldData = productRuleDao.getReferenceById(c.getBprid());
				// 可能->新的?
				oldData.setSysmdate(new Date());
				oldData.setSysmuser(packageBean.getUserAccount());
				oldData.setSysodate(new Date());
				oldData.setSysouser(packageBean.getUserAccount());
				oldData.setSysheader(false);
				oldData.setSyssort(0);
				oldData.setSysstatus(0);
				oldData.setSysnote(c.getSysnote());
				oldData.setBprbisitem(c.getBprbisitem());
				oldData.setBprbpsnv(c.getBprbpsnv());
				oldData.setBprmodel(c.getBprmodel());
				oldData.setBprname(c.getBprname());
				oldData.setBprtype(c.getBprtype());
				oldData.setBprtypename(c.getBprtypename());
				saveDatasUpdate.add(oldData);
			}
		});

		// =======================資料儲存=======================
		// 資料Data
		productRuleDao.saveAll(saveDatasUpdate);
		packageBean.setCallBackValue("BPR");
		return packageBean;
	}

	/** 新增資料 */
	// @Transactional
	public PackageBean setAdd(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<BomProductRule> entityDatas = new ArrayList<>();
		ArrayList<BomProductRule> entitySave = new ArrayList<>();
		ArrayList<BomKeeper> bomKeepers = bomKeeperDao.findAllBySearch(packageBean.getUserAccount(), null, null, null);
		// =======================資料檢查=======================
		// 一般BOM規格
		if (packageBean.getOtherSet().equals("BPR")) {
			if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
				// Step1.資料轉譯(一般)
				entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
						new TypeReference<ArrayList<BomProductRule>>() {
						});
				// Step1-1.負責人配置?
				if (bomKeepers.size() == 0) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1006, Lan.zh_TW,
							new String[] { "This account has no permissions :" + packageBean.getUserAccount() });
				}
				// Step2-1.資料檢查
				for (BomProductRule entityData : entityDatas) {
					// 檢查-名稱重複(有資料 && 不是同一筆資料)
					ArrayList<BomProductRule> checkDatas = productRuleDao.findAllByCheck(entityData.getBprname(), null);
					if (checkDatas.size() != 0) {
						throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
								new String[] { entityData.getBprname() });
					}
					// Step2-2.資料檢查-缺少值?
					if (entityData.getBprmodel() == null || entityData.getBprmodel().equals("") || // 型號
							entityData.getBprname() == null || entityData.getBprname().equals("") || // 成品名稱
							entityData.getBprtype() == null || entityData.getBprtype() < 0 || // 成品類
							entityData.getBprbpsnv() == null || entityData.getBprbpsnv().equals("") || // 參數
							entityData.getBprbisitem() == null || entityData.getBprbisitem().equals("")) {// 物料匹配規格
						throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1003, Lan.zh_TW,
								new String[] { entityData.getBprname() });
					}
					// Step2-3.檢查匹配權限?
					Boolean throwCheck = true;
					String info = entityData.getBprname() + " / " + entityData.getBprmodel();
					for (BomKeeper keeper : bomKeepers) {
						String bkmodel = keeper.getBkmodel();
						String bknb = keeper.getBknb();
						// 2=normal(增改刪)/1=limited(改)/0=disabled(禁用)
						if (keeper.getBktype() == 0 || keeper.getBktype() == 1) {
							// 沒權限?
							if (!bkmodel.equals("") && entityData.getBprmodel().contains(bkmodel)) {
								throwCheck = true;
								info = bkmodel;
								break;
							}
							if (!bknb.equals("") && entityData.getBprname().contains(bknb)) {
								throwCheck = true;
								info = bknb;
								break;
							}
						} else {
							// 有權限?
							info = bknb + " / " + bkmodel;
							if (!bknb.equals("") && entityData.getBprname().contains(bknb)) {
								// 有權限
								throwCheck = false;
								break;
							} else if (!bkmodel.equals("") && entityData.getBprmodel().contains(bkmodel)) {
								// 有權限
								throwCheck = false;
								break;
							}
						}
					}
					if (throwCheck) {
						throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1006, Lan.zh_TW,
								new String[] { "This account has no permissions : " + packageBean.getUserAccount()
										+ " : " + info });
					}
				}
			}
			// =======================資料整理=======================
			for (BomProductRule x : entityDatas) {
				// 新增
				x.setBprid(null);
				x.setSysmdate(new Date());
				x.setSysmuser(packageBean.getUserAccount());
				x.setSysodate(new Date());
				x.setSysouser(packageBean.getUserAccount());
				x.setSyscdate(new Date());
				x.setSyscuser(packageBean.getUserAccount());
				x.setSysheader(false);
				x.setSyssort(0);
				x.setSysstatus(0);
				entitySave.add(x);
			}
			// =======================資料儲存=======================
			// 資料Data
			productRuleDao.saveAll(entitySave);
			packageBean.setCallBackValue("BPR");
		}

		return packageBean;
	}

	/** 作廢資料 */
	@Transactional
	public PackageBean setInvalid(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<BomProductRule> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<BomProductRule>>() {
					});
			// Step2.資料檢查
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<BomProductRule> saveDatas = new ArrayList<>();
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getBprid() != null) {
				BomProductRule entityDataOld = productRuleDao.findById(x.getBprid()).get();
				entityDataOld.setSysmdate(new Date());
				entityDataOld.setSysmuser(packageBean.getUserAccount());
				entityDataOld.setSysstatus(2);
				saveDatas.add(entityDataOld);
			}
		});
		// =======================資料儲存=======================
		// 資料Data
		productRuleDao.saveAll(saveDatas);
		return packageBean;
	}

	/** 移除資料 */
	@Transactional
	public PackageBean setDetele(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<BomProductRule> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<BomProductRule>>() {
					});
			// Step2.資料檢查
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<BomProductRule> saveDatas = new ArrayList<>();
		// 一般-移除內容
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getBprid() != null) {
				BomProductRule entityDataOld = productRuleDao.getReferenceById(x.getBprid());
				saveDatas.add(entityDataOld);
			}
		});

		// =======================資料儲存=======================
		// 資料Data
		productRuleDao.deleteAll(saveDatas);
		packageBean.setCallBackValue("BPR");
		return packageBean;
	}

	/** 取得資料 */
	// @Transactional
	@SuppressWarnings("unchecked")
	public PackageBean getReport(PackageBean packageBean) throws Exception {
		String entityReport = packageBean.getEntityReportJson();
		JsonArray reportAry = packageService.StringToAJson(entityReport);
		List<BomProductRule> entitys = new ArrayList<>();
		Map<String, String> sqlQuery = new HashMap<>();
		// =======================查詢語法=======================
		// 拼湊SQL語法
		String nativeQuery = "SELECT e.* FROM bom_product_rule e Where ";
		for (JsonElement x : reportAry) {
			// entity 需要轉換SQL與句 && 欄位
			String cellName = x.getAsString().split("<_>")[0];
			cellName = cellName.replace("sys", "sys_");
			cellName = cellName.replace("sys_m", "sys_m_");
			cellName = cellName.replace("sys_c", "sys_c_");
			cellName = cellName.replace("sys_o", "sys_o_");
			cellName = cellName.replace("bpr", "bpr_");

			cellName = cellName.replace("bpr_bisitem", "bpr_bis_item");
			cellName = cellName.replace("bpr_bpsnv", "bpr_bps_nv");
			cellName = cellName.replace("bpr_typename", "bpr_type_name");

			String where = x.getAsString().split("<_>")[1];
			String value = x.getAsString().split("<_>")[2];// 有可能空白
			String valueType = x.getAsString().split("<_>")[3];

			switch (where) {
			case "AllSame":
				nativeQuery += "(e." + cellName + " = :" + cellName + ") AND ";
				sqlQuery.put(cellName, value + "<_>" + valueType);
				break;
			case "NotSame":
				nativeQuery += "(e." + cellName + " != :" + cellName + ") AND ";
				sqlQuery.put(cellName, value + "<_>" + valueType);
				break;
			case "Like":
				nativeQuery += "(e." + cellName + " LIKE :" + cellName + ") AND ";
				sqlQuery.put(cellName, "%" + value + "%<_>" + valueType);
				break;
			case "NotLike":
				nativeQuery += "(e." + cellName + "NOT LIKE :" + cellName + ") AND ";
				sqlQuery.put(cellName, "%" + value + "%<_>" + valueType);
				break;
			case "MoreThan":
				nativeQuery += "(e." + cellName + " >= :" + cellName + ") AND ";
				sqlQuery.put(cellName, value + "<_>" + valueType);
				break;
			case "LessThan":
				nativeQuery += "(e." + cellName + " <= :" + cellName + ") AND ";
				sqlQuery.put(cellName, value + "<_>" + valueType);
				break;
			}
		}

		nativeQuery = StringUtils.removeEnd(nativeQuery, "AND ");
		nativeQuery += " order by e.bpr_model asc,e.bpr_name asc";
		nativeQuery += " LIMIT 2500 OFFSET 0 ";
		Query query = em.createNativeQuery(nativeQuery, BomProductRule.class);
		// =======================查詢參數=======================
		sqlQuery.forEach((key, valAndType) -> {
			String val = valAndType.split("<_>")[0];
			String tp = valAndType.split("<_>")[1];
			if (tp.equals("dateTime")) {
				// 時間格式?
				query.setParameter(key, Fm_T.toDate(val));
			} else if (tp.equals("number")) {
				// 數字?
				query.setParameter(key, Integer.parseInt(val));
			} else {
				// 文字?
				query.setParameter(key, val);
			}
		});
		try {
			entitys = query.getResultList();
		} catch (PersistenceException e) {
			throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1004, Lan.zh_TW, null);
		}

		// 資料包裝
		String entityJsonDatas = packageService.beanToJson(entitys);
		packageBean.setEntityJson(entityJsonDatas);

		return packageBean;
	}
}
