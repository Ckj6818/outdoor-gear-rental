<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getGearPage } from '@/api/gear'
import { getFallbackGearImage, getGearImages } from '@/utils/gearImages'

const router = useRouter()

const drawerVisible = ref(false)
const currentArticle = ref(null)
const gearInfoMap = ref({})
const imageErrorMap = ref({})

const routeArticles = [
  {
    id: 1,
    gearId: 1,
    title: '香山 · 鬼见愁环线',
    tag: 'LOCAL · MODERATE',
    summary: '8.2 km 经典当日往返 · 爬升约 520 m · 适合轻装日徒步',
    routeLabel: '当日往返',
    distance: '8.2 km',
    elevation: '+520 m',
    difficulty: 'Moderate',
    fullText: `香山鬼见愁环线是北京户外入门经典，全程约 8.2 公里，爬升约 520 米。台阶与土路交替，春秋两季体验最佳；十月红叶季人流大，建议工作日或清晨出发。

推荐租赁 Osprey Stratos 36L（装备大厅 1 号）：36L 容量足够装 2L 水、路餐、急救包与备用软壳，AirSpeed 背板在夏季台阶路段保持背部相对干爽。鬼见愁段台阶较陡，建议搭配 Salomon X Ultra 4 等抓地力良好的徒步鞋。

Stratos 36L 租赁提示：调节好腰封后再出发，顶仓放头灯与雨衣便于取用。请沿官方步道行走，勿进入未开放区域。`
  },
  {
    id: 2,
    gearId: 3,
    title: '妙峰山 · 玫瑰谷步道',
    tag: 'LOCAL · MODERATE',
    summary: '12.5 km 周末轻徒步 · 爬升约 680 m · 谷地缓坡与林道交替',
    routeLabel: '周末轻徒步',
    distance: '12.5 km',
    elevation: '+680 m',
    difficulty: 'Moderate',
    fullText: `妙峰山玫瑰谷步道约 12.5 公里，爬升 680 米。四月底至五月初玫瑰谷花期是视觉高光，此线比香山游客更少，中段有几处较陡上坡，建议「慢走 + 短休」。

推荐租赁 Osprey Hikelite 26（装备大厅 3 号）：自重约 740g，26L 仓体对一日轻徒步恰到好处。AirSpeed 背板在谷地缓坡与林道交替路段背负舒适，侧袋可放 500ml–1L 水壶，主仓容纳路餐与轻薄保暖层无压力。

若预报降雨，另租硬壳与背包防雨罩。Hikelite 26 不适合两日露营重装——若计划过夜，请改选 Stratos 36L 或更大容量包型。`
  },
  {
    id: 3,
    gearId: 6,
    title: '海坨山 · 鞍部营地线',
    tag: 'LOCAL · HARD',
    summary: '18 km 两日露营 · 爬升超 1100 m · 华北高山营地标杆',
    routeLabel: '两日露营',
    distance: '18.0 km',
    elevation: '+1100 m',
    difficulty: 'Hard',
    fullText: `海坨山鞍部营地线约 18 公里，爬升超 1100 米，是华北两日露营标杆。鞍部海拔约 2000 米，夜间温度即使在夏季也可能接近 0°C，需另租羽绒睡袋与保暖层。

推荐租赁 MSR Hubba Hubba NX 2 人帐（装备大厅 6 号）：约 1.54kg 三季双人帐，抗风性与搭建速度经过大量用户验证。Hubba Hubba 在 4–5 级风中表现稳定，建议 Day 1 在 14:00 前完成搭建。

搭配建议：背包可选 Terraframe 65 或 Stratos 36L + 外挂帐篷；炉具可租 PocketRocket 2。鞍部生态脆弱，所有垃圾务必打包带走。`
  },
  {
    id: 4,
    gearId: 8,
    title: '凤凰岭 · 北线穿越',
    tag: 'LOCAL · EASY',
    summary: '6.8 km 亲子友好入门 · 爬升约 340 m · 石阶与土路为主',
    routeLabel: '亲子友好',
    distance: '6.8 km',
    elevation: '+340 m',
    difficulty: 'Easy',
    fullText: `凤凰岭北线约 6.8 公里，爬升 340 米，以石阶与土路为主，适合家庭或户外新人。春季桃花、秋季彩叶为视觉高光；夏季避开 11:00–15:00 暴晒，幼童需成人牵扶窄台阶。

推荐租赁 Salomon X Ultra 4 GTX 徒步鞋（装备大厅 8 号）：Gore-Tex 防水与 Contagrip 大底在雨后石阶上抓地稳定。对此类成熟步道保护充足，比运动鞋提供更好的侧向支撑与防水。

20L 左右日包即可；凤凰岭门票与开放时间请提前查询官方公告。`
  }
]

