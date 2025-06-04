package Camaras.VIDEOCAMARAS.infraestructure._Websocket;

import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.BinaryMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
public class SpringWebSocketSessionAdapter implements WebSocketSessionAdapter {
    private final WebSocketSession session;

    public SpringWebSocketSessionAdapter(WebSocketSession session) {
        this.session = session;
    }

    @Override
    public void sendMessage(byte[] data) throws IOException {
        // Para enviar datos binarios en Spring, se usa BinaryMessage
        session.sendMessage(new BinaryMessage(data));
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