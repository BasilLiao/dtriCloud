package dtri.com.tw.mssql.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dtri.com.tw.mssql.dto.ValidatedPurtcDto;
import dtri.com.tw.mssql.entity.Purtc;

public interface PurtcDao extends JpaRepository<Purtc, Long> {
   
    /**
     * 查詢有效採購單明細列表 (未交貨部分)
     * <p>
     * 核心邏輯 (Core Logic)：
     * 1. <b>單據狀態</b>：僅包含「已審核(Y)」且單身「未結束(N)」之採購單。
     * 2. <b>待驗量扣除</b>：透過 Subquery 統計進貨單 (PURTH) 中「未驗收」的數量。
     * 3. <b>淨未交量計算</b>：
     * - 公式：採購量 (TD008) - 已交量 (TD015) - 待驗中 (TH007)
     * - 篩選：上述運算結果 > 0 才視為有效需求。
     * </p>
     * * @return List 包含詳細物料、廠商資訊與淨未交量的採購單列表
     * 
     * @author Allen
     */
    @Query(value = """
            SELECT
                TC.CREATE_DATE AS CreateDate, -- 建立時間

                -- 單號串接 (CONCAT 自動處理 NULL)
                CONCAT(RTRIM(LTRIM(TC.TC001)), '-', RTRIM(LTRIM(TC.TC002))) AS TC001_TC002,

                TD.TD003, -- 序號
                TD.TD012, -- 預交日

                -- 計算公式：採購量(TD008) - 已交量(TD015) - 待驗量(TH007)
                (TD.TD008 - TD.TD015 - COALESCE(TH.TH007, 0)) AS TD008_TH007,

                MB.MB001, -- 品號
                MB.MB002, -- 品名
                MB.MB003, -- 規格
                MB.MB017, -- 倉別代號
                TC.TC004 AS MB032, -- 供應商代號 (注意：這裡用 TC004)
                MB.MB036, -- 固定前置天數
                MB.MB039, -- 最低補量
                MB.MB040, -- 補貨倍量

                COALESCE(MC.MC002, '') AS MC002, -- 倉別名稱
                COALESCE(MA.MA002, '') AS MA002, -- 供應商名稱
                COALESCE(TH.TH007, 0)  AS TH007, -- 代驗中量
                '採購單' AS TK000

            FROM DTR_TW.dbo.PURTC AS TC -- 採購單頭

            LEFT JOIN DTR_TW.dbo.PURTD AS TD -- 採購單身
                -- 拆開 JOIN 條件
                ON TC.TC001 = TD.TD001 AND TC.TC002 = TD.TD002

            LEFT JOIN DTR_TW.dbo.INVMB AS MB -- 物料檔
                ON TD.TD004 = MB.MB001

            LEFT JOIN DTR_TW.dbo.CMSMC AS MC -- 倉別檔
                ON MB.MB017 = MC.MC001

            LEFT JOIN DTR_TW.dbo.PURMA AS MA -- 廠商檔
                ON MA.MA001 = TC.TC004

            -- [Subquery] 進貨單 (計算待驗量)
            LEFT JOIN (
                SELECT
                    TH011, -- 採購單別
                    TH012, -- 採購單號
                    TH013, -- 採購序號
                    SUM(TH007) AS TH007 -- 累加待驗量
                FROM DTR_TW.dbo.PURTH
                WHERE
                    TH028 != '3'         -- 排除結案
                    AND TH030 LIKE 'N'   -- 未驗收
                    AND TH007 > 0
                GROUP BY
                    TH011, TH012, TH013
            ) AS TH

                ON  TH.TH011 = TC.TC001
                AND TH.TH012 = TC.TC002
                AND TH.TH013 = TD.TD003

            WHERE
                TD.TD016 = 'N'              -- 未結束
                AND TC.TC014 = 'Y'          -- 已審核
                AND (TD.TD008 - TD.TD015) > 0 -- 未交量 > 0
                -- 核心邏輯：扣掉待驗後還要 > 0
                AND (TD.TD008 - TD.TD015 - COALESCE(TH.TH007, 0)) > 0
                AND (:materialNos IS NULL OR CHARINDEX(',' + TRIM(MB.MB001) + ',', :materialNos) > 0)

            ORDER BY
                MB.MB001 ASC,
                TD.TD012 ASC,
                TC.TC001, TC.TC002, TD.TD003 ASC
            """, nativeQuery = true)
    List<ValidatedPurtcDto> findAllByValidatedPurtc(@Param("materialNos") String materialNos);
}
