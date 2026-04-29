package dtri.com.tw.pgsql.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * @author Basil
 * @see ---物料相關模組-前端控制項專用(假實體)---<br>
 *      此類別整合了「缺料預計」、「EOL 估算」、「物料替代」與「虛擬專案」的前端控制項。<br>
 *      包含：語系(i18n)、彈窗標題、按鈕、欄位顯示等，不進行資料持久化。<br>
 */
@Entity
@Table(name = "material_front")
public class MaterialFront {

	@Id
	private String id;

	// =========================================================================
	// === 1. 共通 / 全域 (Common & Global) ===
	// =========================================================================
	@Transient
	private String mod_material_shortage; // 物料缺料預計
	@Transient
	private String mod_material_eol; // 停產物料估算
	@Transient
	private String mod_material_replacement; // 物料替代規則
	@Transient
	private String mod_material_virtual_project; // 產品系列主建

	@Transient
	private String btn_close; // 關閉
	@Transient
	private String btn_reset; // 重置
	@Transient
	private String btn_create; // 新增
	@Transient
	private String btn_save; // 儲存 (共用)
	@Transient
	private String btn_export; // 匯出 (共用)
	@Transient
	private String btn_confirm; // 確認 (共用)
	@Transient
	private String btn_delete; // 刪除 (共用)
	@Transient
	private String btn_cancel; // 取消 (共用)
	@Transient
	private String label_sysnote; // 備註 (共用)
	@Transient
	private String msg_loading; // Loading...
	@Transient
	private String msg_no_data; // 目前無資料
	@Transient
	private String msg_processing; // 處理中...
	@Transient
	private String msg_downloading; // 下載中...
	@Transient
	private String alert_leave_dirty; // 此專案有尚未儲存的變更！確定要離開嗎？
	@Transient
	private String label_not_saved; // 尚未儲存

	// =========================================================================
	// === 2. 物料缺料預計 (Material Shortage) ===
	// =========================================================================

	@Transient
	private String title_alt_selection; // 選擇替代方案
	@Transient
	private String title_alt_history; // 已執行的替代紀錄
	@Transient
	private String title_recalculate; // 庫存試算 (Recalculate)
	@Transient
	private String title_committed_list; // 已執行替代清單
	@Transient
	private String title_click_exec; // 點擊執行替代方案
	@Transient
	private String title_view_executed; // 查看已執行的筆數
	@Transient
	private String title_delete_record; // 刪除此筆
	@Transient
	private String label_for_part_no; // 針對料號
	@Transient
	private String label_include_transit; // 含待驗量
	@Transient
	private String label_shortage_qty; // 缺欠
	@Transient
	private String label_exec_qty; // 本次執行數量
	@Transient
	private String label_select_warehouses; // 1. 選擇試算倉別
	@Transient
	private String label_select_order_types; // 2. 選擇試算單別
	@Transient
	private String label_filter_part_no; // 3. 品號篩選 (選填)
	@Transient
	private String label_required; // (必選)
	@Transient
	private String label_limit_product; // 限定產品
	@Transient
	private String label_limit_customer; // 限定客戶
	@Transient
	private String label_universal; // 通用
	@Transient
	private String label_consume; // 消耗
	@Transient
	private String label_link; // 連動
	@Transient
	private String btn_fill_max; // 填入最大
	@Transient
	private String btn_confirm_exec; // 確認執行
	@Transient
	private String btn_search_exec; // 開始試算
	@Transient
	private String btn_recalculate; // 試算
	@Transient
	private String btn_alt_list; // 替代清單
	@Transient
	private String btn_modify; // 追加/修改
	@Transient
	private String btn_execute; // 執行替代
	@Transient
	private String msg_no_alt_rule; // 此品號無替代規則
	@Transient
	private String msg_alt_rule_scope_mismatch; // 替代規則不符合本工單適用範圍
	@Transient
	private String msg_stock_insufficient; // ⚠️ 庫存不足:
	@Transient
	private String msg_querying_stock; // 正在查詢即時庫存...
	@Transient
	private String msg_balance_insufficient; // ❌ 餘額不足！無法扣除。
	@Transient
	private String msg_stock_insufficient_detail; // ❌ 庫存不足: 剩餘 X / 需 Y
	@Transient
	private String msg_confirm_delete; // 確定要刪除嗎？
	@Transient
	private String msg_running_mrp; // 正在執行 MRP 試算...
	@Transient
	private String msg_finalizing; // 正在完成最後數據封裝...
	@Transient
	private String msg_data_fetched; // Data Fetched:
	@Transient
	private String msg_no_history; // 目前沒有任何已執行的替代紀錄。
	@Transient
	private String msg_invalid_qty; // 請輸入有效數量
	@Transient
	private String msg_recalc_success; // 試算更新完成
	@Transient
	private String msg_missing_in_list; // 未在目前清單中
	@Transient
	private String msg_select_warehouse_req; // 請選擇至少一個試算倉別
	@Transient
	private String msg_select_doctype_req; // 請選擇至少一個單據類型
	@Transient
	private String val_stock_sufficient; // 🟢 充足
	@Transient
	private String val_stock_shortage; // 🔴 缺
	@Transient
	private String val_alt_qty; // ✅ 已替
	@Transient
	private String val_allocated; // 佔用
	@Transient
	private String val_none; // 無
	@Transient
	private String simadvice; // 模擬建議 (Simulation Advice)
	@Transient
	private String placeholder_part_no; // 品號搜尋
	@Transient
	private String placeholder_exclude_doc; // 排除單號 (如 A1,B2)
	@Transient
	private String placeholder_input_qty; // 輸入數量
	@Transient
	private String help_recalculate; // Leave empty for full recalculation.
	@Transient
	private String filter_doc_type; // 單別篩選
	@Transient
	private String filter_all; // 顯示全部
	@Transient
	private String filter_has_rule; // ✅ 有替代規則
	@Transient
	private String filter_no_rule; // ⬜ 無替代規則
	@Transient
	private String order_type_m; // 製令單
	@Transient
	private String order_type_m_ret; // 製令單(退)
	@Transient
	private String order_type_im; // 內製令單
	@Transient
	private String order_type_p; // 採購單
	@Transient
	private String order_type_ir; // 進貨單
	@Transient
	private String order_type_pr; // 請購單
	@Transient
	private String order_type_co; // 客訂單

