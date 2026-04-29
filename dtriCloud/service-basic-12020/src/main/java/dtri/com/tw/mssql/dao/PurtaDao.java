package dtri.com.tw.mssql.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dtri.com.tw.mssql.dto.ValidatedPurtaDto;
import dtri.com.tw.mssql.entity.Purta;

public interface PurtaDao extends JpaRepository<Purta, Long> {
    /**
     * 查詢有效請購單明細列表
     * <p>
     * 篩選邏輯 (Filtering Logic)：
     * 1. <b>單據狀態</b>：僅包含「簽核中(3)」或「已核准(1)」且未作廢之單據。
     * 2. <b>有效性檢查</b>：單頭 (TA007) 與 單身 (TB025) 皆不可為作廢狀態 (V)。
     * 3. <b>結案判定</b>：
     * - 納入「未結案 (N)」之單據。
     * - 特別納入「已結案 (Y)」但後續採購單「未核准 (N)」之回流單據。
     * </p>
     * * @return List 包含詳細物料、廠商與倉別資訊的請購單列表
     * 
     * @author Allen
     */
    @Query(value = """
            SELECT
                -- 1. 單號串接
                CONCAT(RTRIM(LTRIM(TB.TB001)), '-', RTRIM(LTRIM(TB.TB002))) AS TB001_TB002,

                TB.TB003, -- 序號
                TB.TB009, -- 數量
                TB.TB019 AS TB011, -- 交貨日

                MB.MB001, -- 品號
                MB.MB002, -- 品名
                MB.MB003, -- 規格
                MB.MB017, -- 倉別代號
                MB.MB032, -- 供應商代號
                MB.MB036, -- 固定前置天數
                MB.MB039, -- 最低補量
                MB.MB040, -- 補貨倍量

                COALESCE(MC.MC002, '') AS MC002, -- 倉別名稱
                COALESCE(MA.MA002, '') AS MA002, -- 供應商名稱
                '請購單' AS TK000

            FROM DTR_TW.dbo.PURTA AS TA -- 請購頭

            LEFT JOIN DTR_TW.dbo.PURTB AS TB -- 請購身
                ON TA.TA001 = TB.TB001 AND TA.TA002 = TB.TB002

            LEFT JOIN DTR_TW.dbo.PURTC AS TC
                ON LEFT(TB.TB022, 15) = CONCAT(RTRIM(TC.TC001), '-', RTRIM(TC.TC002))

            LEFT JOIN DTR_TW.dbo.INVMB AS MB -- 物料檔
                ON TB.TB004 = MB.MB001

            LEFT JOIN DTR_TW.dbo.CMSMC AS MC -- 倉別檔
                ON MB.MB017 = MC.MC001

            LEFT JOIN DTR_TW.dbo.PURMA AS MA -- 廠商檔
                ON MA.MA001 = MB.MB032

            WHERE
                (TA.TA015 = '3' OR TA.TA015 = '1') -- 簽核中 OR 已核准
                AND TA.TA007 != 'V'               -- 單頭未作廢
                AND TB.TB025 != 'V'               -- 單身未作廢
                AND TB.TB009 > 0                  -- 數量 > 0

                -- (未結案) OR (已結案 但 採購單未核准)
                AND (
                    (TB.TB033 = 'N')
                    OR
                    (TB.TB033 = 'Y' AND TC.TC014 = 'N')
                )
                AND (:materialNos IS NULL OR CHARINDEX(',' + TRIM(MB.MB001) + ',', :materialNos) > 0)
            ORDER BY
                MB.MB001 ASC,
                TB.TB019 ASC,
                TB.TB001, TB.TB002, TB.TB003 ASC
            """, nativeQuery = true)
    List<ValidatedPurtaDto> findAllByValidatedPurta(@Param("materialNos") String materialNos);
}
