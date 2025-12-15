package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;
import java.util.Date;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.WarehouseInventory;

public interface WarehouseInventoryDao extends JpaRepository<WarehouseInventory, Long> {

	@Query("SELECT c FROM WarehouseInventory c WHERE "//
			+ "(:wiwmpnb is null or c.wiwmpnb Like %:wiwmpnb%) and "// 物料號
			+ "(:wiwaslocation is null or c.wiwaslocation Like %:wiwaslocation%) and "// 物料儲位
			+ "(:wiwaalias is null or c.wiwaalias Like %:wiwaalias%) and "// 物料倉別
			+ "(:wiwaaliasnb is null or c.wiwaaliasnb Like %:wiwaaliasnb%) and "// 物料倉別_料號
			+ "(cast(:ssyscdate as date) is null or c.widate >= :ssyscdate) and " // 盤點時間
			+ "(cast(:esyscdate as date) is null or c.widate <= :esyscdate) and " // 盤點時間
			+ "(:wicuser is null or c.wicuser Like %:wicuser% ) and "// 盤點人
			+ "(:wicheck is null or c.wicheck=:wicheck) ") // 確認勾選
	ArrayList<WarehouseInventory> findAllBySearch(String wiwmpnb, String wiwaslocation, String wiwaalias,
			String wiwaaliasnb, Date ssyscdate, Date esyscdate, String wicuser, Boolean wicheck, Pageable pageable);

	@Query("SELECT c FROM WarehouseInventory c WHERE "//
			+ "(:wiwmpnb is null or c.wiwmpnb=:wiwmpnb) and "// 物料號
			+ "(:wiwaslocation is null or c.wiwaslocation=:wiwaslocation) and "// 物料儲位
			+ "(:wiwaalias is null or c.wiwaalias=:wiwaalias) and "// 物料倉別
			+ "(:wicuser is null or c.wicuser=:wicuser) and "// 盤點人
			+ "(:wicheck is null or c.wicheck=:wicheck) ") // 確認勾選
	ArrayList<WarehouseInventory> findAllByCheck(String wiwmpnb, String wiwaslocation, String wiwaalias, String wicuser,
			Boolean wicheck, Pageable pageable);

}