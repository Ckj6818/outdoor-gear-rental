<script setup>
import { nextTick, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Close, Promotion } from '@element-plus/icons-vue'
import { consultAi } from '@/api/ai'

const router = useRouter()

const panelOpen = ref(false)
const inputText = ref('')
const sending = ref(false)
const messageListRef = ref(null)

const messages = ref([
  {
    id: 'welcome',
    role: 'assistant',
    reply: '你好，我是山行 AI 导购。告诉我你的出行计划、路线或预算，我会从当前可租库存里为你推荐合适的装备。',
    recommendGears: [],
    isPlainText: false
  }
])

/**
 * 解析后端返回的 JSON 字符串；失败则降级为纯文本展示。
 */
function parseAiResponse(raw) {
  if (raw == null || raw === '') {
    return { reply: '暂无回复内容', recommendGears: [], isPlainText: true }
  }

  try {
    let parsed = raw
    if (typeof parsed === 'string') {
      parsed = JSON.parse(parsed.trim())
    }
    if (typeof parsed === 'string') {
      parsed = JSON.parse(parsed.trim())
    }

    const reply = typeof parsed.reply === 'string' ? parsed.reply : String(parsed.reply ?? '')
    const recommendGears = Array.isArray(parsed.recommend_gears)
      ? parsed.recommend_gears.filter((item) => item && item.id != null)
      : []

    return { reply, recommendGears, isPlainText: false }
  } catch {
    return {
      reply: String(raw),
      recommendGears: [],
      isPlainText: true
    }
  }
}

function formatRent(rent) {
  const value = Number(rent)
  if (Number.isNaN(value)) return '¥--/天'
  return `¥${value % 1 === 0 ? value.toFixed(0) : value.toFixed(2)}/天`
}

/** 构建多轮对话历史（不含欢迎语与当前未发送消息） */
function buildChatHistory() {
  const maxMessages = 8
  const items = messages.value.filter(
    (msg) => msg.role === 'user' || (msg.role === 'assistant' && msg.id !== 'welcome')
  )
  return items.slice(-maxMessages).map((msg) => ({
    role: msg.role,
    content: msg.role === 'user' ? msg.content : (msg.reply || msg.content || '')
  }))
}

async function scrollToBottom(smooth = true) {
  await nextTick()
  const el = messageListRef.value
  if (!el) return
  el.scrollTo({
    top: el.scrollHeight,
    behavior: smooth ? 'smooth' : 'auto'
  })
}

function togglePanel() {
  panelOpen.value = !panelOpen.value
}

function closePanel() {
  panelOpen.value = false
}

function goRent(gear) {
  closePanel()
  router.push({
    path: '/gears',
    query: { gearId: String(gear.id) }
  })
}

async function sendMessage() {
  const question = inputText.value.trim()
  if (!question || sending.value) return

  messages.value.push({
    id: `user-${Date.now()}`,
    role: 'user',
    content: question
  })
  inputText.value = ''
  await scrollToBottom()

  const history = buildChatHistory()
  // 历史中不含刚发送的当前提问（已在 question 参数中传递）
  if (history.length > 0 && history[history.length - 1].role === 'user') {
    history.pop()
  }

  sending.value = true
  try {
    const res = await consultAi(question, history)
    const { reply, recommendGears, isPlainText } = parseAiResponse(res.data)

    messages.value.push({
      id: `assistant-${Date.now()}`,
      role: 'assistant',
      reply,
      recommendGears,
      isPlainText
    })
  } catch (err) {
    messages.value.push({
      id: `assistant-err-${Date.now()}`,
      role: 'assistant',
      reply: err?.message || 'AI 导购暂时不可用，请稍后再试。',
      recommendGears: [],
      isPlainText: true
    })
  } finally {
    sending.value = false
    await scrollToBottom()
  }
}

