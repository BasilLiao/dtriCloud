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

	// 查詢一部分
	@Query("SELECT c FROM SystemConfig.systemConfigs c WHERE "//
			+ "(:scgname is null or c.scgname LIKE %:scgname% ) and "//
			+ "(:scname is null or c.systemConfig.scname LIKE %:scname% ) and "//
			+ "(coalesce(:sysmdatestart,null) is null or :sysmdatestart <= c.sysmdate ) and "//
			+ "(coalesce(:sysmdateend,null) is null or :sysmdateend >= c.sysmdate ) and "//
			+ "(c.sysheader =true ) and "//
			+ "( c.sysstatus = :sysstatus ) ")
	ArrayList<SystemConfig> findAllByConfig(String scname, String scgname, Date sysmdatestart, Date sysmdateend, Integer sysstatus,
			Pageable pageable);

	// 查詢是否重複 群組
	@Query("SELECT c FROM SystemConfig c WHERE  (c.scgname = :scgname) ")
	ArrayList<SystemConfig> findAllByConfigGroupTop1(String scgname, Pageable pageable);

	// 取得 目前ID
	@Query(value = "SELECT CURRVAL('system_config_seq')", nativeQuery = true)
	Long getSystemConfigSeq();

	// delete
	Long deleteByScidAndSysheader(Long id, Boolean sysheader);
}