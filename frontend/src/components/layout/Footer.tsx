'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';

export default function Footer() {
    const pathname = usePathname();
    if (pathname.startsWith('/logistica')) return null;

    return (
        <footer className="bg-white border-t mt-auto">
            <div className="container mx-auto px-4 py-6">
                <div className="flex flex-col md:flex-row justify-between items-center gap-4">
                    <Link href="/" className="text-xl font-bold hover:text-blue-600 transition-colors">
                        SmartLogix
                    </Link>
                    <p className="text-sm text-gray-500">
                        &copy; {new Date().getFullYear()} SmartLogix. Todos los derechos reservados.
                    </p>
                    <div className="flex gap-4 text-sm text-gray-500">
                        <Link href="/" className="hover:text-blue-600 transition-colors">Inicio</Link>
                        <Link href="/productos" className="hover:text-blue-600 transition-colors">Productos</Link>
                    </div>
                </div>
            </div>
        </footer>
    );
}
