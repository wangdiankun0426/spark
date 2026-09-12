package com.spark.web.rest.controller.manage.external;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.enums.TenantConfigEnum;
import com.spark.manage.external.IWeComService;
import com.spark.common.bean.base.AESException;
import com.spark.config.wecom.WeComMsgCryptUtil;
import com.spark.manage.sys.ITenantConfigService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

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
 * 多租户场景下回调接口通过 URL 上的 tenantId 参数区分租户，取该租户的企微配置进行验签与解密，
 * 形如：https://域名/weCom/receiveMsg?tenantId=租户id
 */
@RestController
@RequestMapping("weCom")
public class WeComController {
    private static Logger logger = LoggerFactory.getLogger(WeComController.class);
    @Autowired
    private IWeComService weComService;
    @Autowired
    private ITenantConfigService tenantConfigService;

    /**
     * 企业微信回调 URL 验证
     * 企业微信后台配置回调 URL 时，会发送 GET 请求验证 URL 的有效性
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @param tenantId 租户id，回调 URL 上携带
     * @throws IOException IO 异常
     */
    @GetMapping("receiveMsg")
    public void receiveMessage(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "tenantId", required = false) Long tenantId) throws IOException {
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
        logger.info("receiveMessage tenantId={}, msg_signature={}, timestamp={}, nonce={}, echostr={}", tenantId, msgSignature, timestamp, nonce, echoStr);
        // 按租户取企微配置
        ResultData<Map<String, String>> mapResult = tenantConfigService.queryTenantConfigMap(tenantId);
        Map<String, String> weComConfigMap = mapResult.getData();
        if (weComConfigMap == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        String result;
        try {
            WeComMsgCryptUtil wxcpt = new WeComMsgCryptUtil(weComConfigMap.get(TenantConfigEnum.WECOM_MSG_TOKEN.getKey()),
                    weComConfigMap.get(TenantConfigEnum.WECOM_MSG_ENCODING_AES_KEY.getKey()),
                    weComConfigMap.get(TenantConfigEnum.WECOM_CORP_ID.getKey()));
            // 验证 URL 并解密 echostr
            result = wxcpt.verifyUrl(msgSignature, timestamp, nonce, echoStr);
        } catch (AESException e) {
            logger.error("receiveMessage error, tenantId={}", tenantId, e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        // 返回解密后的 echostr
        PrintWriter out = response.getWriter();
        out.print(result);
        out.close();
    }

    /**
     * 接收企业微信消息
     * 企业微信推送消息时，会发送 POST 请求携带加密的 XML 消息体
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @param tenantId 租户id，回调 URL 上携带
     * @throws IOException IO 异常
     */
    @PostMapping("receiveMsg")
    public void receiveMessageV2(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "tenantId", required = false) Long tenantId) throws IOException {
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
        logger.info("receiveMessageV2 tenantId={}, postData={}", tenantId, postData);
        // 按租户取企微配置
        ResultData<Map<String, String>> mapResult = tenantConfigService.queryTenantConfigMap(tenantId);
        Map<String, String> weComConfigMap = mapResult.getData();
        if (weComConfigMap == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        String msg;
        WeComMsgCryptUtil wxcpt;
        try {
            wxcpt = new WeComMsgCryptUtil(weComConfigMap.get(TenantConfigEnum.WECOM_MSG_TOKEN.getKey()),
                    weComConfigMap.get(TenantConfigEnum.WECOM_MSG_ENCODING_AES_KEY.getKey()),
                    weComConfigMap.get(TenantConfigEnum.WECOM_CORP_ID.getKey()));
            //解密消息
            msg = wxcpt.decryptMsg(msg_signature, timestamp, nonce, postData);
        } catch (Exception e) {
            logger.error("receiveMessageV2 decrypt error, tenantId={}", tenantId, e);
            return;
        }
        logger.info("receiveMessageV2 msg={}", msg);
        String encryptMsg = "";
        try {
            // 绑定租户上下文，供消息处理时按租户落库
            SessionHolder.setCurrentTenantId(tenantId);
            // 调用核心业务类接收消息、处理消息
            ResultData<String> result = weComService.handleWeComMsg(msg);
            logger.info("receiveMessageV2 result={}", result);
            //加密回复消息
            encryptMsg = wxcpt.encryptMsg("", timestamp, nonce);
        } catch (Exception e) {
            logger.error("receiveMessageV2 error, tenantId={}", tenantId, e);
        } finally {
            // 清理租户上下文，避免线程复用串租户
            SessionHolder.setCurrentTenantId(null);
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
    @GetMapping("syncOrg")
    public ResultData<Void> syncOrg() {
        return weComService.syncWeComOrganization();
    }

}