package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;
import java.util.Date;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.BasicCommandList;

public interface BasicCommandListDao extends JpaRepository<BasicCommandList, Long> {

	@Query("SELECT c FROM BasicCommandList c WHERE "//
			+ "(:bclsn is null or c.bclsn=:bclsn) and "//
			+ "(:bclclass is null or c.bclclass=:bclclass) and "//
			+ "(:bclpnumber is null or c.bclpnumber=:bclpnumber) and "//
			+ "(:checksum is null or c.checksum=:checksum) "//
			+ "order by c.bclclass asc, c.bclpnumber asc")
	ArrayList<BasicCommandList> findAllByComList(String bclclass, String bclsn, String bclpnumber, String checksum);

	@Query("SELECT c FROM BasicCommandList c WHERE "//
			+ "(:sysstatus is null or c.sysstatus=:sysstatus) "//
			+ "order by c.bclclass asc, c.bclpnumber asc")
	ArrayList<BasicCommandList> findAllByStatus(Integer sysstatus);

	@Query("SELECT c FROM BasicCommandList c WHERE "//
			+ "(:bclclass is null or c.bclclass LIKE %:bclclass%) and "//
			+ "(:bclsn is null or c.bclsn LIKE %:bclsn%) and "//
			+ "(:sysstatus is null or c.sysstatus != :sysstatus) and "//
			+ "(:bclpnumber is null or c.bclpnumber LIKE %:bclpnumber% or c.bclproduct LIKE %:bclpnumber%) ")
	ArrayList<BasicCommandList> findAllBySearch(String bclclass, String bclsn, String bclpnumber,Integer sysstatus, Pageable pageable);

	@Query("SELECT c FROM BasicCommandList c WHERE "//
			+ "(:bclclass is null or c.bclclass=:bclclass) and "//
			+ "(:bclsn is null or c.bclsn=:bclsn) and "//
			+ "(:bclpnumber is null or c.bclpnumber=:bclpnumber) ")
	ArrayList<BasicCommandList> findAllByCheck(String bclclass, String bclsn, String bclpnumber, Pageable pageable);

	@Query("SELECT c FROM BasicCommandList c WHERE "//
			+ "(cast(:syscdate as date) is null or c.syscdate <= :syscdate)") //
	ArrayList<BasicCommandList> findAllBySyscdateRemove(Date syscdate);
}