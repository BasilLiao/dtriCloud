package dtri.com.tw.mssql.dao;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.mssql.entity.Invth;

public interface InvthDao extends JpaRepository<Invth, Long> {

	// 多筆查詢範例
	@Query(value = "SELECT	"// --借出歸還A151+借入歸還單A161
			+ "	ROW_NUMBER() OVER(order by INVTI.TI001) AS INVTH_ID,"//
			+ "	(INVTI.TI001+'-'+TRIM(INVTI.TI002)+'-'+INVTI.TI003) AS TI001_TI002_TI003,"// ---借出歸還單
			+ "	(INVTI.TI014+'-'+TRIM(INVTI.TI015)+'-'+INVTI.TI016) AS TI014_TI015_TI016,"// --來源
			+ "	INVTI.TI009, "// --數量
			+ "	INVTI.TI022,"// --確認碼Y:已確認,N:未確認,U:確認失敗,V:作廢
			+ "	INVTI.TI007,"// --轉出庫別
			+ "	INVTI.TI008,"// --轉入庫別
			+ "	INVTH.TH006,"// --對象
			+ "	INVTH.TH027,"// --0.待處理,1.簽核中,2.退件,3.已核准,4.取消確認中,5.作廢中,6.取消作廢中,N.不執行電子簽核[DEF:N]
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
			+ "	'領料類'  AS TK000 "//
			+ "FROM "//
			+ "	[DTR_TW].[dbo].INVTH"//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].INVTI AS INVTI "// ---歸還單身
			+ "	ON (INVTH.TH001+'-'+TRIM(INVTH.TH002)) =(INVTI.TI001+'-'+TRIM(INVTI.TI002))"//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].INVMB AS INVMB "// --倉庫別
			+ "	ON INVTI.TI004 = INVMB.MB001 "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].CMSMC AS CMSMC "// --基本資料
			+ "	ON INVMB.MB017 = CMSMC.MC001 "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].PURMA AS PURMA "// --廠商
			+ "	ON PURMA.MA001 = INVMB.MB032 "//
			+ "WHERE "//
			+ "	INVTI.TI001 is not null "//
			+ "	AND (INVTI.CREATE_DATE = CONVERT(VARCHAR(8), GETDATE(), 112) "//
			+ "	OR INVTI.MODI_DATE = CONVERT(VARCHAR(8), GETDATE(), 112)) "// 今天
			+ "ORDER BY "//
			+ "	(INVTI.TI001+'-'+TRIM(INVTI.TI002)+'-'+INVTI.TI003)  ASC"// --單號+序號
			, nativeQuery = true) // coalesce 回傳非NULL值
	ArrayList<Invth> findAllByMocth();

}