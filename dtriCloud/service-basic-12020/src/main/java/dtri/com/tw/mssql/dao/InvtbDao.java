package dtri.com.tw.mssql.dao;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.mssql.entity.Invtb;

public interface InvtbDao extends JpaRepository<Invtb, Long> {

	// 多筆查詢範例
	@Query(value = "SELECT	"// --倉儲
			+ "	TRIM(INVMB.MB001)+'_'+TRIM(ISNULL(INVMC.MC002,''))+'_'+TRIM(ISNULL(INVMC.MC003, '')) AS INVTB_ID,"//
			+ "	INVMB.MB001, "// --品號
			+ "	INVMB.MB002, "// --品名
			+ "	INVMB.MB003, "// --規格
			+ "	INVMB.MB009, "// --商品描述
			+ "	ISNULL(INVMC.MC002,'') AS MC002, "// --倉別代號
			+ "	CMSMC.MC002 AS CMC002, "// --倉別名稱
			+ "	ISNULL(INVMC.MC003,'') AS MC003, "// --儲位位置
			+ "	FLOOR(INVMC.MC007) AS MC007, "// --儲位數量
			+ "	INVMB.MB017, "// --主要-倉別代號
			+ "	INVMB.MB032, "// --主要-供應商代號
			+ "	INVMB.MB036, "// --主要-固定前置天數
			+ "	INVMB.MB039, "// --主要-最低補量
			+ "	INVMB.MB040, "// --主要-補貨倍量
			+ "	COALESCE(PURMA.MA002,'') AS MA002 "// --供應商名稱
			+ "FROM "//
			+ "	[DTR_TW].[dbo].INVMB  "// --倉庫別
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].INVMC AS INVMC "// --品號庫別檔
			+ "	ON INVMB.MB001 = INVMC.MC001 "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].CMSMC AS CMSMC "// --基本資料
			+ "	ON INVMC.MC002 = CMSMC.MC001 "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].PURMA AS PURMA "// --廠商
			+ "	ON PURMA.MA001 = INVMB.MB032 "//
			// + "WHERE "//
			// + " INVMC.MC007 is not null "//
			// + " AND (INVMC.MC007 >=1 OR INVMC.MC007 =0) "//
			+ "ORDER BY " //
			+ "	INVMB.MB001 ASC,"//
			+ "	INVMC.MC002 ASC,"//
			+ "	INVMC.MC003 ASC "// --單號+序號
			, nativeQuery = true) // coalesce 回傳非NULL值
	ArrayList<Invtb> findAllByMoctb();

}