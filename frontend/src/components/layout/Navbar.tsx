'use client';

import Link from 'next/link';
import { useRouter, usePathname } from 'next/navigation';
import { Suspense } from 'react';
import { useSession, signOut } from 'next-auth/react';
import {
    ShoppingCart, LogOut, User, ChevronDown,
    Package, LayoutDashboard,
} from 'lucide-react';
import { useTotalItems } from '@/store/carritoStore';
import { Button } from '@/components/ui/button';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import SearchBar from './SearchBar';
import CategoryNav from './CategoryNav';

export default function Navbar() {
    const router = useRouter();
    const pathname = usePathname();
    const { data: session } = useSession();
    const totalItems = useTotalItems();
    const userRoles: string[] = session?.roles || [];
    const isGestor = userRoles.some((r) => r.toLowerCase() === 'gestor');
    const isLogistica = pathname.startsWith('/logistica');

    const handleLogout = async () => {
        try {
            const res = await fetch('/api/auth/logout');
            const data = await res.json();
            await signOut({ redirect: false });
            window.location.href = data.url;
        } catch (error) {
            console.error('Error durante el logout:', error);
            await signOut({ callbackUrl: '/login' });
        }
    };

    const userInitials = session?.user?.name
        ? session.user.name.split(' ').map((n) => n[0]).join('').toUpperCase().slice(0, 2)
        : session?.user?.email?.charAt(0).toUpperCase() || 'U';

    const scrollToTop = () => {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    const UserMenu = () => (
        <>
            {session ? (
                <DropdownMenu modal={false}>
                    <DropdownMenuTrigger asChild>
                        <Button variant="ghost" className="flex items-center gap-1.5 px-2">
                            <Avatar className="h-8 w-8">
                                <AvatarFallback className="bg-muted text-muted-foreground text-xs font-semibold">
                                    {userInitials}
                                </AvatarFallback>
                            </Avatar>
                            <span className="hidden md:inline text-sm font-medium">
                                {session.user?.name?.split(' ')[0] ||
                                    session.user?.email?.split('@')[0]}
                            </span>
                            <ChevronDown className="h-3.5 w-3.5 text-muted-foreground" />
                        </Button>
                    </DropdownMenuTrigger>

                    <DropdownMenuContent align="end" className="w-56">
                        <DropdownMenuLabel className="font-normal">
                            <p className="text-sm font-medium truncate">
                                {session.user?.name || 'Usuario'}
                            </p>
                        </DropdownMenuLabel>

                        <DropdownMenuSeparator />

                        {!isGestor && (
                            <>
                                <DropdownMenuItem asChild>
                                    <Link href="/dashboard/perfil" className="cursor-pointer">
                                        <User className="mr-2 h-4 w-4" />
                                        Mi perfil
                                    </Link>
                                </DropdownMenuItem>
                                <DropdownMenuItem asChild>
                                    <Link href="/dashboard/pedidos" className="cursor-pointer">
                                        <Package className="mr-2 h-4 w-4" />
                                        Mis pedidos
                                    </Link>
                                </DropdownMenuItem>
                            </>
                        )}

                        {isGestor && (
                            <DropdownMenuItem asChild>
                                <Link href="/logistica" className="cursor-pointer">
                                    <LayoutDashboard className="mr-2 h-4 w-4" />
                                    Panel Logística
                                </Link>
                            </DropdownMenuItem>
                        )}

                        <DropdownMenuSeparator />

                        <DropdownMenuItem
                            onClick={handleLogout}
                            className="cursor-pointer text-destructive focus:text-destructive"
                        >
                            <LogOut className="mr-2 h-4 w-4" />
                            Cerrar sesión
                        </DropdownMenuItem>
                    </DropdownMenuContent>
                </DropdownMenu>
            ) : (
                <div className="flex items-center gap-2">
                    <Link href="/registro">
                        <Button variant="outline" size="sm">
                            Registrarse
                        </Button>
                    </Link>
                    <Link href="/login">
                        <Button size="sm">Iniciar sesión</Button>
                    </Link>
                </div>
            )}
        </>
    );

    return (
        <header className="sticky top-0 z-50 bg-background border-b">
            {isLogistica ? (
                /* ── Layout logistica: logo alineado con sidebar ── */
                <div className="h-16 flex items-center">
                    <div className="hidden lg:flex items-center lg:w-64 px-4 shrink-0">
                        <Link
                            href="/"
                            className="text-xl font-bold hover:text-primary transition-colors"
                        >
                            SmartLogix
                        </Link>
                    </div>
                    <div className="flex items-center justify-end flex-1 px-4 gap-3">
                        <UserMenu />
                    </div>
                </div>
            ) : (
                /* ── Layout público: flex responsivo ── */
                <div className="h-16 container mx-auto flex items-center justify-between gap-2 px-4">
                    <div className="shrink-0 min-w-0">
                        <Link
                            href="/"
                            onClick={scrollToTop}
                            className="text-lg sm:text-xl font-bold hover:text-primary transition-colors truncate block"
                        >
                            SmartLogix
                        </Link>
                    </div>

                    <div className="hidden sm:flex flex-1 justify-center max-w-xl mx-auto px-4">
                        <SearchBar />
                    </div>

                    <div className="flex items-center gap-1 sm:gap-2 shrink-0">
                        {!isGestor && (
                            <Button
                                variant="outline"
                                size="icon"
                                className="relative shrink-0"
                                onClick={() => router.push('/dashboard/carrito')}
                                aria-label="Carrito"
                            >
                                <ShoppingCart className="h-5 w-5" />
                                {totalItems > 0 && (
                                    <span className="absolute -top-1.5 -right-1.5 bg-destructive text-destructive-foreground rounded-full w-4 h-4 text-[10px] flex items-center justify-center font-bold leading-none">
                                        {totalItems > 99 ? '99+' : totalItems}
                                    </span>
                                )}
                            </Button>
                        )}
                        <UserMenu />
                    </div>
                </div>
            )}

            {!isLogistica && (
                <Suspense fallback={null}>
                    <CategoryNav />
                </Suspense>
            )}
        </header>
    );
}
