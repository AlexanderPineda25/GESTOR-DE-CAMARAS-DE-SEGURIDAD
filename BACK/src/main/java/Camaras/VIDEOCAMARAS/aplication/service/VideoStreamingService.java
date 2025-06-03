package Camaras.VIDEOCAMARAS.aplication.service;

import java.io.IOException;
import jakarta.websocket.Session;

public interface VideoStreamingService {

    /**
     * Inicia el streaming de video para una cámara y sesión de cliente.
     * @param cameraId ID de la cámara a transmitir.
     * @param session Sesión WebSocket del cliente.
     */
    void startStreaming(Long cameraId, Session session) throws IOException;

    /**
     * Envía un frame o paquete de datos de video a la sesión.
     * @param session Sesión WebSocket.
     * @param frame Datos binarios del frame.
     */
    void sendFrame(Session session, byte[] frame) throws IOException;

    /**
     * Detiene el streaming y cierra la sesión.
     * @param session Sesión WebSocket.
     */
    void stopStreaming(Session session) throws IOException;

    byte[] captureFrame(Long cameraId);

    String processCommand(String command);
}
