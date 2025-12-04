package dtri.com.tw.service;

import java.lang.reflect.Field;
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
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dtri.com.tw.pgsql.dao.BasicIncomingListDao;
import dtri.com.tw.pgsql.dao.BasicShippingListDao;
import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.dao.WarehouseAreaDao;
import dtri.com.tw.pgsql.dao.WarehouseTypeFilterDao;
import dtri.com.tw.pgsql.entity.BasicIncomingList;
import dtri.com.tw.pgsql.entity.BasicShippingList;
import dtri.com.tw.pgsql.entity.SystemLanguageCell;
import dtri.com.tw.pgsql.entity.WarehouseArea;
import dtri.com.tw.pgsql.entity.WarehouseSynchronizeDetailFront;
import dtri.com.tw.pgsql.entity.WarehouseSynchronizeFront;
import dtri.com.tw.pgsql.entity.WarehouseTypeFilter;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.CloudExceptionService.ErCode;
import dtri.com.tw.shared.CloudExceptionService.ErColor;
import dtri.com.tw.shared.CloudExceptionService.Lan;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;

@Service
public class WarehouseSynchronizeServiceAc {

	@Autowired
	private PackageService packageService;

	@Autowired
	private SystemLanguageCellDao languageDao;

	@Autowired
	private WarehouseAreaDao areaDao;

	@Autowired
	private BasicIncomingListDao incomingListDao;

	@Autowired
	private BasicShippingListDao shippingListDao;

	@Autowired
	private WarehouseTypeFilterDao filterDao;

