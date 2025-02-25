package dtri.com.tw.mssql.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.mssql.entity.Bomtf;

public interface BomtfDao extends JpaRepository<Bomtf, Long> {

	// 多筆查詢範例
	@Query(value = "SELECT"// --OK 拆解單/A431
			+ "	ROW_NUMBER() OVER(order by BOMTF.TF001) AS BOMTF_ID,"//
			+ "	(BOMTG.TG001+'-'+TRIM(BOMTG.TG002)+'-'+BOMTG.TG003) as TG001_TG002_TG003,"// --單號
			+ "	BOMTF.TF004,"// --(-)成品號
			+ "	BOMTF.TF007,"// --(-)成品數量
			+ "	BOMTF.TF008,"// --(-)出庫
			+ "	BOMTF.TF014,"// --(-)簽核碼0.待處理,1.簽核中,2.退件,3.已核准,4.取消確認中,5.作廢中,6.取消作廢中,N.不執行電子簽核[DEF:N]
			+ "	BOMTG.TG004,"// --(+)元件號
			+ "	BOMTG.TG007,"// --(+)入庫
			+ "	BOMTG.TG008,"// --(+)元件數量
			+ "	BOMTG.TG010,"// --(+)確認碼 Y/N/V
			+ "	INVMB.MB001,"// --品號
			+ "	INVMB.MB002,"// --品名
			+ "	INVMB.MB003,"// --規格
			+ "	INVMB.MB017,"// --主要-倉別代號
			+ "	INVMB.MB032,"// --供應商代號
			+ "	INVMB.MB036,"// --固定前置天數
			+ "	INVMB.MB039,"// --最低補量
			+ "	INVMB.MB040,"// --補貨倍量
			+ "	CMSMC.MC002,"// --主要-倉別名稱
			+ "	COALESCE(PURMA.MA002,'') AS MA002,"// --供應商名稱
			+ "	'領料類'  AS TK000,"//
			+ "	BOMTG.CREATE_DATE,"// --建立單據時間
			+ "	BOMTG.MODI_DATE,"// --修改單據時間
			+ "	BOMTG.CREATOR"// --建立單據者
			+ " FROM "//
			+ "	[DTR_TW].[dbo].BOMTF"//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].BOMTG AS BOMTG "// --組合單據單身檔
			+ "	ON (BOMTF.TF001+'-'+TRIM(BOMTF.TF002)) =(BOMTG.TG001+'-'+TRIM(BOMTG.TG002))"//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].INVMB AS INVMB "// --(+)倉庫別
			+ "	ON BOMTG.TG004 = INVMB.MB001 "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].CMSMC AS CMSMC "// --基本資料
			+ "	ON INVMB.MB017 = CMSMC.MC001 "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].PURMA AS PURMA "// --廠商
			+ "	ON PURMA.MA001 = INVMB.MB032 "//
			+ " WHERE"//
			+ "	BOMTG.TG001 is not null "//
			+ "	AND BOMTG.TG008 > 0 "//
			+ " AND BOMTG.TG001 = 'A431' "//
			+ "	AND (BOMTG.CREATE_DATE >= CONVERT(VARCHAR(8), GETDATE()-10, 112) "//
			+ "	OR BOMTG.MODI_DATE = CONVERT(VARCHAR(8), GETDATE(), 112))"//
			+ " ORDER BY"//
			+ "	(BOMTG.TG001+'-'+TRIM(BOMTG.TG002)+'-'+BOMTG.TG003)  ASC"// --單號+序號
			, nativeQuery = true) // coalesce 回傳非NULL值
	ArrayList<Bomtf> findAllByBomtf();
	// 多筆查詢範例
	@Query(value = "SELECT"// --OK 拆解單/A431
			+ "	ROW_NUMBER() OVER(order by BOMTF.TF001) AS BOMTF_ID,"//
			+ "	(BOMTG.TG001+'-'+TRIM(BOMTG.TG002)+'-'+BOMTG.TG003) as TG001_TG002_TG003,"// --單號
			+ "	BOMTF.TF004,"// --(-)成品號
			+ "	BOMTF.TF007,"// --(-)成品數量
			+ "	BOMTF.TF008,"// --(-)出庫
			+ "	BOMTF.TF014,"// --(-)簽核碼0.待處理,1.簽核中,2.退件,3.已核准,4.取消確認中,5.作廢中,6.取消作廢中,N.不執行電子簽核[DEF:N]
			+ "	BOMTG.TG004,"// --(+)元件號
			+ "	BOMTG.TG007,"// --(+)入庫
			+ "	BOMTG.TG008,"// --(+)元件數量
			+ "	BOMTG.TG010,"// --(+)確認碼 Y/N/V
			+ "	INVMB.MB001,"// --品號
			+ "	INVMB.MB002,"// --品名
			+ "	INVMB.MB003,"// --規格
			+ "	INVMB.MB017,"// --主要-倉別代號
			+ "	INVMB.MB032,"// --供應商代號
			+ "	INVMB.MB036,"// --固定前置天數
			+ "	INVMB.MB039,"// --最低補量
			+ "	INVMB.MB040,"// --補貨倍量
			+ "	CMSMC.MC002,"// --主要-倉別名稱
			+ "	COALESCE(PURMA.MA002,'') AS MA002,"// --供應商名稱
			+ "	'領料類'  AS TK000,"//
			+ "	BOMTG.CREATE_DATE,"// --建立單據時間
			+ "	BOMTG.MODI_DATE,"// --修改單據時間
			+ "	BOMTG.CREATOR"// --建立單據者
			+ " FROM "//
			+ "	[DTR_TW].[dbo].BOMTF"//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].BOMTG AS BOMTG "// --組合單據單身檔
			+ "	ON (BOMTF.TF001+'-'+TRIM(BOMTF.TF002)) =(BOMTG.TG001+'-'+TRIM(BOMTG.TG002))"//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].INVMB AS INVMB "// --(+)倉庫別
			+ "	ON BOMTG.TG004 = INVMB.MB001 "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].CMSMC AS CMSMC "// --基本資料
			+ "	ON INVMB.MB017 = CMSMC.MC001 "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].PURMA AS PURMA "// --廠商
			+ "	ON PURMA.MA001 = INVMB.MB032 "//
			+ " WHERE"//
			+ "	BOMTG.TG001 is not null "//
			+ "	AND BOMTG.TG008 > 0 "//
			+ " AND BOMTG.TG001 = 'A431' "//
			+ "	AND (BOMTG.CREATE_DATE >= CONVERT(VARCHAR(8), GETDATE()-600, 112) "//
			+ "	OR BOMTG.MODI_DATE = CONVERT(VARCHAR(8), GETDATE(), 112))"//
			+ " AND (CONCAT(BOMTG.TG001, '-', TRIM(BOMTG.TG002), '-', BOMTG.TG003) IN (:TG001TG002TG003)) "// 比對製令單+序號?
			+ " ORDER BY"//
			+ "	(BOMTG.TG001+'-'+TRIM(BOMTG.TG002)+'-'+BOMTG.TG003)  ASC"// --單號+序號
			, nativeQuery = true) // coalesce 回傳非NULL值
	ArrayList<Bomtf> findAllByBomtf(List<String> TG001TG002TG003);
}