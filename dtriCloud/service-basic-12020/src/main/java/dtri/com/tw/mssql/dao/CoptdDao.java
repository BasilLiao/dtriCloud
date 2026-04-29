package dtri.com.tw.mssql.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dtri.com.tw.mssql.dto.ValidatedCoptdDto;
import dtri.com.tw.mssql.entity.Coptd;

public interface CoptdDao extends JpaRepository<Coptd, Long> {
    
    /**
     * 查詢有效客訂單明細列表 (未交貨需求)
     * <p>
     * 核心邏輯 (Core Logic)：
     * 1. <b>資料範圍</b>：查詢來自客戶端的正式訂單需求。
     * 2. <b>狀態過濾</b>：
     * - 僅包含「已確認 (Y)」之單據 (TD021 = 'Y')。
     * - 排除「已結案 (Y)」之單據，僅鎖定「未結案 (N)」(TD016 = 'N')。
     * 3. <b>淨需求計算</b>：
     * - 公式：訂單數量 (TD008) - 已交數量 (TD009)。
     * - 意義：反映當前尚未出貨的實質欠交量。
     * </p>
     * * @return List 包含訂單單號、預交日與淨未交量的客訂單列表
     * 
     * @author Allen
     */
    @Query(value = """
            SELECT
                -- 1. 單號串接 (使用 CONCAT 防止 NULL)
                CONCAT(RTRIM(LTRIM(COPTD.TD001)), '-', RTRIM(LTRIM(COPTD.TD002))) AS TD001_TD002,

                -- 2. 計算未交量
                (COPTD.TD008 - COPTD.TD009) AS TD008_TD009,
                COPTD.TD013, -- 預交日
                INVMB.MB001, -- 品號
                INVMB.MB002, -- 品名
                INVMB.MB003, -- 規格
                INVMB.MB017, -- 倉別代號
                INVMB.MB032, -- 供應商代號
                INVMB.MB036, -- 固定前置天數
                INVMB.MB039, -- 最低補量
                INVMB.MB040, -- 補貨倍量

                COALESCE(CMSMC.MC002, '') AS MC002, -- 倉別名稱
                COALESCE(PURMA.MA002, '') AS MA002, -- 供應商名稱
                '客訂單' AS TK000
            FROM DTR_TW.dbo.COPTD AS COPTD
            LEFT JOIN DTR_TW.dbo.INVMB AS INVMB
                ON COPTD.TD004 = INVMB.MB001
            LEFT JOIN DTR_TW.dbo.CMSMC AS CMSMC
                ON INVMB.MB017 = CMSMC.MC001
            LEFT JOIN DTR_TW.dbo.PURMA AS PURMA
                ON PURMA.MA001 = INVMB.MB032
            WHERE
                COPTD.TD021 = 'Y'           -- 已確認
                AND COPTD.TD016 = 'N'       -- 未結案
                AND (:materialNos IS NULL OR CHARINDEX(',' + TRIM(INVMB.MB001) + ',', :materialNos) > 0)
            ORDER BY
                INVMB.MB001 ASC,
                COPTD.TD013 ASC,
                COPTD.TD001, COPTD.TD002, COPTD.TD003 ASC
            """, nativeQuery = true)
    List<ValidatedCoptdDto> findAllByValidatedCoptd(@Param("materialNos") String materialNos);
}