function handleInputKeydown(event) {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

watch(panelOpen, (open) => {
  if (open) {
    scrollToBottom(false)
  }
})
</script>

<template>
  <div class="ai-chat-root">
    <!-- 悬浮入口 -->
    <button
      v-show="!panelOpen"
      type="button"
      class="ai-chat-fab"
      aria-label="打开 AI 导购"
      @click="togglePanel"
    >
      <el-icon :size="22"><ChatDotRound /></el-icon>
      <span class="ai-chat-fab__label">AI 导购</span>
    </button>

    <!-- 聊天面板 -->
    <Transition name="ai-panel">
      <aside v-if="panelOpen" class="ai-chat-panel" aria-label="AI 装备导购">
        <header class="ai-chat-panel__header">
          <div class="ai-chat-panel__title-wrap">
            <span class="ai-chat-panel__eyebrow">RAG · CUI</span>
            <h2 class="ai-chat-panel__title">山行 AI 导购</h2>
          </div>
          <button type="button" class="ai-chat-panel__close" aria-label="关闭" @click="closePanel">
            <el-icon :size="18"><Close /></el-icon>
          </button>
        </header>

        <div ref="messageListRef" class="ai-chat-messages" data-lenis-prevent>
          <article
            v-for="msg in messages"
            :key="msg.id"
            class="ai-chat-message"
            :class="`ai-chat-message--${msg.role}`"
          >
            <!-- 用户消息 -->
            <div v-if="msg.role === 'user'" class="ai-chat-bubble ai-chat-bubble--user">
              {{ msg.content }}
            </div>

            <!-- AI 消息：文本气泡 + 推荐卡片 -->
            <template v-else>
              <div class="ai-chat-bubble ai-chat-bubble--assistant">
                {{ msg.reply }}
              </div>

              <div
                v-if="msg.recommendGears?.length"
                class="ai-chat-gear-scroll"
                role="list"
                aria-label="推荐装备"
              >
                <div
                  v-for="gear in msg.recommendGears"
                  :key="gear.id"
                  class="ai-chat-gear-card"
                  role="listitem"
                >
                  <div class="ai-chat-gear-card__info">
                    <p class="ai-chat-gear-card__name">{{ gear.name }}</p>
                    <p class="ai-chat-gear-card__rent">{{ formatRent(gear.rent) }}</p>
                  </div>
                  <el-button
                    type="primary"
                    size="small"
                    class="ai-chat-gear-card__btn"
                    @click="goRent(gear)"
                  >
                    去租赁
                  </el-button>
                </div>
              </div>
            </template>
          </article>

          <div v-if="sending" class="ai-chat-typing">
            <span class="ai-chat-typing__dot" />
            <span class="ai-chat-typing__dot" />
            <span class="ai-chat-typing__dot" />
          </div>
        </div>

        <footer class="ai-chat-input-bar">
          <textarea
            v-model="inputText"
            class="ai-chat-input"
            rows="1"
            placeholder="例如：周末去露营，需要租什么？"
            :disabled="sending"
            @keydown="handleInputKeydown"
          />
          <button
            type="button"
            class="ai-chat-send"
            :disabled="sending || !inputText.trim()"
            aria-label="发送"
            @click="sendMessage"
          >
            <el-icon :size="18"><Promotion /></el-icon>
          </button>
        </footer>
      </aside>
    </Transition>
  </div>
</template>

<style scoped>
.ai-chat-root {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 1200;
  pointer-events: none;
}

.ai-chat-root > * {
  pointer-events: auto;
}

/* ---------- 悬浮按钮 ---------- */
.ai-chat-fab {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 18px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 999px;
  background: #1a1a1a;
  color: #f5f5f3;
  cursor: pointer;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.28);
  transition: transform 0.22s ease, box-shadow 0.22s ease, background 0.22s ease;
}

.ai-chat-fab:hover {
  transform: translateY(-2px);
  background: #222;
  box-shadow: 0 12px 36px rgba(0, 0, 0, 0.34);
}

.ai-chat-fab__label {
  font-size: 13px;
  font-weight: 500;
  letter-spacing: 0.04em;
}

/* ---------- 面板 ---------- */
.ai-chat-panel {
  display: flex;
  flex-direction: column;
  width: min(400px, calc(100vw - 32px));
  height: min(560px, calc(100vh - 48px));
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 16px;
  background: #141414;
  color: #e8e8e6;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.45);
  overflow: hidden;
}

.ai-panel-enter-active,
.ai-panel-leave-active {
  transition: opacity 0.24s ease, transform 0.24s ease;
}

.ai-panel-enter-from,
.ai-panel-leave-to {
  opacity: 0;
  transform: translateY(12px) scale(0.98);
}

.ai-chat-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;
}

.ai-chat-panel__eyebrow {
  display: block;
  font-size: 10px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.38);
}

.ai-chat-panel__title {
  margin: 4px 0 0;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.02em;
  color: #fafafa;
}

.ai-chat-panel__close {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease;
}

.ai-chat-panel__close:hover {
  background: rgba(255, 255, 255, 0.06);
  color: #fff;
}

/* ---------- 消息列表（独立滚动，不影响页面 Lenis） ---------- */
.ai-chat-messages {
  flex: 1;
  min-height: 0;
  padding: 16px 14px;
  overflow-y: auto;
  overflow-x: hidden;
  overscroll-behavior: contain;
  -webkit-overflow-scrolling: touch;
  scroll-behavior: smooth;
}

.ai-chat-messages::-webkit-scrollbar {
  width: 4px;
}

