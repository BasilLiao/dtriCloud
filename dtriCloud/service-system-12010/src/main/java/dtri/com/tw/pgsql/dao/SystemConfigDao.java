package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;
import java.util.Date;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.SystemConfig;

public interface SystemConfigDao extends JpaRepository<SystemConfig, Long> {

	// 查詢全部
	ArrayList<SystemConfig> findAll();

	// 查詢一部分(一般用)
	@Query("SELECT c FROM SystemConfig c  WHERE "//
			+ "(:scname is null or c.scname LIKE %:scname% ) and "//
			+ "(:scgname is null or c.scgname LIKE %:scgname% ) and "//
			+ "(coalesce(:sysmdatestart,null) is null or :sysmdatestart <= c.sysmdate ) and "//
			+ "(coalesce(:sysmdateend,null) is null or :sysmdateend >= c.sysmdate ) and "//
			+ "(c.scid !=0 )and "//
			+ "(:sysheader is null or c.sysheader =:sysheader) and "//
			+ "(:sysstatus is null or c.sysstatus = :sysstatus ) ")
	ArrayList<SystemConfig> findAllByConfig(String scname, String scgname, Date sysmdatestart, Date sysmdateend, Integer sysstatus, Boolean sysheader,
			Pageable pageable);

	// 查詢一部分(根源類別)
	@Query("SELECT c FROM SystemConfig c  WHERE "//
			+ "(c.scid = 0 )")
	ArrayList<SystemConfig> findAllByConfigDefCheck();

	// 檢查一部分(父/子類別)
	@Query("SELECT c FROM SystemConfig c  WHERE "//
			+ "(:scname is null or c.scname = :scname ) and "//
			+ "(:scgname is null or c.scgname = :scgname ) and "//
			+ "(:sysheader is null or c.sysheader =:sysheader) and "//
			+ "(c.scid !=0 )") //
	ArrayList<SystemConfig> findAllByConfigCheck(String scname, String scgname, boolean sysheader);

	// 查詢一部分(ID)
	@Query("SELECT c FROM SystemConfig c  WHERE ( c.scid = :scid ) ")
	ArrayList<SystemConfig> findAllByConfigByScid(Long scid);

	// 查詢一部分(GID)
	@Query("SELECT c FROM SystemConfig c  WHERE ( c.scgid = :scgid ) ")
	ArrayList<SystemConfig> findAllByConfigByScgid(Long scgid);

	// 查詢是否重複 群組
	@Query("SELECT c FROM SystemConfig c WHERE  (c.scgname = :scgname) ")
	ArrayList<SystemConfig> findAllByConfigGroupTop1(String scgname, Pageable pageable);

	// 取得 目前ID
	@Query(value = "SELECT CURRVAL('system_config_seq')", nativeQuery = true)
	Long getSystemConfigSeq();

	// 取得 下一個GID
	@Query(value = "SELECT NEXTVAL('system_config_g_seq')", nativeQuery = true)
	Long getSystemNextConfigGSeq();

	// delete
	Long deleteByScidAndSysheader(Long id, Boolean sysheader);
}