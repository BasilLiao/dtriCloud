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
			+ "WHERE (:bisgname is null or c.bisgname LIKE %:bisgname% ) and "// 正規化群組-名稱
			+ "(:bisfname is null or c.bisfname LIKE %:bisfname% ) and "// 正規化-名稱
			+ "(:bisnb is null or c.bisnb LIKE %:bisnb% ) ") // 物料號
	ArrayList<BomItemSpecifications> findAllBySearch(String bisgname, String bisfname, String bisnb, Pageable pageable);

	// 檢查
	@Query("SELECT c FROM BomItemSpecifications c "//
			+ "WHERE (:bisgname is null or c.bisgname =:bisgname ) and "//
			+ "(:bisfname is null or c.bisfname = :bisfname ) and "//
			+ "(:bisnb is null or c.bisnb =:bisnb ) ") //
	ArrayList<BomItemSpecifications> findAllByCheck(String bisgname, String bisfname, String bisnb);

	// 檢查-指定條件
	@Query("SELECT c FROM BomItemSpecifications c "//
			+ "WHERE (:bisgcondition is null or c.bisgcondition =:bisgcondition ) ") //
	ArrayList<BomItemSpecifications> findAllByBisGConditionCheck(String bisgcondition);

	// 找GID
	ArrayList<BomItemSpecifications> findAllByBisgid(Long bisgcondition);

	// 取得G_ID
	@Query(value = "SELECT NEXTVAL('bom_item_specifications_g_seq')", nativeQuery = true)
	Long getBomItemSpecificationsGroupGSeq();

	// 多筆查詢範例
//	@Query(" SELECT i.suname FROM BomKeeper i WHERE "//
//			+ "(coalesce(:accounts, null) is null or i.suaccount IN :accounts ) ") // coalesce 回傳非NULL值
//	ArrayList<String> readAccounts(List<String> accounts);

}