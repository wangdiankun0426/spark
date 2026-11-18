package com.spark.dao.llm;

import com.spark.common.bean.llm.dto.ProviderKeyCredential;
import com.spark.common.bean.llm.entity.ProviderKey;
import com.spark.common.bean.llm.query.ProviderKeyQuery;
import com.spark.common.bean.llm.result.ProviderKeyResult;
import com.spark.dao.BaseDao;
import org.apache.ibatis.annotations.Param;

import java.sql.Timestamp;
import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-18 15:00:00
 * 模型厂商密钥数据访问接口
 */
public interface ProviderKeyDao extends BaseDao<ProviderKey> {

    /**
     * 插入数据
     * @param providerKey 数据对象
     * @return 影响行数
     */
    @Override
    int insert(ProviderKey providerKey);

    /**
     * 删除数据
     * @param providerKey 数据对象
     * @return 影响行数
     */
    @Override
    int deleteById(ProviderKey providerKey);

    /**
     * 修改数据
     * @param providerKey 数据对象
     * @return 影响行数
     */
    @Override
    int updateById(ProviderKey providerKey);

    /**
     * 查询数量
     * @param query 查询条件
     * @return 数量
     */
    int queryProviderKeyCount(ProviderKeyQuery query);

    /**
     * 查询列表
     * @param query 查询条件
     * @return 列表
     */
    List<ProviderKeyResult> queryProviderKeyList(ProviderKeyQuery query);

    /**
     * 查询单条
     * @param query 查询条件
     * @return 单条数据
     */
    ProviderKeyResult queryProviderKey(ProviderKeyQuery query);

    /**
     * 查询厂商当前可用密钥，按id正序返回
     * @param providerId 厂商id
     * @return 可用密钥凭据
     */
    List<ProviderKeyCredential> queryAvailableProviderKeyList(@Param("providerId") Long providerId);

    /**
     * 查询数据库当前时间
     * @return 当前时间
     */
    Timestamp queryCurrentTime();

}
