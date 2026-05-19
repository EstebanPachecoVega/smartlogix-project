'use client';

import { useEffect, useState } from 'react';
import { useSearchParams } from 'next/navigation';
import { pedidosApi } from '@/lib/api';
import { PedidoResponse } from '@/types';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';  // ← IMPORT agregado
import Spinner from '@/components/shared/Spinner';

const estadoColor: Record<string, string> = {
    PENDIENTE: 'bg-yellow-500',
    APROBADO: 'bg-blue-500',
    RECHAZADO: 'bg-red-500',
    EN_CAMINO: 'bg-purple-500',
    ENTREGADO: 'bg-green-500',
};

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
                    <a href="/cliente">
                        <Button className="mt-4" variant="outline">
                            Ir al catálogo
                        </Button>
                    </a>
                </div>
            ) : (
                <div className="space-y-4">
                    {pedidos.map((pedido) => (
                        <Card key={pedido.id}>
                            <CardHeader className="flex flex-row justify-between items-center">
                                <CardTitle className="text-lg">Pedido #{pedido.numeroOrden}</CardTitle>
                                <Badge className={estadoColor[pedido.estado] || 'bg-gray-500'}>
                                    {pedido.estado}
                                </Badge>
                            </CardHeader>
                            <CardContent>
                                <p className="text-sm text-gray-600">
                                    Fecha: {pedido.fechaCreacion ? new Date(pedido.fechaCreacion).toLocaleDateString() : 'Fecha no disponible'}
                                </p>
                                <p className="text-xl font-bold mt-2">Total: ${pedido.totalCompra.toLocaleString()}</p>
                            </CardContent>
                        </Card>
                    ))}
                </div>
            )}
        </div>
    );
}