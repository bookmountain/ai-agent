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
  messages.value.push({
    content,
    isUser,
    type,
    time: new Date().getTime()
  })
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
  
  // Temporary state
  let messageBuffer = []; // Buffer SSE message chunks
  let lastBubbleTime = Date.now(); // Timestamp of the previous bubble
  let isFirstResponse = true; // Whether this is the first reply
  
  const sentenceEndPunctuation = ['.', '!', '?', '。', '！', '？', '…']; // Sentence-ending punctuation
  const minBubbleInterval = 800; // Minimum delay between bubbles in ms
  
  // Create a message bubble
  const createBubble = (content, type = 'ai-answer') => {
    if (!content.trim()) return;
    
    // Add a small delay so replies feel more natural
    const now = Date.now();
    const timeSinceLastBubble = now - lastBubbleTime;
    
    if (isFirstResponse) {
      // Show the first message immediately
      addMessage(content, false, type);
      isFirstResponse = false;
    } else if (timeSinceLastBubble < minBubbleInterval) {
      // Delay if the previous bubble was too recent
      setTimeout(() => {
        addMessage(content, false, type);
      }, minBubbleInterval - timeSinceLastBubble);
    } else {
      // Otherwise add it right away
      addMessage(content, false, type);
    }
    
    lastBubbleTime = now;
    messageBuffer = []; // Clear the buffer
  };
  
  eventSource = chatWithManus(message)
  
  // Listen for SSE messages
  eventSource.onmessage = (event) => {
    const data = event.data
    
    if (data && data !== '[DONE]') {
      messageBuffer.push(data);
      
      // Decide whether to create a new bubble
      const combinedText = messageBuffer.join('');
      
      // Split on sentence boundaries or long chunks
      const lastChar = data.charAt(data.length - 1);
      const hasCompleteSentence = sentenceEndPunctuation.includes(lastChar) || data.includes('\n\n');
      const isLongEnough = combinedText.length > 40;
      
      if (hasCompleteSentence || isLongEnough) {
        createBubble(combinedText);
      }
    }
    
    if (data === '[DONE]') {
      // Flush any remaining buffered text
      if (messageBuffer.length > 0) {
        const remainingContent = messageBuffer.join('');
        createBubble(remainingContent, 'ai-final');
      }
      
      // Close the connection when complete
      connectionStatus.value = 'disconnected'
      eventSource.close()
    }
  }
  
  // Listen for SSE errors
  eventSource.onerror = (error) => {
    console.error('SSE Error:', error)
    connectionStatus.value = 'error'
    eventSource.close()
    
    // Flush buffered text even if an error occurs
    if (messageBuffer.length > 0) {
      const remainingContent = messageBuffer.join('');
      createBubble(remainingContent, 'ai-error');
    }
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
