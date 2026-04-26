import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import type { UserPreferences } from '@/types'

const fixedTheme: UserPreferences['theme'] = 'light'

export const useAppStore = defineStore('app', () => {
  // State
  const loading = ref(false)
  const userPreferences = ref<UserPreferences>({
    theme: fixedTheme,
    language: 'en-US',
    fontSize: 'medium'
  })

  const isDark = computed(() => false)

  const applyTheme = () => {
    if (typeof document === 'undefined') return

    const root = document.documentElement

    root.dataset.theme = fixedTheme
    root.classList.remove('dark', 'cyber')
    root.classList.add(fixedTheme)
    root.style.colorScheme = fixedTheme
  }

  // Actions
  const setLoading = (isLoading: boolean) => {
    loading.value = isLoading
  }

  const updatePreferences = (preferences: Partial<UserPreferences>) => {
    userPreferences.value = { ...userPreferences.value, ...preferences, theme: fixedTheme }
    applyTheme()
    // Save to localStorage
    localStorage.setItem('user-preferences', JSON.stringify(userPreferences.value))
  }

  const loadPreferences = () => {
    try {
      const saved = localStorage.getItem('user-preferences')
      if (saved) {
        const parsed = JSON.parse(saved)
        userPreferences.value = {
          ...userPreferences.value,
          ...parsed,
          theme: fixedTheme
        }
      }
    } catch (error) {
      console.warn('Failed to load user preferences:', error)
    }
    applyTheme()
  }

  return {
    // State
    loading,
    userPreferences,
    isDark,
    
    // Actions
    setLoading,
    updatePreferences,
    loadPreferences,
    applyTheme
  }
})
