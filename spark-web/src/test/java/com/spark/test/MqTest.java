package com.spark.test;

import com.spark.common.bean.sys.vo.MessageVO;
import com.spark.config.rabbitmq.MqProducer;
import com.spark.common.utils.JsonUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/6/3 13:47
 */
@SpringBootTest
public class MqTest {

    @Autowired
    private MqProducer mqProducer;

    @Test
    public void sendMsg() {
        for (int i = 0; i < 10 ; i++) {
            MessageVO messageVO = new MessageVO();
            messageVO.setTenantId(103L);
            messageVO.setType(i);
            String msg = JsonUtil.toString(messageVO);
            mqProducer.sendSystemMessageMq(msg);
        }
    }
}
