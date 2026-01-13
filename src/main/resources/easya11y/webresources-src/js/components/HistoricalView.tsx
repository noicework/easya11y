import { useState, useEffect } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@components/ui/card'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@components/ui/select'
import { Button } from '@components/ui/button'
import { Calendar as CalendarIcon, Download } from 'lucide-react'
import { format, subDays, startOfDay, endOfDay } from 'date-fns'
import DatePicker from 'react-datepicker'
import "react-datepicker/dist/react-datepicker.css"
import {
  LineChart,
  Line,
  AreaChart,
  Area,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
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

interface HistoricalViewProps {
  pages: Page[]
  initialPagePath?: string
}

export function HistoricalView({ pages, initialPagePath }: HistoricalViewProps) {
  const [selectedPage, setSelectedPage] = useState<string>(initialPagePath || '')
  const [dateRange, setDateRange] = useState<[Date, Date]>([
    subDays(new Date(), 30),
    new Date()
  ])
  const [timeframe, setTimeframe] = useState<'7d' | '30d' | '90d' | 'custom'>('30d')

  // Update selected page when prop changes
  useEffect(() => {
    if (initialPagePath && initialPagePath !== selectedPage) {
      setSelectedPage(initialPagePath)
    }
  }, [initialPagePath])

  // Fetch historical trends
  const { data: trendsData, isLoading } = useQuery({
    queryKey: ['trends', selectedPage, dateRange],
    queryFn: async () => {
      const days = Math.ceil((dateRange[1].getTime() - dateRange[0].getTime()) / (1000 * 60 * 60 * 24))
      return await accessibilityService.getHistoricalTrends(selectedPage || undefined, days)
    },
    enabled: true,
    refetchInterval: 5 * 60 * 1000 // Refresh every 5 minutes
  })

  // Handle timeframe changes
  useEffect(() => {
    const end = new Date()
    let start: Date
    
    switch (timeframe) {
      case '7d':
        start = subDays(end, 7)
        break
      case '30d':
        start = subDays(end, 30)
        break
      case '90d':
        start = subDays(end, 90)
        break
      default:
        return // Keep custom range
    }
    
    setDateRange([startOfDay(start), endOfDay(end)])
  }, [timeframe])

  const handleDateRangeChange = (dates: [Date | null, Date | null]) => {
    if (dates[0] && dates[1]) {
      setDateRange([dates[0], dates[1]])
      setTimeframe('custom')
    }
  }

  const handleExport = async () => {
    const exportUrl = await accessibilityService.exportResults(
      'csv',
      selectedPage || undefined
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
          <p className="text-sm font-medium">{label}</p>
          {payload.map((entry: any, index: number) => (
            <p key={index} className="text-sm" style={{ color: entry.color }}>
              {entry.name}: {entry.value}
            </p>
          ))}
        </div>
      )
    }
    return null
  }

  return (
    <div className="space-y-6">
      {/* Controls */}
      <Card>
        <CardHeader>
          <CardTitle>Historical Analysis</CardTitle>
          <CardDescription>
            Track accessibility improvements and trends over time
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex flex-wrap gap-4">
            {/* Page Filter */}
            <Select value={selectedPage || "all"} onValueChange={(value) => setSelectedPage(value === "all" ? "" : value)}>
              <SelectTrigger className="w-[250px]">
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

            {/* Timeframe Selection */}
            <div className="flex gap-2">
              <Button
                variant={timeframe === '7d' ? 'default' : 'outline'}
                size="sm"
                onClick={() => setTimeframe('7d')}
              >
                7 Days
              </Button>
              <Button
                variant={timeframe === '30d' ? 'default' : 'outline'}
                size="sm"
                onClick={() => setTimeframe('30d')}
              >
                30 Days
              </Button>
              <Button
                variant={timeframe === '90d' ? 'default' : 'outline'}
                size="sm"
                onClick={() => setTimeframe('90d')}
              >
                90 Days
              </Button>
            </div>

            {/* Date Range Picker */}
            <div className="flex items-center gap-2">
              <CalendarIcon className="h-4 w-4 text-muted-foreground" />
              <DatePicker
                selectsRange
                startDate={dateRange[0]}
                endDate={dateRange[1]}
                onChange={handleDateRangeChange}
                className="flex h-9 w-full rounded-md border border-input bg-background px-3 py-1 text-sm shadow-sm transition-colors file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50"
                dateFormat="MMM d, yyyy"
              />
            </div>

            {/* Export Button */}
            <Button
              variant="outline"
              onClick={handleExport}
              disabled={!chartData.length}
            >
              <Download className="mr-2 h-4 w-4" />
              Export CSV
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Charts */}
      {isLoading ? (
        <div className="flex justify-center py-8">
          <div className="text-muted-foreground">Loading historical data...</div>
        </div>
      ) : chartData.length === 0 ? (
        <Card>
          <CardContent className="pt-6">
            <div className="text-center text-muted-foreground">
              No historical data available for the selected timeframe
            </div>
          </CardContent>
        </Card>
      ) : (
        <>
          {/* Accessibility Score Trend */}
          <Card>
            <CardHeader>
              <CardTitle>Accessibility Score Trend</CardTitle>
              <CardDescription>
                Average, minimum, and maximum scores over time
              </CardDescription>
            </CardHeader>
            <CardContent>
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={chartData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis
                    dataKey="date"
                    tickFormatter={(date) => format(new Date(date), 'MMM d')}
                  />
                  <YAxis domain={[0, 100]} />
                  <Tooltip
                    content={<CustomTooltip />}
                    labelFormatter={(date) => format(new Date(date), 'MMM d, yyyy')}
                  />
                  <Legend />
                  <ReferenceLine y={90} stroke="#10b981" strokeDasharray="5 5" label="Target (90)" />
                  <Line
                    type="monotone"
                    dataKey="averageScore"
                    name="Average Score"
                    stroke="#3b82f6"
                    strokeWidth={2}
                    dot={{ r: 3 }}
                  />
                  <Line
                    type="monotone"
                    dataKey="maxScore"
                    name="Best Score"
                    stroke="#10b981"
                    strokeWidth={1}
                    strokeDasharray="3 3"
                    dot={false}
                  />
                  <Line
                    type="monotone"
                    dataKey="minScore"
                    name="Worst Score"
                    stroke="#ef4444"
                    strokeWidth={1}
                    strokeDasharray="3 3"
                    dot={false}
                  />
                </LineChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>

          {/* Violations by Severity */}
          <Card>
            <CardHeader>
              <CardTitle>Violations by Severity</CardTitle>
              <CardDescription>
                Track the number of issues by impact level
              </CardDescription>
            </CardHeader>
            <CardContent>
              <ResponsiveContainer width="100%" height={300}>
                <AreaChart data={chartData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis
                    dataKey="date"
                    tickFormatter={(date) => format(new Date(date), 'MMM d')}
                  />
                  <YAxis />
                  <Tooltip
                    content={<CustomTooltip />}
                    labelFormatter={(date) => format(new Date(date), 'MMM d, yyyy')}
                  />
                  <Legend />
                  <Area
                    type="monotone"
                    dataKey="criticalCount"
                    name="Critical"
                    stackId="1"
                    stroke="#991b1b"
                    fill="#dc2626"
                  />
                  <Area
                    type="monotone"
                    dataKey="seriousCount"
                    name="Serious"
                    stackId="1"
                    stroke="#c2410c"
                    fill="#ea580c"
                  />
                  <Area
                    type="monotone"
                    dataKey="moderateCount"
                    name="Moderate"
                    stackId="1"
                    stroke="#ca8a04"
                    fill="#f59e0b"
                  />
                  <Area
                    type="monotone"
                    dataKey="minorCount"
                    name="Minor"
                    stackId="1"
                    stroke="#65a30d"
                    fill="#84cc16"
                  />
                </AreaChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>

          {/* Scan Activity */}
          <Card>
            <CardHeader>
              <CardTitle>Scan Activity</CardTitle>
              <CardDescription>
                Number of accessibility scans performed
              </CardDescription>
            </CardHeader>
            <CardContent>
              <ResponsiveContainer width="100%" height={200}>
                <BarChart data={chartData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis
                    dataKey="date"
                    tickFormatter={(date) => format(new Date(date), 'MMM d')}
                  />
                  <YAxis />
                  <Tooltip
                    content={<CustomTooltip />}
                    labelFormatter={(date) => format(new Date(date), 'MMM d, yyyy')}
                  />
                  <Bar
                    dataKey="totalScans"
                    name="Scans Performed"
                    fill="#6366f1"
                    radius={[4, 4, 0, 0]}
                  />
                </BarChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </>
      )}
    </div>
  )
}
