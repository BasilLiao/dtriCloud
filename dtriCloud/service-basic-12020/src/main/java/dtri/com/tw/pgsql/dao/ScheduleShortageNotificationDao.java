package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.ScheduleShortageNotification;

public interface ScheduleShortageNotificationDao extends JpaRepository<ScheduleShortageNotification, Long> {

	// 查詢用
	@Query("SELECT c FROM ScheduleShortageNotification c WHERE "//
			+ "(:ssnnb is null or c.ssnnb LIKE %:ssnnb%) and "// 成品BOM號
			+ "(:ssnsslerpcuser is null or c.ssnsslerpcuser LIKE %:ssnsslerpcuser%) and "// ERP 建單人
			+ "(:ssnsnotice is null or c.ssnsnotice = :ssnsnotice) and "// 是否缺料通知
			+ "(:ssnonotice is null or c.ssnonotice = :ssnonotice) and "// 是否排程通知
			+ "(:ssnimnotice is null or c.ssnimnotice = :ssnimnotice) and "// 是否排程通知(每日廠內)
			+ "(:sysstatus is null or c.sysstatus =:sysstatus)") //
	ArrayList<ScheduleShortageNotification> findAllBySearch(String ssnnb, String ssnsslerpcuser, Integer sysstatus,
			Boolean ssnsnotice, Boolean ssnonotice, Boolean ssnimnotice, Pageable pageable);

	// 檢查用
	@Query("SELECT c FROM ScheduleShortageNotification c WHERE "//
			+ "(:ssnsuid is null or c.ssnsuid=:ssnsuid) and "//
			+ "(:ssnsslerpcuser is null or c.ssnsslerpcuser=:ssnsslerpcuser) ")
	ArrayList<ScheduleShortageNotification> findAllByCheck(Long ssnsuid, String ssnsslerpcuser, Pageable pageable);

}