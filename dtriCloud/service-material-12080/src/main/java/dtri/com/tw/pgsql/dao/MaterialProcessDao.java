package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.MaterialProcess;

public interface MaterialProcessDao extends JpaRepository<MaterialProcess, Long> {
    // 取得所有未作廢資料
    ArrayList<MaterialProcess> findAllBySysstatusNot(Integer status);

    // 檢查代號是否重複 (新增/修改時使用)
    ArrayList<MaterialProcess> findAllByMpnameAndSysstatusNot(String mpname, Integer status);

    // 關鍵字搜尋 (依代號或群組)
    @Query("SELECT m FROM MaterialProcess m WHERE m.sysstatus != 2 AND " +
            "(m.mpname LIKE %?1% OR m.mpgroup LIKE %?1%)")
    ArrayList<MaterialProcess> findAllByKeyword(String keyword);

    @Query("SELECT m FROM MaterialProcess m WHERE m.sysstatus != 2 "
            + "AND (?1 IS NULL OR m.mpname LIKE %?1%) "
            + "AND (?2 IS NULL OR m.mpgroup LIKE %?2%)")
    ArrayList<MaterialProcess> findAllBySearch(String mpname, String mpgroup, Pageable pageable);
}
