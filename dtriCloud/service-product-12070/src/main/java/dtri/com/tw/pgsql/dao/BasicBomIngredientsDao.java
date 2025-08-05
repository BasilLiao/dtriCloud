package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dtri.com.tw.pgsql.entity.BasicBomIngredients;

public interface BasicBomIngredientsDao extends JpaRepository<BasicBomIngredients, Long> {

	@Query("SELECT c FROM BasicBomIngredients c WHERE "//
			+ "(:bbisn is null or c.bbisn=:bbisn) and "//
			+ "(:bbiname is null or c.bbiname=:bbiname) and "//
			+ "(:bbiisn is null or c.bbiisn=:bbiisn) and "//
			+ "(:bbiiname is null or c.bbiiname=:bbiiname) and "//
			+ "(:checksum is null or c.checksum=:checksum) "//
			+ "order by c.bbisnnb asc")
	ArrayList<BasicBomIngredients> findAllByBomList(String bbisn, String bbiname, String bbiisn, String bbiiname,
			String checksum);

	// 查詢用
	@Query("SELECT c FROM BasicBomIngredients c WHERE "//
			+ "(:bbisn is null or c.bbisn LIKE :bbisn%) and "//
			+ "(:bbiname is null or c.bbiname LIKE %:bbiname%) and "//
			+ "(:bbiisn is null or c.bbiisn LIKE %:bbiisn%) and "//
			+ "(:bbiiname is null or c.bbiiname LIKE %:bbiiname%) and "//
			+ "(:bbiispecification is null or c.bbiispecification LIKE %:bbiispecification%) ")
	ArrayList<BasicBomIngredients> findAllBySearch(String bbisn, String bbiname, String bbiisn, String bbiiname,
			String bbiispecification, Pageable pageable);

	@Query(value = """
			-- 使用遞迴展開 BOM 結構，並針對子項描述包含「正規化」的項目做過濾
			WITH RECURSIVE bom_tree AS (

			    -- 🔹 第一層（初始階）: 從特定成品料號展開第一層 BOM
			    SELECT
			        b.*,                                          -- 取得該筆 BOM 所有欄位
			        1 AS level,                                   -- 記錄階層層數，第一層為 1
			        b.bbi_sn AS root_bbi_sn,                      -- 記錄此展開樹的起始成品料號（根）   
			         CAST(b.bbi_i_sn AS TEXT) AS current_bbi_path -- 初始化目前展開路徑為子項料號
			    FROM basic_bom_ingredients b
			    WHERE (:bbisn IS NULL OR b.bbi_sn LIKE CONCAT('%', :bbisn, '%'))             -- ✅ 限定從指定的成品料號開始展開
					AND (:bbiname IS NULL OR b.bbi_name LIKE  CONCAT('%', :bbiname, '%')) 
				
			    UNION ALL

			    -- 🔁 遞迴展開其他階層：以上一層的子項作為下一層的父項繼續展開
			    SELECT
			        b.*,                                          -- 同樣取出所有欄位
			        bt.level + 1 AS level,                        -- 階層加 1
			        bt.root_bbi_sn,                               -- 維持展開樹的根料號
			        (bt.current_bbi_path || ' → ' || b.bbi_i_sn)  -- 更新展開路徑：加上本層子項料號
			    FROM basic_bom_ingredients b
			    INNER JOIN bom_tree bt ON b.bbi_sn = bt.bbi_i_sn  -- 🔗 關鍵：將上一層的子項對應為本層的父項
			    WHERE bt.level < 5                               -- ✅ 限制展開最大階層深度為 10 層（避免無限遞迴）
			      AND position(b.bbi_i_sn in bt.current_bbi_path) = 0  -- ✅ 防止循環展開（例如 A → B → A）
			)

			-- 📄 最終輸出：只顯示子項描述中含「正規化」關鍵字的那些節點記錄
			SELECT
			    t.bbi_id,               -- 主鍵 ID
			    t.root_bbi_sn AS bbi_sn,               -- 成品料號（父項）
			    t.bbi_nb,               -- 成品批號
			    t.bbi_sn_nb,            -- 成品批次編號（唯一鍵）
			    t.bbi_name,             -- 成品名稱
			    t.bbi_specification,    -- 成品規格
			    t.bbi_description,      -- 成品描述
			    t.bbi_i_sn,             -- 零件料號（子項）
			    t.bbi_i_name,           -- 零件名稱
			    t.bbi_i_specification,  -- 零件規格
			    t.bbi_i_description,    -- 零件描述（🌟搜尋關鍵欄位）
			    t.bbi_i_process,        -- 零件製程
			    t.bbi_i_qty,            -- 零件用量
			    t.bbi_i_s_erp,          -- 來源 ERP 編號
			    t.sys_c_date, t.sys_c_user,   -- 建立時間與人員
			    t.sys_m_date, t.sys_m_user,   -- 修改時間與人員
			    t.sys_o_date, t.sys_o_user,   -- 最後操作時間與人員
			    t.sys_header,           -- 是否為表頭
			    t.sys_status,           -- 狀態
			    t.sys_sort,             -- 排序編號
			    t.sys_note,             -- 備註
			    t.check_sum            -- 校驗碼
			    --t.level,                -- 遞迴層級
			    --t.current_bbi_path      -- 展開路徑（顯示展開歷程）
			FROM bom_tree t
			WHERE t.bbi_i_description LIKE '%正規化%'  -- ✅ 只回傳描述中含「正規化」的節點
			OR t.level = 1 -- 保留這個篩選條件，以顯示所有第一層物料
			ORDER BY t.root_bbi_sn ASC, t.bbi_i_sn ASC  -- 排序顯示：根料號 → 階層 → 展開路徑
			""", nativeQuery = true)
	ArrayList<BasicBomIngredients> findFlattenedBomLevel(@Param("bbisn") String bbisn,
			@Param("bbiname") String bbiname);

	// 檢查用
	@Query("SELECT c FROM BasicBomIngredients c WHERE "//
			+ "(:bbisn is null or c.bbisn=:bbisn) and "//
			+ "(:bbiname is null or c.bbiname=:bbiname) and "//
			+ "(:bbiisn is null or c.bbiisn=:bbiisn) and "//
			+ "(:bbiiname is null or c.bbiiname=:bbiiname) ")
	ArrayList<BasicBomIngredients> findAllByCheck(String bbisn, String bbiname, String bbiisn, String bbiiname);

}