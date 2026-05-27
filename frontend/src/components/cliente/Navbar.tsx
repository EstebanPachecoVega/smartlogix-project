'use client';

import Link from 'next/link';
import { useSession, signOut } from "next-auth/react";
import { ShoppingCart, Package, Truck, LogOut } from 'lucide-react';
import { useTotalItems } from '@/store/carritoStore';
import { Button } from '@/components/ui/button';

export default function NavbarCliente() {
  const { data: session } = useSession();
  const totalItems = useTotalItems();

  const handleLogout = () => signOut({ callbackUrl: '/login' });

  return (
    <nav className="border-b bg-white">
      <div className="container mx-auto flex justify-between items-center px-4 py-3">
        <Link href="/cliente" className="text-xl font-bold">
          SmartLogix
        </Link>
        <div className="flex items-center gap-4">
          {session ? (
            <>
              <span className="text-sm text-gray-600">Hola, {session.user?.name || session.user?.email}</span>
              <Button variant="ghost" size="sm" onClick={handleLogout}>
                <LogOut className="h-4 w-4 mr-1" /> Salir
              </Button>
            </>
          ) : null}
          <Link href="/cliente/pedidos">
            <Button variant="ghost" size="sm">
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