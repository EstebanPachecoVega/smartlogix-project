'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { LayoutDashboard, Package, Tag, ShoppingCart, Truck, Search } from 'lucide-react';
import { cn } from '@/lib/utils';

const items = [
  { href: '/logistica', label: 'Dashboard', icon: LayoutDashboard },
  { href: '/logistica/productos', label: 'Productos', icon: Package },
  { href: '/logistica/categorias', label: 'Categorías', icon: Tag },
  { href: '/logistica/pedidos', label: 'Pedidos', icon: ShoppingCart },
  { href: '/logistica/envios', label: 'Envíos', icon: Truck },
  { href: '/logistica/buscar', label: 'Buscar', icon: Search },
];

export default function SidebarLogistica() {
  const pathname = usePathname();

  return (
    <aside className="w-64 border-r bg-gray-50 p-4">
      <div className="mb-8 px-4 text-xl font-bold">SmartLogix</div>
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