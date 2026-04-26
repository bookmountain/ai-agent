<template>
  <div class="chat-container">
    <!-- Chat history -->
    <div class="chat-messages" ref="messagesContainer">
      <div v-for="(msg, index) in messages" :key="index" class="message-wrapper">
        <!-- AI message -->
        <div v-if="!msg.isUser" 
             class="message ai-message" 
             :class="[msg.type]">
          <div class="avatar ai-avatar">
            <AiAvatarFallback :type="aiType" />
          </div>
          <div class="message-bubble">
            <details v-if="msg.type === 'ai-reasoning'" class="reasoning-details">
              <summary class="reasoning-summary">
                <span>Reasoning</span>
                <span class="reasoning-count">{{ reasoningLineCount(msg.content) }}</span>
              </summary>
              <div class="message-content reasoning-content">{{ msg.content }}</div>
            </details>
            <div v-else class="message-content">
              {{ msg.content }}
              <span v-if="connectionStatus === 'connecting' && index === messages.length - 1" class="typing-indicator">▋</span>
            </div>
            <div class="message-time">{{ formatTime(msg.time) }}</div>
          </div>
        </div>
        
        <!-- User message -->
        <div v-else class="message user-message" :class="[msg.type]">
          <div class="message-bubble">
            <div class="message-content">{{ msg.content }}</div>
            <div class="message-time">{{ formatTime(msg.time) }}</div>
          </div>
          <div class="avatar user-avatar">
            <div class="avatar-placeholder">B</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Input area -->
    <div class="chat-input-container">
      <div class="chat-input">
        <textarea 
          v-model="inputMessage" 
          @keydown.enter.prevent="sendMessage"
          placeholder="Type your message..." 
          class="input-box"
          :disabled="connectionStatus === 'connecting'"
        ></textarea>
        <button 
          @click="sendMessage" 
          class="send-button"
          :disabled="connectionStatus === 'connecting' || !inputMessage.trim()"
        >Send</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import AiAvatarFallback from './AiAvatarFallback.vue'

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  },
  connectionStatus: {
    type: String,
    default: 'disconnected'
  },
  aiType: {
    type: String,
    default: 'default'  // 'love' or 'super'
  }
})

const emit = defineEmits(['send-message'])

const inputMessage = ref('')
const messagesContainer = ref(null)

// Send a message
const sendMessage = () => {
  if (!inputMessage.value.trim()) return
  
  emit('send-message', inputMessage.value)
  inputMessage.value = ''
}

// Format timestamps
const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })
}

const reasoningLineCount = (content) => {
  const count = content.split('\n').filter(line => line.trim()).length
  return count === 1 ? '1 step' : `${count} steps`
}

// Auto-scroll to the bottom
const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// Auto-scroll when messages or their content change
watch(() => props.messages.length, () => {
  scrollToBottom()
})

watch(() => props.messages.map(m => m.content).join(''), () => {
  scrollToBottom()
})

onMounted(() => {
  scrollToBottom()
})
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  min-height: 0;
  background-color: var(--background-color-elevated);
  border: 1px solid var(--border-color-light);
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  box-shadow: var(--shadow-lg);
}

.chat-messages {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 18px;
  display: flex;
  flex-direction: column;
  position: relative;
  scroll-behavior: smooth;
}

.message-wrapper {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  width: 100%;
}

.message {
  display: flex;
  align-items: flex-start;
  max-width: 78%;
  margin-bottom: 8px;
}

.user-message {
  margin-left: auto; /* User messages align right */
  flex-direction: row; /* Bubble first, avatar second */
}

.ai-message {
  margin-right: auto; /* AI messages align left */
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-avatar {
  margin-left: 8px; /* User avatar on the right */
}

.ai-avatar {
  margin-right: 8px; /* AI avatar on the left */
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--primary-color);
  color: white;
  font-weight: bold;
}

.message-bubble {
  padding: 12px;
  border-radius: 8px;
  position: relative;
  word-wrap: break-word;
  min-width: 100px;
  box-shadow: var(--shadow-sm);
}

