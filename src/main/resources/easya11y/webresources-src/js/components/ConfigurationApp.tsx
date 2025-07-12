import { useState, useEffect } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@components/ui/card'
import { Button } from '@components/ui/button'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@components/ui/select'
import { Label } from '@components/ui/label'
import { Alert, AlertDescription } from '@components/ui/alert'
import { Loader2, Save, RotateCcw, CheckCircle2, XCircle } from 'lucide-react'
import { accessibilityService } from '@services/accessibility.service'
import type { Configuration, WCAGLevel, WCAGVersion } from '@types'

export function ConfigurationApp() {
  const [configuration, setConfiguration] = useState<Configuration>({
    wcagVersion: '2.2',
    wcagLevel: 'AA'
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
        wcagLevel: config.wcagLevel || 'AA'
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

            <div className="flex justify-end gap-3 pt-4">
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
          </CardContent>
        </Card>
      </form>
    </div>
  )
}