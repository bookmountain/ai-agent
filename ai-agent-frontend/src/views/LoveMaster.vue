<template>
  <div class="love-master-container">
    <div class="header">
      <div class="back-button" @click="goBack">Back</div>
      <h1 class="title">AI Love Coach</h1>
      <div class="header-actions">
        <div class="chat-id">Session ID: {{ chatId }}</div>
      </div>
    </div>
    
    <div class="content-wrapper">
      <div class="chat-area">
        <ChatRoom 
          :messages="messages" 
          :connection-status="connectionStatus"
          ai-type="love"
          @send-message="sendMessage"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useHead } from '@vueuse/head'
import ChatRoom from '../components/ChatRoom.vue'
import { chatWithLoveApp } from '../api'

// Set page title and metadata
useHead({
  title: 'AI Love Coach - ai-agent',
  meta: [
    {
      name: 'description',
      content: 'AI Love Coach in ai-agent by Book helps with dating questions and relationship advice.'
    },
    {
      name: 'keywords',
      content: 'AI Love Coach, relationship advice, dating chat, Book, ai-agent'
    }
  ]
})

const router = useRouter()
const messages = ref([])
const chatId = ref('')
const connectionStatus = ref('disconnected')
let eventSource = null

// Generate a random session ID
const generateChatId = () => {
  return 'love_' + Math.random().toString(36).substring(2, 10)
}

// Add a message to the list
const addMessage = (content, isUser) => {
  messages.value.push({
    content,
    isUser,
    time: new Date().getTime()
  })
}

// Send a message
const sendMessage = (message) => {
  addMessage(message, true)
  
  // Connect to SSE
  if (eventSource) {
    eventSource.close()
  }
  
  // Create an empty AI reply entry
  const aiMessageIndex = messages.value.length
  addMessage('', false)
  
  connectionStatus.value = 'connecting'
  eventSource = chatWithLoveApp(message, chatId.value)
  
  // Listen for SSE messages
  eventSource.onmessage = (event) => {
    const data = event.data
    if (data && data !== '[DONE]') {
      // Update the latest AI message instead of creating a new one
      if (aiMessageIndex < messages.value.length) {
        messages.value[aiMessageIndex].content += data
      }
    }
    
    if (data === '[DONE]') {
      connectionStatus.value = 'disconnected'
      eventSource.close()
    }
  }
  
  // Listen for SSE errors
  eventSource.onerror = (error) => {
    console.error('SSE Error:', error)
    connectionStatus.value = 'error'
    eventSource.close()
  }
}

// Return to the home page
const goBack = () => {
  router.push('/')
}

// Add a welcome message on page load
onMounted(() => {
  // Generate the chat ID
  chatId.value = generateChatId()
  
  // Add the welcome message
  addMessage('Welcome to AI Love Coach. Tell me what is going on, and I will do my best to help with practical relationship advice.', false)
})

// Close the SSE connection before the component is destroyed
onBeforeUnmount(() => {
  if (eventSource) {
    eventSource.close()
  }
})
</script>

<style scoped>
.love-master-container {
  display: flex;
  flex-direction: column;
  height: 100dvh;
  background-color: var(--background-color-secondary);
  overflow: hidden;
}

.header {
  display: grid;
  grid-template-columns: minmax(120px, 1fr) auto minmax(120px, 1fr);
  align-items: center;
  flex: 0 0 auto;
  padding: 14px 24px;
  border-bottom: 1px solid var(--border-color-light);
  background-color: var(--background-color-elevated);
  color: var(--text-color-primary);
  z-index: 10;
}

.back-button {
  min-height: 44px;
  padding: 0 12px;
  border-radius: var(--border-radius-lg);
  color: var(--primary-color);
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: background-color 0.2s ease;
  justify-self: start;
}

.back-button:hover {
  background-color: var(--background-color-secondary);
}

.back-button:before {
  content: '←';
  margin-right: 8px;
}

.title {
  font-size: 18px;
  font-weight: 760;
  margin: 0;
  justify-self: center;
}

.chat-id {
  color: var(--text-color-secondary);
  font-size: 13px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  flex: 1;
  width: min(100%, 980px);
  min-height: 0;
  margin: 0 auto;
}

.chat-area {
  display: flex;
  flex: 1;
  min-height: 0;
  padding: 18px 20px 22px;
  overflow: visible;
  position: relative;
}

/* Responsive layout */
@media (max-width: 768px) {
  .header {
    grid-template-columns: 1fr;
    gap: 10px;
    padding: 12px 16px;
  }
  
  .title {
    font-size: 18px;
  }
  
  .chat-id {
    font-size: 12px;
  }

  .header-actions {
    width: 100%;
    justify-content: center;
    gap: 8px;
  }
  
  .chat-area {
    padding: 12px;
  }
}

@media (max-width: 480px) {
  .header {
    padding: 10px 12px;
  }
  
  .back-button {
    font-size: 14px;
  }
  
  .title {
    font-size: 16px;
  }
  
  .chat-id {
    display: none;
  }
  
  .chat-area {
    padding: 8px;
  }
}
</style> 
