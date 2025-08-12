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

	// 查詢清單
	@Query(value = "SELECT c.* FROM basic_bom_ingredients c WHERE "//
			+ "(:bbisnnb is null or c.bbi_sn_nb = ANY(CAST(:bbisnnb as VARCHAR[])))" //
			+ "order by c.bbi_sn_nb asc", nativeQuery = true)
	ArrayList<BasicBomIngredients> findAllByBomLists(@Param("bbisnnb") String[] bbisnnb);

	// 查詢清單第一次跑
	@Query(value = "SELECT c.* FROM basic_bom_ingredients c " //
			+ "order by c.bbi_sn_nb asc", nativeQuery = true)
	ArrayList<BasicBomIngredients> findAllByBomListsFirst();

	// 查詢用
	@Query("SELECT c FROM BasicBomIngredients c WHERE "//
			+ "(:bbisn is null or c.bbisn LIKE %:bbisn%) and "//
			+ "(:bbiname is null or c.bbiname LIKE %:bbiname%) and "//
			+ "(:bbiisn is null or c.bbiisn LIKE %:bbiisn%) and "//
			+ "(:bbiiname is null or c.bbiiname LIKE %:bbiiname%) and "//
			+ "(:bbiispecification is null or c.bbiispecification LIKE %:bbiispecification%) ")
	ArrayList<BasicBomIngredients> findAllBySearch(String bbisn, String bbiname, String bbiisn, String bbiiname,
			String bbiispecification, Pageable pageable);

	// 展BOM用
	@Query(value = """
			    WITH RECURSIVE bom_up AS (
			        SELECT * FROM basic_bom_ingredients WHERE bbi_i_sn = :bbiisn
			        UNION ALL
			        SELECT b.*
			        FROM basic_bom_ingredients b
			        INNER JOIN bom_up t ON b.bbi_i_sn = t.bbi_sn
			    )
			    SELECT * FROM bom_up
			""", nativeQuery = true)
	ArrayList<BasicBomIngredients> findBomParents(String bbiisn);

	// 檢查用
	@Query("SELECT c FROM BasicBomIngredients c WHERE "//
			+ "(:bbisn is null or c.bbisn=:bbisn) and "//
			+ "(:bbiname is null or c.bbiname=:bbiname) and "//
			+ "(:bbiisn is null or c.bbiisn=:bbiisn) and "//
			+ "(:bbiiname is null or c.bbiiname=:bbiiname) ")
	ArrayList<BasicBomIngredients> findAllByCheck(String bbisn, String bbiname, String bbiisn, String bbiiname);

}