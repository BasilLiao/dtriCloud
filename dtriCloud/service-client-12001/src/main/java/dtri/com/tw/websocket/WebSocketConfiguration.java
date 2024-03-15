package dtri.com.tw.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfiguration {

	@Bean
	public ServerEndpointExporter serverEndpointExporter() {

		ServerEndpointExporter exporter = new ServerEndpointExporter();

		// 手動註冊 WebSocket 端點
		exporter.setAnnotatedEndpointClasses(ScheduleOutsourcerWebSocket.class);

		return exporter;
	}
}
