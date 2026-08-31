package com.spark.kg.service.impl;

import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.kg.query.KgCommunityQuery;
import com.spark.bean.kg.result.KgCommunityResult;
import com.spark.dao.kg.KgCommunityDao;
import com.spark.kg.service.IKgCommunityService;
import com.spark.manage.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-31 19:00:00
 * 图谱社区服务实现
 */
@Service
public class KgCommunityServiceImpl extends BaseService<KgCommunityQuery, KgCommunityResult> implements IKgCommunityService {
    private final static Logger logger = LoggerFactory.getLogger(KgCommunityServiceImpl.class);
    @Autowired
    private KgCommunityDao kgCommunityDao;

    /**
     * 分页查询社区列表
     * @param query 查询条件
     * @return 分页结果
     */
    @Override
    public ResultData<PageResult<KgCommunityResult>> pageCommunityList(KgCommunityQuery query) {
        ResultData<PageResult<KgCommunityResult>> result = new ResultData<>();
        if (query == null) {
            query = new KgCommunityQuery();
        }
        PageResult<KgCommunityResult> pageResult = super.pageList(query);
        result.setData(pageResult);
        result.setCode(ResultData.OK);
        return result;
    }

    @Override
    protected void supplyList(List<KgCommunityResult> list) {
    }

    @Override
    protected int queryCount(KgCommunityQuery query) {
        return kgCommunityDao.queryKgCommunityCount(query);
    }

    @Override
    protected List<KgCommunityResult> queryList(KgCommunityQuery query) {
        return kgCommunityDao.queryKgCommunityList(query);
    }
}
