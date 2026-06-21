'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useSession } from 'next-auth/react';
import {
  LayoutDashboard, Package, Tag, ShoppingCart,
  Truck, Search, Store, User, Menu, X,
} from 'lucide-react';
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
  const [mobileOpen, setMobileOpen] = useState(false);

  useEffect(() => {
    if (session) {
      getUserProfile().then((profile) => setPerfil(profile));
    }
  }, [session]);

  // Cerrar sidebar móvil al cambiar de ruta
  useEffect(() => {
    setMobileOpen(false);
  }, [pathname]);

  const nombreCompleto = perfil
    ? [perfil.primerNombre, perfil.segundoNombre, perfil.primerApellido, perfil.segundoApellido]
      .filter(Boolean)
      .join(' ')
    : session?.user?.name || 'Gestor';

  const userInitials = nombreCompleto
    .split(' ')
    .map((n) => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);

  const roles = session?.roles || [];
  const rolTexto = roles.includes('gestor')
    ? 'Gestor Logístico'
    : roles.includes('cliente')
      ? 'Cliente'
      : '';

  const emailCensurado = censorEmail(session?.user?.email || '');

  const NavContent = () => (
    <>
      {/* Logo / título */}
      <div className="px-4 py-4 border-b">
        <p className="text-xs font-semibold uppercase tracking-widest text-muted-foreground">
          Panel Logística
        </p>
      </div>

      {/* Navegación */}
      <nav className="flex-1 px-2 py-4 space-y-1 overflow-y-auto">
        {items.map((item) => {
          const isActive =
            item.href === '/logistica'
              ? pathname === '/logistica'
              : pathname.startsWith(item.href);

          return (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors',
                isActive
                  ? 'bg-primary text-primary-foreground'
                  : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground',
              )}
            >
              <item.icon className="h-4 w-4 shrink-0" />
              {item.label}
            </Link>
          );
        })}
      </nav>

      {/* Footer del sidebar */}
      <div className="border-t px-2 py-4 space-y-1">
        <div className="flex items-center gap-3 px-3 py-2">
          <Avatar className="h-9 w-9 shrink-0">
            <AvatarFallback className="bg-primary/10 text-primary text-xs font-semibold">
              {userInitials}
            </AvatarFallback>
          </Avatar>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-semibold truncate">{nombreCompleto}</p>
            <p className="text-xs text-muted-foreground truncate">{emailCensurado}</p>
            {rolTexto && (
              <p className="text-xs text-primary font-medium mt-0.5">{rolTexto}</p>
            )}
          </div>
        </div>

        <Link
          href="/logistica/perfil"
          className="flex items-center gap-3 px-3 py-2 rounded-lg text-sm text-muted-foreground hover:bg-accent hover:text-accent-foreground transition-colors"
        >
          <User className="h-4 w-4 shrink-0" />
          Editar perfil
        </Link>

        <Link
          href="/"
          className="flex items-center gap-3 px-3 py-2 rounded-lg text-sm text-muted-foreground hover:bg-accent hover:text-accent-foreground transition-colors"
        >
          <Store className="h-4 w-4 shrink-0" />
          Volver a la tienda
        </Link>
      </div>
    </>
  );

  return (
    <>
      {/* Botón hamburguesa (móvil) */}
      <button
        onClick={() => setMobileOpen(true)}
        className="lg:hidden fixed top-[4.5rem] left-4 z-50 p-2 rounded-md bg-background border shadow-sm"
        aria-label="Abrir menú"
      >
        <Menu className="h-5 w-5" />
      </button>

      {/* Overlay móvil */}
      {mobileOpen && (
        <div
          className="lg:hidden fixed inset-0 z-40 bg-black/40 backdrop-blur-sm"
          onClick={() => setMobileOpen(false)}
        />
      )}

      {/* Drawer móvil */}
      <aside
        className={cn(
          'lg:hidden fixed inset-y-0 left-0 z-50 w-72 flex flex-col bg-card border-r',
          'transition-transform duration-300 ease-in-out',
          mobileOpen ? 'translate-x-0' : '-translate-x-full',
          // Empezar desde debajo del navbar (h-16 = 4rem)
          'top-16',
        )}
      >
        <div className="flex items-center justify-between px-4 py-3 border-b">
          <p className="text-xs font-semibold uppercase tracking-widest text-muted-foreground">
            Menú
          </p>
          <button
            onClick={() => setMobileOpen(false)}
            className="p-1 rounded hover:bg-accent"
            aria-label="Cerrar menú"
          >
            <X className="h-5 w-5" />
          </button>
        </div>
        <div className="flex flex-col flex-1 overflow-hidden">
          <NavContent />
        </div>
      </aside>

      {/* Sidebar desktop (fijo, debajo del navbar) */}
      <aside className="hidden lg:flex lg:flex-col fixed top-16 left-0 bottom-0 w-64 z-40 bg-card border-r">
        <NavContent />
      </aside>
    </>
  );
}