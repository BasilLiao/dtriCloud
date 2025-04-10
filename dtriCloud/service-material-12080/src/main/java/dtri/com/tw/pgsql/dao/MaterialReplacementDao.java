package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.MaterialReplacement;

public interface MaterialReplacementDao extends JpaRepository<MaterialReplacement, Long> {


	// 查詢全部
	ArrayList<MaterialReplacement> findAll();

	// 查詢全部含-頁數
	@Query("SELECT c FROM MaterialReplacement c "//
			+ "WHERE (:mrnb is null or c.mrnb LIKE %:mrnb% ) and "//
			+ "(:mrnote is null or c.mrnote LIKE %:mrnote% ) and "//
			+ "(:mrsubnote is null or c.mrsubnote LIKE %:mrsubnote% ) ") //
	ArrayList<MaterialReplacement> findAllBySearch(String mrnb, String mrnote, String mrsubnote, Pageable pageable);

	// 檢查
	@Query("SELECT c FROM MaterialReplacement c "//
			+ "WHERE (:mrnb  is null or c.mrnb  =:mrnb  ) and "//
			+ "(:mrnote is null or c.mrnote = :mrnote ) and "//
			+ "(:mrsubnote is null or c.mrsubnote =:mrsubnote ) ") //
	ArrayList<MaterialReplacement> findAllByCheck(String mrnb, String mrnote, String mrsubnote);

	// 多筆查詢範例
//	@Query(" SELECT i.suname FROM MaterialReplacement i WHERE "//
//			+ "(coalesce(:accounts, null) is null or i.suaccount IN :accounts ) ") // coalesce 回傳非NULL值
//	ArrayList<String> readAccounts(List<String> accounts);

}