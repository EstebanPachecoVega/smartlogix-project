'use client';

import Link from 'next/link';
import { useRouter, usePathname } from 'next/navigation';
import { Suspense } from 'react';
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
import SearchBar from './SearchBar';
import CategoryNav from './CategoryNav';

export default function Navbar() {
    const router = useRouter();
    const pathname = usePathname();
    const { data: session } = useSession();
    const totalItems = useTotalItems();
    const userRoles: string[] = session?.roles || [];
    const isGestor = userRoles.some(r => r.toLowerCase() === 'gestor');

    const handleLogout = async () => {
        try {
            const res = await fetch('/api/auth/logout');
            const data = await res.json();
            await signOut({ redirect: false });
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
        <header className="sticky top-0 z-50 bg-white border-b">
            <div className="container mx-auto flex items-center justify-between px-4 py-2.5 gap-4">
                <Link href="/" className="text-2xl font-bold hover:text-blue-600 transition-colors shrink-0">
                    SmartLogix
                </Link>

                {!pathname.startsWith('/logistica') && (
                    <div className="flex-1 flex justify-center">
                        <SearchBar />
                    </div>
                )}

                <div className="flex items-center gap-2 shrink-0">
                    {!isGestor && (
                        <Button variant="outline" size="default" className="relative" onClick={() => router.push('/dashboard/carrito')}>
                            <ShoppingCart className="h-6 w-6" />
                            {totalItems > 0 && (
                                <span className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full w-5 h-5 text-xs flex items-center justify-center font-medium">
                                    {totalItems}
                                </span>
                            )}
                        </Button>
                    )}
                    {session ? (
                        <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                                <Button variant="ghost" className="flex items-center gap-2 px-2">
                                    <Avatar className="h-10 w-10">
                                        <AvatarFallback className="bg-gray-200 text-gray-700 text-sm">
                                            {userInitials}
                                        </AvatarFallback>
                                    </Avatar>
                                    <span className="hidden md:inline-block text-sm font-medium">
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
                        <div className="flex items-center gap-2">
                            <Link href="/registro">
                                <Button variant="outline" size="default">Registrarse</Button>
                            </Link>
                            <Link href="/login">
                                <Button variant="default" size="default">Iniciar sesión</Button>
                            </Link>
                        </div>
                    )}
                </div>
            </div>

            {!pathname.startsWith('/logistica') && (
                <Suspense fallback={null}>
                    <CategoryNav />
                </Suspense>
            )}
        </header>
    );
}
