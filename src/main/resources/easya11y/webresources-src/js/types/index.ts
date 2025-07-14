export type WCAGLevel = 'A' | 'AA' | 'AAA'

export type ViolationImpact = 'critical' | 'serious' | 'moderate' | 'minor'

export type SortOrder = 'date-desc' | 'score-asc' | 'score-desc' | 'violations-desc'

export interface Page {
  path: string
  title?: string
  name?: string
  url?: string
  lastScanned?: string
  score?: number
}

export interface Violation {
  id: string
  impact: ViolationImpact
  description: string
  help: string
  helpUrl?: string
  tags: string[]
  nodes: Array<{
    target?: string | string[]
    html?: string
    selector?: string
    failureSummary?: string
  }>
}

export interface ScanResult {
  scanId: string
  pagePath: string
  pageUrl: string
  pageTitle: string
  scanDate: string
  wcagLevel: WCAGLevel
  score: number
  violationCount: number
  violations?: Violation[]
  passes?: any[]
  incomplete?: any[]
  inapplicable?: any[]
  criticalCount?: number
  seriousCount?: number
  moderateCount?: number
  minorCount?: number
  violations_critical?: number
  violations_serious?: number
  violations_moderate?: number
  violations_minor?: number
}

export interface ScanInit {
  scanId: string
  pagePath: string
  pageUrl: string
  pageTitle: string
}

export interface FilterState {
  severity?: ViolationImpact | ''
  wcagLevel?: WCAGLevel | ''
  searchTerm?: string
  pagePath?: string
}

export interface ScanProgress {
  current: number
  total: number
  currentPage: string
  percentage: number
}

export interface ScanStats {
  totalPages: number
  scannedPages: number
  averageScore: number
  criticalIssues: number
}

export interface DetailedResult extends ScanResult {
  fullResults?: {
    violations: Violation[]
    passes: any[]
    incomplete: any[]
    inapplicable: any[]
    timestamp: string
    url: string
    errorMessage?: string
  }
}

export type WCAGVersion = '2.0' | '2.1' | '2.2'

export type ScheduleFrequency = 'daily' | 'weekly' | 'monthly' | 'custom'

export interface Configuration {
  wcagVersion: WCAGVersion
  wcagLevel: WCAGLevel
  emailEnabled?: boolean
  emailRecipients?: string
  emailFrom?: string
  emailOnViolations?: boolean
  emailDigest?: boolean
  scanScheduleEnabled?: boolean
  scanScheduleCron?: string
  scheduleFrequency?: ScheduleFrequency
  serverSideScan?: boolean
  scanPaths?: string
  excludePaths?: string
}