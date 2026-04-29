package dtri.com.tw.mssql.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dtri.com.tw.mssql.dto.MaterialQtyDto;
import dtri.com.tw.mssql.entity.Invtb;

public interface InvtbDao extends JpaRepository<Invtb, Long> {

	// 多筆查詢範例
	@Query(value = "SELECT	"// --倉儲
			+ "	TRIM(INVMB.MB001)+'_'+TRIM(ISNULL(INVMC.MC002,''))+'_'+TRIM(ISNULL(INVMC.MC003, '')) AS INVTB_ID,"//
			+ "	INVMB.MB001, "// --品號
			+ "	INVMB.MB002, "// --品名
			+ "	INVMB.MB003, "// --規格
			+ "	INVMB.MB009, "// --商品描述
			+ "	ISNULL(INVMC.MC002,'') AS MC002, "// --倉別代號
			+ "	CMSMC.MC002 AS CMC002, "// --倉別名稱
			+ "	ISNULL(INVMC.MC003,'') AS MC003, "// --儲位位置
			+ "	FLOOR(INVMC.MC007) AS MC007, "// --儲位數量
			+ "	INVMB.MB017, "// --主要-倉別代號
			+ "	INVMB.MB032, "// --主要-供應商代號
			+ "	INVMB.MB036, "// --主要-固定前置天數
			+ "	INVMB.MB039, "// --主要-最低補量
			+ "	INVMB.MB040, "// --主要-補貨倍量
			+ "	COALESCE(PURMA.MA002,'') AS MA002 "// --供應商名稱
			+ "FROM "//
			+ "	[DTR_TW].[dbo].INVMB  "// --倉庫別
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].INVMC AS INVMC "// --品號庫別檔
			+ "	ON INVMB.MB001 = INVMC.MC001 "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].CMSMC AS CMSMC "// --基本資料
			+ "	ON INVMC.MC002 = CMSMC.MC001 "//
			+ "	LEFT JOIN "//
			+ "	[DTR_TW].[dbo].PURMA AS PURMA "// --廠商
			+ "	ON PURMA.MA001 = INVMB.MB032 "//
			// + "WHERE "//
			// + " INVMC.MC007 is not null "//
			// + " AND (INVMC.MC007 >=1 OR INVMC.MC007 =0) "//
			+ "ORDER BY " //
			+ "	INVMB.MB001 ASC,"//
			+ "	INVMC.MC002 ASC,"//
			+ "	INVMC.MC003 ASC "// --單號+序號
			, nativeQuery = true) // coalesce 回傳非NULL值
	ArrayList<Invtb> findAllByMoctb();

	/**
	 * 查詢 A111 庫存異動/調撥統計
	 * <p>
	 * 核心邏輯 ：
	 * 1. <b>資料範圍</b>：鎖定單別 'A111' 之庫存異動單據。
	 * 2. <b>狀態過濾</b>：
	 * - 單身確認碼 (TB018) 需為 'Y'。
	 * - 單頭過帳碼 (TA016) 需為 '3' (代表已確認過帳)。
	 * 3. <b>時間區間</b>：僅計算過去 6 個月內之異動紀錄。
	 * </p>
	 * * @return List<MaterialQtyDto> 包含品號與異動總量(qty)的列表
	 * 
	 * @author Allen
	 */
	@Query(value = """
            SELECT
                INVMB.MB001,                                 -- 品號
                INVMB.MB002,                                 -- 品名

                -- 配合 DTO 為 qty
                COALESCE(SUM(INVTB.TB007), 0) AS qty

            FROM DTR_TW.dbo.INVTA AS INVTA                   -- 異動單頭

            LEFT JOIN DTR_TW.dbo.INVTB AS INVTB              -- 異動單身
                ON (INVTA.TA001 + '-' + TRIM(INVTA.TA002)) = (INVTB.TB001 + '-' + TRIM(INVTB.TB002))

            LEFT JOIN DTR_TW.dbo.INVMB AS INVMB              -- 品號資料
                ON INVTB.TB004 = INVMB.MB001

            WHERE
                INVTB.TB001 IS NOT NULL
                AND INVTB.TB007 != 0
                AND INVTB.TB001 = 'A111'

                AND INVTA.TA014 >= CONVERT(varchar(8), DATEADD(MONTH, DATEDIFF(MONTH, 0, GETDATE()) - 6, 0), 112)
                AND INVTA.TA014 <= CONVERT(varchar(8), EOMONTH(GETDATE(), -1), 112)

                AND INVTB.TB018 = 'Y'                        -- 確認碼
                AND INVTA.TA016 = '3'

                -- 料號篩選 (優化效能：使用 CHARINDEX 並保留 INVMB 關聯)
                AND (:materialNos IS NULL OR CHARINDEX(',' + TRIM(INVMB.MB001) + ',', :materialNos) > 0)

            GROUP BY
                INVMB.MB001,
                INVMB.MB002

            ORDER BY
                INVMB.MB001 ASC
            """, nativeQuery = true)
    List<MaterialQtyDto> getImvtb007QtyList(@Param("materialNos") String materialNos);

}