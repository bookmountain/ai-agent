import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: {
      title: 'Home - ai-agent',
      description: 'ai-agent by Book offers AI Love Coach and Super Agent chat experiences.'
    }
  },
  {
    path: '/love-master',
    name: 'LoveMaster',
    component: () => import('../views/LoveMaster.vue'),
    meta: {
      title: 'AI Love Coach - ai-agent',
      description: 'AI Love Coach in ai-agent by Book helps with dating questions and relationship advice.'
    }
  },
  {
    path: '/super-agent',
    name: 'SuperAgent',
    component: () => import('../views/SuperAgent.vue'),
    meta: {
      title: 'Super Agent - ai-agent',
      description: 'Super Agent in ai-agent by Book is a general AI assistant for broad questions and practical guidance.'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Global navigation guard for document titles
router.beforeEach((to, from, next) => {
  // Set the page title
  if (to.meta.title) {
    document.title = to.meta.title
  }
  next()
})

export default router 
