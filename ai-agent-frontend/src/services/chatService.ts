import request from '@/utils/request'
import type { AIType } from '@/types'

export interface SSEOptions {
  onMessage?: (data: string) => void
  onError?: (error: Event) => void
  onOpen?: (event: Event) => void
  onClose?: (event: Event) => void
}

export class ChatService {
  private static instance: ChatService
  private eventSources: Map<string, EventSource> = new Map()

  static getInstance(): ChatService {
    if (!ChatService.instance) {
      ChatService.instance = new ChatService()
    }
    return ChatService.instance
  }

  /**
   * Create an SSE connection
   */
  connectSSE(
    url: string,
    params: Record<string, any>,
    options: SSEOptions = {}
  ): EventSource {
    // Build the query string
    const queryString = Object.keys(params)
      .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
      .join('&')
    
    const fullUrl = `${request.defaults.baseURL}${url}?${queryString}`
    
    // Close any existing connection for this endpoint
    this.closeConnection(url)
    
    // Create a new EventSource
    const eventSource = new EventSource(fullUrl)
    this.eventSources.set(url, eventSource)
    
    // Bind event handlers
    eventSource.onopen = (event) => {
      console.log('SSE connection opened:', url)
      options.onOpen?.(event)
    }
    
    eventSource.onmessage = (event) => {
      try {
        const data = event.data
        if (data === '[DONE]') {
          console.log('SSE stream completed:', url)
          options.onMessage?.(data)
          this.closeConnection(url)
        } else {
          options.onMessage?.(data)
        }
      } catch (error) {
        console.error('Error parsing SSE message:', error)
        options.onError?.(error as Event)
      }
    }
    
    eventSource.onerror = (error) => {
      console.error('SSE error:', error)
      options.onError?.(error)
      this.closeConnection(url)
    }
    
    return eventSource
  }

  /**
   * Close a specific SSE connection
   */
  closeConnection(url: string): void {
    const eventSource = this.eventSources.get(url)
    if (eventSource) {
      eventSource.close()
      this.eventSources.delete(url)
    }
  }

  /**
   * Close all SSE connections
   */
  closeAllConnections(): void {
    this.eventSources.forEach((eventSource) => {
      eventSource.close()
    })
    this.eventSources.clear()
  }

  /**
   * AI Love Coach chat
   */
  chatWithLoveApp(message: string, chatId: string, options: SSEOptions = {}): EventSource {
    return this.connectSSE('/ai/love_app/chat/sse', { message, chatId }, options)
  }

  /**
   * Super Agent chat
   */
  chatWithManus(message: string, options: SSEOptions = {}): EventSource {
    return this.connectSSE('/ai/manus/chat', { message }, options)
  }

  /**
   * Choose a chat service by AI type
   */
  chatWithAI(
    type: AIType,
    message: string,
    chatId?: string,
    options: SSEOptions = {}
  ): EventSource {
    switch (type) {
      case 'love':
        return this.chatWithLoveApp(message, chatId || '', options)
      case 'super':
        return this.chatWithManus(message, options)
      default:
        throw new Error(`Unsupported AI type: ${type}`)
    }
  }
}

// Export the singleton instance
export const chatService = ChatService.getInstance()
