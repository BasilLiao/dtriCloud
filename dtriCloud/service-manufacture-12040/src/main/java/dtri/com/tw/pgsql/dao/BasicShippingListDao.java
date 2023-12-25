package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.BasicShippingList;

public interface BasicShippingListDao extends JpaRepository<BasicShippingList, Long> {

	// 製造-點料
	@Query("SELECT c FROM BasicShippingList c WHERE "//
			+ "(:bslclass is null or  c.bslclass LIKE %:bslclass%) and "//
			+ "(:bslsn is null or  c.bslsn LIKE %:bslsn%) and "//
			+ "(:bsltype is null or  c.bsltype LIKE %:bsltype%) and "// 類型
			+ "(c.bslfuser !='') and "// 已完成-負責人
			+ "(c.bslcuser !='') and " // 核准人
			+ "((:bslsmuser is null and c.bslsmuser ='') or (:bslsmuser='未點料' and c.bslsmuser ='') or (:bslsmuser='已點料' and c.bslsmuser !='')) ") // 已完成-產線點料
	ArrayList<BasicShippingList> findAllByManufactureSearchAction(String bslclass, String bslsn, String bsltype,
			String bslsmuser, Pageable pageable);

	// 製造-點料
	@Query("SELECT c FROM BasicShippingList c WHERE "//
			+ "(:bslclass is null or  c.bslclass LIKE %:bslclass%) and "//
			+ "(:bslsn is null or  c.bslsn LIKE %:bslsn%) and "//
			+ "(:bsltype is null or  c.bsltype LIKE %:bsltype%) and "// 類型
			+ "(c.bslfuser !='') and "// 已完成-負責人
			+ "(c.bslcuser !='') ") // 核准人
	ArrayList<BasicShippingList> findAllByManufactureDetailSearchAction(String bslclass, String bslsn, String bsltype,
			Pageable pageable);

	// 製造-點料
	@Query("SELECT c FROM BasicShippingList c WHERE "//
			+ "(:bslclass is null or c.bslclass=:bslclass) and "//
			+ "(:bslsn is null or c.bslsn=:bslsn) and "//
			+ "(:bslnb is null or c.bslnb=:bslnb) and "//
			+ "(c.bslsmuser='') ") //
	ArrayList<BasicShippingList> findAllByCheckUser(String bslclass, String bslsn, String bslnb);

}