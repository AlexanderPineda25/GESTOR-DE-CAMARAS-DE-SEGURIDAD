package Camaras.VIDEOCAMARAS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import static org.bytedeco.ffmpeg.global.avutil.*;

@SpringBootApplication
public class VideocamarasApplication {
	public static void main(String[] args) {
		av_log_set_level(AV_LOG_ERROR);
		SpringApplication.run(VideocamarasApplication.class, args);
	}
}
