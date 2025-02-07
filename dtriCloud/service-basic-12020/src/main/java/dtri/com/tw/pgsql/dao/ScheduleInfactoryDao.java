package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.ScheduleInfactory;

public interface ScheduleInfactoryDao extends JpaRepository<ScheduleInfactory, Long> {

	// 查詢用
	@Query("SELECT c FROM ScheduleInfactory c WHERE "//
			+ "(:sinb is null or c.sinb LIKE %:sinb%) and "// 製令單號
			+ "(:sipnb is null or c.sipnb LIKE %:sipnb%) and "// 產品品號
			+ "(:sipname is null or c.sipname LIKE %:sipname%) and "// 產品品名
			+ "(c.sysstatus = 0) and "// 正常=0/移除標記=2
			+ "(:sistatus is null or c.sistatus =:sistatus)") // 狀態: 0=暫停中/1=未生產/2=已發料/3=生產中_Y=已完工/y=指定完工
	ArrayList<ScheduleInfactory> findAllBySearch(String sinb, String sipnb, String sipname, String sistatus,
			Pageable pageable);

	// 檢查用
	@Query("SELECT c FROM ScheduleInfactory c WHERE "//
			+ "(:sinb is null or c.sinb =:sinb) and "// 製令單號
			+ "(:sipnb is null or c.sipnb =:sipnb) and "// 產品品號
			+ "(:sipname is null or c.sipname =:sipname) and "// 產品品名
			+ "(c.sysstatus = 0) and "// 正常=0/移除標記=2
			+ "(:sistatus is null or c.sistatus =:sistatus)") // 狀態: 0=暫停中/1=未生產/2=已發料/3=生產中_Y=已完工/y=指定完工
	ArrayList<ScheduleInfactory> findAllByCheck(String sinb, String sipnb, String sipname, String sistatus,
			Pageable pageable);

	// 檢查用// 狀態:0=暫停中/1=未生產/2=已發料/3=生產中_Y=已完工/y=指定完工
	@Query("SELECT c FROM ScheduleInfactory c WHERE "//
			+ "(c.sysstatus = 0) and "// 正常=0/移除標記=2
			+ "(c.sistatus ='0' or c.sistatus ='1' or c.sistatus ='2' or c.sistatus ='3')")
	ArrayList<ScheduleInfactory> findAllByNotFinish(Pageable pageable);

	// 檢查用// 狀態:0=暫停中/1=未生產/2=已發料/3=生產中_Y=已完工/y=指定完工
	@Query("SELECT c FROM ScheduleInfactory c WHERE "//
			+ "(c.sysstatus = 2) and "// 正常=0/移除標記=2
			+ "(:sinb is null or c.sinb =:sinb)")
	ArrayList<ScheduleInfactory> findAllByFinish(String sinb, Pageable pageable);

}