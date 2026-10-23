package com.spark.web.rest.controller.monitor;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.monitor.result.MonitorNodeResult;
import com.spark.monitor.service.IMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-23 10:00:00
 * 监控节点
 */
@RestController
@RequestMapping("monitor/node")
public class NodeController {

    @Autowired
    private IMonitorService monitorService;

    /**
     * 查询节点清单与在线状态
     * @return 节点清单
     */
    @GetMapping("list")
    private ResultData<List<MonitorNodeResult>> queryNodeList() {
        return monitorService.queryNodeList();
    }
}
