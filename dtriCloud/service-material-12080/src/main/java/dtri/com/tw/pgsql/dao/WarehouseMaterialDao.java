package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.dto.WarehouseMaterialDto;
import dtri.com.tw.pgsql.entity.WarehouseMaterial;

public interface WarehouseMaterialDao extends JpaRepository<WarehouseMaterial, Long> {

	// 關聯物料
	ArrayList<WarehouseMaterial> findAllByWmpnb(String wmpnb);

	// 查詢用
	@Query("SELECT c FROM WarehouseMaterial c WHERE "//
			+ "(:wmpnb is null or c.wmpnb LIKE %:wmpnb%) and "//
			+ "(:wmname is null or c.wmname LIKE %:wmname%) and "//
			+ "(:wmspecification is null or c.wmspecification LIKE %:wmspecification%) ")
	ArrayList<WarehouseMaterial> findAllBySearch(String wmpnb, String wmname, String wmspecification,
			Pageable pageable);

	// 檢查用
	@Query("SELECT c FROM WarehouseMaterial c WHERE "//
			+ "(:wmpnb is null or c.wmpnb=:wmpnb) and "//
			+ "(:wmname is null or c.wmname=:wmname) and "//
			+ "(:wmspecification is null or c.wmspecification=:wmspecification) ")
	ArrayList<WarehouseMaterial> findAllByCheck(String wmpnb, String wmname, String wmspecification, Pageable pageable);

	/**
	 * 查詢所有「啟用中」的物料 (投影查詢)
	 * 用途: 系統初始化時，一次撈取所有料號給前端做快取 (Client-side Cache)
	 * * @return List<WarehouseMaterialDto> 只包含 wmpnb 和 wmname
	 */
	@Query("SELECT w.wmpnb as wmpnb, w.wmname as wmname " +
			"FROM WarehouseMaterial w " +
			"ORDER BY w.wmpnb ASC")
	List<WarehouseMaterialDto> findAllActiveMaterials();
}