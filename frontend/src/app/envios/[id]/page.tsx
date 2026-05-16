'use client';
import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import { enviosService } from '@/services/enviosService';
import { EnvioResponse } from '@/types';
import Spinner from '@/components/Spinner';
import { useUIStore } from '@/stores/uiStore';

export default function EnvioPage() {
  const { id } = useParams();
  const [envio, setEnvio] = useState<EnvioResponse | null>(null);
  const { loading, setLoading, showToast } = useUIStore();

  useEffect(() => {
    const cargar = async () => {
      if (!id) return;
      setLoading(true);
      try {
        const data = await enviosService.obtener(Number(id));
        setEnvio(data);
      } catch (err: any) {
        showToast(err.detail || 'Error al cargar el envío', 'error');
      } finally {
        setLoading(false);
      }
    };
    cargar();
  }, [id, setLoading, showToast]);

  if (loading) return <Spinner />;
  if (!envio) return <p>No se encontró el envío</p>;

  const estadoColor = {
    CREADO: 'text-blue-600',
    EN_CURSO: 'text-yellow-600',
    ENTREGADO: 'text-green-600',
  }[envio.estado] || 'text-gray-600';

  return (
    <div className="max-w-md mx-auto bg-white p-6 rounded shadow">
      <h1 className="text-2xl font-bold mb-4">Detalle del Envío</h1>
      <p><strong>ID Envío:</strong> {envio.id}</p>
      <p><strong>Pedido Asociado:</strong> {envio.pedidoId}</p>
      <p className={estadoColor}><strong>Estado:</strong> {envio.estado}</p>
    </div>
  );
}