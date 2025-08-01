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
			SELECT
			    merged.*
			FROM (
			    -- 第一階
			    SELECT
			        b1.bbi_id,
			       		b1.bbi_sn,
			            b1.bbi_nb,
			            b1.bbi_sn_nb,
			            b1.bbi_name,
			            b1.bbi_specification,
			            b1.bbi_description,
			            b1.bbi_i_sn,
			            b1.bbi_i_name,
			            b1.bbi_i_specification,
			            b1.bbi_i_description,
			            b1.bbi_i_process,
			            b1.bbi_i_qty,
			            b1.bbi_i_s_erp,
			            b1.sys_c_date,
			            b1.sys_c_user,
			            b1.sys_m_date,
			            b1.sys_m_user,
			            b1.sys_o_date,
			            b1.sys_o_user,
			            b1.sys_header,
			            b1.sys_status,
			            b1.sys_sort,
			            b1.sys_note,
			            b1.check_sum
			    FROM basic_bom_ingredients b1
			    WHERE (:bbisn IS NULL or b1.bbi_sn LIKE CONCAT(:bbisn, '%'))
			    	AND (:bbiname IS NULL or b1.bbi_name LIKE CONCAT(:bbiname, '%'))
			    UNION ALL
			    -- 第二階（明確列欄位數與順序）
			    SELECT
						b2.bbi_id,
			       		b1.bbi_sn,         -- ⬅️ 覆蓋這一欄
			           	(b2.bbi_sn ||'->'||b2.bbi_nb )AS bbi_nb,
			            b2.bbi_sn_nb,
			            b2.bbi_name,
			            b2.bbi_specification,
			            b2.bbi_description,
			            b2.bbi_i_sn,
			            b2.bbi_i_name,
			            b2.bbi_i_specification,
			            b2.bbi_i_description,
			            b2.bbi_i_process,
			            b2.bbi_i_qty,
			            b2.bbi_i_s_erp,
			            b2.sys_c_date,
			            b2.sys_c_user,
			            b2.sys_m_date,
			            b2.sys_m_user,
			            b2.sys_o_date,
			            b2.sys_o_user,
			            b2.sys_header,
			            b2.sys_status,
			            b2.sys_sort,
			            b2.sys_note,
			            b2.check_sum
			    FROM basic_bom_ingredients b1
			    JOIN basic_bom_ingredients b2 ON b1.bbi_i_sn = b2.bbi_sn
			    WHERE (:bbisn IS NULL or b1.bbi_sn LIKE CONCAT(:bbisn, '%'))
			    	AND (:bbiname IS NULL or b1.bbi_name LIKE CONCAT(:bbiname, '%'))
			      	AND (b2.bbi_sn LIKE '92-%' OR b2.bbi_sn LIKE '81-%')
				  	AND (b2.bbi_i_sn NOT LIKE 'ECN')
			) AS merged
			ORDER BY bbi_sn ASC, bbi_nb ASC
			LIMIT 50000;""", nativeQuery = true)
	ArrayList<BasicBomIngredients> findFlattenedBomLevel2ByBbisn(@Param("bbisn") String bbisn,
			@Param("bbiname") String bbiname);

	// 檢查用
	@Query("SELECT c FROM BasicBomIngredients c WHERE "//
			+ "(:bbisn is null or c.bbisn=:bbisn) and "//
			+ "(:bbiname is null or c.bbiname=:bbiname) and "//
			+ "(:bbiisn is null or c.bbiisn=:bbiisn) and "//
			+ "(:bbiiname is null or c.bbiiname=:bbiiname) ")
	ArrayList<BasicBomIngredients> findAllByCheck(String bbisn, String bbiname, String bbiisn, String bbiiname);

}