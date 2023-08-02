package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.db.entity.SystemGroup;

public interface SystemGroupDao extends JpaRepository<SystemGroup, Long> {

	// 查詢群組(群組ID)
	@Query("SELECT c FROM SystemGroup c "//
			+ "WHERE  (c.sggid = :sggid) "//
			+ "order by c.sggid asc, c.systemPermission.syssort asc")
	ArrayList<SystemGroup> findBySggidOrderBySggid(Long sggid);

	// 查詢ID(單一ID)
	List<SystemGroup> findBySgidOrderBySgidAscSyssortAsc(Long sgid);

	// 查詢群組名稱
	@Query("SELECT c FROM SystemGroup c "//
			+ "WHERE  (:sgname is null or c.sgname LIKE %:sgname%) and "// 指定-群組名稱
			// + "((:sggid) is null or c.sggid in (:sggid)) and"///* List<Long> sggid, */
			+ "(:sggid=0L or c.sggid = :sggid) and"// 指定-群組ID
			+ "(:notsggid=0L or c.sggid != :notsggid) and"// 排除-群組ID
			+ "(:notsysstatus is null or c.sysstatus != :notsysstatus ) and "// 排除-一般查詢(特定排除 Admin(3))/登入-排除(2資料作廢)/沒有要排除 (4)
			+ "(:sysstatus is null or c.sysstatus = :sysstatus ) "// 指定-顯示/沒有要指定 (4)
			+ "order by c.sggid asc,c.sysheader desc,c.systemPermission.syssort asc")
	List<SystemGroup> findAllBySystemGroup(String sgname, Long sggid, Long notsggid, //
			Integer sysstatus, Integer notsysstatus, Pageable pageable);

	// 查詢全部
	ArrayList<SystemGroup> findAll();

	// 查詢是否重複 群組名稱
	@Query("SELECT c FROM SystemGroup c WHERE  "//
			+ "(:sgname is null or c.sgname = :sgname) and "//
			+ "(:header is null or c.sysheader = :header) and "//
			+ "(:spname is null or c.systemPermission.spname = :spname)" //
			+ " order by c.sgid desc")
	ArrayList<SystemGroup> findAllByGroupHeader(String sgname, String spname, Boolean header, Pageable pageable);

	// 取得G_ID
	@Query(value = "SELECT NEXTVAL('system_group_g_seq')", nativeQuery = true)
	Long getSystemGroupGSeq();

	// 移除
	Long deleteBySggid(Long sggid);

}