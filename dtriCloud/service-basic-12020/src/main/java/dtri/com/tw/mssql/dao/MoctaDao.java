package dtri.com.tw.mssql.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dtri.com.tw.mssql.dto.MaterialQtyDto;
import dtri.com.tw.mssql.dto.ValidatedMoctaDto;
import dtri.com.tw.mssql.dto.ValidatedMoctabDto;
import dtri.com.tw.mssql.entity.Mocta;

public interface MoctaDao extends JpaRepository<Mocta, Long> {

	// 多筆查詢範例
	@Query(value = " SELECT  "// --製令單A511 廠內製令單/A512 委外製令單/A521 廠內重工單/A522 委外領料單
			+ " ROW_NUMBER() OVER(order by INVMB.MB001) AS MOCTA_ID,"//
			+ " (TRIM(MOCTA.TA026)+'-'+TRIM(MOCTA.TA027)+'-'+TRIM(MOCTA.TA028)) AS TA026_TA027_TA028,"// --訂單項
			+ "	(MOCTA.TA001+'-'+MOCTA.TA002) AS TA001_TA002,"// --製令單
			+ " MOCTA.TA006, "// --成品品號
			+ " COPTD.TD004, "// --客戶品號
			+ " MOCTA.TA015, "// --預計生產數量
			+ " MOCTA.TA017, "// --已生產數
			+ " MOCTA.TA029, "// --生管備註
			+ "	MOCTA.TA050, "// --訂單生產加工包裝資訊(客戶資訊)
			+ " INVMAB.MA003,"// --產品機型
			+ "	MOCTB.TB015, "// --預計領料日
			+ " MOCTA.TA009, "// --預計開工日
			+ "	MOCTA.TA010, "// --預計完工日
			+ " MOCTA.TA011, "// --,--確認結單?1.未生產,2.已發料,3.生產中,Y.已完工,y.指定完工
			+ "	CEILING(MOCTB.TB004) AS TB004, "// --正數 預計領
			+ "	MOCTB.TB005, "// --負數 已領用量
			+ "	(TB004 - TB005) AS TB004_TB005, "// --需領用-已領用(正數 預計領 / 負數 已領用量)
			+ "	MOCTB.TB017, "// --項目備註
			+ "	MOCTA.TA054, "// --生產注意事項
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
			+ "LEFT JOIN [DTR_TW].[dbo].COPTD AS COPTD ON (REPLACE(MOCTA.TA026+'-'+MOCTA.TA027+'-'+MOCTA.TA028, ' ', '') = REPLACE(COPTD.TD001+'-'+COPTD.TD002+'-'+COPTD.TD003, ' ', '')) "// --
			+ "LEFT JOIN "//
			+ "	(SELECT  *"//
			+ "		FROM (SELECT "//
			+ "		  MA.MA003,"//
			+ "		  MB.MB008,"//
			+ "		  MB.MB001"//
			+ "		FROM [DTR_TW].[dbo].INVMB AS MB "//
			+ "		LEFT JOIN [DTR_TW].[dbo].INVMA AS MA "//
			+ "		  ON MB.MB008 = MA.MA002 "//
			+ "		WHERE MA.MA003 IS NOT NULL) AS INVMAB)AS INVMAB " // --成品皆關聯(品號基本資料檔)\n
			+ "	 ON INVMAB.MB001= MOCTA.TA006 "//
			+ "	 LEFT JOIN "//
			+ "	 [DTR_TW].[dbo].INVMB AS INVMB "// --倉庫別
			+ "	 ON MOCTB.TB003 = INVMB.MB001 "//
			+ "	 LEFT JOIN "//
			+ "	 [DTR_TW].[dbo].CMSMC AS CMSMC "// --基本資料
			+ "	 ON INVMB.MB017 = CMSMC.MC001 "//
			+ "	 LEFT JOIN "//
			+ "	 [DTR_TW].[dbo].PURMA AS PURMA "// --廠商
			+ "	 ON PURMA.MA001 = INVMB.MB032 "//
			+ " WHERE "//
			+ " (MOCTA.TA011 = '1' OR MOCTA.TA011 = '2' OR MOCTA.TA011 = '3') "//
			// + " (MOCTA.TA011 = '1' OR MOCTA.TA011 = '2' OR MOCTA.TA011 = '3' OR
			// MOCTA.TA011 = 'y' OR MOCTA.TA011 = 'Y') "//
			+ "	 AND (MOCTA.TA001='A511' OR MOCTA.TA001='A512' OR MOCTA.TA001='A521' OR MOCTA.TA001='A522') "//
			+ "	 AND (MOCTB.TB018 = 'Y' OR MOCTB.TB018 = 'N') "// --核單碼
			+ "  AND (MOCTB.CREATE_DATE >= CONVERT(VARCHAR(8), GETDATE()-250, 112) "//
			+ " OR MOCTB.MODI_DATE = CONVERT(VARCHAR(8), GETDATE(), 112)) "// 今天
			+ " ORDER BY "//
			+ "	 MOCTA.TA001+MOCTA.TA002 ASC,"// --工單號
			+ "	 INVMB.MB001 ASC,"// --物料
			+ "	 MOCTA.TA009 ASC"// --時間
			, nativeQuery = true) // coalesce 回傳非NULL值
	ArrayList<Mocta> findAllByMocta();

