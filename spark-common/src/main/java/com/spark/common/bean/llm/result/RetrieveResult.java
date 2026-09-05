package com.spark.common.bean.llm.result;

import lombok.Data;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-21 10:00:00
 * 召回结果
 * （包含给LLM的文本和给前端的参考文档）
 */
@Data
public class RetrieveResult {

    /**
     * 给LLM用的文本列表
     */
    private List<String> contents;

    /**
     * 参考文档id列表
     */
    private List<Long> references;
}
