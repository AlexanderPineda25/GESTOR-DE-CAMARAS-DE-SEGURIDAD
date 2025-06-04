package Camaras.VIDEOCAMARAS.infraestructure._Websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketVideoConfig implements WebSocketConfigurer {

    @Autowired
    private VideoSocketHandler videoSocketHandler;
    
    public WebSocketVideoConfig(VideoSocketHandler videoSocketHandler) {
        this.videoSocketHandler = videoSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(videoSocketHandler, "/video-ws/{cameraId}")
                .setAllowedOrigins("*");
    }
}