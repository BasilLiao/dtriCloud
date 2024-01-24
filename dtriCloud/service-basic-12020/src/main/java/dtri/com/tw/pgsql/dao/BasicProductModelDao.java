package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.BasicProductModel;

public interface BasicProductModelDao extends JpaRepository<BasicProductModel, Long> {

	@Query("SELECT c FROM BasicProductModel c WHERE "//
			+ "(:bpmname is null or c.bpmname=:bpmname) and  "//
			+ "(:bpmfeatures is null or c.bpmfeatures=:bpmfeatures)")
	ArrayList<BasicProductModel> findAllByCheck(String bpmname, String bpmfeatures, Pageable pageable);

	@Query("SELECT c FROM BasicProductModel c WHERE "//
			+ "(:bpmname is null or c.bpmname LIKE %:bpmname%) and "//
			+ "(:bpmfeatures is null or c.bpmfeatures LIKE %:bpmfeatures%)")
	ArrayList<BasicProductModel> findAllBySearch(String bpmname, String bpmfeatures, Pageable pageable);

}