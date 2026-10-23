<template>
  <div
    class="hamburger-wrapper"
    role="button"
    tabindex="0"
    aria-label="切换侧边栏"
    @click="toggleClick"
    @keydown.enter="toggleClick"
  >
    <div
      :class="[
        'i-svg:collapse',
        { hamburger: true, 'is-active': isActive },
        hamburgerClass,
      ]"
    />
  </div>
</template>

<script setup>
import { useAppStore } from "@/stores";
import { ThemeMode, SidebarColor, LayoutMode } from "@/enums/settings";
/**
 * 侧边栏折叠按钮
 */
defineProps({
  isActive: { type: Boolean, required: true },
});
/**
 * 点击时上抛折叠切换
 */
const emit = defineEmits(["toggleClick"]);
const settingsStore = useAppStore();
const layout = computed(() => "left");
const hamburgerClass = computed(() => {
  if (settingsStore.resolvedTheme === ThemeMode.DARK) {
    return "hamburger--white";
  }
  if (
    layout.value === LayoutMode.MIX &&
    settingsStore.sidebarColorScheme === SidebarColor.CLASSIC_BLUE
  ) {
    return "hamburger--white";
  }
  return "";
});
/**
 * 触发侧边栏折叠切换
 */
function toggleClick() {
  emit("toggleClick");
}
</script>

<style scoped lang="scss">
.hamburger-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 100%;
  padding: 0;
  color: var(--el-text-color-regular);
  cursor: pointer;

  .hamburger {
    width: 16px;
    height: 16px;
    font-size: 16px;
    vertical-align: middle;
    color: currentcolor;
    background-color: currentcolor;
    transform: scaleX(-1);
    transition:
      color 0.16s,
      transform 0.3s ease;

    &--white {
      color: #fff;
    }

    &.is-active {
      transform: scaleX(1);
    }
  }

  &:hover {
    color: var(--el-color-primary);
  }
}
</style>
