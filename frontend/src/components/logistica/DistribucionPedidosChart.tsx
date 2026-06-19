'use client';

import { PedidoResponse } from '@/types';
import { Pie, PieChart } from 'recharts';
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  ChartLegend,
  ChartLegendContent,
} from '@/components/ui/chart';
import { useMemo } from 'react';
import {
  estadoPedidoHexColor,
  estadoPedidoTexto,
  isEstadoPedido,
} from '@/lib/estados';

export default function DistribucionPedidosChart({ pedidos }: { pedidos: PedidoResponse[] }) {
  const data = useMemo(() => {
    const grouped = pedidos.reduce<Record<string, number>>((acc, p) => {
      const estado = isEstadoPedido(p.estado) ? p.estado : 'DESCONOCIDO';
      acc[estado] = (acc[estado] || 0) + 1;
      return acc;
    }, {});

    return Object.entries(grouped)
      .map(([estado, cantidad]) => ({
        estado,
        cantidad,
        fill: estadoPedidoHexColor[estado as keyof typeof estadoPedidoHexColor] || '#9ca3af',
      }))
      .sort((a, b) => b.cantidad - a.cantidad);
  }, [pedidos]);

  const chartConfig = useMemo(() => {
    const config: Record<string, { label: string; color: string }> = {};
    for (const { estado, fill } of data) {
      const label = isEstadoPedido(estado) ? estadoPedidoTexto[estado] : estado;
      config[estado] = { label, color: fill };
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
        <ChartLegend
          content={<ChartLegendContent nameKey="estado" />}
        />
      </PieChart>
    </ChartContainer>
  );
}
