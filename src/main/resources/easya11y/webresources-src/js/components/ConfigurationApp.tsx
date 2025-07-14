import { useState, useEffect } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@components/ui/card'
import { Button } from '@components/ui/button'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@components/ui/select'
import { Label } from '@components/ui/label'
import { Alert, AlertDescription } from '@components/ui/alert'
import { Input } from '@components/ui/input'
import { Textarea } from '@components/ui/textarea'
import { Switch } from '@components/ui/switch'
import { Loader2, Save, RotateCcw, CheckCircle2, XCircle, Mail, Calendar } from 'lucide-react'
import { accessibilityService } from '@services/accessibility.service'
import type { Configuration, WCAGLevel, WCAGVersion, ScheduleFrequency } from '@types'

export function ConfigurationApp() {
  const [configuration, setConfiguration] = useState<Configuration>({
    wcagVersion: '2.2',
    wcagLevel: 'AA',
    emailEnabled: false,
    emailRecipients: '',
    emailFrom: 'noreply@easya11y.com',
    emailOnViolations: true,
    emailDigest: true,
    scanScheduleEnabled: false,
    scanScheduleCron: '0 0 9 * * MON',
    scheduleFrequency: 'weekly',
    serverSideScan: true,
    scanPaths: '',
    excludePaths: ''
  })
  const [isLoading, setIsLoading] = useState(true)
  const [isSaving, setIsSaving] = useState(false)
  const [alert, setAlert] = useState<{ type: 'success' | 'error', message: string } | null>(null)

  useEffect(() => {
    loadConfiguration()
  }, [])

  useEffect(() => {
    if (alert?.type === 'success') {
      const timer = setTimeout(() => setAlert(null), 5000)
      return () => clearTimeout(timer)
    }
  }, [alert])

  const loadConfiguration = async () => {
    setIsLoading(true)
    setAlert(null)
    
    try {
      const config = await accessibilityService.getConfiguration()
      setConfiguration({
        wcagVersion: config.wcagVersion || '2.2',
        wcagLevel: config.wcagLevel || 'AA',
        emailEnabled: String(config.emailEnabled) === 'true',
        emailRecipients: config.emailRecipients || '',
        emailFrom: config.emailFrom || 'noreply@easya11y.com',
        emailOnViolations: String(config.emailOnViolations) !== 'false',
        emailDigest: String(config.emailDigest) !== 'false',
        scanScheduleEnabled: String(config.scanScheduleEnabled) === 'true',
        scanScheduleCron: config.scanScheduleCron || '0 0 9 * * MON',
        scheduleFrequency: config.scheduleFrequency || 'weekly',
        serverSideScan: String(config.serverSideScan) !== 'false',
        scanPaths: config.scanPaths || '',
        excludePaths: config.excludePaths || ''
      })
    } catch (error) {
      setAlert({
        type: 'error',
        message: `Error loading configuration: ${error instanceof Error ? error.message : 'Unknown error'}`
      })
    } finally {
      setIsLoading(false)
    }
  }

  const saveConfiguration = async () => {
    setIsSaving(true)
    setAlert(null)
    
    try {
      await accessibilityService.saveConfiguration(configuration)
      setAlert({
        type: 'success',
        message: 'Configuration saved successfully'
      })
    } catch (error) {
      setAlert({
        type: 'error',
        message: `Error saving configuration: ${error instanceof Error ? error.message : 'Unknown error'}`
      })
    } finally {
      setIsSaving(false)
    }
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    saveConfiguration()
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center space-y-4">
          <Loader2 className="h-8 w-8 animate-spin mx-auto text-primary" />
          <p className="text-muted-foreground">Loading configuration...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="container max-w-3xl mx-auto py-8 px-4">
      {alert && (
        <Alert className={`mb-6 ${alert.type === 'success' ? 'border-green-600' : 'border-destructive'}`}>
          {alert.type === 'success' ? (
            <CheckCircle2 className="h-4 w-4 text-green-600" />
          ) : (
            <XCircle className="h-4 w-4" />
          )}
          <AlertDescription className={alert.type === 'success' ? 'text-green-600' : ''}>
            {alert.message}
          </AlertDescription>
        </Alert>
      )}

      <form onSubmit={handleSubmit}>
        <Card>
          <CardHeader>
            <CardTitle>WCAG Accessibility Settings</CardTitle>
            <CardDescription>
              Configure default settings for accessibility scanning
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="space-y-2">
              <Label htmlFor="wcagVersion">Default WCAG Version</Label>
              <Select
                value={configuration.wcagVersion}
                onValueChange={(value) => setConfiguration({ ...configuration, wcagVersion: value as WCAGVersion })}
                disabled={isSaving}
              >
                <SelectTrigger id="wcagVersion">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="2.0">WCAG 2.0</SelectItem>
                  <SelectItem value="2.1">WCAG 2.1</SelectItem>
                  <SelectItem value="2.2">WCAG 2.2</SelectItem>
                </SelectContent>
              </Select>
              <p className="text-sm text-muted-foreground">
                Default WCAG version for accessibility checks
              </p>
            </div>

            <div className="space-y-2">
              <Label htmlFor="wcagLevel">Default WCAG Conformance Level</Label>
              <Select
                value={configuration.wcagLevel}
                onValueChange={(value) => setConfiguration({ ...configuration, wcagLevel: value as WCAGLevel })}
                disabled={isSaving}
              >
                <SelectTrigger id="wcagLevel">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="A">Level A</SelectItem>
                  <SelectItem value="AA">Level AA</SelectItem>
                  <SelectItem value="AAA">Level AAA</SelectItem>
                </SelectContent>
              </Select>
              <p className="text-sm text-muted-foreground">
                Default conformance level for accessibility checks
              </p>
            </div>

            <div className="flex items-center space-x-2">
              <Switch
                id="serverSideScan"
                checked={configuration.serverSideScan}
                onCheckedChange={(checked: boolean) => setConfiguration({ ...configuration, serverSideScan: checked })}
                disabled={isSaving}
              />
              <Label htmlFor="serverSideScan">Use server-side scanning</Label>
            </div>
            <p className="text-sm text-muted-foreground ml-8">
              Run accessibility scans on the server instead of in the browser (recommended for scheduled scans)
            </p>
          </CardContent>
        </Card>

        <Card className="mt-6">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Mail className="h-5 w-5" />
              Email Notification Settings
            </CardTitle>
            <CardDescription>
              Configure email notifications for accessibility violations
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="flex items-center space-x-2">
              <Switch
                id="emailEnabled"
                checked={configuration.emailEnabled}
                onCheckedChange={(checked: boolean) => setConfiguration({ ...configuration, emailEnabled: checked })}
                disabled={isSaving}
              />
              <Label htmlFor="emailEnabled">Enable email notifications</Label>
            </div>

            {configuration.emailEnabled && (
              <>
                <div className="space-y-2">
                  <Label htmlFor="emailRecipients">Recipients</Label>
                  <Textarea
                    id="emailRecipients"
                    placeholder="user1@example.com, user2@example.com"
                    value={configuration.emailRecipients}
                    onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setConfiguration({ ...configuration, emailRecipients: e.target.value })}
                    disabled={isSaving}
                    rows={3}
                  />
                  <p className="text-sm text-muted-foreground">
                    Comma-separated list of email addresses
                  </p>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="emailFrom">From Address</Label>
                  <Input
                    id="emailFrom"
                    type="email"
                    value={configuration.emailFrom}
                    onChange={(e) => setConfiguration({ ...configuration, emailFrom: e.target.value })}
                    disabled={isSaving}
                  />
                  <p className="text-sm text-muted-foreground">
                    Sender email address for notifications
                  </p>
                </div>

                <div className="space-y-4">
                  <div className="flex items-center space-x-2">
                    <Switch
                      id="emailOnViolations"
                      checked={configuration.emailOnViolations}
                      onCheckedChange={(checked: boolean) => setConfiguration({ ...configuration, emailOnViolations: checked })}
                      disabled={isSaving}
                    />
                    <Label htmlFor="emailOnViolations">Send email for each page with violations</Label>
                  </div>

                  <div className="flex items-center space-x-2">
                    <Switch
                      id="emailDigest"
                      checked={configuration.emailDigest}
                      onCheckedChange={(checked: boolean) => setConfiguration({ ...configuration, emailDigest: checked })}
                      disabled={isSaving}
                    />
                    <Label htmlFor="emailDigest">Send digest email for batch scans</Label>
                  </div>
                </div>
              </>
            )}
          </CardContent>
        </Card>

        <Card className="mt-6">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Calendar className="h-5 w-5" />
              Scheduled Scanning
            </CardTitle>
            <CardDescription>
              Configure automated accessibility scans
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="flex items-center space-x-2">
              <Switch
                id="scanScheduleEnabled"
                checked={configuration.scanScheduleEnabled}
                onCheckedChange={(checked: boolean) => setConfiguration({ ...configuration, scanScheduleEnabled: checked })}
                disabled={isSaving}
              />
              <Label htmlFor="scanScheduleEnabled">Enable scheduled scans</Label>
            </div>

            {configuration.scanScheduleEnabled && (
              <>
                <div className="space-y-2">
                  <Label htmlFor="scheduleFrequency">Scan Frequency</Label>
                  <Select
                    value={configuration.scheduleFrequency}
                    onValueChange={(value) => {
                      const freq = value as ScheduleFrequency
                      let cron = configuration.scanScheduleCron
                      
                      switch (freq) {
                        case 'daily':
                          cron = '0 0 9 * * *' // Every day at 9 AM
                          break
                        case 'weekly':
                          cron = '0 0 9 * * MON' // Every Monday at 9 AM
                          break
                        case 'monthly':
                          cron = '0 0 9 1 * *' // First day of month at 9 AM
                          break
                      }
                      
                      setConfiguration({ 
                        ...configuration, 
                        scheduleFrequency: freq,
                        scanScheduleCron: freq !== 'custom' ? cron : configuration.scanScheduleCron
                      })
                    }}
                    disabled={isSaving}
                  >
                    <SelectTrigger id="scheduleFrequency">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="daily">Daily</SelectItem>
                      <SelectItem value="weekly">Weekly</SelectItem>
                      <SelectItem value="monthly">Monthly</SelectItem>
                      <SelectItem value="custom">Custom</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                {configuration.scheduleFrequency === 'custom' && (
                  <div className="space-y-2">
                    <Label htmlFor="scanScheduleCron">Custom Schedule (Cron Expression)</Label>
                    <Input
                      id="scanScheduleCron"
                      value={configuration.scanScheduleCron}
                      onChange={(e) => setConfiguration({ ...configuration, scanScheduleCron: e.target.value })}
                      disabled={isSaving}
                      placeholder="0 0 9 * * MON"
                    />
                    <p className="text-sm text-muted-foreground">
                      Example: 0 0 9 * * MON (Every Monday at 9:00 AM)
                    </p>
                  </div>
                )}

                <div className="space-y-2">
                  <Label htmlFor="scanPaths">Paths to Scan</Label>
                  <Textarea
                    id="scanPaths"
                    placeholder="/home, /products, /about"
                    value={configuration.scanPaths}
                    onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setConfiguration({ ...configuration, scanPaths: e.target.value })}
                    disabled={isSaving}
                    rows={3}
                  />
                  <p className="text-sm text-muted-foreground">
                    Comma-separated list of paths to scan. Leave empty to scan all pages.
                  </p>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="excludePaths">Paths to Exclude</Label>
                  <Textarea
                    id="excludePaths"
                    placeholder="/admin, /internal, /temp"
                    value={configuration.excludePaths}
                    onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setConfiguration({ ...configuration, excludePaths: e.target.value })}
                    disabled={isSaving}
                    rows={3}
                  />
                  <p className="text-sm text-muted-foreground">
                    Comma-separated list of paths to exclude from scanning.
                  </p>
                </div>
              </>
            )}
          </CardContent>
        </Card>

        <div className="flex justify-end gap-3 pt-6">
              <Button
                type="button"
                variant="outline"
                onClick={loadConfiguration}
                disabled={isSaving}
              >
                <RotateCcw className="mr-2 h-4 w-4" />
                Reset
              </Button>
              <Button
                type="submit"
                disabled={isSaving}
              >
                {isSaving ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Saving...
                  </>
                ) : (
                  <>
                    <Save className="mr-2 h-4 w-4" />
                    Save Configuration
                  </>
                )}
              </Button>
            </div>
      </form>
    </div>
  )
}