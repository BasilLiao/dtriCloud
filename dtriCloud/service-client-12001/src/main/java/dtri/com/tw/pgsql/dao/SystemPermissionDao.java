package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dtri.com.tw.pgsql.entity.SystemPermission;

@Repository
public interface SystemPermissionDao extends JpaRepository<SystemPermission, Long> {

	// 查詢群組權限
	ArrayList<SystemPermission> findBySpgid(Long spgid);

	// 查詢全部
	ArrayList<SystemPermission> findAllByOrderBySpgidAscSpidAsc(Pageable pageable);

	// 查詢一部分
	@Query("SELECT c FROM SystemPermission c "
			+ "WHERE (:spname is null or c.spname LIKE %:spname% ) and "//
			+ "(:spgname is null or c.spgname LIKE %:spgname% ) and "//
			+ "(:user='admin' or c.sysstatus = :sysstatus ) "//
			+ "order by c.spgid asc,c.syssort asc,c.sysmdate desc")
	ArrayList<SystemPermission> findAllByPermission(@Param("spname") String spname, @Param("spgname") String spgname,
			@Param("sysstatus") Integer sysstatus,String user, Pageable pageable);

	// 查詢是否重複 群組
	@Query("SELECT c FROM SystemPermission c "
			+ "WHERE  (c.spgname = :spgname) "
			+ "order by c.spgid desc")
	ArrayList<SystemPermission> findAllByPermissionGroupTop1(@Param("spgname") String spgname,Pageable pageable);

	// 取得G_ID
	@Query(value = "SELECT NEXTVAL('system_permission_g_seq')", nativeQuery = true)
	Long getSystem_config_g_seq();
	
	//delete
	Long deleteBySpidAndSysheader(Long id, Boolean sysheader);
}