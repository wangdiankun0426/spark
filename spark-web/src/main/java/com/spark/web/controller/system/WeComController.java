package com.spark.web.controller.system;

import com.spark.bean.base.ResultData;
import com.spark.config.wecom.WeComConfig;
import com.spark.manage.si.IWeComService;
import com.spark.bean.base.AESException;
import com.spark.config.wecom.WXBizMsgCrypt;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-28 10:02:00
 * 企业微信集成控制器
 * 接收消息服务器配置接口，处理企业微信回调 URL 验证和消息接收
 */
@RestController
@RequestMapping("weCom")
public class WeComController {
    private static Logger logger = LoggerFactory.getLogger(WeComController.class);
    @Autowired
    private IWeComService weComService;
    @Autowired
    private WeComConfig weComConfig;

    /**
     * 企业微信回调 URL 验证（GET 请求）
     * 企业微信后台配置回调 URL 时，会发送 GET 请求验证 URL 的有效性
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @throws IOException IO 异常
     */
    @GetMapping("receiveMsg")
    public void receiveMessage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        // 微信加密签名
        String msgSignature = request.getParameter("msg_signature");
        // 时间戳
        String timestamp = request.getParameter("timestamp");
        // 随机数
        String nonce = request.getParameter("nonce");
        // 加密的 echostr
        String echoStr = request.getParameter("echostr");
        logger.info("receiveMessageV2 msg_signature={}, timestamp={}, nonce={}, echostr={}", msgSignature, timestamp, nonce, echoStr);
        String result;
        try {
            WXBizMsgCrypt wxcpt = new WXBizMsgCrypt(weComConfig.getMsgToken(), weComConfig.getMsgEncodingAESKey(), weComConfig.getCorpId());
            // 验证 URL 并解密 echostr
            result = wxcpt.verifyUrl(msgSignature, timestamp, nonce, echoStr);
        } catch (AESException e) {
            logger.error("receiveMessageV2 error", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        // 返回解密后的 echostr
        PrintWriter out = response.getWriter();
        out.print(result);
        out.close();
    }

    /**
     * 接收企业微信消息（POST 请求）
     * 企业微信推送消息时，会发送 POST 请求携带加密的 XML 消息体
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @throws IOException IO 异常
     */
    @PostMapping("receiveMsg")
    public void receiveMessageV2(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 将请求、响应的编码均设置为UTF-8（防止中文乱码）
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        // 微信加密签名
        String msg_signature = request.getParameter("msg_signature");
        // 时间戳
        String timestamp = request.getParameter("timestamp");
        // 随机数
        String nonce = request.getParameter("nonce");
        //从请求中读取整个post数据
        InputStream inputStream = request.getInputStream();
        String postData = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        logger.info("receiveMessageV2 postData={}", postData);
        String msg = "";
        WXBizMsgCrypt wxcpt = null;
        try {
            wxcpt = new WXBizMsgCrypt(weComConfig.getMsgToken(), weComConfig.getMsgEncodingAESKey(), weComConfig.getCorpId());
            //解密消息
            msg = wxcpt.decryptMsg(msg_signature, timestamp, nonce, postData);
        } catch (Exception e) {
            logger.error("exeErpWechatMsg error",e);
        }
        logger.info("receiveMessageV2 msg={}", msg);
        // 调用核心业务类接收消息、处理消息
        ResultData<String> result = weComService.handleWeComMsg(msg);
        logger.info("receiveMessageV2 result={}",result);
        String encryptMsg = "";
        try {
            //加密回复消息
            encryptMsg = wxcpt.encryptMsg("", timestamp, nonce);
        } catch (Exception e) {
            logger.error("receiveMessageV2 error",e);
        }
        // 响应消息
        PrintWriter out = response.getWriter();
        out.print(encryptMsg);
        out.close();
    }

    /**
     * 同步企业微信组织架构
     * 包括部门同步和用户同步
     * @return 同步结果
     */
    @GetMapping("syncOrganization")
    public ResultData<String> syncOrganization() {
        return weComService.syncWeComOrganization();
    }
}