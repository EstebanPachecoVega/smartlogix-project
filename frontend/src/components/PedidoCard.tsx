import { PedidoResponse } from '@/types';
import Link from 'next/link';

export default function PedidoCard({ pedido }: { pedido: PedidoResponse }) {
    const estadoColor = {
        APROBADO: 'text-green-600',
        RECHAZADO: 'text-red-600',
        PENDIENTE: 'text-yellow-600',
    }[pedido.estado] || 'text-gray-600';

    return (
        <div className="border p-4 rounded shadow bg-white">
            <div className="flex justify-between items-center">
                <div>
                    <p className="font-bold">Pedido #{pedido.id}</p>
                    <p className={estadoColor}>Estado: {pedido.estado}</p>
                </div>
                <Link href={`/envios/${pedido.id}`} className="text-blue-600 hover:underline">
                    Ver Envío →
                </Link>
            </div>
        </div>
    );
}