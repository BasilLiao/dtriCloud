package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.SystemUser;

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
			+ "(:sysstatus=4 or c.sysstatus = :sysstatus )  ") // (4)不過濾
	ArrayList<SystemUser> findAllBySystemUser(String suname, String suaccount, String suposition, Integer sysstatus, Pageable pageable);

	// 多筆查詢範例
	@Query(" SELECT i.suname FROM SystemUser i WHERE "//
			+ "(coalesce(:accounts, null) is null or i.suaccount IN :accounts ) ") // coalesce 回傳非NULL值
	ArrayList<String> readAccounts(List<String> accounts);


}