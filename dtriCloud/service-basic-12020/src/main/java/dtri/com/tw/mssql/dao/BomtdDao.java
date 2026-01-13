package dtri.com.tw.mssql.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.mssql.entity.Bomtd;

public interface BomtdDao extends JpaRepository<Bomtd, Long> {

	// 多筆查詢範例
	@Query(value = "SELECT "// --OK 組合單/A421
			+ "	(TRIM(BOMTE.TE001)+'-'+TRIM(BOMTE.TE002)+'-'+TRIM(BOMTE.TE003)) AS BOMTD_ID,"//
			+ "	(TRIM(BOMTE.TE001)+'-'+TRIM(BOMTE.TE002)+'-'+TRIM(BOMTE.TE003)) as TE001_TE002_TE003,"// --單號
			+ "	BOMTE.TE004,"// --(-)元件號
			+ "	BOMTE.TE007,"// --(-)出庫
			+ "	BOMTE.TE008,"// --(-)元件數量
			+ "	BOMTE.TE010,"// --(-)確認碼 Y/N/V
			+ "	BOMTD.TD004,"// --(+)成品號
			+ "	BOMTD.TD007,"// --(+)成品數量
			+ "	BOMTD.TD010,"// --(+)入庫
			+ "	BOMTD.TD016,"// --(+)簽核碼0.待處理,1.簽核中,2.退件,3.已核准,4.取消確認中,5.作廢中,6.取消作廢中,N.不執行電子簽核[DEF:N]
			+ "	"//
			+ "	INVMB.MB001,"// --品號
			+ "	INVMB.MB002,"// --品名
			+ "	INVMB.MB003,"// --規格
			+ "	INVMB.MB017,"// --主要-倉別代號
			+ "	INVMB.MB032,"// --供應商代號
			+ "	INVMB.MB036,"// --固定前置天數
			+ "	INVMB.MB039,"// --最低補量
			+ "	INVMB.MB040,"// --補貨倍量
			+ "	CMSMC.MC002,"// --主要-倉別名稱
			+ "	COALESCE(PURMA.MA002,'') AS MA002, "// --供應商名稱
			+ "	'領料類'  AS TK000,"//
			+ "	BOMTE.CREATE_DATE,"// --建立單據時間
			+ "	BOMTE.MODI_DATE,"// --修改單據時間
			+ "	BOMTE.CREATOR"// --建立單據者
			+ " FROM "//
			+ "	[DTR_TW].[dbo].BOMTD"//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].BOMTE AS BOMTE "// ---組合單據單身檔
			+ "	ON (BOMTD.TD001+'-'+TRIM(BOMTD.TD002)) =(BOMTE.TE001+'-'+TRIM(BOMTE.TE002))"//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].INVMB AS INVMB "// --(+)倉庫別
			+ "	ON BOMTD.TD004 = INVMB.MB001 "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].CMSMC AS CMSMC "// --基本資料
			+ "	ON INVMB.MB017 = CMSMC.MC001 "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].PURMA AS PURMA "// --廠商
			+ "	ON PURMA.MA001 = INVMB.MB032 "//
			+ " WHERE "//
			+ "	BOMTE.TE001 is not null "//
			+ "	AND BOMTE.TE008 > 0 "//
			+ " AND BOMTE.TE001='A421' "//
			+ "	AND (BOMTE.CREATE_DATE >= CONVERT(VARCHAR(8), GETDATE()-10, 112) "//
			+ "	OR BOMTE.MODI_DATE = CONVERT(VARCHAR(8), GETDATE(), 112))"//
			+ " ORDER BY "//
			+ "	(BOMTE.TE001+'-'+TRIM(BOMTE.TE002)+'-'+BOMTE.TE003)  ASC"// --單號+序號
			, nativeQuery = true) // coalesce 回傳非NULL值
	ArrayList<Bomtd> findAllByBomtd();

	// 多筆查詢範例
	@Query(value = "SELECT "// --OK 組合單/A421
			+ "	(TRIM(BOMTE.TE001)+'-'+TRIM(BOMTE.TE002)+'-'+TRIM(BOMTE.TE003)) AS BOMTD_ID,"//
			+ "	(TRIM(BOMTE.TE001)+'-'+TRIM(BOMTE.TE002)+'-'+TRIM(BOMTE.TE003)) as TE001_TE002_TE003,"// --單號
			+ "	BOMTE.TE004,"// --(-)元件號
			+ "	BOMTE.TE007,"// --(-)出庫
			+ "	BOMTE.TE008,"// --(-)元件數量
			+ "	BOMTE.TE010,"// --(-)確認碼 Y/N/V
			+ "	BOMTD.TD004,"// --(+)成品號
			+ "	BOMTD.TD007,"// --(+)成品數量
			+ "	BOMTD.TD010,"// --(+)入庫
			+ "	BOMTD.TD016,"// --(+)簽核碼0.待處理,1.簽核中,2.退件,3.已核准,4.取消確認中,5.作廢中,6.取消作廢中,N.不執行電子簽核[DEF:N]
			+ "	"//
			+ "	INVMB.MB001,"// --品號
			+ "	INVMB.MB002,"// --品名
			+ "	INVMB.MB003,"// --規格
			+ "	INVMB.MB017,"// --主要-倉別代號
			+ "	INVMB.MB032,"// --供應商代號
			+ "	INVMB.MB036,"// --固定前置天數
			+ "	INVMB.MB039,"// --最低補量
			+ "	INVMB.MB040,"// --補貨倍量
			+ "	CMSMC.MC002,"// --主要-倉別名稱
			+ "	COALESCE(PURMA.MA002,'') AS MA002, "// --供應商名稱
			+ "	'領料類'  AS TK000,"//
			+ "	BOMTE.CREATE_DATE,"// --建立單據時間
			+ "	BOMTE.MODI_DATE,"// --修改單據時間
			+ "	BOMTE.CREATOR"// --建立單據者
			+ " FROM "//
			+ "	[DTR_TW].[dbo].BOMTD"//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].BOMTE AS BOMTE "// ---組合單據單身檔
			+ "	ON (BOMTD.TD001+'-'+TRIM(BOMTD.TD002)) =(BOMTE.TE001+'-'+TRIM(BOMTE.TE002))"//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].INVMB AS INVMB "// --(+)倉庫別
			+ "	ON BOMTD.TD004 = INVMB.MB001 "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].CMSMC AS CMSMC "// --基本資料
			+ "	ON INVMB.MB017 = CMSMC.MC001 "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].PURMA AS PURMA "// --廠商
			+ "	ON PURMA.MA001 = INVMB.MB032 "//
			+ " WHERE "//
			+ "	BOMTE.TE001 is not null "//
			+ "	AND BOMTE.TE008 > 0 "//
			+ " AND BOMTE.TE010 !='V' "//
			+ " AND BOMTE.TE001='A421' "//
			+ "	AND (BOMTE.CREATE_DATE >= CONVERT(VARCHAR(8), GETDATE()-60, 112) "//
			+ "	OR BOMTE.MODI_DATE = CONVERT(VARCHAR(8), GETDATE(), 112))"//
			+ " AND (CONCAT(BOMTE.TE001, '-', TRIM(BOMTE.TE002), '-', BOMTE.TE003) IN (:TE001TE002TE003)) "// 比對製令單+序號?
			+ " ORDER BY "//
			+ "	(BOMTE.TE001+'-'+TRIM(BOMTE.TE002)+'-'+BOMTE.TE003)  ASC"// --單號+序號
			, nativeQuery = true) // coalesce 回傳非NULL值
	ArrayList<Bomtd> findAllByBomtd60(List<String> TE001TE002TE003);
}