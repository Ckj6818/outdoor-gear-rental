<script setup>
import { ref } from 'vue'
import { getFallbackGearImage } from '@/utils/gearImages'

const drawerVisible = ref(false)
const currentArticle = ref(null)

const articleList = ref([
  {
    id: 1,
    title: 'Osprey Stratos 36L：AirSpeed 背板三日轻装实测',
    tag: 'BACKPACKS',
    summary: '对应装备图 1-main · Osprey Stratos 36L 徒步背包，AirSpeed 空速背板与 36L 仓体的全能表现。',
    coverImage: '/images/gears/1-main.jpg',
    readTime: '8 min read',
    fullText: `本文评测对象即为封面所示的 Osprey Stratos 36L 徒步背包——装备大厅 id 1 号租赁款，主打 AirSpeed 空速背板与 2-3 日轻装出行。

36 升容量对「帐篷外挂、炉具入内、食物按天分配」的打包逻辑刚刚好。我们在鬼见愁至八大处约 14 公里、两日行程中实测，总负重约 11.8kg（含 2L 水），腰封承载约 70% 重量，肩带无明显勒痕。

AirSpeed 背板在背板与包体之间留出通风间隙，夏季 30°C 以上徒步时，背部闷热感明显低于同价位贴合式背板。侧袋可容纳 1L 软水壶，顶仓适合头灯与急救包快速取用。

当负重超过 13kg 或计划四日以上重装，建议升级至 50L 以上型号。对典型周末轻装穿越，Stratos 36L 是租赁库中值得优先尝试的全能背包。`
  },
  {
    id: 2,
    title: 'Mystery Ranch Terraframe 65：重装穿越背负系统解析',
    tag: 'BACKPACKS',
    summary: '对应装备图 2-main · Mystery Ranch Terraframe 65，Guide Light 背负与 65L 仓体应对多日重装。',
    coverImage: '/images/gears/2-main.jpg',
    readTime: '7 min read',
    fullText: `封面图为 Mystery Ranch Terraframe 65 重装背包（装备大厅 2 号），Guide Light 背负系统面向多日重装穿越与登山场景。

65 升主仓可容纳四季衣物、多日食物与帐篷外挂系统。我们在模拟 18kg 负重打包后进行了 8 公里爬升测试，腰封宽度与硬度对重量转移非常直接——正确调节后，肩部压力可控制在可接受范围内。

Terraframe 的 Overload 扩展仓设计适合最后一天补给消耗后的装备收纳变化。面料耐磨性在灌木穿行中表现可靠，但空载时略重，不建议作为单日轻量包使用。

与 Osprey Stratos 36L 相比，Terraframe 65 牺牲轻量换取负重上限与长途稳定性。若你的行程是海坨鞍部露营、武功山穿越等多日线路，这是比 36L 日包更匹配的选择。`
  },
  {
    id: 3,
    title: 'MSR Hubba Hubba NX 2：轻量化双人帐实战评测',
    tag: 'SHELTER',
    summary: '对应装备图 6-main · MSR Hubba Hubba NX 2 人帐篷，三季双人帐的经典标杆。',
    coverImage: '/images/gears/6-main.jpg',
    readTime: '6 min read',
    fullText: `封面所示为 MSR Hubba Hubba NX 2 人帐篷——装备大厅 6 号租赁款，重量约 1.54kg，三季双人轻量化帐杆帐。

我们在海坨山鞍部营地实测过夜，夜间 4-5 级风中帐杆弧度与地钉配合良好，无明显内摆。Dual 门厅设计使两人在雨天进出互不干扰，内部高度足够坐直穿脱冲锋衣。

熟练搭建约 8 分钟可完成内外帐固定。单人使用时空间充裕；双人并排 60cm 睡垫刚好，中间仍可放置头灯与软壳。210T 面料在结露控制上表现正常，清晨内外帐之间仍建议留通风间隙。

租赁归还前请确认帐杆无裂纹、地钉数量完整，并在入库前彻底晾干。适合两日露营与高原过渡营地，是租赁库中帐篷类的首选推荐。`
  },
  {
    id: 4,
    title: 'Salomon X Ultra 4 GTX：中低海拔徒步鞋全场景测试',
    tag: 'FOOTWEAR',
    summary: '对应装备图 8-main · Salomon X Ultra 4 GTX 徒步鞋，Gore-Tex 防水与 Contagrip 大底。',
    coverImage: '/images/gears/8-main.jpg',
    readTime: '5 min read',
    fullText: `封面图为 Salomon X Ultra 4 GTX 徒步鞋（装备大厅 8 号），Gore-Tex 防水膜搭配 Contagrip MA 大底，定位中低海拔徒步与单日穿越。

我们在香山雨后台阶与凤凰岭土路上对比测试，湿滑路面抓地稳定，Advanced Chassis 系统提供清晰足底反馈，20 公里后未见明显足弓疲劳。

Gore-Tex 在草丛露水与浅溪溅水中表现可靠，深潭浸泡仍应绕行。鞋楦偏窄，宽脚用户建议大半码或薄袜试穿。与纯越野跑鞋相比，X Ultra 4 在防护与防水上更全面，是北京周边单日线路的高频租赁款。

若路线含大量 OFF-TRAIL 或湿滑岩石，这是比路跑鞋更稳妥的选择；若追求极致轻量跑走结合，可另选 Hoka 等低帮款对比。`
  },
  {
    id: 5,
    title: 'Hoka Trail GTX 低帮：长距离徒步脚感评测',
    tag: 'FOOTWEAR',
    summary: '对应装备图 11-main · Hoka Trail GTX 低帮徒步鞋，Meta-Rocker 滚动感中底。',
    coverImage: '/images/gears/11-main.jpg',
    readTime: '6 min read',
    fullText: `封面所示为 Hoka Trail GTX 低帮徒步鞋（装备大厅 11 号），Meta-Rocker 滚动感中底是 Hoka 的核心标识，适合长距离徒步与 fast hiking。

我们在妙峰山 12 公里线路上实测，滚动感中底在平整土路与防火道上明显降低小腿负担，25 公里以内脚感一致性良好。Gore-Tex 内衬在春季露水与短时小雨中保持鞋内干爽。

低帮设计在 ankle 灵活性上优于中帮，但在碎石坡与不规范台阶上，脚踝保护弱于 Salomon X Ultra 4。若你的路线以成熟步道为主、总爬升在 800 米以内，Trail GTX 的舒适优势非常明显。

与封面同系列的租赁建议：凤凰岭、玫瑰谷等轻徒步优先 Hoka；香山鬼见愁等台阶密集路线可优先考虑 X Ultra 4。`
  },
  {
    id: 6,
    title: 'MSR PocketRocket 2：超轻气炉效率与收纳实测',
    tag: 'COOKING',
    summary: '对应装备图 16-main · MSR PocketRocket 2 炉具套装，单人轻量化露营炊事方案。',
    coverImage: '/images/gears/16-main.jpg',
    readTime: '5 min read',
    fullText: `封面图为 MSR PocketRocket 2 超轻气炉（装备大厅 16 号），含锅具组合，面向单人轻量化露营与徒步露营。

炉头折叠后约打火机大小，收纳进 750ml 钛杯无压力。我们在海拔约 1800 米营地测试，1 升水常温至滚约 4 分 30 秒，风力 3 级时需配合简易挡风板。

调节旋钮线性清晰，小火慢炖与大火快煮均可胜任。与 Jetboil 一体化系统相比，PocketRocket 2 更轻、更灵活，但需要自备锅具与挡风板，适合已有炊具经验的用户。

租赁使用前请检查气罐接口密封圈，使用后清理燃烧残渣。归还时确认点火正常、折叠铰链无卡滞。对海坨、东灵等需要营地炊事的线路，这是比冷食更省心的轻量方案。`
  }
])

