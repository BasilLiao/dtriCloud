package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.ScheduleOutsourcer;

public interface ScheduleOutsourcerDao extends JpaRepository<ScheduleOutsourcer, Long> {

	// 查詢用
	@Query("SELECT c FROM ScheduleOutsourcer c WHERE "//
			+ "(:sonb is null or c.sonb LIKE %:sonb%) and "// 製令單號
			+ "(:sopnb is null or c.sopnb LIKE %:sopnb%) and "// 產品品號
			+ "(:sopname is null or c.sopname LIKE %:sopname%) and "// 產品品名
			+ "(c.sysstatus = 0) and "// 正常=0/移除標記=2
			+ "(:sostatus is null or c.sostatus =:sostatus)") // 狀態: 0=暫停中/1=未生產/2=已發料/3=生產中_Y=已完工/y=指定完工
	ArrayList<ScheduleOutsourcer> findAllBySearch(String sonb, String sopnb, String sopname, String sostatus,
			Pageable pageable);

	// 檢查用
	@Query("SELECT c FROM ScheduleOutsourcer c WHERE "//
			+ "(:sonb is null or c.sonb =:sonb) and "// 製令單號
			+ "(:sopnb is null or c.sopnb =:sopnb) and "// 產品品號
			+ "(:sopname is null or c.sopname =:sopname) and "// 產品品名
			+ "(c.sysstatus = 0) and "// 正常=0/移除標記=2
			+ "(:sostatus is null or c.sostatus =:sostatus)") // 狀態: 0=暫停中/1=未生產/2=已發料/3=生產中_Y=已完工/y=指定完工
	ArrayList<ScheduleOutsourcer> findAllByCheck(String sonb, String sopnb, String sopname, String sostatus,
			Pageable pageable);

	// 檢查用// 狀態:0=暫停中/1=未生產/2=已發料/3=生產中_Y=已完工/y=指定完工
	@Query("SELECT c FROM ScheduleOutsourcer c WHERE "//
			+ "(c.sysstatus = 0) and "// 正常=0/移除標記=2
			+ "(c.sostatus ='0' or c.sostatus ='1' or c.sostatus ='2' or c.sostatus ='3')")
	ArrayList<ScheduleOutsourcer> findAllByNotFinish(Pageable pageable);

}