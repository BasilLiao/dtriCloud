package dtri.com.tw.filter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

public class GZipResponseWrapper extends HttpServletResponseWrapper {
    private HttpServletResponse originalResponse;
    private GZIPOutputStream gzipOutputStream;
    private ServletOutputStream servletOutputStream;
    private PrintWriter printWriter;

    public GZipResponseWrapper(HttpServletResponse response) {
        super(response);
        this.originalResponse = response;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (printWriter != null)
            throw new IllegalStateException("PrintWriter obtained already");
        if (servletOutputStream == null) {
            servletOutputStream = new ServletOutputStream() {
                private GZIPOutputStream gzip = getGzipStream();

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setWriteListener(WriteListener writeListener) {
                }

                @Override
                public void write(int b) throws IOException {
                    gzip.write(b);
                }

                @Override
                public void write(byte[] b) throws IOException {
                    gzip.write(b);
                }

                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    gzip.write(b, off, len);
                }

                @Override
                public void flush() throws IOException {
                    gzip.flush();
                }

                @Override
                public void close() throws IOException {
                    gzip.finish();
                }
            };
        }
        return servletOutputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (servletOutputStream != null)
            throw new IllegalStateException("OutputStream obtained already");
        if (printWriter == null) {
            printWriter = new PrintWriter(
                    new OutputStreamWriter(getGzipStream(), originalResponse.getCharacterEncoding()));
        }
        return printWriter;
    }

    private GZIPOutputStream getGzipStream() throws IOException {
        if (this.gzipOutputStream == null) {
            this.gzipOutputStream = new GZIPOutputStream(originalResponse.getOutputStream());
        }
        return this.gzipOutputStream;
    }

    // ★ 關鍵修正：強制寫入 GZIP 結尾，並刷新底層串流
    public void finish() throws IOException {
        if (printWriter != null) {
            printWriter.close();
        } else if (servletOutputStream != null) {
            servletOutputStream.close();
        } else if (gzipOutputStream != null) {
            gzipOutputStream.finish();
            gzipOutputStream.flush(); // 確保送到網路上
        }
    }

    // ★ 關鍵修正：禁止 Spring 設定 Content-Length
    // 因為壓縮後大小會變，如果 Header 還是 47MB，瀏覽器會等到死
    @Override
    public void setContentLength(int len) {
    }

    @Override
    public void setContentLengthLong(long len) {
    }

    @Override
    public void addHeader(String name, String value) {
        if ("Content-Length".equalsIgnoreCase(name))
            return;
        super.addHeader(name, value);
    }

    @Override
    public void setHeader(String name, String value) {
        if ("Content-Length".equalsIgnoreCase(name))
            return;
        super.setHeader(name, value);
    }
}
