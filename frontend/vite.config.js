import { defineConfig } from 'vite'; import react from '@vitejs/plugin-react';
export default defineConfig({plugins:[react()],server:{host:'0.0.0.0',port:5173},build:{chunkSizeWarningLimit:650,rollupOptions:{output:{manualChunks:{vendor:['react','react-dom','react-router-dom'],charts:['recharts'],motion:['framer-motion']}}}}});
