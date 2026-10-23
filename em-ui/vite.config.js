import { fileURLToPath, URL } from "node:url";

import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import AutoImport from "unplugin-auto-import/vite";
import Components from "unplugin-vue-components/vite";
import { ElementPlusResolver } from "unplugin-vue-components/resolvers";
import UnoCSS from "unocss/vite";

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    UnoCSS(),
    AutoImport({
      imports: ["vue", "vue-router", "pinia", "@vueuse/core"],
      resolvers: [ElementPlusResolver({ importStyle: "sass" })],
      vueTemplate: true,
      dts: false,
    }),
    Components({
      dirs: ["src/components", "src/layouts/components"],
      resolvers: [ElementPlusResolver({ importStyle: "sass" })],
      dts: false,
    }),
  ],
  css: {
    preprocessorOptions: {
      scss: { additionalData: '@use "@/styles/variables.scss" as *;' },
    },
  },
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
  server: {
    host: "0.0.0.0",
    port: 7070,
    strictPort: true,
    open: false,
  },
});
