<template>
  <aside class="sidebar">
    <div class="sidebar-logo">
      <img :src="appIcon" alt="HLH AI Agent" class="logo-img" />
      <span class="logo-text">HLH AI Agent</span>
    </div>

    <nav class="sidebar-menu">
      <router-link
        v-for="item in menus"
        :key="item.path"
        :to="item.path"
        class="menu-item"
        :style="{ '--item-color': item.color, '--item-bg': item.bg }"
      >
        <span class="menu-icon">
          <component :is="item.icon" />
        </span>
        <span class="menu-text">{{ item.label }}</span>
      </router-link>
    </nav>

    <div class="sidebar-footer">Powered by Spring AI</div>
  </aside>
</template>

<script>
import { IconHome, IconHeartFill, IconThunderbolt } from '@arco-design/web-vue/es/icon'
import appIcon from '@/assets/app-icon.png'

export default {
  name: 'AppSidebar',
  setup() {
    const menus = [
      { path: '/', label: '主页', icon: IconHome, color: '#4080ff', bg: '#e8f0fe' },
      { path: '/love', label: 'AI 恋爱大师', icon: IconHeartFill, color: '#ec6f8c', bg: '#fdeaef' },
      { path: '/manus', label: 'AI 超级智能体', icon: IconThunderbolt, color: '#4080ff', bg: '#e8f0fe' }
    ]
    return { menus, appIcon }
  }
}
</script>

<style scoped>
.sidebar {
  flex: none;
  display: flex;
  flex-direction: column;
  width: 220px;
  height: 100vh;
  background: #ffffff;
  border-right: 1px solid rgba(150, 160, 180, 0.12);
  z-index: 10;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 18px 16px;
}

.logo-img {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(64, 128, 255, 0.25);
}

.logo-text {
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.5px;
  background: linear-gradient(120deg, #4080ff, #7a6bff);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  white-space: nowrap;
}

.sidebar-menu {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 8px 12px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 10px;
  font-size: 14px;
  color: #5a6172;
  transition: background 0.25s ease, color 0.25s ease;
}

.menu-item:hover {
  background: rgba(150, 160, 180, 0.1);
}

.menu-icon {
  flex: none;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: 8px;
  font-size: 16px;
  color: #8a91a3;
  background: rgba(150, 160, 180, 0.1);
  transition: background 0.25s ease, color 0.25s ease;
}

.menu-text {
  white-space: nowrap;
}

/* 选中态：使用各菜单项自身的主题色 */
.menu-item.router-link-exact-active {
  color: var(--item-color);
  background: var(--item-bg);
  font-weight: 600;
}

.menu-item.router-link-exact-active .menu-icon {
  color: var(--item-color);
  background: rgba(255, 255, 255, 0.75);
}

.sidebar-footer {
  padding: 14px 18px;
  font-size: 12px;
  color: #c0c5d1;
  white-space: nowrap;
}

/* 窄屏：收起为纯图标导航 */
@media (max-width: 860px) {
  .sidebar {
    width: 64px;
  }

  .sidebar-logo {
    justify-content: center;
    padding: 16px 8px 12px;
  }

  .logo-text,
  .menu-text,
  .sidebar-footer {
    display: none;
  }

  .sidebar-menu {
    padding: 8px;
  }

  .menu-item {
    justify-content: center;
    padding: 10px 0;
  }
}
</style>