	// =========================================================================
	// === 3. 停產物料估算 (Material EOL) ===
	// =========================================================================
	@Transient
	private String title_criteria; // 估算條件
	@Transient
	private String col_root_bom; // 來源 BOM
	@Transient
	private String col_bom_level; // 階層
	@Transient
	private String col_part_no; // 品號
	@Transient
	private String col_part_name; // 品名
	@Transient
	private String col_qty_per_set; // 組合用量
	@Transient
	private String col_wh_stock; // 庫存數量
	@Transient
	private String col_available_sets; // 可做(組合套)
	@Transient
	private String col_support_months; // 可撐(月)
	@Transient
	private String label_bom_list; // BOM 清單
	@Transient
	private String label_max_level; // 最大階層
	@Transient
	private String label_monthly_demand; // 月用量
	@Transient
	private String label_warehouse; // 倉別設定
	@Transient
	private String btn_calculate; // 計算

	// =========================================================================
	// === 4. 物料替代規則 (Material Replacement) ===
	// =========================================================================
	@Transient
	private String title_substitution_rules; // 替代規則設定
	@Transient
	private String msg_policy_eq; // 🟢 雙向替代 (Equivalent)
	@Transient
	private String msg_policy_rd; // 🔴 單向取代 (Run-down)
	@Transient
	private String msg_source; // 原始料號 (Source)
	@Transient
	private String msg_target; // 替代料號 (Target)
	@Transient
	private String scope_0; // 🌍 通用規則
	@Transient
	private String scope_1; // 🏢 指定客戶
	@Transient
	private String scope_2; // 📦 指定產品
	@Transient
	private String msg_sh_customer; // 輸入客戶代號或名稱...
	@Transient
	private String msg_sh_product; // 輸入產品代號...

	// =========================================================================
	// === 5. 虛擬專案模擬 (Material Virtual Project) ===
	// =========================================================================
	@Transient
	private String tab_other_project_settings; // 專案設定/模擬
	@Transient
	private String placeholder_search_project; // 搜尋專案名稱...
	@Transient
	private String placeholder_mvp_memo; // 可輸入專案相關備註...
	@Transient
	private String help_mvp_bom; // (搜尋機型...)
	@Transient
	private String placeholder_mvp_bom; // 輸入BOM號/機型/規格...
	@Transient
	private String label_mvp_exclude; // 排除前綴
	@Transient
	private String placeholder_mvp_exclude; // 例：81-
	@Transient
	private String label_mvp_warehouses; // 計算倉別設定
	@Transient
	private String val_mvp_all_warehouses; // 所有可用倉別
	@Transient
	private String btn_mvp_apply_close; // 套用並收合
	@Transient
	private String label_mvp_include; // 強制抓取物料
	@Transient
	private String help_mvp_include; // (關鍵字搜尋...)
	@Transient
	private String placeholder_mvp_include; // 品名/關鍵字...
	@Transient
	private String btn_mvp_other_projects; // 納入其他專案
	@Transient
	private String help_mvp_sim_info; // 請先設定目標，並可在下方表格填寫特定料號的「延遲天數」，再點擊執行。
	@Transient
	private String placeholder_mvp_global_search; // 搜尋物料...
	@Transient
	private String btn_mvp_apply_category; // 套用分類
	@Transient
	private String btn_mvp_move_to_sub; // 移至副物料
	@Transient
	private String btn_mvp_move_to_main; // 移至主物料
	@Transient
	private String title_mvp_report; // 模擬生產報告
	@Transient
	private String label_mvp_bottlenecks; // 瓶頸物料數
	@Transient
	private String title_mvp_detail; // 預計明細
	@Transient
	private String btn_mvp_tree_mode; // 樹狀模式
	@Transient
	private String btn_mvp_flat_mode; // 扁平模式
	@Transient
	private String placeholder_mvp_detail_search; // 搜尋...
	@Transient
	private String title_mvp_replacement; // 替代方案選擇
	@Transient
	private String title_mvp_custom_order; // 自訂單據管理
	@Transient
	private String msg_mvp_dirty_confirm; // 有尚未儲存變更
	@Transient
	private String msg_mvp_delete_confirm; // 確定要刪除專案嗎？
	@Transient
	private String title_mvp_selection; // 物料挑選
	@Transient
	private String label_mvp_selected; // 已選取
	@Transient
	private String val_mvp_atp_pass; // 備料充足，可安全發料
	@Transient
	private String val_mvp_atp_warning; // 被未來排程預約
	@Transient
	private String val_mvp_atp_error; // 庫存不足
	@Transient
	private String label_atp_safe_sets; // 安全供貨上限
	@Transient
	private String label_atp_shortage; // 距離齊套尚缺
	@Transient
	private String label_atp_future_warn; // 強行投產將引發未來缺料風險
	@Transient
	private String val_mvp_priority_1; // 極高
	@Transient
	private String val_mvp_priority_2; // 預設
	@Transient
	private String val_mvp_priority_3; // 極低
	@Transient
	private String val_mvp_reading; // 讀取中...
	@Transient
	private String val_mvp_found_count_short; // 共 {0} 筆
	@Transient
	private String alert_mvp_export_no_data; // 無資料可匯出
	@Transient
	private String btn_mvp_restore_window; // 恢復視窗
	@Transient
	private String alert_mvp_need_select; // 請先勾選項目
	@Transient
	private String alert_mvp_need_qty_date; // 請填寫數量與日期
	@Transient
	private String alert_mvp_qty_nonzero; // 數量需 > 0

	@Transient
	private String val_mvp_has_repl_tip; // 有替代方案
	@Transient
	private String alert_mvp_need_material; // 請至少勾選一項物料
	@Transient
	private String val_mvp_analyzing; // 分析中...
	@Transient
	private String val_mvp_simulating; // 模擬中...
	@Transient
	private String label_mvp_finish_date; // 完工日
	@Transient
	private String btn_run_sim; // 執行模擬
	@Transient
	private String btn_full_screen; // 全螢幕
	@Transient
	private String label_mvp_rule; // 規則
	@Transient
	private String help_mvp_replacement; // 將選定的方案從此料號的缺口中沖銷

