package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.BomParameterSettings;

public interface BomParameterSettingsDao extends JpaRepository<BomParameterSettings, Long> {

	// 帳號查詢
	BomParameterSettings findByBpsmodel(String bpsmodel);

	// 查詢全部
	ArrayList<BomParameterSettings> findAll();

	// 查詢全部含-頁數
	@Query("SELECT c FROM BomParameterSettings c "//
			+ "WHERE (:bpsname is null or c.bpsname LIKE %:bpsname% ) and "//
			+ "(:bpsmodel is null or c.bpsmodel LIKE %:bpsmodel% ) and "//
			+ "(:bpsnb is null or c.bpsnb LIKE %:bpsnb% ) ") // (4)不過濾
	ArrayList<BomParameterSettings> findAllBySearch(String bpsname, String bpsmodel, String bpsnb, Pageable pageable);

	// 檢查
	@Query("SELECT c FROM BomParameterSettings c "//
			+ "WHERE (:bpsname  is null or c.bpsname  =:bpsname  ) and "//
			+ "(:bpsmodel is null or c.bpsmodel = :bpsmodel ) and "//
			+ "(:bpsnb is null or c.bpsnb =:bpsnb ) ") // (4)不過濾
	ArrayList<BomParameterSettings> findAllByCheck(String bpsname, String bpsmodel, String bpsnb);

	// 多筆查詢範例
//	@Query(" SELECT i.suname FROM BomParameterSettings i WHERE "//
//			+ "(coalesce(:accounts, null) is null or i.suaccount IN :accounts ) ") // coalesce 回傳非NULL值
//	ArrayList<String> readAccounts(List<String> accounts);

}