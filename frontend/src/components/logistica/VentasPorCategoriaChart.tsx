'use client';

import * as React from 'react';
import { Bar, BarChart, CartesianGrid, XAxis, YAxis, Cell } from 'recharts';
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
import { Producto, PedidoResponse } from '@/types';
import { filterByDate, RANGES } from '@/lib/filtro';

const COLORS = [
  '#3b82f6', '#ef4444', '#22c55e', '#f59e0b', '#8b5cf6',
  '#ec4899', '#14b8a6', '#f97316', '#6366f1', '#84cc16',
  '#06b6d4', '#e11d48', '#a855f7', '#eab308', '#64748b',
];

const TOP_N = 15;

const chartConfig = {
  cantidad: { label: 'Unidades vendidas', color: '#3b82f6' },
};

export default function VentasPorCategoriaChart({ pedidos, productos }: { pedidos: PedidoResponse[]; productos: Producto[] }) {
  const [dias, setDias] = React.useState('30');

  const data = React.useMemo(() => {
    const productoMap = new Map<number, string>();
    for (const p of productos) {
      productoMap.set(p.id, p.categoriaNombre || 'Sin categoría');
    }

    const filtrados = filterByDate(pedidos, 'fechaPedido', dias);
    const grouped = new Map<string, number>();

    for (const pedido of filtrados) {
      if (!pedido.detalles) continue;
      for (const detalle of pedido.detalles) {
        const cat = productoMap.get(detalle.productoId) || 'Sin categoría';
        grouped.set(cat, (grouped.get(cat) || 0) + detalle.cantidad);
      }
    }

    const sorted = Array.from(grouped.entries())
      .map(([categoria, cantidad]) => ({ categoria, cantidad }))
      .sort((a, b) => b.cantidad - a.cantidad);

    if (sorted.length > TOP_N) {
      const top = sorted.slice(0, TOP_N);
      const others = sorted.slice(TOP_N).reduce((sum, d) => sum + d.cantidad, 0);
      if (others > 0) {
        top.push({ categoria: 'Otras', cantidad: others });
      }
      return top;
    }
    return sorted;
  }, [pedidos, productos, dias]);

  const empty = data.length === 0 || data.every((d) => d.cantidad === 0);
  const chartHeight = Math.max(250, data.length * 32 + 40);

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between py-4">
        <CardTitle className="text-base">Unidades vendidas por categoría</CardTitle>
        <Select value={dias} onValueChange={(v: string | null) => setDias(v ?? '')}>
          <SelectTrigger className="w-[130px] h-8 text-xs" aria-label="Seleccionar rango">
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
        {empty ? (
          <p className="text-sm text-muted-foreground py-8 text-center">No hay ventas registradas.</p>
        ) : (
          <ChartContainer
            config={chartConfig}
            className="w-full"
            style={{ height: chartHeight }}
          >
            <BarChart
              data={data}
              layout="vertical"
              margin={{ top: 0, right: 16, left: 0, bottom: 0 }}
              accessibilityLayer
              barCategoryGap="20%"
            >
              <CartesianGrid horizontal={false} />
              <YAxis
                type="category"
                dataKey="categoria"
                tickLine={false}
                axisLine={false}
                tick={{ fontSize: 11 }}
                width={140}
              />
              <XAxis type="number" tickLine={false} axisLine={false} tick={{ fontSize: 11 }} />
              <ChartTooltip
                cursor={false}
                content={
                  <ChartTooltipContent
                    formatter={(value) => (
                      <span className="font-mono font-medium tabular-nums text-foreground">
                        {Number(value ?? 0).toLocaleString('es-CL')} uds.
                      </span>
                    )}
                  />
                }
              />
              <Bar dataKey="cantidad" radius={[0, 4, 4, 0]} barSize={20} animationDuration={0}>
                {data.map((entry, index) => (
                  <Cell key={entry.categoria} fill={COLORS[index % COLORS.length]} />
                ))}
              </Bar>
            </BarChart>
          </ChartContainer>
        )}
      </CardContent>
    </Card>
  );
}
