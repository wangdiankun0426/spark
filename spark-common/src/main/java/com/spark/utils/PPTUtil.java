package com.spark.utils;

import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextShape;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/12/22 下午4:31
 */
public class PPTUtil {
    private static final Logger logger = LoggerFactory.getLogger(PPTUtil.class);

    /**
     * 获取ppt文件内容
     * @param filePath
     * @return
     */
    public static String readPPTContent(String filePath) {
        StringBuilder content = new StringBuilder();
        try {
            FileInputStream fis = new FileInputStream(filePath);
            HSLFSlideShow ppt = new HSLFSlideShow(fis);
            if (ppt.getSlides() == null) {
                return content.toString();
            }
            for (HSLFSlide slide : ppt.getSlides()) {
                List<HSLFShape> shapes = slide.getShapes();
                if (shapes == null) {
                    continue;
                }
                for (HSLFShape shape : shapes) {
                    if (shape instanceof HSLFTextShape) {
                        HSLFTextShape textShape = (HSLFTextShape) shape;
                        content.append(textShape.getText());
                        content.append("\n");
                    }
                }
            }
        } catch (IOException e) {
            logger.error("readPPTContent error, file path is {}", filePath, e);
        }
        return content.toString();
    }

    /**
     * 获取pptx文件内容
     * @param filePath
     * @return
     */
    public static String readPPTXContent(String filePath) {
        StringBuilder content = new StringBuilder();
        try {
            FileInputStream fis = new FileInputStream(filePath);
            XMLSlideShow ppt = new XMLSlideShow(fis);
            if (ppt.getSlides() == null) {
                return content.toString();
            }
            for (XSLFSlide slide : ppt.getSlides()) {
                List<XSLFShape> shapes = slide.getShapes();
                if (shapes == null) {
                    continue;
                }
                for (XSLFShape shape : shapes) {
                    if (shape instanceof XSLFTextShape) {
                        XSLFTextShape textShape = (XSLFTextShape) shape;
                        content.append(textShape.getText());
                        content.append("\n");
                    }
                }
            }
        } catch (IOException e) {
            logger.error("readPPTXContent error, file path is {}", filePath, e);
        }
        return content.toString();
    }

    }
