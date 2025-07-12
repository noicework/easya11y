import React from 'react'
import { cn } from '@lib/utils'

interface EmptyStateProps {
  icon?: React.ReactNode
  title: string
  description?: string
  action?: React.ReactNode
  className?: string
}

export function EmptyState({ 
  icon, 
  title, 
  description, 
  action,
  className 
}: EmptyStateProps) {
  return (
    <div className={cn(
      "flex flex-col items-center justify-center py-12 text-center",
      className
    )}>
      {icon && (
        <div className="mb-4 text-muted-foreground/50">
          {icon}
        </div>
      )}
      <h3 className="mb-2 text-lg font-medium">{title}</h3>
      {description && (
        <p className="mb-4 max-w-sm text-sm text-muted-foreground">
          {description}
        </p>
      )}
      {action && <div className="mt-4">{action}</div>}
    </div>
  )
}

// Default empty state for no scan results
export function NoResultsEmptyState() {
  return (
    <EmptyState
      icon={
        <svg 
          viewBox="0 0 24 24" 
          fill="none" 
          stroke="currentColor" 
          strokeWidth="2"
          className="h-20 w-20"
        >
          <circle cx="12" cy="12" r="10"></circle>
          <path d="M12 16v-4"></path>
          <path d="M12 8h.01"></path>
        </svg>
      }
      title="No scan results found"
      description="Select a page and click 'Scan Page' to start checking for accessibility issues."
    />
  )
}

// Empty state for filtered results
export function NoFilteredResultsEmptyState() {
  return (
    <EmptyState
      icon={
        <svg 
          viewBox="0 0 24 24" 
          fill="none" 
          stroke="currentColor" 
          strokeWidth="2"
          className="h-20 w-20"
        >
          <circle cx="11" cy="11" r="8"></circle>
          <path d="m21 21-4.35-4.35"></path>
        </svg>
      }
      title="No results match your filters"
      description="Try adjusting your search criteria or clearing some filters."
    />
  )
}