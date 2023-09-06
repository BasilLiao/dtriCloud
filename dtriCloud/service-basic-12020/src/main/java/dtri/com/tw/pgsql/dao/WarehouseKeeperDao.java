package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.WarehouseKeeper;

public interface WarehouseKeeperDao extends JpaRepository<WarehouseKeeper, Long> {

	// 查詢用
	@Query("SELECT c FROM WarehouseKeeper c WHERE "//
			+ "(:wksuaccount is null or c.wksuaccount LIKE %:wksuaccount%) and "//
			+ "(:wkwaslocation is null or c.wkwaslocation LIKE %:wkwaslocation%)") //
	ArrayList<WarehouseKeeper> findAllBySearch(String wksuaccount, String wkwaslocation, Pageable pageable);

	// 檢查用
	@Query("SELECT c FROM WarehouseKeeper c WHERE "//
			+ "(:wksuid is null or c.wksuid=:wksuid) and "//
			+ "(:wksuaccount is null or c.wksuaccount=:wksuaccount) and "//
			+ "(:wkwaslocation is null or c.wkwaslocation=:wkwaslocation) ")
	ArrayList<WarehouseKeeper> findAllByCheck(Long wksuid, String wksuaccount, String wkwaslocation, Pageable pageable);

}