	// 多筆查詢範例
	@Query(value = " SELECT  "// --製令單A511 廠內製令單/A512 委外製令單/A521 廠內重工單/A522 委外領料單
			+ " ROW_NUMBER() OVER(order by INVMB.MB001) AS MOCTA_ID,"//
			+ " (TRIM(MOCTA.TA026)+'-'+TRIM(MOCTA.TA027)+'-'+TRIM(MOCTA.TA028)) AS TA026_TA027_TA028,"// --訂單項
			+ "	(MOCTA.TA001+'-'+MOCTA.TA002) AS TA001_TA002,"// --製令單
			+ " MOCTA.TA006, "// --成品品號
			+ " COPTD.TD004, "// --客戶品號
			+ " MOCTA.TA015, "// --預計生產數量
			+ " MOCTA.TA017, "// --已生產數
			+ " MOCTA.TA029, "// --生管備註
			+ "	MOCTA.TA050, "// --訂單生產加工包裝資訊(客戶資訊)
			+ " INVMAB.MA003,"// --產品機型
			+ "	MOCTB.TB015, "// --預計領料日
			+ " MOCTA.TA009, "// --預計開工日
			+ "	MOCTA.TA010, "// --預計完工日
			+ " MOCTA.TA011, "// --,--確認結單?1.未生產,2.已發料,3.生產中,Y.已完工,y.指定完工
			+ "	CEILING(MOCTB.TB004) AS TB004, "// --正數 預計領
			+ "	MOCTB.TB005, "// --負數 已領用量
			+ "	(TB004 - TB005) AS TB004_TB005, "// --需領用-已領用(正數 預計領 / 負數 已領用量)
			+ "	MOCTB.TB017, "// --項目備註
			+ "	MOCTA.TA054, "// --生產注意事項
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
			+ "LEFT JOIN [DTR_TW].[dbo].COPTD AS COPTD ON (REPLACE(MOCTA.TA026+'-'+MOCTA.TA027+'-'+MOCTA.TA028, ' ', '') = REPLACE(COPTD.TD001+'-'+COPTD.TD002+'-'+COPTD.TD003, ' ', '')) "// --
			+ "LEFT JOIN "//
			+ "	(SELECT  *"//
			+ "		FROM (SELECT "//
			+ "		  MA.MA003,"//
			+ "		  MB.MB008,"//
			+ "		  MB.MB001"//
			+ "		FROM [DTR_TW].[dbo].INVMB AS MB "//
			+ "		LEFT JOIN [DTR_TW].[dbo].INVMA AS MA "//
			+ "		  ON MB.MB008 = MA.MA002 "//
			+ "		WHERE MA.MA003 IS NOT NULL) AS INVMAB)AS INVMAB " // --成品皆關聯(品號基本資料檔)\n
			+ "	 ON INVMAB.MB001= MOCTA.TA006 "//
			+ "	 LEFT JOIN "//
			+ "	 [DTR_TW].[dbo].INVMB AS INVMB "// --倉庫別
			+ "	 ON MOCTB.TB003 = INVMB.MB001 "//
			+ "	 LEFT JOIN "//
			+ "	 [DTR_TW].[dbo].CMSMC AS CMSMC "// --基本資料
			+ "	 ON INVMB.MB017 = CMSMC.MC001 "//
			+ "	 LEFT JOIN "//
			+ "	 [DTR_TW].[dbo].PURMA AS PURMA "// --廠商
			+ "	 ON PURMA.MA001 = INVMB.MB032 "//
			+ " WHERE "//
			// + " (MOCTA.TA011 = '1' OR MOCTA.TA011 = '2' OR MOCTA.TA011 = '3') "//
			+ " (MOCTA.TA011 = '1' OR MOCTA.TA011 = '2' OR MOCTA.TA011 = '3' OR MOCTA.TA011 = 'y' OR MOCTA.TA011 = 'Y') "//
			+ "	 AND (MOCTA.TA001='A511' OR MOCTA.TA001='A512' OR MOCTA.TA001='A521' OR MOCTA.TA001='A522') "//
			+ "	 AND (MOCTB.TB018 = 'Y' OR MOCTB.TB018 = 'N') "// --核單碼
			+ "  AND (MOCTB.CREATE_DATE >= CONVERT(VARCHAR(8), GETDATE()-500, 112) "//
			+ " OR MOCTB.MODI_DATE >= CONVERT(VARCHAR(8), GETDATE()-30, 112)) "// 今天
			+ "  AND (CONCAT(MOCTA.TA001, '-', TRIM(MOCTA.TA002), '-', INVMB.MB001) IN (:TA001TA002MB001)) "// 比對製令單+物料號?
			+ " ORDER BY "//
			+ "	 MOCTA.TA001+MOCTA.TA002 ASC,"// --工單號
			+ "	 INVMB.MB001 ASC,"// --物料
			+ "	 MOCTA.TA009 ASC"// --時間
			, nativeQuery = true) // coalesce 回傳非NULL值
	ArrayList<Mocta> findAllByMocta(List<String> TA001TA002MB001);// 工單號+物料號

