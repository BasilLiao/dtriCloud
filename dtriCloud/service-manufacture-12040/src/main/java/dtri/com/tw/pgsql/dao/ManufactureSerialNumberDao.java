package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.ManufactureSerialNumber;

public interface ManufactureSerialNumberDao extends JpaRepository<ManufactureSerialNumber, Long> {

	// 查詢用
	@Query("SELECT c FROM ManufactureSerialNumber c WHERE "//
			+ "(:msnssn is null or (c.msnesn >= :msnssn  and c.msnssn <= :msnssn)) and "// 區間查詢
			+ "(:msnwo is null or c.msnwo LIKE %:msnwo%) and "// 工單號
			+ "(:msnmodel is null or c.msnmodel LIKE %:msnmodel%)") // 產品型號
	ArrayList<ManufactureSerialNumber> findAllBySearch(String msnssn, String msnwo, String msnmodel, Pageable pageable);

	// 查詢用
	@Query("SELECT c FROM ManufactureSerialNumber c WHERE "//
			+ "(:msnesn is null or c.msnesn LIKE %:msnesn% )" //
			+ "Order by c.msnesn desc") // 區間查詢
	ArrayList<ManufactureSerialNumber> findAllBySn(String msnesn);

	// 檢查用
	@Query("SELECT c FROM ManufactureSerialNumber c WHERE "//
			+ "((:msnssn IS NULL OR :msnesn IS NULL) OR ( c.msnssn <= :msnesn AND c.msnesn >= :msnssn )) and"// (結束/開始)包含查詢
			+ "(:msnwo is null or c.msnwo =  :msnwo) and "// 工單號
			+ "(:msnmodel is null or c.msnmodel = :msnmodel)") // 產品型號
	ArrayList<ManufactureSerialNumber> findAllByCheck(String msnssn, String msnesn, String msnwo, String msnmodel);

}