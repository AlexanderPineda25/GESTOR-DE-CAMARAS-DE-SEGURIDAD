package Camaras.VIDEOCAMARAS.aplication.service.impl;

import Camaras.VIDEOCAMARAS.aplication.service.CameraService;
import Camaras.VIDEOCAMARAS.domain.model.Camera;
import org.springframework.stereotype.Service;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.springframework.beans.factory.annotation.Value;
import org.bytedeco.javacv.Frame;

@Service
public class FrameGrabberService {

    private final CameraService cameraService;

    @Value("${streaming.grabber.width:1280}")
    private int grabberWidth;

    @Value("${streaming.grabber.height:720}")
    private int grabberHeight;

    @Value("${streaming.grabber.rtsp.transport:tcp}")
    private String rtspTransport;

    @Value("${streaming.grabber.frame.rate:30}")
    private int frameRate;

    public FrameGrabberService(CameraService cameraService) {
        this.cameraService = cameraService;
    }

    public FFmpegFrameGrabber createGrabber(Long cameraId) throws FrameGrabber.Exception {
        Camera camera = cameraService.findEntityById(cameraId);
        String streamUrl = camera.getStreamUrl();
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(streamUrl);
        configureGrabber(grabber, streamUrl);
        return grabber;
    }

    private void configureGrabber(FFmpegFrameGrabber grabber, String streamUrl) {
        if (streamUrl.startsWith("rtsp://")) {
            grabber.setFormat("rtsp");
            grabber.setOption("rtsp_transport", rtspTransport);
        } else if (streamUrl.startsWith("http://") && streamUrl.contains("/video")) {
            grabber.setFormat("mjpeg"); // <-- SOLO SI TIENES PROBLEMAS SIN ESTA LÍNEA
        }
        grabber.setImageWidth(grabberWidth);
        grabber.setImageHeight(grabberHeight);
        grabber.setFrameRate(frameRate);
        grabber.setOption("stimeout", "5000000");
        grabber.setOption("threads", "2");
    }


    public void reconfigureGrabber(FFmpegFrameGrabber grabber, int width, int height, int fps) {
        try {
            grabber.setImageWidth(width);
            grabber.setImageHeight(height);
            grabber.setFrameRate(fps);
            grabber.restart();
        } catch (Exception e) {
            throw new RuntimeException("Error reconfigurando grabber", e);
        }
    }

    public Frame grabLatestFrame(Long cameraId) {
        FFmpegFrameGrabber grabber = null;
        try {
            grabber = createGrabber(cameraId);
            grabber.start();

            // Configuración para reducir latencia
            grabber.setOption("fflags", "nobuffer");
            grabber.setOption("flags", "low_delay");

            Frame frame = null;
            int attempts = 0;
            final int maxAttempts = 3;

            while (attempts < maxAttempts && frame == null) {
                frame = grabber.grabImage();
                if (frame == null) {
                    attempts++;
                    Thread.sleep(100); // Pequeña pausa entre intentos
                }
            }

            if (frame == null) {
                throw new RuntimeException("No se pudo obtener frame de la cámara después de " + maxAttempts + " intentos");
            }

            return frame;
        } catch (Exception e) {
            throw new RuntimeException("Error al capturar frame: " + e.getMessage(), e);
        } finally {
            if (grabber != null) {
                try {
                    grabber.stop();
                } catch (Exception e) {
                    System.err.println("Error al detener grabber: " + e.getMessage());
                }
            }
        }
    }
}
