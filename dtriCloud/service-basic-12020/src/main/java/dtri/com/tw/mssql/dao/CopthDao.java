package dtri.com.tw.mssql.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.mssql.entity.Copth;

public interface CopthDao extends JpaRepository<Copth, Long> {

	// 多筆查詢範例
	@Query(value = "SELECT	"// --OK 銷貨單單 A231/A232
			+ "  (TRIM(COPTH.TH001)+ '-' + TRIM(COPTH.TH002) +'-'+ TRIM(COPTH.TH003)) AS COPTH_ID, "//
			+ "  (TRIM(COPTH.TH001)+ '-' + TRIM(COPTH.TH002) +'-'+ TRIM(COPTH.TH003)) AS TH001_TH002_TH003,"// --銷貨單
			+ "  COPTH.TH007,"// -- 出庫別 "
			+ "  COPTH.TH008, "// --出庫數量 "
			+ "  COPTH.TH018, "// --備註 "
			+ "  COPTH.TH020,"// --簽核確認碼 Y/N/V "
			+ "  COPTG.TG047,"// --簽核狀態碼0.待處理,1.簽核中,2.退件,3.已核准,4.取消確認中,5.作廢中,6.取消作廢中,N.不執行電子簽核[DEF:N]傳送次數[DEF:0]
			+ "  COPTG.TG042, "// --單據日期時間 "
			+ "  COPTG.TG020,"// --單頭備註
			+ "  INVMB.MB001, "// --品號 "
			+ "  COPTG.TG007, "// --客戶對象
			+ "  INVMB.MB002, "// --品名 "
			+ "  INVMB.MB003, "// --規格 "
			+ "  INVMB.MB017, "// --主要-倉別代號 "
			+ "  INVMB.MB032, "// --供應商代號 "
			+ "  INVMB.MB036, "// --固定前置天數 "
			+ "  INVMB.MB039, "// --最低補量 "
			+ "  INVMB.MB040, "// --補貨倍量 "
			+ "  CMSMC.MC002, "// --主要-倉別名稱 "
			+ "  COALESCE(PURMA.MA002, '') AS MA002, "// --供應商名稱 "
			+ "  '領料類' AS TK000, COPTG.CREATE_DATE,"// --建立單據時間 "
			+ "  COPTG.MODI_DATE,"// --修改單據時間 "
			+ "  COPTG.CREATOR "// --建立單據者 "
			+ "FROM [DTR_TW].[dbo].COPTG"// --銷貨單頭 "
			+ "     LEFT JOIN [DTR_TW].[dbo].COPTH AS COPTH "// --銷貨單身 "
			+ "     ON (COPTG.TG001+'-'+TRIM(COPTG.TG002)) =(COPTH.TH001+'-'+TRIM(COPTH.TH002)) "//
			+ "     LEFT JOIN [DTR_TW].[dbo].INVMB AS INVMB "// --倉庫別 "
			+ "     ON COPTH.TH004 = INVMB.MB001 "//
			+ "     LEFT JOIN [DTR_TW].[dbo].CMSMC AS CMSMC "// --基本資料 "
			+ "     ON INVMB.MB017 = CMSMC.MC001 "//
			+ "     LEFT JOIN [DTR_TW].[dbo].PURMA AS PURMA "// --廠商 "
			+ "     ON PURMA.MA001 = INVMB.MB032 "//
			+ "WHERE COPTH.TH008 > 0 "//
			+ " AND (COPTH.TH001='A231' OR COPTH.TH001='A232') "//
			+ " AND (COPTH.CREATE_DATE >= CONVERT(VARCHAR(8), GETDATE()-10, 112) "//
			+ " OR COPTH.MODI_DATE = CONVERT(VARCHAR(8), GETDATE(), 112)) "//
			+ "ORDER BY (COPTH.TH001+ '-' + TRIM(COPTH.TH002) +'-'+ COPTH.TH003) ASC"// --單號+序號
			, nativeQuery = true) // coalesce 回傳非NULL值
	ArrayList<Copth> findAllByCopth();

