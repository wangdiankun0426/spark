package com.spark.dao.system;

import com.spark.bean.system.entity.Notice;
import com.spark.bean.system.query.NoticeQuery;
import com.spark.bean.system.result.NoticeResult;
import com.spark.dao.BaseDao;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/5/6 17:43
 */
public interface NoticeDao extends BaseDao<Notice> {
    /**
     * 插入数据
     * @param notice
     * @return
     */
    @Override
    int insert(Notice notice);

    /**
     * 删除数据
     * @param notice
     * @return
     */
    @Override
    int deleteById(Notice notice);

    /**
     * 修改数据
     * @param notice
     * @return
     */
    @Override
    int updateById(Notice notice);

    /**
     * 查询数量
     * @param query
     * @return
     */
    int queryNoticeCount(NoticeQuery query);

    /**
     * 查询列表
     * @param query
     * @return
     */
    List<NoticeResult> queryNoticeList(NoticeQuery query);

    /**
     * 查询单条
     * @param query
     * @return
     */
    NoticeResult queryNotice(NoticeQuery query);
}
