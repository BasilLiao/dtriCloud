package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.WarehouseShortageList;

public interface WarehouseShortageListDao extends JpaRepository<WarehouseShortageList, Long> {

	// 查詢用
	@Query("SELECT c FROM WarehouseShortageList c WHERE "//
			+ "(:wslbslsnnb is null or c.wslbslsnnb LIKE %:wslbslsnnb%) and "//
			+ "(:wslpnumber is null or c.wslpnumber LIKE %:wslpnumber%) and "//
			+ "(c.wslfuser != 'ERP_Remove(Auto)') and "//
			+ "(:sysstatus is null or c.sysstatus =:sysstatus)") //
	ArrayList<WarehouseShortageList> findAllBySearch(String wslbslsnnb, String wslpnumber, Integer sysstatus, Pageable pageable);

	// 檢查用
	@Query("SELECT c FROM WarehouseShortageList c WHERE "//
			+ "(:wslbslsnnb is null or c.wslbslsnnb=:wslbslsnnb) and "//
			+ "(:wslpnumber is null or c.wslpnumber=:wslpnumber) ")
	ArrayList<WarehouseShortageList> findAllByCheck(String wslbslsnnb, String wslpnumber, Pageable pageable);

}