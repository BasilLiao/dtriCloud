package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.MaterialVirtualProject;

public interface MaterialVirtualProjectDao extends JpaRepository<MaterialVirtualProject, Long>, MaterialVirtualProjectDaoCustom {

    // 取得所有未作廢資料
    ArrayList<MaterialVirtualProject> findAllBySysstatusNot(Integer status);

    // 依名稱搜尋 (模糊比對，不區分大小寫)
    @Query("SELECT m FROM MaterialVirtualProject m WHERE m.sysstatus != 2 AND LOWER(m.mvpname) LIKE LOWER(CONCAT('%', ?1, '%'))")
    ArrayList<MaterialVirtualProject> findAllByKeyword(String keyword);

    // 檢查名稱是否重複
    ArrayList<MaterialVirtualProject> findAllByMvpnameAndSysstatusNot(String mvpname, Integer status);

    // 取得所有其他使用者的專案 (排除指定 mvpId)
    @Query("SELECT m FROM MaterialVirtualProject m WHERE m.sysstatus != 2 AND m.mvpid != ?1")
    ArrayList<MaterialVirtualProject> findOtherProjects(Long excludeMvpId);
}
