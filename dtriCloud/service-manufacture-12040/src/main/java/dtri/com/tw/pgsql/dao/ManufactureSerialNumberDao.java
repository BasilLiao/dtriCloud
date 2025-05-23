package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.ManufactureSerialNumber;

public interface ManufactureSerialNumberDao extends JpaRepository<ManufactureSerialNumber, Long> {

	// 查詢用
	@Query("SELECT c FROM ManufactureSerialNumber c WHERE "//
			+ "(:msnssn is null or (c.msnssn <= :msnssn and c.msnesn >= :msnssn)) and "// 區間查詢
			+ "(:msnwo is null or c.msnwo LIKE %:msnwo%) and "// 工單號
			+ "(:msnmodel is null or c.msnmodel LIKE %:msnmodel%)") // 產品型號
	ArrayList<ManufactureSerialNumber> findAllBySearch(String msnssn, String msnwo, String msnmodel, Pageable pageable);

	// 檢查用
	@Query("SELECT c FROM ManufactureSerialNumber c WHERE "//
			+ "(:msnssn is null or (c.msnssn <= :msnssn and c.msnesn <= :msnssn)) or "// (開始)區間查詢
			+ "(:msnesn is null or (c.msnssn <= :msnesn and c.msnesn <= :msnesn)) and "// (結束)區間查詢
			+ "(:msnwo is null or c.msnwo =  :msnwo) and "// 工單號
			+ "(:msnmodel is null or c.msnmodel = :msnmodel)") // 產品型號
	ArrayList<ManufactureSerialNumber> findAllByCheck(String msnssn,String msnesn, String msnwo, String msnmodel);

}