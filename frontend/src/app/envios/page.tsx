'use client';

import { useEffect } from 'react';
import { useEnvioStore } from '@/store/envioStore';
import EnvioCard from '@/components/envios/EnvioCard';
import Spinner from '@/components/common/Spinner';

export default function EnviosPage() {
  const { envios, loading, error, listarEnvios } = useEnvioStore();

  useEffect(() => {
    listarEnvios();
  }, [listarEnvios]);

  if (loading) return <Spinner />;
  if (error) return <div className="text-red-500">Error: {error}</div>;

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Listado de Envíos</h1>
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {envios.map((envio) => (
          <EnvioCard key={envio.id} envio={envio} />
        ))}
        {envios.length === 0 && <p>No hay envíos registrados.</p>}
      </div>
    </div>
  );
}