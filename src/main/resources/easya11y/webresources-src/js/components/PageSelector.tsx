import { useState, useMemo } from 'react'
import { Check, ChevronsUpDown, Star } from 'lucide-react'
import { cn } from '@lib/utils'
import { Button } from '@components/ui/button'
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from '@components/ui/command'
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@components/ui/popover'
import { usePinnedPages } from '@hooks/usePinnedPages'
import type { Page } from '@types'

interface PageSelectorProps {
  pages: Page[]
  value: Page | null
  onSelect: (page: Page | null) => void
  placeholder?: string
  disabled?: boolean
  className?: string
}

export function PageSelector({
  pages,
  value,
  onSelect,
  placeholder = 'Select a page...',
  disabled = false,
  className,
}: PageSelectorProps) {
  const [open, setOpen] = useState(false)
  const [search, setSearch] = useState('')
  const { pinnedPages, togglePin, isPinned } = usePinnedPages()

  const filteredPages = useMemo(() => {
    if (!search) return pages

    const searchLower = search.toLowerCase()
    return pages.filter((page) => {
      const title = page.title || page.name || ''
      const path = page.path || ''
      return (
        title.toLowerCase().includes(searchLower) ||
        path.toLowerCase().includes(searchLower)
      )
    })
  }, [pages, search])

  const groupedPages = useMemo((): Array<[string, Page[]]> => {
    const groups = new Map<string, Page[]>()
    const pinnedGroup: Page[] = []
    
    filteredPages.forEach((page) => {
      // Check if page is pinned
      if (isPinned(page.path)) {
        pinnedGroup.push(page)
      } else {
        const parts = page.path.split('/')
        const group = parts.length > 2 ? parts[1] : 'Root'
        
        if (!groups.has(group)) {
          groups.set(group, [])
        }
        groups.get(group)!.push(page)
      }
    })

    // Sort groups and add pinned pages at the top if any
    const sortedGroups = Array.from(groups.entries()).sort(([a], [b]) => a.localeCompare(b))
    
    if (pinnedGroup.length > 0) {
      return [['Pinned Pages', pinnedGroup], ...sortedGroups]
    }
    
    return sortedGroups
  }, [filteredPages, pinnedPages, isPinned])

  const selectedLabel = value
    ? value.title || value.name || value.path
    : placeholder

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button
          variant="outline"
          role="combobox"
          aria-expanded={open}
          className={cn('w-full justify-between', className)}
          disabled={disabled}
        >
          <span className="truncate">{selectedLabel}</span>
          <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-[400px] p-0" align="start">
        <Command shouldFilter={false}>
          <CommandInput
            placeholder="Search pages..."
            value={search}
            onValueChange={setSearch}
          />
          <CommandList>
            <CommandEmpty>No pages found.</CommandEmpty>
            {groupedPages.map(([group, groupPages]) => {
              if (!Array.isArray(groupPages)) return null
              return (
                <CommandGroup key={`group-${group}`} heading={group}>
                  {groupPages.map((page: Page) => {
                  const isSelected = value?.path === page.path
                  const label = page.title || page.name || page.path
                  
                  return (
                    <CommandItem
                      key={page.path}
                      value={page.path}
                      onSelect={() => {
                        onSelect(isSelected ? null : page)
                        setOpen(false)
                      }}
                      className="flex items-center justify-between group"
                    >
                      <div className="flex flex-col items-start flex-1 min-w-0">
                        <div className="flex items-center gap-1">
                          <span className={cn('truncate', isSelected && 'font-medium')}>
                            {label}
                          </span>
                          {isPinned(page.path) && (
                            <Star className="h-3 w-3 fill-yellow-500 text-yellow-500" />
                          )}
                        </div>
                        <span className="text-xs text-muted-foreground truncate max-w-[300px]">
                          {page.path}
                        </span>
                      </div>
                      <div className="flex items-center gap-1 ml-2">
                        <Button
                          size="icon"
                          variant="ghost"
                          className="h-6 w-6 opacity-0 group-hover:opacity-100 transition-opacity"
                          onClick={(e) => {
                            e.stopPropagation()
                            togglePin(page.path)
                          }}
                        >
                          <Star 
                            className={cn(
                              "h-3 w-3",
                              isPinned(page.path) 
                                ? "fill-yellow-500 text-yellow-500" 
                                : "text-muted-foreground"
                            )}
                          />
                        </Button>
                        <Check
                          className={cn(
                            'h-4 w-4',
                            isSelected ? 'opacity-100' : 'opacity-0'
                          )}
                        />
                      </div>
                    </CommandItem>
                  )
                  })}
                </CommandGroup>
              )
            })}
          </CommandList>
        </Command>
      </PopoverContent>
    </Popover>
  )
}