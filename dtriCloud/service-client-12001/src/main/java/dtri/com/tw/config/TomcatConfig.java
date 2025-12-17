package dtri.com.tw.config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {

	@Bean
	public TomcatServletWebServerFactory servletContainer() {
		TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();

		Connector httpConnector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);

		httpConnector.setPort(12002); // 內部 HTTP
		httpConnector.setScheme("http");
		httpConnector.setSecure(false);

		tomcat.addAdditionalTomcatConnectors(httpConnector);
		return tomcat;
	}
}
