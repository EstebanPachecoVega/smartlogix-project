import Link from 'next/link';
import { ChevronRight, Home } from 'lucide-react';

interface BreadcrumbItem {
  label: string;
  href?: string;
}

export default function Breadcrumbs({ items }: { items: BreadcrumbItem[] }) {
  return (
    <nav aria-label="Breadcrumb" className="flex items-center gap-1 text-sm text-muted-foreground mb-4 flex-wrap">
      <Link href="/" aria-label="Inicio" className="hover:text-primary transition-colors">
        <Home className="h-4 w-4 inline" />
      </Link>
      {items.map((item, i) => (
        <span key={i} className="flex items-center gap-1">
          <ChevronRight className="h-3 w-3 shrink-0" />
          {item.href ? (
            <Link href={item.href} className="hover:text-primary transition-colors whitespace-nowrap">
              {item.label}
            </Link>
          ) : (
            <span className="text-foreground font-medium whitespace-nowrap">{item.label}</span>
          )}
        </span>
      ))}
    </nav>
  );
}
