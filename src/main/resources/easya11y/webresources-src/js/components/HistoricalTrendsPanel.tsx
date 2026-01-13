import { useState } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@components/ui/card'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@components/ui/tabs'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@components/ui/select'
import { Button } from '@components/ui/button'
import { TrendingUp, BarChart3, Activity, Calendar, Download } from 'lucide-react'
import { format } from 'date-fns'
import {
  LineChart,
  Line,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  ReferenceLine
} from 'recharts'
import { useQuery } from '@tanstack/react-query'
import { accessibilityService } from '@services/accessibility.service'
import type { Page } from '@types'

interface TrendData {
  date: string
  averageScore: number
  minScore: number
  maxScore: number
  totalScans: number
  criticalCount: number
  seriousCount: number
  moderateCount: number
  minorCount: number
}

interface HistoricalTrendsPanelProps {
  pages: Page[]
  selectedPage?: Page | null
}

export function HistoricalTrendsPanel({ pages, selectedPage }: HistoricalTrendsPanelProps) {
  const [selectedPagePath, setSelectedPagePath] = useState<string>(selectedPage?.path || '')
  const [activeTab, setActiveTab] = useState<'7d' | '30d' | '90d'>('30d')

  // Calculate date range based on active tab
  const getDaysForTab = (tab: string) => {
    switch (tab) {
      case '7d': return 7
      case '30d': return 30
      case '90d': return 90
      default: return 30
    }
  }

  // Fetch historical trends
  const { data: trendsData, isLoading } = useQuery({
    queryKey: ['trends', selectedPagePath, activeTab],
    queryFn: async () => {
      const days = getDaysForTab(activeTab)
      return await accessibilityService.getHistoricalTrends(selectedPagePath || undefined, days)
    },
    enabled: true,
    refetchInterval: 5 * 60 * 1000 // Refresh every 5 minutes
  })

  const handleExport = async () => {
    const exportUrl = await accessibilityService.exportResults(
      'csv',
      selectedPagePath || undefined
    )
    window.open(exportUrl, '_blank')
  }

  // Format data for charts
  const chartData: TrendData[] = trendsData?.trends || []

  // Custom tooltip for charts
  const CustomTooltip = ({ active, payload, label }: any) => {
    if (active && payload && payload.length) {
      return (
        <div className="bg-background border rounded-lg shadow-lg p-3">
          <p className="text-sm font-medium">{format(new Date(label), 'MMM d, yyyy')}</p>
          {payload.map((entry: any, index: number) => (
            <p key={index} className="text-sm" style={{ color: entry.color }}>
              {entry.name}: {typeof entry.value === 'number' ? Math.round(entry.value * 100) / 100 : entry.value}
            </p>
          ))}
        </div>
      )
    }
    return null
  }

  // Calculate summary stats
  const latestData = chartData[chartData.length - 1]
  const previousData = chartData[chartData.length - 2]
  const scoreChange = latestData && previousData
    ? latestData.averageScore - previousData.averageScore
    : 0

  return (
    <Card className="h-fit">
      <CardHeader>
        <div className="flex items-center gap-2">
          <TrendingUp className="h-5 w-5 text-purple-600" />
          <CardTitle>Historical Trends</CardTitle>
        </div>
        <CardDescription>
          Analyze accessibility compliance trends over time
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-6">
        {/* Controls */}
        <div className="space-y-4">
          {/* Page Selection */}
          <div className="space-y-2">
            <label className="text-sm font-medium">Page Filter</label>
            <Select 
              value={selectedPagePath || "all"} 
              onValueChange={(value) => setSelectedPagePath(value === "all" ? "" : value)}
            >
              <SelectTrigger>
                <SelectValue placeholder="All pages" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All pages</SelectItem>
                {pages.map((page) => (
                  <SelectItem key={page.path} value={page.path}>
                    {page.title || page.name || page.path}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          {/* Quick Stats */}
          {latestData && (
            <div className="grid grid-cols-2 gap-4">
              <div className="bg-blue-50 rounded-lg p-3 border-l-4 border-blue-400">
                <div className="flex items-center justify-between">
                  <p className="text-sm font-medium text-blue-900">Current Score</p>
                  <BarChart3 className="h-4 w-4 text-blue-600" />
                </div>
                <p className="text-lg font-bold text-blue-900">
                  {Math.round(latestData.averageScore)}
                </p>
                {scoreChange !== 0 && (
                  <p className={`text-xs ${scoreChange > 0 ? 'text-green-600' : 'text-red-600'}`}>
                    {scoreChange > 0 ? '+' : ''}{Math.round(scoreChange * 100) / 100} from last scan
                  </p>
                )}
              </div>
              <div className="bg-green-50 rounded-lg p-3 border-l-4 border-green-400">
                <div className="flex items-center justify-between">
                  <p className="text-sm font-medium text-green-900">Total Scans</p>
                  <Activity className="h-4 w-4 text-green-600" />
                </div>
                <p className="text-lg font-bold text-green-900">
                  {chartData.reduce((sum, data) => sum + data.totalScans, 0)}
                </p>
                <p className="text-xs text-green-600">
                  Last {getDaysForTab(activeTab)} days
                </p>
              </div>
            </div>
          )}
        </div>

        {/* Time Range Tabs with Charts */}
        <Tabs value={activeTab} onValueChange={(value) => setActiveTab(value as '7d' | '30d' | '90d')}>
          <div className="flex items-center justify-between">
            <TabsList className="grid w-full grid-cols-3">
              <TabsTrigger value="7d" className="text-xs">7 Days</TabsTrigger>
              <TabsTrigger value="30d" className="text-xs">30 Days</TabsTrigger>
              <TabsTrigger value="90d" className="text-xs">90 Days</TabsTrigger>
            </TabsList>
            <Button
              variant="outline"
              size="sm"
              onClick={handleExport}
              disabled={!chartData.length}
              className="ml-2"
            >
              <Download className="h-3 w-3" />
            </Button>
          </div>

          {isLoading ? (
            <div className="flex justify-center py-8">
              <div className="text-sm text-muted-foreground">Loading trends...</div>
            </div>
          ) : chartData.length === 0 ? (
            <div className="text-center text-muted-foreground py-8">
              <Calendar className="h-12 w-12 mx-auto mb-2 opacity-50" />
              <p className="text-sm">No data available for selected timeframe</p>
            </div>
          ) : (
            <>
              <TabsContent value="7d" className="space-y-4 mt-4">
                <ScoreChart data={chartData} timeframe="7d" tooltip={CustomTooltip} />
                <ViolationsChart data={chartData} timeframe="7d" tooltip={CustomTooltip} />
              </TabsContent>
              
              <TabsContent value="30d" className="space-y-4 mt-4">
                <ScoreChart data={chartData} timeframe="30d" tooltip={CustomTooltip} />
                <ViolationsChart data={chartData} timeframe="30d" tooltip={CustomTooltip} />
              </TabsContent>
              
              <TabsContent value="90d" className="space-y-4 mt-4">
                <ScoreChart data={chartData} timeframe="90d" tooltip={CustomTooltip} />
                <ViolationsChart data={chartData} timeframe="90d" tooltip={CustomTooltip} />
              </TabsContent>
            </>
          )}
        </Tabs>
      </CardContent>
    </Card>
  )
}

// Score Chart Component
function ScoreChart({ data, timeframe, tooltip }: { data: TrendData[], timeframe: string, tooltip: any }) {
  return (
    <div className="space-y-2">
      <h4 className="text-sm font-medium">Accessibility Score</h4>
      <div className="h-40">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={data}>
            <CartesianGrid strokeDasharray="3 3" opacity={0.3} />
            <XAxis
              dataKey="date"
              tickFormatter={(date) => format(new Date(date), timeframe === '7d' ? 'EEE' : 'M/d')}
              tick={{ fontSize: 10 }}
            />
            <YAxis domain={[0, 100]} tick={{ fontSize: 10 }} />
            <Tooltip content={tooltip} />
            <ReferenceLine y={90} stroke="#10b981" strokeDasharray="3 3" opacity={0.7} />
            <Line
              type="monotone"
              dataKey="averageScore"
              stroke="#3b82f6"
              strokeWidth={2}
              dot={{ r: 2, fill: '#3b82f6' }}
              activeDot={{ r: 4, fill: '#3b82f6' }}
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  )
}

// Violations Chart Component
function ViolationsChart({ data, timeframe, tooltip }: { data: TrendData[], timeframe: string, tooltip: any }) {
  return (
    <div className="space-y-2">
      <h4 className="text-sm font-medium">Issues by Severity</h4>
      <div className="h-32">
        <ResponsiveContainer width="100%" height="100%">
          <AreaChart data={data}>
            <CartesianGrid strokeDasharray="3 3" opacity={0.3} />
            <XAxis
              dataKey="date"
              tickFormatter={(date) => format(new Date(date), timeframe === '7d' ? 'EEE' : 'M/d')}
              tick={{ fontSize: 10 }}
            />
            <YAxis tick={{ fontSize: 10 }} />
            <Tooltip content={tooltip} />
            <Area
              type="monotone"
              dataKey="criticalCount"
              stackId="1"
              stroke="#dc2626"
              fill="#dc2626"
              fillOpacity={0.8}
            />
            <Area
              type="monotone"
              dataKey="seriousCount"
              stackId="1"
              stroke="#ea580c"
              fill="#ea580c"
              fillOpacity={0.8}
            />
            <Area
              type="monotone"
              dataKey="moderateCount"
              stackId="1"
              stroke="#f59e0b"
              fill="#f59e0b"
              fillOpacity={0.8}
            />
            <Area
              type="monotone"
              dataKey="minorCount"
              stackId="1"
              stroke="#84cc16"
              fill="#84cc16"
              fillOpacity={0.8}
            />
          </AreaChart>
        </ResponsiveContainer>
      </div>
    </div>
  )
}
