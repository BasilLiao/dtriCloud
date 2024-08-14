package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

	// 檢查用
	@Query("SELECT c FROM BasicBomIngredients c WHERE "//
			+ "(:bbisn is null or c.bbisn=:bbisn) and "//
			+ "(:bbiname is null or c.bbiname=:bbiname) and "//
			+ "(:bbiisn is null or c.bbiisn=:bbiisn) and "//
			+ "(:bbiiname is null or c.bbiiname=:bbiiname) ")
	ArrayList<BasicBomIngredients> findAllByCheck(String bbisn, String bbiname, String bbiisn, String bbiiname);

}