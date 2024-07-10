package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.BomItemSpecifications;

public interface BomItemSpecificationsDao extends JpaRepository<BomItemSpecifications, Long> {

	// 查詢全部
	ArrayList<BomItemSpecifications> findAll();

	// 查詢全部含-頁數
	@Query("SELECT c FROM BomItemSpecifications c "//
			+ "WHERE (:bisgname is null or c.bisgname LIKE %:bisgname% ) and "//
			+ "(:bisname is null or c.bisname LIKE %:bisname% ) and "//
			+ "(:bisnb is null or c.bisnb LIKE %:bisnb% ) ") //
	ArrayList<BomItemSpecifications> findAllBySearch(String bisgname, String bisname, String bisnb, Pageable pageable);

	// 檢查
	@Query("SELECT c FROM BomItemSpecifications c "//
			+ "WHERE (:bisgname is null or c.bisgname =:bisgname ) and "//
			+ "(:bisname is null or c.bisname = :bisname ) and "//
			+ "(:bisnb is null or c.bisnb =:bisnb ) ") //
	ArrayList<BomItemSpecifications> findAllByCheck(String bisgname, String bisname, String bisnb);

	// 取得G_ID
	@Query(value = "SELECT NEXTVAL('bom_item_specifications_g_seq')", nativeQuery = true)
	Long getBomItemSpecificationsGroupGSeq();

	// 多筆查詢範例
//	@Query(" SELECT i.suname FROM BomKeeper i WHERE "//
//			+ "(coalesce(:accounts, null) is null or i.suaccount IN :accounts ) ") // coalesce 回傳非NULL值
//	ArrayList<String> readAccounts(List<String> accounts);

}