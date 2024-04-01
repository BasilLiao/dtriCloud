package dtri.com.tw.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
public class WebSocketConfiguration {

	@Bean
	public ServerEndpointExporter serverEndpointExporter() {

		ServerEndpointExporter exporter = new ServerEndpointExporter();

		// 手動註冊 WebSocket 端點
		exporter.setAnnotatedEndpointClasses(ScheduleOutsourcerWebSocket.class);

		return exporter;
	}

	@Bean
	public ServletServerContainerFactoryBean createWebSocketContainer() {
		ServletServerContainerFactoryBean containerFactoryBean = new ServletServerContainerFactoryBean();
		// 設置WebSocket大小
		containerFactoryBean.setMaxTextMessageBufferSize(512000);//緩衝大小
		containerFactoryBean.setMaxBinaryMessageBufferSize(512000);
		containerFactoryBean.setMaxSessionIdleTimeout(15 * 60000L);//時間
		return containerFactoryBean;

	}
}
