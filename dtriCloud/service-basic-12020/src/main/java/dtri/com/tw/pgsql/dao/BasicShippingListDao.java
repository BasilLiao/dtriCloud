package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.BasicShippingList;

public interface BasicShippingListDao extends JpaRepository<BasicShippingList, Long> {

	@Query("SELECT c FROM BasicShippingList c WHERE "//
			+ "(:sysstatus is null or c.sysstatus=:sysstatus) "//
			+ "and (COALESCE(:bslclass) is null or c.bslclass IN :bslclass) "//
			+ "order by c.bslclass asc, c.bslpnumber asc")
	ArrayList<BasicShippingList> findAllByStatus(Integer sysstatus, List<String> bslclass);

	@Query("SELECT c FROM BasicShippingList c WHERE "//
			+ "(:bslclass is null or c.bslclass=:bslclass) and "//
			+ "(:bslsn is null or c.bslsn=:bslsn) and "//
			+ "(:bslpnumber is null or c.bslpnumber=:bslpnumber) ")
	ArrayList<BasicShippingList> findAllByCheck(String bslclass, String bslsn, String bslpnumber, Pageable pageable);

	@Query("SELECT c FROM BasicShippingList c WHERE "//
			+ "(:bslclass is null or c.bslclass LIKE %:bslclass%) and "//
			+ "(:bslsn is null or c.bslsn LIKE %:bslsn%) and "//
			+ "(:bslfuser is null or c.bslfuser LIKE %:bslfuser%) and "//
			+ "(:bslpnumber is null or c.bslpnumber LIKE %:bslpnumber%) ")
	ArrayList<BasicShippingList> findAllBySearch(String bslclass, String bslsn, String bslpnumber, String bslfuser,
			Pageable pageable);

	@Query("SELECT c FROM BasicShippingList c WHERE "//
			+ "(cast(:syscdate as date) is null or c.syscdate <= :syscdate) and sysstatus = 1") //
	ArrayList<BasicShippingList> findAllBySyscdateRemove(Date syscdate);

	@Query("SELECT c FROM BasicShippingList c WHERE "//
			+ "(:sysstatus is null or c.sysstatus = :sysstatus) "//
			+ "and (c.bslsuser = '') "//
			+ "and (c.bslsuser != 'ERP_Remove(Auto)') "//
			+ "and (COALESCE(:bslclass) is null or c.bslclass IN :bslclass) "//
			+ "order by c.bslclass asc, c.bslsn asc, c.bslnb asc")
	ArrayList<BasicShippingList> findAllByBslclass(Integer sysstatus, List<String> bslclass);

}