'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Banknote, CreditCard, MapPin, Clock } from 'lucide-react';

export default function Footer() {
    const pathname = usePathname();
    if (pathname.startsWith('/logistica')) return null;

    const scrollToTop = () => {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    return (
        <footer className="bg-gray-900 text-gray-300 mt-auto">
            <div className="container mx-auto px-4 py-10">
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
                    {/* Enlaces rápidos */}
                    <div>
                        <h3 className="text-white font-semibold mb-4">Enlaces rápidos</h3>
                        <ul className="space-y-2 text-sm">
                            <li>
                                <Link
                                    href="/"
                                    onClick={scrollToTop}
                                    className="hover:text-white transition-colors"
                                >
                                    Inicio
                                </Link>
                            </li>
                            <li>
                                <Link href="/quienes-somos" className="hover:text-white transition-colors">
                                    Quiénes somos
                                </Link>
                            </li>
                            <li>
                                <Link href="/centro-de-ayuda" className="hover:text-white transition-colors">
                                    Centro de ayuda
                                </Link>
                            </li>
                        </ul>
                    </div>

                    {/* Políticas */}
                    <div>
                        <h3 className="text-white font-semibold mb-4">Políticas</h3>
                        <ul className="space-y-2 text-sm">
                            <li>
                                <Link href="/terminos-y-condiciones" className="hover:text-white transition-colors">
                                    Términos y condiciones
                                </Link>
                            </li>
                            <li>
                                <Link href="/privacidad-y-seguridad" className="hover:text-white transition-colors">
                                    Privacidad y Seguridad
                                </Link>
                            </li>
                            <li>
                                <Link href="/devolucion-y-reembolso" className="hover:text-white transition-colors">
                                    Devolución y Reembolso
                                </Link>
                            </li>
                            <li>
                                <Link href="/entrega-y-envios" className="hover:text-white transition-colors">
                                    Entrega y Envíos
                                </Link>
                            </li>
                        </ul>
                    </div>

                    {/* Medios de pago */}
                    <div>
                        <h3 className="text-white font-semibold mb-4">Medios de pago</h3>
                        <div className="flex gap-4">
                            <div className="flex items-center gap-2 bg-gray-800 px-3 py-2 rounded-lg">
                                <Banknote className="h-5 w-5 text-green-400" />
                                <span className="text-sm">Efectivo</span>
                            </div>
                            <div className="flex items-center gap-2 bg-gray-800 px-3 py-2 rounded-lg">
                                <CreditCard className="h-5 w-5 text-blue-400" />
                                <span className="text-sm">Tarjeta</span>
                            </div>
                        </div>
                    </div>

                    {/* Ubicación y horario */}
                    <div>
                        <h3 className="text-white font-semibold mb-4">Ubicación</h3>
                        <div className="space-y-3 text-sm">
                            <div className="flex items-start gap-2">
                                <MapPin className="h-4 w-4 mt-0.5 shrink-0 text-red-400" />
                                <span>
                                    Avenida Concha Y Toro, Av. San Carlos 1340,<br />
                                    Puente Alto, Región Metropolitana.
                                </span>
                            </div>
                            <div className="flex items-start gap-2">
                                <Clock className="h-4 w-4 mt-0.5 shrink-0 text-yellow-400" />
                                <span>
                                    <strong className="text-white">Horario de Atención Tienda y Retiro</strong><br />
                                    Lunes a Viernes - 09:00 a 18:00
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div className="border-t border-gray-800">
                <div className="container mx-auto px-4 py-4 text-center text-sm text-gray-400">
                    &copy; {new Date().getFullYear()} SmartLogix. Todos los derechos reservados.
                </div>
            </div>
        </footer>
    );
}
