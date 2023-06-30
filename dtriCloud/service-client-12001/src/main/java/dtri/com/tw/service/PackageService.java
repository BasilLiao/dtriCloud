package dtri.com.tw.service;

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

import dtri.com.tw.db.entity.SystemLanguageCell;

@Service
public class PackageService {

	// 查詢欄位-格式
	public enum SearchType {
		text, select, datetime_local, datetime
	};

	// 寬度
	public enum SearchWidth {
		col_2, col_3, col_4
	}

	// Stirng to JSON(一般轉換)
	public JsonObject StringToJson(String s) {
		JsonObject jsonObject = JsonParser.parseString(s).getAsJsonObject();
		return jsonObject;
	}

	// JSON to Bean(單一包裝)
	public <T> T jsonToBean(String packageJson, Class<T> valueType) throws JsonMappingException, JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		T packageBean = objectMapper.readValue(packageJson, valueType);
		return packageBean;
	}

	// JSON to Beans(複數包裝)
	public <T> ArrayList<T> jsonToBean(String packageJson, TypeReference<ArrayList<T>> typeReference)
			throws JsonMappingException, JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		ArrayList<T> packageBean = objectMapper.readValue(packageJson, typeReference);
		return packageBean;
	}

	// (PackageBean/Object)Bean to JSON
	public String beanToJson(Object packageBean) throws JsonProcessingException {
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
	 * 
	 **/
	public JsonArray searchSet(JsonArray arr, JsonArray selectArr, String name, SearchType type, SearchWidth width) {
		JsonObject Json = new JsonObject();// 查詢設定項目
		Json.addProperty("searchName", name);
		Json.addProperty("type", type + "");// text/select/datetime/datetime_local
		Json.addProperty("width", width + "");
		if (selectArr != null && selectArr.size() > 0) {
			Json.add("select", selectArr);
		}
		arr.add(Json);
		return arr;
	}

	// 一般回傳資料
	public JsonObject resultSet(JsonObject arr, Field[] fields, Map<String, SystemLanguageCell> mapLanguages) {
		JsonObject resultJson = new JsonObject();// 每一格-名稱
		String sort_cellName = "";
		for (Field field : fields) {
			System.out.println(field.getName());
			// 有比對到=>欄位資料 設定||沒比對到=>欄位資料 預設
			resultJson = new JsonObject();
			resultJson.addProperty("cellName", field.getName());
			String sort = "999";
			if (mapLanguages.containsKey(field.getName())) {
				sort = String.format("%03d", mapLanguages.get(field.getName()).getSyssort());
				resultJson.addProperty("sort", sort);
				resultJson.addProperty("show", mapLanguages.get(field.getName()).getSlcshow());
				resultJson.addProperty("width", mapLanguages.get(field.getName()).getSlcwidth());
				resultJson.addProperty("cellLanguage", mapLanguages.get(field.getName()).getSllanguage());
				sort_cellName = sort + "_" + field.getName();
			} else {
				resultJson.addProperty("sort", "999");
				resultJson.addProperty("show", 1);
				resultJson.addProperty("width", 100);
				resultJson.addProperty("cellLanguage", "");
				sort_cellName = sort + "_" + field.getName();
			}
			arr.add(sort_cellName, resultJson);
		}
		return arr;
	}

	// 細節回傳資料
	public JsonArray resultDetailSet(JsonArray arr) {
		return arr;
	}
}
