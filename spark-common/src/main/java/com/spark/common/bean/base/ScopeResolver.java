package com.spark.common.bean.base;

import com.spark.common.enums.DataScopeEnum;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.StringUtil;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-13 10:00:00
 * 数据范围解析器，按会话多角色档位 OR 合并生成 SQL 片段与单对象判定
 */
public final class ScopeResolver {

    private ScopeResolver() {
    }

    /**
     * 判断当前用户是否豁免数据权限
     * @return 系统管理员或组织管理员返回true
     */
    public static boolean isBypass() {
        return SessionHolder.isSysAdmin() || SessionHolder.isOrgAdmin();
    }

    /**
     * 获取生效的数据范围档位集合，空集合兜底仅本人
     * @return 数据范围档位集合
     */
    public static List<Integer> effectiveScopeList() {
        List<Integer> scopeList = SessionHolder.getCurrentDataScopeList();
        if (CollectionUtil.isEmpty(scopeList)) {
            Integer onlyOneself = DataScopeEnum.ONLY_ONESELF.getValue();
            List<Integer> defaultList = new ArrayList<>();
            defaultList.add(onlyOneself);
            return defaultList;
        }
        return scopeList;
    }

    /**
     * 判断当前用户是否拥有全部数据权限
     * @return 任一角色档位为全部数据返回true
     */
    public static boolean hasFullAccess() {
        Integer allAccess = DataScopeEnum.ALL_ACCESS.getValue();
        List<Integer> scopeList = effectiveScopeList();
        for (Integer scope : scopeList) {
            if (Objects.equals(scope, allAccess)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 构造数据权限 SQL 片段，多角色档位 OR 合并
     * @param tableAlias 表别名
     * @return and 包裹的过滤片段，豁免或全部数据返回空串，无可解析条件返回永不命中片段
     */
    public static String buildSql(String tableAlias) {
        if (isBypass() || hasFullAccess()) {
            return "";
        }
        Long userId = SessionHolder.getCurrentUserId();
        Long deptId = SessionHolder.getCurrentDeptId();
        String deptIds = SessionHolder.getCurrentDeptIds();
        String customDeptIds = SessionHolder.getCurrentCustomDeptIds();
        String prefix = "";
        if (StringUtil.isNotBlank(tableAlias)) {
            prefix = tableAlias + ".";
        }
        Set<String> conditionSet = new LinkedHashSet<>();
        List<Integer> scopeList = effectiveScopeList();
        for (Integer scope : scopeList) {
            DataScopeEnum scopeEnum = DataScopeEnum.indexOf(scope);
            switch (scopeEnum) {
                case CUSTOM:
                    if (StringUtil.isNotBlank(customDeptIds)) {
                        conditionSet.add(prefix + "dept_id in (" + customDeptIds + ")");
                    }
                    break;
                case DEPART_AND_SUB_DEPART:
                    if (StringUtil.isNotBlank(deptIds)) {
                        conditionSet.add(prefix + "dept_id in (" + deptIds + ")");
                    }
                    break;
                case ONLY_DEPART:
                    if (deptId != null) {
                        conditionSet.add(prefix + "dept_id = " + deptId);
                    }
                    break;
                default:
                    if (userId != null) {
                        conditionSet.add(prefix + "created_by = " + userId);
                    }
                    break;
            }
        }
        if (conditionSet.isEmpty()) {
            return "and 1 = 0";
        }
        List<String> conditionList = new ArrayList<>(conditionSet);
        String conditionSql = StringUtil.join(conditionList, " or ");
        return "and (" + conditionSql + ")";
    }

    /**
     * 判断单对象是否落在当前数据范围内，多角色档位 OR 合并
     * @param deptId 对象部门id
     * @param createdBy 对象创建人id
     * @return 可访问返回true
     */
    public static boolean matches(Long deptId, Long createdBy) {
        if (isBypass() || hasFullAccess()) {
            return true;
        }
        Long userId = SessionHolder.getCurrentUserId();
        Long currentDeptId = SessionHolder.getCurrentDeptId();
        String deptIds = SessionHolder.getCurrentDeptIds();
        String customDeptIds = SessionHolder.getCurrentCustomDeptIds();
        List<Integer> scopeList = effectiveScopeList();
        for (Integer scope : scopeList) {
            DataScopeEnum scopeEnum = DataScopeEnum.indexOf(scope);
            switch (scopeEnum) {
                case CUSTOM:
                    if (containsDeptId(customDeptIds, deptId)) {
                        return true;
                    }
                    break;
                case DEPART_AND_SUB_DEPART:
                    if (containsDeptId(deptIds, deptId)) {
                        return true;
                    }
                    break;
                case ONLY_DEPART:
                    if (deptId != null && Objects.equals(deptId, currentDeptId)) {
                        return true;
                    }
                    break;
                default:
                    if (createdBy != null && Objects.equals(createdBy, userId)) {
                        return true;
                    }
                    break;
            }
        }
        return false;
    }

    /**
     * 判断部门id是否在逗号分隔的部门ids中
     * @param deptIdsCsv 逗号分隔部门ids
     * @param deptId 部门id
     * @return 命中返回true
     */
    private static boolean containsDeptId(String deptIdsCsv, Long deptId) {
        if (deptId == null || StringUtil.isBlank(deptIdsCsv)) {
            return false;
        }
        String targetDeptId = String.valueOf(deptId);
        for (String item : deptIdsCsv.split(",")) {
            String trimItem = item.trim();
            if (targetDeptId.equals(trimItem)) {
                return true;
            }
        }
        return false;
    }
}