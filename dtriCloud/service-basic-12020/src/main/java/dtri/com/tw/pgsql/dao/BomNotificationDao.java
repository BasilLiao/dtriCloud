package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.BomNotification;

public interface BomNotificationDao extends JpaRepository<BomNotification, Long> {

	// 查詢用
	@Query("SELECT c FROM BomNotification c WHERE "//
			+ "(:bnnb is null or c.bnnb LIKE %:bnnb%) and "// 成品BOM號
			+ "(:bnmodel is null or c.bnmodel LIKE %:bnmodel%) and "// 型號
			+ "(:bnmnotice is null or c.bnmnotice = :bnmnotice) and "// 必須要有勾一個(更新)
			+ "(:bnanotice is null or c.bnanotice = :bnanotice) and "// 必須要有勾一個(新增)
			+ "(:bndnotice is null or c.bndnotice = :bndnotice) and "// 必須要有勾一個(移除)
			+ "(:sysstatus is null or c.sysstatus =:sysstatus)") //
	ArrayList<BomNotification> findAllBySearch(String bnnb, String bnmodel, Boolean bnmnotice, Boolean bnanotice,
			Boolean bndnotice, Integer sysstatus, Pageable pageable);

	// 檢查用
	@Query("SELECT c FROM BomNotification c WHERE "//
			+ "(:bnsuid is null or c.bnsuid=:bnsuid) and "//
			+ "(:bnnb is null or c.bnnb=:bnnb) ")
	ArrayList<BomNotification> findAllByCheck(Long bnsuid, String bnnb, Pageable pageable);

}