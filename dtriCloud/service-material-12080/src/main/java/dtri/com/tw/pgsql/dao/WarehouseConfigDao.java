package dtri.com.tw.pgsql.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.entity.WarehouseConfig;

public interface WarehouseConfigDao extends JpaRepository<WarehouseConfig, Long> {
    // 動態抓取有效倉別選單用
    @Query("SELECT c FROM WarehouseConfig c WHERE c.sysstatus = 0 AND c.wcmrcheck = true AND c.wcsepncheck = true ORDER BY c.wcalias ASC")
    List<WarehouseConfig> findAllWarehouses();
}
