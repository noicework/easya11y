import { Card, CardContent } from '@components/ui/card'
import { Label } from '@components/ui/label'
import { RadioGroup, RadioGroupItem } from '@components/ui/radio-group'
import { Button } from '@components/ui/button'
import { X } from 'lucide-react'
import type { FilterState, WCAGLevel, ViolationImpact } from '@types'

interface FilterPanelProps {
  filters: FilterState
  onFiltersChange: (filters: FilterState) => void
}

export function FilterPanel({ filters, onFiltersChange }: FilterPanelProps) {
  const handleSeverityChange = (value: string) => {
    if (value === 'all') {
      const { severity, ...rest } = filters
      onFiltersChange(rest)
    } else {
      onFiltersChange({ ...filters, severity: value as ViolationImpact })
    }
  }

  const handleWcagLevelChange = (value: string) => {
    if (value === 'all') {
      const { wcagLevel, ...rest } = filters
      onFiltersChange(rest)
    } else {
      onFiltersChange({ ...filters, wcagLevel: value as WCAGLevel })
    }
  }

  const handleClearFilters = () => {
    onFiltersChange({})
  }

  const hasActiveFilters = Object.keys(filters).length > 0

  return (
    <Card>
      <CardContent className="p-4">
        <div className="flex items-center justify-between mb-4">
          <h3 className="font-medium">Filters</h3>
          {hasActiveFilters && (
            <Button
              variant="ghost"
              size="sm"
              onClick={handleClearFilters}
              className="h-8"
            >
              <X className="mr-1 h-3 w-3" />
              Clear All
            </Button>
          )}
        </div>

        <div className="grid gap-6 md:grid-cols-2">
          <div className="space-y-3">
            <Label>Severity</Label>
            <RadioGroup
              value={filters.severity || 'all'}
              onValueChange={handleSeverityChange}
            >
              <div className="flex items-center space-x-2">
                <RadioGroupItem value="all" id="severity-all" />
                <Label htmlFor="severity-all" className="font-normal cursor-pointer">
                  All Severities
                </Label>
              </div>
              <div className="flex items-center space-x-2">
                <RadioGroupItem value="critical" id="severity-critical" />
                <Label htmlFor="severity-critical" className="font-normal cursor-pointer">
                  <span className="inline-flex items-center gap-1">
                    <span className="text-red-600">●</span> Critical
                  </span>
                </Label>
              </div>
              <div className="flex items-center space-x-2">
                <RadioGroupItem value="serious" id="severity-serious" />
                <Label htmlFor="severity-serious" className="font-normal cursor-pointer">
                  <span className="inline-flex items-center gap-1">
                    <span className="text-orange-600">●</span> Serious
                  </span>
                </Label>
              </div>
              <div className="flex items-center space-x-2">
                <RadioGroupItem value="moderate" id="severity-moderate" />
                <Label htmlFor="severity-moderate" className="font-normal cursor-pointer">
                  <span className="inline-flex items-center gap-1">
                    <span className="text-yellow-600">●</span> Moderate
                  </span>
                </Label>
              </div>
              <div className="flex items-center space-x-2">
                <RadioGroupItem value="minor" id="severity-minor" />
                <Label htmlFor="severity-minor" className="font-normal cursor-pointer">
                  <span className="inline-flex items-center gap-1">
                    <span className="text-blue-600">●</span> Minor
                  </span>
                </Label>
              </div>
            </RadioGroup>
          </div>

          <div className="space-y-3">
            <Label>WCAG Level</Label>
            <RadioGroup
              value={filters.wcagLevel || 'all'}
              onValueChange={handleWcagLevelChange}
            >
              <div className="flex items-center space-x-2">
                <RadioGroupItem value="all" id="wcag-all" />
                <Label htmlFor="wcag-all" className="font-normal cursor-pointer">
                  All Levels
                </Label>
              </div>
              <div className="flex items-center space-x-2">
                <RadioGroupItem value="A" id="wcag-a" />
                <Label htmlFor="wcag-a" className="font-normal cursor-pointer">
                  Level A
                </Label>
              </div>
              <div className="flex items-center space-x-2">
                <RadioGroupItem value="AA" id="wcag-aa" />
                <Label htmlFor="wcag-aa" className="font-normal cursor-pointer">
                  Level AA
                </Label>
              </div>
              <div className="flex items-center space-x-2">
                <RadioGroupItem value="AAA" id="wcag-aaa" />
                <Label htmlFor="wcag-aaa" className="font-normal cursor-pointer">
                  Level AAA
                </Label>
              </div>
            </RadioGroup>
          </div>
        </div>
      </CardContent>
    </Card>
  )
}