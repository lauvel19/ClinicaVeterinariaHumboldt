import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  esbuild: {
    logOverride: { "this-is-undefined-in-esm": "silent" }
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    host: '0.0.0.0',
    port: 5173,
    strictPort: false,
    open: false,
  },
  build: {
    sourcemap: false,
    minify: true,
    commonjsOptions: {
      transformMixedEsModules: true,
    }
  },
})
