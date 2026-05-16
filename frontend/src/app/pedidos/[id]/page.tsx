'use client';

import { useEffect, useState } from 'react';
import { usePedidoStore } from '@/store/pedidoStore';
import { useEnvioStore } from '@/store/envioStore';
import { useParams } from 'next/navigation';
import Spinner from '@/components/common/Spinner';
import Link from 'next/link';

export default function PedidoDetallePage() {
    const { id } = useParams();
    const { currentPedido, loading: loadingPedido, error: errorPedido, obtenerPedido } = usePedidoStore();
    const { obtenerEnvioPorPedidoId } = useEnvioStore();
    const [envioId, setEnvioId] = useState<number | null>(null);
    const [loadingEnvio, setLoadingEnvio] = useState(false);

    useEffect(() => {
        if (id) obtenerPedido(Number(id));
    }, [id, obtenerPedido]);

    useEffect(() => {
        if (currentPedido && currentPedido.estado === 'APROBADO') {
            setLoadingEnvio(true);
            obtenerEnvioPorPedidoId(currentPedido.id).then(envio => {
                if (envio) setEnvioId(envio.id);
                setLoadingEnvio(false);
            });
        }
    }, [currentPedido, obtenerEnvioPorPedidoId]);

    if (loadingPedido) return <Spinner />;
    if (errorPedido) return <div className="text-red-500">Error: {errorPedido}</div>;
    if (!currentPedido) return <div>Pedido no encontrado</div>;

    return (
        <div>
            <h1 className="text-2xl font-bold">Pedido #{currentPedido.id}</h1>
            <p>Estado: {currentPedido.estado}</p>
            {currentPedido.estado === 'APROBADO' && (
                loadingEnvio ? (
                    <p className="text-gray-500 mt-4">Cargando envío...</p>
                ) : envioId ? (
                    <Link href={`/envios/${envioId}`} className="text-blue-500 mt-4 inline-block">
                        Ver envío asociado →
                    </Link>
                ) : (
                    <p className="text-red-500 mt-4">No se encontró un envío para este pedido.</p>
                )
            )}
            {currentPedido.estado === 'RECHAZADO' && (
                <p className="text-red-500 mt-4">El pedido fue rechazado, no tiene envío asociado.</p>
            )}
        </div>
    );
}