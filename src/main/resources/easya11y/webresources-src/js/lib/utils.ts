import { type ClassValue, clsx } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

/**
 * Format date for display
 */
export function formatDate(timestamp: string | Date | null): string {
  if (!timestamp) return '-'
  
  const date = new Date(timestamp)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)
  
  if (diffMins < 1) return 'Just now'
  if (diffMins < 60) return `${diffMins} minute${diffMins !== 1 ? 's' : ''} ago`
  if (diffHours < 24) return `${diffHours} hour${diffHours !== 1 ? 's' : ''} ago`
  if (diffDays < 7) return `${diffDays} day${diffDays !== 1 ? 's' : ''} ago`
  
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

/**
 * Get color based on accessibility score
 */
export function getScoreColor(score: number): string {
  if (score >= 90) return 'text-a11y-score-good'
  if (score >= 70) return 'text-a11y-score-warning'
  return 'text-a11y-score-bad'
}

/**
 * Get color based on impact level
 */
export function getImpactColor(impact: string): string {
  switch(impact) {
    case 'critical': return 'bg-a11y-critical text-white'
    case 'serious': return 'bg-a11y-serious text-white'
    case 'moderate': return 'bg-a11y-moderate text-white'
    case 'minor': return 'bg-a11y-minor text-gray-900'
    default: return 'bg-gray-500 text-white'
  }
}

/**
 * Calculate accessibility score based on violations
 */
export function calculateScore(scanResult: { violations?: any[], passes?: any[] }): number {
  if (!scanResult || !scanResult.violations) return 100
  
  const violations = scanResult.violations
  const totalElements = (scanResult.passes ? scanResult.passes.length : 0) + violations.length
  
  if (totalElements === 0) return 100
  
  // Weight violations by impact
  let weightedViolations = 0
  violations.forEach(violation => {
    const weight = violation.impact === 'critical' ? 10 : 
                 violation.impact === 'serious' ? 5 : 
                 violation.impact === 'moderate' ? 2 : 1
    weightedViolations += weight * (violation.nodes?.length || 0)
  })
  
  const score = Math.max(0, Math.round(100 - (weightedViolations / totalElements) * 100))
  return score
}

/**
 * Debounce function for input handling
 */
export function debounce<T extends (...args: any[]) => any>(
  func: T,
  wait: number
): (...args: Parameters<T>) => void {
  let timeout: NodeJS.Timeout | null = null
  
  return function executedFunction(...args: Parameters<T>) {
    const later = () => {
      timeout = null
      func(...args)
    }
    
    if (timeout) clearTimeout(timeout)
    timeout = setTimeout(later, wait)
  }
}