package dtri.com.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 系統服務-DAO
 * 
 * */
@SpringBootApplication
@EnableDiscoveryClient
 // Spring cloud Eureka 客戶端，自動將此服務註冊到Eureka Server註冊中心
public class StartMainSystemDao {
	public static void main(String[] args) {
		SpringApplication.run(StartMainSystemDao.class, args);
	}
}
