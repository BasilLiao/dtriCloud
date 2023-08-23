package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;

import dtri.com.tw.pgsql.entity.WarehouseMaterial;

public interface WarehouseMaterialDao extends JpaRepository<WarehouseMaterial, Long> {

	// 關聯物料
	ArrayList<WarehouseMaterial> findAllByWmpnb(String wmpnb);

}