function openArticle(article) {
  currentArticle.value = article
  drawerVisible.value = true
}

function closeDrawer() {
  drawerVisible.value = false
}

function handleImageError(event) {
  event.target.src = getFallbackGearImage()
}
</script>

<template>
  <div class="editorial-page">
    <header class="page-hero">
      <p class="page-eyebrow">Editorial</p>
      <h1 class="page-title">GEAR REVIEWS</h1>
      <p class="page-lead">深度装备评测 · 真实户外场景 · 帮你做出更明智的选择</p>
    </header>

    <section class="article-grid" aria-label="评测文章列表">
      <article
        v-for="article in articleList"
        :key="article.id"
        class="article-card"
        @click="openArticle(article)"
      >
        <div class="article-card__thumb">
          <img
            class="editorial-img editorial-img--zoom"
            :src="article.coverImage"
            :alt="article.title"
            loading="lazy"
            @error="handleImageError"
          />
        </div>
        <div class="article-card__body">
          <span class="article-card__category">{{ article.tag }}</span>
          <h2 class="article-card__title">{{ article.title }}</h2>
          <p class="article-card__excerpt">{{ article.summary }}</p>
          <span class="article-card__meta">{{ article.readTime }}</span>
        </div>
      </article>
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
          <img
            class="drawer-cover"
            :src="currentArticle.coverImage"
            :alt="currentArticle.title"
            @error="handleImageError"
          />
          <div class="drawer-body">
            <p
              v-for="(paragraph, index) in currentArticle.fullText.split('\n\n')"
              :key="index"
              class="drawer-paragraph"
            >
              {{ paragraph }}
            </p>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<style scoped>
