/**
 * 装备展示图：每件商品对应 public/images/gears/{id}-main.jpg
 * 图片来源见 scripts/gear-image-sources.json 与 download-report.json
 */

const LOCAL = (id, type) => `/images/gears/${id}-${type}.jpg`

const DEFAULT_MAIN = LOCAL(1, 'main')
const DEFAULT_HOVER = LOCAL(1, 'hover')

/**
 * @param {object} gear
 * @returns {{ main: string, hover: string }}
 */
export function getGearImages(gear) {
  if (!gear?.id) {
    return { main: DEFAULT_MAIN, hover: DEFAULT_HOVER }
  }

  const main = LOCAL(gear.id, 'main')
  const hover = LOCAL(gear.id, 'hover')
  return { main, hover }
}

/** 图片加载失败时的兜底图 */
export function getFallbackGearImage() {
  return DEFAULT_MAIN
}
