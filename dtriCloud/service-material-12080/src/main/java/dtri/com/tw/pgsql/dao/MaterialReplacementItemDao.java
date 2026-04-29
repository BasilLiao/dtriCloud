package dtri.com.tw.pgsql.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dtri.com.tw.pgsql.entity.MaterialReplacementItem;

public interface MaterialReplacementItemDao extends JpaRepository<MaterialReplacementItem, Long> {

    /**
     * [反向搜尋核心 - N:N 的救星] ⭐⭐⭐
     * 功能：給定一個料號 (mrnb)，找出所有把它當作 "SOURCE" (被替代掉) 的規則。
     * * 這是 BOM 計算缺料時最常用的查詢！
     * 例如：缺 A，這裡會直接告訴你「ID=1001 的規則可以用」。
     */
    @Query("SELECT i FROM MaterialReplacementItem i " +
            "JOIN FETCH i.group g " + // 預先抓取 Group 資訊，避免 N+1 問題
            "WHERE " +
            "i.mrnb = :mrnb AND " +
            "i.role = 'SOURCE' AND " + // 只找「被替代」的角色
            "g.sysstatus = 0") // 確保規則本身是啟用的
    List<MaterialReplacementItem> findRulesBySourceMaterial(@Param("mrnb") String mrnb);

    /**
     * [Scope 優先級篩選]
     * 如果上一步找出了多個規則，這個查詢可以幫你進一步過濾 Scope。
     * 但通常建議在 Service 層用 Java Stream 過濾，因為 Logic 比較靈活。
     * 這裡先留一個簡單的查詢備用。
     */
    @Query("SELECT i FROM MaterialReplacementItem i " +
            "JOIN FETCH i.group g " +
            "WHERE i.mrnb = :mrnb AND i.role = 'SOURCE' AND g.sysstatus = 0 " +
            "ORDER BY g.scopetype DESC") // 讓特定 Scope 排前面 (假設數字大優先，或根據需求調整)
    List<MaterialReplacementItem> findRulesBySourceMaterialSorted(@Param("mrnb") String mrnb);
}