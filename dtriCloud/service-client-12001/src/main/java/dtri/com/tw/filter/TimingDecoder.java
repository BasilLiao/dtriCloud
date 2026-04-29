package dtri.com.tw.filter;

import java.io.IOException;
import java.lang.reflect.Type;

import feign.FeignException;
import feign.Response;
import feign.codec.DecodeException;
import feign.codec.Decoder;

public class TimingDecoder implements Decoder {

    private final Decoder delegate;

    public TimingDecoder(Decoder delegate) {
        this.delegate = delegate;
    }

    @Override
    public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {
        // 1. 開始計時
        long start = System.currentTimeMillis();

        // 2. 執行真正的解壓縮與解析 (最花時間都在這行)
        Object result = delegate.decode(response, type);

        // 3. 結束計時
        long cost = System.currentTimeMillis() - start;

        // 4. 印出鐵證！
        System.out.println(">>> [Feign監控] 收到資料大小(估計): " + response.body().length() + " bytes");
        System.out.println(">>> [Feign監控] 解壓+解析耗時: " + cost + " ms");

        return result;
    }
}
