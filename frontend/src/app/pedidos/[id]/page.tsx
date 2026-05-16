'use client';

import { useEffect } from 'react';
import { usePedidoStore } from '@/store/pedidoStore';
import { useParams } from 'next/navigation';
import Spinner from '@/components/common/Spinner';
import Link from 'next/link';

export default function PedidoDetallePage() {
  const { id } = useParams();
  const { currentPedido, loading, error, obtenerPedido } = usePedidoStore();

  useEffect(() => {
    if (id) obtenerPedido(Number(id));
  }, [id, obtenerPedido]);

  if (loading) return <Spinner />;
  if (error) return <div className="text-red-500">Error: {error}</div>;
  if (!currentPedido) return <div>Pedido no encontrado</div>;

  return (
    <div>
      <h1 className="text-2xl font-bold">Pedido #{currentPedido.id}</h1>
      <p>Estado: {currentPedido.estado}</p>
      <Link href={`/envios/${currentPedido.id}`} className="text-blue-500 mt-4 inline-block">
        Ver envío asociado →
      </Link>
    </div>
  );
}