package Camaras.VIDEOCAMARAS.infraestructure.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VideoStreamingLogger implements VideoStreamingObserver {

    private static final Logger logger = LoggerFactory.getLogger(VideoStreamingLogger.class);

    @Override
    public void onStreamingStarted(Long cameraId, String sessionId) {
        logger.info("Streaming started for camera {} session {}", cameraId, sessionId);
    }

    @Override
    public void onFrameSent(Long cameraId, String sessionId, int frameSize) {
        logger.debug("Frame sent for camera {} session {}: {} bytes", cameraId, sessionId, frameSize);
    }

    @Override
    public void onStreamingStopped(Long cameraId, String sessionId) {
        logger.info("Streaming stopped for camera {} session {}", cameraId, sessionId);
    }

    @Override
    public void onStreamingError(Long cameraId, String sessionId, Exception ex) {
        logger.error("Streaming error for camera {} session {}: {}", cameraId, sessionId, ex.getMessage());
    }
}

