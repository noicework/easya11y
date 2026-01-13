import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@components/ui/dialog'
import { HistoricalView } from '@components/HistoricalView'
import type { Page } from '@types'

interface HistoricalModalProps {
  isOpen: boolean
  onClose: () => void
  pages: Page[]
  initialPagePath?: string
}

export function HistoricalModal({
  isOpen,
  onClose,
  pages,
  initialPagePath = '',
}: HistoricalModalProps) {
  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="sm:max-w-4xl max-h-[85vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Historical Analysis</DialogTitle>
          <DialogDescription>
            View accessibility trends and historical scan data
          </DialogDescription>
        </DialogHeader>

        <div className="py-4">
          <HistoricalView pages={pages} initialPagePath={initialPagePath} />
        </div>
      </DialogContent>
    </Dialog>
  )
}