	@Transient
	private String btn_select_all; // 全選
	@Transient
	private String btn_select_none; // 全不選
	@Transient
	private String btn_search; // 搜尋
	@Transient
	private String btn_analyze; // 分析
	@Transient
	private String label_mvp_target_sets; // 試算模擬基準數
	@Transient
	private String label_mvp_max_sets; // 現貨可做上限
	@Transient
	private String label_target_qty; // 預計生產總套數
	@Transient
	private String label_mvp_demand_total; // 需求總量

	@Transient
	private String label_sel_leaf; // 全選零件
	@Transient
	private String label_sel_full; // 全選 (零件+組件)
	@Transient
	private String label_sel_clear; // 清除

	@Transient
	private String val_sets; // 套
	@Transient
	private String val_items; // 項
	@Transient
	private String placeholder_mvp_category; // 批次分類名稱(如:Camera)
	@Transient
	private String title_mvp_others; // 專案模擬競爭
	@Transient
	private String help_mvp_others_modal; // 打勾表示要納入本次模擬。您可以<strong>臨時修改</strong>下方清單中的預計套數與預交日進行「假設性分析(What-If)」，這些修改<strong>不會</strong>回寫到原專案資料庫。
	@Transient
	private String help_mvp_selection; // BOM 已展開完畢，請勾選要納入專案的料件
	@Transient
	private String label_mvp_custom_add_title; // 新增虛擬單據
	@Transient
	private String label_mvp_custom_qty_label; // 數量 (正=進貨, 負=領用)
	@Transient
	private String label_mvp_custom_date_label; // 預計交期
	@Transient
	private String label_mvp_custom_list_title; // 已設定單據清單
	@Transient
	private String label_mvp_custom_list_th1; // 交期
	@Transient
	private String label_mvp_custom_list_th2; // 數量變動
	@Transient
	private String msg_mvp_custom_empty; // 目前無任何自訂單據
	@Transient
	private String tip_mvp_custom_remove; // 移除此筆單據
	@Transient
	private String help_mvp_bom_input; // (輸入關鍵字搜尋或直接貼上BOM號按Enter)
	@Transient
	private String label_delay_days; // 延遲(天)
	@Transient
	private String label_bom_count; // BOM數量
	@Transient
	private String val_repl_history; // 備案
	@Transient
	private String label_action; // 操作
	@Transient
	private String label_mvp_ext_project; // 外部專案
	@Transient
	private String label_mvp_ext_conflict; // 衝突料號
	@Transient
	private String val_mvp_no_conflict; // 無衝突

	// --- Tabulator Columns ---
	@Transient
	private String col_item_type; // 屬性
	@Transient
	private String col_item_category; // 分類
	@Transient
	private String col_item_kind; // 類型
	@Transient
	private String col_item_spec; // 規格
	@Transient
	private String col_virtual_demand; // 預計領用量
	@Transient
	private String col_current_stock; // 現有庫存
	@Transient
	private String col_max_sets_physical; // 現貨套數
	@Transient
	private String col_max_sets_safe; // 安全套數
	@Transient
	private String col_balance_before; // 可動用餘額
	@Transient
	private String col_ext_demand_qty; // 外部擠壓
	@Transient
	private String col_replacement_supply; // 系統替代
	@Transient
	private String col_priority; // 優先權
	@Transient
	private String col_virtual_date; // 預交日

	// --- Column Values & Tooltips ---
	@Transient
	private String val_main_mat; // 主料
	@Transient
	private String val_sub_mat; // 副料
	@Transient
	private String val_part; // 零件
	@Transient
	private String val_assembly; // 組件

	@Transient
	private String btn_view_details; // 查看詳情
	@Transient
	private String log_mvp_cache_success; // MVP Caching: 成功下載 {0} 筆 BOM, {1} 筆 Items, {2} 筆 倉別.
	@Transient
	private String log_mvp_dict_parse_error; // 字典檔解析失敗
	@Transient
	private String log_mvp_dict_download_error; // 字典檔下載失敗
	@Transient
	private String title_ext_demand_detail; // 【競爭來源明細】
	@Transient
	private String btn_mvp_select_scheme; // 挑選方案
	@Transient
	private String val_mvp_has_repl; // 方案可用
	@Transient
	private String label_mvp_category_tag; // 分類標籤
	@Transient
	private String btn_remove_from_list; // 從清單移除
	@Transient
	private String msg_not_analyzed; // 尚未執行分析
	@Transient
	private String tip_click_to_edit; // 點擊編輯

	// =========================================================================
	// === Getters and Setters ===
	// =========================================================================

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMod_material_shortage() {
		return mod_material_shortage;
	}

	public void setMod_material_shortage(String s) {
		this.mod_material_shortage = s;
	}

	public String getMod_material_eol() {
		return mod_material_eol;
	}

	public void setMod_material_eol(String s) {
		this.mod_material_eol = s;
	}

	public String getMod_material_replacement() {
		return mod_material_replacement;
	}

	public void setMod_material_replacement(String s) {
		this.mod_material_replacement = s;
	}

	public String getMod_material_virtual_project() {
		return mod_material_virtual_project;
	}

	public void setMod_material_virtual_project(String s) {
		this.mod_material_virtual_project = s;
	}

	public String getBtn_close() {
		return btn_close;
	}

	public void setBtn_close(String s) {
		this.btn_close = s;
	}

	public String getBtn_reset() {
		return btn_reset;
	}

	public void setBtn_reset(String s) {
		this.btn_reset = s;
	}

	public String getBtn_save() {
		return btn_save;
	}

	public void setBtn_save(String s) {
		this.btn_save = s;
	}

	public String getBtn_export() {
		return btn_export;
	}

	public void setBtn_export(String s) {
		this.btn_export = s;
	}

	public String getBtn_confirm() {
		return btn_confirm;
	}

	public void setBtn_confirm(String s) {
		this.btn_confirm = s;
	}

	public String getBtn_delete() {
		return btn_delete;
	}

	public void setBtn_delete(String s) {
		this.btn_delete = s;
	}

	public String getBtn_cancel() {
		return btn_cancel;
	}

	public void setBtn_cancel(String s) {
		this.btn_cancel = s;
	}

	public String getMsg_loading() {
		return msg_loading;
	}

	public void setMsg_loading(String s) {
		this.msg_loading = s;
	}

	public String getMsg_no_data() {
		return msg_no_data;
	}

