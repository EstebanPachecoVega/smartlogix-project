"use client"

import { useTheme } from "next-themes"
import { useEffect, useState } from "react"
import { Moon, Sun, Monitor } from "lucide-react"

export default function ThemeToggle() {
  const [mounted, setMounted] = useState(false)
  const { theme, resolvedTheme, setTheme } = useTheme()

  useEffect(() => setMounted(true), [])

  if (!mounted) {
    return <div className="size-9 rounded-md bg-muted animate-pulse" />
  }

  const isDark = resolvedTheme === "dark"
  const isSystem = theme === "system"

  return (
    <button
      onClick={() => {
        if (isSystem && isDark) setTheme("light")
        else if (isSystem && !isDark) setTheme("dark")
        else setTheme(isDark ? "light" : "dark")
      }}
      className="relative size-8 inline-flex items-center justify-center rounded-md hover:bg-accent hover:text-accent-foreground transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
      aria-label={isDark ? "Tema claro" : "Tema oscuro"}
      title={isSystem ? "Usando preferencia del sistema" : undefined}
    >
      <div className="relative size-5">
        <Sun
          className={`absolute inset-0 size-5 transition-all duration-300 ${
            isDark ? "rotate-0 scale-100 opacity-100" : "rotate-90 scale-0 opacity-0"
          }`}
        />
        <Moon
          className={`absolute inset-0 size-5 transition-all duration-300 ${
            isDark ? "-rotate-90 scale-0 opacity-0" : "rotate-0 scale-100 opacity-100"
          }`}
        />
      </div>
      {isSystem && (
        <span className="absolute -top-0.5 -right-0.5 flex size-2">
          <span className="absolute inset-0 rounded-full bg-muted-foreground/50" />
          <span className="absolute inset-0 rounded-full bg-muted-foreground/50 animate-ping" />
        </span>
      )}
    </button>
  )
}
