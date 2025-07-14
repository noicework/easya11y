import { useState, useMemo } from 'react'
import { Check, ChevronsUpDown } from 'lucide-react'
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

  const groupedPages = useMemo(() => {
    const groups = new Map<string, Page[]>()
    
    filteredPages.forEach((page) => {
      const parts = page.path.split('/')
      const group = parts.length > 2 ? parts[1] : 'Root'
      
      if (!groups.has(group)) {
        groups.set(group, [])
      }
      groups.get(group)!.push(page)
    })

    return Array.from(groups.entries()).sort(([a], [b]) => a.localeCompare(b))
  }, [filteredPages])

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
            {groupedPages.map(([group, groupPages]) => (
              <CommandGroup key={group} heading={group}>
                {groupPages.map((page) => {
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
                      className="flex items-center justify-between"
                    >
                      <div className="flex flex-col items-start">
                        <span className={cn('truncate', isSelected && 'font-medium')}>
                          {label}
                        </span>
                        <span className="text-xs text-muted-foreground truncate max-w-[300px]">
                          {page.path}
                        </span>
                      </div>
                      <Check
                        className={cn(
                          'ml-2 h-4 w-4',
                          isSelected ? 'opacity-100' : 'opacity-0'
                        )}
                      />
                    </CommandItem>
                  )
                })}
              </CommandGroup>
            ))}
          </CommandList>
        </Command>
      </PopoverContent>
    </Popover>
  )
}