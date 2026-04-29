package dtri.com.tw.filter;

import java.io.IOException;

import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class GZipFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // 判斷是否為目標 API 且支援 Gzip
        String uri = req.getRequestURI();
        String acceptEncoding = req.getHeader("Accept-Encoding");

        boolean isTarget = uri.contains("ajax"); // 請確認路徑關鍵字是否正確
        boolean supportGzip = acceptEncoding != null && acceptEncoding.toLowerCase().contains("gzip");

        if (isTarget && supportGzip) {

            System.out.println(">>> [GZipFilter] 啟動壓縮: " + uri);

            res.addHeader("Content-Encoding", "gzip");
            res.addHeader("Vary", "Accept-Encoding");

            GZipResponseWrapper gzipResponse = new GZipResponseWrapper(res);

            try {
                // 執行 Controller (這裡會花 2.4 秒)
                chain.doFilter(req, gzipResponse);
            } finally {
                // ★ 無論發生什麼事，一定要封口！
                // 這行沒執行，瀏覽器就會轉圈圈轉到 Timeout
                gzipResponse.finish();
                System.out.println(">>> [GZipFilter] 壓縮封口完成，資料已送出");
            }

        } else {
            chain.doFilter(request, response);
        }
    }
}