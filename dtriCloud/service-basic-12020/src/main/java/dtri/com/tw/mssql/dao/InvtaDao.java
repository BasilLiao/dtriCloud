package dtri.com.tw.mssql.dao;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.mssql.entity.Invta;

public interface InvtaDao extends JpaRepository<Invta, Long> {

	// 多筆查詢範例
	@Query(value = "SELECT "// --調撥/費用/A111-A112-A115-A119-A121
			+ "	ROW_NUMBER() OVER(order by INVTA.TA001) AS INVTA_ID, "//
			+ "	(INVTB.TB001+'-'+TRIM(INVTB.TB002)+'-'+INVTB.TB003) as TB001_TB002_TB003, "// --單號
			+ "	INVTB.TB007, "// --數量
			+ "	INVTB.TB012, "// --出庫
			+ "	INVTB.TB013, "// --入庫
			+ "	INVTB.TB018, "// --確認碼 Y/N/V
			+ "	INVTA.TA005, "// --備註
			+ "	INVTA.TA016, "// --簽核碼0.待處理,1.簽核中,2.退件,3.已核准,4.取消確認中,5.作廢中,6.取消作廢中,N.不執行電子簽核[DEF:N]
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
			+ "	'領料類'  AS TK000 ," + "	INVTB.CREATE_DATE,"// --建立單據時間
			+ "	INVTB.MODI_DATE,"// --修改單據時間
			+ "	INVTB.CREATOR "// --建立單據者
			+ " FROM "//
			+ "	[DTR_TW].[dbo].INVTA"//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].INVTB AS INVTB "// ---異動單據單身檔
			+ "	ON (INVTA.TA001+'-'+TRIM(INVTA.TA002)) =(INVTB.TB001+'-'+TRIM(INVTB.TB002))"//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].INVMB AS INVMB "// --倉庫別
			+ "	ON INVTB.TB004 = INVMB.MB001 "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].CMSMC AS CMSMC "// --基本資料
			+ "	ON INVMB.MB017 = CMSMC.MC001 "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].PURMA AS PURMA "// --廠商
			+ "	ON PURMA.MA001 = INVMB.MB032 "//
			+ "WHERE "//
			+ "	INVTB.TB001 is not null"//
			+ "	AND INVTB.TB007 != 0 "// 數量大於0
			+ " AND ((INVTB.TB001 ='A111' AND INVTB.TB018='N') "//
			+ "	OR (INVTB.TB001 ='A112'AND INVTB.TB018='Y') "//
			+ "	OR (INVTB.TB001 ='A115'AND INVTB.TB018='N') "//
			+ "	OR (INVTB.TB001 ='A119' AND INVTB.TB018='Y') "//
			+ "	OR (INVTB.TB001 ='A121'AND INVTB.TB018='Y')) "//
			+ "	AND (INVTB.CREATE_DATE >= CONVERT(VARCHAR(8), GETDATE()-30, 112) "// 今天
			+ "	OR INVTB.MODI_DATE = CONVERT(VARCHAR(8), GETDATE(), 112)) "//
			+ "ORDER BY"//
			+ "	(INVTB.TB001+'-'+TRIM(INVTB.TB002)+'-'+INVTB.TB003)  ASC"// --單號+序號
			, nativeQuery = true) // coalesce 回傳非NULL值
	ArrayList<Invta> findAllByMocta();

}