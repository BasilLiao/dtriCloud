package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.BiosVersion;

public interface BiosVersionDao extends JpaRepository<BiosVersion, Long> {

	// 查詢用
	@Query("SELECT c FROM BiosVersion c WHERE "//
			+ "(:bvcpugenerations is null or c.bvcpugenerations LIKE %:bvcpugenerations%) and "//
			+ "(:bvmodel is null or c.bvmodel LIKE %:bvmodel%) and "//
			+ "(:bvcname is null or c.bvcname LIKE %:bvcname%) and "//
			+ "(:bvversion is null or c.bvversion LIKE %:bvversion% ) and "//
			+ "(:sysstatus is null or c.sysstatus =:sysstatus)") //
	ArrayList<BiosVersion> findAllBySearch(String bvcpugenerations, String bvmodel, String bvcname, String bvversion,
			Integer sysstatus, Pageable pageable);

	// 查詢用
	@Query("SELECT c FROM BiosVersion c WHERE "//
			+ "(:bvcpugenerations is null or c.bvcpugenerations LIKE %:bvcpugenerations%) and "//
			+ "(:bvmodel is null or c.bvmodel LIKE %:bvmodel%) and "//
			+ "(c.bvcname ='')") //
	ArrayList<BiosVersion> findAllByOnlyDefSearch(String bvcpugenerations, String bvmodel, Pageable pageable);

	// 查詢用
	@Query("SELECT c FROM BiosVersion c WHERE "//
			+ "(:bvoversion is null or c.bvoversion LIKE %:bvoversion%) and "//
			+ "(:sysstatus is null or c.sysstatus =:sysstatus)") //
	ArrayList<BiosVersion> findAllByCheckOld(String bvoversion, Integer sysstatus, Pageable pageable);

	// 檢查用
	@Query("SELECT c FROM BiosVersion c WHERE "//
			+ "(:bvversion is null or c.bvversion=:bvversion) and "//
			+ "(:bvmodel is null or c.bvmodel=:bvmodel) ")
	ArrayList<BiosVersion> findAllByCheck(String bvversion, String bvmodel, Pageable pageable);

}