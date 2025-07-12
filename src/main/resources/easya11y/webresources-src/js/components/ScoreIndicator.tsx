import React from 'react'
import { cn } from '@lib/utils'

interface ScoreIndicatorProps {
  score: number
  size?: 'sm' | 'md' | 'lg'
  showLabel?: boolean
  className?: string
}

const sizeConfig = {
  sm: {
    container: 'h-12 w-12',
    svg: 'h-12 w-12',
    text: 'text-sm',
    strokeWidth: 4,
    radius: 20,
  },
  md: {
    container: 'h-20 w-20',
    svg: 'h-20 w-20',
    text: 'text-xl',
    strokeWidth: 6,
    radius: 32,
  },
  lg: {
    container: 'h-32 w-32',
    svg: 'h-32 w-32',
    text: 'text-3xl',
    strokeWidth: 8,
    radius: 52,
  },
}

export function ScoreIndicator({ 
  score, 
  size = 'md', 
  showLabel = false,
  className 
}: ScoreIndicatorProps) {
  const config = sizeConfig[size]
  const circumference = 2 * Math.PI * config.radius
  const strokeDashoffset = circumference - (score / 100) * circumference
  
  const getScoreColor = (score: number) => {
    if (score >= 90) return '#28a745'
    if (score >= 70) return '#ffc107'
    return '#dc3545'
  }
  
  const color = getScoreColor(score)
  
  return (
    <div className={cn("relative", config.container, className)}>
      <svg 
        className={cn("transform -rotate-90", config.svg)}
        viewBox={`0 0 ${(config.radius + config.strokeWidth) * 2} ${(config.radius + config.strokeWidth) * 2}`}
      >
        {/* Background circle */}
        <circle
          cx={config.radius + config.strokeWidth}
          cy={config.radius + config.strokeWidth}
          r={config.radius}
          strokeWidth={config.strokeWidth}
          className="fill-none stroke-muted"
        />
        {/* Progress circle */}
        <circle
          cx={config.radius + config.strokeWidth}
          cy={config.radius + config.strokeWidth}
          r={config.radius}
          strokeWidth={config.strokeWidth}
          className="fill-none transition-all duration-500 ease-out"
          style={{
            stroke: color,
            strokeDasharray: circumference,
            strokeDashoffset: strokeDashoffset,
            strokeLinecap: 'round',
          }}
        />
      </svg>
      <div className="absolute inset-0 flex items-center justify-center">
        <span 
          className={cn("font-bold", config.text)}
          style={{ color }}
        >
          {score}
        </span>
      </div>
      {showLabel && (
        <div className="mt-2 text-center">
          <span className="text-xs text-muted-foreground">Score</span>
        </div>
      )}
    </div>
  )
}

// Simplified score badge component
export function ScoreBadge({ score, className }: { score: number; className?: string }) {
  const colorClass = score >= 90 ? 'bg-a11y-score-good/10 text-a11y-score-good' :
                    score >= 70 ? 'bg-a11y-score-warning/10 text-a11y-score-warning' :
                    'bg-a11y-score-bad/10 text-a11y-score-bad'
  
  return (
    <span className={cn(
      "inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium",
      colorClass,
      className
    )}>
      Score: {score.toFixed(1)}
    </span>
  )
}