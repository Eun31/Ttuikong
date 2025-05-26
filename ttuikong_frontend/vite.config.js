import { fileURLToPath, URL } from "node:url";

import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import vueDevTools from "vite-plugin-vue-devtools";

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue(), vueDevTools()],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
  server: {
    host: "0.0.0.0",
    port: 5173,
    allowedHosts: [".ngrok-free.app"],
    proxy: {
      "/api": {
        target: "http://192.168.176.13:8080",
        changeOrigin: true,
        secure: false,
        logLevel: "debug", // 프록시 로그 확인용
      },
      "/flask": {
        target: "http://localhost:5001",
        changeOrigin: true,
        secure: false,
        logLevel: "debug",
        rewrite: (path) => path.replace(/^\/flask/, ""),
      },
    },
  },
});
