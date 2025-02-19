package dtri.com.tw.mssql.dao;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.mssql.entity.MoctaScheduleOutsourcer;

public interface MoctaScheduleOutsourcerDao extends JpaRepository<MoctaScheduleOutsourcer, Long> {

	// 多筆查詢範例
	@Query(value = "SELECT "// --
			+ "  ROW_NUMBER() OVER (ORDER BY CTA.TA001) AS MOCTA_ID, "// --ID
			+ "  REPLACE(CTA.TA001+'-'+CTA.TA002, ' ', '') AS TA001_TA002,"// -- 製令單
			+ "  CTA.TA006, "// --產品品號
			+ "  CTA.TA034, "// --產品品名
			+ "  CTA.TA035, "// --產品規格
			+ "  CTA.TA015, "// --預計生產數
			+ "  CTA.TA017, "// --目前生產數
			+ "  CTA.TA009, "// --預計開工日
			+ "  CTA.TA010, "// --預計完工日
			+ "  CTA.TA011, "// --狀態碼1.未生產,2.已發料,3.生產中,Y.已完工,y.指定完工
			+ "	 CTA.TA013, "// -- 作廢狀態Y/N/V
			+ "  CTA.TA029, "// --製令備註(客戶/國家/訂單)
			+ "  CTA.TA054, "// --製令-自訂義備註(自動帶出)
			+ "  CTA.CREATOR, "// --創建工單使用者
			+ "  CTA.MODIFIER, "// --修改工單使用者
			+ "  CTA.CREATE_DATE, "// --單據建立時間
			+ "  CTA.MODI_DATE, "// --單據修改時間
			+ "  CTA.TA032, "// --加工廠(代號)
			+ "  ISNULL(PUR.MA002,'') AS MA002, "// --加工廠(中文)
			+ "  PTD.TD004, "// --客戶品號
			+ "  PTC.TC012, "// --客戶-訂單單號
			+ "  PTC.TC001+'-'+PTC.TC002 AS TC001_TC002 "// --公司-訂單單號
			+ "FROM "// --
			+ "[DTR_TW].[dbo].MOCTA AS CTA "// --
			+ "     LEFT JOIN [DTR_TW].[dbo].COPTD AS PTD ON (REPLACE(CTA.TA026+'-'+CTA.TA027+'-'+CTA.TA028, ' ', '') = REPLACE(PTD.TD001+'-'+PTD.TD002+'-'+PTD.TD003, ' ', '')) "// --
			+ "     LEFT JOIN [DTR_TW].[dbo].COPTC AS PTC ON (PTC.TC001+PTC.TC002 = PTD.TD001+PTD.TD002) "// --
			+ "     LEFT JOIN [DTR_TW].[dbo].PURMA AS PUR ON (PUR.MA001 = CTA.TA032) "// --
			+ "WHERE "// --
			+ "	(:ta013 is null OR CTA.TA013 = :ta013) "// -- 作廢狀態Y/N/V
			+ "	AND ((:ta001ta002 is null AND (CTA.TA011 = '1' OR CTA.TA011 = '2' OR CTA.TA011 = '3')) "
			+ " OR REPLACE(CTA.TA001+'-'+CTA.TA002, ' ', '') = :ta001ta002 ) "// --
			+ " AND (CTA.TA006 LIKE '81-105%' OR CTA.TA006 = '81-228-582070') "// --
			+ " AND ((CTA.TA001 = 'A511') OR (CTA.TA001= 'A521') "// -- 廠內 一般/重工製令單
			+ "	OR (CTA.TA001 = 'A512') OR (CTA.TA001= 'A522')) "// -- 廠外 一般/重工製令單
			+ "ORDER BY "// --
			+ "	CTA.TA001+CTA.TA002 ASC "// --時間
			, nativeQuery = true) // coalesce 回傳非NULL值
	ArrayList<MoctaScheduleOutsourcer> findAllByMocta(String ta001ta002, String ta013);

}