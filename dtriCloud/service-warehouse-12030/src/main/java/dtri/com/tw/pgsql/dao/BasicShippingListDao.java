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
			+ "(:bslclass is null or c.bslclass=:bslclass) and "//
			+ "(:bslsn is null or c.bslsn=:bslsn) and "//
			+ "(:bslpnumber is null or c.bslpnumber=:bslpnumber) ")
	ArrayList<BasicShippingList> findAllBySearch(String bslclass, String bslsn, String bslpnumber, Pageable pageable);

}