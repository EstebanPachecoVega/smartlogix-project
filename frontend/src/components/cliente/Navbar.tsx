'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { ShoppingCart, Package, Truck } from 'lucide-react';
import { useTotalItems } from '@/store/carritoStore';
import { Button } from '@/components/ui/button';

export default function NavbarCliente() {
  const totalItems = useTotalItems();
  const pathname = usePathname();

  return (
    <nav className="border-b bg-white">
      <div className="container mx-auto flex justify-between items-center px-4 py-3">
        <Link href="/cliente" className="text-xl font-bold">
          SmartLogix
        </Link>
        <div className="flex gap-4">
          <Link href="/cliente/pedidos">
            <Button variant={pathname === '/cliente/pedidos' ? 'default' : 'ghost'} size="sm">
              <Package className="h-4 w-4 mr-1" /> Mis pedidos
            </Button>
          </Link>
          <Link href="/logistica">
            <Button variant="ghost" size="sm">
              <Truck className="h-4 w-4 mr-1" /> Logística
            </Button>
          </Link>
          <Link href="/cliente/carrito">
            <Button variant="outline" className="relative">
              <ShoppingCart className="h-5 w-5" />
              {totalItems > 0 && (
                <span className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full w-5 h-5 text-xs flex items-center justify-center">
                  {totalItems}
                </span>
              )}
            </Button>
          </Link>
        </div>
      </div>
    </nav>
  );
}