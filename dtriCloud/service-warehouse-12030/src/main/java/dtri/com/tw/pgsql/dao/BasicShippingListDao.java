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

	@Query("SELECT c FROM BasicShippingList c WHERE "//
			+ "(:sysstatus is null or c.sysstatus=:sysstatus) and"//
			+ "(:bslclass is null or c.bslclass LIKE %:bslclass% ) and "//
			+ "(:bslsn is null or c.bslsn LIKE %:bslsn% ) and "//
			+ "(:bsltype is null or  c.bsltype LIKE %:bsltype%) and "//
			+ "(:bslcuser is null or( :bslcuser ='true' and  c.bslcuser != '') or ( :bslcuser ='false' and  c.bslcuser = '')) and "// 已核准人// 未核准人
			+ "(:bslfromcommand is null or c.bslfromcommand LIKE %:bslfromcommand%) ")
	ArrayList<BasicShippingList> findAllBySearchStatus(String bslclass, String bslsn, String bslfromcommand, String bsltype, String bslcuser,
			Integer sysstatus, Pageable pageable);

	@Query("SELECT c FROM BasicShippingList c WHERE "//
			+ "(:bslclass is null or  c.bslclass LIKE %:bslclass%) and "//
			+ "(:bslsn is null or  c.bslsn LIKE %:bslsn%) and "//
			+ "(:bsltype is null or  c.bsltype LIKE %:bsltype%) and "// 類型
			+ "(:bslmuser is null or c.bslmuser LIKE %:bslmuser%) and "// 負責人
			+ "(c.bslfuser ='') ") // 已完成-負責人
	ArrayList<BasicShippingList> findAllBySearchAction(String bslclass, String bslsn, String bsltype, String bslmuser, Pageable pageable);

	// 同步查詢用
	@Query("SELECT c FROM BasicShippingList c WHERE "//
			+ "(:bslclass is null or c.bslclass LIKE %:bslclass% ) and "//
			+ "(:bslsn is null or c.bslsn LIKE %:bslsn% ) and "//
			+ "(:bsltype is null or  c.bsltype LIKE %:bsltype%) and "//
			+ "(c.bslpnqty !=c.bslpngqty) and "// 領的數量不同於需求量
			+ "(c.bslfuser != '') ") // 已完成
	ArrayList<BasicShippingList> findAllBySearchSynchronize(String bslclass, String bslsn, String bsltype, Pageable pageable);

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

}