package com.itguo.guooj.controller;

import com.itguo.guooj.common.BaseResponse;
import com.itguo.guooj.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/captcha")
@Slf4j
public class CaptchaController {

    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final int CAPTCHA_LENGTH = 4;
    private static final long CAPTCHA_EXPIRE_SECONDS = 300;
    private static final int IMAGE_WIDTH = 130;
    private static final int IMAGE_HEIGHT = 40;
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/get")
    public BaseResponse<Map<String, String>> getCaptcha() throws IOException {
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        String code = generateCode();

        stringRedisTemplate.opsForValue().set(
                CAPTCHA_PREFIX + captchaId,
                code.toLowerCase(),
                CAPTCHA_EXPIRE_SECONDS,
                TimeUnit.SECONDS
        );

        String base64Image = generateImageBase64(code);

        Map<String, String> result = new HashMap<>();
        result.put("captchaId", captchaId);
        result.put("captchaImage", "data:image/png;base64," + base64Image);

        return ResultUtils.success(result);
    }

    private String generateCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    private String generateImageBase64(String code) throws IOException {
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(new Color(245, 245, 245));
        g.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

        Random random = new Random();

        // 干扰线
        for (int i = 0; i < 6; i++) {
            g.setColor(new Color(200 + random.nextInt(55), 200 + random.nextInt(55), 200 + random.nextInt(55)));
            g.setStroke(new BasicStroke(1.2f));
            g.drawLine(random.nextInt(IMAGE_WIDTH), random.nextInt(IMAGE_HEIGHT),
                    random.nextInt(IMAGE_WIDTH), random.nextInt(IMAGE_HEIGHT));
        }

        // 干扰点
        for (int i = 0; i < 30; i++) {
            g.setColor(new Color(180 + random.nextInt(75), 180 + random.nextInt(75), 180 + random.nextInt(75)));
            g.fillOval(random.nextInt(IMAGE_WIDTH), random.nextInt(IMAGE_HEIGHT), 2, 2);
        }

        String[] fontNames = {"Arial", "Verdana", "Tahoma", "Georgia"};
        Color[] colors = {
                new Color(59, 89, 152),
                new Color(220, 68, 55),
                new Color(15, 157, 88),
                new Color(244, 160, 0),
                new Color(102, 51, 153)
        };

        int charWidth = (IMAGE_WIDTH - 20) / CAPTCHA_LENGTH;
        for (int i = 0; i < code.length(); i++) {
            g.setFont(new Font(fontNames[random.nextInt(fontNames.length)], Font.BOLD, 24 + random.nextInt(6)));
            g.setColor(colors[random.nextInt(colors.length)]);

            double angle = (random.nextDouble() - 0.5) * 0.4;
            int x = 10 + i * charWidth;
            int y = 28 + random.nextInt(6);

            g.rotate(angle, x, y);
            g.drawString(String.valueOf(code.charAt(i)), x, y);
            g.rotate(-angle, x, y);
        }

        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    /**
     * 校验验证码（供其他 Controller 调用）
     */
    public boolean verifyCaptcha(String captchaId, String captchaCode) {
        if (captchaId == null || captchaCode == null) {
            return false;
        }
        String key = CAPTCHA_PREFIX + captchaId;
        String storedCode = stringRedisTemplate.opsForValue().get(key);
        if (storedCode == null) {
            return false;
        }
        stringRedisTemplate.delete(key);
        return storedCode.equals(captchaCode.toLowerCase());
    }
}
