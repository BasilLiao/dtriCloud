package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.BiosPrincipal;

public interface BiosPrincipalDao extends JpaRepository<BiosPrincipal, Long> {

	// 查詢用
	@Query("SELECT c FROM BiosPrincipal c WHERE "//
			+ "(:bpsuname is null or c.bpsuname LIKE %:bpsuname%) and "// 主要負責人
			+ "(:bpbvmodel is null or c.bpbvmodel LIKE %:bpbvmodel%) and "//
			+ "(:sysstatus is null or c.sysstatus =:sysstatus)") //
	ArrayList<BiosPrincipal> findAllBySearch(String bpsuname, String bpbvmodel, Integer sysstatus, Pageable pageable);

	// 查詢用(Model 反向比對)
	@Query("SELECT c FROM BiosPrincipal c WHERE "// 僅機種別 串聯%%
			+ "(:bpbvmodel is null or :bpbvmodel LIKE  CONCAT('%',c.bpbvmodel, '%' )) ") //
	ArrayList<BiosPrincipal> findAllBySearch(String bpbvmodel);

	// 檢查用
	@Query("SELECT c FROM BiosPrincipal c WHERE "//
			+ "(:bpsuid is null or c.bpsuid=:bpsuid) and "//
			+ "(:bpbvmodel is null or c.bpbvmodel=:bpbvmodel) ")
	ArrayList<BiosPrincipal> findAllByCheck(Long bpsuid, String bpbvmodel, Pageable pageable);

}