package Camaras.VIDEOCAMARAS.aplication.service;

import Camaras.VIDEOCAMARAS.infraestructure._Websocket.WebSocketSessionAdapter;
import jakarta.websocket.Session;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public interface VideoStreamingManager {
    StreamingResponseBody streamVideoHttp(Long cameraId);
    byte[] captureFrame(Long cameraId);
    StreamingResponseBody streamVideo(Long cameraId);
    void startStreamingWebSocket(Long cameraId, WebSocketSessionAdapter session);
}
