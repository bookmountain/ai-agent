import axios, { AxiosInstance, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import type { InternalAxiosRequestConfig } from 'axios'
import type { ApiResponse } from '@/types'

// Set the API base URL from the environment
const API_BASE_URL = import.meta.env.PROD 
  ? '/api' // Use a relative path in production
  : 'http://localhost:8123/api' // Point to the local backend in development

// Create the axios instance
const request: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // Authentication headers such as a token can be added here
    // const token = localStorage.getItem('token')
    // if (token) {
    //   config.headers.Authorization = `Bearer ${token}`
    // }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const { data } = response
    
    // Handle the standard API response shape
    if (data && typeof data === 'object' && 'code' in data) {
      if (data.code === 200) {
        return response
      } else {
        ElMessage.error(data.message || 'Request failed')
        return Promise.reject(new Error(data.message))
      }
    }
    
    return response
  },
  (error) => {
    let message = 'Network error. Please try again.'
    
    if (error.response) {
      const { status, data } = error.response
      switch (status) {
        case 400:
          message = data?.message || 'Invalid request parameters.'
          break
        case 401:
          message = 'Unauthorized. Please sign in again.'
          // Redirect to sign-in here if needed
          break
        case 403:
          message = 'Access denied.'
          break
        case 404:
          message = 'Requested resource not found.'
          break
        case 500:
          message = 'Internal server error.'
          break
        default:
          message = data?.message || `Request failed (${status})`
      }
    } else if (error.request) {
      message = 'Network connection failed. Check your connection and try again.'
    }
    
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request
