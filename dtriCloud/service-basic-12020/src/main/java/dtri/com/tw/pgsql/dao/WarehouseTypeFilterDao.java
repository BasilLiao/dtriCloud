package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.WarehouseTypeFilter;

public interface WarehouseTypeFilterDao extends JpaRepository<WarehouseTypeFilter, Long> {

	// 查詢用
	@Query("SELECT c FROM WarehouseTypeFilter c WHERE "//
			+ "(:wtfcode is null or c.wtfcode LIKE %:wtfcode%) and "//
			+ "(:wtfname is null or c.wtfname LIKE %:wtfname%)") //
	ArrayList<WarehouseTypeFilter> findAllBySearch(String wtfcode, String wtfname, Pageable pageable);

	// 檢查用
	@Query("SELECT c FROM WarehouseTypeFilter c WHERE "//
			+ "(:wtfcode is null or c.wtfcode=:wtfcode) and "//
			+ "(:wtfname is null or c.wtfname=:wtfname) ")
	ArrayList<WarehouseTypeFilter> findAllByCheck(String wtfcode, String wtfname, Pageable pageable);

}