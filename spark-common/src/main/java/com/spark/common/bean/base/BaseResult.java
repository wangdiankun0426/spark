package com.spark.common.bean.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/2/17 15:31
 * Jackson常用注解
 * @JsonProperty此注解用于属性上，作用是把该属性的名称序列化为另外一个名称，如把testPwd属性序列化为pwd，@JsonProperty(value="pwd")。
 * @JsonPropertyOrder作用在类上，被用来指明当序列化时需要对属性做排序，它有2个属性一个是alphabetic：布尔类型，表示是否采用字母拼音顺序排序，默认是为false，即不排序。如@JsonPropertyOrder(alphabetic=true)。
 * @JsonInclude是用在实体类的方法类的头上 作用是实体类的参数查询到的为null的不显示，比如说你想传一些json数据到前台，但是不想传值为null的数据，就可以使用该标签。如@JsonInclude(JsonInclude.Include.NON_NULL)
 * @JsonIgnoreProperties可以注明是想要忽略的属性列表如@JsonIgnoreProperties({"name","age","title"})，也可以注明过滤掉未知的属性如@JsonIgnoreProperties(ignoreUnknown=true),@JsonIgnore表示忽略当前属性。
 * @JsonFormat用在属性和方法上，可以方便的进行格式转换，如把Date转换为我们要的模式@JsonFormat(pattern = "yyyy-MM-dd HH-mm-ss")。
 * @JsonUnwrapped当实体类中成员属性是一个类的对象时候，忽略包装。直接显示属性。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResult implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 创建人
     */
    private Long createdBy;

    /**
     * 创建人名称
     */
    private String createdByName;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Timestamp createdDt;

    /**
     * 修改人
     */
    private Long updatedBy;

    /**
     * 修改人名称
     */
    private String updatedByName;

    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Timestamp updatedDt;

    /**
     * 删除标记位
     */
    private Integer deleteFlag;
}
