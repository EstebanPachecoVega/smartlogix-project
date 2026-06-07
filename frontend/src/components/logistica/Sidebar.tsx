'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useSession } from "next-auth/react";
import { LayoutDashboard, Package, Tag, ShoppingCart, Truck, Search, Store, User } from 'lucide-react';
import { cn } from '@/lib/utils';
import { useEffect, useState } from 'react';
import { getUserProfile, UserProfile } from '@/lib/userService';
import { censorEmail } from '@/lib/emailUtils';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';

const items = [
  { href: '/logistica', label: 'Dashboard', icon: LayoutDashboard },
  { href: '/logistica/productos', label: 'Productos', icon: Package },
  { href: '/logistica/categorias', label: 'Categorías', icon: Tag },
  { href: '/logistica/pedidos', label: 'Pedidos', icon: ShoppingCart },
  { href: '/logistica/envios', label: 'Envíos', icon: Truck },
  { href: '/logistica/buscar', label: 'Buscar envío', icon: Search },
];

export default function SidebarLogistica() {
  const pathname = usePathname();
  const { data: session } = useSession();
  const [perfil, setPerfil] = useState<UserProfile | null>(null);

  useEffect(() => {
    if (session) {
      getUserProfile().then(profile => setPerfil(profile));
    }
  }, [session]);

  const nombreCompleto = perfil
    ? [perfil.primerNombre, perfil.segundoNombre, perfil.primerApellido, perfil.segundoApellido].filter(Boolean).join(' ')
    : session?.user?.name || 'Gestor';

  const userInitials = nombreCompleto
    .split(' ')
    .map(n => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);

  const roles = session?.roles || [];
  const rolTexto = roles.includes('gestor') ? 'Gestor Logístico' : roles.includes('cliente') ? 'Cliente' : '';

  const emailCensurado = censorEmail(session?.user?.email || '');

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

      <div className="border-t pt-4 mt-4 space-y-3">
        <div className="flex items-center gap-3 px-2">
          <Avatar className="h-10 w-10">
            <AvatarFallback className="bg-blue-100 text-blue-800">
              {userInitials}
            </AvatarFallback>
          </Avatar>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-semibold truncate">{nombreCompleto}</p>
            <p className="text-xs text-gray-500 truncate">{emailCensurado}</p>
            <p className="text-xs text-blue-600 font-medium mt-0.5">{rolTexto}</p>
          </div>
        </div>
        <Link
          href="/logistica/perfil"
          className="flex items-center gap-2 px-4 py-2 rounded-lg text-gray-600 hover:bg-gray-100 transition-colors text-sm"
        >
          <User className="h-4 w-4" />
          Editar perfil
        </Link>
        <Link
          href="/"
          className="flex items-center gap-2 px-4 py-2 rounded-lg text-gray-600 hover:bg-gray-100 transition-colors"
        >
          <Store className="h-5 w-5" />
          Volver a la tienda
        </Link>
      </div>
    </aside>
  );
}