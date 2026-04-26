import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { pinia } from './stores'
import { createHead } from '@vueuse/head'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import Toast from 'vue-toastification'
import 'vue-toastification/dist/index.css'
import './styles/index.scss'

// Create the app instance
const app = createApp(App)

// Create head manager
const head = createHead()

// Configure toast notifications
const toastOptions = {
  timeout: 3000,
  closeOnClick: true,
  pauseOnFocusLoss: true,
  pauseOnHover: true,
  draggable: true,
  draggablePercent: 0.6,
  showCloseButtonOnHover: false,
  hideProgressBar: false,
  closeButton: 'button',
  icon: true,
  rtl: false
}

// Register plugins
app.use(router)
app.use(pinia)
app.use(head)
app.use(ElementPlus)
app.use(Toast, toastOptions)

// Mount the app
app.mount('#app')
