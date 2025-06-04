package Camaras.VIDEOCAMARAS.aplication.service;

import Camaras.VIDEOCAMARAS.infraestructure._Websocket.WebSocketSessionAdapter;

import java.io.IOException;

public interface VideoStreamingService {

    void startStreaming(Long cameraId, WebSocketSessionAdapter session) throws IOException;
    void sendFrame(WebSocketSessionAdapter session, byte[] frame) throws IOException;
    void stopStreaming(WebSocketSessionAdapter session) throws IOException;
    byte[] captureFrame(Long cameraId);
    String processCommand(String command);
    void processVideoFrame(byte[] videoFrame, WebSocketSessionAdapter sessionAdapter);
}
