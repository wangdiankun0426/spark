package com.spark.dms.service;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.dms.query.DocumentVersionQuery;
import com.spark.common.bean.dms.result.DocumentVersionResult;
import com.spark.common.bean.dms.vo.DocumentVersionVO;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-09-17 10:00:00
 */
public interface IDocumentVersionService {

    /**
     * 上传文档新版本
     * @param docId 文档id
     * @param name 名称
     * @param ext 拓展名
     * @param size 大小
     * @param path 存储路径
     * @return 处理结果
     */
    ResultData<Void> uploadVersion(Long docId, String name, String ext, Long size, String path);

    /**
     * 回滚文档版本
     * @param documentVersionVO 版本参数
     * @return 处理结果
     */
    ResultData<Void> rollbackVersion(DocumentVersionVO documentVersionVO);

    /**
     * 删除文档历史版本
     * @param documentVersionVO 版本参数
     * @return 删除结果
     */
    ResultData<Void> deleteVersion(DocumentVersionVO documentVersionVO);

    /**
     * 分页查询文档版本
     * @param query 查询参数
     * @return 版本列表
     */
    ResultData<PageResult<DocumentVersionResult>> pageVersionList(DocumentVersionQuery query);

    /**
     * 下载文档历史版本
     * @param query 查询参数
     * @return 版本结果
     */
    ResultData<DocumentVersionResult> downloadVersion(DocumentVersionQuery query);
}
