<template>
  <div class="layout-toolbar">
    <el-tooltip content="切换主题"
      ><button
        class="layout-toolbar__item"
        aria-label="切换主题"
        @click="
          appStore.resolvedTheme =
            appStore.resolvedTheme === 'dark' ? 'light' : 'dark'
        "
      >
        <el-icon
          ><Sunny v-if="appStore.resolvedTheme === 'light'" /><Moon v-else
        /></el-icon></button
    ></el-tooltip>
    <el-tooltip content="全屏"
      ><button class="layout-toolbar__item" aria-label="全屏">
        <Fullscreen /></button
    ></el-tooltip>
    <div class="layout-toolbar__item"><SizeSelect /></div>
    <el-dropdown @command="logout"><span class="layout-user"
      ><el-avatar :size="28" style="background: #e8f1ff; color: #165dff"
        ><User /></el-avatar
      ><span>{{ loginUser?.name || '当前用户' }}</span></span>
      <template #dropdown><el-dropdown-menu><el-dropdown-item command="logout">退出登录</el-dropdown-item></el-dropdown-menu></template>
    </el-dropdown>
    <el-tooltip content="布局设置"
      ><button
        class="layout-toolbar__item"
        aria-label="布局设置"
        @click="settingsVisible = true"
      >
        <el-icon><Setting /></el-icon></button
    ></el-tooltip>
  </div>
  <el-drawer v-model="settingsVisible" title="布局设置" size="300px">
    <el-form label-position="left" label-width="160px">
      <el-form-item label="显示标签页"
        ><el-switch v-model="appStore.showTagsView"
      /></el-form-item>
      <el-form-item label="显示 Logo"
        ><el-switch v-model="appStore.showAppLogo"
      /></el-form-item>
      <el-form-item label="深色主题"
        ><el-switch
          :model-value="appStore.resolvedTheme === 'dark'"
          @change="appStore.resolvedTheme = $event ? 'dark' : 'light'"
      /></el-form-item>
      <el-form-item label="经典蓝侧边栏"
        ><el-switch
          :model-value="appStore.sidebarColorScheme === 'classic-blue'"
          @change="
            appStore.sidebarColorScheme = $event
              ? 'classic-blue'
              : 'minimal-white'
          "
      /></el-form-item>
      <el-form-item label="标签风格"
        ><el-radio-group v-model="appStore.tagsViewStyle"
          ><el-radio-button value="card">卡片</el-radio-button
          ><el-radio-button value="line">线条</el-radio-button></el-radio-group
        ></el-form-item
      >
    </el-form>
  </el-drawer>
</template>
<script setup>
import { useAppStore } from "@/stores";
import { loginUser } from "@/composables/useLoginUser";
import { doGet } from "@/utils/ajax";
import { clearToken, confirmBox } from "@/utils/common";
async function logout() {
  try { await confirmBox("确定退出登录吗？"); } catch { return; }
  try { await doGet("/logout"); }
  finally { clearToken(); loginUser.value = null; location.replace("/login"); }
}
const appStore = useAppStore();
const settingsVisible = ref(false);
</script>
<style lang="scss" scoped>
.layout-toolbar {
  display: flex;
  align-items: center;
  gap: 6px;
}
.layout-toolbar__item {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  height: 32px;
  padding: 0 6px;
  border: 0;
  background: transparent;
  color: var(--el-text-color-secondary);
  cursor: pointer;
  border-radius: 6px;
  font-size: 16px;
  &:hover {
    color: var(--el-color-primary);
    background: var(--el-fill-color-light);
  }
}
.layout-user {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 8px;
  font-size: 13px;
  color: var(--el-text-color-regular);
}
@media (max-width: 600px) {
  .layout-user > span {
    display: none;
  }
}
</style>
