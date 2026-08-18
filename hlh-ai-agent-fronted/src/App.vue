<template>
  <div class="app-shell">
    <AppSidebar />
    <main class="app-main">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script>
import AppSidebar from '@/components/AppSidebar.vue'

export default {
  name: 'App',
  components: { AppSidebar }
}
</script>

<style>
#app {
  width: 100%;
  min-height: 100vh;
}

.app-shell {
  display: flex;
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

.app-main {
  flex: 1;
  min-width: 0;
  height: 100vh;
  overflow: hidden;
  background: #f7f8fa;
}

/* 页面切换过渡动画 */
.page-enter-active,
.page-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
