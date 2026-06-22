/**
 * 装备展示图：每件商品对应 public/images/gears/{id}-main.jpg
 * 图片来源见 scripts/gear-image-sources.json 与 download-report.json
 */

const LOCAL = (id, type) => `/images/gears/${id}-${type}.jpg`

const PLACEHOLDER = '/images/gears/placeholder.svg'
const DEFAULT_MAIN = LOCAL(1, 'main')
const DEFAULT_HOVER = LOCAL(1, 'hover')

function pickImage(dbValue, localPath) {
  const value = (dbValue || '').trim()
  // 本地路径优先；外链 CDN 在浏览器中常被防盗链拦截，统一回落到 public/images/gears/{id}-*.jpg
  if (value.startsWith('/images/gears/')) {
    return value
  }
  if (value.startsWith('http://') || value.startsWith('https://')) {
    return localPath
  }
  return value || localPath
}

/**
 * @param {object} gear
 * @returns {{ main: string, hover: string }}
 */
export function getGearImages(gear) {
  if (!gear?.id) {
    return { main: DEFAULT_MAIN, hover: DEFAULT_HOVER }
  }

  const main = pickImage(gear.mainImage, LOCAL(gear.id, 'main'))
  const hover = pickImage(gear.hoverImage, LOCAL(gear.id, 'hover'))
  return { main, hover }
}

/** 图片加载失败时的兜底图（保证始终可显示） */
export function getFallbackGearImage() {
  return PLACEHOLDER
}
