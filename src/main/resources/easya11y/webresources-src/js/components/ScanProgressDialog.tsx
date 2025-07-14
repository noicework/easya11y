import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@components/ui/dialog'
import { Progress } from '@components/ui/progress'
import { Button } from '@components/ui/button'
import { Loader2, X } from 'lucide-react'
import type { ScanProgress } from '@types'

interface ScanProgressDialogProps {
  isOpen: boolean
  progress: ScanProgress | null
  onCancel: () => void
}

export function ScanProgressDialog({
  isOpen,
  progress,
  onCancel,
}: ScanProgressDialogProps) {
  const percentage = progress && progress.total > 0 
    ? Math.round((progress.current / progress.total) * 100) 
    : 0

  if (!progress) {
    return (
      <Dialog open={isOpen} onOpenChange={(open) => !open && onCancel()}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Preparing Scan</DialogTitle>
            <DialogDescription>
              Initializing accessibility scan...
            </DialogDescription>
          </DialogHeader>
          <div className="flex items-center justify-center py-8">
            <Loader2 className="h-8 w-8 animate-spin" />
          </div>
        </DialogContent>
      </Dialog>
    )
  }

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onCancel()}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Scanning Pages</DialogTitle>
          <DialogDescription>
            Scanning {progress.current} of {progress.total} pages
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4 py-4">
          <div className="space-y-2">
            <div className="flex justify-between text-sm">
              <span>Progress</span>
              <span className="font-medium">{percentage}%</span>
            </div>
            <Progress value={percentage} className="h-2" />
          </div>

          {progress.currentPage && (
            <div className="flex items-center gap-2">
              <Loader2 className="h-4 w-4 animate-spin" />
              <span className="text-sm text-muted-foreground truncate">
                Scanning: {progress.currentPage}
              </span>
            </div>
          )}

          <div className="text-sm space-y-1">
            <div className="flex justify-between">
              <span className="text-muted-foreground">Pages scanned:</span>
              <span className="font-medium tabular-nums">
                {progress.current} / {progress.total}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">Time elapsed:</span>
              <span className="font-medium tabular-nums">
                {Math.floor((progress.current * 2) / 60)}m {(progress.current * 2) % 60}s
              </span>
            </div>
          </div>
        </div>

        <DialogFooter>
          <Button
            variant="outline"
            onClick={onCancel}
            className="w-full"
          >
            <X className="mr-2 h-4 w-4" />
            Cancel Scan
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}