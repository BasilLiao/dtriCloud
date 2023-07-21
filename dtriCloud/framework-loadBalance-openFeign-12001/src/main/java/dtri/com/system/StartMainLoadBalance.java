package dtri.com.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 系統服務
 * 
 */
@SpringBootApplication
@EnableFeignClients
// Spring Open Feign 平衡負載器
public class StartMainLoadBalance {
	public static void main(String[] args) {
		SpringApplication.run(StartMainLoadBalance.class, args);
	}
}
