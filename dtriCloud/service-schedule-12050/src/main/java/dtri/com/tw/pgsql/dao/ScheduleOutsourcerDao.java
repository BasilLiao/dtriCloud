package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.ScheduleOutsourcer;

public interface ScheduleOutsourcerDao extends JpaRepository<ScheduleOutsourcer, Long> {

	// 查詢用(製令單號/產品品號/產品品名/產品規格/單據狀態/加工廠(代號+中文)/開單人名/加工廠開工日期/預計其料日)
	@Query("SELECT c FROM ScheduleOutsourcer c WHERE "//
			+ "(:sonb is null or c.sonb LIKE %:sonb%) and "// 單號
			+ "(:sopnb is null or c.sopnb LIKE %:sopnb%) and "// 產品品號
			+ "(:sopname is null or c.sopname LIKE %:sopname%) and "// 產品品名
			+ "(:sopspecifications is null or c.sopspecifications LIKE %:sopspecifications%) and "// 產品規格
			+ "(:sostatus is null or c.sostatus = :sostatus) and "// 單據狀態
			+ "(:sofname is null or c.sofname LIKE %:sofname%) and "// 加工廠(代號+中文)
			+ "(:souname is null or c.souname LIKE %:souname%) and "// 開單人名
			+ "(:sofodate is null or c.sofodate LIKE %:sofodate%) and "// 加工廠上線日
			+ "(:somcdates is null or c.somcdate >= :somcdates) and "// 預計齊料(起)
			+ "(:somcdatee is null or c.somcdate <= :somcdatee) and "// 預計齊料(終)
			+ "(:somcnote is null or c.somcnote LIKE %:somcnote%) and "// 物控資料
			+ "(:sysstatus is null or c.sysstatus =:sysstatus)") //
	ArrayList<ScheduleOutsourcer> findAllBySearch(String sonb, String sopnb, //
			String sopname, String sopspecifications, //
			String sostatus, String sofname, String souname, //
			String sofodate, String somcdates, String somcdatee, String somcnote, Integer sysstatus, Pageable pageable);

	// 檢查用
	@Query("SELECT c FROM ScheduleOutsourcer c WHERE "//
			+ "(:sonb is null or c.sonb=:sonb) and "//
			+ "(:sopnb is null or c.sopnb=:sopnb) ")
	ArrayList<ScheduleOutsourcer> findAllByCheck(String sonb, String sopnb, Pageable pageable);

}