'use client';

import { Suspense } from 'react';
import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useSearchParams } from 'next/navigation';
import { pedidosApi } from '@/lib/api';
import { PedidoResponse } from '@/types';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { EstadoPedidoBadge } from '@/components/ui/EstadoPedidoBadge';
import Spinner from '@/components/shared/Spinner';

function PedidosContent() {
    const searchParams = useSearchParams();
    const [pedidos, setPedidos] = useState<PedidoResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const exito = searchParams.get('exito');

    const cargarPedidos = () => {
        setLoading(true);
        setError(null);
        pedidosApi
            .listar()
            .then((data) => {
                if (Array.isArray(data)) {
                    setPedidos(data);
                } else {
                    setPedidos(data.content);
                }
            })
            .catch((err) => {
                console.error(err);
                setError("No se pudieron cargar tus pedidos. Intenta nuevamente más tarde.");
            })
            .finally(() => setLoading(false));
    };

    useEffect(() => {
        cargarPedidos();
    }, []);

    if (loading) return <Spinner />;

    if (error) {
        return (
            <div>
                <div className="text-center py-12">
                    <p className="text-red-500 mb-4">{error}</p>
                    <Button onClick={cargarPedidos} variant="outline">Reintentar</Button>
                </div>
            </div>
        );
    }

    return (
        <div>
            <h1 className="text-xl sm:text-2xl font-bold mb-6">Mis pedidos</h1>

            {exito && (
                <div className="bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-100 p-4 rounded mb-4">
                    ¡Pedido creado exitosamente! Número de orden: {exito}
                </div>
            )}

            {pedidos.length === 0 ? (
                <div className="text-center py-12">
                    <p className="text-muted-foreground">No tienes pedidos aún</p>
                    <Link href="/">
                        <Button className="mt-4" variant="outline">
                            Ir al catálogo
                        </Button>
                    </Link>
                </div>
            ) : (
                <div className="space-y-4">
                    {pedidos.map((pedido) => (
                        <Card key={pedido.id}>
                            <CardHeader className="flex flex-row flex-wrap justify-between items-start">
                                <div>
                                    <CardTitle className="text-base sm:text-lg">
                                        Pedido #{pedido.numeroOrden}
                                    </CardTitle>
                                    <p className="text-sm text-muted-foreground mt-1">
                                        Fecha: {pedido.fechaPedido ? new Date(pedido.fechaPedido).toLocaleDateString() : 'Fecha no disponible'}
                                    </p>
                                </div>
                                <EstadoPedidoBadge estado={pedido.estado} />
                            </CardHeader>
                            <CardContent>
                                <div className="flex justify-between items-center">
                                    <p className="text-lg sm:text-xl font-bold">
                                        Total: ${pedido.totalCompra.toLocaleString()}
                                    </p>
                                    <Link href={`/dashboard/pedidos/${pedido.id}`}>
                                        <Button variant="outline" size="sm">
                                            Ver detalle
                                        </Button>
                                    </Link>
                                </div>
                            </CardContent>
                        </Card>
                    ))}
                </div>
            )}
        </div>
    );
}

export default function PedidosPage() {
    return (
        <Suspense fallback={<Spinner />}>
            <PedidosContent />
        </Suspense>
    );
}