	/**
	 * 查詢製令單缺料明細列表 (未領用需求)
	 * <p>
	 * 核心邏輯 ：
	 * 1. <b>資料範圍</b>：查詢已發行且尚未結案之製令單材料需求。
	 * 2. <b>狀態過濾</b>：
	 * - 單頭 (TA011)：包含「未確認(1)」、「已確認(2)」、「生產中(3)」。
	 * - 單身 (TB011)：排除「已結案/指定結案」之項目 (NOT IN 2,3,4)。
	 * - 需確認單身為有效 (TB018 = 'Y')。
	 * 3. <b>淨需求計算</b>：
	 * - 公式：應領用量 (TB004) - 已領用量 (TB005)。
	 * - 意義：反映產線當前尚未領取的實質缺料量。
	 * </p>
	 * * @return List 包含製令單號、預計領料日與淨缺料量的列表
	 * 
	 * @author Allen
	 */
	@Query(value = """
			SELECT
			    -- 製令單號 (加上 ISNULL 防止串接變 NULL)
				CONCAT(TRIM(MOCTA.TA001), '-', TRIM(MOCTA.TA002)) AS TA001_TA002,
			    MOCTA.TA006,            -- 產品品號
			    MOCTB.TB015 AS TA009,   -- 預計領料日 (TA009)
			    MOCTA.TA010,            -- 預計完工
			    MOCTB.TB017,            -- 備註
				MOCTA.TA032,			-- 加工廠商
			    -- 缺料計算
			    (MOCTB.TB004 - MOCTB.TB005) AS TB004_TB005,

			    INVMB.MB001,            -- 品號
			    INVMB.MB002,            -- 品名
			    INVMB.MB003,            -- 規格
			    INVMB.MB017,            -- 倉別代號
			    INVMB.MB032,            -- 供應商代號
			    INVMB.MB036,            -- 固定前置天數
			    INVMB.MB039,            -- 最低補量
			    INVMB.MB040,            -- 補貨倍量

				-- (新增) 客戶資訊 -------------------------
				COPTC.TC004,  	-- 客戶代號
				COALESCE(COPMA.MA002, '') AS COPMA002, 	-- 客戶名稱
				-- ---------------------------------------
			    COALESCE(CMSMC.MC002, '') AS MC002, -- 倉別名稱
			    COALESCE(PURMA.MA002, '') AS MA002, -- 供應商名稱
			    '製令單' AS TK000                   -- Doc Type

			FROM [DTR_TW].[dbo].MOCTA AS MOCTA

			LEFT JOIN [DTR_TW].[dbo].COPTC AS COPTC
			ON MOCTA.TA026 = COPTC.TC001	AND MOCTA.TA027 = COPTC.TC002

			LEFT JOIN [DTR_TW].[dbo].COPMA AS COPMA
				ON COPTC.TC004 = COPMA.MA001

			LEFT JOIN [DTR_TW].[dbo].MOCTB AS MOCTB
				ON MOCTA.TA001 = MOCTB.TB001 AND MOCTA.TA002 = MOCTB.TB002

			LEFT JOIN [DTR_TW].[dbo].INVMB AS INVMB
			    ON MOCTB.TB003 = INVMB.MB001

			LEFT JOIN [DTR_TW].[dbo].CMSMC AS CMSMC
			    ON INVMB.MB017 = CMSMC.MC001

			LEFT JOIN [DTR_TW].[dbo].PURMA AS PURMA
			    ON PURMA.MA001 = INVMB.MB032

			WHERE
			    (MOCTA.TA011 IN ('1', '2', '3'))
			    AND MOCTB.TB011 NOT IN ('2', '3', '4')
			    AND MOCTB.TB018 = 'Y'
			    AND (:materialNos IS NULL OR CHARINDEX(',' + TRIM(MOCTB.TB003) + ',', :materialNos) > 0)

			ORDER BY
			    INVMB.MB001 ASC,
			    MOCTA.TA009 ASC,
			    MOCTA.TA001, MOCTA.TA002 ASC
			""", nativeQuery = true)
	List<ValidatedMoctaDto> findAllByValidatedMocta(@Param("materialNos") String materialNos);

