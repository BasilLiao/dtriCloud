package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.db.entity.SystemUser;

public interface SystemUserDao extends JpaRepository<SystemUser, Long> {

	// 帳號查詢
	SystemUser findBySuaccount(String suaccount);

	// 查詢全部
	ArrayList<SystemUser> findAll();

	// 查詢ID
	ArrayList<SystemUser> findAllBySuid(Long id);

	// 查詢全部含-頁數
	@Query("SELECT c FROM SystemUser c "//
			+ "WHERE (:suname is null or c.suname LIKE %:suname% ) and "//
			+ "(:suaccount is null or c.suaccount LIKE %:suaccount% ) and "//
			+ "(:suposition is null or c.suposition LIKE %:suposition% ) and "//
			+ "(:admin is not null or c.sysstatus != 3 ) and "//
			+ "(:sysstatus is null or c.sysstatus = :sysstatus )  ") // (4)不過濾
	ArrayList<SystemUser> findAllBySystemUser(String suname, String suaccount, String suposition, String admin, Integer sysstatus, Pageable pageable);

	@Query("SELECT c FROM SystemUser c WHERE "//
			+ "(:suaccount is null or c.suaccount =:suaccount) and"//
			+ "(:suname is null or c.suname =:suname) and"//
			+ "(:suename is null or c.suename =:suename) and"//
			+ "(:suemail is null or c.suemail =:suemail)")
	ArrayList<SystemUser> findAllBySystemUserCheck(String suaccount, String suemail, String suname, String suename);

	// 多筆查詢範例
	@Query(" SELECT i.suname FROM SystemUser i WHERE "//
			+ "(coalesce(:accounts, null) is null or i.suaccount IN :accounts ) ") // coalesce 回傳非NULL值
	ArrayList<String> readAccounts(List<String> accounts);

	// 移除
	Long deleteBySuid(Long suid);

}