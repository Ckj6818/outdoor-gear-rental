<script setup>
import { computed, ref } from 'vue'
import { getFallbackGearImage } from '@/utils/gearImages'

const props = defineProps({
  gear: {
    type: Object,
    required: true
  },
  mainImage: {
    type: String,
    default: ''
  },
  hoverImage: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['click'])

const mainImageSrc = computed(
  () => props.mainImage || props.gear.mainImage || ''
)

const hoverImageSrc = computed(
  () => props.hoverImage || props.gear.hoverImage || mainImageSrc.value
)

const hasDistinctHover = computed(
  () => !!hoverImageSrc.value && hoverImageSrc.value !== mainImageSrc.value
)

const resolvedMainSrc = ref('')
const resolvedHoverSrc = ref('')

const displayMainSrc = computed(() => resolvedMainSrc.value || mainImageSrc.value)
const displayHoverSrc = computed(() => resolvedHoverSrc.value || hoverImageSrc.value)

function handleMainError() {
  resolvedMainSrc.value = getFallbackGearImage()
}

function handleHoverError() {
  resolvedHoverSrc.value = displayMainSrc.value || getFallbackGearImage()
}

function handleClick() {
  emit('click', props.gear)
}
</script>

<template>
  <article class="gear-card" @click="handleClick">
    <div class="image-wrap" :class="{ 'has-swap': hasDistinctHover }">
      <img
        v-if="displayMainSrc"
        class="image image-main"
        :src="displayMainSrc"
        :alt="gear.gearName"
        loading="lazy"
        @error="handleMainError"
      />
      <img
        v-if="hasDistinctHover"
        class="image image-hover"
        :src="displayHoverSrc"
        :alt="`${gear.gearName} alternate view`"
        loading="lazy"
        @error="handleHoverError"
      />
      <div v-if="!displayMainSrc" class="image-placeholder" aria-hidden="true" />
      <span v-if="gear.conditionGrade" class="condition-badge">{{ gear.conditionGrade }}</span>
    </div>

    <div class="info">
      <p class="brand">{{ gear.brand }}</p>
      <div class="meta-row">
        <h3 class="name" :title="gear.gearName">{{ gear.gearName }}</h3>
        <p class="price">
          <span class="price-value">¥{{ gear.dailyRent }}</span>
          <span class="price-unit">/天</span>
        </p>
      </div>
    </div>
  </article>
</template>

<style scoped>
.gear-card {
  cursor: pointer;
  background: transparent;
  border: none;
  box-shadow: none;
  padding: 0;
  transition: transform 0.35s cubic-bezier(0.25, 0.46, 0.45, 0.94),
    box-shadow 0.35s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  will-change: transform;
}

.gear-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.06);
}

.image-wrap {
  position: relative;
  overflow: hidden;
  aspect-ratio: 4 / 5;
  background: var(--color-border-light, #f0f0ee);
}

.image,
.image-placeholder {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-main,
.image-hover {
  transition: opacity 0.55s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  will-change: opacity;
}

.image-main {
  opacity: 1;
  z-index: 1;
}

.image-hover {
  opacity: 0;
  z-index: 2;
}

.image-placeholder {
  background: linear-gradient(
    160deg,
    var(--color-border-light, #f0f0ee) 0%,
    var(--color-border, #e8e8e6) 100%
  );
}

.condition-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 3;
  padding: 4px 10px;
  font-size: 10px;
  font-weight: 400;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--color-text, #222222);
  background: rgba(247, 247, 245, 0.92);
  line-height: 1.4;
}

.gear-card:hover .has-swap .image-main {
  opacity: 0;
}

.gear-card:hover .has-swap .image-hover {
  opacity: 1;
}

.info {
  margin-top: 20px;
}

.brand {
  margin: 0 0 6px;
  font-size: 10px;
  font-weight: 400;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--color-text-subtle, #999999);
  line-height: 1.4;
}

.meta-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.name {
  margin: 0;
  flex: 1;
  min-width: 0;
  font-size: 14px;
  font-weight: 400;
  letter-spacing: var(--letter-spacing-base, 0.02em);
  line-height: var(--line-height-tight, 1.4);
  color: var(--color-text, #222222);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price {
  margin: 0;
  flex-shrink: 0;
  text-align: right;
  line-height: var(--line-height-tight, 1.4);
  color: var(--color-text, #222222);
}

.price-value {
  font-size: 14px;
  font-weight: 400;
  letter-spacing: var(--letter-spacing-base, 0.02em);
}

.price-unit {
  font-size: 11px;
  color: var(--color-text-muted, #6b6b6b);
  letter-spacing: 0.04em;
}
</style>
