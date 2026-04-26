<template>
  <main class="home-page">
    <section class="home-hero" aria-labelledby="home-title">
      <div class="hero-copy">
        <p class="eyebrow">Book's AI Workspace</p>
        <h1 id="home-title">ai-agent</h1>
        <p class="hero-text">
          Choose the right assistant and keep the conversation focused, practical, and easy to scan.
        </p>
      </div>

      <div class="hero-panel" aria-label="Workspace status">
        <div class="status-row">
          <Connection class="status-icon" aria-hidden="true" />
          <span>Ready for a new session</span>
        </div>
        <div class="status-grid">
          <div>
            <strong>2</strong>
            <span>Assistants</span>
          </div>
          <div>
            <strong>Live</strong>
            <span>Streaming chat</span>
          </div>
        </div>
      </div>
    </section>

    <section class="apps-section" aria-label="Available assistants">
      <button
        v-for="app in apps"
        :key="app.path"
        type="button"
        class="app-card"
        @click="navigateTo(app.path)"
      >
        <span class="app-icon" :class="app.tone">
          <component :is="app.icon" aria-hidden="true" />
        </span>
        <span class="app-copy">
          <span class="app-title">{{ app.title }}</span>
          <span class="app-desc">{{ app.description }}</span>
        </span>
        <span class="open-action">
          <span>Open</span>
          <ArrowRight aria-hidden="true" />
        </span>
      </button>
    </section>
  </main>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useHead } from '@vueuse/head'
import { ArrowRight, ChatDotRound, Connection, MagicStick } from '@element-plus/icons-vue'

useHead({
  title: 'ai-agent - Home',
  meta: [
    {
      name: 'description',
      content: 'ai-agent by Book offers AI Love Coach and Super Agent chat experiences.'
    },
    {
      name: 'keywords',
      content: 'ai-agent, Book, AI Love Coach, Super Agent, AI chat assistant, home'
    }
  ]
})

const router = useRouter()

const apps = [
  {
    title: 'AI Love Coach',
    description: 'Relationship and dating guidance with a calm, direct coaching style.',
    path: '/love-master',
    icon: ChatDotRound,
    tone: 'warm'
  },
  {
    title: 'Super Agent',
    description: 'General-purpose help for questions, decisions, and practical tasks.',
    path: '/super-agent',
    icon: MagicStick,
    tone: 'cool'
  }
]

const navigateTo = (path) => {
  router.push(path)
}
</script>

<style scoped>
.home-page {
  min-height: 100dvh;
  padding: clamp(28px, 5vw, 72px) clamp(18px, 5vw, 64px);
  color: var(--text-color-primary);
  background:
    linear-gradient(180deg, rgba(246, 248, 251, 0) 0%, var(--background-color-secondary) 100%),
    radial-gradient(circle at 18% 18%, rgba(42, 92, 170, 0.12), transparent 30%),
    var(--background-color);
}

.home-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(280px, 360px);
  gap: 28px;
  align-items: end;
  width: min(100%, 1120px);
  margin: 0 auto;
  padding-bottom: clamp(28px, 5vw, 56px);
  border-bottom: 1px solid var(--border-color-light);
}

.hero-copy {
  max-width: 720px;
}

.eyebrow {
  margin-bottom: 14px;
  color: var(--primary-color);
  font-size: 0.875rem;
  font-weight: 700;
  text-transform: uppercase;
}

h1 {
  margin: 0;
  color: var(--text-color-primary);
  font-size: clamp(3rem, 9vw, 7.5rem);
  font-weight: 760;
  line-height: 0.95;
}

.hero-text {
  max-width: 620px;
  margin-top: 22px;
  color: var(--text-color-secondary);
  font-size: clamp(1rem, 2vw, 1.25rem);
  line-height: 1.65;
}

.hero-panel {
  display: grid;
  gap: 22px;
  padding: 22px;
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-lg);
  background: var(--background-color-elevated);
  box-shadow: var(--shadow-md);
}

.status-row {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--text-color-secondary);
  font-weight: 600;
}

.status-icon {
  width: 22px;
  height: 22px;
  color: var(--secondary-color);
}

.status-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.status-grid div {
  display: grid;
  gap: 4px;
  padding: 14px;
  border-radius: var(--border-radius-lg);
  background: var(--background-color-secondary);
}

.status-grid strong {
  color: var(--text-color-primary);
  font-size: 1.35rem;
  line-height: 1;
}

.status-grid span {
  color: var(--text-color-secondary);
  font-size: 0.875rem;
}

.apps-section {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
  width: min(100%, 1120px);
  margin: clamp(28px, 5vw, 56px) auto 0;
  padding-bottom: 32px;
}

.app-card {
  display: grid;
  grid-template-columns: 58px minmax(0, 1fr) auto;
  gap: 18px;
  align-items: center;
  min-height: 148px;
  padding: 22px;
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-lg);
  color: inherit;
  text-align: left;
  background: var(--background-color-elevated);
  box-shadow: var(--shadow-sm);
  transition: border-color 180ms ease, box-shadow 180ms ease, transform 180ms ease;
}

.app-card:hover {
  transform: translateY(-2px);
  border-color: color-mix(in srgb, var(--primary-color) 42%, var(--border-color));
  box-shadow: var(--shadow-lg);
}

.app-card:focus-visible {
  outline: 3px solid color-mix(in srgb, var(--primary-color) 34%, transparent);
  outline-offset: 3px;
}

.app-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 58px;
  height: 58px;
  border-radius: var(--border-radius-lg);
}

.app-icon svg {
  width: 28px;
  height: 28px;
}

.app-icon.warm {
  color: #9f2943;
  background: #ffe7ec;
}

.app-icon.cool {
  color: #124c7d;
  background: #e2f1ff;
}

.app-copy {
  display: grid;
  gap: 8px;
  min-width: 0;
}

.app-title {
  color: var(--text-color-primary);
  font-size: 1.25rem;
  font-weight: 720;
  line-height: 1.2;
}

.app-desc {
  color: var(--text-color-secondary);
  font-size: 0.98rem;
  line-height: 1.55;
}

.open-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 44px;
  padding: 0 16px;
  border-radius: var(--border-radius-lg);
  color: var(--text-color-on-primary);
  font-weight: 700;
  background: var(--primary-color);
}

.open-action svg {
  width: 18px;
  height: 18px;
}

@media (max-width: 860px) {
  .home-hero,
  .apps-section {
    grid-template-columns: 1fr;
  }

  .hero-panel {
    max-width: 520px;
  }
}

@media (max-width: 560px) {
  .home-page {
    padding: 24px 14px;
  }

  .app-card {
    grid-template-columns: 52px minmax(0, 1fr);
    min-height: 0;
    padding: 18px;
  }

  .app-icon {
    width: 52px;
    height: 52px;
  }

  .open-action {
    grid-column: 1 / -1;
    width: 100%;
  }
}
</style>
