<script setup>
import { ref } from 'vue'
import { getFallbackGearImage } from '@/utils/gearImages'

const drawerVisible = ref(false)
const currentArticle = ref(null)
const mapImage = '/images/gears/3-main.jpg'

const articleList = ref([
  {
    id: 1,
    title: 'Osprey Stratos 36L 打包指南：一日至两日轻装',
    tag: 'HOW-TO · PACKING',
    summary: '封面图 1-main · Stratos 36L 背包的分区装载与重心调节实操。',
    coverImage: '/images/gears/1-main.jpg',
    duration: '12 min read',
    fullText: `本教程以封面所示 Osprey Stratos 36L（装备大厅 1 号）为示范包型，讲解 26-36L 日包的科学打包流程。

Stratos 36L 主仓较深，建议底层放睡袋与备用衣物，中层贴近 AirSpeed 背板处放置炉具、食物与水等重物，顶层与顶仓放头灯、急救包与雨衣。36L 容量下，侧袋可稳定容纳 1L 软水壶，避免主仓反复翻找。

打包完成后，务必调节肩带、胸带与腰封：在平地试背并小幅跳跃，确认无顶背感。Stratos 的 AirSpeed 背板需要腰封承担主要重量——若腰封位置过低，长时间徒步会导致肩部过早疲劳。

租赁归还前请清空所有仓室、擦拭背板汗渍，并检查防雨罩是否在顶仓。掌握这套流程，可让 Stratos 36L 在香山、凤凰岭等单日线路上发挥最佳背负效率。`
  },
  {
    id: 2,
    title: 'Sawyer Squeeze 滤水器：野外补水标准流程',
    tag: 'HOW-TO · WATER',
    summary: '封面图 28-main · Sawyer Squeeze 挤压式滤水系统使用与维护。',
    coverImage: '/images/gears/28-main.jpg',
    duration: '8 min read',
    fullText: `封面图为 Sawyer Squeeze 滤水系统——0.1 微米级别滤芯，是长线徒步补水的轻量化标配。

正确流程：选取流动溪水，先让原水静置减少悬浮物；将脏袋与滤芯连接，缓慢挤压清水袋，避免暴力挤压导致密封圈渗漏。滤后水仍建议在不确定源头时煮沸或使用消毒片二次处理。

滤芯使用后切勿冰冻，否则微孔可能永久损坏。长期存放前按说明进行反冲洗，租赁归还时检查螺纹接口有无裂纹、脏袋有无穿孔。

在北京周边 12-20 公里线路中，携带 Sawyer 可将行前存水量减少 1-2L，但务必在地图上标注最后一个可靠补水点。封面所示 Squeeze 系统配合软水壶，是妙峰山、海坨等有多处溪流线路的理想组合。`
  },
  {
    id: 3,
    title: '挪客 Cloud Up 2 帐篷：快速搭建与收帐要点',
    tag: 'HOW-TO · SHELTER',
    summary: '封面图 7-main · Naturehike Cloud Up 2 双人帐篷搭建与维护。',
    coverImage: '/images/gears/7-main.jpg',
    duration: '10 min read',
    fullText: `封面所示为挪客 Cloud Up 2 双人帐篷（装备大厅 7 号），210T 涤纶面料，适合入门露营与徒步露营。

搭建顺序：清理地面 → 铺设地布 → 穿帐杆 → 挂内帐 → 罩外帐 → 打地钉 → 拉风绳。Cloud Up 2 结构直观，新手约 12-15 分钟可完成首次独立搭建，熟练后可压缩至 8 分钟以内。

风雨天要点：迎风侧地钉先打，地钉 45° 外倾；内外帐保持间隙减少冷凝；门厅背风便于炊事。收帐前拍净外帐泥点，回城后完全晾干再折叠，避免发霉。

租赁归还请清点地钉与帐杆，检查拉链顺滑度。Cloud Up 2 重量与空间平衡较好，是预算有限但想尝试过夜露营用户的合理首选。`
  },
  {
    id: 4,
    title: 'Patagonia Torrentshell 3L：硬壳层动态穿脱',
    tag: 'HOW-TO · CLOTHING',
    summary: '封面图 27-main · Torrentshell 3L 冲锋衣在三明治穿衣法中的角色。',
    coverImage: '/images/gears/27-main.jpg',
    duration: '7 min read',
    fullText: `封面图为 Patagonia Torrentshell 3L 冲锋衣——三层穿衣法中的「防护层」，负责防风防雨，对应装备图 27-main。

三层法则：贴身排汗层（美利奴/聚酯）→ 中间保暖层（抓绒/羽绒）→ 外层硬壳。Torrentshell 3L 应在起风或降雨前提前穿上，而非等湿透后再套——硬壳的核心是「保持内层干燥」。

调节节奏：上坡前解开腋下拉链或半开前襟，避免闷汗；到顶休息前 5 分钟拉好拉链。北京周边春秋温差大，背包中应始终保留一件可快速取用的硬壳。

租赁 Torrentshell 类硬壳时，注意袖口与帽檐调节是否完好。与封面 Stratos 等背包搭配，可将硬壳卷收进顶仓，实现「随时可穿、不占主仓」的打包方案。`
  },
  {
    id: 5,
    title: 'Black Diamond Spot 400 头灯：夜行与营地照明',
    tag: 'HOW-TO · NAVIGATION',
    summary: '封面图 23-main · Spot 400 头灯模式选择与夜行安全守则。',
    coverImage: '/images/gears/23-main.jpg',
    duration: '9 min read',
    fullText: `封面所示为 Black Diamond Spot 400 头灯——400 流明高亮档，IPX8 防水，是夜行下撤的标准装备。

模式选择：高亮档用于辨路，低亮档用于营地整理，红光档 preserve 暗适应且不惊扰队友。Spot 400 的 PowerTap 感应切换在戴手套时仍可操作，适合冬季与高海拔场景。

夜行守则：至少两人结伴，间距 3-5 米；优先选择成熟步道； sunset 前预留 1 小时安全余量。出发前检查 AAA 电池电量，长线携带备用电池。

与手机照明相比，头灯的泛光/聚光切换、头带稳定性与解放双手是本质差异。租赁 Spot 400 后，建议先在室内熟悉按键逻辑，避免夜间误操作。任何可能 sunset 后仍在路上的行程，都应将封面同款头灯列入必备清单。`
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
      <p class="page-eyebrow">Skills &amp; Learning</p>
      <h1 class="page-title">HOW-TO &amp; SKILLS</h1>
      <p class="page-lead">从入门到进阶 · 视频教程与图文指南 · 提升你的户外能力</p>
    </header>

    <section class="guide-list" aria-label="技能教程列表">
      <article
        v-for="article in articleList"
        :key="article.id"
        class="guide-item"
        @click="openArticle(article)"
      >
        <div class="guide-item__media">
          <img
            class="editorial-img editorial-img--zoom"
            :src="article.coverImage"
            :alt="article.title"
            loading="lazy"
            @error="handleImageError"
          />
          <span class="guide-item__type">{{ article.tag.split('·').pop()?.trim() || 'GUIDE' }}</span>
        </div>
        <div class="guide-item__content">
          <span class="guide-item__tag">{{ article.tag }}</span>
          <h2 class="guide-item__title">{{ article.title }}</h2>
          <p class="guide-item__desc">{{ article.summary }}</p>
        </div>
        <div class="guide-item__meta">
          <span>{{ article.duration }}</span>
          <span class="guide-item__arrow" aria-hidden="true">→</span>
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
  max-width: 960px;
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
  font-size: clamp(28px, 4.5vw, 48px);
  font-weight: 600;
  letter-spacing: 0.06em;
  line-height: 1.15;
  color: var(--color-text, #222);
}

.page-lead {
  margin: 0;
  max-width: 480px;
  font-size: 15px;
  line-height: var(--line-height-base, 1.7);
  color: var(--color-text-muted, #6b6b6b);
}

.guide-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.guide-item {
  display: grid;
  grid-template-columns: 120px 1fr auto;
  align-items: center;
  gap: var(--space-md, 24px);
  padding: var(--space-md, 24px) 0;
  border-bottom: 1px solid var(--color-border, #e8e8e6);
  cursor: pointer;
  transition: background 0.2s ease;
}

.guide-item:first-child {
  border-top: 1px solid var(--color-border, #e8e8e6);
}

.guide-item:hover {
  background: var(--color-border-light, #f0f0ee);
  margin: 0 calc(-1 * var(--space-sm, 16px));
  padding-left: var(--space-sm, 16px);
  padding-right: var(--space-sm, 16px);
}

.guide-item:hover .guide-item__title {
  color: var(--color-accent-hover, #1a1a1a);
}

.guide-item__media {
  position: relative;
  width: 120px;
  height: 72px;
  overflow: hidden;
  background: var(--color-border-light, #f0f0ee);
  border: 1px solid var(--color-border, #e8e8e6);
}

.guide-item__media .editorial-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
  display: block;
}

.editorial-img--zoom {
  transition: transform 0.4s ease;
}

.guide-item:hover .editorial-img--zoom {
  transform: scale(1.03);
}

.guide-item__type {
  position: absolute;
  bottom: 6px;
  left: 6px;
  padding: 2px 6px;
  font-size: 9px;
  font-weight: 600;
  letter-spacing: 0.1em;
  color: var(--color-text, #222);
  background: var(--color-bg-elevated, #fff);
  border: 1px solid var(--color-border, #e8e8e6);
}

.guide-item__tag {
  display: block;
  margin-bottom: 6px;
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.1em;
  color: var(--color-text-subtle, #999);
}

.guide-item__title {
  margin: 0 0 8px;
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 0.02em;
  color: var(--color-text, #222);
  transition: color 0.25s ease;
}

.guide-item__desc {
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
  color: var(--color-text-muted, #6b6b6b);
}

.guide-item__meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  letter-spacing: 0.04em;
  color: var(--color-text-subtle, #999);
  white-space: nowrap;
}

.guide-item__arrow {
  font-size: 16px;
  color: var(--color-text-muted, #6b6b6b);
  transition: transform 0.2s ease;
}

.guide-item:hover .guide-item__arrow {
  transform: translateX(4px);
  color: var(--color-text, #222);
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

@media (max-width: 768px) {
  :deep(.article-drawer) {
    width: 88% !important;
  }
}

@media (max-width: 640px) {
  .guide-item {
    grid-template-columns: 88px 1fr;
    grid-template-rows: auto auto;
  }

  .guide-item__media {
    width: 88px;
    height: 56px;
  }

  .guide-item__meta {
    grid-column: 2;
    justify-self: start;
  }
}
</style>
