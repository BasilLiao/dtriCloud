package dtri.com.tw.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
import com.google.gson.JsonParser;

import dtri.com.tw.pgsql.dao.MaterialReplacementDao;
import dtri.com.tw.pgsql.dao.MaterialReplacementGroupDao;
import dtri.com.tw.pgsql.dao.MaterialShortageDao;
import dtri.com.tw.pgsql.dao.BasicShippingListDao;
import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.dao.WarehouseMaterialDao;
import dtri.com.tw.pgsql.dto.MaterialShortageDto;
import dtri.com.tw.pgsql.dto.WarehouseMaterialDto;
import dtri.com.tw.pgsql.entity.MaterialReplacement;
import dtri.com.tw.pgsql.entity.MaterialReplacementGroup;
import dtri.com.tw.pgsql.entity.MaterialReplacementItem;
import dtri.com.tw.pgsql.entity.SystemLanguageCell;
import dtri.com.tw.pgsql.entity.WarehouseMaterial;
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

/** 功能模組: 通用-替代料關聯 */
@Service
public class MaterialReplacementServiceAc {

	@Autowired
	private PackageService packageService;
	@Autowired
	private SystemLanguageCellDao languageDao;
	@Autowired
	private MaterialReplacementDao materialReplacementDao;

	@Autowired
	private WarehouseMaterialDao warehouseMaterialDao;

	@Autowired
	private MaterialReplacementGroupDao materialReplacementGroupDao;

	@Autowired
	private MaterialShortageDao shortageDao;

	@Autowired
	private BasicShippingListDao shippingDao;

	@Autowired
	private EntityManager em;

	@Transactional(readOnly = true)
	public PackageBean getData(PackageBean packageBean) throws Exception {
		// 1. 分頁設定
		JsonObject pageSetJson = JsonParser.parseString(packageBean.getSearchPageSet()).getAsJsonObject();
		int total = pageSetJson.get("total").getAsInt();
		int batch = pageSetJson.get("batch").getAsInt();
		// 依照建立時間排序，新的在上面
		PageRequest pageable = PageRequest.of(batch, total, Sort.by(Direction.DESC, "syscdate"));

		// ================= Query 模式 (執行搜尋) =================
		// 這裡不再包含重型字典預載，秒開！
		JsonObject searchData = JsonParser.parseString(packageBean.getEntityJson()).getAsJsonObject();

		Integer statusParam = 0;
		if (searchData.has("sysstatus") && !searchData.get("sysstatus").getAsString().equals("null")) {
			statusParam = searchData.get("sysstatus").getAsInt();
		} else if (searchData.has("sysstatus") && searchData.get("sysstatus").getAsString().equals("null")) {
			statusParam = null;
		}

		Integer scopeTypeParam = null;
		if (searchData.has("scopetype") && !searchData.get("scopetype").getAsString().isEmpty()) {
			scopeTypeParam = searchData.get("scopetype").getAsInt();
		}

		String scopeValParam = null;
		if (searchData.has("scopeval") && !searchData.get("scopeval").getAsString().isEmpty()) {
			scopeValParam = searchData.get("scopeval").getAsString();
		}

		String keywordParam = null;
		if (searchData.has("mrgnb") && !searchData.get("mrgnb").getAsString().isEmpty()) {
			keywordParam = searchData.get("mrgnb").getAsString();
		} else if (searchData.has("mrnb") && !searchData.get("mrnb").getAsString().isEmpty()) {
			// [修正] 增加支援前端專用 mrnb 參數搜尋
			keywordParam = searchData.get("mrnb").getAsString();
		}

		Integer finalStatusParam = statusParam;
		Integer finalScopeTypeParam = scopeTypeParam;
		String finalScopeValParam = scopeValParam;
		String finalKeywordParam = keywordParam;

		Page<MaterialReplacementGroup> entityPage = materialReplacementGroupDao.findAll((root, query, cb) -> {
			query.distinct(true);
			List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

			if (finalStatusParam != null) {
				predicates.add(cb.equal(root.get("sysstatus"), finalStatusParam));
			}
			if (finalScopeTypeParam != null) {
				predicates.add(cb.equal(root.get("scopetype"), finalScopeTypeParam));
			}
			if (finalScopeValParam != null && !finalScopeValParam.trim().isEmpty()) {
				predicates.add(cb.like(root.get("scopeval"), "%" + finalScopeValParam.trim() + "%"));
			}

			if (finalKeywordParam != null && !finalKeywordParam.trim().isEmpty()) {
				String[] kws = finalKeywordParam.split("[,\\s]+");
				List<jakarta.persistence.criteria.Predicate> orPreds = new ArrayList<>();
				jakarta.persistence.criteria.Join<Object, Object> itemsJoin = root.join("items", jakarta.persistence.criteria.JoinType.LEFT);
				
				for (String kw : kws) {
					if (kw.trim().isEmpty()) continue;
					String pattern = "%" + kw.trim() + "%";
					orPreds.add(cb.like(root.get("mrgnb"), pattern));
					orPreds.add(cb.like(itemsJoin.get("mrnb"), pattern));
				}
				if (!orPreds.isEmpty()) {
					predicates.add(cb.or(orPreds.toArray(new jakarta.persistence.criteria.Predicate[0])));
				}
			}

			return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
		}, pageable);

		ArrayList<MaterialReplacementGroup> entities = new ArrayList<>(entityPage.getContent());

		String entityJson = packageService.beanToJson(entities);
		packageBean.setEntityJson(entityJson);
		packageBean.setEntityDetailJson("[]");
		packageBean.setSearchPageSet(Math.toIntExact(entityPage.getTotalElements()), entityPage.getNumber());

		// 只有在「非第一頁」時才檢查空結果
		if (entities.isEmpty() && entityPage.getNumber() != 0) {
			throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1000, Lan.zh_TW, null);
		}

