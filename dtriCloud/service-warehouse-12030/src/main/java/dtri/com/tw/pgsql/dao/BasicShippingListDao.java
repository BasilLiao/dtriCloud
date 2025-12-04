package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.BasicShippingList;

public interface BasicShippingListDao extends JpaRepository<BasicShippingList, Long> {

	@Query("SELECT c FROM BasicShippingList c WHERE "//
			+ "(:sysstatus is null or c.sysstatus=:sysstatus) "//
			+ "order by c.bslclass asc, c.bslpnumber asc")
	ArrayList<BasicShippingList> findAllByStatus(Integer sysstatus);

	// 單據分配處理用
	@Query("SELECT c FROM BasicShippingList c WHERE "//
			+ "(:sysstatus is null or c.sysstatus=:sysstatus) and"//
			+ "(:bslclass is null or c.bslclass LIKE %:bslclass% ) and "//
			+ "(:bslsn is null or c.bslsn LIKE %:bslsn% ) and "//
			+ "(:bsltype is null or  c.bsltype LIKE %:bsltype%) and "//
			+ "(:syshnote is null or  c.syshnote LIKE %:syshnote%) and "//
			+ "(c.bslfuser != 'ERP_Remove(Auto)') and "//
			+ "(:bslfuser is null or (:bslfuser ='true' and  c.bslfuser != '') or (:bslfuser ='false' and  c.bslfuser = '')) and "// 已領料_
																																	// 未領料
			+ "(:bslcuser is null or (:bslcuser ='true' and  c.bslcuser != '') or (:bslcuser ='false' and  c.bslcuser = '')) and "// 已核准人_
																																	// 未核准人
			+ "(:bslsmuser is null or (:bslsmuser ='true' and  c.bslsmuser != '') or (:bslsmuser ='false' and  c.bslsmuser = '')) and "// 已核准人_

			+ "(:bslfromcommand is null or c.bslfromcommand LIKE %:bslfromcommand%) ")
	ArrayList<BasicShippingList> findAllBySearchStatus(String bslclass, String bslsn, String bslfromcommand,
			String bsltype, String bslcuser, String bslfuser, String bslsmuser, Integer sysstatus, String syshnote,
			Pageable pageable);

	@Query("SELECT c FROM BasicShippingList c WHERE "//
			+ "(:bslclass is null or  c.bslclass LIKE %:bslclass%) and "//
			+ "(:bslsn is null or  c.bslsn LIKE %:bslsn%) and "//
			+ "(:bsltype is null or  c.bsltype LIKE %:bsltype%) and "// 類型
			+ "(:bslmuser is null or (c.bslmuser LIKE %:bslmuser% or c.bslmuser='')) and "// 負責人
			+ "(c.bslcuser !='') and " // 核准人
			+ "(:bslfuser is null or c.bslfuser =:bslfuser) ") // 已完成-負責人
	ArrayList<BasicShippingList> findAllBySearchAction(String bslclass, String bslsn, String bsltype, String bslmuser,
			String bslfuser, Pageable pageable);

	// 同步查詢用
	@Query("SELECT c FROM BasicShippingList c WHERE "//
			+ "(:bslclass is null or c.bslclass LIKE %:bslclass% ) and "//
			+ "(:bslsn is null or c.bslsn LIKE %:bslsn% ) and "//
			+ "(:bsltype is null or  c.bsltype LIKE %:bsltype%) and "//
			+ "((c.bslpnqty  >=0) or "// 需領的數量大於等於 0
			+ " (c.bslclass='A541' and c.bslpngqty != c.bslpnerpqty)) and "// ERP(領退數量(帳務)="A541" && 已領用量 !=領退數量(帳務) )
			+ "(c.bslfuser != 'ERP_Remove(Auto)') and (c.bslsuser = '')") // 已完成+最後-同步人
	ArrayList<BasicShippingList> findAllBySearchSynchronize(String bslclass, String bslsn, String bsltype,
			Pageable pageable);

	// 同步查詢用(單據完成率)
	@Query("SELECT c FROM BasicShippingList c WHERE "//
			+ "(:bslclass is null or c.bslclass LIKE %:bslclass%) and "//
			+ "(:bslsn is null or c.bslsn LIKE %:bslsn% ) and "//
			+ "(:bsltype is null or c.bsltype LIKE %:bsltype%) and "//
			+ "(:sysstatus is null or c.sysstatus = :sysstatus) and "//
			+ "(c.bslfuser != 'ERP_Remove(Auto)') and "//
			+ "(c.bslcuser != '') and (c.bslsuser = '')") // 已核准人+最後-同步人
	ArrayList<BasicShippingList> findAllBySearchDetailSynchronize(String bslclass, String bslsn, String bsltype,
			Integer sysstatus, Pageable pageable);

	@Query("SELECT c FROM BasicShippingList c WHERE "//
			+ "(:bslclass is null or c.bslclass=:bslclass) and "//
			+ "(:bslsn is null or c.bslsn=:bslsn) and "//
			+ "(:bslpnumber is null or c.bslpnumber=:bslpnumber) ")
	ArrayList<BasicShippingList> findAllBySearch(String bslclass, String bslsn, String bslpnumber, Pageable pageable);

	@Query("SELECT c FROM BasicShippingList c WHERE "//
			+ "(:bslclass is null or c.bslclass=:bslclass) and "//
			+ "(:bslsn is null or c.bslsn=:bslsn) and "//
			+ "(:bslnb is null or c.bslnb=:bslnb) ")
	ArrayList<BasicShippingList> findAllByCheck(String bslclass, String bslsn, String bslnb);

	@Query("SELECT c FROM BasicShippingList c WHERE "//
			+ "(:bslclass is null or c.bslclass=:bslclass) and "//
			+ "(:bslsn is null or c.bslsn=:bslsn) and "//
			+ "(c.bslfuser='') and "//
			+ "(:bslnb is null or c.bslnb=:bslnb) ")
	ArrayList<BasicShippingList> findAllByCheckUser(String bslclass, String bslsn, String bslnb);

	// 列出 -倉儲負責人 "已領料" 領料單類 -領料單類的 "未核單" 狀態
	@Query("SELECT c FROM BasicShippingList c WHERE "//
			+ "(c.bslmuser!='') and "// 已經領料
			+ "(c.bslpngqty>0) and "// 已經領料>0
			+ "(c.bslcheckin = 0) ") // 未核單
	ArrayList<BasicShippingList> findAllByCheckBslpngqty();

	// 缺料單
	@Query("SELECT c FROM BasicShippingList c WHERE "//
			+ "(:bslclass is null or c.bslclass=:bslclass) and "//
			+ "(:bslsn is null or c.bslsn=:bslsn) and "//
			+ "(:bslnb is null or c.bslnb=:bslnb) and "//
			+ "(c.bslfuser!='') and "//
			+ "(c.bslpngqty < c.bslpnqty)") //
	ArrayList<BasicShippingList> findAllByCheckShortageList(String bslclass, String bslsn, String bslnb,
			Pageable pageable);

}