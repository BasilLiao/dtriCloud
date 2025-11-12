package dtri.com.tw.mssql.dao;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.mssql.entity.Invma;

public interface InvmaDao extends JpaRepository<Invma, Long> {

	// 多筆查詢範例
	@Query(value = "SELECT"//
			+ "  (TRIM(INVMA.MA002)+ '-' + TRIM(INVMA.MA003)) AS INVMA_ID,"//
			+ "  MA001,"// --分類方式(4:生管)
			+ "  MA002,"// --產品代號
			+ "  MA003 "// --產品機種別
			+ " FROM "//
			+ "	 INVMA"// 
			+ " WHERE "//
			+ "	 MA001 = '4';"// --單號+序號
			, nativeQuery = true) // coalesce 回傳非NULL值
	ArrayList<Invma> findAllByInvma();

}