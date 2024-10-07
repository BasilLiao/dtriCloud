package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.BiosNotification;

public interface BiosNotificationDao extends JpaRepository<BiosNotification, Long> {

	// 查詢用
	@Query("SELECT c FROM BiosNotification c WHERE "//
			+ "(:bnsuname is null or c.bnsuname LIKE %:bnsuname%) and "// 主要負責人
			+ "(:bnbvmodel is null or c.bnbvmodel LIKE %:bnbvmodel%) and "//
			+ "(:sysstatus is null or c.sysstatus =:sysstatus)") //
	ArrayList<BiosNotification> findAllBySearch(String bnsuname, String bnbvmodel, Integer sysstatus,
			Pageable pageable);

	// 檢查用
	@Query("SELECT c FROM BiosNotification c WHERE "//
			+ "(:bnsuid is null or c.bnsuid=:bnsuid) and "//
			+ "(:bnbvmodel is null or c.bnbvmodel=:bnbvmodel) ")
	ArrayList<BiosNotification> findAllByCheck(Long bnsuid, String bnbvmodel, Pageable pageable);

}