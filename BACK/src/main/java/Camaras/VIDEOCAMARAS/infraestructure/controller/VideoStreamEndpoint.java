package Camaras.VIDEOCAMARAS.infraestructure.controller;

import Camaras.VIDEOCAMARAS.aplication.service.VideoStreamingService;
import Camaras.VIDEOCAMARAS.infraestructure.config.SpringConfigurator;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/video-stream/{cameraId}", configurator = SpringConfigurator.class)
public class VideoStreamEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(VideoStreamEndpoint.class);
    private static final Map<String, Session> activeSessions = new ConcurrentHashMap<>();

    private final VideoStreamingService videoStreamingService;

    public VideoStreamEndpoint(VideoStreamingService videoStreamingService) {
        this.videoStreamingService = videoStreamingService;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("cameraId") String cameraId) {
        activeSessions.put(session.getId(), session);
        logger.info("Conexión WebSocket abierta - Cámara: {}, Sesión: {}", cameraId, session.getId());

        try {
            Long cameraIdLong = Long.parseLong(cameraId);
            session.getUserProperties().put("cameraId", cameraIdLong);

            // Envío de confirmación
            session.getBasicRemote().sendText("CONECTADO_A_CÁMARA_" + cameraId);

            // Iniciar streaming
            videoStreamingService.startStreaming(cameraIdLong, session);
        } catch (NumberFormatException e) {
            logger.error("ID de cámara inválido: {}", cameraId, e);
            safeClose(session, CloseReason.CloseCodes.CANNOT_ACCEPT, "ID de cámara inválido");
        } catch (IOException e) {
            logger.error("Error al iniciar streaming para cámara {}", cameraId, e);
            safeClose(session, CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Error de servidor");
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        logger.debug("Mensaje recibido de {}: {}", session.getId(), message);

        try {
            // Procesar mensaje (ejemplo: comando para la cámara)
            String response = videoStreamingService.processCommand(message);
            session.getBasicRemote().sendText(response);
        } catch (IOException e) {
            logger.error("Error al procesar mensaje para sesión {}", session.getId(), e);
        }
    }

    @OnClose
    public void onClose(Session session) {
        if (session != null) {
            String sessionId = session.getId();
            try {
                videoStreamingService.stopStreaming(session);
                logger.info("Streaming detenido para sesión {}", sessionId);
            } catch (IOException e) {
                logger.error("Error al detener streaming en sesión {}", sessionId, e);
            } finally {
                activeSessions.remove(sessionId);
                logger.info("Conexión WebSocket cerrada - Sesión: {}", sessionId);
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        String sessionId = session != null ? session.getId() : "session-null";
        logger.error("Error en WebSocket (Sesión: {}): {}", sessionId, throwable.getMessage(), throwable);

        if (session != null) {
            safeClose(session, CloseReason.CloseCodes.UNEXPECTED_CONDITION,
                    "Error: " + throwable.getMessage());
            activeSessions.remove(sessionId);
        }
    }

    private void safeClose(Session session, CloseReason.CloseCode code, String reason) {
        try {
            if (session != null && session.isOpen()) {
                session.close(new CloseReason(code, reason));
            }
        } catch (IOException e) {
            logger.error("Error al cerrar sesión WebSocket", e);
        }
    }

    // Método para transmisión global
    public static void broadcast(String message) {
        activeSessions.forEach((id, session) -> {
            try {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(message);
                }
            } catch (IOException e) {
                logger.error("Error en broadcast a sesión {}", id, e);
            }
        });
    }
}