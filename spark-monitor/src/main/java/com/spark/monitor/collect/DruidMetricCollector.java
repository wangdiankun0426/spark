package com.spark.monitor.collect;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.stat.DruidDataSourceStatManager;
import com.alibaba.druid.stat.JdbcSqlStat;
import com.spark.common.utils.JsonUtil;
import com.spark.monitor.constant.MonitorConstant;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-23 10:00:00
 * Druid慢SQL指标采集器
 */
@Component
public class DruidMetricCollector {

    /**
     * 采集全部数据源的慢SQL快照内容
     * @return 慢SQL JSON
     */
    public String buildSqlJson() {
        List<Map<String, Object>> dataSourceList = new ArrayList<>();
        Set<DruidDataSource> dataSources = DruidDataSourceStatManager.getDruidDataSourceInstances();
        for (DruidDataSource dataSource : dataSources) {
            String name = resolveName(dataSource);
            List<Map<String, Object>> sqlList = buildSqlList(dataSource);
            Map<String, Object> dataSourceMap = new LinkedHashMap<>();
            dataSourceMap.put("name", name);
            dataSourceMap.put("sqlList", sqlList);
            dataSourceList.add(dataSourceMap);
        }
        return JsonUtil.toString(dataSourceList);
    }

    /**
     * 构造单数据源慢SQL列表
     * @param dataSource 数据源
     * @return 慢SQL列表
     */
    private List<Map<String, Object>> buildSqlList(DruidDataSource dataSource) {
        Map<String, JdbcSqlStat> sqlStatMap = dataSource.getSqlStatMap();
        Collection<JdbcSqlStat> statValues = sqlStatMap.values();
        List<JdbcSqlStat> sqlStats = new ArrayList<>(statValues);
        Comparator<JdbcSqlStat> byMaxCost = Comparator.comparingLong(JdbcSqlStat::getExecuteMillisMax).reversed();
        sqlStats.sort(byMaxCost);
        int limit = Math.min(MonitorConstant.DRUID_SQL_LIMIT, sqlStats.size());
        List<Map<String, Object>> sqlList = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            JdbcSqlStat sqlStat = sqlStats.get(i);
            Map<String, Object> sql = new LinkedHashMap<>();
            sql.put("sql", sqlStat.getSql());
            sql.put("executeCount", sqlStat.getExecuteCount());
            sql.put("totalTime", sqlStat.getExecuteMillisTotal());
            sql.put("maxTime", sqlStat.getExecuteMillisMax());
            sqlList.add(sql);
        }
        return sqlList;
    }

    /**
     * 解析数据源展示名
     * @param dataSource 数据源
     * @return 名称
     */
    private String resolveName(DruidDataSource dataSource) {
        return dataSource.getUrl();
    }
}
