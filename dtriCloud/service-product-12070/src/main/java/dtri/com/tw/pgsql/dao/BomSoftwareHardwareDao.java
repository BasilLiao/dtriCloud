package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.BomKeeper;
import dtri.com.tw.pgsql.entity.BomSoftwareHardware;

public interface BomSoftwareHardwareDao extends JpaRepository<BomSoftwareHardware, Long> {

	// 帳號查詢
	BomKeeper findByBshmodel(String bshmodel);

	// 查詢全部
	ArrayList<BomSoftwareHardware> findAll();

	// 查詢ID
	ArrayList<BomSoftwareHardware> findAllBshid(Long id);

	// 查詢全部含-頁數
	@Query("SELECT c FROM BomSoftwareHardware c "//
			+ "WHERE (:bshnb is null or c.bshnb LIKE %:bshnb% ) and "//
			+ "(:bshmodel is null or c.bshmodel LIKE %:bshmodel% ) and "//
			+ "(:bshcname is null or c.bshcname LIKE %:bshcname% ) ") // (4)不過濾
	ArrayList<BomSoftwareHardware> findAllByBomKeeper(String bshnb, String bshmodel, String bshcname,
			Pageable pageable);

	// 多筆查詢範例
//	@Query(" SELECT i.suname FROM BomKeeper i WHERE "//
//			+ "(coalesce(:accounts, null) is null or i.suaccount IN :accounts ) ") // coalesce 回傳非NULL值
//	ArrayList<String> readAccounts(List<String> accounts);

}