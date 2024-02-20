package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.BiosCustomerTag;

public interface BiosCustomerTagDao extends JpaRepository<BiosCustomerTag, Long> {

	// 查詢用
	@Query("SELECT c FROM BiosCustomerTag c WHERE "//
			+ "(:bctname is null or c.bctname LIKE %:bctname%) and "// 主要負責人
			+ "(:bctnabbreviation is null or c.bctnabbreviation LIKE %:bctnabbreviation%) and "//
			+ "(:sysstatus is null or c.sysstatus =:sysstatus)") //
	ArrayList<BiosCustomerTag> findAllBySearch(String bctname, String bctnabbreviation, Integer sysstatus,
			Pageable pageable);

	// 檢查用
	@Query("SELECT c FROM BiosCustomerTag c WHERE "//
			+ "(:bctname is null or c.bctname=:bctname) and "//
			+ "(:bctnabbreviation is null or c.bctnabbreviation=:bctnabbreviation) ")
	ArrayList<BiosCustomerTag> findAllByCheck(String bctname, String bctnabbreviation, Pageable pageable);

}