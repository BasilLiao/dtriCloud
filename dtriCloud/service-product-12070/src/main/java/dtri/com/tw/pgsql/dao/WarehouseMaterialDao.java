package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.WarehouseMaterial;

public interface WarehouseMaterialDao extends JpaRepository<WarehouseMaterial, Long> {

	// 關聯物料
	ArrayList<WarehouseMaterial> findAllByWmpnb(String wmpnb);

	// 查詢用
	@Query("SELECT c FROM WarehouseMaterial c WHERE "//
			+ "(:wmpnb is null or c.wmpnb LIKE %:wmpnb%) and "//
			+ "(:wmname is null or c.wmname Not LIKE %:wmname%) and "//
			+ "(:wmspecification is null or c.wmspecification LIKE %:wmspecification%) ")
	ArrayList<WarehouseMaterial> findAllBySearch(String wmpnb, String wmname, String wmspecification,
			Pageable pageable);

	// 檢查用
	@Query("SELECT c FROM WarehouseMaterial c WHERE "//
			+ "(:wmpnb is null or c.wmpnb=:wmpnb) and "//
			+ "(:wmname is null or c.wmname=:wmname) and "//
			+ "(:wmspecification is null or c.wmspecification=:wmspecification) ")
	ArrayList<WarehouseMaterial> findAllByCheck(String wmpnb, String wmname, String wmspecification, Pageable pageable);

}