'use client';

import { useEffect } from 'react';
import { usePedidoStore } from '@/store/pedidoStore';
import PedidoCard from './PedidoCard';
import Spinner from '../common/Spinner';

export default function PedidoList() {
  const { pedidos, loading, error, limpiarError } = usePedidoStore();

  // Opcional: cargar lista desde API si no se tiene aún
  // useEffect(() => { ... }, [])

  if (loading) return <Spinner />;
  if (error) return <div className="text-red-500">Error: {error}</div>;

  return (
    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
      {pedidos.map((pedido) => (
        <PedidoCard key={pedido.id} pedido={pedido} />
      ))}
      {pedidos.length === 0 && <p>No hay pedidos.</p>}
    </div>
  );
}