package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.BasicIncomingList;

public interface BasicIncomingListDao extends JpaRepository<BasicIncomingList, Long> {

	@Query("SELECT c FROM BasicIncomingList c WHERE "//
			+ "(:sysstatus is null or c.sysstatus=:sysstatus) and "//
			+ "(COALESCE(:bilclass) is null or c.bilclass IN :bilclass) "//
			+ "order by c.bilclass asc, c.bilpnumber asc")
	ArrayList<BasicIncomingList> findAllByStatus(Integer sysstatus, List<String> bilclass);

	@Query("SELECT c FROM BasicIncomingList c WHERE "//
			+ "(:bilclass is null or c.bilclass LIKE %:bilclass%) and "//
			+ "(:bilsn is null or c.bilsn LIKE %:bilsn%) and "//
			+ "(:bilfuser is null or c.bilfuser LIKE %:bilfuser%) and "//
			+ "(:bilpnumber is null or c.bilpnumber LIKE %:bilpnumber%) ")
	ArrayList<BasicIncomingList> findAllBySearch(String bilclass, String bilsn, String bilpnumber, String bilfuser,
			Pageable pageable);

	@Query("SELECT c FROM BasicIncomingList c WHERE "//
			+ "(:bilclass is null or c.bilclass=:bilclass) and "//
			+ "(:bilsn is null or c.bilsn=:bilsn) and "//
			+ "(:bilpnumber is null or c.bilpnumber=:bilpnumber) ")
	ArrayList<BasicIncomingList> findAllByCheck(String bilclass, String bilsn, String bilpnumber, Pageable pageable);

	@Query("SELECT c FROM BasicIncomingList c WHERE "//
			+ "(:bilclass is null or  c.bilclass LIKE %:bilclass%) and "//
			+ "(:bilsn is null or  c.bilsn LIKE %:bilsn%) and "//
			+ "(:biltype is null or  c.biltype LIKE %:biltype%) and "// 類型
			+ "(:bilmuser is null or (c.bilmuser LIKE %:bilmuser% or c.bilmuser='')) and "// 負責人
			+ "(c.bilcuser !='') and " // 核准人
			+ "(c.bilcheckin =1) and " // 已核單
			+ "(:bilfuser is null or c.bilfuser =:bilfuser) ") // 已完成-負責人
	ArrayList<BasicIncomingList> findAllBySearchAction(String bilclass, String bilsn, String biltype, String bilmuser,
			String bilfuser, Pageable pageable);

	@Query("SELECT c FROM BasicIncomingList c WHERE "//
			+ "(cast(:syscdate as date) is null or c.syscdate <= :syscdate)") //
	ArrayList<BasicIncomingList> findAllBySyscdateRemove(Date syscdate);
}