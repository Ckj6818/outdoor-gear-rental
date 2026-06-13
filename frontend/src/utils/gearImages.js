/**
 * 装备官方商品图映射
 * 图片来源：品牌官网 / 官方授权零售商 CDN
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
    main: LOCAL('2-main.jpg'),
    hover: LOCAL('2-hover.jpg'),
    source: 'Mystery Ranch — Terraframe 65'
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
  },
  12: {
    main: LOCAL('12-main.jpg'),
    hover: LOCAL('12-hover.jpg'),
    source: 'Campmor / Gregory — Baltoro 65'
  },
  13: {
    main: LOCAL('13-main.jpg'),
    hover: LOCAL('13-hover.jpg'),
    source: 'Campmor / Osprey — Atmos AG 65'
  },
  14: {
    main: LOCAL('14-main.jpg'),
    hover: LOCAL('14-hover.jpg'),
    source: 'Campmor / Big Agnes — Blacktail 2'
  },
  15: {
    main: LOCAL('15-main.jpg'),
    hover: LOCAL('15-hover.jpg'),
    source: 'Campmor / Kelty — Mistral 20 睡袋'
  },
  16: {
    main: LOCAL('16-main.jpg'),
    hover: LOCAL('16-hover.jpg'),
    source: 'Campmor / MSR — PocketRocket 2 炉具'
  },
  17: {
    main: LOCAL('17-main.jpg'),
    hover: LOCAL('17-hover.jpg'),
    source: 'Campmor / Black Diamond — Distance Z 登山杖'
  },
  18: {
    main: LOCAL('18-main.jpg'),
    hover: LOCAL('18-hover.jpg'),
    source: 'Campmor / Osprey — Kestrel 48'
  }
}

const CATEGORY_IMAGES = {
  重装背包: GEAR_IMAGES_BY_ID[12],
  轻量化背包: GEAR_IMAGES_BY_ID[18],
  帐篷: GEAR_IMAGES_BY_ID[14],
  徒步鞋: GEAR_IMAGES_BY_ID[8],
  睡袋: GEAR_IMAGES_BY_ID[15],
  炉具: GEAR_IMAGES_BY_ID[16],
  登山杖: GEAR_IMAGES_BY_ID[17]
}

function isLocalPath(url) {
  return url && url.startsWith('/')
}

/**
 * @param {object} gear
 * @returns {{ main: string, hover: string }}
 */
export function getGearImages(gear) {
  const byId = GEAR_IMAGES_BY_ID[gear.id]

  if (gear.mainImage && gear.hoverImage) {
    // 数据库中的远程 URL 可能被防盗链拦截，优先使用本地映射
    if (gear.mainImage.startsWith('http') && byId && isLocalPath(byId.main)) {
      return { main: byId.main, hover: byId.hover }
    }
    return { main: gear.mainImage, hover: gear.hoverImage }
  }
  if (gear.mainImage) {
    if (gear.mainImage.startsWith('http') && byId && isLocalPath(byId.main)) {
      return { main: byId.main, hover: byId.hover }
    }
    return { main: gear.mainImage, hover: gear.hoverImage || gear.mainImage }
  }

  if (byId) {
    return { main: byId.main, hover: byId.hover }
  }

  const byCategory = CATEGORY_IMAGES[gear.category]
  if (byCategory) {
    return { main: byCategory.main, hover: byCategory.hover }
  }

  return { main: '', hover: '' }
}
