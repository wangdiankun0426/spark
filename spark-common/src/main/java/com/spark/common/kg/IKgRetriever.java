package com.spark.common.kg;

import com.spark.common.bean.base.ResultData;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-29 16:00:00
 * 知识图谱召回能力接口，实现位于知识图谱模块，供模型层与工作流按接口注入
 */
public interface IKgRetriever {

    /**
     * 图谱检索
     * @param question 查询问题
     * @param graphIds 图谱ID列表
     * @return 三元组文本列表
     */
    ResultData<List<String>> retrieve(String question, List<Long> graphIds);
}