	public void setMsg_no_data(String s) {
		this.msg_no_data = s;
	}

	public String getTitle_alt_selection() {
		return title_alt_selection;
	}

	public void setTitle_alt_selection(String s) {
		this.title_alt_selection = s;
	}

	public String getTitle_alt_history() {
		return title_alt_history;
	}

	public void setTitle_alt_history(String s) {
		this.title_alt_history = s;
	}

	public String getTitle_recalculate() {
		return title_recalculate;
	}

	public void setTitle_recalculate(String s) {
		this.title_recalculate = s;
	}

	public String getTitle_committed_list() {
		return title_committed_list;
	}

	public void setTitle_committed_list(String s) {
		this.title_committed_list = s;
	}

	public String getTitle_click_exec() {
		return title_click_exec;
	}

	public void setTitle_click_exec(String s) {
		this.title_click_exec = s;
	}

	public String getTitle_view_executed() {
		return title_view_executed;
	}

	public void setTitle_view_executed(String s) {
		this.title_view_executed = s;
	}

	public String getTitle_delete_record() {
		return title_delete_record;
	}

	public void setTitle_delete_record(String s) {
		this.title_delete_record = s;
	}

	public String getLabel_for_part_no() {
		return label_for_part_no;
	}

	public void setLabel_for_part_no(String s) {
		this.label_for_part_no = s;
	}

	public String getLabel_include_transit() {
		return label_include_transit;
	}

	public void setLabel_include_transit(String s) {
		this.label_include_transit = s;
	}

	public String getLabel_shortage_qty() {
		return label_shortage_qty;
	}

	public void setLabel_shortage_qty(String s) {
		this.label_shortage_qty = s;
	}

	public String getLabel_exec_qty() {
		return label_exec_qty;
	}

	public void setLabel_exec_qty(String s) {
		this.label_exec_qty = s;
	}

	public String getLabel_select_warehouses() {
		return label_select_warehouses;
	}

	public void setLabel_select_warehouses(String s) {
		this.label_select_warehouses = s;
	}

	public String getLabel_select_order_types() {
		return label_select_order_types;
	}

	public void setLabel_select_order_types(String s) {
		this.label_select_order_types = s;
	}

	public String getLabel_filter_part_no() {
		return label_filter_part_no;
	}

	public void setLabel_filter_part_no(String s) {
		this.label_filter_part_no = s;
	}

	public String getLabel_required() {
		return label_required;
	}

	public void setLabel_required(String s) {
		this.label_required = s;
	}

	public String getLabel_limit_product() {
		return label_limit_product;
	}

	public void setLabel_limit_product(String s) {
		this.label_limit_product = s;
	}

	public String getLabel_limit_customer() {
		return label_limit_customer;
	}

	public void setLabel_limit_customer(String s) {
		this.label_limit_customer = s;
	}

	public String getSimadvice() {
		return simadvice;
	}

	public void setSimadvice(String simadvice) {
		this.simadvice = simadvice;
	}

	public String getLabel_universal() {
		return label_universal;
	}

	public void setLabel_universal(String s) {
		this.label_universal = s;
	}

	public String getLabel_consume() {
		return label_consume;
	}

	public void setLabel_consume(String s) {
		this.label_consume = s;
	}

	public String getLabel_link() {
		return label_link;
	}

	public void setLabel_link(String s) {
		this.label_link = s;
	}

	public String getBtn_fill_max() {
		return btn_fill_max;
	}

	public void setBtn_fill_max(String s) {
		this.btn_fill_max = s;
	}

	public String getBtn_confirm_exec() {
		return btn_confirm_exec;
	}

	public void setBtn_confirm_exec(String s) {
		this.btn_confirm_exec = s;
	}

	public String getBtn_search_exec() {
		return btn_search_exec;
	}

	public void setBtn_search_exec(String s) {
		this.btn_search_exec = s;
	}

	public String getBtn_recalculate() {
		return btn_recalculate;
	}

	public void setBtn_recalculate(String s) {
		this.btn_recalculate = s;
	}

	public String getBtn_alt_list() {
		return btn_alt_list;
	}

	public void setBtn_alt_list(String s) {
		this.btn_alt_list = s;
	}

	public String getBtn_modify() {
		return btn_modify;
	}

	public void setBtn_modify(String s) {
		this.btn_modify = s;
	}

	public String getBtn_execute() {
		return btn_execute;
	}

	public void setBtn_execute(String s) {
		this.btn_execute = s;
	}

	public String getMsg_no_alt_rule() {
		return msg_no_alt_rule;
	}

	public void setMsg_no_alt_rule(String s) {
		this.msg_no_alt_rule = s;
	}

	public String getMsg_alt_rule_scope_mismatch() {
		return msg_alt_rule_scope_mismatch;
	}

	public void setMsg_alt_rule_scope_mismatch(String s) {
		this.msg_alt_rule_scope_mismatch = s;
	}

	public String getMsg_stock_insufficient() {
		return msg_stock_insufficient;
	}

	public void setMsg_stock_insufficient(String s) {
		this.msg_stock_insufficient = s;
	}

	public String getMsg_querying_stock() {
		return msg_querying_stock;
	}

	public void setMsg_querying_stock(String s) {
		this.msg_querying_stock = s;
	}

	public String getMsg_balance_insufficient() {
		return msg_balance_insufficient;
	}

	public void setMsg_balance_insufficient(String s) {
		this.msg_balance_insufficient = s;
	}

	public String getMsg_stock_insufficient_detail() {
		return msg_stock_insufficient_detail;
	}

	public void setMsg_stock_insufficient_detail(String s) {
		this.msg_stock_insufficient_detail = s;
	}

	public String getMsg_confirm_delete() {
		return msg_confirm_delete;
	}

	public void setMsg_confirm_delete(String s) {
		this.msg_confirm_delete = s;
	}

	public String getMsg_running_mrp() {
		return msg_running_mrp;
	}

	public void setMsg_running_mrp(String s) {
		this.msg_running_mrp = s;
	}

	public String getMsg_finalizing() {
		return msg_finalizing;
	}

	public void setMsg_finalizing(String s) {
		this.msg_finalizing = s;
	}

	public String getMsg_data_fetched() {
		return msg_data_fetched;
	}

	public void setMsg_data_fetched(String s) {
		this.msg_data_fetched = s;
	}

