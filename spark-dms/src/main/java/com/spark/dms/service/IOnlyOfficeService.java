package com.spark.dms.service;

import com.spark.common.bean.base.ResultData;

import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-09-17 15:00:00
 * OnlyOffice 在线编辑服务
 */
public interface IOnlyOfficeService {

    /**
     * 查询编辑器配置
     * @param id 文档id
     * @return 编辑器配置
     */
    ResultData<Map<String, Object>> queryEditorConfig(Long id);

    /**
     * 下载编辑文件
     * @param id 文档id
     * @param ext 文件后缀
     * @param token 下载凭证
     * @return 文件信息
     */
    ResultData<Map<String, Object>> downloadFile(Long id, String ext, String token);

    /**
     * 处理保存回调
     * @param id 文档id
     * @param ext 文件后缀
     * @param token 下载凭证
     * @param body 回调内容
     * @return 回调响应
     */
    Map<String, Object> handleCallback(Long id, String ext, String token, Map<String, Object> body);
}
