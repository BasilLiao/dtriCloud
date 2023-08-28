package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.WarehouseConfig;

public interface WarehouseConfigDao extends JpaRepository<WarehouseConfig, Long> {

	// 查詢用
	@Query("SELECT c FROM WarehouseConfig c WHERE "//
			+ "(:wcalias is null or c.wcalias LIKE %:wcalias%) and "//
			+ "(:wcwkaname is null or c.wcwkaname LIKE %:wcwkaname%)") //
	ArrayList<WarehouseConfig> findAllBySearch(String wcalias, String wcwkaname, Pageable pageable);

	// 檢查用
	@Query("SELECT c FROM WarehouseConfig c WHERE "//
			+ "(:wcalias is null or c.wcalias=:wcalias) and "//
			+ "(:wcwkaname is null or c.wcwkaname=:wcwkaname) ")
	ArrayList<WarehouseConfig> findAllByCheck(String wcalias, String wcwkaname, Pageable pageable);

}