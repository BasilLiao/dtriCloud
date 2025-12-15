package dtri.com.tw.shared;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dtri.com.tw.pgsql.entity.SystemLanguageCell;

@Service
public class PackageService {

	// 查詢欄位-格式
	public enum SearchType {
		text, select, time, date, datetime
	};

	// 寬度
	public enum SearchWidth {
		col_lg_1, col_lg_2, col_lg_3, col_lg_4, col_lg_5, col_lg_6
	}

	// Stirng to JSON(一般轉換)
	public JsonObject StringToJson(String s) {
		try {
			JsonObject jsonObject = JsonParser.parseString(s).getAsJsonObject();
			return jsonObject;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// Stirng to JsonArray(一般轉換)
	public JsonArray StringToAJson(String s) {
		try {
			JsonArray jsonObject = JsonParser.parseString(s).getAsJsonArray();
			return jsonObject;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// JSON to Bean(單一包裝)
	public <T> T jsonToBean(String packageJson, Class<T> valueType)
			throws JsonMappingException, JsonProcessingException, Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		T packageBean = objectMapper.readValue(packageJson, valueType);
		return packageBean;
	}

	// JSON to Beans(複數包裝)
	public <T> ArrayList<T> jsonToBean(String packageJson, TypeReference<ArrayList<T>> typeReference)
			throws JsonMappingException, JsonProcessingException, Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		ArrayList<T> packageBean = objectMapper.readValue(packageJson, typeReference);
		return packageBean;
	}

	// (PackageBean/Object)Bean to JSON
	public String beanToJson(Object packageBean) throws JsonProcessingException, Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		String packageJson = objectMapper.writeValueAsString(packageBean);
		return packageJson;
	}

	/**
	 * searchSet(查詢項目)
	 * 
	 * @param arr       要放入的對象
	 * @param selectArr 如果是Select 則請放入 key_value
	 * @param name      標題名稱
	 * @param type      類型 text/select/datetime/datetime_local
	 * @param width     寬度 col_2, col_3, col_4
	 * @param show      顯示 查詢用?(true)修改用?(如果查詢用不到:false)
	 * 
	 **/
	public JsonArray searchSet(JsonArray arr, JsonArray selectArr, String name, String placeholder, Boolean show,
			SearchType type, SearchWidth width) {
		JsonObject Json = new JsonObject();// 查詢設定項目
		Json.addProperty("searchName", name);
		Json.addProperty("type", type + "");// text/select/datetime/datetime_local
		Json.addProperty("width", width + "");
		Json.addProperty("placeholder", placeholder + "");
		Json.addProperty("show", show);
		if (selectArr != null && selectArr.size() > 0) {
			Json.add("select", selectArr);
		}
		arr.add(Json);
		return arr;
	}

	// 一般回傳資料(欄位)
	public JsonObject resultSet(Field[] fields, ArrayList<String> exception,
			Map<String, SystemLanguageCell> mapLanguages) {
		JsonObject returnArr = new JsonObject();
		String sort_cellName = "";
		String fieldName = "";
		for (Field field : fields) {
			JsonObject resultJson = new JsonObject();// 每一格-名稱
			System.out.println(field.getName());
			// 有比對到=>欄位資料 設定||沒比對到=>欄位資料 預設
			String sort = "999";
			// 欄位-例外不納入前端使用
			boolean check = exception.contains(field.getName());
			if (!check) {
				if (mapLanguages.containsKey(field.getName())) {
					// 查詢欄位
					sort = String.format("%03d", mapLanguages.get(field.getName()).getSyssort());
					sort_cellName = sort + "_" + field.getName();
					fieldName = field.getName() + "";
					resultJson = new JsonObject();
					resultJson.addProperty("cellName", fieldName);
					resultJson.addProperty("sort", sort);
					resultJson.addProperty("show", mapLanguages.get(field.getName()).getSlcshow());
					resultJson.addProperty("width", mapLanguages.get(field.getName()).getSlcwidth());
					resultJson.addProperty("cellLanguage", mapLanguages.get(field.getName()).getSllanguage());
					// 修改欄位
					resultJson.addProperty("m_show", mapLanguages.get(field.getName()).getSlcmshow());
					resultJson.addProperty("m_type", mapLanguages.get(field.getName()).getSlcmtype());
					resultJson.addProperty("m_placeholder", mapLanguages.get(field.getName()).getSlcmplaceholder());
					resultJson.addProperty("m_defval", mapLanguages.get(field.getName()).getSlcmdefval());
					resultJson.addProperty("m_must", mapLanguages.get(field.getName()).getSlcmmust());
					resultJson.addProperty("m_select", "" + mapLanguages.get(field.getName()).getSlcmselect());
					resultJson.addProperty("m_fixed", mapLanguages.get(field.getName()).getSlcmfixed());
					returnArr.add(sort_cellName, resultJson);
				} else {
					// 查詢欄位
					sort_cellName = sort + "_" + field.getName(); // 修改欄位
					fieldName = field.getName() + "";
					resultJson.addProperty("cellName", fieldName);
					resultJson.addProperty("sort", "999");
					resultJson.addProperty("show", 1);
					resultJson.addProperty("width", 100);
					resultJson.addProperty("cellLanguage", "");
					resultJson.addProperty("m_show", 1);
					resultJson.addProperty("m_type", "text");
					resultJson.addProperty("m_placeholder", "Ex:");
					resultJson.addProperty("m_defval", "");
					resultJson.addProperty("m_must", 0);
					resultJson.addProperty("m_select", "" + new JsonArray());
					resultJson.addProperty("m_fixed", 0);
					returnArr.add(sort_cellName, resultJson);
				}
			}
		}
		return returnArr;
	}

	// 細節回傳資料
	public JsonArray resultDetailSet(JsonArray arr) {
		return arr;
	}
}
