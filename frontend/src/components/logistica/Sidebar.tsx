'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { LayoutDashboard, Package, Tag, ShoppingCart, Truck, Search, Store } from 'lucide-react';

const items = [
  { href: '/logistica', label: 'Dashboard', icon: LayoutDashboard },
  { href: '/logistica/productos', label: 'Productos', icon: Package },
  { href: '/logistica/categorias', label: 'Categorías', icon: Tag },
  { href: '/logistica/pedidos', label: 'Pedidos', icon: ShoppingCart },
  { href: '/logistica/envios', label: 'Envíos', icon: Truck },
  { href: '/logistica/buscar', label: 'Buscar envío', icon: Search },
];

function cn(...classes: (string | boolean | undefined)[]) {
  return classes.filter(Boolean).join(' ');
}

export default function SidebarLogistica() {
  const pathname = usePathname();

  return (
    <aside className="w-64 border-r bg-gray-50 p-4 flex flex-col h-screen sticky top-0">
      <Link href="/logistica" className="mb-8 px-4 text-xl font-bold hover:text-blue-600 transition-colors">
        SmartLogix
      </Link>

      <nav className="flex-1 space-y-2">
        {items.map((item) => {
          const isActive = pathname === item.href;
          return (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                'flex items-center gap-2 px-4 py-2 rounded-lg transition-colors',
                isActive && 'bg-gray-200 font-semibold',
                !isActive && 'hover:bg-gray-100'
              )}
            >
              <item.icon className="h-5 w-5" />
              {item.label}
            </Link>
          );
        })}
      </nav>

      <div className="border-t pt-4 mt-4">
        <Link
          href="/cliente"
          className="flex items-center gap-2 px-4 py-2 rounded-lg text-gray-600 hover:bg-gray-100 transition-colors"
        >
          <Store className="h-5 w-5" />
          Volver a la tienda
        </Link>
      </div>
    </aside>
  );
}