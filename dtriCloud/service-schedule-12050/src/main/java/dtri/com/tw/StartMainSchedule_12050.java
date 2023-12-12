package dtri.com.tw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 系統生產管理服務
 * 
 */
@SpringBootApplication
@EnableDiscoveryClient
// Spring cloud Eureka 客戶端，自動將此服務註冊到Eureka Server註冊中心
public class StartMainSchedule_12050 {
	public static void main(String[] args) {
		SpringApplication.run(StartMainSchedule_12050.class, args);
	}
}
