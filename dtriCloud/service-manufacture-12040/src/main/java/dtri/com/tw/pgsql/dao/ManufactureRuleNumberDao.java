package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.ManufactureRuleNumber;

public interface ManufactureRuleNumberDao extends JpaRepository<ManufactureRuleNumber, Long> {

	// 查詢用
	@Query("SELECT c FROM ManufactureRuleNumber c WHERE "//
			+ "(:mrngname is null or c.mrngname LIKE %:mrngname%) and "//
			+ "(:mrnname is null or c.mrnname LIKE %:mrnname%) ") //
	ArrayList<ManufactureRuleNumber> findAllBySearch(String mrngname, String mrnname, Pageable pageable);

	// 檢查用
	@Query("SELECT c FROM ManufactureRuleNumber c WHERE "//
			+ "(:mrngname is null or c.mrngname=:mrngname) and "//
			+ "(:mrnname is null or c.mrnname=:mrnname) and "//
			+ "(:mrnval is null or c.mrnval=:mrnval) ")
	ArrayList<ManufactureRuleNumber> findAllByCheck(String mrngname, String mrnname, String mrnval);

	ArrayList<ManufactureRuleNumber> findAllByMrngid(Long mrngid);

	// 取得G_ID
	@Query(value = "SELECT NEXTVAL('manufacture_rule_number_g_seq')", nativeQuery = true)
	Long getManufactureRuleNumberGSeq();

	@Modifying
	@Query(value = "CREATE SEQUENCE manufacture_rule_number_g_seq START WITH 1 INCREMENT BY 1 MINVALUE 1 CACHE 1 ", nativeQuery = true)
	void createManufactureRuleNumberGSeq();

	@Query(value = "SELECT COUNT(*)	FROM pg_class WHERE relname = 'manufacture_rule_number_g_seq' AND relkind = 'S'", nativeQuery = true)
	int checkIfSequenceExists();

}