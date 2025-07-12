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
 * This should match the Java backend calculation in AccessibilityScanResult.calculateScore()
 */
export function calculateScore(scanResult: { violations?: any[], passes?: any[] }): number {
  if (!scanResult || !scanResult.violations) return 100
  
  const violations = scanResult.violations
  const passes = scanResult.passes || []
  
  // Count total elements tested (unique elements, not violations)
  let totalElements = passes.length
  violations.forEach(violation => {
    totalElements += (violation.nodes?.length || 0)
  })
  
  if (totalElements === 0) return 100
  
  // Count violations by impact (just count, not nodes)
  const violationsByImpact = {
    critical: 0,
    serious: 0,
    moderate: 0,
    minor: 0
  }
  
  violations.forEach(violation => {
    if (violation.impact && violationsByImpact.hasOwnProperty(violation.impact)) {
      violationsByImpact[violation.impact as keyof typeof violationsByImpact]++
    }
  })
  
  // Calculate weighted violations (matching Java backend)
  const weightedViolations = 
    (violationsByImpact.critical * 10.0) +
    (violationsByImpact.serious * 5.0) +
    (violationsByImpact.moderate * 2.0) +
    (violationsByImpact.minor * 1.0)
  
  // Calculate score (matching Java backend formula)
  const violationRatio = weightedViolations / (totalElements * 10.0)
  const score = Math.max(0, Math.round(100.0 - (violationRatio * 100.0)))
  
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