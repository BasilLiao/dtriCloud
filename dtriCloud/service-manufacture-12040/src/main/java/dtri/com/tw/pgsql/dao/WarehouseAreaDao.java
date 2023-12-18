package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.WarehouseArea;

public interface WarehouseAreaDao extends JpaRepository<WarehouseArea, Long> {

	// 物料號
	ArrayList<WarehouseArea> findAllByWawmpnb(String wawmpnb);

	// 倉儲+物料號
	ArrayList<WarehouseArea> findAllByWaaliasawmpnb(String wawmpnb);

	// 物料號+物料不為0
	ArrayList<WarehouseArea> findAllByWawmpnbAndWaerptqtyNot(String wawmpnb, Integer waerptqty);

	// 物料為0
	ArrayList<WarehouseArea> findAllByWaerptqty(Integer waerptqty);

	// 查詢用
	@Query("SELECT c FROM WarehouseArea c WHERE "//
			+ "(:wawmpnb is null or c.wawmpnb LIKE %:wawmpnb%) and "//
			+ "(:waslocation is null or c.waslocation LIKE %:waslocation%) and "//
			+ "(:waalias is null or c.waalias LIKE %:waalias%)") //
	ArrayList<WarehouseArea> findAllBySearch(String wawmpnb, String waslocation, String waalias, Pageable pageable);

	// 檢查用
	@Query("SELECT c FROM WarehouseArea c WHERE "//
			+ "(:wawmpnb is null or c.wawmpnb=:wawmpnb) and "//
			+ "(:waslocation is null or c.waslocation=:waslocation) and "//
			+ "(:waalias is null or c.waalias=:waalias) ")
	ArrayList<WarehouseArea> findAllByCheck(String wawmpnb, String waslocation, String waalias);

}