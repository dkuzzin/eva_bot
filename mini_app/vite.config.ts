import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],

  server: {
    proxy: {
      '/api': {
        //target: 'https://api.chernushka.fun',
        target: 'http://192.168.68.59:8080',
        changeOrigin: true,
      },
    },
  },
})