import { toast } from '@lib/toast'

export interface RetryOptions {
  maxRetries?: number
  retryDelay?: number
  shouldRetry?: (error: any) => boolean
  onRetry?: (attempt: number, error: any) => void
}

export class AccessibilityError extends Error {
  constructor(
    message: string,
    public code?: string,
    public details?: any
  ) {
    super(message)
    this.name = 'AccessibilityError'
  }
}

export async function withRetry<T>(
  fn: () => Promise<T>,
  options: RetryOptions = {}
): Promise<T> {
  const {
    maxRetries = 3,
    retryDelay = 1000,
    shouldRetry = (error) => {
      // Retry on network errors or 5xx server errors
      if (error.name === 'NetworkError' || error.message.includes('fetch')) {
        return true
      }
      if (error.status && error.status >= 500) {
        return true
      }
      return false
    },
    onRetry = () => {}
  } = options

  let lastError: any
  
  for (let attempt = 0; attempt <= maxRetries; attempt++) {
    try {
      return await fn()
    } catch (error) {
      lastError = error
      
      if (attempt < maxRetries && shouldRetry(error)) {
        onRetry(attempt + 1, error)
        await new Promise(resolve => setTimeout(resolve, retryDelay * (attempt + 1)))
      } else {
        break
      }
    }
  }
  
  throw lastError
}

export function handleError(error: any, context?: string) {
  console.error(`Error ${context ? `in ${context}` : ''}:`, error)
  
  let message = 'An unexpected error occurred'
  let details = ''
  
  if (error instanceof AccessibilityError) {
    message = error.message
    details = error.details
  } else if (error instanceof TypeError && error.message.includes('fetch')) {
    message = 'Network error: Unable to connect to the server'
    details = 'Please check your internet connection and try again'
  } else if (error.status) {
    switch (error.status) {
      case 400:
        message = 'Invalid request'
        details = 'Please check your input and try again'
        break
      case 401:
        message = 'Authentication required'
        details = 'Please log in and try again'
        break
      case 403:
        message = 'Permission denied'
        details = 'You do not have permission to perform this action'
        break
      case 404:
        message = 'Resource not found'
        details = 'The requested page or resource could not be found'
        break
      case 500:
        message = 'Server error'
        details = 'The server encountered an error. Please try again later'
        break
      default:
        message = `Server error (${error.status})`
    }
  } else if (error.message) {
    message = error.message
  }
  
  // Show user-friendly error message
  toast.error(message, { description: details })
  
  return { message, details }
}

export function createErrorBoundary(onError?: (error: Error, errorInfo: any) => void) {
  return {
    onError: (error: Error, errorInfo: any) => {
      handleError(error, 'React Error Boundary')
      if (onError) {
        onError(error, errorInfo)
      }
    }
  }
}