package com.spark.web.controller.system;

import com.spark.bean.system.query.NoticeQuery;
import com.spark.bean.system.result.NoticeResult;
import com.spark.bean.system.vo.NoticeVO;
import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.manage.system.INoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/5/6 13:33
 */
@RestController
@RequestMapping("system/notice")
public class NoticeController {
    @Autowired
    private INoticeService noticeService;

    /**
     * 创建公告
     * @param noticeVO 公告参数
     * @return 创建结果
     */
    @PostMapping("create")
    public ResultData<Void> createNotice(NoticeVO noticeVO) {
        return noticeService.createNotice(noticeVO);
    }

    /**
     * 修改公告
     * @param noticeVO 修改的参数
     * @return 修改结果
     */
    @PostMapping("update")
    public ResultData<Void> updateNotice(NoticeVO noticeVO) {
        return noticeService.updateNotice(noticeVO);
    }

    /**
     * 下架公告
     * @param noticeVO 删除的参数
     * @return 删除结果
     */
    @PostMapping("delist")
    public ResultData<Void> delistNotice(NoticeVO noticeVO) {
        return noticeService.delistNotice(noticeVO);
    }

    /**
     * 删除公告
     * @param noticeVO 删除的参数
     * @return 删除结果
     */
    @PostMapping("delete")
    public ResultData<Void> deleteNotice(NoticeVO noticeVO) {
        return noticeService.deleteNotice(noticeVO);
    }

    /**
     * 分页查询
     * @param query 查询参数
     * @return 查询结果
     */
    @GetMapping("pageList")
    public ResultData<PageResult<NoticeResult>> pageNoticeList(NoticeQuery query) {
        return noticeService.pageNoticeList(query);
    }

    /**
     * 公告详情
     * @param query 查询参数
     * @return 查询结果
     */
    @GetMapping("detail")
    public ResultData<NoticeResult> detailNotice(NoticeQuery query) {
        return noticeService.detailNotice(query);
    }

    /**
     * 保存文本
     * @param noticeVO 保存文本参数
     * @return   保存结果
     */
    @PostMapping("saveText")
    private ResultData<Void> saveText(@RequestBody NoticeVO noticeVO) {
        return noticeService.saveNoticeText(noticeVO);
    }

    /**
     * 查询公告列表
     * @param query 查询参数
     * @return 列表
     */
    @GetMapping("list")
    public ResultData<List<NoticeResult>> queryNoticeList(NoticeQuery query) {
        return noticeService.queryNoticeList(query);
    }

    /**
     * 查看文本
     * @param query 查询参数
     * @return 文本
     */
    @GetMapping("viewText")
    private ResultData<String> viewText(NoticeQuery query) {
        return noticeService.viewNoticeText(query);
    }
}
