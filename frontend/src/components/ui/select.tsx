"use client"

import * as React from "react"
import { Select as SelectPrimitive } from "@base-ui/react"

import { cn } from "@/lib/utils"
import { ChevronDownIcon, CheckIcon } from "lucide-react"

type SelectItemLabelContextValue = {
  getLabel: (value: string) => string | undefined
  registerLabel: (value: string, label: string) => void
  unregisterLabel: (value: string) => void
}

const SelectItemLabelContext = React.createContext<SelectItemLabelContextValue>({
  getLabel: () => undefined,
  registerLabel: () => {},
  unregisterLabel: () => {},
})

function extractLabelsFromChildren(children: React.ReactNode): Record<string, string> {
  const map: Record<string, string> = {}
  const walk = (node: React.ReactNode) => {
    React.Children.forEach(node, (child) => {
      if (!React.isValidElement(child)) return
      const p = child.props as Record<string, unknown>
      if (child.type === SelectItem) {
        const value = p.value != null ? String(p.value) : ""
        map[value] = typeof p.children === "string" ? p.children : value
      }
      if (p.children) {
        walk(p.children as React.ReactNode)
      }
    })
  }
  walk(children)
  return map
}

function Select<Value, Multiple extends boolean | undefined = false>({
  modal = false,
  children,
  ...props
}: SelectPrimitive.Root.Props<Value, Multiple>) {
  const [labelMap, setLabelMap] = React.useState<Record<string, string>>(() =>
    extractLabelsFromChildren(children),
  )
  const registerLabel = React.useCallback((value: string, label: string) => {
    setLabelMap((prev) => ({ ...prev, [value]: label }))
  }, [])
  const unregisterLabel = React.useCallback((value: string) => {
    setLabelMap((prev) => {
      const next = { ...prev }
      delete next[value]
      return next
    })
  }, [])
  const ctx = React.useMemo(
    () => ({
      getLabel: (value: string) => labelMap[value],
      registerLabel,
      unregisterLabel,
    }),
    [labelMap, registerLabel, unregisterLabel],
  )
  return (
    <SelectItemLabelContext.Provider value={ctx}>
      <SelectPrimitive.Root data-slot="select" modal={modal} {...props}>
        {children}
      </SelectPrimitive.Root>
    </SelectItemLabelContext.Provider>
  )
}

function SelectGroup({
  className,
  ...props
}: SelectPrimitive.Group.Props) {
  return (
    <SelectPrimitive.Group
      data-slot="select-group"
      className={cn("scroll-my-1 p-1", className)}
      {...props}
    />
  )
}

function SelectValue({
  children,
  placeholder,
  ...props
}: SelectPrimitive.Value.Props) {
  const { getLabel } = React.useContext(SelectItemLabelContext)
  return (
    <SelectPrimitive.Value data-slot="select-value" {...props}>
      {(value: any) => {
        if (children !== undefined) {
          return typeof children === "function" ? children(value) : children
        }
        if (value != null) {
          const label = getLabel(String(value))
          if (label != null) return label
        }
        if (placeholder != null && (value == null || value === "")) {
          return placeholder
        }
        return String(value ?? "")
      }}
    </SelectPrimitive.Value>
  )
}

function SelectTrigger({
  className,
  size = "default",
  children,
  ...props
}: SelectPrimitive.Trigger.Props & {
  size?: "sm" | "default"
}) {
  return (
    <SelectPrimitive.Trigger
      data-slot="select-trigger"
      data-size={size}
      className={cn(
        "flex w-fit items-center justify-between gap-1.5 rounded-lg border border-input bg-transparent py-2 pr-2 pl-2.5 text-sm whitespace-nowrap transition-colors outline-none select-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 disabled:cursor-not-allowed disabled:opacity-50 aria-invalid:border-destructive aria-invalid:ring-3 aria-invalid:ring-destructive/20 data-placeholder:text-muted-foreground data-[size=default]:h-8 data-[size=sm]:h-7 data-[size=sm]:rounded-[min(var(--radius-md),10px)] *:data-[slot=select-value]:line-clamp-1 *:data-[slot=select-value]:flex *:data-[slot=select-value]:items-center *:data-[slot=select-value]:gap-1.5 dark:bg-input/30 dark:hover:bg-input/50 dark:aria-invalid:border-destructive/50 dark:aria-invalid:ring-destructive/40 [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4",
        className
      )}
      {...props}
    >
      {children}
      <ChevronDownIcon className="pointer-events-none size-4 text-muted-foreground shrink-0" />
    </SelectPrimitive.Trigger>
  )
}

