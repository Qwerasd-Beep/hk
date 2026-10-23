<template>
  <ErrorPage
    status-code="404"
    label="页面未找到"
    title="抱歉，访问的页面不存在"
    description="页面地址可能已变更或被移除，请检查地址后重试"
    variant="missing"
  >
    <template #actions>
      <el-button type="primary" :icon="House" @click="goHome"
        >返回首页</el-button
      >
      <el-button :icon="Refresh" @click="reload">重新加载</el-button>
    </template>
  </ErrorPage>
</template>

<script setup>
import ErrorPage from "./components/ErrorPage.vue";
import { House, Refresh } from "@element-plus/icons-vue";
defineOptions({ name: "Page404" });
const router = useRouter();
/**
 * 回到静态首页
 */
function goHome() {
  router.push("/");
}
/**
 * 重新加载当前页面
 */
function reload() {
  window.location.reload();
}
</script>
