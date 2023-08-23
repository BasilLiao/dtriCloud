package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.BasicIncomingList;

public interface BasicIncomingListDao extends JpaRepository<BasicIncomingList, Long> {

	@Query("SELECT c FROM BasicIncomingList c WHERE "//
			+ "(:sysstatus is null or c.sysstatus=:sysstatus) "//
			+ "order by c.bilclass asc, c.bilpnumber asc")
	ArrayList<BasicIncomingList> findAllByStatus(Integer sysstatus);

}