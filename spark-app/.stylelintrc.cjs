module.exports = {
  customSyntax: 'postcss-html',
  overrides: [
    {
      files: ['**/*.scss'],
      customSyntax: 'postcss-scss'
    },
    // 样式定义层职责就是声明 CSS 变量与字面值，hex 与 var 均放行，插值红线仍然生效
    {
      files: ['src/styles/**'],
      rules: {
        'color-no-hex': null,
        'function-disallowed-list': null
      }
    },
    // uni.scss 是 SCSS 变量入口，映射右值必须写 var()，只放行 var 红线
    {
      files: ['src/uni.scss'],
      rules: {
        'function-disallowed-list': null
      }
    },
    // highlight.js 语法高亮沿用 GitHub 亮暗色板，与主题体系无关
    {
      files: ['src/views/chat/index.vue'],
      rules: {
        'color-no-hex': null
      }
    },
    // 头像分类渐变色板按业务分类取色，不参与主题 token
    {
      files: ['src/components/UserAvatar/index.vue'],
      rules: {
        'color-no-hex': null
      }
    }
  ],
  rules: {
    // 红线一：消费层禁止 var()，主题色统一经 uni.scss 的 $app-* 映射引用
    'function-disallowed-list': ['var'],
    // 红线二：禁止 hex 字面量，颜色一律走 token
    'color-no-hex': true,
    // 红线三：自定义属性右值的 SCSS 变量必须插值，裸写 $x 会被原样输出到 CSS
    'declaration-property-value-disallowed-list': {
      '/^--/': [/(?<!#\{)\$/]
    }
  }
};
