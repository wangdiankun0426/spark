package com.spark.config.es;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
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
        if (SessionHolder.isSysAdmin() || SessionHolder.isOrgAdmin()) {
            return null;
        }
        DataScopeEnum scope = DataScopeEnum.indexOf(SessionHolder.getCurrentDataScop());
        if (scope == DataScopeEnum.ALL_ACCESS) {
            return null;
        }
        if (scope == DataScopeEnum.ONLY_DEPART) {
            Long deptId = SessionHolder.getCurrentDeptId();
            if (deptId == null) {
                return noneQuery();
            }
            return Query.of(q -> q.term(t -> t.field("deptId").value(deptId)));
        }
        if (scope == DataScopeEnum.DEPART_AND_SUB_DEPART) {
            String deptIds = SessionHolder.getCurrentDeptIds();
            if (StringUtil.isBlank(deptIds)) {
                return noneQuery();
            }
            List<FieldValue> values = new ArrayList<>();
            for (String deptId : deptIds.split(",")) {
                String trimDeptId = deptId.trim();
                if (StringUtil.isNotBlank(trimDeptId)) {
                    values.add(FieldValue.of(Long.parseLong(trimDeptId)));
                }
            }
            if (values.isEmpty()) {
                return noneQuery();
            }
            return Query.of(q -> q.terms(t -> t.field("deptId").terms(tv -> tv.value(values))));
        }
        // 仅本人与未知权限按创建人过滤
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            return noneQuery();
        }
        return Query.of(q -> q.term(t -> t.field("createdBy").value(userId)));
    }

    /**
     * 构造永不命中的条件
     * @return matchNone 条件
     */
    private static Query noneQuery() {
        return Query.of(q -> q.matchNone(mn -> mn));
    }
}
