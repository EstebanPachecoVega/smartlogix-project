'use client';

import { useState } from 'react';
import { usePedidoStore } from '@/store/pedidoStore';
import { useRouter } from 'next/navigation';

export default function PedidoForm() {
  const [productoId, setProductoId] = useState('');
  const [cantidad, setCantidad] = useState('');
  const { crearPedido, loading, error } = usePedidoStore();
  const router = useRouter();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    await crearPedido({
      productoId: parseInt(productoId),
      cantidad: parseInt(cantidad),
    });
    router.push('/pedidos');
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4 max-w-md mx-auto bg-white p-6 rounded shadow">
      <h2 className="text-xl font-bold">Crear Pedido</h2>
      <div>
        <label className="block text-sm font-medium">ID del Producto</label>
        <input
          type="number"
          value={productoId}
          onChange={(e) => setProductoId(e.target.value)}
          required
          className="mt-1 block w-full border border-gray-300 rounded-md p-2"
        />
      </div>
      <div>
        <label className="block text-sm font-medium">Cantidad</label>
        <input
          type="number"
          value={cantidad}
          onChange={(e) => setCantidad(e.target.value)}
          required
          min="1"
          className="mt-1 block w-full border border-gray-300 rounded-md p-2"
        />
      </div>
      <button
        type="submit"
        disabled={loading}
        className="w-full bg-blue-500 text-white py-2 rounded hover:bg-blue-600 disabled:opacity-50"
      >
        {loading ? 'Procesando...' : 'Crear Pedido'}
      </button>
      {error && <div className="text-red-500 text-sm">{error}</div>}
    </form>
  );
}