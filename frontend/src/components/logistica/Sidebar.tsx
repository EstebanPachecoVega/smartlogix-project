'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { LayoutDashboard, PackageSearch, AlertTriangle, Search } from 'lucide-react';
import { cn } from '@/lib/utils';

const items = [
  { href: '/logistica', label: 'Dashboard', icon: LayoutDashboard },
  { href: '/logistica/envios', label: 'Envíos', icon: PackageSearch },
  { href: '/logistica/problemas', label: 'Problemas', icon: AlertTriangle },
  { href: '/logistica/buscar', label: 'Buscar', icon: Search },
];

export default function Sidebar() {
  const pathname = usePathname();

  return (
    <aside className="w-64 border-r bg-gray-50 p-4">
      <nav className="space-y-2">
        {items.map((item) => {
          const isActive = pathname === item.href;
          return (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                'flex items-center gap-2 px-4 py-2 rounded-lg transition-colors',
                isActive ? 'bg-gray-200 font-semibold' : 'hover:bg-gray-100'
              )}
            >
              <item.icon className="h-5 w-5" />
              {item.label}
            </Link>
          );
        })}
      </nav>
    </aside>
  );
}