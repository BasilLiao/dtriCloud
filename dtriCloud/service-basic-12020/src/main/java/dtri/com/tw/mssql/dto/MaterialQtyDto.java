package dtri.com.tw.mssql.dto;

import java.math.BigDecimal;

public interface MaterialQtyDto {

    // (品號)
    String getMb001();

    // 數量
    BigDecimal getQty();
}
