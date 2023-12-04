package dtri.com.tw.mssql.dao;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.mssql.entity.Mocte;

public interface MocteDao extends JpaRepository<Mocte, Long> {

	// 多筆查詢範例
	@Query(value = "SELECT "// --領/退料單(領/退料單 A541 廠內領料單/ A542 補料單/ A551 委外領料單/ A561 廠內退料單/ A571 委外退料單)
			+ "	ROW_NUMBER() OVER(order by MOCTE.TE001) AS MOCTE_ID, "//
			+ "	TE001+'-'+TRIM(TE002)+'-'+TE003 AS TE001_TE002_TE003, "// --出料單號
			+ "	(MOCTA.TA001+'-'+MOCTA.TA002) AS TA001_TA002, "// --製令單
			+ "	MOCTA.TA006, "// --產品品號
			+ "	MOCTA.TA009, "// --預計開工
			+ "	MOCTA.TA015, "// --產品套數
			+ "	MOCTA.TA021, "// --生產產線別
			+ "	MOCTA.TA034, "// --產品品名
			+ "	MOCTA.TA035, "// --產品規格
			+ "	MOCTC.TC007, "// --領單頭備註
			+ "	CEILING(MOCTB.TB004-MOCTB.TB005) AS TB004, "// --(需領用 - 已領用) = 未領用
			+ "	CEILING(MOCTE.TE005) AS TE005, "//(退料用),--物料領退用量
			+ " MOCTB.TB005, "// --已領用
			+ "	MOCTE.TE008 AS TB009, "// --(改為 領調/退料單的)庫別
			+ "	MOCTC.TC008, "// --單據性質別54.廠內領料,55.託外領料,56.廠內退料,57.託外退料
			+ "	MOCTC.TC016, "// --簽核狀態碼0.待處理,1.簽核中,2.退件,3.已核准,4.取消確認中,5.作廢中,6.取消作廢中,N.不執行電子簽核[DEF:N]傳送次數[DEF:0]
			+ "	MOCTE.TE013, "// --領料說明(可領用量)
			+ "	MOCTE.TE014, "// --備註(來料時間 or 匹配進貨單)
			+ "	MOCTE.TE019, "// --簽核確認碼 Y/N/V

			+ "	INVMB.MB001, "// --品號
			+ "	INVMB.MB002, "// --品名
			+ "	INVMB.MB003, "// --規格
			+ "	INVMB.MB017, "// --主要-倉別代號
			+ "	INVMB.MB032, "// --主要-供應商代號
			+ "	INVMB.MB036, "// --主要-固定前置天數
			+ "	INVMB.MB039, "// --主要-最低補量
			+ "	INVMB.MB040, "// --主要-補貨倍量
			+ "	CMSMC.MC002, "// --主要-倉別名稱
			+ " COALESCE(PURMA.MA002,'') AS MA002,  "// --供應商名稱
			+ "	'入料類'  AS TK000, "//
			+ " MOCTE.CREATE_DATE, "// --建立單據時間
			+ "	MOCTE.MODI_DATE,"// --修改單據時間
			+ "	MOCTE.CREATOR "// --建立單據者
			+ " FROM "//
			+ "	[DTR_TW].[dbo].MOCTE AS MOCTE"//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].MOCTB AS MOCTB "// --製令單身
			+ "	ON (MOCTE.TE011+'_'+MOCTE.TE012+'_'+MOCTE.TE004+'_'+MOCTE.TE009) = (MOCTB.TB001+'_'+TRIM(MOCTB.TB002)+'_'+TRIM(MOCTB.TB003)+'_'+MOCTB.TB006)"//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].MOCTA AS MOCTA "// --製令單頭
			+ "	ON (MOCTB.TB001+'-'+TRIM(MOCTB.TB002)) =(MOCTA.TA001+'-'+MOCTA.TA002) "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].MOCTC AS MOCTC "// --製令單別
			+ "	ON (MOCTC.TC001+'-'+TRIM(MOCTC.TC002)) =(MOCTE.TE001+'-'+TRIM(MOCTE.TE002)) "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].INVMB AS INVMB "// --倉庫別
			+ "	ON MOCTE.TE004 = INVMB.MB001 "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].CMSMC AS CMSMC "// --基本資料
			+ "	ON INVMB.MB017 = CMSMC.MC001 "//
			+ " LEFT JOIN "//
			+ "	[DTR_TW].[dbo].PURMA AS PURMA "// --廠商
			+ "	ON PURMA.MA001 = INVMB.MB032 "//
			+ " WHERE "//
			+ "	(MOCTE.CREATE_DATE = CONVERT(VARCHAR(8), GETDATE(), 112) "//
			+ "	OR MOCTE.MODI_DATE = CONVERT(VARCHAR(8), GETDATE(), 112)) "//
			+ "	AND (MOCTB.TB004-MOCTB.TB005 > 0 "// --數量不為0
			+ " OR (MOCTE.TE005 >0 AND (MOCTE.TE001 = 'A543' OR MOCTE.TE001 = 'A561' OR MOCTE.TE001 = 'A571')))"//退料機制
			+ "ORDER BY "//
			+ " MOCTC.TC008 asc, "//
			+ "	(MOCTE.TE001 + MOCTE.TE002+MOCTE.TE003) asc"// --單號+序號
			, nativeQuery = true) // coalesce 回傳非NULL值
	ArrayList<Mocte> findAllByMocte();

}