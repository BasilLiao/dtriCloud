package dtri.com.tw.mssql.dao;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.mssql.entity.Purth;

public interface PurthDao extends JpaRepository<Purth, Long> {

	// 多筆查詢範例
	@Query(value = " SELECT "// --進貨單
			+ "	ROW_NUMBER() OVER(order by PURTH.TH001) AS PURTH_ID,"//
			+ "	(PURTH.TH011+'-'+PURTH.TH012+'-'+PURTH.TH013) AS TH011_TH012_TH013,"// --採購單
			+ "	(PURTH.TH001+'-'+PURTH.TH002) AS TH001_TH002,"// --進貨單
			+ "	PURTH.TH003, "// --進貨單序號
			+ "	PURTH.TH007, "// --數量
			+ "	PURTH.TH009, "// --庫別
			+ "	PURTH.TH014, "// --驗收時間
			+ "	PURTH.TH028, "// --檢驗狀態0.免檢,1.待驗,2.合格,3.不良,4.特採
			+ " PURTH.TH030," // --簽核確認碼 Y/N/V
			+ " PURTH.TH050, "// --簽核碼0.待處理,1.簽核中,2.退件,3.已核准,4.取消確認中,5.作廢中,6.取消作廢中,N.不執行電子簽核[DEF:N]
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
			+ "	'入料類'  AS TK000"//
			+ " FROM "//
			+ "	[DTR_TW].[dbo].PURTH AS PURTH "// --進貨單身
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].INVMB AS INVMB "// --倉庫別
			+ "	ON PURTH.TH004 = INVMB.MB001 "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].CMSMC AS CMSMC "// --基本資料
			+ "	ON INVMB.MB017 = CMSMC.MC001 "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].PURMA AS PURMA "// --廠商
			+ "	ON PURMA.MA001 = INVMB.MB032 "//
			+ "WHERE "//
			+ "	PURTH.TH028 != '3' "// 除了 不合格
			+ "	AND (PURTH.TH030 LIKE 'N' OR PURTH.TH030 LIKE 'Y' ) "// 已結項目 與 未結項目
			+ "	AND PURTH.TH007 > 0 "// 數量大於0
			+ "	AND (PURTH.CREATE_DATE = CONVERT(VARCHAR(8), GETDATE(), 112)"//
			+ "	OR PURTH.MODI_DATE = CONVERT(VARCHAR(8), GETDATE(), 112)) "// 今天
			+ "ORDER BY "//
			+ "	PURTH.TH001+'-'+PURTH.TH002+'-'+PURTH.TH003 ASC ,"// --進貨單號
			+ "	INVMB.MB001 ASC,"// --物料
			+ "	PURTH.TH014 ASC "// --時間
			, nativeQuery = true) // coalesce 回傳非NULL值
	ArrayList<Purth> findAllByPurth();

}