'use client';

import Link from 'next/link';
import { useRouter, usePathname } from 'next/navigation';
import { useSession, signOut } from 'next-auth/react';
import { useState } from 'react';
import {
    ShoppingCart, LogOut, User, ChevronDown, Menu, X,
    Package, LayoutDashboard,
} from 'lucide-react';
import ThemeToggle from '@/components/ui/ThemeToggle';
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
import { Dialog, DialogContent, DialogTitle } from '@/components/ui/dialog';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import SearchBar from './SearchBar';

export default function Navbar() {
    const router = useRouter();
    const pathname = usePathname();
    const { data: session } = useSession();
    const totalItems = useTotalItems();
    const userRoles: string[] = session?.roles || [];
    const isGestor = userRoles.some((r) => r.toLowerCase() === 'gestor');
    const isLogistica = pathname.startsWith('/logistica');
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

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

    const closeMobileMenu = () => setMobileMenuOpen(false);

    const UserMenu = () => (
        <>
            {session ? (
                <DropdownMenu>
                    <DropdownMenuTrigger
                        render={
                            <Button variant="ghost" className="flex items-center gap-1.5 px-2" />
                        }
                    >
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
                                <DropdownMenuItem
                                    render={<Link href="/dashboard/perfil" className="cursor-pointer flex items-center gap-2" />}
                                >
                                    <User className="size-4 shrink-0" />
                                    Mi perfil
                                </DropdownMenuItem>
                                <DropdownMenuItem
                                    render={<Link href="/dashboard/pedidos" className="cursor-pointer flex items-center gap-2" />}
                                >
                                    <Package className="size-4 shrink-0" />
                                    Mis pedidos
                                </DropdownMenuItem>
                            </>
                        )}

                        {isGestor && (
                            <DropdownMenuItem
                                render={<Link href="/logistica" className="cursor-pointer flex items-center gap-2" />}
                            >
                                <LayoutDashboard className="size-4 shrink-0" />
                                Panel Logística
                            </DropdownMenuItem>
                        )}

                        <DropdownMenuSeparator />

                        <DropdownMenuItem
                            onClick={handleLogout}
                            className="cursor-pointer text-destructive focus:text-destructive"
                        >
                            <LogOut className="size-4 shrink-0" />
                            Cerrar sesión
                        </DropdownMenuItem>
                    </DropdownMenuContent>
                </DropdownMenu>
            ) : (
                <div className="hidden sm:flex items-center gap-2">
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
        <>
            <header className="fixed top-0 right-0 left-0 z-50 bg-background border-b">
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
                        <div className="flex items-center justify-end flex-1 px-4 gap-1">
                            <ThemeToggle />
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
                            <ThemeToggle />
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
                            <div className="hidden sm:block">
                                <UserMenu />
                            </div>
                            <Button
                                variant="ghost"
                                size="icon"
                                className="sm:hidden"
                                onClick={() => setMobileMenuOpen(true)}
                                aria-label="Abrir menú"
                            >
                                <Menu className="h-5 w-5" />
                            </Button>
                        </div>
                    </div>
                )}
            </header>

            {/* Mobile menu drawer */}
            <Dialog open={mobileMenuOpen} onOpenChange={setMobileMenuOpen}>
                <DialogContent
                    className="fixed left-0 top-0 translate-x-0 translate-y-0 h-full w-[min(85vw,320px)] rounded-none border-r p-0 gap-0"
                    showCloseButton={false}
                >
                    <div className="flex items-center justify-between p-4 border-b">
                        <DialogTitle className="font-bold text-lg">Menú</DialogTitle>
                        <Button
                            variant="ghost"
                            size="icon-sm"
                            onClick={closeMobileMenu}
                            aria-label="Cerrar menú"
                        >
                            <X className="h-5 w-5" />
                        </Button>
                    </div>
                    <div className="flex-1 overflow-y-auto p-4 space-y-4">
                        <div className="sm:hidden">
                            <SearchBar />
                        </div>
                        <nav className="flex flex-col gap-1">
                            <Link
                                href="/"
                                onClick={closeMobileMenu}
                                className="px-3 py-2 rounded-md hover:bg-accent transition-colors text-sm"
                            >
                                Inicio
                            </Link>
                            {!isGestor && (
                                <>
                                    <Link
                                        href="/dashboard/carrito"
                                        onClick={closeMobileMenu}
                                        className="px-3 py-2 rounded-md hover:bg-accent transition-colors text-sm flex items-center justify-between"
                                    >
                                        <span>Carrito</span>
                                        {totalItems > 0 && (
                                            <span className="bg-destructive text-destructive-foreground rounded-full w-5 h-5 text-xs flex items-center justify-center font-bold">
                                                {totalItems > 99 ? '99+' : totalItems}
                                            </span>
                                        )}
                                    </Link>
                                    <Link
                                        href="/dashboard/pedidos"
                                        onClick={closeMobileMenu}
                                        className="px-3 py-2 rounded-md hover:bg-accent transition-colors text-sm"
                                    >
                                        Mis pedidos
                                    </Link>
                                    <Link
                                        href="/dashboard/perfil"
                                        onClick={closeMobileMenu}
                                        className="px-3 py-2 rounded-md hover:bg-accent transition-colors text-sm"
                                    >
                                        Mi perfil
                                    </Link>
                                </>
                            )}
                            {isGestor && (
                                <Link
                                    href="/logistica"
                                    onClick={closeMobileMenu}
                                    className="px-3 py-2 rounded-md hover:bg-accent transition-colors text-sm"
                                >
                                    Panel Logística
                                </Link>
                            )}
                        </nav>
                        {!session && (
                            <div className="flex flex-col gap-2 pt-2 border-t">
                                <Link href="/registro" onClick={closeMobileMenu}>
                                    <Button variant="outline" className="w-full">Registrarse</Button>
                                </Link>
                                <Link href="/login" onClick={closeMobileMenu}>
                                    <Button className="w-full">Iniciar sesión</Button>
                                </Link>
                            </div>
                        )}
                        {session && (
                            <div className="pt-2 border-t">
                                <Button
                                    variant="ghost"
                                    onClick={handleLogout}
                                    className="w-full justify-start text-destructive hover:text-destructive"
                                >
                                    <LogOut className="size-4 mr-2" />
                                    Cerrar sesión
                                </Button>
                            </div>
                        )}
                    </div>
                    <div className="p-4 border-t flex items-center justify-between">
                        <span className="text-sm text-muted-foreground">Tema</span>
                        <ThemeToggle />
                    </div>
                </DialogContent>
            </Dialog>
        </>
    );
}
