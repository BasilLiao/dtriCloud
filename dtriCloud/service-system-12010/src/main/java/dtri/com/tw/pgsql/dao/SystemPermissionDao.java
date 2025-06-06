package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import dtri.com.tw.pgsql.entity.SystemPermission;

@Repository
public interface SystemPermissionDao extends JpaRepository<SystemPermission, Long> {

	// 查詢權限
	ArrayList<SystemPermission> findBySpid(Long spid);

	// 查詢群組權限
	ArrayList<SystemPermission> findBySpgid(Long spgid);

	// 查詢全部
	ArrayList<SystemPermission> findAllByOrderBySpgidAscSpidAsc(Pageable pageable);

	// 查詢一部分
	@Query("SELECT c FROM SystemPermission c " //
			+ "WHERE (:spname is null or c.spname LIKE %:spname% ) and "//
			+ "(:spgname is null or c.spgname LIKE %:spgname% ) and "//
			+ "(c.spgid !=0L ) and "//
			+ "(:user='admin' or c.sysstatus = :sysstatus ) ")//
	ArrayList<SystemPermission> findAllByPermission(String spname, String spgname, Integer sysstatus, String user, Pageable pageable);

	// 查詢是否重複 名稱+群組+權限
	@Query("SELECT c FROM SystemPermission c " //
			+ "WHERE  (:spgname is null or c.spgname = :spgname) and "//
			+ " (:spname is null or c.spname = :spname) and " //
			+ " (:spcontrol is null or c.spcontrol = :spcontrol)" //
			+ "order by c.spgid desc")
	ArrayList<SystemPermission> findAllByPCheck(String spgname, String spname, String spcontrol);

	// 取得G_ID
	@Query(value = "SELECT NEXTVAL('system_permission_g_seq')", nativeQuery = true)
	Long getSystemConfigGseq();

	// delete
	Long deleteBySpidAndSysheader(Long id, Boolean sysheader);
}