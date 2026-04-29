package dtri.com.tw.pgsql.dao;

import java.util.List;
import java.util.Map;
import dtri.com.tw.pgsql.entity.MaterialVirtualProject;

/**
 * 自定義 DAO 介面，用於處理複雜的原生 SQL (如 BOM 展開、齊套模擬)
 */
public interface MaterialVirtualProjectDaoCustom {

    // 1. BOM 展開分析
    List<Object[]> analyzeBomTree(List<String> bomSnList, int maxDepth);
    List<Object[]> getMissingMaterialsInfo(List<String> missingSns);
    List<Object[]> getInventoryQuantities(List<String> items, List<String> warehouses);

    // 2. 齊套模擬
    List<Object[]> executeSimulationSql(String virtualdate, int virtualqty, List<String> warehouses, 
                                        List<Map<String, Object>> allItems,
                                        List<Map<String, Object>> otherProjects,
                                        List<MaterialVirtualProject> extEntities,
                                        Map<String, Double> extDemandByItem,
                                        Map<String, List<Map<String, Object>>> extDemandDetailByItem);

    // 3. 沖銷邏輯與 BOM 樹關聯查詢
    List<Object[]> getBomChildren(List<String> parentSns);
    List<Object[]> getBomEdges(List<String> parentItems);
}
