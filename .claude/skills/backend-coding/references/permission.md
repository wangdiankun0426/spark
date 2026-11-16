# 后端鉴权与数据权限

配套规范：`../SKILL.md`。本文件是平台鉴权与数据权限逻辑的唯一事实来源，涉及身份判定、操作权限、数据权限、租户隔离、登录会话的编码必须先 Read 本文件。

## 1. 三身份模型

| 身份 | 判定依据 | 判定方法 | 权限特征 |
|------|----------|----------|----------|
| 系统管理员 sysAdmin | `sys_user.account_type == 2`，userId=101 | `SessionHolder.isSysAdmin()` | 通配 `*:*:*`，绕过操作权限、数据权限、租户断言一切校验 |
| 组织管理员 orgAdmin | `sys_tenant_user.role_type` 位掩码 `(roleType & 2) == 2` | `SessionHolder.isOrgAdmin()` | 数据权限豁免但限本租户；菜单=租户组织菜单∪角色授权菜单∪公共菜单 |
| 普通用户 | 非以上两者 | — | 菜单=公共菜单∪角色授权菜单（租户菜单限租户已授权范围）；数据范围=角色档位并集，空兜底仅本人 |

- orgAdmin 是 `sys_tenant_user.role_type` 位标志，**不是 `sys_role` 角色**；`RoleTypeEnum.hasRole(roleType, ORG_ADMIN)` 按位与判定。
- orgAdmin 菜单来源：租户授权组织菜单（platform3）∪ 角色授权租户菜单（platform0）∪ 公共菜单（platform2 全员固定）；数据范围 `applyAllAccess` 即 `[ALL_ACCESS]`。
- **租户级菜单授权**：`sys_tenant_menu`（tenant_id + menu_id）由站点管理员在租户管理页逐租户配置，仅覆盖 platform 0/3，公共菜单（2）全员固定不纳入租户授权，站点菜单（1）仅 sysAdmin；存量与运行时新建租户默认空授权，未配置则该租户用户登录仅公共菜单；`queryUserMenuList` 租户菜单分支与 `queryTenantOrgMenuList` 叠加租户授权交集，公共菜单不受限，sysAdmin 不受影响。
- 角色授权树＝platform0 ∩ 当前租户已授权菜单（`queryRoleMenuTreeList` 非 sysAdmin 用 `tenantMenuDao.queryMenuIdsByTenantId` 过滤，sysAdmin 豁免看全量）。
- 租户菜单树 `queryTenantMenuTreeList`（`GET /sys/menu/tenantTree`）与租户授权读写 `queryTenantMenuIds` / `saveTenantMenus`（`/sys/tenant/menuIds`、`/sys/tenant/saveMenu`）均站点域 sysAdmin 服务守卫；保存物理删＋批量插，授权变更不主动失效现有会话，用户重新登录后生效。
- 身份判定一律走 `SessionHolder.isSysAdmin()` / `isOrgAdmin()`，**禁止手写 `accountType == 2`、`roleType & 2` 等魔数判定**。

### 枚举精确名值

| 枚举 | 取值 |
|------|------|
| `AccountTypeEnum` | UNKNOWN(0) / COMMON(1,普通账号) / SITE_ADMIN(2,管理员账号)；`indexOf` 未知返回 UNKNOWN |
| `RoleTypeEnum` | UNKNOWN(0) / COMMON(1,普通用户) / ORG_ADMIN(2,组织管理员)；`hasRole(Integer, RoleTypeEnum)` 按位与 |
| `DataScopeEnum` | UNKNOWN(0) / ONLY_ONESELF(1,仅本人) / ONLY_DEPART(2,仅本部门) / DEPART_AND_SUB_DEPART(3,本部门及以下) / ALL_ACCESS(4,所有数据) / CUSTOM(5,自定义)；`indexOf` 未知返回 UNKNOWN |
| `MenuPlatformEnum` | TENANT(0,租户菜单) / SITE(1,站点菜单) / HOME(2,公共菜单) / ORG(3,组织菜单)；`indexOf` 未知返回 **null** |
| `MenuTypeEnum` | UNKNOWN(0) / MODULE(1,模块) / GROUP(2,分组) / MENU(3,菜单) / BUTTON(4,按钮)；`indexOf` 未知返回 UNKNOWN |

## 2. 会话机制

- 登录成功写 Redis：键 `ObjectCacheKey.LOGIN_SESSION`（前缀 `l_s_`）+ sessionId，TTL 3600 秒，每请求续期。
- 请求头 `Authorization` 即 sessionId，前端 token 就是 sessionId。
- `Session` 承载 userId / deptId / deptIds / roleType / tenantId / accountType / loginPlatform / permissions / dataScopeList / customDeptIds / sessionVersion。
- 会话版本 `ObjectCacheKey.SESSION_VERSION = 1`；`PermissionServiceImpl.loadPermission` 入口统一 `setSessionVersion`；`LoginInterceptor` 判 `session == null || sessionVersion == null || sessionVersion != SESSION_VERSION` 则删键并返回 601。
- `SessionHolder` 是 ThreadLocal 容器：`initLocalSession(Session)` 注入、`clearLocalSession()` 清空；拦截器 preHandle 注入、afterCompletion 清空。
- 跨线程透传用 `SessionSnapshot` + `SessionContextUtil`，异步任务、MQ 消费、线程池禁止直接 new Thread 丢会话。

