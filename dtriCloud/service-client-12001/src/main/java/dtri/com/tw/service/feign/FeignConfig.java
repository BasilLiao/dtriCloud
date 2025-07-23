package dtri.com.tw.service.feign;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Feign;
import feign.Request;

@Configuration
public class FeignConfig {
	@Bean
	public Feign.Builder feignBuilder() {
		return Feign.builder().options(new Request.Options(//
				10, TimeUnit.SECONDS, // connectTimeout: 10 秒
				180, TimeUnit.SECONDS, // readTimeout: 180 秒
				true // 是否允許 redirect（視業務需求）此參數控制 Feign 在遇到 HTTP 3xx 轉址（Redirect）
						// 時，是否要自動跟隨重導（Redirect）。
		));
	}
}
