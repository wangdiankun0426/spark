package com.spark.dao.system;

import com.spark.bean.system.entity.Attachment;
import com.spark.bean.system.query.AttachmentQuery;
import com.spark.bean.system.result.AttachmentResult;
import com.spark.dao.BaseDao;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-20 14:35:00
 * 系统附件DAO
 */
public interface AttachmentDao extends BaseDao<Attachment> {

    /**
     * 插入数据
     * @param attachment 系统附件
     * @return 插入数量
     */
    @Override
    int insert(Attachment attachment);

    /**
     * 删除数据（逻辑删除）
     * @param attachment 系统附件
     * @return 删除数量
     */
    @Override
    int deleteById(Attachment attachment);

    /**
     * 修改数据
     * @param attachment 系统附件
     * @return 修改数量
     */
    @Override
    int updateById(Attachment attachment);

    /**
     * 查询数量
     * @param query 查询参数
     * @return 数量
     */
    int queryAttachmentCount(AttachmentQuery query);

    /**
     * 查询列表
     * @param query 查询参数
     * @return 列表
     */
    List<AttachmentResult> queryAttachmentList(AttachmentQuery query);

    /**
     * 查询单条
     * @param query 查询参数
     * @return 系统附件
     */
    AttachmentResult queryAttachment(AttachmentQuery query);

    /**
     * 查询最大id
     * @return 最大id
     */
    Long queryAttachmentMaxId();
}
