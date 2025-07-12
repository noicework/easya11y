import React from 'react'
import { motion } from 'framer-motion'
import { cn } from '@lib/utils'
import type { ScanStats } from '@types'

interface StatsOverviewProps {
  stats: ScanStats
  className?: string
}

export function StatsOverview({ stats, className }: StatsOverviewProps) {
  const statCards = [
    {
      label: 'Total Pages',
      value: stats.totalPages,
      color: 'text-primary',
    },
    {
      label: 'Pages Scanned',
      value: stats.scannedPages,
      color: 'text-primary',
    },
    {
      label: 'Average Score',
      value: stats.averageScore.toFixed(1),
      color: stats.averageScore >= 90 ? 'text-a11y-score-good' : 
             stats.averageScore >= 70 ? 'text-a11y-score-warning' : 
             'text-a11y-score-bad',
    },
    {
      label: 'Critical Issues',
      value: stats.criticalIssues,
      color: stats.criticalIssues > 0 ? 'text-destructive' : 'text-a11y-success',
    },
  ]

  return (
    <div className={cn("grid gap-4 md:grid-cols-2 lg:grid-cols-4", className)}>
      {statCards.map((stat, index) => (
        <motion.div
          key={stat.label}
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.3, delay: index * 0.1 }}
          className="relative overflow-hidden rounded-lg border bg-card p-6 shadow-sm transition-all hover:shadow-md"
        >
          <div className="flex flex-col">
            <span className="text-sm font-medium text-muted-foreground">
              {stat.label}
            </span>
            <span className={cn("text-3xl font-bold tabular-nums", stat.color)}>
              <CountUp value={Number(stat.value)} />
            </span>
          </div>
          <div className="absolute right-0 top-0 h-24 w-24 translate-x-8 translate-y-[-4rem]">
            <div className="h-full w-full rounded-full bg-primary/5" />
          </div>
        </motion.div>
      ))}
    </div>
  )
}

// Simple count-up animation component
function CountUp({ value }: { value: number }) {
  const [count, setCount] = React.useState(0)
  
  React.useEffect(() => {
    const duration = 1000 // 1 second
    const steps = 20
    const increment = value / steps
    const stepDuration = duration / steps
    
    let current = 0
    const timer = setInterval(() => {
      current += increment
      if (current >= value) {
        setCount(value)
        clearInterval(timer)
      } else {
        setCount(Math.floor(current))
      }
    }, stepDuration)
    
    return () => clearInterval(timer)
  }, [value])
  
  return <>{count}</>
}