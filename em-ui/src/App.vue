<template>
  <el-config-provider :locale="zhCn" :size="appStore.size">
    <router-view />
  </el-config-provider>
</template>

<script setup>
import zhCn from "element-plus/es/locale/lang/zh-cn";
import { useAppStore } from "@/stores";
const appStore = useAppStore();
</script>
