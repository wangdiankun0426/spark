package com.spark.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/9/14 16:57
 */
@Component
public class OCRUtil {
    private final static Logger logger = LoggerFactory.getLogger(OCRUtil.class);
    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${spark.ocr.url}")
    private String OCR_URL;

    /**
     * 获取图片内容
     * @param filePath
     * @return
     */
    public String readImgContent(String filePath) {
        if (StringUtil.isBlank(filePath)) {
            return "";
        }
        logger.info("readImgContent file path={}", filePath);
        try {
            BufferedImage image = ImageIO.read(new File(filePath));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            byte[] imageBytes = baos.toByteArray();
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return "page.jpg";
                }
            });
            body.add("compress", "960");
            var headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(OCR_URL, HttpMethod.POST, request, Map.class);
            Map<String, Object> result = response.getBody();
            if (result != null && Integer.valueOf(200).equals(result.get("code"))) {
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                return data != null ? (String) data.get("content") : "";
            }
            return "";
        } catch (Exception e) {
            logger.error("getContent error, file path={}", filePath, e);
            return "";
        }
    }

    /**
     * 获取内容
     */
    public String getContent(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            byte[] imageBytes = baos.toByteArray();
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return "page.jpg";
                }
            });
            body.add("compress", "960");
            var headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(OCR_URL, HttpMethod.POST, request, Map.class);
            Map<String, Object> result = response.getBody();
            if (result != null && Integer.valueOf(200).equals(result.get("code"))) {
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                return data != null ? (String) data.get("content") : "";
            }
            return "";
        } catch (Exception e) {
            logger.error("getContent error", e);
            return "";
        }
    }
}
