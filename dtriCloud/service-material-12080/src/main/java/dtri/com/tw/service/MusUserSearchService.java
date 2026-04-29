package dtri.com.tw.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dtri.com.tw.pgsql.dao.MusUserSearchDao;
import dtri.com.tw.pgsql.entity.MusUserSearch;
import dtri.com.tw.shared.PackageBean;

@Service
public class MusUserSearchService {

    @Autowired
    private MusUserSearchDao musUserSearchDao;

    @Transactional
    public void setMussearch(PackageBean packageBean) {
        try {
            String entityJsonStr = packageBean.getEntityJson();
            if (entityJsonStr == null || entityJsonStr.isEmpty()) {
                return;
            }

            JsonObject jsonObject = JsonParser.parseString(entityJsonStr).getAsJsonObject();
            if (!jsonObject.has("mus_search")) {
                return;
            }
            String musSearchContent = jsonObject.get("mus_search").getAsString();

            String gKey = packageBean.getEntityIKeyGKey();
            Long userId = (gKey == null || gKey.isEmpty()) ? null : Long.valueOf(gKey);

            if (userId == null) {
                return;
            }

            MusUserSearch musUserSearch = musUserSearchDao.findByMusuid(userId)
                    .orElseGet(() -> {
                        // 如果找不到，就 new 一個新的，建構子會自動帶入 syscdate, syscuser 等預設值
                        MusUserSearch newEntity = new MusUserSearch();
                        newEntity.setMusuid(userId);
                        newEntity.setSyscuser(packageBean.getUserName());
                        newEntity.setSysouser(packageBean.getUserName());
                        return newEntity;
                    });

            musUserSearch.setMussearch(musSearchContent); // 存入那串 JSON
            musUserSearch.setSysmdate(new Date()); // 更新修改時間
            musUserSearch.setSysmuser(packageBean.getUserName()); // 更新修改者

            musUserSearchDao.save(musUserSearch);

        } catch (Exception e) {
            // 這裡建議加上日誌，方便偵錯
            System.err.println("儲存使用者偏好失敗: " + e.getMessage());
            e.printStackTrace();
        }
    }
}