package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.WarehouseArea;

public interface WarehouseAreaDao extends JpaRepository<WarehouseArea, Long> {

	// 物料號
	ArrayList<WarehouseArea> findAllByWawmpnb(String wawmpnb);

	// 物料號+倉儲
	ArrayList<WarehouseArea> findAllByWaaliasawmpnb(String wawmpnb);

	// 物料號+物料不為0
	ArrayList<WarehouseArea> findAllByWawmpnbAndWaerptqtyNot(String wawmpnb, Integer waerptqty);

	// 物料為0
	ArrayList<WarehouseArea> findAllByWaerptqty(Integer waerptqty);

	// 物料號
	@Query("SELECT c FROM WarehouseArea c WHERE "//
			+ "(:wawmpnb is null or c.wawmpnb  LIKE %:wawmpnb%) and " //
			+ "(c.waerptqty >0) ")
	ArrayList<WarehouseArea> findAllByWawmpnbNot0(String wawmpnb, Pageable pageable);

	@Query("SELECT c FROM WarehouseArea c WHERE c.wawmpnb IN :wawmpnbs AND c.waerptqty > 0")
	List<WarehouseArea> findAllByWawmpnbInAndWaerptqtyGreaterThanZero(List<String> wawmpnbs);

}