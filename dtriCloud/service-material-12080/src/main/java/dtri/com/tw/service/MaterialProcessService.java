package dtri.com.tw.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dtri.com.tw.pgsql.dao.MaterialProcessDao;
import dtri.com.tw.pgsql.dao.SystemLanguageCellDao;
import dtri.com.tw.pgsql.entity.MaterialProcess;
import dtri.com.tw.pgsql.entity.SystemLanguageCell;
import dtri.com.tw.shared.CloudExceptionService;
import dtri.com.tw.shared.CloudExceptionService.ErCode;
import dtri.com.tw.shared.CloudExceptionService.ErColor;
import dtri.com.tw.shared.CloudExceptionService.Lan;
import dtri.com.tw.shared.PackageBean;
import dtri.com.tw.shared.PackageService;

/** 功能模組: 設置-製程對照表 */
@Service
public class MaterialProcessService {

    @Autowired
    private PackageService packageService;

    @Autowired
    private MaterialProcessDao materialProcessDao;

    @Autowired
    private SystemLanguageCellDao languageDao;

    /** 查詢資料 (完整版：分頁+翻譯+動態欄位) */
    /** 查詢資料 (完整版：分頁+手動翻譯+動態欄位過濾) */
    public PackageBean getSearch(PackageBean packageBean) throws Exception {
        // ======================== 分頁設置 ========================
        int total = 10000;
        int batch = 0;
        if (packageBean.getSearchPageSet() != null && !packageBean.getSearchPageSet().isEmpty()) {
            JsonObject pageSetJson = JsonParser.parseString(packageBean.getSearchPageSet()).getAsJsonObject();
            if (pageSetJson.has("total"))
                total = pageSetJson.get("total").getAsInt();
            if (pageSetJson.has("batch"))
                batch = pageSetJson.get("batch").getAsInt();
        }

        // ======================== 排序設置 ========================
        List<Order> orders = new ArrayList<>();
        orders.add(new Order(Direction.ASC, "mpname"));
        PageRequest pageable = PageRequest.of(batch, total, Sort.by(orders));

        // ======================== 區分: 訪問(Visit) / 查詢(Search) ========================
        if (packageBean.getEntityJson() == null || packageBean.getEntityJson().equals("")) {

            // Step3-1. 取得資料
            ArrayList<MaterialProcess> entitys = materialProcessDao.findAllBySearch(null, null, pageable);

            // Step3-2. 資料包裝
            String entityJson = packageService.beanToJson(entitys);
            packageBean.setEntityJson(entityJson);
            packageBean.setEntityDetailJson("{}");

            // ======================== 🌟 重點修正區域：萬用語言包 ========================

            // Step3-3. 取得翻譯
            Map<String, SystemLanguageCell> mapLanguages = new HashMap<>();
            ArrayList<SystemLanguageCell> languages = languageDao.findAllByLanguageCellSame("MaterialProcess", null, null);
            languages.forEach(x -> mapLanguages.put(x.getSltarget(), x));

            // Step3-4. 欄位過濾
            JsonObject searchSetJsonAll = new JsonObject();
            JsonArray searchJsons = new JsonArray();
            JsonObject resultDataTJsons = new JsonObject();

            Field[] fields = MaterialProcess.class.getDeclaredFields();
            ArrayList<String> exceptionCell = new ArrayList<>();

            // 【自動過濾迴圈】
            for (Field field : fields) {
                String name = field.getName();
                if (name.equals("syssort")) {
                    exceptionCell.add(name);
                }
            }

            // 產生結果
            resultDataTJsons = packageService.resultSet(fields, exceptionCell, mapLanguages);
            // 隱藏 mpid 欄位不顯示在前端介面
            for (String key : resultDataTJsons.keySet()) {
                if (key.endsWith("_mpid")) {
                    resultDataTJsons.getAsJsonObject(key).addProperty("show", 0); // 隱藏查詢清單的欄位
                    resultDataTJsons.getAsJsonObject(key).addProperty("m_show", 0); // 隱藏修改介面的欄位
                    break;
                }
            }

            // Step3-5. 建立前端搜尋框
            // 取得語系
            String userLg = packageBean.getUserLanguaue();
            String ph_mpname = "Ex:製程代號?";
            String ph_mpgroup = "Ex:製程對照名稱?";
            if (mapLanguages.containsKey("ph_mpname")) {
                JsonObject lg = JsonParser.parseString(mapLanguages.get("ph_mpname").getSllanguage()).getAsJsonObject();
                ph_mpname = lg.has(userLg) ? lg.get(userLg).getAsString() : ph_mpname;
            }
            if (mapLanguages.containsKey("ph_mpgroup")) {
                JsonObject lg = JsonParser.parseString(mapLanguages.get("ph_mpgroup").getSllanguage()).getAsJsonObject();
                ph_mpgroup = lg.has(userLg) ? lg.get(userLg).getAsString() : ph_mpgroup;
            }

            searchJsons = packageService.searchSet(searchJsons, null, "mpname", ph_mpname, true,
                    PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);

            searchJsons = packageService.searchSet(searchJsons, null, "mpgroup", ph_mpgroup, true,
                    PackageService.SearchType.text, PackageService.SearchWidth.col_lg_2);

            // 包裝設定
            searchSetJsonAll.add("searchSet", searchJsons);
            searchSetJsonAll.add("resultThead", resultDataTJsons);
            searchSetJsonAll.add("resultDetailThead", new JsonObject());
            packageBean.setSearchSet(searchSetJsonAll.toString());

        } else {
            // ======================== Search模式 ========================
            MaterialProcess searchData = packageService.jsonToBean(packageBean.getEntityJson(), MaterialProcess.class);

            ArrayList<MaterialProcess> entitys = materialProcessDao.findAllBySearch(
                    searchData.getMpname(),
                    searchData.getMpgroup(),
                    pageable);

            String entityJson = packageService.beanToJson(entitys);
            packageBean.setEntityJson(entityJson);
            packageBean.setEntityDetailJson("");

            if (packageBean.getEntityJson().equals("[]")) {
                Lan lan = Lan.zh_TW;
                try {
                    lan = Lan.valueOf(packageBean.getUserLanguaue().replace("-", "_"));
                } catch (Exception e) {
                }
                throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1000, lan, null);
            }
        }

