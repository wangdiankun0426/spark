package com.spark.bean.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/2/17 15:33
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseQuery implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * ids
     */
    private List<Long> ids;

    /**
     * 删除标记位
     */
    private Integer deleteFlag = 1;

    /**
     * 是否分页
     */
    private boolean page = true;

    /**
     * 页码
     */
    private Integer pageNo = 1;

    /**
     * 数量
     */
    private Integer pageSize = 30;

    /**
     * 起始行
     */
    private Integer startRow = 0;

    /**
     * 数据权限sql
     */
    private String dataScopeSQL;

    /**
     * 创建时间 起始
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createdStartTime;

    /**
     * 创建时间 结束
     */
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createdEndTime;

    /**
     * 创建人
     */
    private Long createdBy;

    /**
     * 部门id
     */
    private Long deptId;

    /**
     * 排序列表
     */
    private Map<String, String> sorts;

    public Integer getStartRow() {
        return (pageNo - 1) * pageSize;
    }

}
