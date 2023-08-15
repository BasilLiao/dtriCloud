package dtri.com.tw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 客戶端入口服務
 * 
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
// Spring cloud Eureka 客戶端，自動將此服務註冊到Eureka Server註冊中心
public class StartMainClient_12001 {
	public static void main(String[] args) {
		SpringApplication.run(StartMainClient_12001.class, args);
	}
}
