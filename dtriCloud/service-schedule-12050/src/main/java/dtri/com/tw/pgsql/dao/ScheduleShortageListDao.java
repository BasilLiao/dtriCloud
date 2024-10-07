package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.ScheduleShortageList;

public interface ScheduleShortageListDao extends JpaRepository<ScheduleShortageList, Long> {

	// 查詢用
	@Query("SELECT c FROM ScheduleShortageList c WHERE "//
			+ "(:sslbslsnnb is null or c.sslbslsnnb LIKE %:sslbslsnnb%) and "//
			+ "(:sslpnumber is null or c.sslpnumber LIKE %:sslpnumber%) and "//
			+ "(c.sslfuser != 'ERP_Remove(Auto)') and "//
			+ "(:sslfromcommand is null or c.sslfromcommand LIKE %:sslfromcommand%) and "//
			+ "(:sysstatus is null or c.sysstatus =:sysstatus)") //
	ArrayList<ScheduleShortageList> findAllBySearch(String sslbslsnnb, String sslpnumber, Integer sysstatus,
			String sslfromcommand, Pageable pageable);

	// 檢查用
	@Query("SELECT c FROM ScheduleShortageList c WHERE "//
			+ "(:sslbslsnnb is null or c.sslbslsnnb=:sslbslsnnb) and "//
			+ "(:sslpnumber is null or c.sslpnumber=:sslpnumber) ")
	ArrayList<ScheduleShortageList> findAllByCheck(String sslbslsnnb, String sslpnumber, Pageable pageable);

}