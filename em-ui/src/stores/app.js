import { defineStore } from "pinia";
import { ref, reactive, watch, computed } from "vue";
import { useStorage } from "@vueuse/core";

// Only visual Layout state is stored. Demo records stay inside their own components.
export const useAppStore = defineStore("app", () => {
  const sidebarOpen = useStorage("em-ui:sidebar-open", true);
  const sidebar = reactive({
    opened: sidebarOpen.value,
    withoutAnimation: false,
  });
  const device = ref("desktop");
  const size = useStorage("em-ui:size", "default");
  const showTagsView = useStorage("em-ui:tags-visible", true);
  const showAppLogo = useStorage("em-ui:logo-visible", true);
  const tagsViewStyle = useStorage("em-ui:tags-style", "card");
  const resolvedTheme = useStorage("em-ui:theme", "light");
  const sidebarColorScheme = useStorage("em-ui:sidebar-color", "minimal-white");
  const contentFullscreen = ref(false);
  const refreshKey = ref(0);
  const themeColors = computed(() => ({ primary: "#165dff" }));
  const pageSwitchingAnimation = "fade";
  watch(
    () => sidebar.opened,
    (opened) => {
      sidebarOpen.value = opened;
    },
  );
  watch(
    [resolvedTheme, sidebarColorScheme],
    ([theme, color]) => {
      document.documentElement.classList.toggle("dark", theme === "dark");
      document.documentElement.classList.toggle(
        "sidebar-color-blue",
        color === "classic-blue",
      );
    },
    { immediate: true },
  );
  function toggleSidebar() {
    sidebar.opened = !sidebar.opened;
  }
  function closeSidebar() {
    sidebar.opened = false;
  }
  function openSidebar() {
    sidebar.opened = true;
  }
  function toggleDevice(value) {
    device.value = value;
  }
  function changeSize(value) {
    size.value = value;
  }
  function toggleContentFullscreen() {
    contentFullscreen.value = !contentFullscreen.value;
  }
  return {
    sidebar,
    device,
    size,
    showTagsView,
    showAppLogo,
    tagsViewStyle,
    resolvedTheme,
    sidebarColorScheme,
    contentFullscreen,
    themeColors,
    pageSwitchingAnimation,
    refreshKey,
    toggleSidebar,
    closeSidebar,
    openSidebar,
    toggleDevice,
    changeSize,
    toggleContentFullscreen,
  };
});
