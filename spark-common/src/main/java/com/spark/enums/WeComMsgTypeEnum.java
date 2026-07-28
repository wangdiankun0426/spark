package com.spark.enums;

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
 * @Date: 2020/4/15
 **/
public enum WeComMsgTypeEnum {

    RESP_MESSAGE_TYPE_TEXT("text", "返回消息类型：文本"),
    RESP_MESSAGE_TYPE_MUSIC("music","返回消息类型：音乐"),
    RESP_MESSAGE_TYPE_NEWS("news","返回消息类型：图文"),
    REQ_MESSAGE_TYPE_TEXT("text","请求消息类型：文本"),
    REQ_MESSAGE_TYPE_IMAGE("image","请求消息类型：图片"),
    REQ_MESSAGE_TYPE_LINK("link","请求消息类型：链接"),
    REQ_MESSAGE_TYPE_LOCATION("location","请求消息类型：地理位置"),
    REQ_MESSAGE_TYPE_VOICE("voice","请求消息类型：音频"),
    REQ_MESSAGE_TYPE_EVENT("event","请求消息类型：推送"),
    EVENT_TYPE_SUBSCRIBE("subscribe","事件类型：subscribe(订阅)"),
    EVENT_TYPE_UNSUBSCRIBE("unsubscribe","事件类型：subscribe(订阅)"),
    EVENT_TYPE_CLICK("CLICK","事件类型：subscribe(订阅)"),
    ;

    private String value;
    private String desc;

    WeComMsgTypeEnum(String value, String desc){
        this.value = value;
        this.desc = desc;
    }

    public String getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public static WeComMsgTypeEnum indexOf(Integer value){
        for(WeComMsgTypeEnum item:values()){
            if(item.getValue().equals(value)){
                return item;
            }
        }
        return null;
    }
}
