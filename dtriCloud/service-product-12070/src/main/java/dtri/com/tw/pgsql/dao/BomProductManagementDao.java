package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.BomProductManagement;

public interface BomProductManagementDao extends JpaRepository<BomProductManagement, Long> {

	// 查詢全部
	ArrayList<BomProductManagement> findAll();

	// 查詢全部含-頁數
	@Query("SELECT c FROM BomProductManagement c "//
			+ "WHERE (:bpmnb is null or c.bpmnb LIKE %:bpmnb% ) and "// BOM號
			+ "(:bpmmodel is null or c.bpmmodel LIKE %:bpmmodel% ) and "// BOM型號
			+ "(:bpmtypename is null or c.bpmtypename LIKE %:bpmtypename% ) ") // 產品歸類-名稱
	ArrayList<BomProductManagement> findAllBySearch(String bpmnb, String bpmmodel, String bpmtypename,
			Pageable pageable);

	// 檢查
	@Query("SELECT c FROM BomProductManagement c "//
			+ "WHERE (:bpmnb is null or c.bpmnb =:bpmnb ) and "//
			+ "(:bpmmodel is null or c.bpmmodel = :bpmmodel ) and "//
			+ "(:bpmtypename is null or c.bpmtypename =:bpmtypename ) ") //
	ArrayList<BomProductManagement> findAllByCheck(String bpmnb, String bpmmodel, String bpmtypename);


	// 找
	ArrayList<BomProductManagement> findAllByBpmid(Long bisgcondition);

}