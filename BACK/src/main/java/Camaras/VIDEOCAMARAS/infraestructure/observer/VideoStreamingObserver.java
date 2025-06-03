package Camaras.VIDEOCAMARAS.infraestructure.observer;

public interface VideoStreamingObserver {
    void onStreamingStarted(Long cameraId, String sessionId);
    void onFrameSent(Long cameraId, String sessionId, int frameSize);
    void onStreamingStopped(Long cameraId, String sessionId);
    void onStreamingError(Long cameraId, String sessionId, Exception ex);
}

