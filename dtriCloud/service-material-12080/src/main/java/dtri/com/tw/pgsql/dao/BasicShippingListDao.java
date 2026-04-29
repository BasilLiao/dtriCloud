package dtri.com.tw.pgsql.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.dto.MaterialShortageDto;
import dtri.com.tw.pgsql.entity.BasicShippingList;

public interface BasicShippingListDao extends JpaRepository<BasicShippingList, Long> {

    /**
     * 取得不重複的客戶清單 (供前端規則設定使用)
     * 從 BasicShippingList 提取 bsl_from_customer (同時作為代號及名稱)
     */
    @Query("SELECT DISTINCT new dtri.com.tw.pgsql.dto.MaterialShortageDto(" +
           "e.bslfromcustomer, e.bslfromcustomer) " +
           "FROM BasicShippingList e " +
           "WHERE e.bslfromcustomer IS NOT NULL AND e.bslfromcustomer != '' " +
           "ORDER BY e.bslfromcustomer ASC")
    List<MaterialShortageDto> findDistinctCustomers();

    /**
     * 取得不重複的產品清單 (供前端規則設定使用 Scope=2)
     * 從 BasicShippingList 提取 bsl_p_a_number (成品品號)
     */
    @Query("SELECT DISTINCT new dtri.com.tw.pgsql.dto.MaterialShortageDto(" +
           "e.bslpnumber) " +
           "FROM BasicShippingList e " +
           "WHERE e.bslpnumber LIKE '90-%' " +
           "ORDER BY e.bslpnumber ASC")
    List<MaterialShortageDto> findDistinctProducts();
}
