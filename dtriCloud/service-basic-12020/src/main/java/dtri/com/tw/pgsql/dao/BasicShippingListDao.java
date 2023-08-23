package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.BasicShippingList;

public interface BasicShippingListDao extends JpaRepository<BasicShippingList, Long> {

	@Query("SELECT c FROM BasicShippingList c WHERE "//
			+ "(:sysstatus is null or c.sysstatus=:sysstatus) "//
			+ "order by c.bslclass asc, c.bslpnumber asc")
	ArrayList<BasicShippingList> findAllByStatus(Integer sysstatus);

}