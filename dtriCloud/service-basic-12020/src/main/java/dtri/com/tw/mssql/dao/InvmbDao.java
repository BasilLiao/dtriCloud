package dtri.com.tw.mssql.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dtri.com.tw.mssql.dto.ValidatedInvmbDto;
import dtri.com.tw.mssql.entity.Invmb;

public interface InvmbDao extends JpaRepository<Invmb, Long> {

    /**
     * 查詢各料號之現有庫存總量 (依物料匯總)
     * <p>
     * 核心邏輯 (Core Logic):
     * 1. <b>資料主體</b>：以物料主檔 (INVMB) 為基準，確保所有料號皆被納入計算。
     * 2. <b>庫存計算</b>：
     * - 加總庫存表 (INVMC) 之數量 (MC007)。
     * - 若無庫存紀錄 (NULL)，則透過 COALESCE 回傳 0。
     * 3. <b>篩選條件 (Where Clause)</b>：
     * - <b>情境 A (有效庫存)</b>：納入「有效倉 (MC005='Y')」或「倉別屬性未定義 (NULL)」之庫存。
     * - <b>情境 B (無庫存)</b>：保留完全無庫存紀錄 (MC.MC007 IS NULL) 之料號，確保報表完整性。
     * </p>
     *
     * @return List<ValidatedInvmbkDto> 包含品號 (MB001) 與 庫存總量 (MC007) 的 DTO 列表
     * @author Allen
     */
    @Query(value = """
            SELECT
                MB.MB001, -- 品號

                -- 計算總庫存：若無庫存紀錄則回傳 0
                COALESCE(SUM(MC.MC007), 0) AS MC007

            FROM DTR_TW.dbo.INVMB AS MB -- 主表：物料

            LEFT JOIN DTR_TW.dbo.INVMC AS MC -- 庫存表
                ON MB.MB001 = MC.MC001
                AND (:warehouses IS NULL OR CHARINDEX(',' + TRIM(MC.MC002) + ',', :warehouses) > 0)

            LEFT JOIN DTR_TW.dbo.CMSMC AS CMSMC -- 倉別資料
                ON MC.MC002 = CMSMC.MC001

            LEFT JOIN DTR_TW.dbo.PURMA AS PURMA
                ON PURMA.MA001 = MB.MB032

            WHERE
                (
                    -- 情況 1: 有庫存紀錄
                    (
                        (CMSMC.MC005 = 'Y' OR CMSMC.MC005 IS NULL) -- 納入有效倉
                    )
                )
                OR
                (
                    -- 情況 2: 完全沒有庫存紀錄 (但包含有建料號)
                    MC.MC007 IS NULL
                )

            GROUP BY
                MB.MB001

            HAVING
                (:materialNos IS NULL OR CHARINDEX(',' + TRIM(MB.MB001) + ',', :materialNos) > 0)

            ORDER BY
                MB.MB001 ASC
            """, nativeQuery = true)
    List<ValidatedInvmbDto> sumStockByMaterial(@Param("warehouses") String warehouses, @Param("materialNos") String materialNos);

}
