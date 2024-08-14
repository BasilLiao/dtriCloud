package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.BomProductRule;

public interface BomProductRuleDao extends JpaRepository<BomProductRule, Long> {

	// 查詢全部
	ArrayList<BomProductRule> findAll();

	// 查詢全部含-頁數
	@Query("SELECT c FROM BomProductRule c "//
			+ "WHERE (:bprname is null or c.bprname LIKE %:bprname% ) and "// BOM號
			+ "(:bprtypename is null or c.bprtypename LIKE %:bprtypename% )") // BOM型號
	ArrayList<BomProductRule> findAllBySearch(String bprname, String bprtypename, Pageable pageable);

	// 檢查
	@Query("SELECT c FROM BomProductRule c "//
			+ "WHERE (:bprname is null or c.bprname =:bprname ) and "//
			+ "(:bprtypename is null or c.bprtypename =:bprtypename ) ") //
	ArrayList<BomProductRule> findAllByCheck(String bprname, String bprtypename);

}