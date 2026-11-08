/**
 * 连线走向条件的可视化：把 Spring EL 表达式转成可读文案
 * 画布标签、分支泳道标签共用同一套解析逻辑，避免两处文案不一致
 */

/**
 * 在顶层按 && / || 拆分表达式（引号内的连接符不拆分）
 * @param s 表达式（不含 ${} 包裹）
 * @returns {{parts: string[], logics: string[]}} 片段列表与各片段前的连接符
 */
function splitFragments(s) {
  const parts = [];
  const logics = [];
  let cur = '';
  let inQuote = false;
  for (let i = 0; i < s.length; i++) {
    const ch = s[i];
    if (ch === "'") {
      inQuote = !inQuote;
      cur += ch;
    } else if (!inQuote && (s.startsWith('&&', i) || s.startsWith('||', i))) {
      logics.push(s.substr(i, 2));
      parts.push(cur.trim());
      cur = '';
      i++;
    } else {
      cur += ch;
    }
  }
  parts.push(cur.trim());
  return { parts, logics };
}

/** 转义正则特殊字符 */
function escapeRegExp(str) {
  return String(str).replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

/** 字段编码翻译为字段名称（无映射时返回原编码） */
function getFieldLabel(code, fieldOptions) {
  const find = fieldOptions.find(f => f.value === code);
  return find ? find.label : code;
}

/**
 * 单个条件片段转可读文案：amount > 80 -> 金额 大于 80
 * @param frag 条件片段
 * @param fieldOptions 表单字段选项（label/value）
 * @returns {string} 可读文案
 */
function formatFragment(frag, fieldOptions) {
  let s = frag;
  // 字段编码替换为字段名称（长编码优先，避免前缀误替换）
  const codes = fieldOptions.map(f => f.value).filter(Boolean).sort((a, b) => b.length - a.length);
  codes.forEach(code => {
    const label = getFieldLabel(code, fieldOptions);
    s = s.replace(new RegExp(`(?<![\\w])${escapeRegExp(code)}(?![\\w])`, 'g'), label);
  });
  return s
      .replace(/\.contains\(['"]?([^'")]+)['"]?\)/g, ' 包含 $1 ')
      .replace(/>=/g, ' 大于等于 ')
      .replace(/<=/g, ' 小于等于 ')
      .replace(/==/g, ' 等于 ')
      .replace(/!=/g, ' 不等于 ')
      .replace(/>/g, ' 大于 ')
      .replace(/</g, ' 小于 ')
      .replace(/['"]/g, '')
      .replace(/\s+/g, ' ')
      .trim();
}

/**
 * 条件表达式转多行可读文案，每个条件片段一行，连接符（且/或）在后续行行首
 * 如 ${amount > 80 && status == '1'} -> ['金额 大于 80', '且 状态 等于 1']
 * @param expression 条件表达式
 * @param fieldOptions 表单字段选项（label/value）
 * @returns {string[]} 文案行列表，无表达式返回空数组
 */
export function formatConditionLines(expression, fieldOptions = []) {
  if (!expression) {
    return [];
  }
  let s = String(expression).trim();
  if (s.startsWith('${') && s.endsWith('}')) {
    s = s.slice(2, -1);
  }
  const { parts, logics } = splitFragments(s);
  const lines = [];
  for (let i = 0; i < parts.length; i++) {
    if (!parts[i]) {
      continue;
    }
    const connector = i === 0 ? '' : (logics[i - 1] === '||' ? '或 ' : '且 ');
    lines.push(connector + formatFragment(parts[i], fieldOptions));
  }
  return lines;
}
