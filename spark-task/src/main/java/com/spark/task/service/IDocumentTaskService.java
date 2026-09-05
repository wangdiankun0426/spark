package com.spark.task.service;

import com.spark.common.bean.base.ResultData;

import java.util.concurrent.CompletableFuture;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/12/7 下午11:24
 */
public interface IDocumentTaskService {

    /**
     * 文档内容任务
     * @return 异步返回结果，包含操作是否成功的状态信息
     */
    CompletableFuture<ResultData<Void>> executeContentTask();

    /**
     * 文档索引任务
     * @return 异步返回结果，包含操作是否成功的状态信息
     */
    CompletableFuture<ResultData<Void>> executeIndexTask();

    /**
     * 文档分块任务
     * @return 异步返回结果，包含操作是否成功的状态信息
     */
    CompletableFuture<ResultData<Void>> executeChunkTask();

    /**
     * 文档向量化任务
     * @return 异步返回结果，包含操作是否成功的状态信息
     */
    CompletableFuture<ResultData<Void>> executeVectorTask();

    /**
     * 文档图谱任务
     * @return 异步返回结果，包含操作是否成功的状态信息
     */
    CompletableFuture<ResultData<Void>> executeGraphTask();

}
