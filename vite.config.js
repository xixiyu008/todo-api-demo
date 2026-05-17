import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import path from "node:path";

export default defineConfig({
  root: path.resolve(__dirname, "frontend"),
  plugins: [vue()],
  build: {
    outDir: path.resolve(__dirname, "src/main/resources/static"),
    emptyOutDir: true
  }
});
