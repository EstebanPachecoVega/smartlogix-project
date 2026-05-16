import type { EnvioResponse } from '@/types';
import Link from 'next/link';

interface Props {
  envio: EnvioResponse;
}

export default function EnvioCard({ envio }: Props) {
  const estadoColor = {
    CREADO: 'text-yellow-600',
    EN_CURSO: 'text-blue-600',
    ENTREGADO: 'text-green-600',
  }[envio.estado] || 'text-gray-600';

  return (
    <div className="border p-4 rounded-lg shadow hover:shadow-md transition">
      <div className="flex justify-between items-center">
        <span className="font-semibold">Envío #{envio.id}</span>
        <span className={estadoColor}>{envio.estado}</span>
      </div>
      <p className="text-sm text-gray-600">Pedido: #{envio.pedidoId}</p>
      <Link href={`/envios/${envio.id}`} className="text-blue-500 text-sm">
        Ver detalle →
      </Link>
    </div>
  );
}