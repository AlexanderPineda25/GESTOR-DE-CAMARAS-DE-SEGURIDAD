package Camaras.VIDEOCAMARAS.infraestructure._Websocket;

import Camaras.VIDEOCAMARAS.aplication.service.VideoStreamingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class VideoSocketHandler extends BinaryWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(VideoSocketHandler.class);

    private final VideoStreamingService videoStreamingService;
    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();


    @Autowired
    public VideoSocketHandler(VideoStreamingService videoStreamingService) {
        this.videoStreamingService = videoStreamingService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        try {
            Long cameraIdLong = extractCameraId(session);
            activeSessions.put(session.getId(), session);
            session.sendMessage(new TextMessage("CONECTADO_A_CÁMARA_" + cameraIdLong));

            WebSocketSessionAdapter adapter = new SpringWebSocketSessionAdapter(session);
            videoStreamingService.startStreaming(cameraIdLong, adapter);

        } catch (NumberFormatException e) {
            logger.error("ID de cámara inválido o URL incorrecta: {}", session.getUri(), e);
            session.close(CloseStatus.NOT_ACCEPTABLE);
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        // Se crea el adaptador antes de pasarlo al servicio
        WebSocketSessionAdapter adapter = new SpringWebSocketSessionAdapter(session);
        byte[] videoFrame = message.getPayload().array();
        videoStreamingService.processVideoFrame(videoFrame, adapter);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws IOException {
        activeSessions.remove(session.getId());
        // Se crea el adaptador antes de pasarlo al servicio
        WebSocketSessionAdapter adapter = new SpringWebSocketSessionAdapter(session);
        videoStreamingService.stopStreaming(adapter);
    }

    private Long extractCameraId(WebSocketSession session) throws NumberFormatException {
        String[] parts = session.getUri().getPath().split("/");
        if (parts.length < 3) {
            throw new NumberFormatException("URL no válida para extraer cámara");
        }
        return Long.parseLong(parts[2]);
    }
}