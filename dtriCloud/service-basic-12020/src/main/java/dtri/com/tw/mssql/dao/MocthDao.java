package dtri.com.tw.mssql.dao;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.mssql.entity.Mocth;

public interface MocthDao extends JpaRepository<Mocth, Long> {

	// 多筆查詢範例
	@Query(value = "SELECT	"// --委外進貨
			+ "	ROW_NUMBER() OVER(order by MOCTI.TI001) AS MOCTH_ID,"//
			+ "	(MOCTI.TI001+'-'+TRIM(MOCTI.TI002)+'-'+MOCTI.TI003) AS TI001_TI002_TI003,"// --委外進貨單
			+ "	(MOCTI.TI013+'-'+TRIM(MOCTI.TI014)) AS TI013_TI014,"// --製令單
			+ "	CEILING(MOCTI.TI007) AS TI007,"// --進貨數量
			+ "	MOCTI.TI009,"// --進貨庫別
			+ "	MOCTI.TI035,"// --1.待驗,Y(2).檢驗合格,N(3).檢驗不合格,0.免檢
			+ " MOCTI.TI037,"// --確認碼 Y/N/V
			+ "	MOCTI.TI048, "// --簽核狀態碼0.待處理,1.簽核中,2.退件,3.已核准,4.取消確認中,5.作廢中,6.取消作廢中,N.不執行電子簽核[DEF:N]
			+ "	MOCTH.TH005,"// --加工廠商之代號

			+ "	INVMB.MB001, "// --品號
			+ "	INVMB.MB002, "// --品名
			+ "	INVMB.MB003, "// --規格
			+ "	INVMB.MB017, "// --主要-倉別代號
			+ "	INVMB.MB032, "// --主要-供應商代號
			+ "	INVMB.MB036, "// --主要-固定前置天數
			+ "	INVMB.MB039, "// --主要-最低補量
			+ "	INVMB.MB040, "// --主要-補貨倍量
			+ "	CMSMC.MC002, "// --主要-倉別名稱
			+ "	COALESCE(PURMA.MA002,'') AS MA002, "// --供應商名稱
			+ "	'入料類'  AS TK000 "//
			+ "FROM"//
			+ "	[DTR_TW].[dbo].MOCTH"//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].MOCTI AS MOCTI "// --委外單身
			+ "	ON (MOCTH.TH001+'-'+TRIM(MOCTH.TH002)) =(MOCTI.TI001+'-'+TRIM(MOCTI.TI002)) "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].INVMB AS INVMB "// --倉庫別
			+ "	ON MOCTI.TI004 = INVMB.MB001 "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].CMSMC AS CMSMC "// --基本資料
			+ "	ON INVMB.MB017 = CMSMC.MC001 "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].PURMA AS PURMA "// --廠商
			+ "	ON PURMA.MA001 = INVMB.MB032 "//
			+ "WHERE "//
			+ " MOCTI.TI001 is not null "//
			+ "	AND MOCTI.TI007 > 0 "// --數量不為0
			+ " AND (MOCTI.CREATE_DATE = CONVERT(VARCHAR(8), GETDATE(), 112)"//
			+ "	OR MOCTI.MODI_DATE = CONVERT(VARCHAR(8), GETDATE(), 112)) "// 今天
			+ "ORDER BY"//
			+ "	(MOCTI.TI001+'-'+TRIM(MOCTI.TI002)+'-'+MOCTI.TI003)   ASC"// --單號+序號
			, nativeQuery = true) // coalesce 回傳非NULL值
	ArrayList<Mocth> findAllByMocth();

}