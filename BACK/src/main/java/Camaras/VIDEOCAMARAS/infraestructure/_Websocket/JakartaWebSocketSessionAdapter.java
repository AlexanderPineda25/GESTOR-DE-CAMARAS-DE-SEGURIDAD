package Camaras.VIDEOCAMARAS.infraestructure._Websocket;

import jakarta.websocket.Session;
import java.io.IOException; // Importación correcta para IOException

public class JakartaWebSocketSessionAdapter implements WebSocketSessionAdapter {
    private final Session session;

    public JakartaWebSocketSessionAdapter(Session session) {
        this.session = session;
    }

    @Override
    public void sendMessage(byte[] data) throws IOException {
        // Asegúrate de que el ByteBuffer se envíe correctamente.
        // El método sendBinary espera un ByteBuffer.
        session.getBasicRemote().sendBinary(java.nio.ByteBuffer.wrap(data));
    }


    @Override
    public void close() throws IOException {
        session.close();
    }

    @Override
    public String getId() {
        return session.getId();
    }

    @Override
    public boolean isOpen() {
        return session.isOpen();
    }
}
