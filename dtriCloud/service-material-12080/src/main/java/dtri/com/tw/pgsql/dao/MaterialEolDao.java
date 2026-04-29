package dtri.com.tw.pgsql.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dtri.com.tw.pgsql.dto.MaterialEolDto;
import dtri.com.tw.pgsql.entity.BasicBomIngredients;

public interface MaterialEolDao extends JpaRepository<BasicBomIngredients, Long> {

    /**
     * 核心估算查詢 (PostgreSQL Recursive CTE)
     * 1. 遞迴展開 BOM，計算單套累計用量
     * 2. 左連 warehouse_inventory 取得特定倉別的總庫存
     * 3. 篩選檔名中包含「停」字的料號
     * 4. 計算可供應套數 (availableSets)
     */
    @Query(value = """
            WITH RECURSIVE BomTree AS (
                -- Anchor: 第一階子件
                SELECT
                    bbi_sn AS root_bom,
                    bbi_sn AS parent_bom,
                    bbi_i_sn AS part_no,
                    bbi_i_name AS part_name,
                    bbi_i_specification AS part_spec,
                    bbi_i_description AS part_desc,
                    bbi_i_qty AS qty_per_unit,
                    1 AS bom_level,
                    CAST(bbi_i_qty AS DOUBLE PRECISION) AS accumulated_qty
                FROM basic_bom_ingredients
                WHERE TRIM(bbi_sn) IN (:bomNos)
                  AND bbi_i_qty > 0

                UNION ALL

                -- Recursive: 展開子件的子件
                SELECT
                    parent.root_bom,
                    child.bbi_sn,
                    child.bbi_i_sn,
                    child.bbi_i_name,
                    child.bbi_i_specification,
                    child.bbi_i_description,
                    child.bbi_i_qty,
                    parent.bom_level + 1,
                    parent.accumulated_qty * CAST(child.bbi_i_qty AS DOUBLE PRECISION)
                FROM basic_bom_ingredients child
                INNER JOIN BomTree parent ON TRIM(child.bbi_sn) = TRIM(parent.part_no)
                WHERE child.bbi_i_qty > 0
                  AND parent.bom_level < :maxLevel
            )
            -- 篩選含「停」字的物料 + 加總庫存
            SELECT
                bt.root_bom AS rootbom,
                bt.part_no AS partno,
                bt.part_name AS partname,
                bt.part_spec AS partspec,
                bt.part_desc AS remark,
                bt.bom_level AS bomlevel,
                bt.accumulated_qty AS qtyperset,
                COALESCE(inv.total_stock, 0) AS warehousestock,
                CASE
                    WHEN bt.accumulated_qty > 0
                    THEN FLOOR(COALESCE(inv.total_stock, 0) / bt.accumulated_qty)
                    ELSE 0
                END AS availablesets
            FROM BomTree bt
            LEFT JOIN (
                SELECT wi_wm_p_nb AS item_no, SUM(wi_n_qty) AS total_stock
                FROM warehouse_inventory
                WHERE wi_n_qty > 0
                  AND wi_wa_alias IN (:warehouseList)
                GROUP BY wi_wm_p_nb
            ) inv ON TRIM(bt.part_no) = TRIM(inv.item_no)
            WHERE (bt.part_name LIKE '%停%'
                OR bt.part_spec LIKE '%停%'
                OR bt.part_desc LIKE '%停%')
            ORDER BY bt.bom_level, bt.part_no
            """, nativeQuery = true)
    List<MaterialEolDto> getEstimatedMaterials(
            @Param("bomNos") List<String> bomNos,
            @Param("maxLevel") int maxLevel,
            @Param("warehouseList") List<String> warehouseList);

    /**
     * 取得有庫存的不重複倉別 (供前端下拉選單/Checkboxes使用)
     * 傳回值 [0]: 倉別代號 (如 A0001), [1]: 倉別名稱 (如 成品倉)
     */
    @Query(value = "SELECT wi_wa_alias, MAX(COALESCE(wi_wa_alias_name, '')) " +
            "FROM warehouse_inventory " +
            "WHERE wi_n_qty > 0 " +
            "GROUP BY wi_wa_alias " +
            "ORDER BY wi_wa_alias ASC", nativeQuery = true)
    List<Object[]> getAvailableWarehouses();

}
