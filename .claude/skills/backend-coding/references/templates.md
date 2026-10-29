# Spark 后端代码模板（生成新文件前必读）

配套规范：`.claude/skills/backend-coding/SKILL.md`（核心原则/红线/文件位置）。

## 1. 导入顺序

1. `com.spark.*`
2. 第三方库（`org.slf4j` / `org.springframework` / `org.apache.commons` / `dev.langchain4j` 等）
3. `java.*`

每组之间空一行；**禁止通配符导入**（`import xxx.*`）。

## 2. Controller 模板

```java
@RestController
@RequestMapping("module/entity")
public class EntityController {
    @Autowired
    private IEntityService entityService;

    /**
     * 创建实体
     * @param entityVO 实体参数
     * @return 创建结果
     */
    @PostMapping("create")
    public ResultData<Void> createEntity(EntityVO entityVO) {
        return entityService.createEntity(entityVO);
    }
}
```

- 路径小写：`create` / `update` / `delete` / `pageList` / `detail` / `tree`
- 写操作用 `@PostMapping`，查询用 `@GetMapping`
- **方法修饰符用 `public`**（Controller 方法须声明为 public）
- Controller 不含业务逻辑，直接委托：`return xxxService.xxx(vo);`
- Controller 不抛异常
- 大文本入参（技能全文、表单 JSON 等）加 `@RequestBody`，前端对应改 `data` 传参，避免 params 拼 URL 超 8KB 触发 431（先例 `FormController#saveFormJson`、`SkillController`）

## 3. Service 实现模板

```java
@Service
public class EntityServiceImpl extends BaseService<EntityQuery, EntityResult> implements IEntityService {
    private final static Logger logger = LoggerFactory.getLogger(EntityServiceImpl.class);
    @Autowired
    private EntityDao entityDao;
    // @Autowired 字段间不空行

    @Override
    @LogOperate(operateType = OperateTypeEnum.XXX_INSERT)
    public ResultData<Void> createEntity(EntityVO entityVO) {
        ResultData<Void> result = new ResultData<>();
        // 参数校验：通常抽成私有 validateXxx 方法
        if (entityVO == null || StringUtil.isBlank(entityVO.getName())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Entity entity = new Entity();
        BeanUtil.copyProperties(entityVO, entity);
        entity.setId(super.genObjectId(ObjectTypeEnum.XXX));
        int count = entityDao.insertDB(entity);
        if (count < 1) {
           result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        result.setObjId(entity.getId());
        result.setCode(ResultData.OK);
        return result;
    }
}
```

- 继承 `BaseService<Q, R>`，实现 `IXxxService`
- Logger：`private final static Logger logger = LoggerFactory.getLogger(XxxServiceImpl.class);`
- `@Autowired` 字段注入，字段间不空行
- **每个公有方法必须有 Javadoc**（中文）
- 返回 `ResultData<T>`；成功 `result.setCode(ResultData.OK)`，可带 `result.setObjId(...)`
- 参数校验抽成私有 `validateXxx(EntityVO vo)` 方法返回 `ResultData`
- 列表补充数据抽成 `supplyXxxList(List<XxxResult> list)` 私有方法（用 `super.supplyCreatedByName(list)` / `supplyUpdatedByName(list)` 补创建人/修改人名）
- 方法顺序按 `public → protected → private`，所有 private 方法集中放在类尾部全部 public 方法之后，禁止插在两个 public 方法之间
- `@Transactional` 仅在真正需要时（多表写操作）添加，默认不加
- 上面模板中的 `@LogOperate` 与 `entity.setId(super.genObjectId(ObjectTypeEnum.XXX))` 均为**条件代码**：生成前必须先询问用户是否需要，得到肯定回答后再生成，默认不生成（见 SKILL.md 核心原则第 8 条）
- **守护式校验必须在方法体第一行开始**：先校验入参，再写业务逻辑；校验不通过立即 `return`，禁止"先执行后判断"或依赖 try-catch 拦截非法参数（详则见 `guard-clauses.md`）

## 4. 分页模板（强制）

