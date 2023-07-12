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
		if (packageBean.getEntityJson() == "") {// 訪問
			// ==============取得資料&&格式&&翻譯==============
			// Step1.查詢格式
			String configFormatJson = packageService.beanToJson(new SystemConfig());
			packageBean.setEntityFormatJson(configFormatJson);
			// Step2.批次分頁
			JsonObject pageSetJson = JsonParser.parseString(packageBean.getSearchPageSet()).getAsJsonObject();
			int total = pageSetJson.get("total").getAsInt();
			int batch = pageSetJson.get("batch").getAsInt();
			// Step3.排序
			List<Order> orders = new ArrayList<>();
			orders.add(new Order(Direction.ASC, "systemConfig.scid"));
			orders.add(new Order(Direction.ASC, "scname"));
			PageRequest pageable = PageRequest.of(batch, total, Sort.by(orders));

			/// Step4.取得資料(一般/細節)
			ArrayList<SystemConfig> systemConfigs = configDao.findAllByConfig(null, null, null, null, 0, pageable);
			String configJson = packageService.beanToJson(systemConfigs);
			packageBean.setEntityJson(configJson);

			// (測是用)區分父子類別
			ArrayList<SystemConfig> systemCDatas = new ArrayList<>();
			ArrayList<SystemConfig> systemCDetails = new ArrayList<>();
			systemConfigs.forEach(x -> {
				x.setScgid(x.getScid());
				systemCDatas.add(x);// 父類別
				x.getSystemConfigs().forEach(y -> {
					y.setSystemConfig(null); 
					y.setScgid(x.getScid());
					systemCDetails.add(y);// 子類別
				});
			});

			String configJDatas = packageService.beanToJson(systemCDatas);
			packageBean.setEntityJson(configJDatas);
			String configJDetails = packageService.beanToJson(systemCDetails);
			packageBean.setEntityDetailJson(configJDetails);

			// Step6. 取得翻譯(一般/細節)
			ArrayList<SystemLanguageCell> languages = languageDao.findAllBySystemUser("system_config", 0, null);
			Map<String, SystemLanguageCell> mapLanguages = new HashMap<>();
			languages.forEach(x -> {
				mapLanguages.put(x.getSltarget(), x);
			});
			ArrayList<SystemLanguageCell> languagesDetail = languageDao.findAllBySystemUser("system_config", 0, null);
			Map<String, SystemLanguageCell> mapLanguagesDetail = new HashMap<>();
			languagesDetail.forEach(x -> {
				mapLanguagesDetail.put(x.getSltarget(), x);
			});

			// ==============建立查詢欄位(涵蓋修改資料選項)==============
			JsonObject searchSetJsonAll = new JsonObject();
			JsonArray searchJsons = new JsonArray();// 查詢設定
			JsonObject resultDataTJs = new JsonObject();// 一般回傳欄位-名稱
			JsonObject resultDetailTJs = new JsonObject();// 一般回傳欄位-細節名稱

			// 結果欄位(名稱Entity變數定義)=>取出=>排除/寬度/語言/順序
			Field[] fields = SystemConfig.class.getDeclaredFields();
			ArrayList<String> exceptionCell = new ArrayList<>();
			exceptionCell.add("systemConfig");
			exceptionCell.add("systemConfigs");
			
			resultDataTJs = packageService.resultSet(resultDataTJs, fields, exceptionCell, mapLanguages);
			resultDetailTJs = packageService.resultSet(resultDetailTJs, fields, exceptionCell, mapLanguagesDetail);

			// Step7. 建立查詢項目
			searchJsons = packageService.searchSet(searchJsons, null, "scname", "Ex:DB_NAME", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_md_2);
			searchJsons = packageService.searchSet(searchJsons, null, "scgname", "Ex:DATA_BKUP", true, //
					PackageService.SearchType.text, PackageService.SearchWidth.col_md_2);
			// 查詢項目-時間開始
			searchJsons = packageService.searchSet(searchJsons, null, "sysmdatestart", "Ex:2011-01-02 12:12:00", true, //
					PackageService.SearchType.datetime, PackageService.SearchWidth.col_md_2);
			// 查詢項目-時間結束
			searchJsons = packageService.searchSet(searchJsons, null, "sysmdateend", "Ex:2011-01-02 12:12:00", true, //
					PackageService.SearchType.datetime, PackageService.SearchWidth.col_md_2);
			// 查詢項目-狀態
			JsonArray selectArr = new JsonArray();
			selectArr.add("normal(正常)_0");
			selectArr.add("completed(完成)_1");
			selectArr.add("disabled(禁用)_2");
			selectArr.add("onlyAdmin(特權)_3");
			searchJsons = packageService.searchSet(searchJsons, selectArr, "sysstatus", "", true, //
					PackageService.SearchType.select, PackageService.SearchWidth.col_md_2);
			// 主KEY AND 群組KEY 名稱
			packageBean.setEntityIKeyGKey("scid_scgid");
			// 查詢包裝
			searchSetJsonAll.add("searchSet", searchJsons);
			searchSetJsonAll.add("resultThead", resultDataTJs);
			searchSetJsonAll.add("resultDetailThead", resultDetailTJs);
			packageBean.setSearchSet(searchSetJsonAll.toString());
		} else {
			// 查詢
			// 批次分頁
			JsonObject pageSetJson = JsonParser.parseString(packageBean.getSearchPageSet()).getAsJsonObject();
			int total = pageSetJson.get("total").getAsInt();
			int batch = pageSetJson.get("batch").getAsInt();
			// 排序
			List<Order> orders = new ArrayList<>();
			orders.add(new Order(Direction.DESC, "systemConfig.scid"));
			orders.add(new Order(Direction.ASC, "scname"));
			PageRequest pageable = PageRequest.of(batch, total, Sort.by(orders));

			// 取得資料(一般/細節)
			SystemConfig systemConfig = packageService.jsonToBean(packageBean.getEntityJson(), SystemConfig.class);
			ArrayList<SystemConfig> systemConfigs = configDao.findAllByConfig(//
					systemConfig.getScname(), systemConfig.getScgname(), //
					systemConfig.getSysmdatestart(), systemConfig.getSysmdateend(), 0, pageable);
			// (測是用)區分父子類別
			ArrayList<SystemConfig> systemCDatas = new ArrayList<>();
			ArrayList<SystemConfig> systemCDetails = new ArrayList<>();
			systemConfigs.forEach(x -> {
				x.setScgid(x.getScid());
				systemCDatas.add(x);// 父類別
				x.getSystemConfigs().forEach(y -> {
					//y.setSystemConfig(null);
					y.setScgid(x.getScid());
					systemCDetails.add(y);// 子類別
				});
			});

			String configJson = packageService.beanToJson(systemCDatas);
			packageBean.setEntityJson(configJson);
			String configJDetails = packageService.beanToJson(systemCDetails);
			packageBean.setEntityDetailJson(configJDetails);

			// 主KEY AND 群組KEY 名稱
			packageBean.setEntityIKeyGKey("scid_scgid");
			packageBean.setEntityDetailJson("");
		}
		return packageBean;
	}

}