	public String getMsg_no_history() {
		return msg_no_history;
	}

	public void setMsg_no_history(String s) {
		this.msg_no_history = s;
	}

	public String getMsg_invalid_qty() {
		return msg_invalid_qty;
	}

	public void setMsg_invalid_qty(String s) {
		this.msg_invalid_qty = s;
	}

	public String getMsg_recalc_success() {
		return msg_recalc_success;
	}

	public void setMsg_recalc_success(String s) {
		this.msg_recalc_success = s;
	}

	public String getMsg_missing_in_list() {
		return msg_missing_in_list;
	}

	public void setMsg_missing_in_list(String s) {
		this.msg_missing_in_list = s;
	}

	public String getMsg_select_warehouse_req() {
		return msg_select_warehouse_req;
	}

	public void setMsg_select_warehouse_req(String s) {
		this.msg_select_warehouse_req = s;
	}

	public String getMsg_select_doctype_req() {
		return msg_select_doctype_req;
	}

	public void setMsg_select_doctype_req(String s) {
		this.msg_select_doctype_req = s;
	}

	public String getVal_stock_sufficient() {
		return val_stock_sufficient;
	}

	public void setVal_stock_sufficient(String s) {
		this.val_stock_sufficient = s;
	}

	public String getVal_stock_shortage() {
		return val_stock_shortage;
	}

	public void setVal_stock_shortage(String s) {
		this.val_stock_shortage = s;
	}

	public String getVal_alt_qty() {
		return val_alt_qty;
	}

	public void setVal_alt_qty(String s) {
		this.val_alt_qty = s;
	}

	public String getVal_allocated() {
		return val_allocated;
	}

	public void setVal_allocated(String s) {
		this.val_allocated = s;
	}

	public String getVal_none() {
		return val_none;
	}

	public void setVal_none(String s) {
		this.val_none = s;
	}

	public String getPlaceholder_part_no() {
		return placeholder_part_no;
	}

	public void setPlaceholder_part_no(String s) {
		this.placeholder_part_no = s;
	}

	public String getPlaceholder_exclude_doc() {
		return placeholder_exclude_doc;
	}

	public void setPlaceholder_exclude_doc(String s) {
		this.placeholder_exclude_doc = s;
	}

	public String getPlaceholder_input_qty() {
		return placeholder_input_qty;
	}

	public void setPlaceholder_input_qty(String s) {
		this.placeholder_input_qty = s;
	}

	public String getHelp_recalculate() {
		return help_recalculate;
	}

	public void setHelp_recalculate(String s) {
		this.help_recalculate = s;
	}

	public String getFilter_doc_type() {
		return filter_doc_type;
	}

	public void setFilter_doc_type(String s) {
		this.filter_doc_type = s;
	}

	public String getFilter_all() {
		return filter_all;
	}

	public void setFilter_all(String s) {
		this.filter_all = s;
	}

	public String getFilter_has_rule() {
		return filter_has_rule;
	}

	public void setFilter_has_rule(String s) {
		this.filter_has_rule = s;
	}

	public String getFilter_no_rule() {
		return filter_no_rule;
	}

	public void setFilter_no_rule(String s) {
		this.filter_no_rule = s;
	}

	public String getOrder_type_m() {
		return order_type_m;
	}

	public void setOrder_type_m(String s) {
		this.order_type_m = s;
	}

	public String getOrder_type_m_ret() {
		return order_type_m_ret;
	}

	public void setOrder_type_m_ret(String s) {
		this.order_type_m_ret = s;
	}

	public String getOrder_type_im() {
		return order_type_im;
	}

	public void setOrder_type_im(String s) {
		this.order_type_im = s;
	}

	public String getOrder_type_p() {
		return order_type_p;
	}

	public void setOrder_type_p(String s) {
		this.order_type_p = s;
	}

	public String getOrder_type_ir() {
		return order_type_ir;
	}

	public void setOrder_type_ir(String s) {
		this.order_type_ir = s;
	}

	public String getOrder_type_pr() {
		return order_type_pr;
	}

	public void setOrder_type_pr(String s) {
		this.order_type_pr = s;
	}

	public String getOrder_type_co() {
		return order_type_co;
	}

	public void setOrder_type_co(String s) {
		this.order_type_co = s;
	}

	public String getTitle_criteria() {
		return title_criteria;
	}

	public void setTitle_criteria(String s) {
		this.title_criteria = s;
	}

	public String getCol_root_bom() {
		return col_root_bom;
	}

	public void setCol_root_bom(String s) {
		this.col_root_bom = s;
	}

	public String getCol_bom_level() {
		return col_bom_level;
	}

	public void setCol_bom_level(String s) {
		this.col_bom_level = s;
	}

	public String getCol_part_no() {
		return col_part_no;
	}

	public void setCol_part_no(String s) {
		this.col_part_no = s;
	}

	public String getCol_part_name() {
		return col_part_name;
	}

	public void setCol_part_name(String s) {
		this.col_part_name = s;
	}

	public String getCol_qty_per_set() {
		return col_qty_per_set;
	}

	public void setCol_qty_per_set(String s) {
		this.col_qty_per_set = s;
	}

	public String getCol_wh_stock() {
		return col_wh_stock;
	}

	public void setCol_wh_stock(String s) {
		this.col_wh_stock = s;
	}

	public String getCol_available_sets() {
		return col_available_sets;
	}

	public void setCol_available_sets(String s) {
		this.col_available_sets = s;
	}

	public String getCol_support_months() {
		return col_support_months;
	}

	public void setCol_support_months(String s) {
		this.col_support_months = s;
	}

	public String getLabel_bom_list() {
		return label_bom_list;
	}

	public void setLabel_bom_list(String s) {
		this.label_bom_list = s;
	}

	public String getLabel_max_level() {
		return label_max_level;
	}

	public void setLabel_max_level(String s) {
		this.label_max_level = s;
	}

	public String getLabel_monthly_demand() {
		return label_monthly_demand;
	}

	public void setLabel_monthly_demand(String s) {
		this.label_monthly_demand = s;
	}

	public String getLabel_warehouse() {
		return label_warehouse;
	}

	public void setLabel_warehouse(String s) {
		this.label_warehouse = s;
	}

	public String getBtn_calculate() {
		return btn_calculate;
	}

