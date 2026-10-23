import { createApp } from "vue";
import * as ElementPlusIcons from "@element-plus/icons-vue";
import "element-plus/theme-chalk/dark/css-vars.css";
import "@/styles/index.scss";
import "uno.css";
import App from "./App.vue";
import router from "./router";
import { store } from "./stores";
import { hasPermission } from "@/composables/useLoginUser";

const app = createApp(App);
app.directive("hasPerm", {
  mounted(el, binding) {
    if (!hasPermission(binding.value)) el.remove();
  },
});
Object.entries(ElementPlusIcons).forEach(([name, component]) =>
  app.component(name, component),
);
app.use(store);
app.use(router);
app.mount("#app");
