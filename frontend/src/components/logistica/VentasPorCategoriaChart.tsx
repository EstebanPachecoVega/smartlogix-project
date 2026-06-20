'use client';

import * as React from 'react';
import { Bar, BarChart, CartesianGrid, XAxis, YAxis } from 'recharts';
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

interface DataPoint {
  categoria: string;
  cantidad: number;
}

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

    return Array.from(grouped.entries())
      .map(([categoria, cantidad]) => ({ categoria, cantidad }))
      .sort((a, b) => b.cantidad - a.cantidad);
  }, [pedidos, productos, dias]);

  const chartConfig = {
    cantidad: { label: 'Unidades vendidas', color: '#3b82f6' },
  };

  const empty = data.length === 0 || data.every((d) => d.cantidad === 0);

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between py-4">
        <CardTitle className="text-base">Unidades vendidas por categoría</CardTitle>
        <Select value={dias} onValueChange={setDias}>
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
            className="aspect-auto h-[250px] w-full"
          >
            <BarChart
              data={data}
              margin={{ top: 0, right: 16, left: 0, bottom: 0 }}
              accessibilityLayer
            >
              <CartesianGrid vertical={false} />
              <XAxis
                dataKey="categoria"
                type="category"
                tickLine={false}
                axisLine={false}
                tick={{ fontSize: 12 }}
              />
              <YAxis type="number" tickLine={false} axisLine={false} tick={{ fontSize: 11 }} />
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
              <Bar
                dataKey="cantidad"
                fill="var(--color-cantidad)"
                radius={[4, 4, 0, 0]}
                barSize={20}
                animationDuration={500}
              />
            </BarChart>
          </ChartContainer>
        )}
      </CardContent>
    </Card>
  );
}
