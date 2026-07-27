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
