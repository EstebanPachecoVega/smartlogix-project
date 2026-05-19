'use client';

import { useEffect, useState } from 'react';
import { useSearchParams } from 'next/navigation';
import { pedidosApi } from '@/lib/api';
import { PedidoResponse } from '@/types';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import Spinner from '@/components/shared/Spinner';

export default function PedidosPage() {
    const searchParams = useSearchParams();
    const [pedidos, setPedidos] = useState<PedidoResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const exito = searchParams.get('exito');

    useEffect(() => {
        pedidosApi.listar().then(setPedidos).catch(console.error).finally(() => setLoading(false));
    }, []);

    if (loading) return <Spinner />;

    return (
        <div>
            <h1 className="text-2xl font-bold mb-6">Mis pedidos</h1>
            {exito && <div className="bg-green-100 text-green-800 p-4 rounded mb-4">Pedido creado ID: {exito}</div>}
            {pedidos.length === 0 ? <p>No hay pedidos.</p> : (
                <div className="space-y-4">
                    {pedidos.map(p => (
                        <Card key={p.id}>
                            <CardHeader><CardTitle>Pedido #{p.numeroOrden}</CardTitle></CardHeader>
                            <CardContent><p>Estado: {p.estado}</p><p>Total: ${p.totalCompra.toLocaleString()}</p></CardContent>
                        </Card>
                    ))}
                </div>
            )}
        </div>
    );
}