	/**
	 * 查詢內製令單明細列表 (半成品/自製件供給)
	 * <p>
	 * 核心邏輯 (Core Logic)：
	 * 1. <b>供給來源 (Source)</b>：
	 * - 鎖定「生產中」的製令 (MOCTAB1)，狀態包含 1,2,3。
	 * - 僅計算「剩餘預計產量」 (預計產量 TA015 - 已完工 TA017 > 0)。
	 * 2. <b>內部需求對應 (Dependency Check)</b>：
	 * - 透過 JOIN 檢查該製令產出物 (TA006) 是否為其他製令所需之原料 (TB003)。
	 * - 目的：追蹤「產線自給自足」的半成品進度。
	 * 3. <b>有效性過濾</b>：
	 * - 排除無對應需求之孤兒製令 (WHERE INVMB.MB001 IS NOT NULL)。
	 * </p>
	 * * @return List 包含預計完工日與剩餘產量的內製令列表
	 * 
	 * @author Allen
	 */
	@Query(value = """
			SELECT
			    CONCAT(RTRIM(LTRIM(MOCTAB1.TA001)), '-', RTRIM(LTRIM(MOCTAB1.TA002))) AS TA001_TA002,

			    MOCTAB1.TA009,       -- 預計完工日
			    MOCTAB1.TA015_TA017, -- 剩餘預計產量

			    INVMB.MB001,         -- 品號
			    INVMB.MB002,         -- 品名
			    INVMB.MB003,         -- 規格
			    INVMB.MB017,         -- 倉別代號
			    INVMB.MB032,         -- 供應商代號
			    INVMB.MB036,         -- 固定前置天數
			    INVMB.MB039,         -- 最低補量
			    INVMB.MB040,         -- 補貨倍量

			    COALESCE(CMSMC.MC002, '') AS MC002, -- 倉別名稱
			    COALESCE(PURMA.MA002, '') AS MA002, -- 供應商名稱
			    '內製令單' AS TK000                  -- 單別

			FROM
			    -- [Subquery 1] 生產中的工單 (MOCTAB1)
			    (SELECT
			        TH.TA001,
			        TH.TA002,
			        TH.TA006,
			        TH.TA010 AS TA009,
			        ((TH.TA015 - TH.TA017) - COALESCE(MOCTI_SUM.TI007_SUM, 0)) AS TA015_TA017
			    FROM DTR_TW.dbo.MOCTA AS TH
			    LEFT JOIN
			        (SELECT TI013, TI014, SUM(TI007) AS TI007_SUM 
			         FROM ( 
			           SELECT TI013, TI014, TI007 FROM DTR_TW.dbo.MOCTI 
			           WHERE TI035 = '1' AND TI037 != 'V' AND TI007 > 0 
			         ) AS TMP_MOCTI 
			         GROUP BY TI013, TI014) AS MOCTI_SUM  
			        ON TH.TA001 = MOCTI_SUM.TI013 AND TH.TA002 = MOCTI_SUM.TI014
			    WHERE
			        TH.TA011 IN ('1', '2', '3')
			        AND ((TH.TA015 - TH.TA017) - COALESCE(MOCTI_SUM.TI007_SUM, 0)) > 0
			        AND TH.TA013 != 'V'
			        AND TH.TA013 != 'N'
			    ) AS MOCTAB1

			LEFT JOIN
			    -- [Subquery 2] 需求來源 (MOCTAB2)
			    (SELECT
			        TB.TB003
			    FROM DTR_TW.dbo.MOCTA AS TH
			    LEFT JOIN DTR_TW.dbo.MOCTB AS TB
			        ON TH.TA001 = TB.TB001 AND TH.TA002 = TB.TB002
			    WHERE
			        TH.TA011 IN ('1', '2', '3')
			        AND (TB.TB004 - TB.TB005) > 0
			        AND TB.TB018 != 'V'
			    GROUP BY TB.TB003
			    ) AS MOCTAB2
			    ON MOCTAB1.TA006 = MOCTAB2.TB003

				-- 這樣如果 MOCTAB2 是 NULL (沒人要用這料)，INVMB 也會是 NULL
			LEFT JOIN DTR_TW.dbo.INVMB AS INVMB
			    ON MOCTAB2.TB003 = INVMB.MB001

			LEFT JOIN DTR_TW.dbo.CMSMC AS CMSMC
			    ON INVMB.MB017 = CMSMC.MC001

			LEFT JOIN DTR_TW.dbo.PURMA AS PURMA
			    ON PURMA.MA001 = INVMB.MB032

			WHERE
			    MOCTAB1.TA001 IS NOT NULL
			    -- 這裡會把沒對應到 MOCTAB2 的資料濾掉
			    AND INVMB.MB001 IS NOT NULL
			    AND (:materialNos IS NULL OR CHARINDEX(',' + TRIM(INVMB.MB001) + ',', :materialNos) > 0)

			ORDER BY
			    INVMB.MB001 ASC,
			    MOCTAB1.TA009 ASC,
			    MOCTAB1.TA001, MOCTAB1.TA002 ASC
			""", nativeQuery = true)
	List<ValidatedMoctabDto> findAllByValidatedMoctab(@Param("materialNos") String materialNos);

