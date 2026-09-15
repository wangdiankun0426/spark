/**
 * 表单字段值解析工具
 * @author wangdiankun
 * @since 2026-09-15 10:00:00
 * 统一各字段类型的取值规则，以及附件信息的 JSON 解析
 * 取值约定与 PC 端表单设计器保持一致：
 *   value    业务值（ID、选项值、原始文本），多选为逗号串
 *   showValue 展示值（名称文本），附件字段为附件信息 JSON 数组字符串
 */

/** 空值占位文本 */
export const EMPTY_TEXT = '-'

/** 上传附件字段类型 */
export const FORM_TYPE_ATTACHMENT = 'upload-attachment'

/**
 * 判断字段是否为上传附件
 * @param type 字段类型
 * @returns {boolean}
 */
export function isAttachmentField(type) {
    return type === FORM_TYPE_ATTACHMENT
}

/**
 * 获取表单字段展示文本
 * 优先展示 showValue（已转换的名称文本）；缺失时按选项类字段从选项配置中匹配名称，最后回退原始 value
 * @param value 表单值
 * @param showValue 表单展示值
 * @param options 选项配置 [{value, label}]，用于 showValue 缺失时匹配名称
 * @param emptyText 空值占位文本
 * @returns {string} 展示文本
 */
export function getFieldValueText(value, showValue, options = [], emptyText = EMPTY_TEXT) {
    let text = isEmptyValue(showValue) ? value : showValue
    if (isEmptyValue(text) && !isEmptyValue(value)) {
        const matched = findOptionLabel(options, value)
        if (matched) {
            text = matched
        }
    }
    return isEmptyValue(text) ? emptyText : String(text)
}

/**
 * 从选项配置中匹配选项名称
 * @param options 选项配置 [{value, label}]
 * @param value 表单值
 * @returns {string} 选项名称，未匹配时为空字符串
 */
function findOptionLabel(options, value) {
    if (!Array.isArray(options)) {
        return ''
    }
    for (const option of options) {
        if (option && option.label !== undefined && option.label !== null && String(option.value) === String(value)) {
            return String(option.label)
        }
    }
    return ''
}

/**
 * 解析附件信息列表
 * 展示值约定为附件信息 JSON 数组字符串，如
 * [{"id":1,"name":"a.pdf","sizeStr":"1MB","ownerName":"张三","createdDt":"2026-08-20 15:00:00"}]
 * 兼容历史格式：展示值为逗号分隔的文件名称，表单值为逗号分隔的附件ID
 * @param value 表单值（附件ID，多选为逗号串）
 * @param showValue 表单展示值（附件信息 JSON 数组字符串）
 * @returns {Array} 附件列表 [{id, name}]，仅提取文件名称用于展示
 */
export function parseAttachmentList(value, showValue) {
    const list = []
    const raw = isEmptyValue(showValue) ? value : showValue
    if (isEmptyValue(raw)) {
        return list
    }
    const jsonList = parseAttachmentJson(raw)
    if (jsonList.length > 0) {
        return jsonList
    }
    // 历史格式：展示值为逗号分隔名称，表单值为逗号分隔附件ID
    const ids = splitComma(value)
    const names = splitComma(raw)
    const count = Math.max(ids.length, names.length)
    for (let index = 0; index < count; index++) {
        list.push({
            id: ids[index] ? Number(ids[index]) : undefined,
            name: names[index] || ('附件' + (index + 1))
        })
    }
    return list
}

/**
 * 获取附件文件名称列表
 * @param value 表单值（附件ID，多选为逗号串）
 * @param showValue 表单展示值（附件信息 JSON 数组字符串）
 * @returns {Array} 文件名称列表
 */
export function getAttachmentNames(value, showValue) {
    return parseAttachmentList(value, showValue).map(file => file.name)
}

/**
 * 解析附件信息 JSON
 * 解析失败或结构不符时返回空数组，由调用方回退到历史格式
 * @param raw 待解析内容
 * @returns {Array} 附件列表
 */
function parseAttachmentJson(raw) {
    const list = []
    // 仅处理对象或数组结构的 JSON 文本，避免普通文本被当作 JSON 解析
    if (typeof raw !== 'string' || !/^\s*[{\[]/.test(raw)) {
        return list
    }
    try {
        const parsed = JSON.parse(raw)
        collectAttachmentItems(list, Array.isArray(parsed) ? parsed : [parsed])
    } catch (e) {
        // 展示值超长被截断等 JSON 不完整场景，逐个抢救已写入完整的附件对象
        collectAttachmentItems(list, extractAttachmentObjects(raw))
    }
    return list
}

/**
 * 遍历附件信息并收集到列表
 * @param list 附件列表（结果集）
 * @param items 附件信息项
 */
function collectAttachmentItems(list, items) {
    for (const item of items) {
        if (item === undefined || item === null) {
            continue
        }
        // 兼容仅存名称字符串的写法
        if (typeof item === 'string') {
            if (item.trim()) {
                list.push({ name: item.trim() })
            }
            continue
        }
        const name = item.name || item.fileName || item.originalName
        if (isEmptyValue(name)) {
            continue
        }
        list.push({
            id: isEmptyValue(item.id) ? item.fileId : item.id,
            name: String(name)
        })
    }
}

/**
 * 从可能不完整的 JSON 文本中抢救附件对象
 * @param text JSON 文本
 * @returns {Array} 解析成功的附件对象
 */
function extractAttachmentObjects(text) {
    const objects = []
    for (const fragment of extractJsonFragments(text)) {
        try {
            objects.push(JSON.parse(fragment))
        } catch (e) {
            // 对象本身不完整时跳过
        }
    }
    return objects
}

/**
 * 扫描并提取文本中结构完整的 JSON 对象片段
 * 跳过字符串内的括号，仅截取配对完整的 { ... } 内容
 * @param text 待扫描文本
 * @returns {Array} 完整的对象片段
 */
function extractJsonFragments(text) {
    const fragments = []
    let start = -1
    let depth = 0
    let inString = false
    let escaped = false
    for (let index = 0; index < text.length; index++) {
        const char = text[index]
        if (inString) {
            if (escaped) {
                escaped = false
            } else if (char === '\\') {
                escaped = true
            } else if (char === '"') {
                inString = false
            }
            continue
        }
        if (char === '"') {
            inString = true
        } else if (char === '{') {
            if (depth === 0) {
                start = index
            }
            depth++
        } else if (char === '}') {
            if (depth > 0) {
                depth--
                if (depth === 0 && start >= 0) {
                    fragments.push(text.substring(start, index + 1))
                    start = -1
                }
            }
        }
    }
    return fragments
}

/**
 * 判断表单值是否为空
 * @param value 表单值
 * @returns {boolean}
 */
function isEmptyValue(value) {
    return value === undefined || value === null || value === ''
}

/**
 * 拆分逗号串并去除空项
 * @param value 逗号串
 * @returns {Array} 非空项列表
 */
function splitComma(value) {
    return String(isEmptyValue(value) ? '' : value)
        .split(',')
        .map(item => item.trim())
        .filter(Boolean)
}
