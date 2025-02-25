package dtri.com.tw.mssql.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.mssql.entity.Moctf;

public interface MoctfDao extends JpaRepository<Moctf, Long> {

	// 多筆查詢範例
	@Query(value = "SELECT"// --生產入庫
			+ "	ROW_NUMBER() OVER(order by MOCTF.TF001) AS MOCTF_ID,"//
			+ "	(MOCTG.TG001+ '-' + TRIM(MOCTG.TG002) +'-'+ MOCTG.TG003) AS TG001_TG002_TG003,"// --入庫單
			+ "	MOCTG.TG014+'-'+MOCTG.TG015 AS TG014_TG015,"// --製令單
			+ "	MOCTG.TG010,"// -- 入庫別
			+ "	CEILING(MOCTG.TG011) AS TG011,"// --入庫數量
			+ "	MOCTG.TG016,"// --入庫代驗(1.待驗,Y.檢驗合格,N.檢驗不合格,0.免檢)
			+ "	MOCTG.TG020, "// --備註
			+ "	MOCTG.TG022, "// --簽核確認碼 Y/N/V
			+ "	MOCTF.TF014, "// --簽核狀態碼0.待處理,1.簽核中,2.退件,3.已核准,4.取消確認中,5.作廢中,6.取消作廢中,N.不執行電子簽核[DEF:N]傳送次數[DEF:0]
			+ "	MOCTF.TF003,"// --入庫時間
			+ "	MOCTF.TF005,"// --單頭備註

			+ "	INVMB.MB001,"// --品號
			+ "	INVMB.MB002,"// --品名
			+ "	INVMB.MB003,"// --規格
			+ "	INVMB.MB017,"// --主要-倉別代號
			+ "	INVMB.MB032,"// --主要-供應商代號
			+ "	INVMB.MB036,"// --主要-固定前置天數
			+ "	INVMB.MB039,"// --主要-最低補量
			+ "	INVMB.MB040,"// --主要-補貨倍量
			+ "	CMSMC.MC002,"// --主要-倉別名稱
			+ "	COALESCE(PURMA.MA002,'') AS MA002,"// --供應商名稱
			+ "	'入料類'  AS TK000, "// --
			+ "	MOCTF.CREATE_DATE, "// --建立單據時間
			+ "	MOCTF.MODI_DATE, "// --修改單據時間
			+ "	MOCTF.CREATOR "// --建立單據者
			+ " FROM "//
			+ "	[DTR_TW].[dbo].MOCTF"// --入庫單頭
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].MOCTG AS MOCTG "// --入庫單身
			+ "	ON (MOCTF.TF001+'-'+TRIM(MOCTF.TF002)) =(MOCTG.TG001+'-'+TRIM(MOCTG.TG002)) "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].INVMB AS INVMB "// --倉庫別
			+ "	ON MOCTG.TG004 = INVMB.MB001 "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].CMSMC AS CMSMC "// --基本資料
			+ "	ON INVMB.MB017 = CMSMC.MC001 "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].PURMA AS PURMA "// --廠商
			+ "	ON PURMA.MA001 = INVMB.MB032 "//
			+ "WHERE "//
			+ "	MOCTF.TF014 ='3' OR MOCTF.TF014 ='N'  "//
			+ "	AND MOCTG.TG011 > 0 "// --數量不為0
			+ "	AND (MOCTG.CREATE_DATE >= CONVERT(VARCHAR(8), GETDATE()-10, 112) "//
			+ "	OR MOCTG.MODI_DATE = CONVERT(VARCHAR(8), GETDATE(), 112)) "// 今天
			+ "ORDER BY "//
			+ "	(MOCTG.TG001+ '-' + TRIM(MOCTG.TG002) +'-'+ MOCTG.TG003)  ASC"// --單號+序號
			, nativeQuery = true) // coalesce 回傳非NULL值
	ArrayList<Moctf> findAllByMoctf();

	// 多筆查詢範例
	@Query(value = "SELECT"// --生產入庫
			+ "	ROW_NUMBER() OVER(order by MOCTF.TF001) AS MOCTF_ID,"//
			+ "	(MOCTG.TG001+ '-' + TRIM(MOCTG.TG002) +'-'+ MOCTG.TG003) AS TG001_TG002_TG003,"// --入庫單
			+ "	MOCTG.TG014+'-'+MOCTG.TG015 AS TG014_TG015,"// --製令單
			+ "	MOCTG.TG010,"// -- 入庫別
			+ "	CEILING(MOCTG.TG011) AS TG011,"// --入庫數量
			+ "	MOCTG.TG016,"// --入庫代驗(1.待驗,Y.檢驗合格,N.檢驗不合格,0.免檢)
			+ "	MOCTG.TG020, "// --備註
			+ "	MOCTG.TG022, "// --簽核確認碼 Y/N/V
			+ "	MOCTF.TF014, "// --簽核狀態碼0.待處理,1.簽核中,2.退件,3.已核准,4.取消確認中,5.作廢中,6.取消作廢中,N.不執行電子簽核[DEF:N]傳送次數[DEF:0]
			+ "	MOCTF.TF003,"// --入庫時間
			+ "	MOCTF.TF005,"// --單頭備註

			+ "	INVMB.MB001,"// --品號
			+ "	INVMB.MB002,"// --品名
			+ "	INVMB.MB003,"// --規格
			+ "	INVMB.MB017,"// --主要-倉別代號
			+ "	INVMB.MB032,"// --主要-供應商代號
			+ "	INVMB.MB036,"// --主要-固定前置天數
			+ "	INVMB.MB039,"// --主要-最低補量
			+ "	INVMB.MB040,"// --主要-補貨倍量
			+ "	CMSMC.MC002,"// --主要-倉別名稱
			+ "	COALESCE(PURMA.MA002,'') AS MA002,"// --供應商名稱
			+ "	'入料類'  AS TK000, "// --
			+ "	MOCTF.CREATE_DATE, "// --建立單據時間
			+ "	MOCTF.MODI_DATE, "// --修改單據時間
			+ "	MOCTF.CREATOR "// --建立單據者
			+ " FROM "//
			+ "	[DTR_TW].[dbo].MOCTF"// --入庫單頭
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].MOCTG AS MOCTG "// --入庫單身
			+ "	ON (MOCTF.TF001+'-'+TRIM(MOCTF.TF002)) =(MOCTG.TG001+'-'+TRIM(MOCTG.TG002)) "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].INVMB AS INVMB "// --倉庫別
			+ "	ON MOCTG.TG004 = INVMB.MB001 "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].CMSMC AS CMSMC "// --基本資料
			+ "	ON INVMB.MB017 = CMSMC.MC001 "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].PURMA AS PURMA "// --廠商
			+ "	ON PURMA.MA001 = INVMB.MB032 "//
			+ "WHERE "//
			+ "	MOCTF.TF014 ='3' OR MOCTF.TF014 ='N'  "//
			+ "	AND MOCTG.TG011 > 0 "// --數量不為0
			+ "	AND (MOCTG.CREATE_DATE >= CONVERT(VARCHAR(8), GETDATE()-600, 112) "//
			+ "	OR MOCTG.MODI_DATE = CONVERT(VARCHAR(8), GETDATE(), 112)) "// 今天
			+ "	AND (CONCAT(MOCTG.TG001, '-', TRIM(MOCTG.TG002), '-', MOCTG.TG003) IN (:TG001TG002TG003)) "// 比對製令單+序號?
			+ "ORDER BY "//
			+ "	(MOCTG.TG001+ '-' + TRIM(MOCTG.TG002) +'-'+ MOCTG.TG003)  ASC"// --單號+序號
			, nativeQuery = true) // coalesce 回傳非NULL值
	ArrayList<Moctf> findAllByMoctf(List<String> TG001TG002TG003);

}