	/**
	 * 查詢 A521 製令單之領料統計 (含去重邏輯)
	 * <p>
	 * 核心邏輯 ：
	 * 1. <b>CTE 預處理 (UniqueMOCTB)</b>：
	 * - 使用 Window Function (ROW_NUMBER) 針對材料(TB003)與需領量(TB004)分組。
	 * - 依已領用量(TB005)遞減排序，取排名第 1 之紀錄，避免重複計算。
	 * 2. <b>主查詢關聯</b>：
	 * - 關聯條件嚴謹：品號對應 (TA006=TB003) 且 數量對應 (TA015=TB004)。
	 * 3. <b>篩選範圍</b>：
	 * - 單別：鎖定 'A521'。
	 * - 狀態：已完工 (Y)。
	 * - 時間：過去 6 個月。
	 * </p>
	 * * @return List<MaterialQtyDto> 包含品號與去重後領料總量(qty)的列表
	 * 
	 * @author Allen
	 */
	@Query(value = """
			WITH UniqueMOCTB AS (
			    SELECT DISTINCT
			        TB003,                               -- 材料品號
			        TB004,                               -- 需領用量
			        TB012,                               -- 材料品名
			        TB005,                               -- 已領用量
			        MOCTB.TB001,                         -- 訂單編號
			        MOCTB.TB002,                         -- 訂單序號
			        -- 使用 Window Function 取排名
			        ROW_NUMBER() OVER (PARTITION BY TB003, TB004 ORDER BY TB005 DESC) AS rn
			    FROM
			        DTR_TW.dbo.MOCTB MOCTB
			    WHERE
			        TB005 > 0 AND TB018 = 'Y'
			)

			-- 主查詢開始
			SELECT
			    MOCTA.TA006 AS MB001,                -- 品號 (作為 Key)

			    -- 為了配合 DTO 為 qty
			    COALESCE(SUM(MOCTB.TB005), 0) AS qty -- 總領用量

			FROM DTR_TW.dbo.MOCTA AS MOCTA           -- 製令單頭

			LEFT JOIN UniqueMOCTB AS MOCTB           -- Join 上面的 CTE
			    ON MOCTA.TA006 = MOCTB.TB003
			    AND MOCTA.TA015 = MOCTB.TB004
			    AND MOCTB.rn = 1                     -- 只取排名第 1 的那筆

			WHERE

			    MOCTA.TA003 >= CONVERT(varchar(8), DATEADD(MONTH, DATEDIFF(MONTH, 0, GETDATE()) - 6, 0), 112)
			    AND MOCTA.TA003 <= CONVERT(varchar(8), EOMONTH(GETDATE(), -1), 112)

			    -- 狀態篩選：已完工(Y)
			    AND MOCTA.TA011 = 'Y'

			    -- 單別篩選：A521
			    AND MOCTA.TA001 = 'A521'

			    AND MOCTB.TB005 IS NOT NULL
			    
			    -- 料號篩選 (優化效能：使用 CHARINDEX 並保留 INVMB 關聯)
                AND (:materialNos IS NULL OR CHARINDEX(',' + TRIM(MOCTA.TA006) + ',', :materialNos) > 0)

			GROUP BY
			    MOCTA.TA006

			ORDER BY
			    MB001 ASC
			""", nativeQuery = true)
	List<MaterialQtyDto> findMoctb005Qty(@Param("materialNos") String materialNos);
}