.ai-chat-messages::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.12);
  border-radius: 4px;
}

.ai-chat-message {
  margin-bottom: 16px;
}

.ai-chat-message--user {
  display: flex;
  justify-content: flex-end;
}

.ai-chat-message--assistant {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
}

/* ---------- 气泡 ---------- */
.ai-chat-bubble {
  max-width: 88%;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.65;
  word-break: break-word;
  white-space: pre-wrap;
}

.ai-chat-bubble--user {
  background: #2a2a2a;
  color: #f0f0ee;
  border-bottom-right-radius: 4px;
}

.ai-chat-bubble--assistant {
  background: #1f1f1f;
  color: #d8d8d6;
  border: 1px solid rgba(255, 255, 255, 0.04);
  border-bottom-left-radius: 4px;
}

/* ---------- 推荐装备横向滑动卡片 ---------- */
.ai-chat-gear-scroll {
  display: flex;
  gap: 10px;
  width: 100%;
  max-width: 100%;
  padding-bottom: 4px;
  overflow-x: auto;
  overflow-y: hidden;
  overscroll-behavior-x: contain;
  -webkit-overflow-scrolling: touch;
  scroll-snap-type: x mandatory;
  scrollbar-width: thin;
  scrollbar-color: rgba(255, 255, 255, 0.12) transparent;
}

.ai-chat-gear-scroll::-webkit-scrollbar {
  height: 4px;
}

.ai-chat-gear-scroll::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.12);
  border-radius: 4px;
}

.ai-chat-gear-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex: 0 0 auto;
  min-width: 220px;
  max-width: 280px;
  padding: 12px 14px;
  border-radius: 10px;
  background: #222;
  border: 1px solid rgba(255, 255, 255, 0.06);
  scroll-snap-align: start;
  transition: border-color 0.2s ease, background 0.2s ease;
}

.ai-chat-gear-card:hover {
  background: #282828;
  border-color: rgba(255, 255, 255, 0.1);
}

.ai-chat-gear-card__info {
  min-width: 0;
  flex: 1;
}

.ai-chat-gear-card__name {
  margin: 0 0 4px;
  font-size: 13px;
  font-weight: 500;
  color: #f5f5f3;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ai-chat-gear-card__rent {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.02em;
  color: #ffffff;
}

.ai-chat-gear-card__btn {
  flex-shrink: 0;
  --el-button-bg-color: #fafafa;
  --el-button-border-color: #fafafa;
  --el-button-text-color: #111;
  --el-button-hover-bg-color: #fff;
  --el-button-hover-border-color: #fff;
  --el-button-hover-text-color: #000;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  padding: 6px 12px;
}

/* ---------- 输入区 ---------- */
.ai-chat-input-bar {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  padding: 12px 14px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;
  background: #121212;
}

.ai-chat-input {
  flex: 1;
  min-height: 40px;
  max-height: 96px;
  padding: 10px 12px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 10px;
  background: #1a1a1a;
  color: #f0f0ee;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.5;
  resize: none;
  outline: none;
  transition: border-color 0.2s ease;
}

.ai-chat-input::placeholder {
  color: rgba(255, 255, 255, 0.28);
}

.ai-chat-input:focus {
  border-color: rgba(255, 255, 255, 0.18);
}

.ai-chat-input:disabled {
  opacity: 0.6;
}

.ai-chat-send {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 10px;
  background: #fafafa;
  color: #111;
  cursor: pointer;
  flex-shrink: 0;
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.ai-chat-send:hover:not(:disabled) {
  transform: scale(1.04);
}

.ai-chat-send:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

/* ---------- 加载动画 ---------- */
.ai-chat-typing {
  display: flex;
  gap: 5px;
  padding: 4px 2px 8px;
}

.ai-chat-typing__dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.35);
  animation: ai-typing 1.2s infinite ease-in-out;
}

.ai-chat-typing__dot:nth-child(2) {
  animation-delay: 0.15s;
}

.ai-chat-typing__dot:nth-child(3) {
  animation-delay: 0.3s;
}

@keyframes ai-typing {
  0%,
  80%,
  100% {
    opacity: 0.35;
    transform: translateY(0);
  }
  40% {
    opacity: 1;
    transform: translateY(-3px);
  }
}

@media (max-width: 480px) {
  .ai-chat-root {
    right: 16px;
    bottom: 16px;
  }

  .ai-chat-fab__label {
    display: none;
  }

  .ai-chat-fab {
    width: 52px;
    height: 52px;
    padding: 0;
    justify-content: center;
    border-radius: 50%;
  }

  .ai-chat-panel {
    width: calc(100vw - 32px);
    height: min(520px, calc(100vh - 32px));
  }
}
</style>
