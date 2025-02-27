package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.ScheduleInfactory;

public interface ScheduleInfactoryDao extends JpaRepository<ScheduleInfactory, Long> {

	// 查詢用
	// 查詢用(製令單號/產品品號/產品品名/產品規格/單據狀態/加工廠(代號+中文)/開單人名/加工廠開工日期/預計其料日)
	@Query("SELECT c FROM ScheduleInfactory c WHERE "//
			+ "(:sinb is null or c.sinb LIKE %:sinb%) and "// 單號
			+ "(:sipnb is null or c.sipnb LIKE %:sipnb%) and "// 產品品號
			+ "(:sipname is null or c.sipname LIKE %:sipname%) and "// 產品品名
			+ "(:sipspecifications is null or c.sipspecifications LIKE %:sipspecifications%) and "// 產品規格
			+ "(:sistatus is null or c.sistatus = :sistatus) and "// 單據狀態
			+ "(:sifname is null or c.sifname LIKE %:sifname%) and "// 加工廠(代號+中文)
			+ "(:siuname is null or c.siuname LIKE %:siuname%) and "// 開單人名
			+ "(:sifodate is null or c.sifodate LIKE %:sifodate%) and "// 加工廠上線日
			+ "(:simcdates is null or c.simcdate >= :simcdates) and "// 預計齊料(起)
			+ "(:simcdatee is null or c.simcdate <= :simcdatee) and "// 預計齊料(終)
			+ "(:simcnote is null or c.simcnote LIKE %:simcnote%) and "// 物控資料
			+ "(:simcstatus is null or c.simcstatus = :simcstatus) and "// 物控狀態
			+ "(:sysstatus is null or c.sysstatus = :sysstatus)") //
	ArrayList<ScheduleInfactory> findAllBySearch(String sinb, String sipnb, //
			String sipname, String sipspecifications, //
			String sistatus, String sifname, String siuname, //
			String sifodate, String simcdates, String simcdatee, String simcnote, Integer simcstatus, Integer sysstatus,
			Pageable pageable);

	// 檢查用
	@Query("SELECT c FROM ScheduleInfactory c WHERE "//
			+ "(:sinb is null or c.sinb=:sinb) and "//
			+ "(:sipnb is null or c.sipnb=:sipnb) ")
	ArrayList<ScheduleInfactory> findAllByCheck(String sinb, String sipnb, Pageable pageable);

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