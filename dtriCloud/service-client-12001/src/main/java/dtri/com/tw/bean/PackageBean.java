package dtri.com.tw.bean;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dtri.com.tw.service.CloudExceptionService;

/**
 * 只能用 String Date Int
 * 
 */
public class PackageBean {
	// 可能是任何的型別->先轉成JSON Array->ToString->依照不同單元所帶入
	private String entityJson;
	private String entityIKeyGKey;// Key名稱_GKey名稱Ex:suid_sugid
	private String entityFormatJson;// 格式

	// 可能是任何的型別->先轉成JSON Array->ToString->依照不同單元所帶入
	private String entityDetailJson;
	private String entityDetailIKeyGKey;// Key名稱_GKey名稱Ex:suid_sugid
	// 報告用
	private String entityReportJson;
	// 可能是時間格式
	private String entityDateTime;

	// 轉跳對象
	// 目的地單元(body_index.html/body_system_config.html....)
	private String htmlBody;
	private String htmlBodyUnitName;

	// 訊息回饋(warning, danger, primary;)
	private String info;
	private String infoColor;

	// 回傳呼叫(方法/參數JSON)
	private String callBackFunction;
	private String callBackValue;

	/**
	 * 查詢UI分頁-設定 Ex <br>
	 * {searchPageSet:{total:1000,batch:0~999}}
	 */
	private String searchPageSet;

	/**
	 * 查詢UI項目-設定 <br>
	 * {searchSet:{ <br>
	 * }<br>
	 */
	private String searchSet;

	/**
	 * 修改UI項目-設定 <br>
	 * {modifyItemSet:[name:XXX,inputType:(input/select/date/dateTime/number)]
	 * 
	 * }
	 */
	private String modifySet;
	/**
	 * 報表UI項目-設定 <br>
	 * {}
	 */
	private String reportSet;
	/**
	 * 其他項目-設定 <br>
	 * {}
	 */
	private String otherSet;

	public PackageBean() {
		this.infoColor = "success";
		this.info = "The command has completed successfully!!";
		this.entityJson = "";// 可能是複數
		this.setHtmlBody("");
		this.setHtmlBodyUnitName("");
		// 回傳Function
		this.callBackFunction = "";
		this.callBackValue = "";
		// 設定-查詢UI分頁
		JsonObject searchPageSetJson = new JsonObject();
		searchPageSetJson.addProperty("total", 1000);// 每一次幾筆資料
		searchPageSetJson.addProperty("batch", 0);// 第幾批次
		this.searchPageSet = searchPageSetJson.toString();
		// 設定-查詢結果
		JsonObject searchSetJson = new JsonObject();
		JsonArray searchItems = new JsonArray();
		JsonObject searchItem = new JsonObject();
		searchItem.addProperty("name", "");// 查詢欄位-名稱
		searchItem.addProperty("value", "");// 查詢欄-位值
		searchItem.addProperty("placeholder", "");// 查詢欄-說明
		searchItem.addProperty("inputType", "");// 欄位類型
		searchItem.add("select", new JsonArray());// 如果是多選
		searchItems.add(searchItem);
		searchSetJson.add("searchItem", searchItems);// 查詢欄位
		searchSetJson.addProperty("notShowResult", "");// 不顯示-父項目
		searchSetJson.addProperty("notShowDetailResult", "");// 不顯示-子項目
		this.searchSet = searchSetJson.toString();

		// 時間欄位|主KEY|群組KEY
		this.entityDateTime = "syscdate_sysmdate_sysodate";
		this.entityDetailIKeyGKey = "";
		this.entityIKeyGKey = "";

		this.modifySet = "";
		this.reportSet = "";
		this.otherSet = "";

	}

	public String getEntityJson() {
		return entityJson;
	}

	public void setEntityJson(String entityJson) {
		this.entityJson = entityJson;
	}

	public String getCallBackFunction() {
		return callBackFunction;
	}

	public void setCallBackFunction(String callBackFunction) {
		this.callBackFunction = callBackFunction;
	}

	public String getCallBackValue() {
		return callBackValue;
	}

	public void setCallBackValue(String callBackValue) {
		this.callBackValue = callBackValue;
	}

	public String getInfoColor() {
		return infoColor;
	}

	public void setInfoColor(CloudExceptionService.ErColor infoColor) {
		this.infoColor = infoColor + "";
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getSearchPageSet() {
		return searchPageSet;
	}

	public void setSearchPageSet(int total, int batch) {
		JsonObject searchPageSetJson = new JsonObject();
		searchPageSetJson.addProperty("total", total);// 每一次幾筆資料
		searchPageSetJson.addProperty("batch", batch);// 第幾批次
		this.searchPageSet = searchPageSetJson.toString();
	}

	public String getSearchSet() {
		return searchSet;
	}

	public void setSearchSet(String searchSet) {
		this.searchSet = searchSet;
	}

	public String getModifySet() {
		// return JsonParser.parseString(modifySet).getAsJsonObject();
		return modifySet;
	}

	public void setModifySet(String modifySet) {
		this.modifySet = modifySet;
	}

	public String getReportSet() {
		return reportSet;
	}

	public void setReportSet(String reportSet) {
		this.reportSet = reportSet;
	}

	public String getOtherSet() {
		return otherSet;
	}

	public void setOtherSet(String otherSet) {
		this.otherSet = otherSet;
	}

	public String getHtmlBody() {
		return htmlBody;
	}

	public void setHtmlBody(String htmlBody) {
		this.htmlBody = htmlBody;
	}

	public String getHtmlBodyUnitName() {
		return htmlBodyUnitName;
	}

	public void setHtmlBodyUnitName(String htmlBodyUnitName) {
		this.htmlBodyUnitName = htmlBodyUnitName;
	}

	public String getEntityDetailJson() {
		return entityDetailJson;
	}

	public void setEntityDetailJson(String entityDetailJson) {
		this.entityDetailJson = entityDetailJson;
	}

	public String getEntityIKeyGKey() {
		return entityIKeyGKey;
	}

	public void setEntityIKeyGKey(String entityIKeyGKey) {
		this.entityIKeyGKey = entityIKeyGKey;
	}

	public String getEntityDetailIKeyGKey() {
		return entityDetailIKeyGKey;
	}

	public void setEntityDetailIKeyGKey(String entityDetailIKeyGKey) {
		this.entityDetailIKeyGKey = entityDetailIKeyGKey;
	}

	public String getEntityDateTime() {
		return entityDateTime;
	}

	public void setEntityDateTime(String entityDateTime) {
		this.entityDateTime = entityDateTime;
	}

	public String getEntityFormatJson() {
		return entityFormatJson;
	}

	public void setEntityFormatJson(String entityFormatJson) {
		this.entityFormatJson = entityFormatJson;
	}

	public String getEntityReportJson() {
		return entityReportJson;
	}

	public void setEntityReportJson(String entityReportJson) {
		this.entityReportJson = entityReportJson;
	}
}