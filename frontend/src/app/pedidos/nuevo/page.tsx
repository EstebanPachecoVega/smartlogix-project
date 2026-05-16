'use client';
import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { usePedidoStore } from '@/stores/pedidoStore';
import { useUIStore } from '@/stores/uiStore';
import Spinner from '@/components/Spinner';

export default function NuevoPedidoPage() {
    const [productoId, setProductoId] = useState('');
    const [cantidad, setCantidad] = useState('1');
    const { crearPedido } = usePedidoStore();
    const { loading } = useUIStore();
    const router = useRouter();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            await crearPedido(Number(productoId), Number(cantidad));
            router.push('/pedidos');
        } catch (error) {
            // El error ya se muestra en el toast
        }
    };

    return (
        <div className="max-w-md mx-auto bg-white p-6 rounded shadow">
            <h1 className="text-2xl font-bold mb-4">Nuevo Pedido</h1>
            <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                    <label className="block mb-1">ID del Producto</label>
                    <input
                        type="number"
                        value={productoId}
                        onChange={(e) => setProductoId(e.target.value)}
                        required
                        min="1"
                        className="w-full border p-2 rounded"
                    />
                </div>
                <div>
                    <label className="block mb-1">Cantidad</label>
                    <input
                        type="number"
                        value={cantidad}
                        onChange={(e) => setCantidad(e.target.value)}
                        required
                        min="1"
                        className="w-full border p-2 rounded"
                    />
                </div>
                <button
                    type="submit"
                    disabled={loading}
                    className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700 disabled:opacity-50"
                >
                    {loading ? <Spinner /> : 'Crear Pedido'}
                </button>
            </form>
        </div>
    );
}