### 权限物化 loadPermission

`PermissionServiceImpl.loadPermission` 按身份分支物化权限串与数据范围：

- sysAdmin：permissions = `*:*:*`，`applyAllAccess`。
- orgAdmin：`queryTenantOrgMenuList` ∪ `queryUserMenuList` → `extractPermissions` → `applyAllAccess`。
- 普通用户：`queryUserMenuList` 取菜单与权限串，`queryUserDataScopeList` 取数据档位（空兜底 `[ONLY_ONESELF]`），`queryUserCustomDeptIds` 取自定义部门。

## 3. 操作权限 @RequiresPermi

- 注解 `@RequiresPermi("模块:实体:操作")` 标在 **Controller 的 public 方法**上，权限串如 `workflow:def:create`、`dms:version:download`。
- `RequiresPermiAspect` `@Order(AspectOrder.ROLE_PERMISSION)`（值 1）`@Before` 校验，顺序：
  1. 权限串空串 fail-closed，抛 `NO_PERMISSION`。
  2. 未登录抛 `NOT_LOGIN`。
  3. sysAdmin 放行。
  4. 权限集合含 `*:*:*` 或精确命中目标串则放行。
  5. 否则 `logger.warn` 后抛 `NO_PERMISSION`。
- 权限串必须与 `sys_menu` 按钮种子（menuType=4）的 `perms` 字段**逐字一致**；新增按钮权限先建菜单种子再标注解。
- handler 必须 public，Spring AOP 代理才能织入。
- 自助端点（登录、注册、验证码、会话、协议等）**不标注**。
- 站点管理域不标 `@RequiresPermi`，靠服务层 sysAdmin 身份守卫。
- 无权限直接拦截，不设灰度或仅告警模式。

```java
@RequiresPermi("workflow:def:create")
@PostMapping("create")
public ResultData<Void> createWorkflow(@RequestBody WorkflowVO workflowVO) {
    return workflowService.createWorkflow(workflowVO);
}
```

## 4. 数据权限 @DataScope + ScopeResolver

- 注解 `@DataScope(tableAlias = "别名")` 标在 **Service 查询方法**上，方法首参必须是 `BaseQuery`，别名与 Mapper SQL 表别名一致。
- `DataScopeAspect` `@Order(AspectOrder.DATA_SCOPE)`（值 4）`@Before`：
  1. 入口无条件 `baseQuery.setDataScopeSQL("")`，防请求注入。
  2. sysAdmin 或 orgAdmin 豁免，直接 return。
  3. 未登录抛 `NOT_LOGIN`。
  4. `ScopeResolver.buildSql(tableAlias)` 构造片段回填 `baseQuery.dataScopeSQL`。
- Mapper XML 在 where 末尾用 `${dataScopeSQL}` 拼接，**是 `${}` 不是 `#{}`**。
- `ScopeResolver` 多角色档位 OR 合并：

| 档位 | 生成条件 |
|------|----------|
| ALL_ACCESS(4) | 豁免或任一档位为 4，返回空串 |
| CUSTOM(5) | `dept_id in (customDeptIds)` |
| DEPART_AND_SUB_DEPART(3) | `dept_id in (deptIds)` |
| ONLY_DEPART(2) | `dept_id = deptId` |
| ONLY_ONESELF(1) / UNKNOWN(0) | `created_by = userId` |
| 无可解析条件 | `and 1 = 0` 永不命中 |

- `ScopeResolver` 方法：`isBypass()`（sysAdmin||orgAdmin）、`effectiveScopeList()`（空兜底 `[1]`）、`hasFullAccess()`（任一 4）、`buildSql(tableAlias)`、`matches(deptId, createdBy)` 单对象判定。
- 三消费方：`DataScopeAspect`（MySQL）、`ESDataScope`（ES bool should，minimumShouldMatch=1）、`KgGraphServiceImpl.checkGraphAccess`（图谱）。
- **子表规则**：无 `tenant_id`/`dept_id` 列的子表（版本、明细等）不挂 `@DataScope`、不写 `${dataScopeSQL}`，经父对象 id 条件收口。

```java
@DataScope(tableAlias = "stu")
@Override
public ResultData<PageResult<UserResult>> pageUserList(UserQuery query) {
    return super.pageList(query);
}
```

```xml
<select id="queryUserList" resultType="com.spark.common.bean.sys.result.UserResult">
    select <include refid="Base_Column_List"/>
    from sys_user stu
    <include refid="searchCondition"/>
    ${dataScopeSQL}
    order by stu.id desc
    limit #{startRow}, #{pageSize}
</select>
```

