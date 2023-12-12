package dtri.com.tw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 系統製造服務
 * 
 */
@SpringBootApplication
@EnableDiscoveryClient
// Spring cloud Eureka 客戶端，自動將此服務註冊到Eureka Server註冊中心
public class StartMainSystem_12040 {
	public static void main(String[] args) {
		SpringApplication.run(StartMainSystem_12040.class, args);
	}
}