const articleList = computed(() =>
  routeArticles.map((article) => {
    const gear = gearInfoMap.value[article.gearId]
    return {
      ...article,
      gearName: gear?.gearName || `装备 #${article.gearId}`,
      gearBrand: gear?.brand || '',
      dailyRent: gear?.dailyRent,
      gearImage: resolveGearImage(article.gearId)
    }
  })
)

function resolveGearImage(gearId) {
  if (imageErrorMap.value[gearId]) {
    return getFallbackGearImage()
  }
  const gear = gearInfoMap.value[gearId]
  if (gear) {
    return getGearImages(gear).main
  }
  return getGearImages({ id: gearId }).main
}

function handleImageError(gearId) {
  imageErrorMap.value = { ...imageErrorMap.value, [gearId]: true }
}

function formatRent(gear) {
  if (gear?.dailyRent === null || gear?.dailyRent === undefined) return ''
  return `¥${Number(gear.dailyRent).toFixed(0)}/天`
}

async function loadGearInfoMap() {
  try {
    const res = await getGearPage({ pageNum: 1, pageSize: 100, status: 1 })
    const map = {}
    ;(res.data.records || []).forEach((gear) => {
      map[gear.id] = gear
    })
    gearInfoMap.value = map
  } catch {
    gearInfoMap.value = {}
  }
}

function openArticle(article) {
  currentArticle.value = article
  drawerVisible.value = true
}

function closeDrawer() {
  drawerVisible.value = false
}

function goToGear(article) {
  const keyword = article.gearName || ''
  router.push({ path: '/gears', query: keyword ? { keyword } : {} })
}

onMounted(loadGearInfoMap)
</script>

