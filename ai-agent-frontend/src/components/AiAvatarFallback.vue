<template>
  <div class="ai-avatar-fallback" :class="type">
    <span v-if="type === 'love'">LC</span>
    <span v-else>AI</span>
  </div>
</template>

<script setup>
defineProps({
  type: {
    type: String,
    default: 'default'
  }
})
</script>

<style scoped>
.ai-avatar-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0;
  border-radius: 50%;
}

.love {
  background: #b24a5a;
}

.default, .super {
  background: #275fa8;
}
</style> 
