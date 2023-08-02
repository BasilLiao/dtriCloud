package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;
import java.util.Date;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.db.entity.SystemConfig;

public interface SystemConfigDao extends JpaRepository<SystemConfig, Long> {

	// 查詢全部
	ArrayList<SystemConfig> findAll();

	// 查詢一部分(一般用)
	@Query("SELECT c.systemConfig FROM SystemConfig c  WHERE "//
			+ "(:scname is null or c.scname LIKE %:scname% ) and "//
			+ "(:scgname is null or c.systemConfig.scname LIKE %:scgname% ) and "//
			+ "(coalesce(:sysmdatestart,null) is null or :sysmdatestart <= c.sysmdate ) and "//
			+ "(coalesce(:sysmdateend,null) is null or :sysmdateend >= c.sysmdate ) and "//
			+ "(c.systemConfig.scid !=0 )and "//
			+ "(:sysstatus is null or c.sysstatus = :sysstatus ) ")
	ArrayList<SystemConfig> findAllByConfig(String scname, String scgname, Date sysmdatestart, Date sysmdateend, Integer sysstatus,
			Pageable pageable);

	// 查詢一部分(根源類別)
	@Query("SELECT c FROM SystemConfig c  WHERE "//
			+ "(:scname is null or c.scname  =:scname ) and "//
			+ "(:scgname is null or c.scgname = :scgname ) and "//
			+ "(c.systemConfig.scid = 0 )")
	ArrayList<SystemConfig> findAllByConfigDefCheck(String scname, String scgname);

	// 查詢一部分(父類別)
	@Query("SELECT c.systemConfig FROM SystemConfig c  WHERE "//
			+ "(:scname is null or c.scname = :scname ) and "//
			+ "(:scgname is null or c.systemConfig.scname = :scgname ) and "//
			+ "(c.systemConfig.scid !=0 )") //
	ArrayList<SystemConfig> findAllByConfigCheck(String scname, String scgname);

	// 查詢一部分(子類別)
	@Query("SELECT c FROM SystemConfig c  WHERE "//
			+ "(:scname is null or c.scname  =:scname ) and "//
			+ "(:scgname is null or c.scgname = :scgname ) and "//
			+ "(c.systemConfig.scid !=0 )")
	ArrayList<SystemConfig> findAllByConfigDetaailCheck(String scname, String scgname);

	// 查詢一部分(父類別)
	@Query("SELECT c.systemConfig FROM SystemConfig c  WHERE "//
			+ "( c.systemConfig.scid = :scgid ) ")
	ArrayList<SystemConfig> findAllByConfigByScgid(Long scgid);
	
	// 查詢一部分(子類別)
		@Query("SELECT c FROM SystemConfig c  WHERE "//
				+ "( c.scid = :scid ) ")
		ArrayList<SystemConfig> findAllByConfigSonByScgid(Long scid);

	// 查詢是否重複 群組
	@Query("SELECT c FROM SystemConfig c WHERE  (c.scgname = :scgname) ")
	ArrayList<SystemConfig> findAllByConfigGroupTop1(String scgname, Pageable pageable);

	// 取得 目前ID
	@Query(value = "SELECT CURRVAL('system_config_seq')", nativeQuery = true)
	Long getSystemConfigSeq();

	// 取得 下一個ID
	@Query(value = "SELECT NEXTVAL('system_config_seq')", nativeQuery = true)
	Long getSystemNextConfigSeq();

	// delete
	Long deleteByScidAndSysheader(Long id, Boolean sysheader);
}