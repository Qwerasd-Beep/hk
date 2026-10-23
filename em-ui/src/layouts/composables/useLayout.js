import { computed } from "vue";
import { useAppStore } from "@/stores";
import { constantRoutes } from "@/router";
import { loginUser, toSidebarMenus } from "@/composables/useLoginUser";

export function useLayout() {
  const appStore = useAppStore();
  return {
    isMobile: computed(() => appStore.device === "mobile"),
    isSidebarOpen: computed(() => appStore.sidebar.opened),
    showTagsView: computed(() => appStore.showTagsView),
    showLogo: computed(() => appStore.showAppLogo),
    routes: computed(() => [
      constantRoutes.find(route => route.path === "/"),
      ...toSidebarMenus(loginUser.value?.menuList),
    ]),
    layoutClass: computed(() => ({
      "is-sidebar-collapsed": !appStore.sidebar.opened,
      "is-sidebar-open": appStore.sidebar.opened,
      "is-mobile": appStore.device === "mobile",
      "layout--left": true,
    })),
    closeSidebar: () => appStore.closeSidebar(),
  };
}
