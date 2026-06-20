'use client';

import * as React from 'react';
import { Area, AreaChart, CartesianGrid, XAxis } from 'recharts';
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  ChartLegend,
  ChartLegendContent,
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
import { PedidoResponse } from '@/types';
import { filterByDate, RANGES } from '@/lib/filtro';

const MONTHS = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];

interface DataPoint {
  mes: string;
  añoActual: number;
  añoAnterior: number;
}

const chartConfig = {
  añoActual: { label: 'Año actual', color: '#3b82f6' },
  añoAnterior: { label: 'Año anterior', color: '#94a3b8' },
};

export default function ComparacionAnualChart({ pedidos }: { pedidos: PedidoResponse[] }) {
  const [dias, setDias] = React.useState('30');

  const data = React.useMemo(() => {
    const filtrados = filterByDate(pedidos, 'fechaPedido', dias);
    const now = new Date();
    const currentYear = now.getFullYear();
    const previousYear = currentYear - 1;

    const grouped = new Map<string, { current: number; previous: number }>();
    for (let m = 1; m <= 12; m++) {
      grouped.set(m.toString(), { current: 0, previous: 0 });
    }

    for (const p of filtrados) {
      if (!p.fechaPedido) continue;
      const d = new Date(p.fechaPedido.split('T')[0] + 'T12:00:00');
      const m = d.getMonth() + 1;
      const y = d.getFullYear();
      const entry = grouped.get(m.toString());
      if (!entry) continue;
      if (y === currentYear) entry.current += p.totalCompra;
      else if (y === previousYear) entry.previous += p.totalCompra;
    }

    return Array.from(grouped.entries()).map(([mesNum, { current, previous }]) => ({
      mes: MONTHS[parseInt(mesNum, 10) - 1] || '',
      añoActual: Math.round(current),
      añoAnterior: Math.round(previous),
    }));
  }, [pedidos, dias]);

  const empty = data.every((d) => d.añoActual === 0 && d.añoAnterior === 0);

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between py-4">
        <CardTitle className="text-base">Comparación anual</CardTitle>
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
          <p className="text-sm text-muted-foreground py-8 text-center">No hay datos de comparación anual.</p>
        ) : (
          <ChartContainer
            config={chartConfig}
            className="aspect-auto h-[250px] w-full"
          >
            <AreaChart
              data={data}
              margin={{ left: 12, right: 12 }}
            >
              <defs>
                <linearGradient id="grad-actual" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.3} />
                  <stop offset="95%" stopColor="#3b82f6" stopOpacity={0} />
                </linearGradient>
                <linearGradient id="grad-anterior" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#94a3b8" stopOpacity={0.3} />
                  <stop offset="95%" stopColor="#94a3b8" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid vertical={false} />
              <XAxis
                dataKey="mes"
                tickLine={false}
                axisLine={false}
                tickMargin={8}
                tick={{ fontSize: 11 }}
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
              <Area
                type="monotone"
                dataKey="añoActual"
                stroke="#3b82f6"
                fill="url(#grad-actual)"
                strokeWidth={2}
                dot={{ fill: '#3b82f6', r: 3 }}
                activeDot={{ r: 5 }}
                animationDuration={500}
              />
              <Area
                type="monotone"
                dataKey="añoAnterior"
                stroke="#94a3b8"
                fill="url(#grad-anterior)"
                strokeWidth={2}
                strokeDasharray="4 3"
                dot={{ fill: '#94a3b8', r: 3 }}
                activeDot={{ r: 5 }}
                animationDuration={500}
              />
              <ChartLegend content={<ChartLegendContent />} />
            </AreaChart>
          </ChartContainer>
        )}
      </CardContent>
    </Card>
  );
}
