import Link from 'next/link';
import type { PedidoResponse } from '@/types';

interface Props {
  pedido: PedidoResponse;
}

export default function PedidoCard({ pedido }: Props) {
  const estadoColor = {
    APROBADO: 'text-green-600',
    RECHAZADO: 'text-red-600',
    PENDIENTE: 'text-yellow-600',
  }[pedido.estado] || 'text-gray-600';

  return (
    <div className="border p-4 rounded-lg shadow hover:shadow-md transition">
      <div className="flex justify-between items-center">
        <span className="font-semibold">Pedido #{pedido.id}</span>
        <span className={estadoColor}>{pedido.estado}</span>
      </div>
      <Link href={`/pedidos/${pedido.id}`} className="text-blue-500 text-sm">
        Ver detalle →
      </Link>
    </div>
  );
}