<template>
  <div class="editorial-page">
    <header class="page-hero">
      <p class="page-eyebrow">Explore Nearby</p>
      <h1 class="page-title">LOCAL ROUTES</h1>
      <p class="page-lead">城市周边精选路线 · 行程参考与装备搭配 · 租赁即刻出发</p>
    </header>

    <div class="map-placeholder" role="img" aria-label="路线地图预览">
      <div class="map-placeholder__topo" aria-hidden="true" />
      <ul class="map-placeholder__pins" aria-hidden="true">
        <li><span>香山</span></li>
        <li><span>妙峰山</span></li>
        <li><span>海坨山</span></li>
        <li><span>凤凰岭</span></li>
      </ul>
      <div class="map-placeholder__overlay">
        <span class="map-placeholder__label">LOCAL ROUTES · MAP</span>
        <span class="map-placeholder__hint">交互式路线地图即将上线</span>
      </div>
    </div>

    <section class="route-section" aria-label="周边路线列表">
      <h2 class="route-section__title">推荐路线</h2>
      <div class="route-grid">
        <article
          v-for="article in articleList"
          :key="article.id"
          class="route-card"
          @click="openArticle(article)"
        >
          <div class="route-card__head">
            <div class="route-card__head-main">
              <div class="route-card__top">
                <span class="route-card__tag">{{ article.routeLabel }}</span>
                <span class="route-card__difficulty">{{ article.difficulty }}</span>
              </div>
              <h3 class="route-card__name">{{ article.title }}</h3>
              <p class="route-card__summary">{{ article.summary }}</p>
            </div>
            <dl class="route-card__stats">
              <div>
                <dt>距离</dt>
                <dd>{{ article.distance }}</dd>
              </div>
              <div>
                <dt>爬升</dt>
                <dd>{{ article.elevation }}</dd>
              </div>
            </dl>
          </div>

          <div class="route-card__gear" @click.stop="goToGear(article)">
            <div class="route-card__gear-thumb">
              <img
                class="editorial-img"
                :src="article.gearImage"
                :alt="article.gearName"
                loading="lazy"
                @error="handleImageError(article.gearId)"
              />
            </div>
            <div class="route-card__gear-info">
              <span class="route-card__gear-label">推荐租赁</span>
              <p class="route-card__gear-name">{{ article.gearName }}</p>
              <p v-if="article.gearBrand" class="route-card__gear-brand">{{ article.gearBrand }}</p>
            </div>
            <div class="route-card__gear-action">
              <span v-if="article.dailyRent" class="route-card__gear-price">{{ formatRent(article) }}</span>
              <span class="route-card__gear-link">查看装备 →</span>
            </div>
          </div>
        </article>
      </div>
    </section>

    <el-drawer
      v-model="drawerVisible"
      :with-header="false"
      size="45%"
      class="article-drawer"
      destroy-on-close
    >
      <div class="drawer-scroll-container" data-lenis-prevent>
        <div v-if="currentArticle" class="drawer-content">
          <button type="button" class="drawer-close" aria-label="关闭" @click="closeDrawer">✕</button>
          <span class="drawer-tag">{{ currentArticle.tag }}</span>
          <h2 class="drawer-title">{{ currentArticle.title }}</h2>

          <dl class="drawer-stats">
            <div>
              <dt>距离</dt>
              <dd>{{ currentArticle.distance }}</dd>
            </div>
            <div>
              <dt>爬升</dt>
              <dd>{{ currentArticle.elevation }}</dd>
            </div>
            <div>
              <dt>难度</dt>
              <dd>{{ currentArticle.difficulty }}</dd>
            </div>
          </dl>

          <div class="drawer-body">
            <p
              v-for="(paragraph, index) in currentArticle.fullText.split('\n\n')"
              :key="index"
              class="drawer-paragraph"
            >
              {{ paragraph }}
            </p>
          </div>

          <aside class="drawer-gear-card">
            <div class="drawer-gear-card__thumb">
              <img
                :src="currentArticle.gearImage"
                :alt="currentArticle.gearName"
                @error="handleImageError(currentArticle.gearId)"
              />
            </div>
            <div class="drawer-gear-card__body">
              <span class="drawer-gear-card__label">本路线推荐装备</span>
              <p class="drawer-gear-card__name">{{ currentArticle.gearName }}</p>
              <p v-if="currentArticle.dailyRent" class="drawer-gear-card__price">
                {{ formatRent(currentArticle) }}
              </p>
              <button type="button" class="drawer-gear-card__btn" @click="goToGear(currentArticle)">
                前往装备大厅租赁
              </button>
            </div>
          </aside>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<style scoped>
.editorial-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: var(--space-lg, 48px) var(--space-md, 24px) var(--space-xl, 80px);
}

