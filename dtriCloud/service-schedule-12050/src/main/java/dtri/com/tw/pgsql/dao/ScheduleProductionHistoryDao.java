package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;
import java.util.Date;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.ScheduleProductionHistory;

public interface ScheduleProductionHistoryDao extends JpaRepository<ScheduleProductionHistory, Long> {

	// 查詢用
	@Query("SELECT c FROM ScheduleProductionHistory c WHERE "//
			+ "(:sphbpmnb is null or c.sphbpmnb LIKE %:sphbpmnb%) and "//
			+ "(:sphbpmmodel is null or c.sphbpmmodel LIKE %:sphbpmmodel%) and " //
			+ "(:sphonb is null or c.sphonb LIKE %:sphonb%) and " //
			+ "(:sphpon is null or c.sphpon LIKE %:sphpon%) and "//
			+ "(cast(:ssyscdate as date) is null or c.syscdate >= :ssyscdate) and " //
			+ "(cast(:esyscdate as date) is null or c.syscdate <= :esyscdate) and " //
			+ "(:sysmuser is null or c.sysmuser LIKE %:sysmuser%)") //
	ArrayList<ScheduleProductionHistory> findAllBySearch(String sphpon,String sphbpmnb, String sphbpmmodel, String sphonb,
			String sysmuser, Date ssyscdate, Date esyscdate, Pageable pageable);

	// 檢查用
	@Query("SELECT c FROM ScheduleProductionHistory c WHERE "//
			+ "(:sphpon is null or c.sphpon = :sphpon) and "//
			+ "(:sphbpmnb is null or c.sphbpmnb = :sphbpmnb) and "//
			+ "(:sphbpmmodel is null or c.sphbpmmodel=:sphbpmmodel) ")
	ArrayList<ScheduleProductionHistory> findAllByCheck(String sphpon, String sphbpmnb, String sphbpmmodel,
			Pageable pageable);

}