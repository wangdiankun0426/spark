// highlight.js 按需注册：core 内核 + 常用语言
// 不用完整包（约 1.8MB）是为了控制首屏 bundle 体积，core 仅约 72KB
import hljs from 'highlight.js/lib/core'
import javascript from 'highlight.js/lib/languages/javascript'
import typescript from 'highlight.js/lib/languages/typescript'
import xml from 'highlight.js/lib/languages/xml'
import css from 'highlight.js/lib/languages/css'
import json from 'highlight.js/lib/languages/json'
import markdown from 'highlight.js/lib/languages/markdown'
import python from 'highlight.js/lib/languages/python'
import java from 'highlight.js/lib/languages/java'
import sql from 'highlight.js/lib/languages/sql'
import bash from 'highlight.js/lib/languages/bash'
import shell from 'highlight.js/lib/languages/shell'
import go from 'highlight.js/lib/languages/go'
import cpp from 'highlight.js/lib/languages/cpp'
import c from 'highlight.js/lib/languages/c'
import csharp from 'highlight.js/lib/languages/csharp'
import php from 'highlight.js/lib/languages/php'
import yaml from 'highlight.js/lib/languages/yaml'
import plaintext from 'highlight.js/lib/languages/plaintext'

const LANGUAGES = [
  ['javascript', javascript],
  ['typescript', typescript],
  ['xml', xml],
  ['html', xml],
  ['css', css],
  ['json', json],
  ['markdown', markdown],
  ['python', python],
  ['java', java],
  ['sql', sql],
  ['bash', bash],
  ['shell', shell],
  ['go', go],
  ['cpp', cpp],
  ['c', c],
  ['csharp', csharp],
  ['php', php],
  ['yaml', yaml],
  ['plaintext', plaintext]
]

LANGUAGES.forEach(([name, lang]) => hljs.registerLanguage(name, lang))

export default hljs