function SelectContent({
  className,
  children,
  side = "bottom",
  sideOffset = 6,
  align = "start",
  alignOffset = 0,
  ...props
}: SelectPrimitive.Popup.Props &
  Pick<SelectPrimitive.Positioner.Props, "side" | "align" | "sideOffset" | "alignOffset">) {
  return (
    <SelectPrimitive.Portal>
      <SelectPrimitive.Positioner
        side={side}
        sideOffset={sideOffset}
        align={align}
        alignOffset={alignOffset}
        alignItemWithTrigger={false}
        className="isolate z-50"
      >
        <SelectPrimitive.Popup
          data-slot="select-content"
          className={cn(
            "relative z-50 max-h-(--available-height) min-w-36 w-(--anchor-width) origin-(--transform-origin) overflow-y-auto rounded-lg bg-popover text-popover-foreground shadow-md ring-1 ring-foreground/10 *:data-[slot=select-group]:p-1 data-[side=bottom]:slide-in-from-top-2 data-[side=left]:slide-in-from-right-2 data-[side=right]:slide-in-from-left-2 data-[side=top]:slide-in-from-bottom-2 data-open:animate-in data-open:fade-in-0 data-open:zoom-in-95 data-closed:animate-out data-closed:fade-out-0 data-closed:zoom-out-95",
            className
          )}
          {...props}
        >
          {children}
        </SelectPrimitive.Popup>
      </SelectPrimitive.Positioner>
    </SelectPrimitive.Portal>
  )
}

function SelectLabel({
  className,
  ...props
}: SelectPrimitive.GroupLabel.Props) {
  return (
    <SelectPrimitive.GroupLabel
      data-slot="select-label"
      className={cn("px-1.5 py-1 text-xs text-muted-foreground", className)}
      {...props}
    />
  )
}

function SelectItem({
  className,
  children,
  ...props
}: SelectPrimitive.Item.Props) {
  const textRef = React.useRef<HTMLDivElement | null>(null)
  const { registerLabel, unregisterLabel } = React.useContext(SelectItemLabelContext)
  const value = props.value != null ? String(props.value) : ""
  const labelFromChildren = typeof children === "string" ? children : null

  React.useLayoutEffect(() => {
    const label = labelFromChildren ?? textRef.current?.textContent ?? value
    registerLabel(value, label)
    return () => unregisterLabel(value)
  }, [value, labelFromChildren])

  return (
    <SelectPrimitive.Item
      data-slot="select-item"
      className={cn(
        "relative flex w-full cursor-default items-center gap-1.5 rounded-md py-1 pr-8 pl-1.5 text-sm outline-hidden select-none data-highlighted:bg-accent data-highlighted:text-accent-foreground not-data-[variant=destructive]:data-highlighted:**:text-accent-foreground data-disabled:pointer-events-none data-disabled:opacity-50 [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4",
        className
      )}
      {...props}
    >
      <SelectPrimitive.ItemText ref={(el) => { textRef.current = el }}>{children}</SelectPrimitive.ItemText>
      <SelectPrimitive.ItemIndicator
        render={
          <span className="pointer-events-none absolute right-2 flex size-4 items-center justify-center" />
        }
      >
        <CheckIcon className="pointer-events-none size-4" />
      </SelectPrimitive.ItemIndicator>
    </SelectPrimitive.Item>
  )
}

function SelectSeparator({
  className,
  ...props
}: SelectPrimitive.Separator.Props) {
  return (
    <SelectPrimitive.Separator
      data-slot="select-separator"
      className={cn("-mx-1 my-1 h-px bg-border", className)}
      {...props}
    />
  )
}

export {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectLabel,
  SelectSeparator,
  SelectTrigger,
  SelectValue,
}
