package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.BomKeeper;

public interface BomKeeperDao extends JpaRepository<BomKeeper, Long> {

	// 帳號查詢
	BomKeeper findByBkmodel(String bkmodel);

	// 查詢全部
	ArrayList<BomKeeper> findAll();

	// 查詢全部含-頁數
	@Query("SELECT c FROM BomKeeper c "//
			+ "WHERE (:bksuacc is null or c.bksuacc LIKE %:bksuacc% ) and "//
			+ "(:bknb is null or c.bknb LIKE %:bknb% ) and "//
			+ "(:bkmodel is null or c.bkmodel LIKE %:bkmodel% )") // (4)不過濾
	ArrayList<BomKeeper> findAllBySearch(String bksuacc, String bknb, String bkmodel, Pageable pageable);

	// 檢查
	@Query("SELECT c FROM BomKeeper c "//
			+ "WHERE (:bksuid  is null or c.bksuid  =:bksuid  ) and "//
			+ "(:bknb is null or c.bknb = :bknb ) and "//
			+ "(:bkmodel is null or c.bkmodel =:bkmodel ) ") // (4)不過濾
	ArrayList<BomKeeper> findAllByCheck(Long bksuid, String bknb, String bkmodel);

	// 多筆查詢範例
//	@Query(" SELECT i.suname FROM BomKeeper i WHERE "//
//			+ "(coalesce(:accounts, null) is null or i.suaccount IN :accounts ) ") // coalesce 回傳非NULL值
//	ArrayList<String> readAccounts(List<String> accounts);

}