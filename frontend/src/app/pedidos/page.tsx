'use client';
import { useEffect, useState } from 'react';
import { usePedidoStore } from '@/stores/pedidoStore';
import { pedidosService } from '@/services/pedidosService';
import { PedidoResponse } from '@/types';
import PedidoCard from '@/components/PedidoCard';
import Spinner from '@/components/Spinner';

export default function PedidosPage() {
    const { pedidos, setPedidos, loading, setLoading } = usePedidoStore();
    const [localPedidos, setLocalPedidos] = useState<PedidoResponse[]>([]);

    useEffect(() => {
        const cargarPedidos = async () => {
            setLoading(true);
            try {
                // Aquí idealmente debería haber un endpoint para listar pedidos
                // Como no lo tenemos, simulamos con los últimos creados (los que están en el store)
                setLocalPedidos(pedidos);
            } finally {
                setLoading(false);
            }
        };
        cargarPedidos();
    }, [pedidos, setLoading]);

    if (loading) return <Spinner />;

    return (
        <div>
            <h1 className="text-2xl font-bold mb-4">Mis Pedidos</h1>
            {localPedidos.length === 0 ? (
                <p>No hay pedidos aún. Crea uno nuevo.</p>
            ) : (
                <div className="space-y-4">
                    {localPedidos.map((pedido) => (
                        <PedidoCard key={pedido.id} pedido={pedido} />
                    ))}
                </div>
            )}
        </div>
    );
}