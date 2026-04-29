package dtri.com.tw.pgsql.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import dtri.com.tw.pgsql.entity.MaterialReplacementGroup;

@Repository
public interface MaterialReplacementGroupDao extends JpaRepository<MaterialReplacementGroup, Long>, JpaSpecificationExecutor<MaterialReplacementGroup> {

        /**
         * [UI 綜合搜尋]
         * 修正重點：使用 CONCAT('%', :keyword, '%') 取代 %:keyword% 以確保相容性
         */
        @Query(value = "SELECT DISTINCT g FROM MaterialReplacementGroup g " +
                        "LEFT JOIN g.items i " +
                        "WHERE " +
                        "(:status IS NULL OR g.sysstatus = :status) AND " +
                        "(:scopetype IS NULL OR g.scopetype = :scopetype) AND " +
                        "(:scopeval IS NULL OR g.scopeval LIKE CONCAT('%', :scopeval, '%')) AND " + // 這裡也加 CONCAT
                        "(:keyword IS NULL OR (" +
                        "   g.mrgnb LIKE CONCAT('%', :keyword, '%') OR " + // 修正
                        "   i.mrnb LIKE CONCAT('%', :keyword, '%')" + // 修正
                        "))", countQuery = "SELECT count(DISTINCT g) FROM MaterialReplacementGroup g " +
                                        "LEFT JOIN g.items i " +
                                        "WHERE " +
                                        "(:status IS NULL OR g.sysstatus = :status) AND " +
                                        "(:scopetype IS NULL OR g.scopetype = :scopetype) AND " +
                                        "(:scopeval IS NULL OR g.scopeval LIKE CONCAT('%', :scopeval, '%')) AND " +
                                        "(:keyword IS NULL OR (" +
                                        "   g.mrgnb LIKE CONCAT('%', :keyword, '%') OR " +
                                        "   i.mrnb LIKE CONCAT('%', :keyword, '%')" +
                                        "))")
        Page<MaterialReplacementGroup> findAllBySearch(
                        @Param("status") Integer status,
                        @Param("scopetype") Integer scopeType,
                        @Param("scopeval") String scopeVal,
                        @Param("keyword") String keyword,
                        Pageable pageable);

        /**
         * [防呆檢查]
         */
        @Query("SELECT g FROM MaterialReplacementGroup g WHERE " +
                        "g.sysstatus = 0 AND " +
                        "g.mrgnb = :mrgnb AND " +
                        "g.scopetype = :scopetype AND " +
                        "(:scopeval IS NULL OR g.scopeval = :scopeval) AND " +
                        "(:excludeId IS NULL OR g.mrgid != :excludeId)")
        List<MaterialReplacementGroup> checkDuplicateName(
                        @Param("mrgnb") String mrgNb,
                        @Param("scopetype") Integer scopeType,
                        @Param("scopeval") String scopeVal,
                        @Param("excludeId") Long excludeId);

        /**
         * [核心] 支援 N對N 邏輯的查詢
         * 使用 LEFT JOIN FETCH 解決 N+1 問題
         */
        @Query("SELECT DISTINCT g FROM MaterialReplacementGroup g " +
                        "LEFT JOIN FETCH g.items " +
                        "WHERE g.sysstatus = 0")
        List<MaterialReplacementGroup> findAllActiveRules();
}