## 5. 租户隔离断言

- `BaseService.scopeTenant(BaseQuery query)`：query 为 null 或 sysAdmin 早退，否则强制 `query.setTenantId(currentTenantId)`。用于非分页查询、详情、写操作前置；前提是 DAO `searchCondition` 含 `tenant_id` 过滤。
- `BaseService.assertTenant(Long tenantId)`：sysAdmin 豁免，对象租户与当前租户不等抛 `NO_PERMISSION`。用于单对象归属校验。
- **分页收口**：走 `super.pageList` 的分页方法对非 sysAdmin 统一强制租户，方法内**禁止再写** `setTenantId(SessionHolder.getCurrentTenantId())`。
- **create 强制归属**：非 sysAdmin 创建实体时 `entity.setTenantId(SessionHolder.getCurrentTenantId())`，防越权挂到他租户。
- **子表经父**：无 `tenant_id` 的版本/明细子表，经父对象 id + `scopeTenant` 取父再校验归属。
- 守护式私有守卫返回 boolean 集中置类尾，先例 `checkUserManage` / `checkTenantManage` / `checkDeptInTenant` / `checkVersionTenant`。

```java
// 非站点管理员强制归属当前租户
if (!SessionHolder.isSysAdmin()) {
    entity.setTenantId(SessionHolder.getCurrentTenantId());
}
```

```java
FlowTemplateQuery templateQuery = new FlowTemplateQuery();
templateQuery.setId(templateVersionVO.getTemplateId());
super.scopeTenant(templateQuery);
FlowTemplateResult templateResult = templateDao.queryTemplate(templateQuery);
if (templateResult == null) {
    result.setErrorCode(ErrorCodeEnum.FLOW_TEMPLATE_NOT_EXIST);
    return result;
}
```

## 6. 登录认证

- `LoginInterceptor`：取 `Authorization` sessionId → Redis `l_s_` 取 Session → 版本校验 → `initLocalSession` → expire 3600 → afterCompletion `clearLocalSession`。
- 停用账号拒登，抛 `USER_DISABLED`（705）。
- 密码登录走 DES 一次性密钥（`getEncryptKeyAPI` 返回 keyId + key）+ MD5。
- `forceLogout` 非 sysAdmin fail-closed 租户守卫，会话 JSON 空、会话 null、租户不等均抛 `NO_PERMISSION`。
- 微信首登代建以合成身份（201/103/ORG_ADMIN）调内部服务，finally `clearLocalSession`。

| `LoginTypeEnum` | 取值 |
|------|------|
| PASSWORD | 1 密码登录 |
| MESSAGE | 2 短信登录 |
| EMAIL | 3 邮箱登录 |
| WECOM_OAUTH | 4 企微 oauth2 登录 |
| WECHAT | 5 微信小程序免密登录 |

| `LoginPlatformEnum` | 取值 |
|------|------|
| PC | 1 PC 浏览器端 |
| MOBILE | 2 H5 移动端 |
| PC_CLIENT | 3 PC 客户端 |
| WECOM_PC | 4 企微 PC 端 |
| WECOM_MOBILE | 5 企微移动端 |
| WECHAT_MINIAPP | 6 微信小程序端 |

## 7. 会话保留与权限生效

- 后台业务管理操作和定时任务均**不主动删除已登录会话**，包括角色及角色成员变更、菜单 CRUD、租户菜单授权、部门或管理员身份调整、租户成员移除、普通用户资料编辑。
- 账号停用/删除、修改密码、租户停用/删除/到期同样保留现有会话，不再设置自动踢出例外。
- 用户退出并重新登录后重新物化身份、菜单、操作权限与数据权限；权限模型或菜单上线不强制清 Redis `LOGIN_SESSION*` 键。
- 用户主动退出登录与管理员手动踢出是独立操作，不属于后台状态变更的自动会话失效。
- `SESSION_VERSION` 仅用于显式要求全量重新登录的破坏性升级，不由业务操作自动修改。

## 8. 编码规则与禁止项

- 身份判定走 `SessionHolder.isSysAdmin()` / `isOrgAdmin()`，禁手写魔数位运算。
- 操作权限串与按钮种子逐字一致；新增写接口 `@LogOperate` 须先询问用户（见 SKILL.md 核心原则第 9 条）。
- 数据权限子表不挂 `@DataScope`；`${dataScopeSQL}` 用 `${}` 拼接。
- 租户断言前置；分页方法禁重复 `setTenantId`。
- 跨线程用 `SessionSnapshot` / `SessionContextUtil`，禁丢会话。
- 错误码：`NO_PERMISSION`(506) / `NOT_LOGIN`(601) / `USER_DISABLED`(705)。
- token、密码、密钥禁入日志；logger 消息纯英文首字母小写驼峰 `key={}`。