	public void setBtn_calculate(String s) {
		this.btn_calculate = s;
	}

	public String getTitle_substitution_rules() {
		return title_substitution_rules;
	}

	public void setTitle_substitution_rules(String s) {
		this.title_substitution_rules = s;
	}

	public String getMsg_policy_eq() {
		return msg_policy_eq;
	}

	public void setMsg_policy_eq(String s) {
		this.msg_policy_eq = s;
	}

	public String getMsg_policy_rd() {
		return msg_policy_rd;
	}

	public void setMsg_policy_rd(String s) {
		this.msg_policy_rd = s;
	}

	public String getMsg_source() {
		return msg_source;
	}

	public void setMsg_source(String s) {
		this.msg_source = s;
	}

	public String getMsg_target() {
		return msg_target;
	}

	public void setMsg_target(String s) {
		this.msg_target = s;
	}

	public String getScope_0() {
		return scope_0;
	}

	public void setScope_0(String s) {
		this.scope_0 = s;
	}

	public String getScope_1() {
		return scope_1;
	}

	public void setScope_1(String s) {
		this.scope_1 = s;
	}

	public String getScope_2() {
		return scope_2;
	}

	public void setScope_2(String s) {
		this.scope_2 = s;
	}

	public String getMsg_sh_customer() {
		return msg_sh_customer;
	}

	public void setMsg_sh_customer(String s) {
		this.msg_sh_customer = s;
	}

	public String getMsg_sh_product() {
		return msg_sh_product;
	}

	public void setMsg_sh_product(String s) {
		this.msg_sh_product = s;
	}

	public String getBtn_create() {
		return btn_create;
	}

	public void setBtn_create(String s) {
		this.btn_create = s;
	}

	public String getTab_other_project_settings() {
		return tab_other_project_settings;
	}

	public void setTab_other_project_settings(String s) {
		this.tab_other_project_settings = s;
	}

	public String getPlaceholder_search_project() {
		return placeholder_search_project;
	}

	public void setPlaceholder_search_project(String s) {
		this.placeholder_search_project = s;
	}

	public String getPlaceholder_mvp_memo() {
		return placeholder_mvp_memo;
	}

	public void setPlaceholder_mvp_memo(String s) {
		this.placeholder_mvp_memo = s;
	}

	public String getHelp_mvp_bom() {
		return help_mvp_bom;
	}

	public void setHelp_mvp_bom(String s) {
		this.help_mvp_bom = s;
	}

	public String getPlaceholder_mvp_bom() {
		return placeholder_mvp_bom;
	}

	public void setPlaceholder_mvp_bom(String s) {
		this.placeholder_mvp_bom = s;
	}

	public String getLabel_mvp_exclude() {
		return label_mvp_exclude;
	}

	public void setLabel_mvp_exclude(String s) {
		this.label_mvp_exclude = s;
	}

	public String getPlaceholder_mvp_exclude() {
		return placeholder_mvp_exclude;
	}

	public void setPlaceholder_mvp_exclude(String s) {
		this.placeholder_mvp_exclude = s;
	}

	public String getLabel_mvp_warehouses() {
		return label_mvp_warehouses;
	}

	public void setLabel_mvp_warehouses(String s) {
		this.label_mvp_warehouses = s;
	}

	public String getVal_mvp_all_warehouses() {
		return val_mvp_all_warehouses;
	}

	public void setVal_mvp_all_warehouses(String s) {
		this.val_mvp_all_warehouses = s;
	}

	public String getBtn_mvp_apply_close() {
		return btn_mvp_apply_close;
	}

	public void setBtn_mvp_apply_close(String s) {
		this.btn_mvp_apply_close = s;
	}

	public String getLabel_mvp_include() {
		return label_mvp_include;
	}

	public void setLabel_mvp_include(String s) {
		this.label_mvp_include = s;
	}

	public String getHelp_mvp_include() {
		return help_mvp_include;
	}

	public void setHelp_mvp_include(String s) {
		this.help_mvp_include = s;
	}

	public String getPlaceholder_mvp_include() {
		return placeholder_mvp_include;
	}

	public void setPlaceholder_mvp_include(String s) {
		this.placeholder_mvp_include = s;
	}

	public String getBtn_mvp_other_projects() {
		return btn_mvp_other_projects;
	}

	public void setBtn_mvp_other_projects(String s) {
		this.btn_mvp_other_projects = s;
	}

	public String getHelp_mvp_sim_info() {
		return help_mvp_sim_info;
	}

	public void setHelp_mvp_sim_info(String s) {
		this.help_mvp_sim_info = s;
	}

	public String getPlaceholder_mvp_global_search() {
		return placeholder_mvp_global_search;
	}

	public void setPlaceholder_mvp_global_search(String s) {
		this.placeholder_mvp_global_search = s;
	}

	public String getBtn_mvp_apply_category() {
		return btn_mvp_apply_category;
	}

	public void setBtn_mvp_apply_category(String s) {
		this.btn_mvp_apply_category = s;
	}

	public String getBtn_mvp_move_to_sub() {
		return btn_mvp_move_to_sub;
	}

	public void setBtn_mvp_move_to_sub(String s) {
		this.btn_mvp_move_to_sub = s;
	}

	public String getBtn_mvp_move_to_main() {
		return btn_mvp_move_to_main;
	}

	public void setBtn_mvp_move_to_main(String s) {
		this.btn_mvp_move_to_main = s;
	}

	public String getTitle_mvp_report() {
		return title_mvp_report;
	}

	public void setTitle_mvp_report(String s) {
		this.title_mvp_report = s;
	}

	public String getLabel_mvp_bottlenecks() {
		return label_mvp_bottlenecks;
	}

	public void setLabel_mvp_bottlenecks(String s) {
		this.label_mvp_bottlenecks = s;
	}

	public String getTitle_mvp_detail() {
		return title_mvp_detail;
	}

	public void setTitle_mvp_detail(String s) {
		this.title_mvp_detail = s;
	}

	public String getBtn_mvp_tree_mode() {
		return btn_mvp_tree_mode;
	}

	public void setBtn_mvp_tree_mode(String s) {
		this.btn_mvp_tree_mode = s;
	}

	public String getBtn_mvp_flat_mode() {
		return btn_mvp_flat_mode;
	}

