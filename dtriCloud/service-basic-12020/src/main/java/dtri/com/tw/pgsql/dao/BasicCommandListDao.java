package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

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

}