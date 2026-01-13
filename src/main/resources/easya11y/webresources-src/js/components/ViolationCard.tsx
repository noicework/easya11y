import { useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@components/ui/card'
import { Badge } from '@components/ui/badge'
import { Button } from '@components/ui/button'
import { Alert, AlertDescription } from '@components/ui/alert'
import { ChevronDown, ChevronUp, Code, ExternalLink, Info } from 'lucide-react'
import { cn } from '@lib/utils'

interface ViolationNode {
  target: string | string[]
  html: string
  failureSummary?: string
  any?: any[]
  all?: any[]
  none?: any[]
}

interface Violation {
  id: string
  impact: 'critical' | 'serious' | 'moderate' | 'minor'
  description: string
  help: string
  helpUrl?: string
  nodes: ViolationNode[]
  tags?: string[]
}

interface ViolationCardProps {
  violation: Violation
  expanded?: boolean
}

export function ViolationCard({ violation, expanded: initialExpanded = false }: ViolationCardProps) {
  const [expanded, setExpanded] = useState(initialExpanded)

  const getImpactColor = (impact: string) => {
    switch (impact) {
      case 'critical':
      case 'serious':
        return 'destructive'
      case 'moderate':
        return 'secondary'
      case 'minor':
        return 'outline'
      default:
        return 'outline'
    }
  }

  const getImpactIcon = (impact: string) => {
    switch (impact) {
      case 'critical':
        return '🔴'
      case 'serious':
        return '🟠'
      case 'moderate':
        return '🟡'
      case 'minor':
        return '🔵'
      default:
        return '⚪'
    }
  }

  return (
    <Card className={cn('transition-all', expanded && 'ring-2 ring-primary')}>
      <CardHeader>
        <div className="flex items-start justify-between gap-4">
          <div className="space-y-1.5 flex-1">
            <div className="flex items-center gap-2">
              <span className="text-lg">{getImpactIcon(violation.impact)}</span>
              <CardTitle className="text-base">{violation.help}</CardTitle>
            </div>
            <CardDescription>{violation.description}</CardDescription>
          </div>
          <div className="flex items-center gap-2">
            <Badge variant={getImpactColor(violation.impact)}>
              {violation.impact}
            </Badge>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setExpanded(!expanded)}
            >
              {expanded ? (
                <ChevronUp className="h-4 w-4" />
              ) : (
                <ChevronDown className="h-4 w-4" />
              )}
            </Button>
          </div>
        </div>
      </CardHeader>

      {expanded && (
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <div className="flex items-center justify-between gap-2">
              <Badge variant="outline" className="font-mono text-xs shrink-0">
                {violation.id}
              </Badge>
              {violation.helpUrl && (
                <Button
                  variant="link"
                  size="sm"
                  className="shrink-0"
                  onClick={() => window.open(violation.helpUrl, '_blank')}
                >
                  Learn More
                  <ExternalLink className="ml-1 h-3 w-3" />
                </Button>
              )}
            </div>
            {violation.tags && violation.tags.length > 0 && (
              <div className="flex flex-wrap gap-1">
                {violation.tags.slice(0, 6).map((tag) => (
                  <Badge key={tag} variant="secondary" className="text-xs">
                    {tag}
                  </Badge>
                ))}
                {violation.tags.length > 6 && (
                  <Badge variant="outline" className="text-xs">
                    +{violation.tags.length - 6} more
                  </Badge>
                )}
              </div>
            )}
          </div>

          <div className="space-y-3">
            <div className="font-medium text-sm">
              Affected Elements ({violation.nodes.length})
            </div>
            {violation.nodes.slice(0, 5).map((node, index) => (
              <Alert key={index} className="py-3">
                <Code className="h-4 w-4" />
                <AlertDescription className="space-y-2">
                  <div className="font-mono text-xs overflow-x-auto">
                    {Array.isArray(node.target) ? node.target.join(' > ') : node.target}
                  </div>
                  {node.html && (
                    <pre className="text-xs bg-muted p-2 rounded overflow-x-auto">
                      <code>{node.html}</code>
                    </pre>
                  )}
                  {node.failureSummary && (
                    <div className="text-sm mt-2">
                      <Info className="h-3 w-3 inline mr-1" />
                      {node.failureSummary}
                    </div>
                  )}
                </AlertDescription>
              </Alert>
            ))}
            {violation.nodes.length > 5 && (
              <div className="text-sm text-muted-foreground text-center">
                ... and {violation.nodes.length - 5} more elements
              </div>
            )}
          </div>
        </CardContent>
      )}
    </Card>
  )
}