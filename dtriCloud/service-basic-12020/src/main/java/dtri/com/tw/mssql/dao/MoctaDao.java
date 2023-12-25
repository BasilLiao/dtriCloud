package dtri.com.tw.mssql.dao;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.mssql.entity.Mocta;

public interface MoctaDao extends JpaRepository<Mocta, Long> {

	// 多筆查詢範例
	@Query(value = " SELECT  "// --製令單
			+ " ROW_NUMBER() OVER(order by INVMB.MB001) AS MOCTA_ID,"//
			+ " (MOCTA.TA026+'-'+MOCTA.TA027+'-'+MOCTA.TA028) AS TA026_TA027_TA028,"// --訂單項
			+ "	(MOCTA.TA001+'-'+MOCTA.TA002) AS TA001_TA002,"// --製令單
			+ " MOCTA.TA006, "// --成品品號
			+ " MOCTA.TA009, "// --預計開工日
			+ "	MOCTA.TA010, "// --預計完工日
			+ "	MOCTB.TB015, "// --預計領料日
			+ "	CEILING(MOCTB.TB004)AS TB004 , "// --正數 預計領
			+ "	MOCTB.TB005, "// --負數 已領用量
			+ "	(TB004 - TB005) AS TB004_TB005, "// --需領用-已領用(正數 預計領 / 負數 已領用量)
			+ "	INVMB.MB001, "// --品號
			+ "	INVMB.MB002, "// --品名
			+ "	INVMB.MB003, "// --規格
			+ "	INVMB.MB017, "// --主要-倉別代號
			+ "	INVMB.MB032, "// --主要-供應商代號
			+ "	INVMB.MB036, "// --主要-固定前置天數
			+ "	INVMB.MB039, "// --主要-最低補量
			+ "	INVMB.MB040, "// --主要-補貨倍量
			+ "	COALESCE(CMSMC.MC002,'') AS MC002, "// --倉別名稱
			+ "	COALESCE(PURMA.MA002,'') AS MA002, "// --供應商名稱
			+ "	'製令類' AS TK000 ,"//
			+ "	MOCTB.CREATE_DATE,"// --建立單據時間
			+ "	MOCTB.MODI_DATE,"// --修改單據時間
			+ "	MOCTB.CREATOR "// --建立單據者
			+ " FROM "//
			+ "	[DTR_TW].[dbo].MOCTA AS MOCTA "// --製令單頭
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].MOCTB AS MOCTB "// --製令單身
			+ "	ON (MOCTA.TA001 + MOCTA.TA002) = (MOCTB.TB001 + MOCTB.TB002) "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].INVMB AS INVMB "// --倉庫別
			+ "	ON MOCTB.TB003 = INVMB.MB001 "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].CMSMC AS CMSMC "// --基本資料
			+ "	ON INVMB.MB017 = CMSMC.MC001 "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].PURMA AS PURMA "// --廠商
			+ "	ON PURMA.MA001 = INVMB.MB032 "//
			+ " WHERE "//
			+ "	(TA011 = '1' OR TA011 = '2' OR TA011 = '3') "//
			+ "	AND MOCTB.TB004 > 0 "// --數量不為0
			+ "	AND MOCTB.TB018 = 'Y' "// --確認碼
			+ " AND (MOCTB.CREATE_DATE >= CONVERT(VARCHAR(8), GETDATE()-30, 112) "//
			+ "	OR MOCTB.MODI_DATE = CONVERT(VARCHAR(8), GETDATE(), 112)) "// 今天
			+ " ORDER BY "//
			+ "	MOCTA.TA001+MOCTA.TA002 ASC,"// --工單號
			+ "	INVMB.MB001 ASC,"// --物料
			+ "	MOCTA.TA009 ASC"// --時間
			, nativeQuery = true) // coalesce 回傳非NULL值
	ArrayList<Mocta> findAllByMocta();

}