	public void setBtn_mvp_flat_mode(String s) {
		this.btn_mvp_flat_mode = s;
	}

	public String getPlaceholder_mvp_detail_search() {
		return placeholder_mvp_detail_search;
	}

	public void setPlaceholder_mvp_detail_search(String s) {
		this.placeholder_mvp_detail_search = s;
	}

	public String getTitle_mvp_replacement() {
		return title_mvp_replacement;
	}

	public void setTitle_mvp_replacement(String s) {
		this.title_mvp_replacement = s;
	}

	public String getTitle_mvp_custom_order() {
		return title_mvp_custom_order;
	}

	public void setTitle_mvp_custom_order(String s) {
		this.title_mvp_custom_order = s;
	}

	public String getMsg_mvp_dirty_confirm() {
		return msg_mvp_dirty_confirm;
	}

	public void setMsg_mvp_dirty_confirm(String s) {
		this.msg_mvp_dirty_confirm = s;
	}

	public String getMsg_mvp_delete_confirm() {
		return msg_mvp_delete_confirm;
	}

	public void setMsg_mvp_delete_confirm(String s) {
		this.msg_mvp_delete_confirm = s;
	}

	public String getTitle_mvp_selection() {
		return title_mvp_selection;
	}

	public void setTitle_mvp_selection(String s) {
		this.title_mvp_selection = s;
	}

	public String getVal_mvp_atp_pass() {
		return val_mvp_atp_pass;
	}

	public void setVal_mvp_atp_pass(String s) {
		this.val_mvp_atp_pass = s;
	}

	public String getVal_mvp_atp_warning() {
		return val_mvp_atp_warning;
	}

	public void setVal_mvp_atp_warning(String s) {
		this.val_mvp_atp_warning = s;
	}

	public String getVal_mvp_atp_error() {
		return val_mvp_atp_error;
	}

	public void setVal_mvp_atp_error(String s) {
		this.val_mvp_atp_error = s;
	}

	public String getVal_mvp_priority_1() {
		return val_mvp_priority_1;
	}

	public void setVal_mvp_priority_1(String s) {
		this.val_mvp_priority_1 = s;
	}

	public String getVal_mvp_priority_2() {
		return val_mvp_priority_2;
	}

	public void setVal_mvp_priority_2(String s) {
		this.val_mvp_priority_2 = s;
	}

	public String getVal_mvp_priority_3() {
		return val_mvp_priority_3;
	}

	public void setVal_mvp_priority_3(String s) {
		this.val_mvp_priority_3 = s;
	}

	public String getVal_mvp_reading() {
		return val_mvp_reading;
	}

	public void setVal_mvp_reading(String s) {
		this.val_mvp_reading = s;
	}

	public String getVal_mvp_found_count_short() {
		return val_mvp_found_count_short;
	}

	public void setVal_mvp_found_count_short(String s) {
		this.val_mvp_found_count_short = s;
	}

	public String getAlert_mvp_export_no_data() {
		return alert_mvp_export_no_data;
	}

	public void setAlert_mvp_export_no_data(String s) {
		this.alert_mvp_export_no_data = s;
	}

	public String getBtn_mvp_restore_window() {
		return btn_mvp_restore_window;
	}

	public void setBtn_mvp_restore_window(String s) {
		this.btn_mvp_restore_window = s;
	}

	public String getAlert_mvp_need_select() {
		return alert_mvp_need_select;
	}

	public void setAlert_mvp_need_select(String s) {
		this.alert_mvp_need_select = s;
	}

	public String getAlert_mvp_need_qty_date() {
		return alert_mvp_need_qty_date;
	}

	public void setAlert_mvp_need_qty_date(String s) {
		this.alert_mvp_need_qty_date = s;
	}

	public String getAlert_mvp_qty_nonzero() {
		return alert_mvp_qty_nonzero;
	}

	public void setAlert_mvp_qty_nonzero(String s) {
		this.alert_mvp_qty_nonzero = s;
	}

	public String getVal_mvp_has_repl_tip() {
		return val_mvp_has_repl_tip;
	}

	public void setVal_mvp_has_repl_tip(String s) {
		this.val_mvp_has_repl_tip = s;
	}

	public String getAlert_mvp_need_material() {
		return alert_mvp_need_material;
	}

	public void setAlert_mvp_need_material(String s) {
		this.alert_mvp_need_material = s;
	}

	public String getVal_mvp_analyzing() {
		return val_mvp_analyzing;
	}

	public void setVal_mvp_analyzing(String s) {
		this.val_mvp_analyzing = s;
	}

	public String getVal_mvp_simulating() {
		return val_mvp_simulating;
	}

	public void setVal_mvp_simulating(String s) {
		this.val_mvp_simulating = s;
	}

	public String getLabel_mvp_finish_date() {
		return label_mvp_finish_date;
	}

	public void setLabel_mvp_finish_date(String s) {
		this.label_mvp_finish_date = s;
	}

	public String getBtn_run_sim() {
		return btn_run_sim;
	}

	public void setBtn_run_sim(String s) {
		this.btn_run_sim = s;
	}

	public String getBtn_full_screen() {
		return btn_full_screen;
	}

	public void setBtn_full_screen(String s) {
		this.btn_full_screen = s;
	}

	public String getBtn_select_all() {
		return btn_select_all;
	}

	public void setBtn_select_all(String s) {
		this.btn_select_all = s;
	}

	public String getBtn_select_none() {
		return btn_select_none;
	}

	public void setBtn_select_none(String s) {
		this.btn_select_none = s;
	}

	public String getBtn_search() {
		return btn_search;
	}

	public void setBtn_search(String s) {
		this.btn_search = s;
	}

	public String getBtn_analyze() {
		return btn_analyze;
	}

	public void setBtn_analyze(String s) {
		this.btn_analyze = s;
	}

	public String getLabel_mvp_target_sets() {
		return label_mvp_target_sets;
	}

	public void setLabel_mvp_target_sets(String s) {
		this.label_mvp_target_sets = s;
	}

	public String getLabel_mvp_max_sets() {
		return label_mvp_max_sets;
	}

	public void setLabel_mvp_max_sets(String s) {
		this.label_mvp_max_sets = s;
	}

	public String getLabel_target_qty() {
		return label_target_qty;
	}

