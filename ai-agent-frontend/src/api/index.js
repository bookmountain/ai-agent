import axios from 'axios'

// Set the API base URL from the environment
const API_BASE_URL = process.env.NODE_ENV === 'production' 
 ? '/api' // Use a relative path in production
 : 'http://localhost:8123/api' // Point to the local backend in development

// Create the axios instance
const request = axios.create({
  baseURL: API_BASE_URL,
  timeout: 60000
})

// Wrap SSE connections
export const connectSSE = (url, params, onMessage, onError) => {
  // Build the URL with query parameters
  const queryString = Object.keys(params)
    .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
    .join('&')
  
  const fullUrl = `${API_BASE_URL}${url}?${queryString}`
  
  // Create the EventSource
  const eventSource = new EventSource(fullUrl)
  
  eventSource.onmessage = event => {
    let data = event.data
    
    // Check for the stream terminator
    if (data === '[DONE]') {
      if (onMessage) onMessage('[DONE]')
    } else {
      // Handle regular messages
      if (onMessage) onMessage(data)
    }
  }
  
  eventSource.onerror = error => {
    if (onError) onError(error)
    eventSource.close()
  }
  
  // Return the EventSource so callers can close it later
  return eventSource
}

// AI Love Coach chat
export const chatWithLoveApp = (message, chatId) => {
  return connectSSE('/ai/love_app/chat/sse', { message, chatId })
}

// Super Agent chat
export const chatWithManus = (message) => {
  return connectSSE('/ai/manus/chat', { message })
}

export default {
  chatWithLoveApp,
  chatWithManus
} 
