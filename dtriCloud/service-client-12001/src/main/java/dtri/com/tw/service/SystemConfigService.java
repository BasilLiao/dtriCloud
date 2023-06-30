package dtri.com.tw.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dtri.com.tw.bean.PackageBean;
import dtri.com.tw.db.entity.SystemConfig;
import dtri.com.tw.db.entity.SystemLanguageCell;
import dtri.com.tw.db.entity.SystemUser;
import dtri.com.tw.pgsql.dao.SystemConfigDao;
import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;

@Service
public class SystemConfigService {

	@Autowired
	private PackageService packageService;

	@Autowired
	private SystemConfigDao configDao;

	@Autowired
	private SystemLanguageCellDao languageDao;

	/** 取得資料 */
	public PackageBean getSearch(PackageBean packageBean, SystemUser systemUser) throws Exception {
		// 區分:訪問/查詢
		if (packageBean.getEntityJson() == "") {
			// 訪問

			// 批次分頁
			JsonObject pageSetJson = JsonParser.parseString(packageBean.getSearchPageSet()).getAsJsonObject();
			int total = pageSetJson.get("total").getAsInt();
			int batch = pageSetJson.get("batch").getAsInt();
			// 排序
			List<Order> orders = new ArrayList<>();
			orders.add(new Order(Direction.DESC, "scgid"));
			orders.add(new Order(Direction.ASC, "scname"));
			PageRequest pageable = PageRequest.of(batch, total, Sort.by(orders));

			// 取得資料(一般/細節)
			ArrayList<SystemConfig> systemConfigs = configDao.findAllByConfig(null, null, 0, pageable);
			String configJson = packageService.beanToJson(systemConfigs);
			packageBean.setEntityJson(configJson);
			packageBean.setEntityDetailJson("");

			// 取得翻譯(一般/細節)
			ArrayList<SystemLanguageCell> languages = languageDao.findAllBySystemUser("system_config", null);
			Map<String, SystemLanguageCell> mapLanguages = new HashMap<>();
			languages.forEach(x -> {
				mapLanguages.put(x.getSltarget(), x);
			});

			ArrayList<SystemLanguageCell> languages_detail = languageDao.findAllBySystemUser("system_config_detail", null);
			Map<String, SystemLanguageCell> mapLanguages_detail = new HashMap<>();
			languages_detail.forEach(x -> {
				mapLanguages_detail.put(x.getSltarget(), x);
			});

			// 查詢準備
			JsonObject searchSetJsonAll = new JsonObject();
			JsonArray searchJsons = new JsonArray();// 查詢設定
			JsonObject resulTheadJsons = new JsonObject();// 一般回傳欄位-名稱
			JsonObject resultDetailTheadJsons = new JsonObject();// 一般回傳欄位-細節名稱

			// 結果欄位(名稱Entity變數定義)=>取出=>排除/寬度/語言/順序
			Field[] fields = SystemConfig.class.getDeclaredFields();
			resulTheadJsons = packageService.resultSet(resulTheadJsons, fields, mapLanguages);

			// 查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "scname", //
					PackageService.SearchType.text, PackageService.SearchWidth.col_2);
			// 查詢項目-時間開始
			searchJsons = packageService.searchSet(searchJsons, null, "sysmdatestart", //
					PackageService.SearchType.datetime, PackageService.SearchWidth.col_2);
			// 查詢項目-時間結束
			searchJsons = packageService.searchSet(searchJsons, null, "sysmdateend", //
					PackageService.SearchType.datetime, PackageService.SearchWidth.col_2);
			// 主KEY AND 群組KEY 名稱
			packageBean.setEntityIKeyGKey("scid_scgid");
			// 查詢包裝
			searchSetJsonAll.add("searchSet", searchJsons);
			searchSetJsonAll.add("resultThead", resulTheadJsons);
			searchSetJsonAll.add("resultDetailThead", resultDetailTheadJsons);
			packageBean.setSearchSet(searchSetJsonAll.toString());
		} else {
			// 查詢

		}

		return packageBean;
	}

}
