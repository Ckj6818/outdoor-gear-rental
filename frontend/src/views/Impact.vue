<script setup>
const principles = [
  {
    title: 'Plan Ahead and Prepare',
    text: '提前了解目的地法规、天气与路线难度。合理规划装备与补给，减少现场临时决策对环境的干扰。'
  },
  {
    title: 'Travel and Camp on Durable Surfaces',
    text: '在已开辟的路径与营地活动，避免践踏脆弱植被。营地选择远离水源，保护河岸与湿地的生态完整性。'
  },
  {
    title: 'Dispose of Waste Properly',
    text: '所有垃圾，包括食物残渣，全部带走。遵循「打包进，打包出」原则，让下一位到访者看到同样干净的山野。'
  },
  {
    title: 'Leave What You Find',
    text: '不采集植物、不带走岩石或文化遗存。保留自然原貌，让生态系统和他人体验不受破坏。'
  },
  {
    title: 'Minimize Campfire Impacts',
    text: '优先使用炉具烹饪。如确需生火，仅在允许区域使用既有火塘，完全熄灭后确认无残留温度。'
  },
  {
    title: 'Respect Wildlife',
    text: '保持安全距离观察野生动物，不喂食、不惊扰。妥善储存食物，避免人为接触改变动物的自然行为。'
  },
  {
    title: 'Be Considerate of Other Visitors',
    text: '控制音量，礼让行者。在共享的自然空间中，以克制与尊重，维护所有人都能享受的宁静体验。'
  }
]
</script>

<template>
  <div class="editorial-page">
    <header class="page-hero">
      <p class="page-eyebrow">Leave No Trace</p>
      <h1 class="page-title">OUR IMPACT</h1>
      <p class="page-lead">我们相信，每一次租赁都是一次更负责任的出发</p>
    </header>

    <article class="manifesto">
      <p class="manifesto__intro">
        户外装备租赁的意义，不仅在于降低消费与试错成本，更在于延长装备生命周期、减少闲置浪费。
        我们倡导每一位用户，以 Leave No Trace（无痕山林）七大原则为准则，在享受自然的同时，成为环境的守护者。
      </p>

      <section v-for="(item, index) in principles" :key="item.title" class="principle">
        <span class="principle__index">{{ String(index + 1).padStart(2, '0') }}</span>
        <h2 class="principle__title">{{ item.title }}</h2>
        <p class="principle__text">{{ item.text }}</p>
      </section>

      <footer class="manifesto__footer">
        <p>租赁 · 使用 · 归还 · 传承</p>
        <p class="manifesto__sign">— GEAR RENTAL · IMPACT INITIATIVE</p>
      </footer>
    </article>
  </div>
</template>

<style scoped>
.editorial-page {
  max-width: 680px;
  margin: 0 auto;
  padding: var(--space-lg, 48px) var(--space-md, 24px) var(--space-xl, 80px);
}

.page-hero {
  margin-bottom: var(--space-lg, 48px);
  text-align: center;
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
  font-size: clamp(32px, 5vw, 48px);
  font-weight: 600;
  letter-spacing: 0.1em;
  line-height: 1.15;
  color: var(--color-text, #222);
}

.page-lead {
  margin: 0 auto;
  max-width: 420px;
  font-size: 16px;
  line-height: var(--line-height-base, 1.7);
  color: var(--color-text-muted, #6b6b6b);
}

.manifesto {
  text-align: center;
}

.manifesto__intro {
  margin: 0 0 var(--space-lg, 48px);
  font-size: 17px;
  line-height: 1.85;
  letter-spacing: 0.02em;
  color: var(--color-text, #222);
}

.principle {
  margin-bottom: var(--space-lg, 48px);
  padding-bottom: var(--space-lg, 48px);
  border-bottom: 1px solid var(--color-border-light, #f0f0ee);
}

.principle:last-of-type {
  border-bottom: none;
  margin-bottom: var(--space-md, 24px);
  padding-bottom: 0;
}

.principle__index {
  display: block;
  margin-bottom: 12px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.12em;
  color: var(--color-text-subtle, #999);
}

.principle__title {
  margin: 0 0 16px;
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 0.04em;
  color: var(--color-text, #222);
}

.principle__text {
  margin: 0 auto;
  max-width: 560px;
  font-size: 15px;
  line-height: 1.85;
  color: var(--color-text-muted, #6b6b6b);
}

.manifesto__footer {
  margin-top: var(--space-lg, 48px);
  padding-top: var(--space-md, 24px);
  border-top: 1px solid var(--color-border, #e8e8e6);
}

.manifesto__footer p {
  margin: 0 0 8px;
  font-size: 14px;
  letter-spacing: 0.06em;
  color: var(--color-text-muted, #6b6b6b);
}

.manifesto__sign {
  font-size: 11px !important;
  font-weight: 600;
  letter-spacing: 0.14em !important;
  color: var(--color-text-subtle, #999) !important;
}
</style>
