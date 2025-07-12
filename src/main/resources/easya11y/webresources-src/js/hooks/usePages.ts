import { useQuery } from '@tanstack/react-query'
import { accessibilityService } from '@services/accessibility.service'
import type { Page } from '@types/index'

export function usePages() {
  return useQuery<Page[], Error>({
    queryKey: ['pages'],
    queryFn: () => accessibilityService.getPages(true),
    staleTime: 5 * 60 * 1000, // 5 minutes
  })
}