package Camaras.VIDEOCAMARAS.infraestructure.pool;

import java.io.InputStream;

public interface InputStreamPool {
    InputStream borrowObject(String path) throws Exception;
    void returnObject(InputStream stream) throws Exception;
}
