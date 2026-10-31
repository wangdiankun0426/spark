/**
 * 由 496x496 logo.png 生成任务栏优化版 icon.ico
 * 全部使用 BMP 光栅帧并保留 AND 掩码，覆盖 16~256 常用 DPI：
 * Windows 任务栏/Alt-Tab 对光栅帧的 DPI 匹配优于 PNG 压缩帧，可避免发虚
 */
const fs = require('fs')
const path = require('path')
const zlib = require('zlib')

const FRAMES_DIR = path.join(__dirname, '..', 'icons', 'frames')
const ICON_SOURCE = path.join(__dirname, '..', 'icons', 'icon-source.png')
const OUT = path.join(__dirname, '..', 'icons', 'icon.ico')
const SIZES = [16, 20, 24, 32, 40, 48, 64, 128, 256]

/**
 * 极简 PNG 解码器：仅支持 8-bit RGBA/RGB 且无 interlace，GDI+ 导出的 PNG 满足该条件
 * @param {Buffer} buf PNG 字节
 * @returns {{width:number,height:number,rgba:Buffer}}
 */
function decodePng(buf) {
  if (buf.toString('ascii', 1, 4) !== 'PNG') throw new Error('not a png')
  let pos = 8
  let width = 0, height = 0, bitDepth = 0, colorType = 0
  let palette = null, trns = null
  const idat = []
  while (pos < buf.length) {
    const len = buf.readUInt32BE(pos)
    const type = buf.toString('ascii', pos + 4, pos + 8)
    const data = buf.subarray(pos + 8, pos + 8 + len)
    if (type === 'IHDR') {
      width = data.readUInt32BE(0)
      height = data.readUInt32BE(4)
      bitDepth = data[8]
      colorType = data[9]
      if (data[12] !== 0) throw new Error('interlace unsupported')
    } else if (type === 'PLTE') {
      palette = data
    } else if (type === 'tRNS') {
      trns = data
    } else if (type === 'IDAT') {
      idat.push(data)
    } else if (type === 'IEND') {
      break
    }
    pos += 12 + len
  }
  const raw = zlib.inflateSync(Buffer.concat(idat))
  const channels = colorType === 6 ? 4 : colorType === 2 ? 3 : colorType === 3 ? 1 : 4
  const bpp = Math.max(1, (channels * bitDepth) / 8)
  const stride = width * channels
  const rgba = Buffer.alloc(width * height * 4)
  let rp = 0

  const paeth = (a, b, c) => {
    const p = a + b - c
    const pa = Math.abs(p - a), pb = Math.abs(p - b), pc = Math.abs(p - c)
    return pa <= pb && pa <= pc ? a : pb <= pc ? b : c
  }

  for (let y = 0; y < height; y++) {
    const filter = raw[rp++]
    const rowStart = rp
    for (let x = 0; x < stride; x++) {
      let v = raw[rp]
      const a = x >= bpp ? raw[rp - bpp] : 0
      const b = y > 0 ? raw[rp - stride] : 0
      const c = y > 0 && x >= bpp ? raw[rp - stride - bpp] : 0
      if (filter === 1) v = (v + a) & 255
      else if (filter === 2) v = (v + b) & 255
      else if (filter === 3) v = (v + ((a + b) >> 1)) & 255
      else if (filter === 4) v = (v + paeth(a, b, c)) & 255
      raw[rp] = v
      rp++
    }
    const srcBase = rowStart
    for (let x = 0; x < width; x++) {
      const si = srcBase + x * channels
      const di = (y * width + x) * 4
      let r, g, bl, al
      if (colorType === 6) {
        r = raw[si]; g = raw[si + 1]; bl = raw[si + 2]; al = raw[si + 3]
      } else if (colorType === 2) {
        r = raw[si]; g = raw[si + 1]; bl = raw[si + 2]; al = 255
      } else if (colorType === 3 && palette) {
        const idx = raw[si] * 3
        r = palette[idx]; g = palette[idx + 1]; bl = palette[idx + 2]
        al = trns && idx / 3 < trns.length ? trns[idx / 3] : 255
      } else {
        r = g = bl = raw[si]; al = 255
      }
      rgba[di] = r; rgba[di + 1] = g; rgba[di + 2] = bl; rgba[di + 3] = al
    }
  }
  return { width, height, rgba }
}

/**
 * 小尺寸 alpha 增强：细线渐变 logo 缩小后大量半透明像素会发虚，
 * 将 40~200 区间 alpha 拉伸让线条更实
 */
function enhanceAlpha(size, rgba) {
  if (size > 64) return rgba
  const out = Buffer.from(rgba)
  for (let i = 3; i < out.length; i += 4) {
    const a = out[i]
    if (a <= 40) out[i] = 0
    else if (a >= 200) out[i] = 255
    else out[i] = Math.round(((a - 40) / 160) * 255)
  }
  return out
}

/**
 * 生成单帧 ICO 数据
 * 256 帧按规范使用 PNG 压缩；其余尺寸使用 32bit BMP + 全 0 AND 掩码
 */
function buildFrame(size, rgba, png) {
  if (size >= 256 && png) {
    return png
  }
  rgba = enhanceAlpha(size, rgba)
  const header = Buffer.alloc(40)
  header.writeUInt32LE(40, 0)
  header.writeInt32LE(size, 4)
  header.writeInt32LE(size * 2, 8)
  header.writeUInt16LE(1, 12)
  header.writeUInt16LE(32, 14)
  const xor = Buffer.alloc(size * size * 4)
  const and = Buffer.alloc(Math.ceil(size / 4) * 4 * size)
  for (let y = 0; y < size; y++) {
    const srcY = size - 1 - y
    for (let x = 0; x < size; x++) {
      const si = (srcY * size + x) * 4
      const di = (y * size + x) * 4
      xor[di] = rgba[si + 2]
      xor[di + 1] = rgba[si + 1]
      xor[di + 2] = rgba[si]
      xor[di + 3] = rgba[si + 3]
    }
  }
  return Buffer.concat([header, xor, and])
}

const frames = SIZES.map(size => {
  const png = fs.readFileSync(path.join(FRAMES_DIR, size + '.png'))
  return { size, data: buildFrame(size, decodePng(png).rgba, png) }
})

const headerSize = 6
const entrySize = 16
let offset = headerSize + frames.length * entrySize
const entries = []
for (const frame of frames) {
  const dim = frame.size >= 256 ? 0 : frame.size
  const entry = Buffer.alloc(entrySize)
  entry[0] = dim
  entry[1] = dim
  entry[2] = 0
  entry[3] = 0
  entry.writeUInt16LE(1, 4)
  entry.writeUInt16LE(32, 6)
  entry.writeUInt32LE(frame.data.length, 8)
  entry.writeUInt32LE(offset, 12)
  entries.push(entry)
  offset += frame.data.length
}
const head = Buffer.alloc(headerSize)
head.writeUInt16LE(0, 0)
head.writeUInt16LE(1, 2)
head.writeUInt16LE(frames.length, 4)

const ico = Buffer.concat([head, ...entries, ...frames.map(f => f.data)])
fs.writeFileSync(OUT, ico)

// 同步为网页 favicon：WebView2 任务栏窗口图标实际取 favicon，必须与窗口图标一致
const FAVICON = path.join(__dirname, '..', '..', 'public', 'favicon.ico')
fs.writeFileSync(FAVICON, ico)

console.log(`icon.ico + favicon.ico generated: ${SIZES.join('/')} -> ${(ico.length / 1024).toFixed(1)}KB`)
