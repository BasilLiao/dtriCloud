package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.BasicIncomingList;

public interface BasicIncomingListDao extends JpaRepository<BasicIncomingList, Long> {

	@Query("SELECT c FROM BasicIncomingList c WHERE "//
			+ "(:sysstatus is null or c.sysstatus=:sysstatus) "//
			+ "order by c.bilclass asc, c.bilpnumber asc")
	ArrayList<BasicIncomingList> findAllByStatus(Integer sysstatus);

	@Query("SELECT c FROM BasicIncomingList c WHERE "//
			+ "(:sysstatus is null or c.sysstatus=:sysstatus) and"//
			+ "(:bilclass is null or  c.bilclass LIKE %:bilclass%) and "//
			+ "(:bilsn is null or  c.bilsn LIKE %:bilsn%) and "//
			+ "(:biltype is null or  c.biltype LIKE %:biltype%) and "//
			+ "(:bilcuser is null or (:bilcuser ='true'  and c.bilcuser != '') or (:bilcuser ='false'  and c.bilcuser = '')) and "// 已核准人// 未核准人
			+ "(:bilfromcommand is null or c.bilfromcommand LIKE %:bilfromcommand%) ")
	ArrayList<BasicIncomingList> findAllBySearchStatus(String bilclass, String bilsn, String bilfromcommand, String biltype, String bilcuser,
			Integer sysstatus, Pageable pageable);

	@Query("SELECT c FROM BasicIncomingList c WHERE "//
			+ "(:bilclass is null or  c.bilclass LIKE %:bilclass%) and "//
			+ "(:bilsn is null or  c.bilsn LIKE %:bilsn%) and "//
			+ "(:biltype is null or  c.biltype LIKE %:biltype%) and "// 類型
			+ "(:bilmuser is null or c.bilmuser LIKE %:bilmuser%) and "// 負責人
			+ "(c.bilfuser ='') ") // 已完成-負責人
	ArrayList<BasicIncomingList> findAllBySearchAction(String bilclass, String bilsn, String biltype, String bilmuser, Pageable pageable);

	// 同步查詢用
	@Query("SELECT c FROM BasicIncomingList c WHERE "//
			+ "(:bilclass is null or  c.bilclass LIKE %:bilclass%) and "//
			+ "(:bilsn is null or  c.bilsn LIKE %:bilsn%) and "//
			+ "(:biltype is null or  c.biltype LIKE %:biltype%) and "//
			+ "(c.bilpnqty !=c.bilpngqty) and "// 領的數量不同於需求量
			+ "(c.bilfuser != '') ") // 已完成
	ArrayList<BasicIncomingList> findAllBySearchSynchronize(String bilclass, String bilsn, String biltype, Pageable pageable);

	@Query("SELECT c FROM BasicIncomingList c WHERE "//
			+ "(:bilclass is null or c.bilclass=:bilclass) and "//
			+ "(:bilsn is null or c.bilsn=:bilsn) and "//
			+ "(:bilpnumber is null or c.bilpnumber=:bilpnumber) ")
	ArrayList<BasicIncomingList> findAllBySearch(String bilclass, String bilsn, String bilpnumber, Pageable pageable);

	@Query("SELECT c FROM BasicIncomingList c WHERE "//
			+ "(:bilclass is null or c.bilclass=:bilclass) and "//
			+ "(:bilsn is null or c.bilsn=:bilsn) and "//
			+ "(:bilnb is null or c.bilnb=:bilnb) ")
	ArrayList<BasicIncomingList> findAllByCheck(String bilclass, String bilsn, String bilnb);

	@Query("SELECT c FROM BasicIncomingList c WHERE "//
			+ "(:bilclass is null or c.bilclass=:bilclass) and "//
			+ "(:bilsn is null or c.bilsn=:bilsn) and "//
			+ "(c.bilfuser='') and "//
			+ "(:bilnb is null or c.bilnb=:bilnb) ")
	ArrayList<BasicIncomingList> findAllByCheckUser(String bilclass, String bilsn, String bilnb);

}