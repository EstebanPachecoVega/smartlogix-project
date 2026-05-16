'use client';

import { useEffect } from 'react';
import { useEnvioStore } from '@/store/envioStore';
import { useParams } from 'next/navigation';
import Spinner from '@/components/common/Spinner';

export default function EnvioDetallePage() {
  const { id } = useParams();
  const { currentEnvio, loading, error, obtenerEnvio } = useEnvioStore();

  useEffect(() => {
    if (id) obtenerEnvio(Number(id));
  }, [id, obtenerEnvio]);

  if (loading) return <Spinner />;
  if (error) return <div className="text-red-500">Error: {error}</div>;
  if (!currentEnvio) return <div>Envío no encontrado</div>;

  return (
    <div>
      <h1 className="text-2xl font-bold">Envío #{currentEnvio.id}</h1>
      <p>Pedido asociado: {currentEnvio.pedidoId}</p>
      <p>Estado: {currentEnvio.estado}</p>
    </div>
  );
}