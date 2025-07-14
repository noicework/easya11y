import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'
import { fileURLToPath } from 'url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

const SRC_DIR = path.resolve(__dirname, 'src/main/resources/easya11y/webresources-src')
const DIST_DIR = path.resolve(__dirname, 'src/main/resources/easya11y/webresources')

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  root: SRC_DIR,
  publicDir: path.join(SRC_DIR, 'public'),
  base: process.env.NODE_ENV === 'production' ? '/magnoliaAuthor/.resources/easya11y/webresources/' : '/',
  build: {
    outDir: DIST_DIR,
    emptyOutDir: false, // Don't clean output to preserve other files
    rollupOptions: {
      input: {
        'accessibility-checker': path.join(SRC_DIR, 'accessibility-checker.html'),
        'accessibility-scan-dialog': path.join(SRC_DIR, 'accessibility-scan-dialog.html'),
        'configuration': path.join(SRC_DIR, 'configuration.html'),
      },
      output: {
        entryFileNames: 'js/[name].js',
        chunkFileNames: 'js/[name]-[hash].js',
        assetFileNames: (assetInfo) => {
          if (assetInfo.name?.endsWith('.css')) {
            return 'css/[name][extname]'
          }
          return 'assets/[name]-[hash][extname]'
        }
      }
    },
    cssMinify: true,
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true
      }
    }
  },
  resolve: {
    alias: {
      '@': SRC_DIR,
      '@components': path.join(SRC_DIR, 'js/components'),
      '@hooks': path.join(SRC_DIR, 'js/hooks'),
      '@lib': path.join(SRC_DIR, 'js/lib'),
      '@services': path.join(SRC_DIR, 'js/services'),
      '@types': path.join(SRC_DIR, 'js/types'),
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/.rest': {
        target: 'http://localhost:8080/magnoliaAuthor',
        changeOrigin: true,
      }
    }
  }
})