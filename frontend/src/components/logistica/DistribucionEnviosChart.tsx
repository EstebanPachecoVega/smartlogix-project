'use client';

import { Envio } from '@/types';
import { Pie, PieChart } from 'recharts';
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
} from '@/components/ui/chart';
import { useMemo } from 'react';

const ESTADO_COLORS: Record<string, string> = {
  PENDIENTE: '#f59e0b',
  EN_TRANSITO: '#3b82f6',
  ENTREGADO: '#22c55e',
  INTENTO_FALLIDO: '#ef4444',
  RETRASADO: '#f97316',
  DEVUELTO: '#6b7280',
};

const ESTADO_LABELS: Record<string, string> = {
  PENDIENTE: 'Pendiente',
  EN_TRANSITO: 'En tránsito',
  ENTREGADO: 'Entregado',
  INTENTO_FALLIDO: 'Intento fallido',
  RETRASADO: 'Retrasado',
  DEVUELTO: 'Devuelto',
};

export default function DistribucionEnviosChart({ envios }: { envios: Envio[] }) {
  const data = useMemo(() => {
    const grouped = envios.reduce<Record<string, number>>((acc, e) => {
      const estado = e.estadoEnvio || 'DESCONOCIDO';
      acc[estado] = (acc[estado] || 0) + 1;
      return acc;
    }, {});

    return Object.entries(grouped)
      .map(([estado, cantidad]) => ({
        estado,
        cantidad,
        fill: ESTADO_COLORS[estado] || '#9ca3af',
      }))
      .sort((a, b) => b.cantidad - a.cantidad);
  }, [envios]);

  const chartConfig = useMemo(() => {
    const config: Record<string, { label: string; color: string }> = {};
    for (const { estado, fill } of data) {
      config[estado] = { label: ESTADO_LABELS[estado] || estado, color: fill };
    }
    return config;
  }, [data]);

  if (data.length === 0) {
    return <p className="text-sm text-muted-foreground py-8 text-center">No hay envíos registrados.</p>;
  }

  return (
    <ChartContainer config={chartConfig} className="min-h-[220px]">
      <PieChart>
        <ChartTooltip
          cursor={false}
          content={<ChartTooltipContent hideLabel />}
        />
        <Pie
          data={data}
          dataKey="cantidad"
          nameKey="estado"
          innerRadius={55}
          strokeWidth={2}
        />
      </PieChart>
    </ChartContainer>
  );
}
