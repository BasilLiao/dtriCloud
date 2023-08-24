package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;

import dtri.com.tw.pgsql.entity.WarehouseArea;

public interface WarehouseAreaDao extends JpaRepository<WarehouseArea, Long> {

	// 物料號
	ArrayList<WarehouseArea> findAllByWawmpnb(String wawmpnb);

	// 物料號+倉儲
	ArrayList<WarehouseArea> findAllByWawmpnbalias(String wawmpnb);

	// 物料號+物料不為0
	ArrayList<WarehouseArea> findAllByWawmpnbAndWaerptqtyNot(String wawmpnb, Integer waerptqty);

	// 物料為0
	ArrayList<WarehouseArea> findAllByWaerptqty(Integer waerptqty);

}