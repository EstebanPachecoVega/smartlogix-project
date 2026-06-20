'use client';

import { useState } from 'react';
import { enviosApi } from '@/lib/api';
import { Button } from '@/components/ui/button';
import {
  Select,
  SelectTrigger,
  SelectValue,
  SelectContent,
  SelectItem,
} from '@/components/ui/select';
import { Loader2 } from 'lucide-react';
import { toast } from 'sonner';
import { estadoEnvioOpciones, EstadoEnvioOption } from '@/lib/estados';

interface Props {
  envioId: number;
  estadoActual: string;
  onSuccess: () => void;
}

export default function ActualizarEstadoForm({ envioId, estadoActual, onSuccess }: Props) {
  const [loading, setLoading] = useState(false);
  const [nuevoEstado, setNuevoEstado] = useState(estadoActual);

  const sinCambios = nuevoEstado === estadoActual;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (sinCambios) return;

    setLoading(true);
    try {
      await enviosApi.actualizarEstado(envioId, nuevoEstado);
      const label = estadoEnvioOpciones.find((o) => o.value === nuevoEstado)?.label ?? nuevoEstado;
      toast.success('Estado actualizado', {
        description: `El envío pasó a "${label}".`,
      });
      onSuccess();
    } catch {
      toast.error('Error al actualizar', {
        description: 'No se pudo cambiar el estado. Intenta de nuevo.',
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="flex flex-wrap items-center gap-3">
      <Select value={nuevoEstado} onValueChange={(v: string | null) => setNuevoEstado(v ?? '')} disabled={loading}>
        <SelectTrigger className="w-52">
          <SelectValue />
        </SelectTrigger>
        <SelectContent>
          {estadoEnvioOpciones.map((est: EstadoEnvioOption) => (
            <SelectItem key={est.value} value={est.value}>
              {est.label}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>

      <Button type="submit" disabled={loading || sinCambios} className="shrink-0">
        {loading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
        {loading ? 'Actualizando…' : 'Actualizar estado'}
      </Button>
    </form>
  );
}