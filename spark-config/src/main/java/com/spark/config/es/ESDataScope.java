package com.spark.config.es;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.spark.common.bean.base.ScopeResolver;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.enums.DataScopeEnum;
import com.spark.common.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-29 10:00:00
 * ES 数据权限过滤条件构造，语义与 DataScopeAspect 保持一致
 */
public final class ESDataScope {

    private ESDataScope() {
    }

    /**
     * 构造当前会话的 ES 过滤条件，恒含租户隔离，并按数据权限追加部门或创建人条件
     * @return 过滤条件；租户或身份缺失返回永不命中条件
     */
    public static Query scopeQuery() {
        Long tenantId = SessionHolder.getCurrentTenantId();
        if (tenantId == null) {
            return noneQuery();
        }
        Query dataScopeQuery = dataScopeQuery();
        return Query.of(q -> q.bool(b -> {
            b.must(m -> m.term(t -> t.field("tenantId").value(tenantId)));
            if (dataScopeQuery != null) {
                b.must(dataScopeQuery);
            }
            return b;
        }));
    }

    /**
     * 构造当前会话的数据权限条件，语义与 DataScopeAspect 保持一致
     * @return 权限条件；管理员或全部数据权限返回 null；身份缺失返回永不命中条件
     */
    private static Query dataScopeQuery() {
        if (ScopeResolver.isBypass() || ScopeResolver.hasFullAccess()) {
            return null;
        }
        Long userId = SessionHolder.getCurrentUserId();
        Long deptId = SessionHolder.getCurrentDeptId();
        String deptIds = SessionHolder.getCurrentDeptIds();
        String customDeptIds = SessionHolder.getCurrentCustomDeptIds();
        List<Query> shouldList = new ArrayList<>();
        List<Integer> scopeList = ScopeResolver.effectiveScopeList();
        for (Integer scope : scopeList) {
            DataScopeEnum scopeEnum = DataScopeEnum.indexOf(scope);
            switch (scopeEnum) {
                case CUSTOM:
                    addDeptTerms(shouldList, customDeptIds);
                    break;
                case DEPART_AND_SUB_DEPART:
                    addDeptTerms(shouldList, deptIds);
                    break;
                case ONLY_DEPART:
                    if (deptId != null) {
                        Query deptQuery = Query.of(q -> q.term(t -> t.field("deptId").value(deptId)));
                        shouldList.add(deptQuery);
                    }
                    break;
                default:
                    if (userId != null) {
                        Query userQuery = Query.of(q -> q.term(t -> t.field("createdBy").value(userId)));
                        shouldList.add(userQuery);
                    }
                    break;
            }
        }
        if (shouldList.isEmpty()) {
            return noneQuery();
        }
        return Query.of(q -> q.bool(b -> b.should(shouldList).minimumShouldMatch("1")));
    }

    /**
     * 按逗号分隔部门ids追加部门terms条件
     * @param shouldList 条件集合
     * @param deptIdsCsv 逗号分隔部门ids
     */
    private static void addDeptTerms(List<Query> shouldList, String deptIdsCsv) {
        if (StringUtil.isBlank(deptIdsCsv)) {
            return;
        }
        List<FieldValue> values = new ArrayList<>();
        for (String item : deptIdsCsv.split(",")) {
            String trimItem = item.trim();
            if (StringUtil.isNotBlank(trimItem)) {
                Long deptIdValue = Long.parseLong(trimItem);
                FieldValue fieldValue = FieldValue.of(deptIdValue);
                values.add(fieldValue);
            }
        }
        if (values.isEmpty()) {
            return;
        }
        Query termsQuery = Query.of(q -> q.terms(t -> t.field("deptId").terms(tv -> tv.value(values))));
        shouldList.add(termsQuery);
    }

    /**
     * 构造永不命中的条件
     * @return matchNone 条件
     */
    private static Query noneQuery() {
        return Query.of(q -> q.matchNone(mn -> mn));
    }
}
