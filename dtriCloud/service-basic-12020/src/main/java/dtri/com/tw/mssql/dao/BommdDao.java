package dtri.com.tw.mssql.dao;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.mssql.entity.Bommd;

public interface BommdDao extends JpaRepository<Bommd, Long> {

	// 多筆查詢範例
	@Query(value = "SELECT " //
			+ "  ROW_NUMBER() OVER(order by BOMMD.MD001) AS BOMMD_ID, "//
			+ "  BOMMD.MD001, "// --主元件-品號
			+ "  INVMB.MB002, "// --主元件-品名
			+ "  INVMB.MB003, "// --主元件-規格
			+ "	 INVMB.MB009, "// --主元件-商品描述
			+ "  BOMMD.MD002, "// --子元件-序號
			+ "  BOMMD.MD003, "// --子元件-品號
			+ "  BOMMD.MD006, "// --子元件-用量
			+ "  BOMMD.MD009, "// --子元件-製成
			+ "  BOMMD.MD016, "// --子元件-備註
			+ "  INVMC.MB002 AS CMB002, "// --子元件-品名
			+ "  INVMC.MB003 AS CMB003, "// --子元件-規格
			+ "  INVMC.MB009 AS CMB009, "// --子元件-商品描述
			+ "  BOMMD.CREATE_DATE, "// --建立時間
			+ "  BOMMD.MODI_DATE, "// --修改時間
			+ "  BOMMD.CREATOR, "// --創建人
			+ "  BOMMD.MODIFIER "// --修改人
			+ " FROM [DTR_TW].[dbo].BOMMD AS BOMMD "//
			+ "     LEFT JOIN [DTR_TW].[dbo].INVMB AS INVMB "// --倉庫別
			+ "     ON BOMMD.MD001 = INVMB.MB001 " //
			+ "  	LEFT JOIN [DTR_TW].[dbo].INVMB AS INVMC "// --倉庫別
			+ "     ON BOMMD.MD003 = INVMC.MB001 "//
			+ " WHERE BOMMD.MD006 > 0 "//
			+ "ORDER BY BOMMD.MD001 ASC, BOMMD.MD002 ASC "//
			, nativeQuery = true) // coalesce 回傳非NULL值
	ArrayList<Bommd> findAllByBommdFirst();

	// 多筆查詢範例
	@Query(value = "SELECT " //
			+ "  ROW_NUMBER() OVER(order by BOMMD.MD001) AS BOMMD_ID, "//
			+ "  BOMMD.MD001, "// --主元件-品號
			+ "  INVMB.MB002, "// --主元件-品名
			+ "  INVMB.MB003, "// --主元件-規格
			+ "	 INVMB.MB009, "// --主元件-商品描述
			+ "  BOMMD.MD002, "// --子元件-序號
			+ "  BOMMD.MD003, "// --子元件-品號
			+ "  BOMMD.MD006, "// --子元件-用量
			+ "  BOMMD.MD009, "// --子元件-製成
			+ "  BOMMD.MD016, "// --子元件-備註
			+ "  INVMC.MB002 AS CMB002, "// --子元件-品名
			+ "  INVMC.MB003 AS CMB003, "// --子元件-規格
			+ "  INVMC.MB009 AS CMB009, "// --子元件-商品描述
			+ "  BOMMD.CREATE_DATE, "// --建立時間
			+ "  BOMMD.MODI_DATE, "// --修改時間
			+ "  BOMMD.CREATOR, "// --創建人
			+ "  BOMMD.MODIFIER "// --修改人
			+ " FROM [DTR_TW].[dbo].BOMMD AS BOMMD "//
			+ "     LEFT JOIN [DTR_TW].[dbo].INVMB AS INVMB "// --倉庫別
			+ "     ON BOMMD.MD001 = INVMB.MB001 "//
			+ "  	LEFT JOIN [DTR_TW].[dbo].INVMB AS INVMC "// --倉庫別
			+ "     ON BOMMD.MD003 = INVMC.MB001 "//
			+ " WHERE BOMMD.MD006 > 0 "//
			+ " AND (BOMMD.MODI_DATE > CONVERT(VARCHAR(8), GETDATE()-30, 112) "// --第一次資料導入忽視此條件
			+ " OR BOMMD.MODI_DATE > CONVERT(VARCHAR(8), GETDATE()-30, 112)) "// --第一次資料導入忽視此條件
			+ "ORDER BY BOMMD.MD001 ASC, BOMMD.MD002 ASC "//
			, nativeQuery = true) // coalesce 回傳非NULL值
	ArrayList<Bommd> findAllByBommd();
}