	/** 取得資料 */
	public PackageBean getSearch(PackageBean packageBean) throws Exception {
		// ========================分頁設置========================
		// Step1.批次分頁
		// JsonObject pageSetJson =
		// JsonParser.parseString(packageBean.getSearchPageSet()).getAsJsonObject();
		int total = 50000;
		int batch = 0;

		// Step2.排序
		List<Order> inOrders = new ArrayList<>();
		inOrders.add(new Order(Direction.ASC, "bilclass"));// 單別
		inOrders.add(new Order(Direction.ASC, "bilsn"));// 單號
		inOrders.add(new Order(Direction.ASC, "bilnb"));// 流水號

		List<Order> shOrders = new ArrayList<>();
		shOrders.add(new Order(Direction.ASC, "bslclass"));// 單別
		shOrders.add(new Order(Direction.ASC, "bslsn"));// 單號
		shOrders.add(new Order(Direction.ASC, "bslnb"));// 流水號

		// 一般模式
		PageRequest inPageable = PageRequest.of(batch, total, Sort.by(inOrders));
		PageRequest shPageable = PageRequest.of(batch, total, Sort.by(shOrders));
		// Step3-1.取得資料(一般/細節)
		ArrayList<WarehouseSynchronizeFront> entitys = new ArrayList<WarehouseSynchronizeFront>();
		ArrayList<WarehouseSynchronizeFront> entityOks = new ArrayList<WarehouseSynchronizeFront>();
		Map<String, Boolean> entityMarks = new HashMap<String, Boolean>();
		ArrayList<WarehouseSynchronizeDetailFront> entityDetails = new ArrayList<WarehouseSynchronizeDetailFront>();
		//
		List<WarehouseArea> areaLists = areaDao.findAll();
		Map<String, WarehouseArea> areaMaps = new HashMap<>();
		//
		List<WarehouseTypeFilter> typeFilters = filterDao.findAll();
		Map<String, String> typeFilterMaps = new HashMap<>();
		// Step3-2.資料區分(一般/細節)
		areaLists.forEach(a -> {
			String key = a.getWaaliasawmpnb();// 倉儲+物料號
			areaMaps.put(key, a);
		});
		typeFilters.forEach(t -> {
			typeFilterMaps.put(t.getWtfcode(), t.getWtfname());
		});

		// ========================區分:訪問/查詢========================
		if (packageBean.getEntityJson() == "") {// 訪問
			// 單據清單
			ArrayList<BasicIncomingList> incomingLists = incomingListDao.findAllBySearchDetailSynchronize(null, null,
					null, null, inPageable);
			ArrayList<BasicShippingList> shippingLists = shippingListDao.findAllBySearchDetailSynchronize(null, null,
					null, null, shPageable);
			// 單據內容
			ArrayList<BasicIncomingList> incomingDetailLists = incomingListDao.findAllBySearchSynchronize(null, null,
					null, inPageable);
			ArrayList<BasicShippingList> shippingDetailLists = shippingListDao.findAllBySearchSynchronize(null, null,
					null, shPageable);

			// header (完成清單)
			Map<String, Integer> listTotail = new HashMap<>();
			Map<String, Integer> listFinish = new HashMap<>();
			incomingLists.forEach(in -> {
				String headerKey = in.getBilclass() + "-" + in.getBilsn();
				// String Key = in.getBilclass() + "-" + in.getBilsn() + "-" + in.getBilnb();
				// 登記進度
				if (!listTotail.containsKey(headerKey)) {
					listTotail.put(headerKey, 1);
					listFinish.put(headerKey, 0);
					WarehouseSynchronizeFront e = new WarehouseSynchronizeFront();
					e.setId(headerKey);
					e.setGid(headerKey);
					// 進料單
					e.setWslclassname(typeFilterMaps.get(in.getBilclass()));// 單據名稱
					e.setWslclasssn(headerKey);// 單據+單據號
					e.setWsltype(in.getBiltype());// : 單據類型(領料類/入料類)<br>

					// System
					e.setSyscdate(in.getSyscdate());
					e.setSyscuser(in.getSyscuser());
					e.setSysmdate(in.getSysmdate());
					e.setSysmuser(in.getSysmuser());
					e.setSysnote(in.getSysnote());
					e.setSysstatus(in.getSysstatus());
					// header
					entitys.add(e);
					// 如果有異常或是尚未滿足
					if (in.getBilfuser().contains("✪") || in.getBilfuser().equals("")
							|| !in.getBilpnqty().equals(in.getBilpngqty())) {
						if (!entityMarks.containsKey(headerKey)) {
							entityMarks.put(headerKey, true);
						}
					}
				} else {
					// 如果有異常或是尚未滿足
					if (in.getBilfuser().contains("✪") || in.getBilfuser().equals("")
							|| !in.getBilpnqty().equals(in.getBilpngqty())) {
						if (!entityMarks.containsKey(headerKey)) {
							entityMarks.put(headerKey, true);
						}
					}
					listTotail.put(headerKey, listTotail.get(headerKey) + 1);
				}
				// 登記完成項目?
				if (!in.getBilfuser().equals("") && listFinish.containsKey(headerKey)) {
					listFinish.put(headerKey, listFinish.get(headerKey) + 1);
				}
			});
			shippingLists.forEach(sh -> {
				String headerKey = sh.getBslclass() + "-" + sh.getBslsn();
				// String Key = sh.getBslclass() + "-" + sh.getBslsn() + "-" + sh.getBslnb();
				// 登記進度
				if (!listTotail.containsKey(headerKey)) {
					listTotail.put(headerKey, 1);
					listFinish.put(headerKey, 0);
					WarehouseSynchronizeFront e = new WarehouseSynchronizeFront();
					e.setId(headerKey);
					e.setGid(headerKey);
					// 領料單
					e.setWslclassname(typeFilterMaps.get(sh.getBslclass()));// 單據名稱
					e.setWslclasssn(headerKey);// 單據+單據號
					e.setWsltype(sh.getBsltype());// : 單據類型(領料類/入料類)<br>
					// System
					e.setSyscdate(sh.getSyscdate());
					e.setSyscuser(sh.getSyscuser());
					e.setSysmdate(sh.getSysmdate());
					e.setSysmuser(sh.getSysmuser());
					e.setSysnote(sh.getSysnote());
					e.setSysstatus(sh.getSysstatus());
					// header
					entitys.add(e);
					// 如果有異常或是尚未滿足
					if (sh.getBslfuser().contains("✪") || sh.getBslfuser().equals("")
							|| !sh.getBslpngqty().equals(sh.getBslpnerpqty())) {
						if (!entityMarks.containsKey(headerKey)) {
							entityMarks.put(headerKey, true);
						}
					}
				} else {
					// 如果有異常或是尚未滿足
					if (sh.getBslfuser().contains("✪") || sh.getBslfuser().equals("")
							|| !sh.getBslpngqty().equals(sh.getBslpnerpqty())) {
						if (!entityMarks.containsKey(headerKey)) {
							entityMarks.put(headerKey, true);
						}
					}
					listTotail.put(headerKey, listTotail.get(headerKey) + 1);
				}
				// 登記完成項目?
				if (!sh.getBslfuser().equals("") && listFinish.containsKey(headerKey)) {
					listFinish.put(headerKey, listFinish.get(headerKey) + 1);
				}
			});
			// 進度
			entitys.forEach(ent -> {
				ent.setWslschedule(listFinish.get(ent.getId()) + "/" + listTotail.get(ent.getId()));
				int lfh = listFinish.get(ent.getId());
				int ltl = listTotail.get(ent.getId());
				if (lfh == ltl) {
					entityOks.add(ent);
				}
				// 異常?
				if (entityMarks.containsKey(ent.getWslclasssn())) {
					ent.setWslclassname("✪ " + ent.getWslclassname());
				}
			});
			// 進料
			incomingDetailLists.forEach(in -> {
				String headerKey = in.getBilclass() + "-" + in.getBilsn();
				String Key = in.getBilclass() + "-" + in.getBilsn() + "-" + in.getBilnb();

				WarehouseSynchronizeDetailFront e = new WarehouseSynchronizeDetailFront();
				e.setId(Key);
				e.setGid(headerKey);
				// 進料單
				e.setWssclassname(typeFilterMaps.get(in.getBilclass()));// 單據名稱
				e.setWssclasssn(headerKey);// 單據+單據號
				e.setWssnb(in.getBilnb());// 序號
				e.setWsstype(in.getBiltype());// : 單據類型(領料類/入料類)<br>
				e.setWssfuser(in.getBilfuser());// 完成人
				e.setWsscuser(in.getBilcuser());// 核准人
				e.setWsspnumber(in.getBilpnumber());// : 物料號<br>
				e.setWsspnqty(in.getBilpnqty());// : 數量<br>
				e.setWsspngqty(in.getBilpngqty());// 已(取入)數量<br>
				e.setWsspnoqty(in.getBilpnoqty());// 超(取入)數量<br>
				e.setWsspnerpqty(in.getBilpnerpqty());// ERP(帳務)數量
				String wsstofromwho = in.getBiltowho().replaceAll("[\\[\\]]", "");
				if (wsstofromwho.split("_").length > 2) {
					e.setWsstofromwho(wsstofromwho.split("_")[1]);// (倉庫)EX:A0001_原物料倉
				}
				// 倉儲(必須符合格式)
				if (in.getBiltowho().split("_").length > 1) {
					String areaKey = in.getBiltowho().split("_")[0].replace("[", "") + "_" + in.getBilpnumber();
					if (areaMaps.containsKey(areaKey)) {
						e.setWsstqty(areaMaps.get(areaKey).getWatqty());// 實際數量
						e.setWsserptqty(areaMaps.get(areaKey).getWaerptqty());// 帳務數量
					}
				}
				// System
				e.setSyscdate(in.getSyscdate());
				e.setSyscuser(in.getSyscuser());
				e.setSysmdate(in.getSysmdate());
				e.setSysmuser(in.getSysmuser());
				e.setSysnote(in.getSysnote());
				e.setSysstatus(in.getSysstatus());
				// header
				entityDetails.add(e);
			});
			// 領料
			shippingDetailLists.forEach(sh -> {
				String headerKey = sh.getBslclass() + "-" + sh.getBslsn();
				String Key = sh.getBslclass() + "-" + sh.getBslsn() + "-" + sh.getBslnb();

				WarehouseSynchronizeDetailFront e = new WarehouseSynchronizeDetailFront();
				e.setId(Key);
				e.setGid(headerKey);
				// 領
				e.setWssclassname(typeFilterMaps.get(sh.getBslclass()));// 單據名稱
				e.setWssclasssn(headerKey);// 單據+單據號
				e.setWssnb(sh.getBslnb());// 序號
				e.setWsstype(sh.getBsltype());// : 單據類型(領料類/入料類)<br>
				e.setWssfuser(sh.getBslfuser());// 完成人
				e.setWsscuser(sh.getBslcuser());// 核准人
				e.setWsspnumber(sh.getBslpnumber());// : 物料號<br>
				e.setWsspnqty(sh.getBslpnqty());// : 數量<br>
				e.setWsspngqty(sh.getBslpngqty());// 已(取入)數量<br>
				e.setWsspnoqty(sh.getBslpnoqty());// 超(取入)數量<br>
				e.setWsspnerpqty(sh.getBslpnerpqty());// ERP(帳務)數量
				String wsstofromwho = sh.getBslfromwho().replaceAll("[\\[\\]]", "");
				if (wsstofromwho.split("_").length > 2) {
					e.setWsstofromwho(wsstofromwho.split("_")[1]);// (倉庫)EX:A0001_原物料倉
				}
				// 倉儲(必須符合格式)
				if (sh.getBslfromwho().split("_").length > 1) {
					String areaKey = sh.getBslfromwho().split("_")[0].replace("[", "") + "_" + sh.getBslpnumber();
					if (areaMaps.containsKey(areaKey)) {
						e.setWsstqty(areaMaps.get(areaKey).getWatqty());// 實際數量
						e.setWsserptqty(areaMaps.get(areaKey).getWaerptqty());// 帳務數量
					}
				}
				// System
				e.setSyscdate(sh.getSyscdate());
				e.setSyscuser(sh.getSyscuser());
				e.setSysmdate(sh.getSysmdate());
				e.setSysmuser(sh.getSysmuser());
				e.setSysnote(sh.getSysnote());
				e.setSysstatus(sh.getSysstatus());
				entityDetails.add(e);
			});

			// 類別(一般模式)
			// 資料包裝
			String entityJsonDatas = packageService.beanToJson(entityOks);
			String entityJsonDetails = packageService.beanToJson(entityDetails);
			packageBean.setEntityJson(entityJsonDatas);
			packageBean.setEntityDetailJson(entityJsonDetails);

			// ========================建立:查詢欄位/對應翻譯/修改選項========================
			// Step3-3. 取得翻譯(一般/細節)
			Map<String, SystemLanguageCell> mapLanguages = new HashMap<>();
			Map<String, SystemLanguageCell> mapLanguagesDetail = new HashMap<>();
			// 一般翻譯
			ArrayList<SystemLanguageCell> languages = languageDao.findAllByLanguageCellSame("WarehouseSynchronizeFront",
					null, 2);
			languages.forEach(x -> {
				mapLanguages.put(x.getSltarget(), x);
			});
			// 細節翻譯
			ArrayList<SystemLanguageCell> languagesDetail = languageDao
					.findAllByLanguageCellSame("WarehouseSynchronizeDetailFront", null, 2);
			languagesDetail.forEach(y -> {
				System.out.println(y.getSltarget());
				mapLanguagesDetail.put(y.getSltarget(), y);
			});
			// 動態->覆蓋寫入->修改UI選項

			// Step3-4. 欄位設置
			JsonObject searchSetJsonAll = new JsonObject();
			JsonArray searchJsons = new JsonArray();// 查詢設定
			JsonObject resultDataTJsons = new JsonObject();// 回傳欄位-一般名稱
			JsonObject resultDetailTJsons = new JsonObject();// 回傳欄位-細節名稱
			// 結果欄位(名稱Entity變數定義)=>取出=>排除/寬度/語言/順序
			Field[] fields = WarehouseSynchronizeFront.class.getDeclaredFields();
			Field[] fieldDetails = WarehouseSynchronizeDetailFront.class.getDeclaredFields();
			// 排除欄位
			ArrayList<String> exceptionCell = new ArrayList<>();
			exceptionCell.add("material");

			// 欄位翻譯(一般)
			resultDataTJsons = packageService.resultSet(fields, exceptionCell, mapLanguages);
			resultDetailTJsons = packageService.resultSet(fieldDetails, exceptionCell, mapLanguagesDetail);

			// Step3-5. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "wslclasssn", "Ex:單別-單號?", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目
			JsonArray selectArr = new JsonArray();
			selectArr.add("領料類_領料類");
			selectArr.add("入料類_入料類");
			searchJsons = packageService.searchSet(searchJsons, selectArr, "wsltype", "Ex:單據類型?", true, //
					PackageService.SearchType.select, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目
			JsonArray selectStatusArr = new JsonArray();
			selectStatusArr.add("未結單_0");
			selectStatusArr.add("已結單_1");
			searchJsons = packageService.searchSet(searchJsons, selectStatusArr, "sysstatus", "Ex:狀態?", true, //
					PackageService.SearchType.select, PackageService.SearchWidth.col_lg_2);
			// Step3-5. 建立查詢項目
			JsonArray selectWssfuserArr = new JsonArray();
			selectWssfuserArr.add("未撿完單_0");
			selectWssfuserArr.add("已撿完單_1");
			searchJsons = packageService.searchSet(searchJsons, selectWssfuserArr, "wslfuser", "Ex:完成人?", true, //
					PackageService.SearchType.select, PackageService.SearchWidth.col_lg_2);

			// 查詢包裝/欄位名稱(一般/細節)
			searchSetJsonAll.add("searchSet", searchJsons);
			searchSetJsonAll.add("resultThead", resultDataTJsons);
			searchSetJsonAll.add("resultDetailThead", resultDetailTJsons);
			packageBean.setSearchSet(searchSetJsonAll.toString());
		} else {
			// Step4-1. 取得資料(一般/細節)
			WarehouseSynchronizeFront searchData = packageService.jsonToBean(packageBean.getEntityJson(),
					WarehouseSynchronizeFront.class);

			String wasclass = null;
			String wassn = null;
			if (searchData.getWslclasssn() != null && searchData.getWslclasssn().split("-").length == 2) {
				wasclass = searchData.getWslclasssn().split("-")[0];
				wassn = searchData.getWslclasssn().split("-")[1];
			} else {
				wasclass = searchData.getWslclasssn();
			}
			// 一般
			ArrayList<BasicIncomingList> incomingLists = incomingListDao.findAllBySearchDetailSynchronize(wasclass,
					wassn, searchData.getWsltype(), searchData.getSysstatus(), inPageable);
			ArrayList<BasicShippingList> shippingLists = shippingListDao.findAllBySearchDetailSynchronize(wasclass,
					wassn, searchData.getWsltype(), searchData.getSysstatus(), shPageable);
			// 細節(清單)
			ArrayList<BasicIncomingList> incomingDetailLists = incomingListDao.findAllBySearchSynchronize(wasclass,
					wassn, searchData.getWsltype(), inPageable);
			ArrayList<BasicShippingList> shippingDetailLists = shippingListDao.findAllBySearchSynchronize(wasclass,
					wassn, searchData.getWsltype(), shPageable);

			// 細節(完成清單)
			Map<String, Integer> listTotail = new HashMap<>();
			Map<String, Integer> listFinish = new HashMap<>();

			// Step4-2.資料區分(一般/細節)
			// 一般
			incomingLists.forEach(in -> {
				String headerKey = in.getBilclass() + "-" + in.getBilsn();
				// String Key = in.getBilclass() + "-" + in.getBilsn() + "-" + in.getBilnb();
				// 登記進度
				if (!listTotail.containsKey(headerKey)) {
					listTotail.put(headerKey, 1);
					listFinish.put(headerKey, 0);
					WarehouseSynchronizeFront e = new WarehouseSynchronizeFront();
					e.setId(headerKey);
					e.setGid(headerKey);
					// 進料單
					e.setWslclassname(typeFilterMaps.get(in.getBilclass()));// 單據名稱
					e.setWslclasssn(headerKey);// 單據+單據號
					e.setWsltype(in.getBiltype());// : 單據類型(領料類/入料類)<br>

					// System
					e.setSyscdate(in.getSyscdate());
					e.setSyscuser(in.getSyscuser());
					e.setSysmdate(in.getSysmdate());
					e.setSysmuser(in.getSysmuser());
					e.setSysnote(in.getSysnote());
					e.setSysstatus(in.getSysstatus());
					// header
					entitys.add(e);
					// 如果有異常或是尚未滿足
					if (in.getBilfuser().contains("✪") || in.getBilfuser().equals("")
							|| !in.getBilpnqty().equals(in.getBilpngqty())) {
						if (!entityMarks.containsKey(headerKey)) {
							entityMarks.put(headerKey, true);
						}
					}
				} else {
					// 如果有異常或是尚未滿足
					if (in.getBilfuser().contains("✪") || in.getBilfuser().equals("")
							|| !in.getBilpnqty().equals(in.getBilpngqty())) {
						if (!entityMarks.containsKey(headerKey)) {
							entityMarks.put(headerKey, true);
						}
					}

					listTotail.put(headerKey, listTotail.get(headerKey) + 1);
				}
				// 登記完成項目?
				if (!in.getBilfuser().equals("") && listFinish.containsKey(headerKey)) {
					listFinish.put(headerKey, listFinish.get(headerKey) + 1);
				}
			});
			shippingLists.forEach(sh -> {
				String headerKey = sh.getBslclass() + "-" + sh.getBslsn();
				// String Key = sh.getBslclass() + "-" + sh.getBslsn() + "-" + sh.getBslnb();
				// 登記進度
				if (!listTotail.containsKey(headerKey)) {
					listTotail.put(headerKey, 1);
					listFinish.put(headerKey, 0);
					WarehouseSynchronizeFront e = new WarehouseSynchronizeFront();
					e.setId(headerKey);
					e.setGid(headerKey);
					// 領料單
					e.setWslclassname(typeFilterMaps.get(sh.getBslclass()));// 單據名稱
					e.setWslclasssn(headerKey);// 單據+單據號
					e.setWsltype(sh.getBsltype());// : 單據類型(領料類/入料類)<br>
					// System
					e.setSyscdate(sh.getSyscdate());
					e.setSyscuser(sh.getSyscuser());
					e.setSysmdate(sh.getSysmdate());
					e.setSysmuser(sh.getSysmuser());
					e.setSysnote(sh.getSysnote());
					e.setSysstatus(sh.getSysstatus());
					// header
					entitys.add(e);
					// 如果有異常或是尚未滿足(pnerpqty / pngqty )
					if (sh.getBslfuser().contains("✪") || sh.getBslfuser().equals("")
							|| !sh.getBslpngqty().equals(sh.getBslpnerpqty())) {
						System.out.println(
								sh.getBslfuser().contains("✪") + ":" + !sh.getBslpngqty().equals(sh.getBslpnerpqty()));
						if (!entityMarks.containsKey(headerKey)) {
							entityMarks.put(headerKey, true);
						}
					}
				} else {
					// 如果有異常或是尚未滿足(pnerpqty / pngqty )
					if (sh.getBslfuser().contains("✪") || sh.getBslfuser().equals("")
							|| !sh.getBslpngqty().equals(sh.getBslpnerpqty())) {
						System.out.println(
								sh.getBslfuser().contains("✪") + ":" + !sh.getBslpngqty().equals(sh.getBslpnerpqty()));
						if (!entityMarks.containsKey(headerKey)) {
							entityMarks.put(headerKey, true);
						}
					}
					listTotail.put(headerKey, listTotail.get(headerKey) + 1);
				}
				// 登記完成項目?
				if (!sh.getBslfuser().equals("") && listFinish.containsKey(headerKey)) {
					listFinish.put(headerKey, listFinish.get(headerKey) + 1);
				}
			});
			// 進度
			entitys.forEach(ent -> {
				ent.setWslschedule(listFinish.get(ent.getId()) + "/" + listTotail.get(ent.getId()));
			});

			// 進度(必須看是否完成?)
			entitys.forEach(ent -> {
				ent.setWslschedule(listFinish.get(ent.getId()) + "/" + listTotail.get(ent.getId()));
				int lfh = listFinish.get(ent.getId());
				int ltl = listTotail.get(ent.getId());
				if (searchData.getWslfuser() == null || // 都顯示
						(lfh == ltl && searchData.getWslfuser().equals("1")) || // 已撿完
						(lfh != ltl && searchData.getWslfuser().equals("0"))) {// 沒撿完
					entityOks.add(ent);
				}
				// 異常?
				if (entityMarks.containsKey(ent.getWslclasssn())) {
					ent.setWslclassname("✪ " + ent.getWslclassname());
				}
			});
			// 細節
			// 進料
			incomingDetailLists.forEach(in -> {
				String headerKey = in.getBilclass() + "-" + in.getBilsn();
				String Key = in.getBilclass() + "-" + in.getBilsn() + "-" + in.getBilnb();

				WarehouseSynchronizeDetailFront e = new WarehouseSynchronizeDetailFront();
				e.setId(Key);
				e.setGid(headerKey);
				// 進料單
				e.setWssclassname(typeFilterMaps.get(in.getBilclass()));// 單據名稱
				e.setWssclasssn(headerKey);// 單據+單據號
				e.setWssnb(in.getBilnb());// 序號
				e.setWsstype(in.getBiltype());// : 單據類型(領料類/入料類)<br>
				e.setWssfuser(in.getBilfuser());// 完成人
				e.setWsscuser(in.getBilcuser());// 核准人
				e.setWsspnumber(in.getBilpnumber());// : 物料號<br>
				e.setWsspnqty(in.getBilpnqty());// : 數量<br>
				e.setWsspngqty(in.getBilpngqty());// 已(取入)數量<br>
				e.setWsspnoqty(in.getBilpnoqty());// 超(取入)數量<br>
				e.setWsspnerpqty(in.getBilpnerpqty());// ERP(帳務)數量
				String wsstofromwho = in.getBiltowho().replaceAll("[\\[\\]]", "");
				if (wsstofromwho.split("_").length > 2) {
					e.setWsstofromwho(wsstofromwho.split("_")[1]);// (倉庫)EX:A0001_原物料倉
				}
				// 倉儲(必須符合格式)
				if (in.getBiltowho().split("_").length > 1) {
					String areaKey = in.getBiltowho().split("_")[0].replace("[", "") + "_" + in.getBilpnumber();
					if (areaMaps.containsKey(areaKey)) {
						e.setWsstqty(areaMaps.get(areaKey).getWatqty());// 實際數量
						e.setWsserptqty(areaMaps.get(areaKey).getWaerptqty());// 帳務數量
					}
				}
				// System
				e.setSyscdate(in.getSyscdate());
				e.setSyscuser(in.getSyscuser());
				e.setSysmdate(in.getSysmdate());
				e.setSysmuser(in.getSysmuser());
				e.setSysnote(in.getSysnote());
				e.setSysstatus(in.getSysstatus());
				// header
				entityDetails.add(e);

			});
			// 領料
			shippingDetailLists.forEach(sh -> {
				String headerKey = sh.getBslclass() + "-" + sh.getBslsn();
				String Key = sh.getBslclass() + "-" + sh.getBslsn() + "-" + sh.getBslnb();

				WarehouseSynchronizeDetailFront e = new WarehouseSynchronizeDetailFront();
				e.setId(Key);
				e.setGid(headerKey);
				// 進料單
				e.setWssclassname(typeFilterMaps.get(sh.getBslclass()));// 單據名稱
				e.setWssclasssn(headerKey);// 單據+單據號
				e.setWssnb(sh.getBslnb());// 序號
				e.setWsstype(sh.getBsltype());// : 單據類型(領料類/入料類)<br>
				e.setWssfuser(sh.getBslfuser());// 完成人
				e.setWsscuser(sh.getBslcuser());// 核准人
				e.setWsspnumber(sh.getBslpnumber());// : 物料號<br>
				e.setWsspnqty(sh.getBslpnqty());// : 數量<br>
				e.setWsspngqty(sh.getBslpngqty());// 已(取入)數量<br>
				e.setWsspnoqty(sh.getBslpnoqty());// 超(取入)數量<br>
				e.setWsspnerpqty(sh.getBslpnerpqty());// ERP(帳務)數量
				String wsstofromwho = sh.getBslfromwho().replaceAll("[\\[\\]]", "");
				if (wsstofromwho.split("_").length > 2) {
					e.setWsstofromwho(wsstofromwho.split("_")[1]);// (倉庫)EX:A0001_原物料倉
				}
				// 倉儲(必須符合格式)
				if (sh.getBslfromwho().split("_").length > 1) {
					String areaKey = sh.getBslfromwho().split("_")[0].replace("[", "") + "_" + sh.getBslpnumber();
					if (areaMaps.containsKey(areaKey)) {
						e.setWsstqty(areaMaps.get(areaKey).getWatqty());// 實際數量
						e.setWsserptqty(areaMaps.get(areaKey).getWaerptqty());// 帳務數量
					}
				}
				// System
				e.setSyscdate(sh.getSyscdate());
				e.setSyscuser(sh.getSyscuser());
				e.setSysmdate(sh.getSysmdate());
				e.setSysmuser(sh.getSysmuser());
				e.setSysnote(sh.getSysnote());
				e.setSysstatus(sh.getSysstatus());
				// header
				entityDetails.add(e);
			});

			// 類別(一般模式)
			// 資料包裝
			String entityJsonDatas = packageService.beanToJson(entityOks);
			String entityJsonDetails = packageService.beanToJson(entityDetails);
			packageBean.setEntityJson(entityJsonDatas);
			packageBean.setEntityDetailJson(entityJsonDetails);

			// 查不到資料
			if (packageBean.getEntityJson().equals("[]") && packageBean.getEntityDetailJson().equals("[]")) {
				throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1000, Lan.zh_TW, null);
			}
		}
		// ========================配置共用參數========================
		// Step5. 取得資料格式/(主KEY/群組KEY)
		// 資料格式
		String entityFormatJson = packageService.beanToJson(new WarehouseSynchronizeFront());
		packageBean.setEntityFormatJson(entityFormatJson);
		// KEY名稱Ikey_Gkey
		packageBean.setEntityIKeyGKey("id_gid");
		packageBean.setEntityDateTime(packageBean.getEntityDateTime() + "_wasedate");
		return packageBean;
	}

