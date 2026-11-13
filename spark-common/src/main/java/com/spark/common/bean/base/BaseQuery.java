package com.spark.common.bean.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
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
     * 数据权限sql，仅服务端切面写入，禁止请求绑定
     */
    @JsonIgnore
    private String dataScopeSQL = "";

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
     * 租户id
     */
    private Long tenantId;

    /**
     * 排序列表
     */
    private Map<String, String> sorts;

    public Integer getStartRow() {
        return (pageNo - 1) * pageSize;
    }

    /**
     * 排序方向消毒，仅保留asc与desc，非法值丢弃
     * @return 消毒后的排序映射
     */
    public Map<String, String> getSorts() {
        if (sorts == null || sorts.isEmpty()) {
            return sorts;
        }
        Map<String, String> safeSorts = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : sorts.entrySet()) {
            String column = entry.getKey();
            String direction = entry.getValue();
            if (direction == null) {
                continue;
            }
            String safeDirection = direction.trim().toLowerCase();
            if (!"asc".equals(safeDirection) && !"desc".equals(safeDirection)) {
                continue;
            }
            safeSorts.put(column, safeDirection);
        }
        return safeSorts;
    }

}
