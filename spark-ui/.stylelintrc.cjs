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
    // SVG 图标文件的 fill/stroke 是矢量资源固有颜色，不参与主题体系
    {
      files: ['src/assets/icons/**'],
      rules: {
        'color-no-hex': null
      }
    }
  ],
  rules: {
    // 红线一：消费层禁止 var()，主题色统一经 variables.scss 的 $ 映射引用
    'function-disallowed-list': ['var'],
    // 红线二：禁止 hex 字面量，颜色一律走 token
    'color-no-hex': true,
    // 红线三：自定义属性右值的 SCSS 变量必须插值，裸写 $x 会被原样输出到 CSS
    'declaration-property-value-disallowed-list': {
      '/^--/': [/(?<!#\{)\$/]
    }
  }
};
