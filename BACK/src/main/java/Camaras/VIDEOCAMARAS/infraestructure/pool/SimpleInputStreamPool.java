package Camaras.VIDEOCAMARAS.infraestructure.pool;

import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.InputStream;

@Component
public class SimpleInputStreamPool implements InputStreamPool {
    @Override
    public InputStream borrowObject(String path) throws Exception {
        return new FileInputStream(path); // O gestión real de pool si lo necesitas
    }

    @Override
    public void returnObject(InputStream stream) throws Exception {
        if (stream != null) stream.close();
    }
}
