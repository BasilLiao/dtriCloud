package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.db.entity.SystemLanguageCell;
import dtri.com.tw.db.entity.SystemUser;

public interface SystemLanguageCellDao extends JpaRepository<SystemLanguageCell, Long> {

	// 帳號查詢
	SystemUser findBySlspcontrol(String slspcontrol);

	// 查詢全部
	ArrayList<SystemLanguageCell> findAll();

	// 查詢ID
	ArrayList<SystemLanguageCell> findAllBySlid(Long id);

	// 查詢全部含-頁數
	@Query("SELECT c FROM SystemLanguageCell c "//
			+ "WHERE (:slspcontrol is null or c.slspcontrol LIKE %:slspcontrol% ) and"//
			+ "(:sltarget is null or c.sltarget LIKE %:sltarget% ) and"//
			+ "(:slclass =null or:slclass =0 or c.slclass =:slclass ) ")
	ArrayList<SystemLanguageCell> findAllBySysLCell(String slspcontrol, String sltarget, Integer slclass, Pageable pageable);

	// 查詢全部含(精準)-頁數
	@Query("SELECT c FROM SystemLanguageCell c "//
			+ "WHERE (:slspcontrol is null or c.slspcontrol = :slspcontrol ) and"//
			+ "(:sltarget is null or c.sltarget = :sltarget ) and" //
			+ "(:slclass =null or:slclass =0 or c.slclass =:slclass ) ")
	ArrayList<SystemLanguageCell> findAllByLanguageCellSame(String slspcontrol, String sltarget, Integer slclass);

	// 移除
	Long deleteBySlid(Long suid);

}