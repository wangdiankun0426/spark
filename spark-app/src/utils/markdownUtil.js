import { Marked } from 'marked'
import hljs from 'highlight.js/lib/core'

import javascript from 'highlight.js/lib/languages/javascript'
import typescript from 'highlight.js/lib/languages/typescript'
import xml from 'highlight.js/lib/languages/xml'
import css from 'highlight.js/lib/languages/css'
import python from 'highlight.js/lib/languages/python'
import java from 'highlight.js/lib/languages/java'
import json from 'highlight.js/lib/languages/json'
import sql from 'highlight.js/lib/languages/sql'
import bash from 'highlight.js/lib/languages/bash'
import plaintext from 'highlight.js/lib/languages/plaintext'

const languages = {
  javascript,
  js: javascript,
  typescript,
  ts: typescript,
  html: xml,
  xml,
  css,
  python,
  py: python,
  java,
  json,
  sql,
  bash,
  sh: bash,
  shell: bash,
  plaintext
}

Object.keys(languages).forEach((name) => {
  hljs.registerLanguage(name, languages[name])
})

const marked = new Marked({
  gfm: true,
  breaks: true,
  headerIds: false,
  mangle: false,
  highlight(code, lang) {
    try {
      if (lang && hljs.getLanguage(lang)) {
        return hljs.highlight(code, { language: lang }).value
      }
      return hljs.highlightAuto(code).value
    } catch (e) {
      return code
    }
  }
})

/**
 * 将 Markdown 渲染为 HTML（带代码高亮）
 * @param {string} content
 * @returns {string}
 */
export function renderMarkdown(content) {
  if (!content) return ''
  return marked.parse(String(content))
}

/**
 * up-parse(mp-html) 的标签默认样式，通过 tag-style 内联到节点上
 * 小程序端 rich-text 节点不认外部样式，只能用内联样式；
 * 配色对用户气泡（深底）与智能体气泡（白底）都要可读
 */
export const MD_TAG_STYLE = {
  p: 'margin:0 0 8px;',
  h1: 'font-size:19px;font-weight:600;margin:12px 0 8px;',
  h2: 'font-size:17px;font-weight:600;margin:12px 0 8px;',
  h3: 'font-size:16px;font-weight:600;margin:12px 0 8px;',
  h4: 'font-size:15px;font-weight:600;margin:12px 0 8px;',
  h5: 'font-size:14px;font-weight:600;margin:12px 0 8px;',
  h6: 'font-size:14px;font-weight:600;margin:12px 0 8px;',
  ul: 'padding-left:20px;margin:8px 0;',
  ol: 'padding-left:20px;margin:8px 0;',
  li: 'margin:4px 0;',
  pre: 'background-color:#f6f8fa;color:#24292e;padding:10px;border-radius:6px;margin:8px 0;font-family:Consolas,Monaco,monospace;font-size:13px;white-space:pre-wrap;word-break:break-all;',
  code: 'background-color:rgba(128,128,128,0.15);font-family:Consolas,Monaco,monospace;font-size:13px;padding:1px 4px;border-radius:3px;',
  a: 'color:inherit;text-decoration:underline;',
  blockquote: 'border-left:3px solid rgba(128,128,128,0.4);padding-left:10px;margin:8px 0;color:inherit;',
  table: 'width:100%;border-collapse:collapse;margin:8px 0;',
  th: 'border:1px solid rgba(128,128,128,0.35);padding:6px;text-align:left;font-weight:600;',
  td: 'border:1px solid rgba(128,128,128,0.35);padding:6px;text-align:left;',
  hr: 'border-top:1px solid rgba(128,128,128,0.35);margin:12px 0;',
  img: 'max-width:100%;'
}
