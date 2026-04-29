package dtri.com.tw.shared;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dtri.com.tw.pgsql.entity.SystemLanguageCell;

@Service
public class PackageService {

	private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new AfterburnerModule());

	/**
	 * 抓取涵蓋父類別-繼承物件Entity
	 * 
	 */
	public static Field[] getEntityFields(Class<?> currentClass) {
		List<Field> allFields = new ArrayList<>();

		// 向上追溯，直到沒有父類別 (Object 的父類是 null)
		while (currentClass != null) {
			Field[] declaredFields = currentClass.getDeclaredFields();
			allFields.addAll(Arrays.asList(declaredFields));
			// 關鍵：移動到父類別
			currentClass = currentClass.getSuperclass();
		}
		// 轉回陣列進行後續的 翻譯/排除/順序 處理
		return allFields.toArray(new Field[0]);
	}

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
		T packageBean = objectMapper.readValue(packageJson, valueType);
		return packageBean;
	}

	// JSON to Beans(複數包裝)
	public <T> ArrayList<T> jsonToBean(String packageJson, TypeReference<ArrayList<T>> typeReference)
			throws JsonMappingException, JsonProcessingException, Exception {
		ArrayList<T> packageBean = objectMapper.readValue(packageJson, typeReference);
		return packageBean;
	}

	// (PackageBean/Object)Bean to JSON
	public String beanToJson(Object packageBean) throws JsonProcessingException, Exception {
		String packageJson = objectMapper.writeValueAsString(packageBean);
		return packageJson;
	}

	// 用於快取不同已解析的類別與它們對應的有效欄位 (加上 JsonIgnore 與 static 檢查)
	private static final java.util.concurrent.ConcurrentHashMap<Class<?>, java.util.List<Field>> fieldCache = new java.util.concurrent.ConcurrentHashMap<>();

	// (List<T>)List to Matrix JSON (減少重複Key體積)
	public <T> String beanToMatrixJson(java.util.List<T> list) throws Exception {
		if (list == null || list.isEmpty()) {
			return "{\"c\":[],\"d\":[]}";
		}

		// 為了避免共用 Instance 衝突，此處 new 一個新的
		ObjectMapper localMapper = new ObjectMapper().registerModule(new AfterburnerModule());

		Class<?> clazz = list.get(0).getClass(); // 取第一個元素的型別

		// 1. 取得或產生快取的欄位資訊 (包含父類別，並過濾掉 @JsonIgnore 及 static)
		java.util.List<Field> validFields = fieldCache.computeIfAbsent(clazz, k -> {
			java.util.List<Field> fields = new java.util.ArrayList<>();
			for (Class<?> c = k; c != null && c != Object.class; c = c.getSuperclass()) {
				for (Field f : c.getDeclaredFields()) {
					if (java.lang.reflect.Modifier.isStatic(f.getModifiers()))
						continue; // 略過靜態欄位
					if (!f.isAnnotationPresent(com.fasterxml.jackson.annotation.JsonIgnore.class)) {
						f.setAccessible(true); // 提升反射效能並允許存取 private
						fields.add(f);
					}
				}
			}
			return fields;
		});

		if (validFields.isEmpty()) {
			return "{\"c\":[],\"d\":[]}";
		}

		// 2. 建立 columns (c) 陣列
		java.util.List<String> columns = new java.util.ArrayList<>(validFields.size());
		for (Field f : validFields) {
			String columnName = f.getName();
			if (f.isAnnotationPresent(com.fasterxml.jackson.annotation.JsonProperty.class)) {
				columnName = f.getAnnotation(com.fasterxml.jackson.annotation.JsonProperty.class).value();
			}
			columns.add(columnName);
		}

		Map<String, Object> matrix = new java.util.HashMap<>();
		matrix.put("c", columns);

		// 3. 建立 data (d) 陣列
		java.util.List<java.util.List<Object>> data = new java.util.ArrayList<>(list.size());
		for (T item : list) {
			java.util.List<Object> row = new java.util.ArrayList<>(validFields.size());
			for (Field f : validFields) {
				row.add(f.get(item));
			}
			data.add(row);
		}
		matrix.put("d", data);

		// 最後轉換 Map 回 JSON 字串
		return localMapper.writeValueAsString(matrix);
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