		String entityFormatJson = packageService.beanToJson(new MaterialReplacementGroup());
		packageBean.setEntityFormatJson(entityFormatJson);
		packageBean.setEntityIKeyGKey("mrgid");

		// [新增] 為了消除前端繪製延遲，這裡直接在極速的 getData 查詢中一併給予表頭結構與語系
		JsonObject searchSetJsonAll = new JsonObject();
		Map<String, SystemLanguageCell> mapLanguages = new HashMap<>();
		ArrayList<SystemLanguageCell> languages = languageDao.findAllByLanguageCellSame("MaterialReplacementGroup", null, 2);
		languages.forEach(x -> mapLanguages.put(x.getSltarget(), x));

		JsonObject resultDataTJsons = packageService.resultSet(MaterialReplacementGroup.class.getDeclaredFields(),
				new ArrayList<>(), mapLanguages);
		searchSetJsonAll.add("resultThead", resultDataTJsons);

		JsonObject uiLang = new JsonObject();
		ArrayList<SystemLanguageCell> uiLangs = new ArrayList<>();
		uiLangs.addAll(languageDao.findAllByLanguageCellSame("MaterialReplacement", null, 1));
		uiLangs.addAll(languages);
		uiLangs.addAll(languageDao.findAllByLanguageCellSame("MaterialReplacementItem", null, 2));
		uiLangs.addAll(languageDao.findAllByLanguageCellSame("MaterialFront", null, 1));
		uiLangs.addAll(languageDao.findAllByLanguageCellSame("MaterialFront", null, 2));
		uiLangs.forEach(x -> {
			if (x.getSllanguage() != null && !x.getSllanguage().isEmpty()) {
				uiLang.addProperty(x.getSltarget(), x.getSllanguage());
			}
		});

		searchSetJsonAll.add("uiLang", uiLang);
		packageBean.setSearchSet(searchSetJsonAll.toString());

