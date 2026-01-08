interface ToastOptions {
  description?: string
  duration?: number
}

class Toast {
  private container: HTMLDivElement | null = null
  
  private getContainer(): HTMLDivElement {
    if (!this.container) {
      this.container = document.createElement('div')
      this.container.className = 'fixed bottom-4 right-4 z-50 space-y-2'
      document.body.appendChild(this.container)
    }
    return this.container
  }
  
  private show(message: string, type: 'success' | 'error' | 'info', options: ToastOptions = {}) {
    const container = this.getContainer()
    const toast = document.createElement('div')
    
    const bgColor = {
      success: 'bg-green-600',
      error: 'bg-red-600',
      info: 'bg-blue-600'
    }[type]
    
    toast.className = `${bgColor} text-white px-4 py-3 rounded-lg shadow-lg max-w-sm animate-in slide-in-from-right`
    
    const titleEl = document.createElement('div')
    titleEl.className = 'font-medium'
    titleEl.textContent = message
    toast.appendChild(titleEl)
    
    if (options.description) {
      const descEl = document.createElement('div')
      descEl.className = 'text-sm opacity-90 mt-1'
      descEl.textContent = options.description
      toast.appendChild(descEl)
    }
    
    container.appendChild(toast)
    
    // Auto remove after duration
    setTimeout(() => {
      toast.classList.add('animate-out', 'slide-out-to-right')
      setTimeout(() => {
        if (toast.parentNode) {
          toast.parentNode.removeChild(toast)
        }
      }, 150)
    }, options.duration || 5000)
  }
  
  success(message: string, options?: ToastOptions) {
    this.show(message, 'success', options)
  }
  
  error(message: string, options?: ToastOptions) {
    this.show(message, 'error', options)
  }
  
  info(message: string, options?: ToastOptions) {
    this.show(message, 'info', options)
  }
}

export const toast = new Toast()