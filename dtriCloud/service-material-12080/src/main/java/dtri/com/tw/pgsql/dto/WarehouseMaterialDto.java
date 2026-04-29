package dtri.com.tw.pgsql.dto;

/**
 * 倉儲物料簡易資料 DTO (Projection)
 * 用於: 前端 Autocomplete 自動完成搜尋，只取需要的欄位以提升效能。
 */
public interface WarehouseMaterialDto {

    /**
     * 料號
     * 對應 Entity 屬性: wmpnb (或 DB欄位 wm_p_nb)
     */
    String getWmpnb();

    /**
     * 品名
     * 對應 Entity 屬性: wmname (或 DB欄位 wm_name)
     */
    String getWmname();

}
