<template>
  <div class="home-page">
    <header class="home-header">
      <h1 class="home-title">HLH AI Agent</h1>
      <p class="home-desc">你的智能应用合集，选择一位助手开始对话</p>
    </header>

    <main class="card-grid">
      <router-link
        v-for="(app, index) in apps"
        :key="app.path"
        :to="app.path"
        class="app-card"
        :style="{ '--card-color': app.color, '--card-bg': app.bg, animationDelay: `${index * 0.12}s` }"
      >
        <div class="app-icon">
          <component :is="app.icon" />
        </div>
        <h2 class="app-title">{{ app.title }}</h2>
        <p class="app-desc">{{ app.desc }}</p>
        <span class="app-go">
          进入应用
          <icon-right />
        </span>
      </router-link>
    </main>

    <footer class="home-footer">Powered by Spring AI · HLH</footer>
  </div>
</template>

<script>
import { IconHeartFill, IconThunderbolt } from '@arco-design/web-vue/es/icon'

export default {
  name: 'HomeView',
  setup() {
    const apps = [
      {
        path: '/love',
        title: 'AI 恋爱大师',
        desc: '贴心的恋爱顾问，帮你分析情感问题、出谋划策，温柔陪伴每一次心动。',
        icon: IconHeartFill,
        color: '#ec6f8c',
        bg: '#fdeaef'
      },
      {
        path: '/manus',
        title: 'AI 超级智能体',
        desc: '全能任务助手，具备规划、搜索与工具调用能力，帮你搞定复杂任务。',
        icon: IconThunderbolt,
        color: '#4080ff',
        bg: '#e8f0fe'
      }
    ]
    return { apps }
  }
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 0 24px;
  background: linear-gradient(160deg, #f7f8fa 0%, #eef2fb 55%, #fdf1f4 100%);
}

.home-header {
  margin: 14vh auto 0;
  text-align: center;
  animation: rise-in 0.5s ease both;
}

.home-title {
  font-size: 40px;
  font-weight: 700;
  letter-spacing: 1px;
  background: linear-gradient(120deg, #4080ff, #ec6f8c);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.home-desc {
  margin-top: 14px;
  font-size: 15px;
  color: #8a91a3;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(260px, 340px));
  gap: 28px;
  justify-content: center;
  margin: 56px auto 0;
}

.app-card {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 32px 28px 26px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 18px;
  border: 1px solid rgba(150, 160, 180, 0.1);
  box-shadow: 0 2px 12px rgba(31, 41, 55, 0.04);
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease, border-color 0.3s ease;
  animation: rise-in 0.5s ease both;
}

.app-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(31, 41, 55, 0.1);
  border-color: var(--card-color);
}

.app-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  border-radius: 14px;
  font-size: 26px;
  color: var(--card-color);
  background: var(--card-bg);
  transition: transform 0.3s ease;
}

.app-card:hover .app-icon {
  transform: scale(1.08) rotate(-4deg);
}

.app-title {
  margin-top: 18px;
  font-size: 18px;
  font-weight: 600;
  color: #2b3245;
}

.app-desc {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.7;
  color: #8a91a3;
}

.app-go {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-top: 20px;
  font-size: 13px;
  color: var(--card-color);
  opacity: 0;
  transform: translateX(-6px);
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.app-card:hover .app-go {
  opacity: 1;
  transform: translateX(0);
}

.home-footer {
  margin: auto 0 0;
  padding: 28px 0;
  text-align: center;
  font-size: 12px;
  color: #b3b9c6;
}

@keyframes rise-in {
  from {
    opacity: 0;
    transform: translateY(18px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 720px) {
  .home-header {
    margin-top: 10vh;
  }

  .home-title {
    font-size: 30px;
  }

  .card-grid {
    grid-template-columns: 1fr;
    gap: 18px;
    margin-top: 40px;
  }
}
</style>
