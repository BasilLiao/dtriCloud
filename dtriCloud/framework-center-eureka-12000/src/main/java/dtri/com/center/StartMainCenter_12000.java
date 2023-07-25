package dtri.com.center;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * 服務監控中心-微服務
 * http://c.biancheng.net/springcloud/
 * */
@SpringBootApplication
@EnableEurekaServer 
//打開Eureka服務器，接受其他微服務的註冊
public class StartMainCenter_12000 {
	public static void main(String[] args) {
		SpringApplication.run(StartMainCenter_12000.class, args);
	}
}
