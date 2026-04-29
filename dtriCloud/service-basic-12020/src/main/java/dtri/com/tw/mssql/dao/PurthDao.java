package dtri.com.tw.mssql.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dtri.com.tw.mssql.dto.ValidatedPurthDto;
import dtri.com.tw.mssql.entity.Purth;

public interface PurthDao extends JpaRepository<Purth, Long> {

	// 多筆查詢範例
	@Query(value = " SELECT "// --進貨單 進貨單 A341 國內進貨單/ A342 國外進貨單/ A343 台北進貨單/ A345 無採購進貨單
			+ "	(TRIM(PURTH.TH001)+'-'+TRIM(PURTH.TH002)+'-'+TRIM(PURTH.TH003)) AS PURTH_ID,"//
			+ "	(TRIM(PURTH.TH001)+'-'+TRIM(PURTH.TH002)+'-'+TRIM(PURTH.TH003)) AS TH011_TH012_TH013,"// --採購單
			+ "	(PURTH.TH001+'-'+PURTH.TH002) AS TH001_TH002,"// --進貨單
			+ "	PURTH.TH003, "// --進貨單序號
			+ "	CEILING(PURTH.TH007) AS TH007, "// --數量
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
			+ "	'入料類'  AS TK000,"//
			+ "	PURTH.CREATE_DATE,"// --建立單據時間
			+ "	PURTH.MODI_DATE,"// --修改單據時間
			+ "	PURTH.CREATOR "// --建立單據者
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
			+ "	(PURTH.TH028 != '1' AND PURTH.TH028 != '3') "// 除了 不合格/待驗
			+ "	AND (PURTH.TH030 LIKE 'N' OR PURTH.TH030 LIKE 'Y' ) "// 已結項目 與 未結項目
			+ "	AND PURTH.TH007 > 0 "// 數量大於0
			+ " AND (PURTH.TH001 ='A341' OR PURTH.TH001 ='A342' OR PURTH.TH001 ='A343' OR PURTH.TH001 ='A345' ) "// 單據
			+ "	AND (PURTH.TH014 >= CONVERT(VARCHAR(8), GETDATE()-10, 112)) "// 遠端優化：改採驗收日期 TH014
			+ "ORDER BY "//
			+ "	PURTH.TH001+'-'+PURTH.TH002+'-'+PURTH.TH003 ASC ,"// --進貨單號
			+ "	INVMB.MB001 ASC,"// --物料
			+ "	PURTH.TH014 ASC "// --時間
			, nativeQuery = true) // coalesce 回傳非NULL值
	ArrayList<Purth> findAllByPurth();

	// 多筆查詢範例
	@Query(value = " SELECT "// --進貨單 進貨單 A341 國內進貨單/ A342 國外進貨單/ A343 台北進貨單/ A345 無採購進貨單
			+ "	(TRIM(PURTH.TH001)+'-'+TRIM(PURTH.TH002)+'-'+TRIM(PURTH.TH003)) AS PURTH_ID,"//
			+ "	(TRIM(PURTH.TH001)+'-'+TRIM(PURTH.TH002)+'-'+TRIM(PURTH.TH003)) AS TH011_TH012_TH013,"// --採購單
			+ "	(PURTH.TH001+'-'+PURTH.TH002) AS TH001_TH002,"// --進貨單
			+ "	PURTH.TH003, "// --進貨單序號
			+ "	CEILING(PURTH.TH007) AS TH007, "// --數量
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
			+ "	'入料類'  AS TK000,"//
			+ "	PURTH.CREATE_DATE,"// --建立單據時間
			+ "	PURTH.MODI_DATE,"// --修改單據時間
			+ "	PURTH.CREATOR "// --建立單據者
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
			+ "	(PURTH.TH028 != '1' AND PURTH.TH028 != '3') "// 除了 不合格/待驗
			+ "	AND (PURTH.TH030 != 'V' ) "// 已結項目 與 未結項目
			+ "	AND (PURTH.TH007 > 0 ) "// 數量大於0
			+ " AND (PURTH.TH001 ='A341' OR PURTH.TH001 ='A342' OR PURTH.TH001 ='A343' OR PURTH.TH001 ='A345' ) "// 單據
			+ "	AND (PURTH.TH014 >= CONVERT(VARCHAR(8), GETDATE()-60, 112)) "// 遠端優化：改採驗收日期 TH014
			+ " AND (CONCAT(PURTH.TH001, '-', TRIM(PURTH.TH002), '-', PURTH.TH003) IN (:TH001TH002TH003)) "// 比對製令單+序號?
			+ "ORDER BY "//
			+ "	PURTH.TH001+'-'+PURTH.TH002+'-'+PURTH.TH003 ASC ,"// --進貨單號
			+ "	INVMB.MB001 ASC,"// --物料
			+ "	PURTH.TH014 ASC "// --時間
			, nativeQuery = true) // coalesce 回傳非NULL值
	ArrayList<Purth> findAllByPurth60(List<String> TH001TH002TH003);

	/**
	 * 查詢進貨單明細列表 (待驗收清單)
	 * <p>
	 * 核心邏輯 (Core Logic)：
	 * 1. <b>資料範圍</b>：查詢已建立進貨單，但尚未完成驗收程序的項目。
	 * 2. <b>狀態過濾</b>：
	 * - 排除已結案/已取消 (TH028 != '3')。
	 * - 鎖定「未驗收 (N)」狀態 (TH030 LIKE 'N')。
	 * 3. <b>有效性</b>：數量 (TH007) 必須大於 0。
	 * </p>
	 * * @return List 包含進貨單號、待驗數量與供應商資訊的列表
	 * 
	 * @author Allen
	 */
	@Query(value = """
			SELECT
			   ISNULL(RTRIM(LTRIM(TH.TH001)), '') + '-' + ISNULL(RTRIM(LTRIM(TH.TH002)), '') AS TH001_TH002,

			    TH.TH003 AS TH003,      --進貨單號
			    TH.TH007 AS TH007,      --序號
			    TH.TH014 AS TH014,      --驗收時間

			    MB.MB001 AS MB001,      --品號
			    MB.MB002 AS MB002,      --品名
			    MB.MB003 AS MB003,      --規格
			    MB.MB017 AS MB017,      --倉別代號
			    MB.MB032 AS MB032,      --供應商代號
			    MB.MB036 AS MB036,      --固定前置天數
			    MB.MB039 AS MB039,      --最低補量
			    MB.MB040 AS MB040,  	--補貨倍量

			    MC.MC002 AS MC002,      --倉別名稱
			    COALESCE(MA.MA002, '') AS MA002, --供應商名稱
			    '進貨單' AS TK000        --單別

			FROM PURTH AS TH
			LEFT JOIN INVMB AS MB ON TH.TH004 = MB.MB001
			LEFT JOIN CMSMC AS MC ON MB.MB017 = MC.MC001
			LEFT JOIN PURMA AS MA ON MA.MA001 = MB.MB032

			WHERE
			    TH.TH028 != '3'
			    AND TH.TH030 LIKE 'N'
			    AND TH.TH007 > 0
			    AND (:materialNos IS NULL OR CHARINDEX(',' + TRIM(MB.MB001) + ',', :materialNos) > 0)
			ORDER BY
			    MB.MB001 ASC,
			    TH.TH014 ASC,
			    TH.TH001, TH.TH002, TH.TH003 ASC
			""", nativeQuery = true)
	List<ValidatedPurthDto> findAllByValidatedPurth(@Param("materialNos") String materialNos);

}