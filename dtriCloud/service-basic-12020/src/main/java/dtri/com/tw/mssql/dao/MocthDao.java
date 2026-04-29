package dtri.com.tw.mssql.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dtri.com.tw.mssql.entity.Mocth;

public interface MocthDao extends JpaRepository<Mocth, Long> {

	// 多筆查詢範例
	@Query(value = "SELECT	"// --委外進貨 A591
			+ "	(TRIM(MOCTI.TI001)+'-'+TRIM(MOCTI.TI002)+'-'+TRIM(MOCTI.TI003)) AS MOCTH_ID,"//
			+ "	(TRIM(MOCTI.TI001)+'-'+TRIM(MOCTI.TI002)+'-'+TRIM(MOCTI.TI003)) AS TI001_TI002_TI003,"// --委外進貨單
			+ "	(MOCTI.TI013+'-'+TRIM(MOCTI.TI014)) AS TI013_TI014,"// --製令單
			+ "	CEILING(MOCTI.TI007) AS TI007,"// --進貨數量
			+ "	MOCTI.TI009,"// --進貨庫別
			+ "	MOCTI.TI035,"// --1.待驗,Y(2).檢驗合格,N(3).檢驗不合格,0.免檢
			+ " MOCTI.TI037,"// --確認碼 Y/N/V
			+ "	MOCTI.TI048, "// --簽核狀態碼0.待處理,1.簽核中,2.退件,3.已核准,4.取消確認中,5.作廢中,6.取消作廢中,N.不執行電子簽核[DEF:N]
			+ "	MOCTH.TH005,"// --加工廠商之代號

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
			+ "	'入料類' AS TK000,"//
			+ "	MOCTI.CREATE_DATE,"// --建立單據時間
			+ "	MOCTI.MODI_DATE,"// --修改單據時間
			+ "	MOCTI.CREATOR "// --建立單據者
			+ " FROM "//
			+ "	[DTR_TW].[dbo].MOCTH"//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].MOCTI AS MOCTI "// --委外單身
			+ "	ON (MOCTH.TH001+'-'+TRIM(MOCTH.TH002)) =(MOCTI.TI001+'-'+TRIM(MOCTI.TI002)) "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].INVMB AS INVMB "// --倉庫別
			+ "	ON MOCTI.TI004 = INVMB.MB001 "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].CMSMC AS CMSMC "// --基本資料
			+ "	ON INVMB.MB017 = CMSMC.MC001 "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].PURMA AS PURMA "// --廠商
			+ "	ON PURMA.MA001 = INVMB.MB032 "//
			+ "WHERE "//
			+ " MOCTI.TI001 is not null "//
			+ "	AND MOCTI.TI007 > 0 "// --數量不為0
			+ " AND (MOCTI.TI001='A591') "// --
			+ "	AND (MOCTI.TI048 ='N' OR MOCTI.TI048='3') "// --
			+ " AND (MOCTI.CREATE_DATE >= CONVERT(VARCHAR(8), GETDATE()-10, 112) "//
			+ "	OR MOCTI.MODI_DATE = CONVERT(VARCHAR(8), GETDATE(), 112)) "// 今天
			+ "ORDER BY"//
			+ "	(MOCTI.TI001+'-'+TRIM(MOCTI.TI002)+'-'+MOCTI.TI003)   ASC"// --單號+序號
			, nativeQuery = true) // coalesce 回傳非NULL值
	ArrayList<Mocth> findAllByMocth();

	// 多筆查詢範例
	@Query(value = "SELECT	"// --委外進貨 A591
			+ "	(TRIM(MOCTI.TI001)+'-'+TRIM(MOCTI.TI002)+'-'+TRIM(MOCTI.TI003)) AS MOCTH_ID,"//
			+ "	(TRIM(MOCTI.TI001)+'-'+TRIM(MOCTI.TI002)+'-'+TRIM(MOCTI.TI003)) AS TI001_TI002_TI003,"// --委外進貨單
			+ "	(MOCTI.TI013+'-'+TRIM(MOCTI.TI014)) AS TI013_TI014,"// --製令單
			+ "	CEILING(MOCTI.TI007) AS TI007,"// --進貨數量
			+ "	MOCTI.TI009,"// --進貨庫別
			+ "	MOCTI.TI035,"// --1.待驗,Y(2).檢驗合格,N(3).檢驗不合格,0.免檢
			+ " MOCTI.TI037,"// --確認碼 Y/N/V
			+ "	MOCTI.TI048, "// --簽核狀態碼0.待處理,1.簽核中,2.退件,3.已核准,4.取消確認中,5.作廢中,6.取消作廢中,N.不執行電子簽核[DEF:N]
			+ "	MOCTH.TH005,"// --加工廠商之代號

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
			+ "	'入料類' AS TK000,"//
			+ "	MOCTI.CREATE_DATE,"// --建立單據時間
			+ "	MOCTI.MODI_DATE,"// --修改單據時間
			+ "	MOCTI.CREATOR "// --建立單據者
			+ " FROM "//
			+ "	[DTR_TW].[dbo].MOCTH"//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].MOCTI AS MOCTI "// --委外單身
			+ "	ON (MOCTH.TH001+'-'+TRIM(MOCTH.TH002)) =(MOCTI.TI001+'-'+TRIM(MOCTI.TI002)) "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].INVMB AS INVMB "// --倉庫別
			+ "	ON MOCTI.TI004 = INVMB.MB001 "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].CMSMC AS CMSMC "// --基本資料
			+ "	ON INVMB.MB017 = CMSMC.MC001 "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].PURMA AS PURMA "// --廠商
			+ "	ON PURMA.MA001 = INVMB.MB032 "//
			+ "WHERE "//
			+ " MOCTI.TI001 is not null "//
			+ "	AND MOCTI.TI007 > 0 "// --數量不為0
			+ " AND (MOCTI.TI001 = 'A591') "// --
			+ "	AND (MOCTI.TI037 !='V') "// --
			+ " AND (MOCTI.CREATE_DATE >= CONVERT(VARCHAR(8), GETDATE()-80, 112) "//
			+ "	OR MOCTI.MODI_DATE = CONVERT(VARCHAR(8), GETDATE(), 112)) "// 今天
			+ " AND (CONCAT(MOCTI.TI001, '-', TRIM(MOCTI.TI002), '-', MOCTI.TI003) IN (:TI001TI002TI003)) "// 比對製令單+序號?
			+ "ORDER BY"//
			+ "	(MOCTI.TI001+'-'+TRIM(MOCTI.TI002)+'-'+MOCTI.TI003)   ASC"// --單號+序號
			, nativeQuery = true) // coalesce 回傳非NULL值
	ArrayList<Mocth> findAllByMocth80(List<String> TI001TI002TI003);

	/**
	 * 查詢託外進貨單明細列表 (待驗收清單)
	 * <p>
	 * 核心邏輯 (Core Logic)：
	 * 1. <b>資料範圍</b>：查詢已建立託外進貨單，但尚未完成驗收程序的項目。
	 * 2. <b>狀態過濾</b>：
	 * - 鎖定「未驗收 (1)」狀態 (TI035 = '1')。
	 * - 排除已作廢 (TI037 != 'V')。
	 * 3. <b>單別過濾</b>：限定 'A591'。
	 * 4. <b>有效性</b>：數量 (TI007) 必須大於 0。
	 * </p>
	 * * @return List 包含託外進貨單號、待驗數量與廠商資訊的列表
	 * 
	 * @author Allen
	 */
	@Query(value = """
			SELECT
			   ISNULL(RTRIM(LTRIM(TI.TI001)), '') + '-' + ISNULL(RTRIM(LTRIM(TI.TI002)), '') AS TI001_TI002,
			    TI.TI003 AS TI003,      --單號序號
			    TI.TI007 AS TI007,      --數量
			    TI.TI014 AS TI014,      --驗收時間 (對應 PURTH 的 TH014)

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
			    '託外進貨單' AS TK000        --單別

			FROM DTR_TW.dbo.MOCTI AS TI
			LEFT JOIN DTR_TW.dbo.INVMB AS MB ON TI.TI004 = MB.MB001
			LEFT JOIN DTR_TW.dbo.CMSMC AS MC ON MB.MB017 = MC.MC001
			LEFT JOIN DTR_TW.dbo.PURMA AS MA ON MA.MA001 = MB.MB032

			WHERE
			    TI.TI001 = 'A591'
			    AND TI.TI035 = '1'
			    AND TI.TI037 != 'V'
			    AND TI.TI007 > 0
			    AND (:materialNos IS NULL OR CHARINDEX(',' + TRIM(MB.MB001) + ',', :materialNos) > 0)
			ORDER BY
			    MB.MB001 ASC,
			    TI.TI014 ASC,
			    TI.TI001, TI.TI002, TI.TI003 ASC
			""", nativeQuery = true)
	List<dtri.com.tw.mssql.dto.ValidatedMoctiDto> findAllByValidatedMocti(@Param("materialNos") String materialNos);

}