import { useState, useEffect } from 'react'
import { Button } from '@components/ui/button'
import { Input } from '@components/ui/input'
import { Label } from '@components/ui/label'
import { Textarea } from '@components/ui/textarea'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@components/ui/dialog'
import {
  Alert,
  AlertDescription,
  AlertTitle,
} from '@components/ui/alert'
import { ExternalLink, AlertCircle, CheckCircle } from 'lucide-react'
import { useQuery, useMutation } from '@tanstack/react-query'
import { accessibilityService } from '@services/accessibility.service'
import type { ScanResult } from '@types'

interface JiraIntegrationProps {
  scanResult: ScanResult
  isOpen: boolean
  onClose: () => void
}

interface JiraIssueData {
  projectKey: string
  summary: string
  description: string
  issueType: string
  priority: string
  labels: string[]
}

export function JiraIntegration({ scanResult, isOpen, onClose }: JiraIntegrationProps) {
  const [issueForm, setIssueForm] = useState<JiraIssueData>({
    projectKey: '',
    summary: `Accessibility Issues: ${scanResult.pageTitle}`,
    description: '',
    issueType: 'Bug',
    priority: 'Medium',
    labels: ['accessibility', 'wcag', `wcag-${scanResult.wcagLevel.toLowerCase()}`],
  })

  // Get configuration
  const { data: config } = useQuery({
    queryKey: ['configuration'],
    queryFn: () => accessibilityService.getConfiguration(),
  })

  // Update form when config loads
  useEffect(() => {
    if (config) {
      setIssueForm(prev => ({
        ...prev,
        projectKey: config.jiraProjectKey || '',
      }))
    }
  }, [config])

  // Create JIRA issue mutation
  const createIssueMutation = useMutation({
    mutationFn: async (issueData: JiraIssueData) => {
      return await accessibilityService.createJiraIssue({
        ...issueData,
        scanResult,
      })
    },
  })

  // Generate issue description
  const generateDescription = () => {
    const violations = scanResult.violations || []
    const criticalCount = violations.filter(v => v.impact === 'critical').length
    const seriousCount = violations.filter(v => v.impact === 'serious').length
    
    let description = `h2. Accessibility Scan Summary
Page: ${scanResult.pageTitle}
URL: ${scanResult.pageUrl}
Scan Date: ${new Date(scanResult.scanDate).toLocaleString()}
WCAG Level: ${scanResult.wcagLevel}
Score: ${scanResult.score}/100

h3. Violations Summary
Total Violations: ${scanResult.violationCount}
- Critical: ${criticalCount}
- Serious: ${seriousCount}
- Moderate: ${scanResult.moderateCount || 0}
- Minor: ${scanResult.minorCount || 0}

h3. Top Issues
`

    // Add top 5 violations
    violations.slice(0, 5).forEach((violation, index) => {
      description += `
${index + 1}. *${violation.help}* (${violation.impact})
   - ${violation.description}
   - Affected elements: ${violation.nodes.length}
   - [More info|${violation.helpUrl}]
`
    })

    if (violations.length > 5) {
      description += `\n... and ${violations.length - 5} more issues`
    }

    setIssueForm(prev => ({ ...prev, description }))
  }

  const handleCreateIssue = () => {
    createIssueMutation.mutate(issueForm)
  }

  const isConfigured = config?.jiraEnabled && config?.jiraUrl && config?.jiraApiToken && config?.jiraEmail

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Export to JIRA</DialogTitle>
          <DialogDescription>
            Create a JIRA issue with accessibility scan results
          </DialogDescription>
        </DialogHeader>

        {!isConfigured ? (
          <div className="space-y-4">
            <Alert>
              <AlertCircle className="h-4 w-4" />
              <AlertTitle>JIRA Integration Not Configured</AlertTitle>
              <AlertDescription>
                Please configure your JIRA settings in the Configuration page to create issues.
              </AlertDescription>
            </Alert>

            <DialogFooter>
              <Button variant="outline" onClick={onClose}>
                Cancel
              </Button>
              <Button
                onClick={() => {
                  window.open('/easya11y/configuration', '_blank')
                  onClose()
                }}
              >
                Go to Configuration
              </Button>
            </DialogFooter>
          </div>
        ) : (
          <div className="space-y-4">
            {createIssueMutation.isSuccess ? (
              <Alert className="border-green-500 bg-green-50">
                <CheckCircle className="h-4 w-4 text-green-600" />
                <AlertTitle>Issue Created Successfully!</AlertTitle>
                <AlertDescription>
                  Issue key: {createIssueMutation.data.key}
                  <br />
                  <a
                    href={`${config.jiraUrl}/browse/${createIssueMutation.data.key}`}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-primary hover:underline"
                  >
                    View in JIRA
                    <ExternalLink className="inline h-3 w-3 ml-1" />
                  </a>
                </AlertDescription>
              </Alert>
            ) : (
              <>
                <div className="space-y-2">
                  <Label htmlFor="project-key">Project Key</Label>
                  <Input
                    id="project-key"
                    value={issueForm.projectKey}
                    onChange={(e) => setIssueForm(prev => ({ ...prev, projectKey: e.target.value }))}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="summary">Summary</Label>
                  <Input
                    id="summary"
                    value={issueForm.summary}
                    onChange={(e) => setIssueForm(prev => ({ ...prev, summary: e.target.value }))}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="description">Description</Label>
                  <Textarea
                    id="description"
                    rows={10}
                    value={issueForm.description}
                    onChange={(e) => setIssueForm(prev => ({ ...prev, description: e.target.value }))}
                    placeholder="Click 'Generate Description' to auto-fill"
                  />
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={generateDescription}
                  >
                    Generate Description
                  </Button>
                </div>

                {createIssueMutation.isError && (
                  <Alert variant="destructive">
                    <AlertCircle className="h-4 w-4" />
                    <AlertTitle>Error</AlertTitle>
                    <AlertDescription>
                      {createIssueMutation.error?.message || 'Failed to create issue'}
                    </AlertDescription>
                  </Alert>
                )}
              </>
            )}

            <DialogFooter>
              <Button variant="outline" onClick={onClose}>
                {createIssueMutation.isSuccess ? 'Close' : 'Cancel'}
              </Button>
              {!createIssueMutation.isSuccess && (
                <Button
                  onClick={handleCreateIssue}
                  disabled={!issueForm.projectKey || !issueForm.summary || createIssueMutation.isPending}
                >
                  {createIssueMutation.isPending ? 'Creating...' : 'Create Issue'}
                </Button>
              )}
            </DialogFooter>
          </div>
        )}
      </DialogContent>
    </Dialog>
  )
}