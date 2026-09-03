package com.spark.config.wecom.response;

import lombok.Data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * + +┌─┐       ┌─┐ + +
 * ┌──┘ ┴───────┘ ┴──┐++
 * │                 │
 * │       ───       │++ + + +
 * ███████───███████ │+
 * │                 │+
 * │       ─┴─       │
 * │                 │
 * └───┐         ┌───┘
 * ----│         │
 * ----│         │   + +
 * ----│         │
 * ----│         └──────────────┐
 * ----│                        │
 * ----│                        ├─┐
 * ----│                        ┌─┘
 * ----│                        │
 * ----└─┐  ┐  ┌───────┬──┐  ┌──┘  + + + +
 * ------│ ─┤ ─┤       │ ─┤ ─┤
 * ------└──┴──┘       └──┴──┘  + + + +
 * 神兽保佑
 * 代码无BUG!
 *
 * @Author: fangzhengpeng
 * @Date: 2021/1/9
 **/
@Data
@XmlRootElement(name = "xml")
public class WeComMsg {

    /**
     * 用户名
     */
    @XmlElement(name = "ToUserName")
    private String ToUserName;

    /**
     * 企业用户id
     */
    @XmlElement(name = "FromUserName")
    private String FromUserName;

    /**
     * 消息类型
     */
    @XmlElement(name = "MsgType")
    private String MsgType;

    /**
     * 事件类型
     */
    @XmlElement(name = "Event")
    private String Event;

    /**
     * 操作时间
     */
    @XmlElement(name = "CreateTime")
    private Long CreateTime;

    /**
     * event Key
     */
    @XmlElement(name = "EventKey")
    private String EventKey;

    /**
     * 任务id
     */
    @XmlElement(name = "TaskId")
    private String TaskId;

    /**
     * AgentId
     */
    @XmlElement(name = "AgentId")
    private String AgentId;

    /**
     * FuncFlag
     */
    @XmlElement(name = "FuncFlag")
    private Integer FuncFlag;

    /**
     * Content
     */
    @XmlElement(name = "Content")
    private String Content;
}
