<template>
  <div class="home-page">
    <div class="home-content">
      <img :src="appIcon" alt="HLH AI Agent" class="home-app-icon" />
      <h1 class="home-title">欢迎使用 HLH AI Agent</h1>
      <p class="home-desc">请从左侧菜单选择一个 AI 助手开始对话或者点击下方按钮</p>

      <div class="card-list">
        <router-link
          v-for="(app, index) in apps"
          :key="app.path"
          :to="app.path"
          class="app-card"
          :style="{
            '--card-color': app.color,
            '--card-icon-bg': app.iconBg,
            animationDelay: `${index * 0.12}s`
          }"
        >
          <div class="card-icon">
            <component :is="app.icon" />
          </div>
          <h2 class="card-title">{{ app.title }}</h2>
          <p class="card-tag">{{ app.tag }}</p>
          <p class="card-desc">{{ app.desc }}</p>
          <span class="card-go">
            进入对话
            <icon-right />
          </span>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { IconHeartFill, IconThunderbolt } from '@arco-design/web-vue/es/icon'
import appIcon from '@/assets/app-icon.png'

export default {
  name: 'HomeView',
  setup() {
    const apps = [
      {
        path: '/love',
        title: 'AI 恋爱大师',
        tag: '贴心恋爱顾问 | 懂你的情感引路人',
        desc: '从单身追求、恋人相处到婚后家庭关系，耐心倾听、分析你的感情心事，定制专属情感解决方案。',
        icon: IconHeartFill,
        color: '#ec6f8c',
        iconBg: '#fdeaef'
      },
      {
        path: '/manus',
        title: 'AI 超级智能体',
        tag: '全能任务助手',
        desc: '具备回答问题、自主规划、网络搜索等各种能力，帮你搞定复杂任务。',
        icon: IconThunderbolt,
        color: '#4080ff',
        iconBg: '#e8f0fe'
      }
    ]
    return { apps, appIcon }
  }
}
</script>

<style scoped>
.home-page {
  height: 100%;
  overflow-y: auto;
  display: flex;
  justify-content: center;
  background: linear-gradient(160deg, #f7f9fc 0%, #eef2fb 55%, #fdf1f4 100%);
}

.home-content {
  width: 100%;
  max-width: 760px;
  margin: auto;
  padding: 48px 24px;
  text-align: center;
}

.home-app-icon {
  width: 84px;
  height: 84px;
  border-radius: 22px;
  box-shadow: 0 8px 24px rgba(64, 128, 255, 0.28);
  animation: rise-in 0.5s ease both;
}

.home-title {
  margin-top: 20px;
  font-size: 30px;
  font-weight: 700;
  color: #2b3245;
  animation: rise-in 0.5s ease 0.06s both;
}

.home-desc {
  margin-top: 10px;
  font-size: 14px;
  color: #8a91a3;
  animation: rise-in 0.5s ease 0.12s both;
}

.card-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 26px;
  margin-top: 40px;
}

.app-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  aspect-ratio: 1 / 1;
  padding: 30px 28px;
  text-align: center;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 20px;
  box-shadow: 0 2px 12px rgba(31, 41, 55, 0.04);
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease, border-color 0.3s ease;
  animation: rise-in 0.5s ease both;
}

.app-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 14px 32px rgba(31, 41, 55, 0.12);
  border-color: var(--card-color);
}

.card-icon {
  flex: none;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 66px;
  height: 66px;
  border-radius: 18px;
  font-size: 32px;
  color: var(--card-color);
  background: var(--card-icon-bg);
  transition: transform 0.3s ease;
}

.app-card:hover .card-icon {
  transform: scale(1.1) rotate(-4deg);
}

.card-title {
  margin-top: 18px;
  font-size: 18px;
  font-weight: 600;
  color: #2b3245;
}

.card-tag {
  margin-top: 6px;
  font-size: 13px;
  font-weight: 500;
  color: var(--card-color);
}

.card-desc {
  margin-top: 10px;
  font-size: 13px;
  line-height: 1.8;
  color: #8a91a3;
}

.card-go {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-top: 18px;
  font-size: 13px;
  color: var(--card-color);
  opacity: 0;
  transform: translateY(6px);
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.app-card:hover .card-go {
  opacity: 1;
  transform: translateY(0);
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

@media (max-width: 640px) {
  .home-content {
    padding: 32px 16px;
  }

  .home-title {
    font-size: 24px;
  }

  .card-list {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .app-card {
    aspect-ratio: auto;
    padding: 26px 20px;
  }

  .card-go {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
