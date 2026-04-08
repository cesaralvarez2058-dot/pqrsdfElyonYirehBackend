package com.elyonyireh.pqrsdfelyonyirehbackend.services.impl;

import com.elyonyireh.pqrsdfelyonyirehbackend.dtos.CaptchaResponseDTO;
import com.elyonyireh.pqrsdfelyonyirehbackend.services.CaptchaService;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaServiceImpl implements CaptchaService {

    // Almacén en memoria: Key = captchaId, Value = captchaText
    private final Map<String, String> captchaStore = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    
    // Caracteres permitidos evitando los confusos (O, 0, I, l)
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";

    @Override
    public CaptchaResponseDTO generarCaptcha() {
        String captchaText = generateRandomText(6);
        String captchaId = UUID.randomUUID().toString();
        
        // Guardar el texto asociado al ID
        captchaStore.put(captchaId, captchaText);
        
        // Generar imagen AWT
        String base64Image = createCaptchaImageBase64(captchaText);
        
        return new CaptchaResponseDTO(captchaId, base64Image);
    }

    @Override
    public boolean validarCaptcha(String captchaId, String captchaValue) {
        if (captchaId == null || captchaValue == null) return false;
        
        String storedCaptcha = captchaStore.get(captchaId);
        if (storedCaptcha != null) {
            // Eliminarlo para que no se re-use (consumo de único uso)
            captchaStore.remove(captchaId);
            return storedCaptcha.equals(captchaValue);
        }
        return false;
    }

    private String generateRandomText(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    private String createCaptchaImageBase64(String text) {
        int width = 180;
        int height = 50;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Fondo y renderizado
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(248, 250, 252)); // Color claro
        g2d.fillRect(0, 0, width, height);

        // Ruido y ruido de fondo (Líneas e interferencia)
        drawNoise(g2d, width, height);

        // Configuración de fuentes y dibujado
        Font[] fonts = {
                new Font("Arial", Font.BOLD, 26),
                new Font("Verdana", Font.BOLD, 24),
                new Font("Courier New", Font.BOLD, 28)
        };

        int x = 15;
        for (int i = 0; i < text.length(); i++) {
            g2d.setFont(fonts[random.nextInt(fonts.length)]);
            g2d.setColor(new Color(random.nextInt(100), random.nextInt(100), random.nextInt(100))); // Tonos oscuros
            
            AffineTransform affineTransform = new AffineTransform();
            affineTransform.rotate(Math.toRadians(random.nextInt(40) - 20), 0, 0); // Rotación entre -20 y 20 grados
            Font renderFont = g2d.getFont().deriveFont(affineTransform);
            g2d.setFont(renderFont);

            g2d.drawString(String.valueOf(text.charAt(i)), x, 35 + random.nextInt(10) - 5);
            x += 25;
        }

        g2d.dispose();

        // Convertir a Base64
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error generador de imagen CAPTCHA", e);
        }
    }

    private void drawNoise(Graphics2D g2d, int width, int height) {
        // Líneas
        for (int i = 0; i < 6; i++) {
            g2d.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255), 150));
            g2d.setStroke(new BasicStroke(1 + random.nextFloat() * 2));
            g2d.drawLine(random.nextInt(width), random.nextInt(height), random.nextInt(width), random.nextInt(height));
        }
        // Ovalos
        for (int i = 0; i < 8; i++) {
            g2d.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255), 100));
            int rValue = random.nextInt(20);
            g2d.drawOval(random.nextInt(width), random.nextInt(height), rValue, rValue);
        }
    }
}
