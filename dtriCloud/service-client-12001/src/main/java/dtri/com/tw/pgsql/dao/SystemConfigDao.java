package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.db.entity.SystemConfig;

public interface SystemConfigDao extends JpaRepository<SystemConfig, Long> {

	// 查詢全部
	ArrayList<SystemConfig> findAll();

	// 查詢一部分
	@Query("SELECT c FROM SystemConfig c WHERE "//
			+ "(:scname is null or c.scname LIKE %:scname% ) and "//
			+ "(:scgname is null or c.scgname LIKE %:scgname% ) and "//
			+ "( c.sysstatus = :sysstatus ) ")
	ArrayList<SystemConfig> findAllByConfig(String scname, String scgname, Integer sysstatus, Pageable pageable);

	// 查詢是否重複 群組
	@Query("SELECT c FROM SystemConfig c WHERE  (c.scgname = :scgname) ")
	ArrayList<SystemConfig> findAllByConfigGroupTop1(String scgname, Pageable pageable);

	// 取得 下一個 G_ID
	@Query(value = "SELECT NEXTVAL('system_config_g_seq')", nativeQuery = true)
	Long getSystemConfigGSeq();

	// 取得 目前ID
	@Query(value = "SELECT CURRVAL('system_config_seq')", nativeQuery = true)
	Long getSystemConfigSeq();

	// delete
	Long deleteByScidAndSysheader(Long id, Boolean sysheader);
}