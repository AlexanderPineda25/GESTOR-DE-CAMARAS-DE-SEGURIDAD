package Camaras.VIDEOCAMARAS.infraestructure._Websocket;

import java.io.IOException; // ¡Importa IOException aquí también!

public interface WebSocketSessionAdapter {
    void sendMessage(byte[] data) throws IOException;
    void close() throws IOException;
    String getId();
    boolean isOpen();
}