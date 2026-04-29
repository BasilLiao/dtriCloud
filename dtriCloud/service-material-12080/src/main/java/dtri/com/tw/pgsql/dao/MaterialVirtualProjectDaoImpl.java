package dtri.com.tw.pgsql.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import dtri.com.tw.pgsql.entity.MaterialVirtualProject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

/**
 * MaterialVirtualProjectDao 的自定義實作類別
 * 負責所有複雜的原生 SQL 構建與執行
 */
public class MaterialVirtualProjectDaoImpl implements MaterialVirtualProjectDaoCustom {

    @PersistenceContext
    private EntityManager em;

    private final Gson gson = new Gson();

    @Override
    @SuppressWarnings("unchecked")
    public List<Object[]> analyzeBomTree(List<String> bomSnList, int maxDepth) {
        StringBuilder sql = new StringBuilder();
        sql.append("WITH RECURSIVE ");
        sql.append("all_parents AS ( ");
        sql.append("  SELECT DISTINCT bbi_sn AS sn FROM basic_bom_ingredients ");
        sql.append("), ");
        sql.append("bom_tree AS ( ");
        sql.append("  SELECT bbi_sn AS rootbom, bbi_i_sn AS itemno, bbi_i_name AS itemname, ");
        sql.append("         bbi_i_specification AS itemspec, ");
        sql.append("         bbi_i_qty AS qty, 1 AS depth ");
        sql.append("  FROM basic_bom_ingredients ");
        sql.append("  WHERE bbi_sn IN (:bomList) ");
        sql.append("  UNION ALL ");
        sql.append("  SELECT bt.rootbom, b.bbi_i_sn, b.bbi_i_name, ");
        sql.append("         b.bbi_i_specification, ");
        sql.append("         bt.qty * CASE WHEN b.bbi_i_qty = 0 THEN 1 ELSE b.bbi_i_qty END, ");
        sql.append("         bt.depth + 1 ");
        sql.append("  FROM bom_tree bt ");
        sql.append("  INNER JOIN basic_bom_ingredients b ON b.bbi_sn = bt.itemno ");
        sql.append("  WHERE bt.depth < :maxDepth ");
        sql.append(") ");
        sql.append("SELECT bt.itemno AS itemno, bt.itemname AS itemname, bt.itemspec AS itemspec, ");
        sql.append("       COUNT(DISTINCT bt.rootbom) AS inbomcount, ");
        sql.append("       SUM(bt.qty) AS totalqty, ");
        sql.append("       MIN(bt.depth) AS mindepth, ");
        sql.append("       BOOL_AND(ap.sn IS NULL) AS isleaf ");
        sql.append("FROM bom_tree bt ");
        sql.append("LEFT JOIN all_parents ap ON ap.sn = bt.itemno ");
        sql.append("GROUP BY bt.itemno, bt.itemname, bt.itemspec ");
        sql.append("ORDER BY inbomcount DESC, mindepth ASC, totalqty DESC");

        Query query = em.createNativeQuery(sql.toString());
        query.setParameter("bomList", bomSnList);
        query.setParameter("maxDepth", maxDepth);
        return query.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object[]> getMissingMaterialsInfo(List<String> missingSns) {
        String sql = "SELECT UPPER(TRIM(wm_p_nb)) AS itemno, TRIM(wm_name) AS itemname, TRIM(wm_specification) AS itemspec "
                + "FROM warehouse_material "
                + "WHERE UPPER(TRIM(wm_p_nb)) IN (:missing) AND sys_status = 0 "
                + "ORDER BY wm_p_nb";
        Query query = em.createNativeQuery(sql);
        query.setParameter("missing", missingSns);
        return query.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object[]> getInventoryQuantities(List<String> items, List<String> warehouses) {
        String sql = "SELECT UPPER(TRIM(wa_wm_p_nb)) AS itemno, COALESCE(SUM(wa_erp_t_qty), 0) AS stock "
                + "FROM warehouse_area "
                + "WHERE UPPER(TRIM(wa_wm_p_nb)) IN (:items) "
                + "AND wa_alias IN (:warehouses) "
                + "GROUP BY UPPER(TRIM(wa_wm_p_nb))";
        Query query = em.createNativeQuery(sql);
        query.setParameter("items", items);
        query.setParameter("warehouses", warehouses);
        return query.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object[]> executeSimulationSql(String virtualdate, int virtualqty, List<String> warehouses,
            List<Map<String, Object>> allItems, List<Map<String, Object>> otherProjects,
            List<MaterialVirtualProject> extEntities, Map<String, Double> extDemandByItem,
            Map<String, List<Map<String, Object>>> extDemandDetailByItem) {

        StringBuilder sql = new StringBuilder();

        // CTE 1: 專案內所有主副物料
        sql.append(buildMvpItemsCte(allItems, virtualqty));

        // 取得自訂單據與外部專案競爭的 SQL 片段
        String customOrdersSql = buildCustomOrdersSql(allItems);
        String externalDemandSql = buildExternalDemandSql(allItems, otherProjects, extEntities,
                extDemandByItem, extDemandDetailByItem);

        // 組合 Timeline
        sql.append("Timeline AS ( \n");
        sql.append("    SELECT * FROM ( \n");
        sql.append("        -- 初始庫存 \n");
        sql.append(
                "        SELECT TRIM(V.itemno) AS itemno, CAST('1900-01-01' AS TIMESTAMP) AS transdate, '初始庫存' AS doctype, 'SYSTEM' AS docno, \n");
        sql.append(
                "               '+' AS typemark, 0 AS demandqty, CAST(COALESCE(SUM(W.wa_erp_t_qty), 0) AS BIGINT) AS supplyqty, 0 AS sortorder, '' AS remark, '' AS productno \n");
        sql.append("        FROM MVP_Items V \n");
        sql.append(
                "        LEFT JOIN warehouse_area W ON TRIM(W.wa_wm_p_nb) = TRIM(V.itemno) AND W.wa_alias IN (:warehouses) \n");
        sql.append("        GROUP BY V.itemno \n");
        sql.append("        UNION ALL \n");
        sql.append("        -- 各項供給與需求單據 \n");
        sql.append("        SELECT TRIM(M.MB001), \n");
        sql.append("               CASE WHEN M.tk002 ~ '^\\\\d{8}$' THEN \n");
        sql.append("                   CASE WHEN M.sys_sy004 > 0 THEN \n");
        sql.append(
                "                       TO_TIMESTAMP(M.tk002, 'YYYYMMDD') + (COALESCE(MI.delaydays, 0) * INTERVAL '1 day') \n");
        sql.append("                   ELSE \n");
        sql.append("                       TO_TIMESTAMP(M.tk002, 'YYYYMMDD') \n");
        sql.append("                   END \n");
        sql.append("               ELSE NULL END AS transdate, \n");
        sql.append("               M.tk000, M.tk001, CASE WHEN M.sys_sy004 > 0 THEN '+' ELSE '-' END, \n");
        sql.append(
                "               COALESCE(M.sys_sy003, 0), COALESCE(M.sys_sy004, 0), CASE WHEN M.sys_sy004 > 0 THEN 2 ELSE 3 END, '', M.tk003 \n");
        sql.append("        FROM material_shortage M \n");
        sql.append("        LEFT JOIN MVP_Items MI ON TRIM(M.MB001) = MI.itemno \n");
        sql.append("        WHERE TRIM(M.MB001) IN (SELECT itemno FROM MVP_Items) \n");
        sql.append("        AND (COALESCE(M.sys_sy003, 0) > 0 OR COALESCE(M.sys_sy004, 0) > 0) \n");

        sql.append(customOrdersSql);
        sql.append(externalDemandSql);

        sql.append("        UNION ALL \n");
        sql.append("        -- 【🌟 虛擬專案需求 MVP-SIM 】🌟 \n");
        sql.append(
                "        SELECT M.itemno, CAST(:virtualdate AS TIMESTAMP), '虛擬專案', 'MVP-SIM', '-', M.virtualdemandqty, 0, 99, '模擬需求', '' \n");
        sql.append("        FROM MVP_Items M \n");
        sql.append("    ) AS RawTimeline \n");
        sql.append("    WHERE transdate IS NOT NULL \n");
        sql.append(") \n");

        // 累積計算
        sql.append(", Cumulative AS ( \n");
        sql.append("    SELECT *, \n");
        sql.append(
                "        SUM(demandqty) OVER (PARTITION BY itemno ORDER BY transdate, sortorder, docno) AS runningdemand, \n");
        sql.append(
                "        SUM(supplyqty) OVER (PARTITION BY itemno ORDER BY transdate, sortorder, docno) AS runningsupply, \n");
        sql.append(
                "        SUM(CASE WHEN transdate = '1900-01-01' THEN supplyqty ELSE 0 END) OVER (PARTITION BY itemno) AS currentstock \n");
        sql.append("    FROM Timeline \n");
        sql.append(") \n");
        sql.append(", FinalCalc AS ( \n");
        sql.append("    SELECT C.*, (C.runningsupply - C.runningdemand) AS runningbalance \n");
        sql.append("    FROM Cumulative C \n");
        sql.append(") \n");
        sql.append(", FutureMinCalc AS ( \n");
        sql.append("    SELECT *, \n");
        sql.append("           MIN(runningbalance) OVER ( \n");
        sql.append("               PARTITION BY itemno \n");
        sql.append("               ORDER BY transdate, sortorder, docno \n");
        sql.append("               ROWS BETWEEN CURRENT ROW AND UNBOUNDED FOLLOWING \n");
        sql.append("           ) AS minfuturebalance \n");
        sql.append("    FROM FinalCalc \n");
        sql.append(") \n");

        // 最終 SELECT
        sql.append("SELECT \n"
                + "    F.itemno AS itemno, \n"
                + "    M.qtyperset AS qtymultiplier, \n"
                + "    M.bomunitqty AS bomunitqty, \n"
                + "    (F.minfuturebalance + F.demandqty) AS balancebefore, \n"
                + "    F.demandqty AS virtualdemandqty, \n"
                + "    F.minfuturebalance AS balanceafter, \n"
                + "    M.delaydays AS delaydays, \n"
                + "    CASE \n"
                + "        WHEN M.bomunitqty = 0 THEN 9999999 \n"
                + "        WHEN (F.minfuturebalance + F.demandqty) <= 0 THEN 0 \n"
                + "        ELSE FLOOR((F.minfuturebalance + F.demandqty) / M.bomunitqty) \n"
                + "    END AS maxsets, \n"
                + "    F.currentstock AS currentstock, \n"
                + "    CASE \n"
                + "        WHEN M.bomunitqty = 0 THEN 9999999 \n"
                + "        WHEN F.currentstock <= 0 THEN 0 \n"
                + "        ELSE FLOOR(F.currentstock / M.bomunitqty) \n"
                + "    END AS maxsetsphysical \n"
                + "FROM FutureMinCalc F \n"
                + "INNER JOIN MVP_Items M ON F.itemno = M.itemno \n"
                + "WHERE F.docno = 'MVP-SIM' \n");

        Query query = em.createNativeQuery(sql.toString());
        query.setParameter("virtualdate", virtualdate);
        query.setParameter("warehouses", warehouses);

        return query.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object[]> getBomChildren(List<String> parentSns) {
        String childSql = "SELECT TRIM(bbi_sn), TRIM(bbi_i_sn), bbi_i_qty FROM basic_bom_ingredients WHERE TRIM(bbi_sn) IN (:parentSns)";
        Query childQuery = em.createNativeQuery(childSql);
        childQuery.setParameter("parentSns", parentSns);
        return childQuery.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object[]> getBomEdges(List<String> parentItems) {
        String edgeSql = "SELECT TRIM(bbi_sn) AS parentno, TRIM(bbi_i_sn) AS childno " +
                "FROM basic_bom_ingredients " +
                "WHERE TRIM(bbi_sn) IN (:parentItems)";
        Query edgeQuery = em.createNativeQuery(edgeSql);
        edgeQuery.setParameter("parentItems", parentItems);
        return edgeQuery.getResultList();
    }

    // ==========================================
    // Private Helper Methods (Migrated from Service)
    // ==========================================

    private String buildMvpItemsCte(List<Map<String, Object>> allItems, int virtualqty) {
        StringBuilder ctMvpItems = new StringBuilder();
        ctMvpItems.append("WITH MVP_Items AS ( \n");
        boolean first = true;
        for (Map<String, Object> item : allItems) {
            if (!first)
                ctMvpItems.append(" UNION ALL \n");

            String itemNo = (String) item.get("itemno");
            double bomUnitQty = 0;
            Object tqObj = item.get("totalqty");
            if (tqObj instanceof Number) {
                bomUnitQty = ((Number) tqObj).doubleValue();
            } else if (tqObj instanceof String && !((String) tqObj).isEmpty()) {
                bomUnitQty = Double.parseDouble((String) tqObj);
            }
            double qtyMultiplier = 1.0;
            int delayDays = 0;
            if (item.containsKey("delaydays") && item.get("delaydays") != null) {
                Object delObj = item.get("delaydays");
                if (delObj instanceof Number) {
                    delayDays = ((Number) delObj).intValue();
                } else if (delObj instanceof String && !((String) delObj).isEmpty()) {
                    delayDays = Integer.parseInt((String) delObj);
                }
            }
            double virtualDemandQty = bomUnitQty * virtualqty;

            ctMvpItems.append(String.format(
                    "SELECT '%s' AS itemno, %f AS qtyperset, %d AS targetsets, %f AS virtualdemandqty, %d AS delaydays, %f AS bomunitqty",
                    itemNo.replace("'", "''"), qtyMultiplier, virtualqty, virtualDemandQty, delayDays, bomUnitQty));
            first = false;
        }
        ctMvpItems.append("\n ), \n");
        return ctMvpItems.toString();
    }

    private String buildCustomOrdersSql(List<Map<String, Object>> allItems) {
        StringBuilder ctCustomOrders = new StringBuilder();
        int customOrderIndex = 0;
        for (Map<String, Object> item : allItems) {
            String itemNo = (String) item.get("itemno");
            if (item.containsKey("customvirtualorders") && item.get("customvirtualorders") != null) {
                Object cvoObj = item.get("customvirtualorders");
                if (cvoObj instanceof List) {
                    for (Object o : (List<?>) cvoObj) {
                        if (o instanceof Map) {
                            Map<?, ?> order = (Map<?, ?>) o;
                            String cDate = (String) order.get("date");
                            Object cQtyObj = order.get("qty");
                            double cQty = 0;
                            if (cQtyObj instanceof Number)
                                cQty = ((Number) cQtyObj).doubleValue();
                            else if (cQtyObj instanceof String)
                                cQty = Double.parseDouble(String.valueOf(cQtyObj));

                            if (cDate != null && !cDate.isEmpty() && cQty != 0) {
                                String typeMark = cQty > 0 ? "'+'" : "'-'";
                                double absQty = Math.abs(cQty);
                                double supply = cQty > 0 ? absQty : 0;
                                double demand = cQty < 0 ? absQty : 0;
                                customOrderIndex++;
                                ctCustomOrders.append("        UNION ALL \n");
                                ctCustomOrders.append(String.format(
                                        "        SELECT '%s', CAST('%s' AS TIMESTAMP), '自訂單據', 'MVP-CUSTOM-%d', %s, %f, %f, 98, '使用者自訂', '' \n",
                                        itemNo.replace("'", "''"), cDate, customOrderIndex, typeMark, demand, supply));
                            }
                        }
                    }
                }
            }
        }
        return ctCustomOrders.toString();
    }

    private String buildExternalDemandSql(List<Map<String, Object>> allItems,
            List<Map<String, Object>> otherProjects,
            List<MaterialVirtualProject> extEntities,
            Map<String, Double> extDemandByItem,
            Map<String, List<Map<String, Object>>> extDemandDetailByItem) {

        StringBuilder ctExternalDemand = new StringBuilder();

        if (otherProjects == null || otherProjects.isEmpty() || extEntities == null || extEntities.isEmpty()) {
            return "";
        }

        Map<Long, Map<String, Object>> extProjSpecs = new HashMap<>();
        for (Map<String, Object> extProj : otherProjects) {
            Long extMvpId = extProj.get("mvpid") instanceof Number ? ((Number) extProj.get("mvpid")).longValue() : 0L;
            int extQty = extProj.get("virtualqty") instanceof Number ? ((Number) extProj.get("virtualqty")).intValue()
                    : 0;
            if (extMvpId > 0 && extQty > 0) {
                extProjSpecs.put(extMvpId, extProj);
            }
        }

        for (MaterialVirtualProject extEntity : extEntities) {
            Map<String, Object> extProjSpec = extProjSpecs.get(extEntity.getMvpid());
            if (extProjSpec == null)
                continue;

            int extQty = extProjSpec.get("virtualqty") instanceof Number
                    ? ((Number) extProjSpec.get("virtualqty")).intValue()
                    : 0;
            String extDate = (String) extProjSpec.getOrDefault("virtualdate", "2099-12-31");
            String extName = (String) extProjSpec.getOrDefault("mvpname", "Project#" + extEntity.getMvpid());

            String priorityStr = String.valueOf(extProjSpec.getOrDefault("priority", "2"));
            int extSortOrder = 97;
            if ("1".equals(priorityStr))
                extSortOrder = 96;
            else if ("3".equals(priorityStr))
                extSortOrder = 98;

            List<Map<String, Object>> extMain = gson.fromJson(
                    extEntity.getMvpmainmaterials() != null ? extEntity.getMvpmainmaterials() : "[]",
                    new TypeToken<List<Map<String, Object>>>() {
                    }.getType());
            List<Map<String, Object>> extSub = gson.fromJson(
                    extEntity.getMvpsubmaterials() != null ? extEntity.getMvpsubmaterials() : "[]",
                    new TypeToken<List<Map<String, Object>>>() {
                    }.getType());

            List<Map<String, Object>> extAll = new ArrayList<>();
            if (extMain != null)
                extAll.addAll(extMain);
            if (extSub != null)
                extAll.addAll(extSub);

            for (Map<String, Object> extItem : extAll) {
                String extItemNo = (String) extItem.getOrDefault("itemno", "");
                double extQtyPerSet = extItem.get("totalqty") instanceof Number
                        ? ((Number) extItem.get("totalqty")).doubleValue()
                        : 0;

                if (extItemNo.isEmpty() || extQtyPerSet <= 0)
                    continue;

                boolean isRelevant = allItems.stream().anyMatch(m -> extItemNo.equals(m.get("itemno")));
                if (!isRelevant)
                    continue;

                double extDemand = extQtyPerSet * extQty;

                ctExternalDemand.append("        UNION ALL \n");
                ctExternalDemand.append(String.format(
                        "        SELECT '%s', CAST('%s' AS TIMESTAMP), '外部專案', 'MVP-EXT-%d', '-', %f, 0, %d, '外部專案競爭', '' \n",
                        extItemNo.replace("'", "''"), extDate, extEntity.getMvpid(), extDemand, extSortOrder));

                if (extDemandByItem != null) {
                    extDemandByItem.merge(extItemNo, extDemand, Double::sum);
                }
                if (extDemandDetailByItem != null) {
                    extDemandDetailByItem.computeIfAbsent(extItemNo, k -> new ArrayList<>())
                            .add(Map.of("mvpid", extEntity.getMvpid(), "mvpname", extName, "qty",
                                    Math.round(extDemand)));
                }
            }
        }
        return ctExternalDemand.toString();
    }
}
