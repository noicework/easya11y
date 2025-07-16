import { useState, useEffect } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { accessibilityService } from '@services/accessibility.service'
import type { Configuration } from '@types'

const PINNED_PAGES_LOCAL_KEY = 'easya11y-pinned-pages'

export function usePinnedPages() {
  const queryClient = useQueryClient()
  const [localPinnedPages, setLocalPinnedPages] = useState<string[]>(() => {
    // Initialize from localStorage
    try {
      const stored = localStorage.getItem(PINNED_PAGES_LOCAL_KEY)
      return stored ? JSON.parse(stored) : []
    } catch {
      return []
    }
  })

  // Get configuration which includes pinned pages
  const { data: config } = useQuery({
    queryKey: ['configuration'],
    queryFn: () => accessibilityService.getConfiguration(),
    staleTime: 60 * 1000, // 1 minute
  })

  // Update configuration mutation
  const updateConfigMutation = useMutation({
    mutationFn: (newConfig: Configuration) => 
      accessibilityService.saveConfiguration(newConfig),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['configuration'] })
    },
  })

  // Sync with server config when it loads
  useEffect(() => {
    if (config?.pinnedPages) {
      setLocalPinnedPages(config.pinnedPages)
      localStorage.setItem(PINNED_PAGES_LOCAL_KEY, JSON.stringify(config.pinnedPages))
    }
  }, [config])

  const togglePin = async (pagePath: string) => {
    const isPinned = localPinnedPages.includes(pagePath)
    const newPinnedPages = isPinned
      ? localPinnedPages.filter(p => p !== pagePath)
      : [...localPinnedPages, pagePath]

    // Update local state immediately
    setLocalPinnedPages(newPinnedPages)
    localStorage.setItem(PINNED_PAGES_LOCAL_KEY, JSON.stringify(newPinnedPages))

    // Update server config
    if (config) {
      await updateConfigMutation.mutateAsync({
        ...config,
        pinnedPages: newPinnedPages,
      })
    }
  }

  const isPinned = (pagePath: string) => {
    return localPinnedPages.includes(pagePath)
  }

  return {
    pinnedPages: localPinnedPages,
    togglePin,
    isPinned,
    isUpdating: updateConfigMutation.isPending,
  }
}