.editorial-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--space-lg, 48px) var(--space-md, 24px) var(--space-xl, 80px);
}

.page-hero {
  margin-bottom: var(--space-lg, 48px);
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

.article-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-md, 24px);
}

.article-card {
  display: flex;
  flex-direction: column;
  border: 1px solid var(--color-border, #e8e8e6);
  background: var(--color-bg-elevated, #fff);
  cursor: pointer;
  transition: box-shadow 0.25s ease;
}

.article-card:hover {
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.06);
}

.article-card:hover .article-card__title {
  color: var(--color-accent-hover, #1a1a1a);
}

.article-card__thumb {
  aspect-ratio: 4 / 3;
  overflow: hidden;
  background: var(--color-border-light, #f0f0ee);
}

.editorial-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
  display: block;
}

.editorial-img--zoom {
  transition: transform 0.4s ease;
}

.article-card:hover .editorial-img--zoom {
  transform: scale(1.03);
}

.article-card__body {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 20px;
}

.article-card__category {
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.12em;
  color: var(--color-text-subtle, #999);
  transition: color 0.25s ease;
}

.article-card:hover .article-card__category {
  color: var(--color-text-muted, #6b6b6b);
}

.article-card__title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  line-height: var(--line-height-tight, 1.4);
  letter-spacing: 0.02em;
  color: var(--color-text, #222);
  transition: color 0.25s ease;
}

.article-card__excerpt {
  margin: 0;
  flex: 1;
  font-size: 13px;
  line-height: 1.6;
  color: var(--color-text-muted, #6b6b6b);
}

.article-card__meta {
  font-size: 11px;
  letter-spacing: 0.06em;
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

.drawer-scroll-container::-webkit-scrollbar-track {
  background: transparent;
}

.drawer-scroll-container::-webkit-scrollbar-thumb {
  background: var(--color-border, #e8e8e6);
  border-radius: 4px;
}

.drawer-scroll-container::-webkit-scrollbar-thumb:hover {
  background: var(--color-text-subtle, #999);
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
  transition: background 0.2s ease, color 0.2s ease;
}

.drawer-close:hover {
  background: var(--color-border, #e8e8e6);
  color: var(--color-text, #222);
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

.drawer-cover {
  width: 100%;
  aspect-ratio: 16 / 10;
  object-fit: cover;
  border-radius: 4px;
  display: block;
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

.drawer-paragraph:last-child {
  margin-bottom: 0;
}

:deep(.article-drawer .el-drawer__body) {
  height: 100%;
  padding: 24px 12px 24px 28px;
  overflow: hidden;
}

@media (max-width: 960px) {
  .article-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  :deep(.article-drawer) {
    width: 88% !important;
  }
}

@media (max-width: 600px) {
  .article-grid {
    grid-template-columns: 1fr;
  }

  .editorial-page {
    padding-top: var(--space-md, 24px);
  }
}
</style>