.user-message .message-bubble {
  background-color: var(--primary-color);
  color: var(--text-color-on-primary);
  border-bottom-right-radius: 3px;
  text-align: left;
}

.ai-message .message-bubble {
  border: 1px solid var(--border-color-light);
  background-color: var(--background-color);
  color: var(--text-color-primary);
  border-bottom-left-radius: 3px;
  text-align: left;
}

.message-content {
  font-size: 16px;
  line-height: 1.5;
  white-space: pre-wrap;
}

.reasoning-details {
  font-size: 14px;
  color: var(--text-color-secondary);
}

.reasoning-summary {
  min-height: 28px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-weight: 700;
  color: var(--text-color-secondary);
  list-style-position: inside;
}

.reasoning-count {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-color-tertiary);
}

.reasoning-content {
  max-width: min(72vw, 760px);
  max-height: 240px;
  margin-top: 8px;
  overflow: auto;
  font-size: 13px;
  line-height: 1.45;
  color: var(--text-color-secondary);
  word-break: break-word;
}

.message-time {
  font-size: 12px;
  opacity: 0.7;
  margin-top: 4px;
  text-align: right;
}

.chat-input-container {
  position: relative;
  background-color: var(--background-color-elevated);
  border-top: 1px solid var(--border-color-light);
  z-index: 2;
}

.chat-input {
  display: flex;
  gap: 12px;
  padding: 14px 16px;
  box-sizing: border-box;
  align-items: center;
}

.input-box {
  flex-grow: 1;
  width: 100%;
  min-width: 0;
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-lg);
  padding: 10px 14px;
  font-size: 16px;
  line-height: 1.45;
  color: var(--text-color-primary);
  background-color: var(--background-color);
  caret-color: var(--primary-color);
  resize: none;
  min-height: 44px;
  max-height: 112px;
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background-color 0.2s ease;
  overflow-y: auto;
  scrollbar-width: thin;
}

.input-box::placeholder {
  color: var(--text-color-tertiary);
}

.input-box:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--primary-color) 24%, transparent);
}

.send-button {
  flex: 0 0 auto;
  background-color: var(--primary-color);
  color: var(--text-color-on-primary);
  border: none;
  border-radius: var(--border-radius-lg);
  padding: 0 20px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
  min-width: 76px;
  height: 44px;
  align-self: center;
}

.send-button:hover:not(:disabled) {
  background-color: var(--primary-color-dark);
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}

.typing-indicator {
  display: inline-block;
  animation: blink 0.7s infinite;
  margin-left: 2px;
}

@keyframes blink {
  0% { opacity: 0; }
  50% { opacity: 1; }
  100% { opacity: 0; }
}

.input-box:disabled {
  color: var(--text-color-disabled);
  background-color: var(--background-color-tertiary);
  border-color: var(--border-color-light);
  cursor: not-allowed;
}

.send-button:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
}

/* Responsive layout */
@media (max-width: 768px) {
  .message {
    max-width: 95%;
  }
  
  .message-content {
    font-size: 15px;
  }
  
  .chat-input {
    padding: 12px;
  }
  
  .input-box {
    padding: 9px 12px;
  }
  
  .send-button {
    padding: 0 15px;
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .avatar {
    width: 32px;
    height: 32px;
  }
  
  .message-bubble {
    padding: 10px;
  }
  
  .message-content {
    font-size: 14px;
  }
  
  .chat-input {
    align-items: stretch;
    flex-direction: column;
  }

  .send-button {
    width: 100%;
  }
}

/* Styles for different message types */
.ai-answer {
  animation: fadeIn 0.3s ease-in-out;
}

.ai-final {
  /* Final answer styling */
}

.ai-error {
  opacity: 0.7;
}

.user-question {
  /* Special styling for user questions */
}

/* Consecutive message bubble styling */
.ai-message + .ai-message {
  margin-top: 4px;
}

.ai-message + .ai-message .avatar {
  visibility: hidden;
}

.ai-message + .ai-message .message-bubble {
  border-top-left-radius: 10px;
}
</style> 
