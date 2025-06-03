package Camaras.VIDEOCAMARAS.infraestructure.config;

import jakarta.websocket.server.ServerEndpointConfig;
import org.springframework.context.ApplicationContext;

public class SpringConfigurator extends ServerEndpointConfig.Configurator {

    private static volatile ApplicationContext context;

    public static void setApplicationContext(ApplicationContext ctx) {
        SpringConfigurator.context = ctx;
    }

    @Override
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        if (context == null) {
            throw new InstantiationException("ApplicationContext no inicializado");
        }
        return context.getBean(endpointClass);
    }


    @Override
    public boolean checkOrigin(String originHeader) {
        return true;
    }
}
