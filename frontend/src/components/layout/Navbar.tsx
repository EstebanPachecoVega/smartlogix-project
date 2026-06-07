'use client';

import Link from 'next/link';
import { useSession, signOut } from "next-auth/react";
import { ShoppingCart, LogOut, User, ChevronDown, Package, LayoutDashboard } from 'lucide-react';
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
        try {
            // 1. Obtener la URL segura desde el servidor
            const res = await fetch('/api/auth/logout');
            const data = await res.json();

            // 2. Cerrar sesión de Next-Auth en el entorno local
            await signOut({ redirect: false });

            // 3. Ejecutar la redirección hacia Keycloak
            window.location.href = data.url;
        } catch (error) {
            console.error("Error durante el logout:", error);
            await signOut({ callbackUrl: '/login' });
        }
    };

    const userInitials = session?.user?.name
        ? session.user.name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)
        : session?.user?.email?.charAt(0).toUpperCase() || 'U';

    return (
        <nav className="border-b bg-white sticky top-0 z-50">
            <div className="container mx-auto flex justify-between items-center px-4 py-3">
                <Link href="/" className="text-xl font-bold hover:text-blue-600 transition-colors">
                    SmartLogix
                </Link>
                <div className="flex items-center gap-2">
                    <Link href="/dashboard/carrito">
                        <Button variant="outline" className="relative">
                            <ShoppingCart className="h-5 w-5" />
                            {totalItems > 0 && (
                                <span className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full w-5 h-5 text-xs flex items-center justify-center">
                                    {totalItems}
                                </span>
                            )}
                        </Button>
                    </Link>
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
                                    <p className="text-sm font-medium">{session.user?.name || 'Usuario'}</p>
                                </DropdownMenuLabel>
                                <DropdownMenuSeparator />
                                {!isGestor && (
                                    <>
                                        <DropdownMenuItem asChild>
                                            <Link href="/dashboard/perfil" className="cursor-pointer">
                                                <User className="mr-2 h-4 w-4" />
                                                <span>Mi perfil</span>
                                            </Link>
                                        </DropdownMenuItem>
                                        <DropdownMenuItem asChild>
                                            <Link href="/dashboard/pedidos" className="cursor-pointer">
                                                <Package className="mr-2 h-4 w-4" />
                                                <span>Mis pedidos</span>
                                            </Link>
                                        </DropdownMenuItem>
                                    </>
                                )}
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
                        <Link href="/login">
                            <Button variant="default" size="sm">Iniciar sesión</Button>
                        </Link>
                    )}
                </div>
            </div>
        </nav>
    );
}