        // ======================== 共用參數配置 ========================
        String entityFormatJson = packageService.beanToJson(new MaterialProcess());
        packageBean.setEntityFormatJson(entityFormatJson);
        packageBean.setEntityIKeyGKey("mpid");

        return packageBean;
    }

    /** 新增與修改 */
    @Transactional
    public PackageBean setModify(PackageBean packageBean) throws Exception {
        ArrayList<MaterialProcess> entityDatas = packageService.jsonToBean(
                packageBean.getEntityJson(),
                new TypeReference<ArrayList<MaterialProcess>>() {
                });

        // 取得翻譯
        Map<String, SystemLanguageCell> mapLanguages = new HashMap<>();
        ArrayList<SystemLanguageCell> languages = languageDao.findAllByLanguageCellSame("MaterialProcess", null, 3);
        languages.forEach(x -> mapLanguages.put(x.getSltarget(), x));
        String userLg = packageBean.getUserLanguaue();
        Lan lan = Lan.zh_TW;
        try {
            lan = Lan.valueOf(userLg.replace("-", "_"));
        } catch (Exception e) {
        }

        for (MaterialProcess item : entityDatas) {
            // 必填檢查
            if (item.getMpname() == null || item.getMpname().trim().isEmpty()) {
                String msg = "製程代號不可為空";
                if (mapLanguages.containsKey("msg_mpname_empty")) {
                    JsonObject lg = JsonParser.parseString(mapLanguages.get("msg_mpname_empty").getSllanguage()).getAsJsonObject();
                    msg = lg.has(userLg) ? lg.get(userLg).getAsString() : msg;
                }
                throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1003, lan, new String[] { msg });
            }
            // JSON 檢查
            try {
                if (item.getMpother() != null && !item.getMpother().isEmpty()) {
                    JsonParser.parseString(item.getMpother());
                }
            } catch (Exception e) {
                String msg = "特殊條件 JSON 格式錯誤";
                if (mapLanguages.containsKey("msg_mpother_json_error")) {
                    JsonObject lg = JsonParser.parseString(mapLanguages.get("msg_mpother_json_error").getSllanguage()).getAsJsonObject();
                    msg = lg.has(userLg) ? lg.get(userLg).getAsString() : msg;
                }
                throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1004, lan, new String[] { msg });
            }

            if (item.getMpid() == null) { // 新增
                if (!materialProcessDao.findAllByMpnameAndSysstatusNot(item.getMpname(), 2).isEmpty()) {
                    throw new CloudExceptionService(packageBean, ErColor.warning, ErCode.W1001, lan,
                            new String[] { item.getMpname() });
                }
                item.setSyscdate(new Date());
                item.setSyscuser(packageBean.getUserAccount());
                item.setSysodate(new Date());
                item.setSysouser(packageBean.getUserAccount());
                item.setSysmdate(new Date());
                item.setSysmuser(packageBean.getUserAccount());
                item.setSysstatus(0);
                materialProcessDao.save(item);
            } else { // 修改
                MaterialProcess oldEntity = materialProcessDao.findById(item.getMpid()).orElse(null);
                if (oldEntity == null)
                    continue;
                // [修正] 只更新前端有傳回來的欄位，其餘保留原樣 (避免 mpOther, syssort 被抹除)
                oldEntity.setMpname(item.getMpname());
                oldEntity.setMpgroup(item.getMpgroup());
                oldEntity.setMpother(item.getMpother());
                oldEntity.setSysnote(item.getSysnote());
                oldEntity.setSysmdate(new Date());
                oldEntity.setSysmuser(packageBean.getUserAccount());
                oldEntity.setSysstatus(0);
                materialProcessDao.save(oldEntity);
            }
        }
        return getSearch(packageBean); // 修改後重新查詢
    }

    /** 刪除 */
    @Transactional
    public PackageBean setDelete(PackageBean packageBean) throws Exception {
        ArrayList<MaterialProcess> entityDatas = packageService.jsonToBean(
                packageBean.getEntityJson(),
                new TypeReference<ArrayList<MaterialProcess>>() {
                });

        for (MaterialProcess item : entityDatas) {
            if (item.getMpid() != null) {
                MaterialProcess target = materialProcessDao.findById(item.getMpid()).orElse(null);
                if (target != null) {
                    target.setSysstatus(2);
                    target.setSysmdate(new Date());
                    target.setSysmuser(packageBean.getUserAccount());
                    materialProcessDao.save(target);
                }
            }
        }
        return getSearch(packageBean);
    }
}