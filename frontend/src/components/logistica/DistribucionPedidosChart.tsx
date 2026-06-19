'use client';

import { PedidoResponse } from '@/types';
import { Pie, PieChart } from 'recharts';
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
} from '@/components/ui/chart';
import { useMemo } from 'react';

const ESTADO_COLORS: Record<string, string> = {
  PENDIENTE: '#f59e0b',
  CONFIRMADO: '#3b82f6',
  EN_PREPARACION: '#8b5cf6',
  ENVIADO: '#06b6d4',
  ENTREGADO: '#22c55e',
  CANCELADO: '#ef4444',
};

const ESTADO_LABELS: Record<string, string> = {
  PENDIENTE: 'Pendiente',
  CONFIRMADO: 'Confirmado',
  EN_PREPARACION: 'En preparación',
  ENVIADO: 'Enviado',
  ENTREGADO: 'Entregado',
  CANCELADO: 'Cancelado',
};

export default function DistribucionPedidosChart({ pedidos }: { pedidos: PedidoResponse[] }) {
  const data = useMemo(() => {
    const grouped = pedidos.reduce<Record<string, number>>((acc, p) => {
      const estado = p.estado || 'DESCONOCIDO';
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
  }, [pedidos]);

  const chartConfig = useMemo(() => {
    const config: Record<string, { label: string; color: string }> = {};
    for (const { estado, fill } of data) {
      config[estado] = { label: ESTADO_LABELS[estado] || estado, color: fill };
    }
    return config;
  }, [data]);

  if (data.length === 0) {
    return <p className="text-sm text-muted-foreground py-8 text-center">No hay pedidos registrados.</p>;
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
