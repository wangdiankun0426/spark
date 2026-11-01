# spark-ui 视觉风格与输出格式（涉及 UI 样式/布局时必读）

配套规范：`.claude/skills/web-coding/SKILL.md`。

## 1. 视觉风格（简约大气）

设计基调：**纯白底、细描边淡投影、贴边铺满、无多余留白**。重装饰（淡彩背景、重阴影、大间距、位移缩放动效）一律不做。

### 1.1 底色与卡片

| 元素 | 写法 | 说明 |
|---|---|---|
| 页面/容器底 | `background-color: $bg-card`（白） | 不用 `$bg-page`(#f8f7f7) 灰底，禁用 `$color-primary-light` 淡彩底 |
| 卡片浮起 | `border: 1px solid $border-color-light; box-shadow: $shadow-card;` | 即 `element.scss` 中 `.el-card` 的原配方，直接复用不另造 |
| 悬浮交互 | `transition: $transition-normal;` + `&:hover { box-shadow: $shadow-card-hover; }` | 只加深投影，不加 `transform` 位移/缩放 |

- 白上白时阴影偏虚，**描边是必要的一刀**，不要只靠投影。
- 圆角一律 `$border-radius-md`(10px)；侧边面板**只朝内容/画布一侧**圆角，贴屏侧直角 —— 左面板 `border-radius: 0 $border-radius-md $border-radius-md 0`，右面板 `border-radius: $border-radius-md 0 0 $border-radius-md`。

### 1.2 布局与留白

- **贴边铺满**：面板顶到底、左右贴屏、内侧贴主内容区，外层容器 `padding: 0`，不留缝隙。
- **间距小**：内边距用 `$spacing-sm`(8px)，慎用 `$spacing-md`(16px)。
- **间距对称**：框内上下等距，内容既不贴头也不贴底。`el-row` 是 flex 容器、子项外边距**不折叠**，`el-form-item` 自带的 `margin-bottom: 14px` 会留在框内撑出多余留白，需要时 `:deep(.el-col .el-form-item) { margin-bottom: 0; }` 归零并改用 `padding`。
- **宽度两头钳制**：`min-width` + `max-width` 一起写，避免大屏出现大面积空白；面板宽度用百分比 + 钳制，例 `width: 11%; min-width: 136px; max-width: 152px`。
- **按钮宁窄不松**：设宽度上限并居中（`grid-template-columns: minmax(0, 120px); justify-content: center`），不撑满容器。
- **注意全局兄弟外边距**：`element.scss` 有 `.el-button+.el-button { margin-left: 8px }`，在 grid/flex 布局中会让非首项整体偏移，需显式归零。

### 1.3 配色与底纹

- 白/灰为主，主色 `$color-primary` 只作点缀（选中态、可点击强调）。上述"纯白底"是**浅色主题**的基准；深色主题下同一处改用 `@/styles/theme.scss` 的 `--app-bg-card` / `--app-bg-page` 等同名变量，页面不要写死 `#fff`。
- 主题切换按钮的图标表示「**点一下会变成什么**」：浅色时显示月亮、深色时显示太阳，tooltip / 提示文案（深色模式 / 浅色模式）必须与图标同义。`src/pages/login/login.vue` 与 `src/navigation/index.vue` 两处保持一致，改一处必须同步另一处。
- 底纹可用低密度点阵营造科技氛围：点距约 26px、点径约 1.2px、alpha 8%–18%，不干扰内容阅读。
- **`background:` 简写会重置 `background-image`**；已有底纹时改背景色必须用 `background-color:`。
- Element Plus 2.3.12 **没有 `--el-border-color-dashed`**；用未定义的 `var()` 会让整条 `border` 简写在计算期失效并退化为无边框。虚线用 `--el-border-color-darker` 等已定义变量。

### 1.4 交付前自查

- 留白是否上下/左右对称，有无贴头不贴底之类的偏移
- 大屏下是否出现大面积空白（宽度是否两头钳制）
- 该贴边的是否贴住，圆角是否只朝内容一侧
- 颜色/间距/圆角/阴影是否全走 `variables.scss` token，无硬编码
- 深色主题下是否仍可读：文字与底色对比、输入框正文字色、弹层与表格底色、图标是否发暗
- 新增视觉手法前先查 `element.scss` 是否已有配方，有就复用

## 2. 输出格式

1. 先确认改动范围，列出将修改/新增的文件路径
2. 再展示代码，每个文件用代码块并标注语言（vue / js / scss）
3. 最后给出注册步骤（路由/store 注册位置）
4. 不确定后端接口路径或字段时，明确说明"需向后端确认"，不要臆造
5. 避免过度解释，关键决策一句话即可
