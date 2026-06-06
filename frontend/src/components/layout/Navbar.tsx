'use client';

import Link from 'next/link';
import { useSession, signOut } from "next-auth/react";
import { ShoppingCart, Package, LayoutDashboard, LogOut, User, ChevronDown } from 'lucide-react';
import { useTotalItems } from '@/store/carritoStore';
import { Button } from '@/components/ui/button';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";

export default function Navbar() {
    const { data: session } = useSession();
    const totalItems = useTotalItems();
    const userRoles = session?.roles || [];
    const isGestor = userRoles.includes('gestor');

    const handleLogout = async () => {
        const idToken = session?.idToken;
        if (idToken) {
            const keycloakLogoutUrl = new URL('http://localhost:8180/realms/smartlogix/protocol/openid-connect/logout');
            keycloakLogoutUrl.searchParams.set('id_token_hint', idToken);
            keycloakLogoutUrl.searchParams.set('post_logout_redirect_uri', `${window.location.origin}/login`);
            await signOut({ redirect: false });
            window.location.href = keycloakLogoutUrl.toString();
        } else {
            signOut({ callbackUrl: '/login' });
        }
    };

    // Obtener iniciales para el avatar
    const userInitials = session?.user?.name
        ? session.user.name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)
        : session?.user?.email?.charAt(0).toUpperCase() || 'U';

    return (
        <nav className="border-b bg-white sticky top-0 z-50">
            <div className="container mx-auto flex justify-between items-center px-4 py-3">
                <Link href="/cliente" className="text-xl font-bold hover:text-blue-600 transition-colors">
                    SmartLogix
                </Link>

                <div className="flex items-center gap-2">
                    {/* Carrito - visible siempre */}
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

                    {/* Si el usuario está autenticado, mostrar menú desplegable */}
                    {session ? (
                        <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                                <Button variant="ghost" className="flex items-center gap-2">
                                    <Avatar className="h-8 w-8">
                                        <AvatarFallback className="bg-gray-200 text-gray-700">
                                            {userInitials}
                                        </AvatarFallback>
                                    </Avatar>
                                    <span className="hidden md:inline-block text-sm">
                                        {session.user?.name?.split(' ')[0] || session.user?.email?.split('@')[0]}
                                    </span>
                                    <ChevronDown className="h-4 w-4" />
                                </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end" className="w-56">
                                <DropdownMenuLabel>
                                    <div className="font-normal">
                                        <p className="text-sm font-medium">{session.user?.name || 'Usuario'}</p>
                                        <p className="text-xs text-gray-500 truncate">{session.user?.email}</p>
                                    </div>
                                </DropdownMenuLabel>
                                <DropdownMenuSeparator />
                                <DropdownMenuItem asChild>
                                    <Link href="/cliente/perfil" className="cursor-pointer">
                                        <User className="mr-2 h-4 w-4" />
                                        <span>Mi perfil</span>
                                    </Link>
                                </DropdownMenuItem>
                                <DropdownMenuItem asChild>
                                    <Link href="/cliente/pedidos" className="cursor-pointer">
                                        <Package className="mr-2 h-4 w-4" />
                                        <span>Mis pedidos</span>
                                    </Link>
                                </DropdownMenuItem>
                                {isGestor && (
                                    <DropdownMenuItem asChild>
                                        <Link href="/logistica" className="cursor-pointer">
                                            <LayoutDashboard className="mr-2 h-4 w-4" />
                                            <span>Panel Logística</span>
                                        </Link>
                                    </DropdownMenuItem>
                                )}
                                <DropdownMenuSeparator />
                                <DropdownMenuItem onClick={handleLogout} className="cursor-pointer text-red-600 focus:text-red-600">
                                    <LogOut className="mr-2 h-4 w-4" />
                                    <span>Cerrar sesión</span>
                                </DropdownMenuItem>
                            </DropdownMenuContent>
                        </DropdownMenu>
                    ) : (
                        // Si no está autenticado, mostrar botón de login (opcional)
                        <Link href="/login">
                            <Button variant="default" size="sm">Iniciar sesión</Button>
                        </Link>
                    )}
                </div>
            </div>
        </nav>
    );
}