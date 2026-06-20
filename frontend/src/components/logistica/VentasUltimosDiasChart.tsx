'use client';

import { useState, useMemo } from 'react';
import { PedidoResponse } from '@/types';
import { Bar, BarChart, CartesianGrid, XAxis } from 'recharts';
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
} from '@/components/ui/chart';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { RANGES } from '@/lib/filtro';

export default function VentasUltimosDiasChart({ pedidos }: { pedidos: PedidoResponse[] }) {
  const [dias, setDias] = useState('7');

  const data = useMemo(() => {
    const map = new Map<string, number>();
    const today = new Date();
    const numDias = parseInt(dias, 10);

    for (let i = numDias; i >= 0; i--) {
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
      if (diffDays < 0 || diffDays > numDias) continue;
      const key = d.toLocaleDateString('es-CL', { weekday: 'short' }) +
        ' ' + d.getDate();
      map.set(key, (map.get(key) || 0) + p.totalCompra);
    }

    return Array.from(map.entries()).map(([dia, venta]) => ({
      dia,
      venta: Math.round(venta),
    }));
  }, [pedidos, dias]);

  const chartConfig = {
    venta: { label: 'Ventas', color: '#22c55e' },
  };

  return (
    <Card>
      <CardHeader className="flex flex-row items-center gap-2 space-y-0 py-4">
        <CardTitle className="text-base flex-1">Ventas por día</CardTitle>
        <Select value={dias} onValueChange={(v: string | null) => setDias(v ?? '')}>
          <SelectTrigger className="w-[110px] h-8 text-xs" aria-label="Seleccionar rango">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            {RANGES.map((r) => (
              <SelectItem key={r.value} value={r.value} className="text-xs">
                {r.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </CardHeader>
      <CardContent>
        {data.every((d) => d.venta === 0) ? (
          <p className="text-sm text-muted-foreground py-8 text-center">No hay ventas en los últimos {dias} días.</p>
        ) : (
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
        )}
      </CardContent>
    </Card>
  );
}