	public void setLabel_target_qty(String s) {
		this.label_target_qty = s;
	}

	public String getLabel_sel_leaf() {
		return label_sel_leaf;
	}

	public void setLabel_sel_leaf(String s) {
		this.label_sel_leaf = s;
	}

	public String getLabel_sel_full() {
		return label_sel_full;
	}

	public void setLabel_sel_full(String s) {
		this.label_sel_full = s;
	}

	public String getLabel_sel_clear() {
		return label_sel_clear;
	}

	public void setLabel_sel_clear(String s) {
		this.label_sel_clear = s;
	}

	public String getLabel_delay_days() {
		return label_delay_days;
	}

	public void setLabel_delay_days(String s) {
		this.label_delay_days = s;
	}

	public String getLabel_bom_count() {
		return label_bom_count;
	}

	public void setLabel_bom_count(String s) {
		this.label_bom_count = s;
	}

	public String getVal_repl_history() {
		return val_repl_history;
	}

	public String getBtn_mvp_select_scheme() {
		return btn_mvp_select_scheme;
	}

	public void setBtn_mvp_select_scheme(String s) {
		this.btn_mvp_select_scheme = s;
	}

	public String getVal_mvp_has_repl() {
		return val_mvp_has_repl;
	}

	public void setVal_mvp_has_repl(String s) {
		this.val_mvp_has_repl = s;
	}

	public String getLabel_mvp_category_tag() {
		return label_mvp_category_tag;
	}

	public void setLabel_mvp_category_tag(String s) {
		this.label_mvp_category_tag = s;
	}

	public String getBtn_remove_from_list() {
		return btn_remove_from_list;
	}

	public void setBtn_remove_from_list(String s) {
		this.btn_remove_from_list = s;
	}

	public String getMsg_not_analyzed() {
		return msg_not_analyzed;
	}

	public void setMsg_not_analyzed(String s) {
		this.msg_not_analyzed = s;
	}

	public String getTip_click_to_edit() {
		return tip_click_to_edit;
	}

	public void setTip_click_to_edit(String s) {
		this.tip_click_to_edit = s;
	}

	public String getLabel_action() {
		return label_action;
	}

	public void setLabel_action(String s) {
		this.label_action = s;
	}

	public String getMsg_processing() {
		return msg_processing;
	}

	public void setMsg_processing(String s) {
		this.msg_processing = s;
	}

	public String getMsg_downloading() {
		return msg_downloading;
	}

	public void setMsg_downloading(String s) {
		this.msg_downloading = s;
	}

	public String getAlert_leave_dirty() {
		return alert_leave_dirty;
	}

	public void setAlert_leave_dirty(String s) {
		this.alert_leave_dirty = s;
	}

	public String getLabel_not_saved() {
		return label_not_saved;
	}

	public void setLabel_not_saved(String s) {
		this.label_not_saved = s;
	}

	public String getLabel_mvp_custom_add_title() {
		return label_mvp_custom_add_title;
	}

	public void setLabel_mvp_custom_add_title(String s) {
		this.label_mvp_custom_add_title = s;
	}

	public String getLabel_mvp_custom_qty_label() {
		return label_mvp_custom_qty_label;
	}

	public void setLabel_mvp_custom_qty_label(String s) {
		this.label_mvp_custom_qty_label = s;
	}

	public String getLabel_mvp_custom_date_label() {
		return label_mvp_custom_date_label;
	}

	public void setLabel_mvp_custom_date_label(String s) {
		this.label_mvp_custom_date_label = s;
	}

	public String getLabel_mvp_custom_list_title() {
		return label_mvp_custom_list_title;
	}

	public void setLabel_mvp_custom_list_title(String s) {
		this.label_mvp_custom_list_title = s;
	}

	public String getLabel_mvp_custom_list_th1() {
		return label_mvp_custom_list_th1;
	}

	public void setLabel_mvp_custom_list_th1(String s) {
		this.label_mvp_custom_list_th1 = s;
	}

	public String getLabel_mvp_custom_list_th2() {
		return label_mvp_custom_list_th2;
	}

	public void setLabel_mvp_custom_list_th2(String s) {
		this.label_mvp_custom_list_th2 = s;
	}

	public String getMsg_mvp_custom_empty() {
		return msg_mvp_custom_empty;
	}

	public void setMsg_mvp_custom_empty(String s) {
		this.msg_mvp_custom_empty = s;
	}

	public String getTip_mvp_custom_remove() {
		return tip_mvp_custom_remove;
	}

	public void setTip_mvp_custom_remove(String s) {
		this.tip_mvp_custom_remove = s;
	}

	public String getHelp_mvp_selection() {
		return help_mvp_selection;
	}

	public void setHelp_mvp_selection(String s) {
		this.help_mvp_selection = s;
	}

	public String getLabel_mvp_ext_project() {
		return label_mvp_ext_project;
	}

	public void setLabel_mvp_ext_project(String s) {
		this.label_mvp_ext_project = s;
	}

	public String getLabel_mvp_ext_conflict() {
		return label_mvp_ext_conflict;
	}

	public void setLabel_mvp_ext_conflict(String s) {
		this.label_mvp_ext_conflict = s;
	}

	public String getVal_mvp_no_conflict() {
		return val_mvp_no_conflict;
	}

	public void setVal_mvp_no_conflict(String s) {
		this.val_mvp_no_conflict = s;
	}

	public String getLabel_mvp_demand_total() {
		return label_mvp_demand_total;
	}

	public void setLabel_mvp_demand_total(String label_mvp_demand_total) {
		this.label_mvp_demand_total = label_mvp_demand_total;
	}

	public String getCol_priority() {
		return col_priority;
	}

	public void setCol_priority(String s) {
		this.col_priority = s;
	}

	public String getCol_virtual_date() {
		return col_virtual_date;
	}

	public void setCol_virtual_date(String s) {
		this.col_virtual_date = s;
	}

	public String getLabel_mvp_rule() {
		return label_mvp_rule;
	}

	public void setLabel_mvp_rule(String label_mvp_rule) {
		this.label_mvp_rule = label_mvp_rule;
	}

	public String getHelp_mvp_replacement() {
		return help_mvp_replacement;
	}

	public void setHelp_mvp_replacement(String help_mvp_replacement) {
		this.help_mvp_replacement = help_mvp_replacement;
	}
}