```java
@Override
@DataScope
public ResultData<PageResult<EntityResult>> pageEntityList(EntityQuery query) {
    ResultData<PageResult<EntityResult>> result = new ResultData<>();
    if (query == null) {
        query = new EntityQuery();
    }
    PageResult<EntityResult> pageResult = super.pageList(query);
    result.setData(pageResult);
    result.setCode(ResultData.OK);
    return result;
}

@Override
protected int queryCount(EntityQuery query) { return entityDao.queryEntityCount(query); }

@Override
protected List<EntityResult> queryList(EntityQuery query) { return entityDao.queryEntityList(query); }

@Override
protected Long queryMaxId() { return entityDao.queryEntityMaxId(); }
```

- 分页方法加 `@DataScope`（数据权限）
- 实现 `queryCount()` / `queryList()`，调用 `super.pageList(query)`
- 租户隔离由 `BaseService.pageList` 统一强制：非系统管理员一律按当前会话租户覆盖 query.tenantId。**分页方法内禁止再写 `query.setTenantId(SessionHolder.getCurrentTenantId())`**（冗余，BaseService 已收口）；系统管理员不覆盖，平台管理类跨租户查询（如租户列表）不传租户即可。非分页查询（不走 `super.pageList`，如直查 DAO 的列表/树、详情、下载、写操作）不享受此兜底，仍需按需自行 setTenantId
- `queryMaxId()` 仅为实体对象级别的 ID 生成（`genObjectId`）服务，与分页无关；按核心原则判断是否需要实现
- 可在 `queryList` 中先 `supplyXxxList` 再返回

## 5. DAO 接口模板

```java
public interface EntityDao extends BaseDao<Entity> {
    int queryEntityCount(EntityQuery query);
    List<EntityResult> queryEntityList(EntityQuery query);
    EntityResult queryEntity(EntityQuery query);
    Long queryEntityMaxId();
}
```

业务层一律调用 `insertDB` / `updateDBById` / `deleteDBById`（BaseDao 默认方法），**禁止直接调用 Mapper 的 `insert` / `updateById` / `deleteById`**。Mapper XML 中的 `insert` / `updateById` / `deleteById` 为默认方法底层语句，`deleteById` 是逻辑删除（`delete_flag = -1`）。

## 6. Mapper XML 要点

- `insert`：`delete_flag` 硬编码 `1`；`created_dt` / `updated_dt` 可用 `now()` 或 `#{createdDt}`/`#{updatedDt}`（后者由 `BaseDao.insertDB` 填充）；实体对象级别的表手动插入 `id` 列（`genObjectId` 构造，须用户确认），普通记录表用 `useGeneratedKeys="true" keyProperty="id"` 自增
- `updateById`：先写 `set updated_dt = #{updatedDt}, updated_by = #{updatedBy}`，再用 `<if>` 动态追加可更新字段，`where id = #{id}`
- 逻辑删除：`update table set delete_flag = -1, updated_by = #{updatedBy}, updated_dt = #{updatedDt} where id = #{id}`
- 查询条件统一放 `<sql id="condition">`（或 `searchCondition`），用 `<if>` 判断；`deleteFlag` 只在 `id`/`ids` 为空时生效
- 分页列表：`order by ...` 后加 `<if test="page == true">limit #{startRow}, #{pageSize}</if>`
- 数据权限：查询语句末尾追加 `${dataScopeSQL}`（配合 `@DataScope`）
- **版本/事件/明细等从属于主对象的子表（强制）**：不建 tenant_id/dept_id 列、Service 分页不挂 `@DataScope`、XML 不写 `${dataScopeSQL}`；权限与租户隔离统一在**主对象表**（如 dms_document、表单模板、流程模板）上处理，子表查询必须带父对象 id 条件（如 docId），父对象可见性即子表可见性。典型：dms_document_version、flow_template_version、dms_document_event
- 多参数 DAO 方法（如 `deleteByXxxId`）用 `@Param` 绑定，批量逻辑删除用 `updated_dt = now()`
- `resultType` 用 `com.spark.bean.{module}.result.XxxResult` 全限定名

