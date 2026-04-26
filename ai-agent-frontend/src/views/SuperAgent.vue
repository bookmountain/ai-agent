<template>
  <div class="super-agent-container">
    <div class="header">
      <div class="back-button" @click="goBack">Back</div>
      <h1 class="title">Super Agent</h1>
      <div class="header-actions"></div>
    </div>
    
    <div class="content-wrapper">
      <div class="chat-area">
        <ChatRoom 
          :messages="messages" 
          :connection-status="connectionStatus"
          ai-type="super"
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
import { chatWithManus } from '../api'

// Set page title and metadata
useHead({
  title: 'Super Agent - ai-agent',
  meta: [
    {
      name: 'description',
      content: 'Super Agent in ai-agent by Book is a general AI assistant for broad questions and practical guidance.'
    },
    {
      name: 'keywords',
      content: 'Super Agent, AI assistant, Book, ai-agent, general AI chat'
    }
  ]
})

const router = useRouter()
const messages = ref([])
const connectionStatus = ref('disconnected')
let eventSource = null

// Add a message to the list
const addMessage = (content, isUser, type = '') => {
  const newMessage = {
    content,
    isUser,
    type,
    time: new Date().getTime()
  }
  messages.value.push(newMessage)
  return newMessage
}

const appendToMessage = (message, content) => {
  if (!message || !content.trim()) return
  message.content = message.content ? `${message.content}\n\n${content}` : content
  message.time = new Date().getTime()
}

const stripStepPrefix = (content) => {
  return content.replace(/^Step\s+\d+\s*:\s*/i, '').trim()
}

const isReasoningEvent = (content) => {
  const normalized = stripStepPrefix(content)
  return /^Step\s+\d+\s*:/i.test(content)
    || /^Tool\w+\s+returned result/i.test(normalized)
    || normalized.startsWith('Tool')
    || normalized.startsWith('Complete thinking, no action needed.')
    || normalized === 'No tool calls to execute.'
}

const splitAgentEvent = (content) => {
  const blocks = content
    .split(/\n{2,}/)
    .map(block => block.trim())
    .filter(Boolean)

  if (blocks.length <= 1) {
    return isReasoningEvent(content)
      ? { reasoning: [stripStepPrefix(content)], answers: [] }
      : { reasoning: [], answers: [stripStepPrefix(content)] }
  }

  return blocks.reduce((result, block) => {
    if (isReasoningEvent(block)) {
      result.reasoning.push(stripStepPrefix(block))
    } else {
      result.answers.push(stripStepPrefix(block))
    }
    return result
  }, { reasoning: [], answers: [] })
}

// Send a message
const sendMessage = (message) => {
  addMessage(message, true, 'user-question')
  
  // Connect to SSE
  if (eventSource) {
    eventSource.close()
  }
  
  // Set the connection state
  connectionStatus.value = 'connecting'
  
  let reasoningMessage = null
  
  eventSource = chatWithManus(message)
  
  // Listen for SSE messages
  eventSource.onmessage = (event) => {
    const data = event.data?.trim()

    if (data === '[DONE]') {
      connectionStatus.value = 'disconnected'
      eventSource.close()
      return
    }

    if (!data) {
      return
    }

    const { reasoning, answers } = splitAgentEvent(data)

    if (reasoning.length > 0) {
      if (!reasoningMessage) {
        reasoningMessage = addMessage('', false, 'ai-reasoning')
      }
      reasoning.forEach(item => appendToMessage(reasoningMessage, item))
    }

    answers.forEach(answer => addMessage(answer, false, 'ai-final'))
  }
  
  // Listen for SSE errors
  eventSource.onerror = (error) => {
    console.error('SSE Error:', error)
    if (eventSource?.readyState === EventSource.CLOSED) {
      connectionStatus.value = 'disconnected'
      return
    }
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
  // Add the welcome message
  addMessage('Hi, I am Super Agent. I can help with a wide range of questions and practical tasks. What would you like to work on?', false)
})

// Close the SSE connection before the component is destroyed
onBeforeUnmount(() => {
  if (eventSource) {
    eventSource.close()
  }
})
</script>

<style scoped>
.super-agent-container {
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
  text-align: center;
  justify-self: center;
}

.header-actions {
  display: flex;
  justify-content: flex-end;
  justify-self: end;
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
  
  .chat-area {
    padding: 12px;
  }

  .header-actions {
    width: 100%;
    justify-self: stretch;
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
  
  .chat-area {
    padding: 8px;
  }
}
</style> 
