import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'
import { fileURLToPath } from 'url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

const SRC_DIR = path.resolve(__dirname, 'src/main/resources/easya11y/webresources-src')
const DIST_DIR = path.resolve(__dirname, 'src/main/resources/easya11y/webresources')

// https://vitejs.dev/config/
// Custom plugin to handle dynamic loading
const dynamicLoadingPlugin = () => {
  return {
    name: 'dynamic-loading-plugin',
    transformIndexHtml: {
      order: 'post',
      handler(html, { filename }) {
        if (process.env.NODE_ENV === 'production') {
          // Extract the main script name from the HTML
          const scriptMatch = html.match(/<script type="module"[^>]*src="([^"]*\.js)"[^>]*><\/script>/);
          const mainScript = scriptMatch ? scriptMatch[1].split('/').pop() : '';
          
          // Build the dynamic loader script
          const dynamicLoader = `
    <script>
        // Dynamically load resources with correct context path
        (function() {
            // Get context path from URL parameter or pathname
            var urlParams = new URLSearchParams(window.location.search);
            var contextPath = urlParams.get('contextPath') || '';
            
            if (!contextPath) {
                var pathname = window.location.pathname;
                var resourcesIndex = pathname.indexOf('/.resources/');
                if (resourcesIndex > 0) {
                    contextPath = pathname.substring(0, resourcesIndex);
                }
            }
            
            // Load resources with correct paths
            var resourceBase = contextPath + '/.resources/easya11y/webresources/';
            
            // Load CSS
            var css = document.createElement('link');
            css.rel = 'stylesheet';
            css.href = resourceBase + 'css/globals.css';
            document.head.appendChild(css);
            
            // Load main script
            var script = document.createElement('script');
            script.type = 'module';
            script.crossOrigin = true;
            script.src = resourceBase + 'js/${mainScript}';
            document.head.appendChild(script);
        })();
    </script>`;
          
          // Remove all Vite-injected scripts and styles
          html = html.replace(/<script type="module"[^>]*src="[^"]*\.js"[^>]*><\/script>/g, '')
                     .replace(/<link rel="modulepreload"[^>]*>/g, '')
                     .replace(/<link rel="stylesheet"[^>]*href="[^"]*\.css"[^>]*>/g, '');
          
          // Add our dynamic loader before the closing head tag
          html = html.replace('</head>', dynamicLoader + '\n</head>');
          
          return html;
        }
        return html;
      }
    }
  };
};

export default defineConfig({
  plugins: [react(), dynamicLoadingPlugin()],
  root: SRC_DIR,
  publicDir: path.join(SRC_DIR, 'public'),
  base: process.env.NODE_ENV === 'production' ? '/.resources/easya11y/webresources/' : '/',
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
        target: process.env.MAGNOLIA_URL || 'http://localhost:8080/magnoliaAuthor',
        changeOrigin: true,
      }
    }
  }
})