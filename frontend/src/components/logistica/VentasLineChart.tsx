'use client';

import { PedidoResponse } from '@/types';
import { Line, LineChart, CartesianGrid, XAxis } from 'recharts';
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
} from '@/components/ui/chart';
import { useMemo } from 'react';

export default function VentasLineChart({ pedidos }: { pedidos: PedidoResponse[] }) {
  const data = useMemo(() => {
    const map = new Map<string, number>();
    const today = new Date();

    for (let i = 30; i >= 0; i--) {
      const d = new Date(today);
      d.setDate(d.getDate() - i);
      const key = d.toLocaleDateString('es-CL', { day: '2-digit', month: 'short' });
      map.set(key, 0);
    }

    for (const p of pedidos) {
      if (!p.fechaPedido) continue;
      const d = new Date(p.fechaPedido);
      const diffDays = Math.round((today.getTime() - d.getTime()) / 86400000);
      if (diffDays < 0 || diffDays > 30) continue;
      const key = d.toLocaleDateString('es-CL', { day: '2-digit', month: 'short' });
      map.set(key, (map.get(key) || 0) + p.totalCompra);
    }

    return Array.from(map.entries()).map(([dia, venta]) => ({
      dia,
      venta: Math.round(venta),
    }));
  }, [pedidos]);

  const chartConfig = {
    venta: { label: 'Ventas', color: '#3b82f6' },
  };

  if (data.every((d) => d.venta === 0)) {
    return <p className="text-sm text-muted-foreground py-8 text-center">No hay ventas en los últimos 30 días.</p>;
  }

  return (
    <ChartContainer config={chartConfig} className="min-h-[220px]">
      <LineChart data={data}>
        <CartesianGrid vertical={false} />
        <XAxis
          dataKey="dia"
          tickLine={false}
          axisLine={false}
          tickMargin={8}
          tick={{ fontSize: 10 }}
          interval="preserveStartEnd"
        />
        <ChartTooltip
          cursor={false}
          content={
            <ChartTooltipContent
              formatter={(value) => (
                <span className="font-mono font-medium tabular-nums text-foreground">
                  ${Number(value ?? 0).toLocaleString('es-CL')}
                </span>
              )}
            />
          }
        />
        <Line
          type="monotone"
          dataKey="venta"
          stroke="var(--color-venta)"
          strokeWidth={2}
          dot={false}
          activeDot={{ r: 4, fill: '#3b82f6' }}
        />
      </LineChart>
    </ChartContainer>
  );
}
