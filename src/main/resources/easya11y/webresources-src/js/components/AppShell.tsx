import React from 'react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cn } from '@lib/utils'

interface AppShellProps {
  children: React.ReactNode
  variant?: 'checker' | 'dialog'
  className?: string
}

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
      staleTime: 5 * 60 * 1000, // 5 minutes
    },
  },
})

export function AppShell({ children, variant = 'checker', className }: AppShellProps) {
  return (
    <QueryClientProvider client={queryClient}>
      <div 
        className={cn(
          "min-h-screen bg-background font-sans antialiased",
          variant === 'dialog' && "p-0",
          variant === 'checker' && "p-5",
          className
        )}
      >
        <div className={cn(
          "mx-auto",
          variant === 'checker' && "max-w-7xl",
          variant === 'dialog' && "h-full"
        )}>
          {children}
        </div>
      </div>
    </QueryClientProvider>
  )
}

// Error Boundary Component
interface ErrorBoundaryState {
  hasError: boolean
  error?: Error
}

export class ErrorBoundary extends React.Component<
  { children: React.ReactNode },
  ErrorBoundaryState
> {
  constructor(props: { children: React.ReactNode }) {
    super(props)
    this.state = { hasError: false }
  }

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return { hasError: true, error }
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error('Error caught by boundary:', error, errorInfo)
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="flex min-h-screen items-center justify-center p-4">
          <div className="max-w-md rounded-lg border bg-card p-6 shadow-sm">
            <h2 className="mb-2 text-lg font-semibold text-destructive">
              Something went wrong
            </h2>
            <p className="mb-4 text-sm text-muted-foreground">
              {this.state.error?.message || 'An unexpected error occurred'}
            </p>
            <button
              onClick={() => this.setState({ hasError: false })}
              className="rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground hover:bg-primary/90"
            >
              Try again
            </button>
          </div>
        </div>
      )
    }

    return this.props.children
  }
}