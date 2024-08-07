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

	// 單據分配處理用
	@Query("SELECT c FROM BasicIncomingList c WHERE "//
			+ "(:sysstatus is null or c.sysstatus=:sysstatus) and"//
			+ "(:bilclass is null or  c.bilclass LIKE %:bilclass%) and "//
			+ "(:bilsn is null or  c.bilsn LIKE %:bilsn%) and "//
			+ "(:biltype is null or  c.biltype LIKE %:biltype%) and "//
			+ "(:syshnote is null or  c.syshnote LIKE %:syshnote%) and "//
			+ "(c.bilfuser != 'ERP_Remove(Auto)') and "//
			+ "(:bilfuser is null or (:bilfuser ='true' and c.bilfuser != '') or (:bilfuser ='false'  and c.bilfuser = '')) and "// 已領料_//
																																	// 未領料
			+ "(:bilcuser is null or (:bilcuser ='true' and c.bilcuser != '') or (:bilcuser ='false'  and c.bilcuser = '')) and "// 已核准人//
																																	// 未核准人
			+ "(:bilfromcommand is null or c.bilfromcommand LIKE %:bilfromcommand%) ")
	ArrayList<BasicIncomingList> findAllBySearchStatus(String bilclass, String bilsn, String bilfromcommand,
			String biltype, String bilcuser, String bilfuser, Integer sysstatus, String syshnote, Pageable pageable);

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

	// 同步查詢用
	@Query("SELECT c FROM BasicIncomingList c WHERE "//
			+ "(:bilclass is null or  c.bilclass LIKE %:bilclass%) and "//
			+ "(:bilsn is null or  c.bilsn LIKE %:bilsn%) and "//
			+ "(:biltype is null or  c.biltype LIKE %:biltype%) and "//
			+ "((c.bilpnoqty >=0) or "// 需入的數量大於等於0 或
			+ " (c.bilclass='A541' and c.bilpngqty != c.bilpnerpqty)) and "// ERP(領退數量(帳務)="A541" && 已領用量 !=領退數量(帳務) )
			+ "(c.bilfuser != 'ERP_Remove(Auto)') and (c.bilsuser = '') ") // 已完成+最後-同步人
	ArrayList<BasicIncomingList> findAllBySearchSynchronize(String bilclass, String bilsn, String biltype,
			Pageable pageable);

	// 同步查詢用(單據完成率)
	@Query("SELECT c FROM BasicIncomingList c WHERE "//
			+ "(:bilclass is null or c.bilclass LIKE %:bilclass%) and "//
			+ "(:bilsn is null or c.bilsn LIKE %:bilsn%) and "//
			+ "(:biltype is null or c.biltype LIKE %:biltype%) and "//
			+ "(c.bilfuser != 'ERP_Remove(Auto)') and "//
			+ "(:sysstatus is null or c.sysstatus = :sysstatus) and "//
			+ "(c.bilcuser != '') and (c.bilsuser = '') ") // 已核准人+最後-同步人
	ArrayList<BasicIncomingList> findAllBySearchDetailSynchronize(String bilclass, String bilsn, String biltype,
			Integer sysstatus, Pageable pageable);

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