	// 多筆查詢範例
	@Query(value = "SELECT	"// --OK 銷貨單單 A231/A232
			+ "  (TRIM(COPTH.TH001)+ '-' + TRIM(COPTH.TH002) +'-'+ TRIM(COPTH.TH003)) AS COPTH_ID, "//
			+ "  (TRIM(COPTH.TH001)+ '-' + TRIM(COPTH.TH002) +'-'+ TRIM(COPTH.TH003)) AS TH001_TH002_TH003,"// --銷貨單
			+ "  COPTH.TH007,"// -- 出庫別 "
			+ "  COPTH.TH008, "// --出庫數量 "
			+ "  COPTH.TH018, "// --備註 "
			+ "  COPTH.TH020,"// --簽核確認碼 Y/N/V "
			+ "  COPTG.TG047,"// --簽核狀態碼0.待處理,1.簽核中,2.退件,3.已核准,4.取消確認中,5.作廢中,6.取消作廢中,N.不執行電子簽核[DEF:N]傳送次數[DEF:0]
			+ "  COPTG.TG042, "// --單據日期時間 "
			+ "  COPTG.TG020,"// --單頭備註
			+ "  INVMB.MB001, "// --品號 "
			+ "  COPTG.TG007, "// --客戶對象
			+ "  INVMB.MB002, "// --品名 "
			+ "  INVMB.MB003, "// --規格 "
			+ "  INVMB.MB017, "// --主要-倉別代號 "
			+ "  INVMB.MB032, "// --供應商代號 "
			+ "  INVMB.MB036, "// --固定前置天數 "
			+ "  INVMB.MB039, "// --最低補量 "
			+ "  INVMB.MB040, "// --補貨倍量 "
			+ "  CMSMC.MC002, "// --主要-倉別名稱 "
			+ "  COALESCE(PURMA.MA002, '') AS MA002, "// --供應商名稱 "
			+ "  '領料類' AS TK000, COPTG.CREATE_DATE,"// --建立單據時間 "
			+ "  COPTG.MODI_DATE,"// --修改單據時間 "
			+ "  COPTG.CREATOR "// --建立單據者 "
			+ "FROM [DTR_TW].[dbo].COPTG"// --銷貨單頭 "
			+ "     LEFT JOIN [DTR_TW].[dbo].COPTH AS COPTH "// --銷貨單身 "
			+ "     ON (COPTG.TG001+'-'+TRIM(COPTG.TG002)) =(COPTH.TH001+'-'+TRIM(COPTH.TH002)) "//
			+ "     LEFT JOIN [DTR_TW].[dbo].INVMB AS INVMB "// --倉庫別 "
			+ "     ON COPTH.TH004 = INVMB.MB001 "//
			+ "     LEFT JOIN [DTR_TW].[dbo].CMSMC AS CMSMC "// --基本資料 "
			+ "     ON INVMB.MB017 = CMSMC.MC001 "//
			+ "     LEFT JOIN [DTR_TW].[dbo].PURMA AS PURMA "// --廠商 "
			+ "     ON PURMA.MA001 = INVMB.MB032 "//
			+ "WHERE COPTH.TH008 > 0 "//
			+ " AND (COPTH.TH020!='V') "//
			+ " AND (COPTH.TH001='A231' OR COPTH.TH001='A232') "//
			+ " AND (COPTH.CREATE_DATE >= CONVERT(VARCHAR(8), GETDATE()-100, 112) "//
			+ " OR COPTH.MODI_DATE = CONVERT(VARCHAR(8), GETDATE(), 112)) "//
			+ " AND (CONCAT(COPTH.TH001, '-', TRIM(COPTH.TH002), '-', COPTH.TH003) IN (:TH001TH002TH003)) "// 比對製令單+序號?
			+ "ORDER BY (COPTH.TH001+ '-' + TRIM(COPTH.TH002) +'-'+ COPTH.TH003) ASC"// --單號+序號
			, nativeQuery = true) // coalesce 回傳非NULL值
	ArrayList<Copth> findAllByCopth(List<String> TH001TH002TH003);
}