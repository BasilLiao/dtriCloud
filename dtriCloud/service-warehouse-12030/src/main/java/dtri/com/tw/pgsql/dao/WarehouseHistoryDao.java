package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.WarehouseHistory;

public interface WarehouseHistoryDao extends JpaRepository<WarehouseHistory, Long> {

	// 查詢用
	@Query("SELECT c FROM WarehouseHistory c WHERE "//
			+ "(:whwmpnb is null or c.whwmpnb LIKE %:whwmpnb%) and "//
			+ "(:whcontent is null or c.whcontent LIKE %:whcontent%)") //
	ArrayList<WarehouseHistory> findAllBySearch(String whwmpnb, String whcontent, Pageable pageable);

	// 檢查用
	@Query("SELECT c FROM WarehouseHistory c WHERE "//
			+ "(:whwmpnb is null or c.whwmpnb=:whwmpnb) and "//
			+ "(:whcontent is null or c.whcontent=:whcontent) ")
	ArrayList<WarehouseHistory> findAllByCheck(String whwmpnb, String whcontent, Pageable pageable);

}