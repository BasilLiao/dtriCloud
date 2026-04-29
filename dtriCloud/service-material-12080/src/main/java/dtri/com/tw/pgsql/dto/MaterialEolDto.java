package dtri.com.tw.pgsql.dto;

/**
 * 停產物料估算 DTO (Projection)
 * 用於: 承接 Native Query 回傳的遞迴 BOM 展開及庫存試算結果。
 */
public interface MaterialEolDto {

    /** 零件料號 */
    String getPartno();

    /** 零件品名 */
    String getPartname();

    /** 零件規格 */
    String getPartspec();

    /** 零件備註 */
    String getRemark();

    /** BOM 階層 */
    Integer getBomlevel();

    /** 單套總用量 */
    Double getQtyperset();

    /** 指定倉別總庫存 */
    Integer getWarehousestock();

    /** 可供應套數 (庫存 / 單套用量) 取整數 */
    Integer getAvailablesets();

    /** 來源 BOM (用於多選合併時識別) */
    String getRootbom();

}
