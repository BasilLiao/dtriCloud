package dtri.com.tw.pgsql.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import dtri.com.tw.pgsql.dto.MaterialShortageDto;
import dtri.com.tw.pgsql.entity.MaterialShortage;

public interface MaterialShortageDao extends JpaRepository<MaterialShortage, Long> {

        @Query("SELECT new dtri.com.tw.pgsql.dto.MaterialShortageDto(" +
                        "e.mb001, e.mb002, e.mb003, e.mb017, e.mc002, " +
                        "e.mb032, e.ma002, e.mb036, e.mb039, e.mb040, " +
                        "e.tk000, e.tk001, e.tk002, e.tk003, e.ta032, " + // tk002 不用 TRIM，直接丟
                                                                          // Object
                        "e.tc004, e.copma002, " +
                        "e.invmbmc007, e.syssy001, e.syssy002, e.syssy003, e.syssy004, " +
                        "e.syssy005, e.syssy006, e.syssy007, e.syssy008, e.syssy009, " +
                        "e.syssy011, e.mc004) " +
                        "FROM MaterialShortage e ")
        List<MaterialShortageDto> findAllData();

        /**
         * 取得不重複的客戶清單 (供前端規則設定使用)
         * 排除沒代號或沒名稱的資料
         */
        @Query("SELECT DISTINCT new dtri.com.tw.pgsql.dto.MaterialShortageDto(" +
                        "e.tc004, e.copma002) " +
                        "FROM MaterialShortage e " +
                        "WHERE e.tc004 IS NOT NULL AND e.tc004 != '' " +
                        "AND e.copma002 IS NOT NULL AND e.copma002 != '' " +
                        "ORDER BY e.tc004 ASC")
        List<MaterialShortageDto> findDistinctCustomers();

        /**
         * 取得不重複的產品清單 (供前端規則設定使用 Scope=2)
         */
        @Query("SELECT DISTINCT new dtri.com.tw.pgsql.dto.MaterialShortageDto(" +
                        "e.tk003) " +
                        "FROM MaterialShortage e " +
                        "WHERE e.tk003 IS NOT NULL AND e.tk003 != '' " +
                        "ORDER BY e.tk003 ASC")
        List<MaterialShortageDto> findDistinctProducts();
}