.page-hero {
  margin-bottom: var(--space-md, 24px);
  padding-bottom: var(--space-md, 24px);
  border-bottom: 1px solid var(--color-border, #e8e8e6);
}

.page-eyebrow {
  margin: 0 0 12px;
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--color-text-subtle, #999);
}

.page-title {
  margin: 0 0 16px;
  font-size: clamp(32px, 5vw, 52px);
  font-weight: 600;
  letter-spacing: 0.08em;
  line-height: 1.1;
  color: var(--color-text, #222);
}

.page-lead {
  margin: 0;
  max-width: 520px;
  font-size: 15px;
  line-height: var(--line-height-base, 1.7);
  color: var(--color-text-muted, #6b6b6b);
}

/* 地图占位 — 纯 CSS 地形风格，不再误用装备图 */
.map-placeholder {
  position: relative;
  width: 100%;
  min-height: 320px;
  margin-bottom: var(--space-lg, 48px);
  overflow: hidden;
  border: 1px solid var(--color-border, #e8e8e6);
  border-radius: 2px;
  background: var(--color-border-light, #f0f0ee);
}

.map-placeholder__topo {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 18% 42%, rgba(0, 0, 0, 0.04) 0 2px, transparent 2px),
    radial-gradient(circle at 72% 28%, rgba(0, 0, 0, 0.035) 0 2px, transparent 2px),
    radial-gradient(circle at 55% 68%, rgba(0, 0, 0, 0.03) 0 2px, transparent 2px),
    linear-gradient(135deg, #ececea 0%, #f7f7f5 45%, #e8e8e6 100%);
  background-size: 48px 48px, 64px 64px, 56px 56px, 100% 100%;
}

.map-placeholder__pins {
  position: absolute;
  inset: 0;
  margin: 0;
  padding: 0;
  list-style: none;
  pointer-events: none;
}

.map-placeholder__pins li {
  position: absolute;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.06em;
  color: var(--color-text-muted, #6b6b6b);
}

.map-placeholder__pins li::before {
  content: '';
  display: block;
  width: 8px;
  height: 8px;
  margin-bottom: 6px;
  border-radius: 50%;
  background: var(--color-text, #222);
}

.map-placeholder__pins li:nth-child(1) { top: 28%; left: 22%; }
.map-placeholder__pins li:nth-child(2) { top: 52%; left: 38%; }
.map-placeholder__pins li:nth-child(3) { top: 18%; left: 62%; }
.map-placeholder__pins li:nth-child(4) { top: 62%; left: 72%; }

.map-placeholder__overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background: rgba(255, 255, 255, 0.55);
  pointer-events: none;
}

.map-placeholder__label {
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.16em;
  color: var(--color-text, #222);
}

.map-placeholder__hint {
  font-size: 12px;
  color: var(--color-text-muted, #6b6b6b);
}

.editorial-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 2px;
  display: block;
}

.route-section__title {
  margin: 0 0 var(--space-md, 24px);
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--color-text-subtle, #999);
}

.route-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.route-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid var(--color-border, #e8e8e6);
  background: var(--color-bg-elevated, #fff);
  cursor: pointer;
  transition: box-shadow 0.25s ease;
}

.route-card:hover {
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.06);
}

.route-card__head {
  padding: var(--space-md, 24px);
  border-bottom: 1px solid var(--color-border-light, #f0f0ee);
}

.route-card__top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.route-card__tag {
  font-size: 11px;
  letter-spacing: 0.06em;
  color: var(--color-text-muted, #6b6b6b);
}

.route-card__difficulty {
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.1em;
  color: var(--color-text-subtle, #999);
}

.route-card__name {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 0.02em;
  color: var(--color-text, #222);
  transition: color 0.25s ease;
}

.route-card:hover .route-card__name {
  color: var(--color-accent-hover, #1a1a1a);
}

.route-card__summary {
  margin: 0 0 16px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--color-text-muted, #6b6b6b);
}

.route-card__stats {
  display: flex;
  gap: var(--space-md, 24px);
  margin: 0;
}

.route-card__stats div {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.route-card__stats dt {
  font-size: 10px;
  font-weight: 500;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-text-subtle, #999);
}

.route-card__stats dd {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text, #222);
}

/* 推荐装备条 — 图片仅出现在装备语境中 */
.route-card__gear {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px var(--space-md, 24px);
  background: var(--color-bg, #f7f7f5);
  transition: background 0.2s ease;
}

.route-card__gear:hover {
  background: var(--color-border-light, #f0f0ee);
}

.route-card__gear-thumb {
  flex-shrink: 0;
  width: 64px;
  height: 64px;
  overflow: hidden;
  border: 1px solid var(--color-border, #e8e8e6);
  background: #fff;
}

.route-card__gear-info {
  flex: 1;
  min-width: 0;
}

.route-card__gear-label {
  display: block;
  margin-bottom: 4px;
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--color-text-subtle, #999);
}

.route-card__gear-name {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text, #222);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.route-card__gear-brand {
  margin: 2px 0 0;
  font-size: 11px;
  color: var(--color-text-muted, #6b6b6b);
}

.route-card__gear-action {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.route-card__gear-price {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text, #222);
}

.route-card__gear-link {
  font-size: 11px;
  letter-spacing: 0.04em;
  color: var(--color-text-subtle, #999);
}

.drawer-scroll-container {
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 16px;
  scrollbar-width: thin;
  scrollbar-color: var(--color-border, #e8e8e6) transparent;
}

.drawer-scroll-container::-webkit-scrollbar {
  width: 6px;
}

.drawer-scroll-container::-webkit-scrollbar-thumb {
  background: var(--color-border, #e8e8e6);
  border-radius: 4px;
}

.drawer-content {
  position: relative;
  padding: 8px 4px 48px;
}

.drawer-close {
  position: sticky;
  top: 0;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  margin: 0 0 16px auto;
  padding: 0;
  border: none;
  background: var(--color-border-light, #f0f0ee);
  border-radius: 50%;
  font-size: 16px;
  color: var(--color-text-muted, #6b6b6b);
  cursor: pointer;
}

.drawer-tag {
  display: block;
  margin-bottom: 12px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--color-text-subtle, #999);
}

.drawer-title {
  margin: 0 0 20px;
  font-size: clamp(22px, 3vw, 28px);
  font-weight: 600;
  letter-spacing: 0.04em;
  line-height: 1.25;
  color: var(--color-text, #222);
}

.drawer-stats {
  display: flex;
  gap: 24px;
  margin: 0;
  padding: 16px 0;
  border-top: 1px solid var(--color-border-light, #f0f0ee);
  border-bottom: 1px solid var(--color-border-light, #f0f0ee);
}

.drawer-stats div {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.drawer-stats dt {
  font-size: 10px;
  font-weight: 500;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-text-subtle, #999);
}

.drawer-stats dd {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text, #222);
}

.drawer-body {
  margin-top: 24px;
}

.drawer-paragraph {
  margin: 0 0 1.2em;
  font-size: 15px;
  line-height: 1.8;
  color: var(--color-text-muted, #6b6b6b);
}

.drawer-gear-card {
  display: flex;
  gap: 16px;
  margin-top: 32px;
  padding: 20px;
  border: 1px solid var(--color-border, #e8e8e6);
  background: var(--color-bg, #f7f7f5);
}

.drawer-gear-card__thumb {
  flex-shrink: 0;
  width: 88px;
  height: 88px;
  overflow: hidden;
  border: 1px solid var(--color-border, #e8e8e6);
  background: #fff;
}

.drawer-gear-card__thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.drawer-gear-card__label {
  display: block;
  margin-bottom: 6px;
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--color-text-subtle, #999);
}

.drawer-gear-card__name {
  margin: 0 0 6px;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text, #222);
}

.drawer-gear-card__price {
  margin: 0 0 14px;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text, #222);
}

.drawer-gear-card__btn {
  margin: 0;
  padding: 9px 18px;
  border: 1px solid var(--color-text, #222);
  background: var(--color-text, #222);
  border-radius: 2px;
  font-size: 12px;
  font-weight: 500;
  letter-spacing: 0.04em;
  color: #fff;
  cursor: pointer;
  transition: opacity 0.2s ease;
}

.drawer-gear-card__btn:hover {
  opacity: 0.86;
}

:deep(.article-drawer .el-drawer__body) {
  height: 100%;
  padding: 24px 12px 24px 28px;
  overflow: hidden;
}

@media (max-width: 768px) {
  :deep(.article-drawer) {
    width: 88% !important;
  }
}

@media (max-width: 640px) {
  .map-placeholder {
    min-height: 220px;
  }

  .route-grid {
    grid-template-columns: 1fr;
  }

  .route-card__gear {
    flex-wrap: wrap;
  }

  .route-card__gear-action {
    width: 100%;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