		return packageBean;
	}

	/** [新增] 非同步預載字典資料 (物料/客戶/產品) */
	@Transactional(readOnly = true)
	public PackageBean getPreloadData(PackageBean packageBean) throws Exception {
		JsonObject searchSetJsonAll = new JsonObject();

		// 2. 預載物料清單 (6.6w 筆)
		List<WarehouseMaterialDto> allMaterials = warehouseMaterialDao.findAllActiveMaterials();
		JsonArray materialJsonArray = new JsonArray();
		for (WarehouseMaterialDto m : allMaterials) {
			JsonObject obj = new JsonObject();
			String pn = m.getWmpnb();
			String name = m.getWmname();
			obj.addProperty("pn", pn);
			obj.addProperty("name", name);
			// 搜尋字串轉小寫優化
			String searchStr = (pn == null ? "" : pn) + " " + (name == null ? "" : name);
			obj.addProperty("s", searchStr.toLowerCase());
			materialJsonArray.add(obj);
		}
		searchSetJsonAll.add("preloadMaterials", materialJsonArray);

		// 3. 預載客戶清單
		List<MaterialShortageDto> allCustomers = shortageDao.findDistinctCustomers();
		List<MaterialShortageDto> shippingCustomers = shippingDao.findDistinctCustomers();
		Map<String, MaterialShortageDto> customerMap = new HashMap<>();
		// 優先放入 Shipping 的客戶名單
		for (MaterialShortageDto c : shippingCustomers) {
			if (c.getTc004() != null) customerMap.put(c.getTc004(), c);
		}
		// 再放入 Shortage (ERP 同步) 的客戶名單，若重複則覆蓋 (以保持 ERP 更新度)
		for (MaterialShortageDto c : allCustomers) {
			if (c.getTc004() != null) customerMap.put(c.getTc004(), c);
		}

		JsonArray customerJsonArray = new JsonArray();
		for (MaterialShortageDto c : customerMap.values()) {
			JsonObject obj = new JsonObject();
			String code = c.getTc004();
			String name = c.getCopma002();
			obj.addProperty("code", code);
			obj.addProperty("name", name);
			String searchStr = (code == null ? "" : code) + " " + (name == null ? "" : name);
			obj.addProperty("s", searchStr.toLowerCase());
			customerJsonArray.add(obj);
		}
		searchSetJsonAll.add("preloadCustomers", customerJsonArray);

		// 4. 預載產品清單
		List<MaterialShortageDto> allProducts = shortageDao.findDistinctProducts();
		List<MaterialShortageDto> shippingProducts = shippingDao.findDistinctProducts();
		Map<String, MaterialShortageDto> productMap = new HashMap<>();
		for (MaterialShortageDto p : shippingProducts) {
			if (p.getTk003() != null) productMap.put(p.getTk003(), p);
		}
		for (MaterialShortageDto p : allProducts) {
			if (p.getTk003() != null) productMap.put(p.getTk003(), p);
		}

		JsonArray productJsonArray = new JsonArray();
		for (MaterialShortageDto p : productMap.values()) {
			JsonObject obj = new JsonObject();
			String code = p.getTk003();
			obj.addProperty("code", code);
			obj.addProperty("s", code != null ? code.toLowerCase() : "");
			productJsonArray.add(obj);
		}
		searchSetJsonAll.add("preloadProducts", productJsonArray);

		packageBean.setSearchSet(searchSetJsonAll.toString());
		packageBean.setEntityJson("[]");
		packageBean.setEntityDetailJson("[]");
		return packageBean;
	}

	@Transactional(rollbackFor = Exception.class)
	public PackageBean doSave(PackageBean packageBean) throws Exception {
		// 1. 解包前端傳來的 JSON 陣列
		JsonArray listJson = JsonParser.parseString(packageBean.getEntityJson()).getAsJsonArray();
		ArrayList<MaterialReplacementGroup> savedGroups = new ArrayList<>();

		// 2. 取得當前操作者
		String currentUser = packageBean.getUserAccount() != null ? packageBean.getUserAccount() : "system";

		for (int i = 0; i < listJson.size(); i++) {
			JsonObject obj = listJson.get(i).getAsJsonObject();

			// --- A. 欄位驗證 ---
			String mrgnb = obj.has("mrgnb") && !obj.get("mrgnb").isJsonNull() ? obj.get("mrgnb").getAsString() : "";
			if (mrgnb.isEmpty()) {
				// [修正] 改用 W1003 (資料缺少)，並傳入 String[] 陣列來替換 ${0}
				// 結果訊息會變成: "[W1003] 資料缺少 : 規則名稱 !!"
				throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1003, Lan.zh_TW,
						new String[] { "規則名稱" });
			}

			// --- B. 判斷 新增/修改 ---
			MaterialReplacementGroup groupEntity;
			Long mrgId = (obj.has("mrgid") && !obj.get("mrgid").isJsonNull()
					&& !obj.get("mrgid").getAsString().isEmpty())
							? obj.get("mrgid").getAsLong()
							: null;

			if (mrgId != null) {
				// [Update]
				groupEntity = materialReplacementGroupDao.findById(mrgId).orElse(null);
				if (groupEntity == null) {
					// [修正] W1000 (查無資料) 不需要參數，所以傳 null
					throw new CloudExceptionService(packageBean, ErColor.danger, ErCode.W1000, Lan.zh_TW, null);
				}
				groupEntity.setSysmdate(new Date());
				groupEntity.setSysmuser(currentUser);
			} else {
				// [Insert]
				groupEntity = new MaterialReplacementGroup();
				groupEntity.setSyscdate(new Date());
				groupEntity.setSyscuser(currentUser);
				groupEntity.setSysmdate(new Date());
				groupEntity.setSysmuser(currentUser);
			}

			// --- C. 寫入主檔 ---
			groupEntity.setMrgnb(mrgnb);
			groupEntity.setScopetype(obj.has("scopetype") ? obj.get("scopetype").getAsInt() : 0);
			groupEntity.setScopeval(
					obj.has("scopeval") && !obj.get("scopeval").isJsonNull() ? obj.get("scopeval").getAsString()
							: null);
			groupEntity.setPolicy(obj.has("policy") ? obj.get("policy").getAsString() : "EQUIVALENT");
			groupEntity.setSysnote(
					obj.has("sysnote") && !obj.get("sysnote").isJsonNull() ? obj.get("sysnote").getAsString() : "");
			groupEntity.setSysstatus(obj.has("sysstatus") ? obj.get("sysstatus").getAsInt() : 0);

			// --- D. 處理明細 ---
			groupEntity.getItems().clear();

			if (obj.has("items")) {
				JsonArray itemsJson = obj.get("items").getAsJsonArray();
				for (int j = 0; j < itemsJson.size(); j++) {
					JsonObject itemObj = itemsJson.get(j).getAsJsonObject();

					MaterialReplacementItem newItem = new MaterialReplacementItem();
					newItem.setMrnb(itemObj.get("mrnb").getAsString());
					newItem.setQty(itemObj.get("qty").getAsDouble());
					newItem.setRole(itemObj.get("role").getAsString());

					newItem.setSyscdate(new Date());
					newItem.setSyscuser(currentUser);

					groupEntity.addItem(newItem);
				}
			}

			if (groupEntity.getItems().isEmpty()) {
				// [修正] 改用 W1003 (資料缺少)，並傳入 String[]
				// 結果訊息會變成: "[W1003] 資料缺少 : 明細資料 !!"
				throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1003, Lan.zh_TW,
						new String[] { "明細資料" });
			}

			// --- E. 存檔 ---
			savedGroups.add(materialReplacementGroupDao.save(groupEntity));
		}

		packageBean.setEntityJson("{}");
		// 回傳最新結果
		return getData(packageBean);
	}

	public PackageBean getSearch(PackageBean packageBean) throws Exception {
		// ========================分頁設置========================
		// Step1.批次分頁
		JsonObject pageSetJson = JsonParser.parseString(packageBean.getSearchPageSet()).getAsJsonObject();
		int total = pageSetJson.get("total").getAsInt();
		int batch = pageSetJson.get("batch").getAsInt();

		// Step2.排序
		List<Order> orders = new ArrayList<>();
		orders.add(new Order(Direction.ASC, "mrnb"));// 物料號
		orders.add(new Order(Direction.DESC, "mrnote"));// 物料備註

		List<Order> ordersW = new ArrayList<>();
		ordersW.add(new Order(Direction.ASC, "wmpnb"));// 物料號

		// 一般模式
		PageRequest pageable = PageRequest.of(batch, total, Sort.by(orders));
		PageRequest pageableW = PageRequest.of(batch, total, Sort.by(ordersW));

		// ========================區分:訪問/查詢========================
		if (packageBean.getEntityJson() == "") {// 訪問

			// Step3-1.取得資料(一般/細節)
			ArrayList<MaterialReplacement> entitys = materialReplacementDao.findAllBySearch(null, null, null, pageable);

			// Step3-2.資料區分(一般/細節)

			// 類別(一般模式)
			String entityJson = packageService.beanToJson(entitys);
			// 資料包裝
			packageBean.setEntityJson(entityJson);
			packageBean.setEntityDetailJson("{}");

			// ========================建立:查詢欄位/對應翻譯/修改選項========================
			// Step3-3. 取得翻譯(一般/細節)
			Map<String, SystemLanguageCell> mapLanguages = new HashMap<>();
			// 一般翻譯
			ArrayList<SystemLanguageCell> languages = languageDao.findAllByLanguageCellSame("MaterialReplacement", null,
					2);
			languages.forEach(x -> {
				mapLanguages.put(x.getSltarget(), x);
			});
			// 動態->覆蓋寫入->修改UI選項

			// Step3-4. 欄位設置
			JsonObject searchSetJsonAll = new JsonObject();
			JsonArray searchJsons = new JsonArray();// 查詢設定
			JsonObject resultDataTJsons = new JsonObject();// 回傳欄位-一般名稱
			JsonObject resultDetailTJsons = new JsonObject();// 回傳欄位-細節名稱
			// 結果欄位(名稱Entity變數定義)=>取出=>排除/寬度/語言/順序
			Field[] fields = MaterialReplacement.class.getDeclaredFields();
			// 排除欄位
			ArrayList<String> exceptionCell = new ArrayList<>();
			// exceptionCell.add("systemgroups");

			// 欄位翻譯(一般)
			resultDataTJsons = packageService.resultSet(fields, exceptionCell, mapLanguages);

			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "mrnb", "Ex:物料號?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "mrsubnote", "Ex:替代料-備註?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "mrnote", "Ex:物料說明?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);

			// 查詢包裝/欄位名稱(一般/細節)
			searchSetJsonAll.add("searchSet", searchJsons);
			searchSetJsonAll.add("resultThead", resultDataTJsons);
			searchSetJsonAll.add("resultDetailThead", resultDetailTJsons);

			// [新增] 多語系 UI 標籤，用於自定義分頁與按鈕
			JsonObject uiLang = new JsonObject();
			ArrayList<SystemLanguageCell> uiLangs = new ArrayList<>();
			uiLangs.addAll(languageDao.findAllByLanguageCellSame("MaterialReplacement", null, 1));
			uiLangs.addAll(languageDao.findAllByLanguageCellSame("MaterialReplacementGroup", null, 2));
			uiLangs.addAll(languageDao.findAllByLanguageCellSame("MaterialReplacementItem", null, 2));
			uiLangs.addAll(languageDao.findAllByLanguageCellSame("MaterialFront", null, 1));
			uiLangs.addAll(languageDao.findAllByLanguageCellSame("MaterialFront", null, 2));
			uiLangs.forEach(x -> {
				if (x.getSllanguage() != null && !x.getSllanguage().isEmpty()) {
					uiLang.addProperty(x.getSltarget(), x.getSllanguage());
				}
			});
			searchSetJsonAll.add("uiLang", uiLang);

			packageBean.setSearchSet(searchSetJsonAll.toString());
		} else {
			// Step4-1. 取得資料(一般/細節)
			MaterialReplacement searchData = packageService.jsonToBean(packageBean.getEntityJson(),
					MaterialReplacement.class);

			HashMap<String, MaterialReplacement> entitysAll = new HashMap<String, MaterialReplacement>();
			ArrayList<MaterialReplacement> materialsAll = new ArrayList<MaterialReplacement>();
			ArrayList<MaterialReplacement> entityUpdates = new ArrayList<MaterialReplacement>();// 可能名稱有需要更新
			// 輔助查詢
			ArrayList<MaterialReplacement> entitys = materialReplacementDao.findAllBySearch(searchData.getMrnb(),
					searchData.getMrnote(), searchData.getMrsubnote(), pageable);
			ArrayList<WarehouseMaterial> materials = warehouseMaterialDao.findAllBySearch(searchData.getMrnb(), null,
					null,
					pageableW);
			AtomicLong keyId = new AtomicLong(100000000L);
			// 區分 是否只查物料號?
			if (searchData.getMrnb() != null && searchData.getMrnote() == null && searchData.getMrsubnote() == null) {
				// 替代
				entitys.forEach(e -> {
					entitysAll.put(e.getMrnb(), e);
				});
				// 物料
				materials.forEach(m -> {
					MaterialReplacement entityOne = new MaterialReplacement();
					entityOne.setMrnb(m.getWmpnb());
					entityOne.setMrname(m.getWmname());
					entityOne.setMrspecification(m.getWmspecification());
					entityOne.setMrid(keyId.getAndIncrement());
					materialsAll.add(entityOne);
				});
				// 替代料
				materialsAll.forEach(m -> {
					// 有匹配到替代料?
					if (entitysAll.containsKey(m.getMrnb())) {
						MaterialReplacement entityOne = entitysAll.get(m.getMrnb());// 替代料表
						m.setMrnb(entityOne.getMrnb());
						// m.setMrname(entityOne.getMrname());//不要抓取舊的
						// m.setMrspecification(entityOne.getMrspecification());//不要抓取舊的
						m.setMrnote(entityOne.getMrnote());
						m.setMrsubnote(entityOne.getMrsubnote());
						m.setMrid(entityOne.getMrid());
						m.setSyscuser(entityOne.getSyscuser());
						m.setSyscdate(entityOne.getSyscdate());
						m.setSysmuser(entityOne.getSysmuser());
						m.setSysmdate(entityOne.getSysmdate());
						if (!m.getMrname().equals(entityOne.getMrname())
								|| !m.getMrspecification().equals(entityOne.getMrspecification())) {
							entityUpdates.add(m);
						}
					}
				});
				materialReplacementDao.saveAll(entityUpdates);
				entitys = materialsAll;
			} else {
				// 有其他查詢?

			}

			// Step4-2.資料區分(一般/細節)

			// 類別(一般模式)
			String entityJson = packageService.beanToJson(entitys);
			// 資料包裝
			packageBean.setEntityJson(entityJson);
			packageBean.setEntityDetailJson("");
			// 查不到資料
			if (packageBean.getEntityJson().equals("[]")) {
				throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1000, Lan.zh_TW, null);
			}

		}
		// ========================配置共用參數========================
		// Step5. 取得資料格式/(主KEY/群組KEY)
		// 資料格式
		String entityFormatJson = packageService.beanToJson(new MaterialReplacement());
		packageBean.setEntityFormatJson(entityFormatJson);
		// KEY名稱Ikey_Gkey
		packageBean.setEntityIKeyGKey("mrid_");
		packageBean.setEntityDateTime(packageBean.getEntityDateTime());
		return packageBean;
	}

	/** 修改資料 */
	@Transactional
	public PackageBean setModify(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<MaterialReplacement> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<MaterialReplacement>>() {
					});

			// Step2.資料檢查
			for (MaterialReplacement entityData : entityDatas) {
				// 檢查-名稱重複(有資料 && 不是同一筆資料)
				ArrayList<MaterialReplacement> checkDatas = materialReplacementDao.findAllByCheck(entityData.getMrnb(),
						null, null);
				for (MaterialReplacement checkData : checkDatas) {
					if (checkData.getMrid().compareTo(entityData.getMrid()) != 0) {
						throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
								new String[] { entityData.getMrnb() });
					}
				}
			}
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<MaterialReplacement> saveDatas = new ArrayList<>();
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getMrid() != null) {
				MaterialReplacement entityDataOld = materialReplacementDao.getReferenceById(x.getMrid());
				entityDataOld.setSysmdate(new Date());
				entityDataOld.setSysmuser(packageBean.getUserAccount());
				entityDataOld.setMrnb(x.getMrnb());
				entityDataOld.setMrnote(x.getMrnote());
				entityDataOld.setMrsubnote(x.getMrsubnote());
				//
				entityDataOld.setMrname(x.getMrname());
				entityDataOld.setMrspecification(x.getMrspecification());

				saveDatas.add(entityDataOld);

			}
		});
		// =======================資料儲存=======================
		// 資料Data
		materialReplacementDao.saveAll(saveDatas);
		return packageBean;
	}

	/** 新增資料 */
	// @Transactional
	public PackageBean setAdd(PackageBean packageBean) throws Exception {
		// =======================資料準備=======================
		ArrayList<MaterialReplacement> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<MaterialReplacement>>() {
					});

			// Step2.資料檢查
			for (MaterialReplacement entityData : entityDatas) {
				// 檢查-名稱重複(有資料 && 不是同一筆資料)
				ArrayList<MaterialReplacement> checkDatas = materialReplacementDao.findAllByCheck(entityData.getMrnb(),
						null, null);

				if (checkDatas.size() > 0) {
					throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, Lan.zh_TW,
							new String[] { entityData.getMrnb()
									+ ". This ID already exists. Please use the 'Modify' function to update records. " });
				}

			}
		}

		// =======================資料整理=======================
		// 資料Data
		ArrayList<MaterialReplacement> saveDatas = new ArrayList<>();
		entityDatas.forEach(x -> {
			// 新增
			x.setMrid(null);
			x.setSysmdate(new Date());
			x.setSysmuser(packageBean.getUserAccount());
			x.setSysodate(new Date());
			x.setSysouser(packageBean.getUserAccount());
			x.setSyscdate(new Date());
			x.setSyscuser(packageBean.getUserAccount());
			x.setSysheader(false);
			saveDatas.add(x);
		});
		// =======================資料儲存=======================
		// 資料Detail
		materialReplacementDao.saveAll(saveDatas);
		return packageBean;
	}

	/** 作廢資料 */
	@Transactional
	public PackageBean setInvalid(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<MaterialReplacement> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<MaterialReplacement>>() {
					});
			// Step2.資料檢查
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<MaterialReplacement> saveDatas = new ArrayList<>();
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getMrid() != null) {
				MaterialReplacement entityDataOld = materialReplacementDao.findById(x.getMrid()).get();
				entityDataOld.setSysmdate(new Date());
				entityDataOld.setSysmuser(packageBean.getUserAccount());
				entityDataOld.setSysstatus(2);
				saveDatas.add(entityDataOld);
			}
		});
		// =======================資料儲存=======================
		// 資料Data
		materialReplacementDao.saveAll(saveDatas);
		return packageBean;
	}

	/** 移除資料 */
	@Transactional
	public PackageBean setDetele(PackageBean packageBean) throws Exception {
		// =======================資料準備 =======================
		ArrayList<MaterialReplacement> entityDatas = new ArrayList<>();
		// =======================資料檢查=======================
		if (packageBean.getEntityJson() != null && !packageBean.getEntityJson().equals("")) {
			// Step1.資料轉譯(一般)
			entityDatas = packageService.jsonToBean(packageBean.getEntityJson(),
					new TypeReference<ArrayList<MaterialReplacement>>() {
					});
			// Step2.資料檢查
		}
		// =======================資料整理=======================
		// Step3.一般資料->寫入
		ArrayList<MaterialReplacement> saveDatas = new ArrayList<>();
		// 一般-移除內容
		entityDatas.forEach(x -> {
			// 排除 沒有ID
			if (x.getMrid() != null) {
				MaterialReplacement entityDataOld = materialReplacementDao.getReferenceById(x.getMrid());
				saveDatas.add(entityDataOld);
			}
		});

		// =======================資料儲存=======================
		// 資料Data
		materialReplacementDao.deleteAll(saveDatas);
		return packageBean;
	}

	/** 取得資料 */
	// @Transactional
	@SuppressWarnings("unchecked")
	public PackageBean getReport(PackageBean packageBean) throws Exception {
		String entityReport = packageBean.getEntityReportJson();
		JsonArray reportAry = packageService.StringToAJson(entityReport);
		List<MaterialReplacement> entitys = new ArrayList<>();
		Map<String, String> sqlQuery = new HashMap<>();
		// =======================查詢語法=======================
		// 拼湊SQL語法
		String nativeQuery = "SELECT e.* FROM material_replacement e Where ";
		for (JsonElement x : reportAry) {
			// entity 需要轉換SQL與句 && 欄位
			String cellName = x.getAsString().split("<_>")[0];
			cellName = cellName.replace("sys", "sys_");
			cellName = cellName.replace("sys_m", "sys_m_");
			cellName = cellName.replace("sys_c", "sys_c_");
			cellName = cellName.replace("sys_o", "sys_o_");
			cellName = cellName.replace("mr", "mr_");
			cellName = cellName.replace("mr_subnote", "mr_sub_note");
			cellName = cellName.replace("mr_nnnote", "mr_nn_note");
			cellName = cellName.replace("mr_clnote", "mr_cl_note");
			cellName = cellName.replace("mr_pnote", "mr_p_note");

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
		nativeQuery += " order by e.mr_nb asc";
		nativeQuery += " LIMIT 10000 OFFSET 0 ";
		Query query = em.createNativeQuery(nativeQuery, MaterialReplacement.class);
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