	/** 修改資料 */
	@Transactional
	public PackageBean setModify(PackageBean packageBean, String action) throws Exception {
		// =======================資料準備 =======================
		List<WarehouseArea> areas = areaDao.findAll();

		// =======================資料檢查=======================

		// =======================資料整理=======================
		// Step3.一般資料->寫入
		if (action.equals("Item")) {
			String finishItem = packageBean.getEntityDetailJson();
			JsonObject object = packageService.StringToJson(finishItem);
			Date updateDate = new Date();
			object.keySet().forEach((k) -> {
				k = k.split("_")[0];
				System.out.println(k);
				// 入料
				ArrayList<BasicIncomingList> ins = incomingListDao.findAllByCheck(k.split("-")[0], k.split("-")[1],
						k.split("-")[2]);
				if (ins.size() > 0) {
					ins.forEach((update_in) -> {
						update_in.setBilsuser(packageBean.getUserAccount());
						update_in.setSysmuser(packageBean.getUserAccount());
						update_in.setSysmdate(updateDate);
						if (update_in.getBilfuser().equals("")) {
							update_in.setBilfuser(packageBean.getUserAccount());
						}
					});
					incomingListDao.saveAll(ins);
				}
				// 領料
				ArrayList<BasicShippingList> shs = shippingListDao.findAllByCheck(k.split("-")[0], k.split("-")[1],
						k.split("-")[2]);
				if (shs.size() > 0) {
					shs.forEach((update_sh) -> {
						update_sh.setBslsuser(packageBean.getUserAccount());
						update_sh.setSysmuser(packageBean.getUserAccount());
						update_sh.setSysmdate(updateDate);
						if (update_sh.getBslfuser().equals("")) {
							update_sh.setBslfuser(packageBean.getUserAccount());
						}
					});
					shippingListDao.saveAll(shs);
				}
			});
		}
		if (action.equals("Qty")) {
			// 領料單
			// B-1.列出 -倉儲負責人 "已領料" 領料單類 -領料單類的 "未核單" 狀態
			ArrayList<BasicShippingList> basicShippingLists = shippingListDao.findAllByCheckBslpngqty();
			Map<String, Integer> basicShMap = new HashMap<String, Integer>();// 儲位 + 物料
			basicShippingLists.forEach(s -> {
				if (s.getBslfromwho() != null && !s.getBslfromwho().equals("")
						&& s.getBslfromwho().split("_").length == 3) {
					// 如果沒有?
					String bslfromwho[] = s.getBslfromwho().replace("[", "").replace("]", "").split("_");
					String key = bslfromwho[0] + "_" + s.getBslpnumber();// 倉別+物料
					String wo = s.getBslclass() + "-" + s.getBslsn();
					// 測試用
					if (key.equals("A0002_81-207-301065")) {
						System.out.println(wo +":"+ s.getBslpngqty());
					}
					if (basicShMap.containsKey(key)) {
						// 有?
						Integer oneQty = basicShMap.get(key) + s.getBslpngqty();
						basicShMap.put(key, oneQty);

					} else {
						// 無?
						basicShMap.put(key, s.getBslpngqty());
					}
				}
			});

			// 入料單-進行(匹配)->將數量修正 寫入
			// B-2.列出->(倉儲負責人 "未歸位" 入料單類) & (入料單類的 "已核單" 狀態)
			ArrayList<BasicIncomingList> reAll = incomingListDao.findAllBySearchAction(null, null, null, null, "",
					null);
			reAll.forEach(bil -> {
				bil.setBilpngqty(bil.getBilpnqty());
			});
			incomingListDao.saveAll(reAll);

			// 將ERP 數量導入 Cloud-
			// B-3.同步-倉儲&單據 再次修正 所有物料庫存清單
			areas.forEach(x -> {
				if (basicShMap.containsKey(x.getWaaliasawmpnb())) {
					// 如果有對上 則要排除
					if (x.getWaerptqty() - basicShMap.get(x.getWaaliasawmpnb()) <= 0) {
						// 避免負數
						x.setWatqty(0);
					} else {
						x.setWatqty(x.getWaerptqty() - basicShMap.get(x.getWaaliasawmpnb()));
					}
				} else {
					x.setWatqty(x.getWaerptqty());
				}
				x.setSysmuser(packageBean.getUserAccount());
				x.setSysmdate(new Date());
			});
			areaDao.saveAll(areas);

		}
		if (action.equals("Remove")) {
			// 完成確認
			String finishObj = packageBean.getEntityJson();
			JsonObject object = packageService.StringToJson(finishObj);
			object.keySet().forEach((k) -> {
				k = k.split("_")[0];
				System.out.println(k);
				// 入料
				ArrayList<BasicIncomingList> ins = incomingListDao.findAllByCheck(k.split("-")[0], k.split("-")[1],
						null);
				if (ins.size() > 0) {
					// incomingListDao.deleteAll(ins);
				}
				// 領料
				ArrayList<BasicShippingList> shs = shippingListDao.findAllByCheck(k.split("-")[0], k.split("-")[1],
						null);
				if (shs.size() > 0) {
					// shippingListDao.deleteAll(shs);
				}
			});
		}
		if (action.equals("Finish")) {
			// 完成確認
			String finishObj = packageBean.getEntityJson();
			JsonObject object = packageService.StringToJson(finishObj);
			Date updateDate = new Date();
			object.keySet().forEach((k) -> {
				k = k.split("_")[0];
				System.out.println(k);
				// 入料
				ArrayList<BasicIncomingList> ins = incomingListDao.findAllByCheck(k.split("-")[0], k.split("-")[1],
						null);
				if (ins.size() > 0) {
					ins.forEach((update_in) -> {
						update_in.setBilsuser(packageBean.getUserAccount());
						update_in.setSysmuser(packageBean.getUserAccount());
						update_in.setSysmdate(updateDate);
						if (update_in.getBilfuser().equals("")) {
							update_in.setBilfuser(packageBean.getUserAccount());
						}
					});
					incomingListDao.saveAll(ins);
				}
				// 領料
				ArrayList<BasicShippingList> shs = shippingListDao.findAllByCheck(k.split("-")[0], k.split("-")[1],
						null);
				if (shs.size() > 0) {
					shs.forEach((update_sh) -> {
						update_sh.setBslsuser(packageBean.getUserAccount());
						update_sh.setSysmuser(packageBean.getUserAccount());
						update_sh.setSysmdate(updateDate);
						if (update_sh.getBslfuser().equals("")) {
							update_sh.setBslfuser(packageBean.getUserAccount());
						}
					});
					shippingListDao.saveAll(shs);
				}
			});
		}
		return packageBean;
	}
}
