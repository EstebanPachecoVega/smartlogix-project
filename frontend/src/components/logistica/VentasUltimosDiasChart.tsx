'use client';

import { PedidoResponse } from '@/types';
import { Bar, BarChart, CartesianGrid, XAxis } from 'recharts';
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
} from '@/components/ui/chart';
import { useMemo } from 'react';

export default function VentasUltimosDiasChart({ pedidos }: { pedidos: PedidoResponse[] }) {
  const data = useMemo(() => {
    const map = new Map<string, number>();
    const today = new Date();

    for (let i = 7; i >= 0; i--) {
      const d = new Date(today);
      d.setDate(d.getDate() - i);
      const key = d.toLocaleDateString('es-CL', { weekday: 'short' }) +
        ' ' + d.getDate();
      map.set(key, 0);
    }

    for (const p of pedidos) {
      if (!p.fechaPedido) continue;
      const d = new Date(p.fechaPedido);
      const diffDays = Math.round((today.getTime() - d.getTime()) / 86400000);
      if (diffDays < 0 || diffDays > 7) continue;
      const key = d.toLocaleDateString('es-CL', { weekday: 'short' }) +
        ' ' + d.getDate();
      map.set(key, (map.get(key) || 0) + p.totalCompra);
    }

    return Array.from(map.entries()).map(([dia, venta]) => ({
      dia,
      venta: Math.round(venta),
    }));
  }, [pedidos]);

  const chartConfig = {
    venta: { label: 'Ventas', color: '#22c55e' },
  };

  if (data.every((d) => d.venta === 0)) {
    return <p className="text-sm text-muted-foreground py-8 text-center">No hay ventas en los últimos 7 días.</p>;
  }

  return (
    <ChartContainer config={chartConfig} className="min-h-[220px]">
      <BarChart data={data}>
        <CartesianGrid vertical={false} />
        <XAxis
          dataKey="dia"
          tickLine={false}
          tickMargin={8}
          axisLine={false}
          tick={{ fontSize: 11 }}
        />
        <ChartTooltip
          cursor={false}
          content={
            <ChartTooltipContent
              hideLabel
              formatter={(value) => (
                <span className="font-mono font-medium tabular-nums text-foreground">
                  ${Number(value ?? 0).toLocaleString('es-CL')}
                </span>
              )}
            />
          }
        />
        <Bar
          dataKey="venta"
          fill="var(--color-venta)"
          radius={[4, 4, 0, 0]}
          barSize={32}
        />
      </BarChart>
    </ChartContainer>
  );
}
