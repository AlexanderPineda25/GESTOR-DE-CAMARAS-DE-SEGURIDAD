package Camaras.VIDEOCAMARAS.aplication.service.impl;

import Camaras.VIDEOCAMARAS.domain.model.enums.FilterType;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.function.Function;

@Service
public class ImageProcessingEngine {

    private static final Set<String> SUPPORTED_FORMATS = Set.of("jpg", "jpeg", "png");
    private static final Map<FilterType, Function<BufferedImage, BufferedImage>> FILTER_OPERATIONS;

    static {
        Map<FilterType, Function<BufferedImage, BufferedImage>> operations = new EnumMap<>(FilterType.class);
        operations.put(FilterType.GRAYSCALE, ImageProcessingEngine::applyGrayscale);
        operations.put(FilterType.BLUR, ImageProcessingEngine::applyBlur);
        operations.put(FilterType.EDGE_DETECTION, ImageProcessingEngine::applyEdgeDetection);
        operations.put(FilterType.SEPIA, ImageProcessingEngine::applySepia);
        operations.put(FilterType.INVERT, ImageProcessingEngine::applyInvert);
        FILTER_OPERATIONS = Collections.unmodifiableMap(operations);
    }

    public String applyFilters(String originalPath, List<FilterType> filters) throws ImageProcessingException {
        if (originalPath == null || originalPath.isBlank()) {
            throw new IllegalArgumentException("Image path cannot be empty");
        }
        BufferedImage image = loadImage(originalPath);
        try {
            BufferedImage result = applyFilterChain(image, filters);
            return saveProcessedImage(result, originalPath);
        } finally {
            image.flush();
        }
    }

    private BufferedImage loadImage(String originalPath) throws ImageProcessingException {
        try {
            Path imagePath = Paths.get(originalPath);
            if (!Files.exists(imagePath) || !Files.isReadable(imagePath)) {
                throw new IOException("Image file cannot be read");
            }
            BufferedImage image = ImageIO.read(imagePath.toFile());
            if (image == null) {
                throw new IOException("Unsupported image format");
            }
            return image;
        } catch (IOException e) {
            throw new ImageProcessingException("Error loading image", e);
        }
    }

    private BufferedImage applyFilterChain(BufferedImage image, List<FilterType> filters) {
        BufferedImage result = image;
        for (FilterType filter : filters) {
            Function<BufferedImage, BufferedImage> operation = FILTER_OPERATIONS.get(filter);
            if (operation != null) {
                result = operation.apply(result);
            }
        }
        return result;
    }

    private String saveProcessedImage(BufferedImage image, String originalPath) throws ImageProcessingException {
        try {
            String formatName = getImageFormat(originalPath);
            String outputPath = generateOutputPath(originalPath, formatName);
            Path outputDir = Paths.get(outputPath).getParent();
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }
            if (!ImageIO.write(image, formatName, new File(outputPath))) {
                throw new IOException("Unsupported image format for writing: " + formatName);
            }
            return outputPath;
        } catch (IOException e) {
            throw new ImageProcessingException("Error saving processed image", e);
        }
    }

    private static String generateOutputPath(String originalPath, String formatName) {
        String filename = String.format("processed_%s_%d.%s",
                UUID.randomUUID().toString().substring(0, 8),
                System.currentTimeMillis(),
                formatName);
        return Paths.get(System.getProperty("java.io.tmpdir"), "processed_images", filename).toString();
    }

    private static String getImageFormat(String path) {
        String extension = path.substring(path.lastIndexOf('.') + 1).toLowerCase();
        return SUPPORTED_FORMATS.contains(extension) ? extension : "jpg";
    }

    private static BufferedImage applyGrayscale(BufferedImage src) {
        BufferedImage result = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = result.getGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return result;
    }

    private static BufferedImage applyBinarize(BufferedImage src) {
        BufferedImage gray = applyGrayscale(src);
        BufferedImage result = new BufferedImage(gray.getWidth(), gray.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2d = result.createGraphics();
        g2d.drawImage(gray, 0, 0, null);
        g2d.dispose();
        return result;
    }

    private static BufferedImage applySepia(BufferedImage src) {
        BufferedImage result = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                Color color = new Color(src.getRGB(x, y));
                int r = (int) Math.min(255, color.getRed() * 0.393 + color.getGreen() * 0.769 + color.getBlue() * 0.189);
                int g = (int) Math.min(255, color.getRed() * 0.349 + color.getGreen() * 0.686 + color.getBlue() * 0.168);
                int b = (int) Math.min(255, color.getRed() * 0.272 + color.getGreen() * 0.534 + color.getBlue() * 0.131);
                result.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }
        return result;
    }

    public static class ImageProcessingException extends Exception {
        public ImageProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static BufferedImage applyBlur(BufferedImage src) {
        BufferedImage result = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);

        for (int y = 1; y < src.getHeight() - 1; y++) {
            for (int x = 1; x < src.getWidth() - 1; x++) {
                int red = 0, green = 0, blue = 0;

                // Kernel 3x3 simple para desenfoque
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        Color pixel = new Color(src.getRGB(x + kx, y + ky));
                        red += pixel.getRed();
                        green += pixel.getGreen();
                        blue += pixel.getBlue();
                    }
                }

                red /= 9;
                green /= 9;
                blue /= 9;

                result.setRGB(x, y, new Color(red, green, blue).getRGB());
            }
        }
        return result;
    }

    private static BufferedImage applyEdgeDetection(BufferedImage src) {
        BufferedImage gray = applyGrayscale(src);
        BufferedImage result = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);

        int[][] sobelX = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
        int[][] sobelY = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};

        for (int y = 1; y < gray.getHeight() - 1; y++) {
            for (int x = 1; x < gray.getWidth() - 1; x++) {
                int pixelX = 0, pixelY = 0;

                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        Color pixel = new Color(gray.getRGB(x + kx, y + ky));
                        int grayValue = pixel.getRed(); // En escala de grises, R=G=B

                        pixelX += grayValue * sobelX[ky + 1][kx + 1];
                        pixelY += grayValue * sobelY[ky + 1][kx + 1];
                    }
                }

                int magnitude = (int) Math.sqrt(pixelX * pixelX + pixelY * pixelY);
                magnitude = Math.min(255, Math.max(0, magnitude));

                result.setRGB(x, y, new Color(magnitude, magnitude, magnitude).getRGB());
            }
        }
        return result;
    }

    private static BufferedImage applyInvert(BufferedImage src) {
        BufferedImage result = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                Color color = new Color(src.getRGB(x, y));
                result.setRGB(x, y, new Color(
                        255 - color.getRed(),
                        255 - color.getGreen(),
                        255 - color.getBlue()).getRGB());
            }
        }
        return result;
    }
}
