package com.spark.manage.system;

import com.spark.bean.system.query.NoticeQuery;
import com.spark.bean.system.result.NoticeResult;
import com.spark.bean.system.vo.NoticeVO;
import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/5/6 13:35
 */
public interface INoticeService {

    /**
     * 创建公告
     * @param noticeVO 公告数据
     * @return 创建结果
     */
    ResultData<Void> createNotice(NoticeVO noticeVO);

    /**
     * 修改公告
     * @param noticeVO 修改的参数
     * @return 修改结果
     */
    ResultData<Void> updateNotice(NoticeVO noticeVO);

    /**
     * 下架公告
     * @param noticeVO 删除的参数
     * @return 删除结果
     */
    ResultData<Void> delistNotice(NoticeVO noticeVO);

    /**
     * 删除公告
     * @param noticeVO 删除的参数
     * @return 删除结果
     */
    ResultData<Void> deleteNotice(NoticeVO noticeVO);

    /**
     * 查询公告详情
     * @param query 查询参数
     * @return 查询结果
     */
    ResultData<NoticeResult> detailNotice(NoticeQuery query);

    /**
     * 分页查询
     * @param query 查询参数
     * @return 列表
     */
    ResultData<PageResult<NoticeResult>> pageNoticeList(NoticeQuery query);

    /**
     * 保存文本公告文本
     * @param noticeVO 保存的参数
     * @return 保存结果
     */
    ResultData<Void> saveNoticeText(NoticeVO noticeVO);

    /**
     * 查询公告列表
     * @param query 查询参数
     * @return 列表
     */
    ResultData<List<NoticeResult>> queryNoticeList(NoticeQuery query);

    /**
     * 查看公告文本
     * @param query 查询参数
     * @return 文本
     */
    ResultData<String> viewNoticeText(NoticeQuery query);
}
