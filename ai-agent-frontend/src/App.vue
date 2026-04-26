<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue'
import { useAppStore } from './stores'

const appStore = useAppStore()

onMounted(() => {
  appStore.loadPreferences()
})
</script>

<style>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

html, body {
  font-family: 'Inter', 'Segoe UI', 'Helvetica Neue', Helvetica, Arial, sans-serif;
  font-size: 16px;
  color: var(--text-color-primary);
  background-color: var(--background-color);
  width: 100%;
  min-height: 100%;
  overflow-x: hidden;
}

#app {
  width: 100%;
  min-height: 100dvh;
}

a {
  text-decoration: none;
  color: inherit;
}

button {
  cursor: pointer;
}

/* Scrollbar styling */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #aaa;
}
</style>
