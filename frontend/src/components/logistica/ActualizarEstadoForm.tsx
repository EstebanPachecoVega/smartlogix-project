'use client';

import { useState } from 'react';
import { enviosApi } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { Select, SelectTrigger, SelectValue, SelectContent, SelectItem } from '@/components/ui/select';

const estados = [
  'PENDIENTE', 'PREPARANDO', 'ENVIADO', 'EN_TRANSITO', 'EN_REPARTO',
  'ENTREGADO', 'INTENTO_FALLIDO', 'RETRASADO', 'DEVUELTO', 'CANCELADO'
];

export default function ActualizarEstadoForm({ envioId, estadoActual, onSuccess }: { envioId: number; estadoActual: string; onSuccess: () => void }) {
  const [loading, setLoading] = useState(false);
  const [nuevoEstado, setNuevoEstado] = useState(estadoActual);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await enviosApi.actualizarEstado(envioId, nuevoEstado);
      alert('Estado actualizado correctamente'); // reemplazar por toast
      onSuccess();
    } catch (error) {
      alert('Error al actualizar');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="flex gap-2">
      <Select value={nuevoEstado} onValueChange={setNuevoEstado}>
        <SelectTrigger className="w-48">
          <SelectValue />
        </SelectTrigger>
        <SelectContent>
          {estados.map((est) => (
            <SelectItem key={est} value={est}>{est}</SelectItem>
          ))}
        </SelectContent>
      </Select>
      <Button type="submit" disabled={loading || nuevoEstado === estadoActual}>
        Actualizar
      </Button>
    </form>
  );
}