import react from '@vitejs/plugin-react'
import { fileURLToPath } from 'node:url'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],

  envDir: ".env/",

    resolve: {
    // Array form with anchored regexes — required for Sass `@use`/`@import`
    // to resolve aliases correctly on Linux/macOS (see vitejs/vite#20292, #21356).
    alias: [
      {
        find: /^@\/(.*)/,
        replacement: fileURLToPath(new URL("./src/", import.meta.url)) + "$1",
      },
      {
        find: /^@lib\/(.*)/,
        replacement: fileURLToPath(new URL("./src/lib/", import.meta.url)) + "$1",
      },
      {
        find: /^@features\/(.*)/,
        replacement:
          fileURLToPath(new URL("./src/features/", import.meta.url)) + "$1",
      },
      {
        find: /^@components\/(.*)/,
        replacement:
          fileURLToPath(new URL("./src/components/", import.meta.url)) + "$1",
      },
      {
        find: /^@styles\/(.*)/,
        replacement:
          fileURLToPath(new URL("./src/styles/", import.meta.url)) + "$1",
      },
    ],
  },


})
