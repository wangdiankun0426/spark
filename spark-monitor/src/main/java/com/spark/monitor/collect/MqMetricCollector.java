package com.spark.monitor.collect;

import com.spark.common.bean.monitor.entity.MonitorMqSnapshot;
import com.spark.monitor.constant.MonitorConstant;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-23 10:00:00
 * 消息队列指标采集器
 */
@Component
public class MqMetricCollector {

    @Autowired
    private AmqpAdmin amqpAdmin;

    /**
     * 采集全部业务队列快照
     * @param nodeKey 实际采集节点标识
     * @return 队列快照列表
     */
    public List<MonitorMqSnapshot> buildSnapshots(String nodeKey) {
        List<MonitorMqSnapshot> snapshotList = new ArrayList<>();
        Timestamp snapshotDt = new Timestamp(System.currentTimeMillis());
        for (Map.Entry<String, String> entry : MonitorConstant.QUEUE_LABEL_MAP.entrySet()) {
            String queueName = entry.getKey();
            Properties properties = amqpAdmin.getQueueProperties(queueName);
            if (properties == null) {
                continue;
            }
            MonitorMqSnapshot snapshot = new MonitorMqSnapshot();
            snapshot.setNodeKey(nodeKey);
            snapshot.setSnapshotDt(snapshotDt);
            long messageCount = readLong(properties, RabbitAdmin.QUEUE_MESSAGE_COUNT);
            int consumerCount = (int) readLong(properties, RabbitAdmin.QUEUE_CONSUMER_COUNT);
            snapshot.setQueueName(queueName);
            snapshot.setQueueLabel(entry.getValue());
            snapshot.setMessageCount(messageCount);
            snapshot.setConsumerCount(consumerCount);
            snapshotList.add(snapshot);
        }
        return snapshotList;
    }

    /**
     * 读取队列属性长整值
     * @param properties 属性集
     * @param key 键（RabbitAdmin队列常量声明为Object）
     * @return 值
     */
    private long readLong(Properties properties, Object key) {
        Object value = properties.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }
}