### Mapper XML 模板（普通记录表，数据库自增）

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="com.spark.dao.{module}.XxxDao" >
    <sql id="allColumnList" >
        xxx_field , delete_flag , created_by , created_dt , updated_by , updated_dt
    </sql>

    <!-- 查询条件：deleteFlag 仅在 id/ids 为空时生效；配合 @DataScope 时末尾追加 ${dataScopeSQL} -->
    <sql id="searchCondition">
        <if test="id != null">
            and id = #{id}
        </if>
        <if test="ids != null and ids.size() > 0">
            and id in(
            <foreach collection="ids" item="id" separator=",">
                #{id}
            </foreach>
            )
        </if>
        <if test="deleteFlag != null and id == null and ids == null">
            and delete_flag = #{deleteFlag}
        </if>
    </sql>

    <!-- 普通记录表：数据库自增，用 useGeneratedKeys，不插入 id 列 -->
    <insert id="insert" useGeneratedKeys="true" keyProperty="id">
        insert into xxx_table (xxx_field , delete_flag , created_by , created_dt , updated_by , updated_dt)
        values ( #{xxxField} , 1, #{createdBy}, #{createdDt}, #{updatedBy}, #{updatedDt})
    </insert>

    <update id="updateById">
        update xxx_table set updated_dt = #{updatedDt}, updated_by = #{updatedBy}
        <if test="xxxField != null">
            ,xxx_field = #{xxxField}
        </if>
        where id = #{id}
    </update>

    <update id="deleteById">
        update xxx_table set delete_flag = -1 , updated_by = #{updatedBy} , updated_dt = #{updatedDt}
        where id = #{id}
    </update>

    <select id="queryXxxCount" resultType="java.lang.Integer">
        select count(1)
        from xxx_table
        where 1 = 1
        <include refid="searchCondition" />
    </select>

    <select id="queryXxxList" resultType="com.spark.bean.{module}.result.XxxResult">
        select <include refid="allColumnList" />
        from xxx_table
        where 1=1
        <include refid="searchCondition" />
        order by id desc
        <if test="page == true">
            limit #{startRow}, #{pageSize}
        </if>
    </select>

    <select id="queryXxx" resultType="com.spark.bean.{module}.result.XxxResult">
        select <include refid="allColumnList" />
        from xxx_table
        where 1=1
        <include refid="searchCondition" />
        limit 1
    </select>
</mapper>
```

**实体对象级别表（须用户确认手动 ID 后使用）**：`insert` 手动插入 `id` 列、不加 `useGeneratedKeys`，列清单含 `id`，并配套实现 `queryXxxMaxId`：

```xml
    <insert id="insert">
        insert into xxx_table (id , xxx_field , delete_flag , created_by , created_dt , updated_by , updated_dt)
        values (#{id}, #{xxxField}, 1, #{createdBy}, #{createdDt}, #{updatedBy}, #{updatedDt})
    </insert>

    <select id="queryXxxMaxId" resultType="java.lang.Long">
        select max(id) from xxx_table
    </select>
```

**多参数批量方法（如按父 id 级联逻辑删除）**：DAO 接口用 `@Param` 绑定，`updated_dt` 用 `now()`：

```java
int deleteByXxxId(@Param("xxxId") Long xxxId, @Param("updatedBy") Long userId);
```

```xml
    <update id="deleteByXxxId">
        update xxx_table set delete_flag = -1 , updated_by = #{updatedBy} , updated_dt = now()
        where xxx_id = #{xxxId}
    </update>
```

## 7. 通用约定

- **错误码**：用 `ErrorCodeEnum.XXX`（如 `INVALID_PARAM`、`DEPT_NOT_EXIST`），不硬编码数字
- **状态枚举**：用 `StatusEnum`（NORMAL 等）
- **对象类型**：`ObjectTypeEnum` 配合 `genObjectId`；新增 `ObjectTypeEnum` 枚举与手动 ID 生成须先询问用户是否需要，默认不生成
- **操作日志枚举**：`OperateTypeEnum.XXX_INSERT / XXX_UPDATE / XXX_DELETE`；写方法加 `@LogOperate` 须先询问用户是否需要，确认生成后成功时 `result.setObjId(...)` 设置操作对象 id（否则操作日志保存不到操作对象）
- **树结构**：`TreeUtil<T>` 转换 List → Tree（`convertTreeList`）
- **日志**：SLF4J，消息必须纯英文、首字母小写驼峰（标识符/方法名/key 保持 camelCase，单词缩写全小写）、输出值用 `key={}` 占位；`logger.error("create agent error, agentId={}, errMsg={}", agentId, e.getMessage(), e)`
- **Redis**：`RedisService`（`incr/setObj` 等），如部门编码生成
- **当前用户**：`SessionHolder.getCurrentUserId()`
