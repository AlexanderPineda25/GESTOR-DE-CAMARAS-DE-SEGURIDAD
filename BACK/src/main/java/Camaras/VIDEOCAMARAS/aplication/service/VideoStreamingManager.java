package Camaras.VIDEOCAMARAS.aplication.service;

import jakarta.websocket.Session;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public interface VideoStreamingManager {
    StreamingResponseBody streamVideoHttp(Long cameraId);
    void startStreamingWebSocket(Long cameraId, Session session);
    byte[] captureFrame(Long cameraId);
    StreamingResponseBody streamVideo(Long cameraId);
}
