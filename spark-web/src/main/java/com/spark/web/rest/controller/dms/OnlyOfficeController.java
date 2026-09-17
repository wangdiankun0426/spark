package com.spark.web.rest.controller.dms;

import com.spark.common.bean.base.ResultData;
import com.spark.dms.service.IOnlyOfficeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-09-17 15:00:00
 * OnlyOffice 在线预览控制器
 */
@RestController
@RequestMapping("dms/onlyoffice")
public class OnlyOfficeController {
    private static final Logger logger = LoggerFactory.getLogger(OnlyOfficeController.class);
    @Autowired
    private IOnlyOfficeService onlyOfficeService;

    /**
     * 查询编辑器配置
     * @param id 文档或附件id
     * @return 编辑器配置
     */
    @GetMapping("config")
    private ResultData<Map<String, Object>> queryEditorConfig(@RequestParam("id") Long id) {
        return onlyOfficeService.queryEditorConfig(id);
    }

    /**
     * 处理保存回调（OnlyOffice 服务器回调，凭证免登录）
     * @param id 文档id
     * @param ext 文件后缀
     * @param token 下载凭证
     * @param body 回调内容
     * @return 回调响应
     */
    @PostMapping("callback")
    private Map<String, Object> handleCallback(@RequestParam("id") Long id, @RequestParam("ext") String ext, @RequestParam("token") String token, @RequestBody Map<String, Object> body) {
        return onlyOfficeService.handleCallback(id, ext, token, body);
    }

    /**
     * 下载预览文件（OnlyOffice 服务器回调，凭证免登录）
     * @param response 响应
     * @param id 文档或附件id
     * @param ext 文件后缀
     * @param token 下载凭证
     */
    @GetMapping("download")
    private void downloadFile(HttpServletRequest request, HttpServletResponse response, @RequestParam("id") Long id, @RequestParam("ext") String ext, @RequestParam("token") String token) {
        ResultData<Map<String, Object>> result = onlyOfficeService.downloadFile(id, ext, token);
        if (result.getCode() != ResultData.OK) {
            return;
        }
        Map<String, Object> fileInfo = result.getData();
        try {
            File file = new File((String) fileInfo.get("path"));
            String filename = (String) fileInfo.get("name");
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStream fis = new BufferedInputStream(fileInputStream);
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            response.setCharacterEncoding("UTF-8");
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
            response.addHeader("Content-Length", "" + file.length());
            OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            outputStream.write(buffer);
            outputStream.flush();
        } catch (IOException ex) {
            logger.error("onlyoffice downloadFile error", ex);
        }
    }
}
