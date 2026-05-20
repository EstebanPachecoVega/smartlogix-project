'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useSearchParams } from 'next/navigation';
import { pedidosApi } from '@/lib/api';
import { PedidoResponse } from '@/types';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { EstadoPedidoBadge } from '@/components/ui/EstadoPedidoBadge';
import Spinner from '@/components/shared/Spinner';

export default function PedidosPage() {
    const searchParams = useSearchParams();
    const [pedidos, setPedidos] = useState<PedidoResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const exito = searchParams.get('exito');

    useEffect(() => {
        pedidosApi
            .listar()
            .then(setPedidos)
            .catch(console.error)
            .finally(() => setLoading(false));
    }, []);

    if (loading) return <Spinner />;

    return (
        <div>
            <h1 className="text-2xl font-bold mb-6">Mis pedidos</h1>

            {exito && (
                <div className="bg-green-100 text-green-800 p-4 rounded mb-4">
                    ¡Pedido creado exitosamente! ID: {exito}
                </div>
            )}

            {pedidos.length === 0 ? (
                <div className="text-center py-12">
                    <p className="text-gray-500">No tienes pedidos aún</p>
                    <Link href="/cliente">
                        <Button className="mt-4" variant="outline">
                            Ir al catálogo
                        </Button>
                    </Link>
                </div>
            ) : (
                <div className="space-y-4">
                    {pedidos.map((pedido) => (
                        <Card key={pedido.id}>
                            <CardHeader className="flex flex-row justify-between items-start">
                                <div>
                                    <CardTitle className="text-lg">
                                        Pedido #{pedido.numeroOrden}
                                    </CardTitle>
                                    <p className="text-sm text-gray-500 mt-1">
                                        Fecha: {pedido.fechaPedido ? new Date(pedido.fechaPedido).toLocaleDateString() : 'Fecha no disponible'}
                                    </p>
                                </div>
                                <EstadoPedidoBadge estado={pedido.estado} />
                            </CardHeader>
                            <CardContent>
                                <div className="flex justify-between items-center">
                                    <p className="text-xl font-bold">
                                        Total: ${pedido.totalCompra.toLocaleString()}
                                    </p>
                                    <Link href={`/cliente/pedidos/${pedido.id}`}>
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