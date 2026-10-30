import request from '@/api/request'

/**
 * 查询监控节点清单
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryNodeListAPI() {
  return request({
    url: '/monitor/node/list',
    method: 'get'
  })
}

/**
 * 查询节点最新主机快照
 * @param query 查询参数 nodeKey
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryLatestHostAPI(query) {
  return request({
    url: '/monitor/server/latest',
    method: 'get',
    params: query
  })
}

/**
 * 查询主机快照趋势
 * @param query 查询参数 nodeKey、startTime、endTime
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryHostSeriesAPI(query) {
  return request({
    url: '/monitor/server/series',
    method: 'get',
    params: query
  })
}

/**
 * 查询节点最新慢SQL快照
 * @param query 查询参数 nodeKey
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryLatestDruidAPI(query) {
  return request({
    url: '/monitor/server/druid',
    method: 'get',
    params: query
  })
}

/**
 * 查询最新Redis快照
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryLatestRedisAPI() {
  return request({
    url: '/monitor/redis/latest',
    method: 'get'
  })
}

/**
 * 查询Redis快照趋势
 * @param query 查询参数 startTime、endTime
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryRedisSeriesAPI(query) {
  return request({
    url: '/monitor/redis/series',
    method: 'get',
    params: query
  })
}

/**
 * 查询各队列最新快照
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryLatestMqAPI() {
  return request({
    url: '/monitor/mq/latest',
    method: 'get'
  })
}

/**
 * 查询队列积压趋势
 * @param query 查询参数 startTime、endTime
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryMqSeriesAPI(query) {
  return request({
    url: '/monitor/mq/series',
    method: 'get',
    params: query
  })
}

/**
 * 查询接口请求汇总
 * @param query 查询参数 nodeKey、startTime、endTime
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryApiSummaryAPI(query) {
  return request({
    url: '/monitor/api/summary',
    method: 'get',
    params: query
  })
}

/**
 * 查询接口请求分钟趋势
 * @param query 查询参数 nodeKey、startTime、endTime
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryApiTrendAPI(query) {
  return request({
    url: '/monitor/api/trend',
    method: 'get',
    params: query
  })
}

/**
 * 分页查询URI维度统计（全量聚合，按调用次数倒排）
 * @param query 查询参数 nodeKey、pageNo、pageSize
 * @returns {Promise<AxiosResponse<any>>}
 */
export function queryUriStatsAPI(query) {
  return request({
    url: '/monitor/api/uriStats',
    method: 'get',
    params: query
  })
}
