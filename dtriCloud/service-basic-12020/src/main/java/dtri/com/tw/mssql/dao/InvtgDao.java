package dtri.com.tw.mssql.dao;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.mssql.entity.Invtg;

public interface InvtgDao extends JpaRepository<Invtg, Long> {

	// 多筆查詢範例
	@Query(value = "SELECT	"// --借出A131+借入單A141
			+ "	ROW_NUMBER() OVER(order by INVTG.TG001) AS INVTG_ID, "//
			+ "	(INVTG.TG001+'-'+TRIM(INVTG.TG002)+'-'+INVTG.TG003) AS TG001_TG002_TG003, "// ---借出單
			+ "	INVTF.TF015, "// --借出對象
			+ "	INVTG.TG007, "// --轉出庫別
			+ "	INVTG.TG008, "// --轉入庫別
			+ "	INVTG.TG009, "// --數量
			+ " INVTG.TG022, "// --確認碼Y:已確認,N:未確認,U:確認失敗,V:作廢
			+ "	INVTF.TF028, "// --簽核狀態碼0.待處理,1.簽核中,2.退件,3.已核准,4.取消確認中,5.作廢中,6.取消作廢中,N.不執行電子簽核[DEF:N]

			+ "	INVMB.MB001, "// --品號
			+ "	INVMB.MB002, "// --品名
			+ "	INVMB.MB003, "// --規格
			+ "	INVMB.MB017, "// --倉別代號
			+ "	INVMB.MB032, "// --供應商代號
			+ "	INVMB.MB036, "// --固定前置天數
			+ "	INVMB.MB039, "// --最低補量
			+ "	INVMB.MB040, "// --補貨倍量
			+ "	CMSMC.MC002, "// --倉別名稱
			+ "	COALESCE(PURMA.MA002,'') AS MA002, "// --供應商名稱
			+ "	'領料類'  AS TK000 "//
			+ "FROM "//
			+ "	[DTR_TW].[dbo].INVTF"//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].INVTG AS INVTG "// ---借出單身
			+ "	ON (INVTG.TG001+'-'+TRIM(INVTG.TG002)) =(INVTF.TF001+'-'+TRIM(INVTF.TF002))"//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].INVMB AS INVMB "// --倉庫別
			+ "	ON INVTG.TG004 = INVMB.MB001 "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].CMSMC AS CMSMC "// --基本資料
			+ "	ON INVMB.MB017 = CMSMC.MC001 "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].PURMA AS PURMA "// --廠商
			+ "	ON PURMA.MA001 = INVMB.MB032 "//
			+ "WHERE"//
			+ "	INVTG.TG022!='V'"//
			+ "	AND INVTG.TG001 is not null"//
			+ "	AND INVTG.MODI_DATE = CONVERT(VARCHAR(8), GETDATE(), 112) "//
			+ "ORDER BY "//
			+ "	(INVTG.TG001+'-'+TRIM(INVTG.TG002)+'-'+INVTG.TG003)  ASC"// --單號+序號
			, nativeQuery = true) // coalesce 回傳非NULL值
	ArrayList<Invtg> findAllByMocth();

}