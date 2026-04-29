package dtri.com.tw.pgsql.dao;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dtri.com.tw.pgsql.entity.WarehouseInventory;

public interface WarehouseInventoryDao extends JpaRepository<WarehouseInventory, Long> {

    /**
     * 根據料號與多個倉別，加總計算帳務庫存數量 (wi_n_qty = INVMB_MC007)
     */
    @Query("SELECT COALESCE(SUM(c.winqty), 0) FROM WarehouseInventory c " +
           "WHERE c.wiwmpnb = :mb001 AND c.wiwaalias IN :warehouses")
    BigDecimal sumStockQtyByMb001AndWarehouses(@Param("mb001") String mb001, @Param("warehouses") List<String> warehouses);

    /**
     * 根據料號與多個倉別，加總計算待驗/在途數量 (wi_t_qty = SYS_SY005)
     */
    @Query("SELECT COALESCE(SUM(c.witqty), 0) FROM WarehouseInventory c " +
           "WHERE c.wiwmpnb = :mb001 AND c.wiwaalias IN :warehouses")
    BigDecimal sumTransitQtyByMb001AndWarehouses(@Param("mb001") String mb001, @Param("warehouses") List<String> warehouses);

    /**
     * 取得明細清單
     */
    List<WarehouseInventory> findAllByWiwmpnbAndWiwaaliasIn(String wiwmpnb, List<String> wiwaalias);
}
