package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;
import java.util.Date;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.BomHistory;

public interface BomHistoryDao extends JpaRepository<BomHistory, Long> {

	// 查詢用
	@Query("SELECT c FROM BomHistory c WHERE "//
			+ "(:bhnb is null or c.bhnb LIKE %:bhnb%) and "//
			+ "(:bhmodel is null or c.bhmodel LIKE %:bhmodel%) and " //
			+ "(:bhpnb is null or c.bhpnb LIKE %:bhpnb%) and " //
			+ "(cast(:ssyscdate as date) is null or c.syscdate >= :ssyscdate) and " //
			+ "(cast(:esyscdate as date) is null or c.syscdate <= :esyscdate) and " //
			+ "(c.bhnotification = false) and " //
			+ "(:sysmuser is null or c.sysmuser LIKE %:sysmuser%)") //
	ArrayList<BomHistory> findAllBySearch(String bhnb, String bhmodel, String bhpnb, String sysmuser, Date ssyscdate,
			Date esyscdate, Pageable pageable);

	// 檢查用
	@Query("SELECT c FROM BomHistory c WHERE "//
			+ "(:bhnb is null or c.bhnb = :bhnb) and "//
			+ "(:bhpnb is null or c.bhpnb=:bhpnb) ")
	ArrayList<BomHistory> findAllByCheck(String bhnb, String bhpnb, Pageable pageable);

}