/**
 * 装备官方商品图映射
 * 图片来源：品牌官网 / 官方授权零售商 CDN（Osprey、Campmor、Naturehike、Salomon 等）
 * 本地文件位于 public/images/gears/
 */

const LOCAL = (name) => `/images/gears/${name}`

/** 每件装备主图 / 悬停图（按 gear_info.id） */
const GEAR_IMAGES_BY_ID = {
  1: {
    main: LOCAL('1-main.jpg'),
    hover: LOCAL('1-hover.jpg'),
    source: 'Osprey 官网 — Stratos 36L'
  },
  2: {
    main: 'https://www.mysteryranchuk.com/cdn/shop/files/Terraframe-2065-20112383_black-10_jpg.jpg?v=1723468112',
    hover: 'https://www.mysteryranchuk.com/cdn/shop/files/Terraframe-2065-20112383_black-_Profile_-1040_jpg.jpg?v=1723468112',
    source: 'Mystery Ranch 官方 — Terraframe 65'
  },
  3: {
    main: LOCAL('3-main.jpg'),
    hover: LOCAL('3-hover.jpg'),
    source: 'Osprey 官网 — Hikelite 26'
  },
  4: {
    main: LOCAL('4-main.jpg'),
    hover: LOCAL('4-hover.jpg'),
    source: 'Campmor / Osprey — Exos 58'
  },
  5: {
    main: LOCAL('5-main.png'),
    hover: LOCAL('5-hover.png'),
    source: 'Mystery Ranch 官方 — Radix 31'
  },
  6: {
    main: LOCAL('6-main-v2.png'),
    hover: LOCAL('6-hover.png'),
    source: 'Campmor / MSR — Hubba Hubba 2 人帐'
  },
  7: {
    main: LOCAL('7-main.jpg'),
    hover: LOCAL('7-hover.jpg'),
    source: 'Naturehike 官网 — Cloud Up 2'
  },
  8: {
    main: LOCAL('8-main.jpg'),
    hover: LOCAL('8-hover.jpg'),
    source: 'Campmor / Salomon — X Ultra 4 GTX'
  },
  9: {
    main: LOCAL('9-main.jpg'),
    hover: LOCAL('9-hover.jpg'),
    source: 'Campmor / Merrell — Moab 3'
  },
  10: {
    main: LOCAL('10-main.jpg'),
    hover: LOCAL('10-hover.jpg'),
    source: 'Campmor / Lowa — Renegade GTX Mid'
  },
  11: {
    main: LOCAL('11-main.jpg'),
    hover: LOCAL('11-hover.jpg'),
    source: 'HOKA 官方授权零售商 — Anacapa Low GTX'
  }
}

const CATEGORY_IMAGES = {
  重装背包: GEAR_IMAGES_BY_ID[2],
  轻量化背包: GEAR_IMAGES_BY_ID[4],
  帐篷: GEAR_IMAGES_BY_ID[6],
  徒步鞋: GEAR_IMAGES_BY_ID[8]
}

/**
 * @param {object} gear
 * @returns {{ main: string, hover: string }}
 */
export function getGearImages(gear) {
  if (gear.mainImage && gear.hoverImage) {
    return { main: gear.mainImage, hover: gear.hoverImage }
  }
  if (gear.mainImage) {
    return { main: gear.mainImage, hover: gear.hoverImage || gear.mainImage }
  }

  const byId = GEAR_IMAGES_BY_ID[gear.id]
  if (byId) {
    return { main: byId.main, hover: byId.hover }
  }

  const byCategory = CATEGORY_IMAGES[gear.category]
  if (byCategory) {
    return { main: byCategory.main, hover: byCategory.hover }
  }

  return { main: '', hover: '' }
}
