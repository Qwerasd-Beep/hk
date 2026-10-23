<template>
  <section class="layout-content" :style="{ height: appMainHeight }">
    <router-view>
      <template #default="{ Component, route }">
        <transition :name="transitionName" mode="out-in">
          <keep-alive :include="cachedViews">
            <component
              :is="currentComponent(Component, route)"
              :key="route.fullPath"
              v-if="isRender"
            />
          </keep-alive>
        </transition>
      </template>
    </router-view>

    <el-backtop target=".layout-content">
      <div class="i-svg:backtop w-6 h-6" />
    </el-backtop>
  </section>
</template>

<script setup>
import { useAppStore, useTagsViewStore } from "@/stores";
import variables from "@/styles/variables.module.scss";
import Error404 from "@/views/error/404.vue";
const isRender = ref(true);
const routeForReload = useRoute();
const tagsForReload = useTagsViewStore();
provide("reload", async () => {
  await tagsForReload.delCachedView(routeForReload);
  wrapperMap.delete(routeForReload.fullPath);
  isRender.value = false;
  await nextTick();
  isRender.value = true;
});
const { cachedViews } = toRefs(useTagsViewStore());
const settingsStore = useAppStore();
const wrapperMap = new Map();
/**
 * 解析当前路由要渲染的组件
 */
const currentComponent = (component, route) => {
  if (!component) return;
  const { fullPath: componentName } = route;
  let wrapper = wrapperMap.get(componentName);
  if (!wrapper) {
    wrapper = {
      name: componentName,
      render: () => {
        try {
          // 原业务页多为多个根节点，增加一层容器以兼容布局切换动画。
          return componentName.startsWith("/main/")
            ? h("div", { class: "business-page" }, [h(component)])
            : h(component);
        } catch (error) {
          console.error(
            `Error rendering component for route: ${componentName}`,
            error,
          );
          return h(Error404);
        }
      },
    };
    wrapperMap.set(componentName, wrapper);
  }
  if (wrapperMap.size > 100) {
    const firstKey = wrapperMap.keys().next().value;
    if (firstKey) {
      wrapperMap.delete(firstKey);
    }
  }
  return h(wrapper);
};
const appMainHeight = computed(() => {
  if (settingsStore.showTagsView) {
    return `calc(100vh - ${variables["navbar-height"]} - ${variables["tags-view-height"]})`;
  } else {
    return `calc(100vh - ${variables["navbar-height"]})`;
  }
});
const transitionName = computed(() => {
  return settingsStore.pageSwitchingAnimation ?? "";
});
</script>

<style lang="scss" scoped>
.business-page {
  min-width: 0;
  padding: 16px;
}
.layout-content {
  position: relative;
  overflow-y: auto;
  background-color: var(--page-bg);

  /* fade */
  .fade-enter-active,
  .fade-leave-active {
    transition: opacity 0.3s ease-in-out;
  }
  .fade-enter-from,
  .fade-leave-to {
    opacity: 0;
  }

  /* fade-slide */
  .fade-slide-leave-active,
  .fade-slide-enter-active {
    transition: all 0.3s;
  }
  .fade-slide-enter-from {
    opacity: 0;
    transform: translateX(-30px);
  }
  .fade-slide-leave-to {
    opacity: 0;
    transform: translateX(30px);
  }

  /* fade-scale */
  .fade-scale-leave-active,
  .fade-scale-enter-active {
    transition: all 0.28s;
  }
  .fade-scale-enter-from {
    opacity: 0;
    transform: scale(1.2);
  }
  .fade-scale-leave-to {
    opacity: 0;
    transform: scale(0.8);
  }
}
</style>
