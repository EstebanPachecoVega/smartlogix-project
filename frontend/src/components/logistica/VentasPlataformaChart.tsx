'use client';

import * as React from 'react';
import { Area, AreaChart, CartesianGrid, XAxis } from 'recharts';
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
import { PedidoResponse } from '@/types';
import { filterByDate, RANGES } from '@/lib/filtro';

interface DataPoint {
  plataforma: string;
  total: number;
}

const COLOR_MAP: Record<string, string> = {
  MOBILE: '#3b82f6',
  DESKTOP: '#22c55e',
  TABLET: '#a855f7',
};

function formatLabel(s: string) {
  return s.charAt(0) + s.slice(1).toLowerCase();
}

export default function VentasPlataformaChart({ pedidos }: { pedidos: PedidoResponse[] }) {
  const [dias, setDias] = React.useState('30');

  const data = React.useMemo(() => {
    const filtrados = filterByDate(pedidos, 'fechaPedido', dias);
    const grouped = filtrados.reduce<Record<string, number>>((acc, p) => {
      const plat = p.plataforma || 'DESCONOCIDO';
      acc[plat] = (acc[plat] || 0) + p.totalCompra;
      return acc;
    }, {});
    return Object.entries(grouped).map(([plataforma, total]) => ({
      plataforma,
      total: Math.round(total),
    }));
  }, [pedidos, dias]);

  const chartConfig: Record<string, { label: string; color: string }> = {};
  for (const d of data) {
    chartConfig[d.plataforma] = {
      label: formatLabel(d.plataforma),
      color: COLOR_MAP[d.plataforma] || '#6b7280',
    };
  }

  if (data.length === 0) {
    return (
      <Card>
        <CardHeader className="flex flex-row items-center justify-between py-4">
          <CardTitle className="text-base">Ventas por plataforma</CardTitle>
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
          <p className="text-sm text-muted-foreground py-8 text-center">No hay datos de ventas por plataforma.</p>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between py-4">
        <CardTitle className="text-base">Ventas por plataforma</CardTitle>
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
        <ChartContainer
          config={chartConfig}
          className="aspect-auto h-[250px] w-full"
        >
          <AreaChart
            data={data}
            margin={{ left: 12, right: 12 }}
          >
            <defs>
              {data.map((d) => (
                <linearGradient key={d.plataforma} id={`grad-${d.plataforma}`} x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor={COLOR_MAP[d.plataforma] || '#6b7280'} stopOpacity={0.3} />
                  <stop offset="95%" stopColor={COLOR_MAP[d.plataforma] || '#6b7280'} stopOpacity={0} />
                </linearGradient>
              ))}
            </defs>
            <CartesianGrid vertical={false} />
            <XAxis
              dataKey="plataforma"
              tickLine={false}
              axisLine={false}
              tickMargin={8}
              tick={{ fontSize: 12 }}
              tickFormatter={formatLabel}
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
            {data.map((d) => (
              <Area
                key={d.plataforma}
                type="monotone"
                dataKey="total"
                data={[d]}
                name={d.plataforma}
                stroke={COLOR_MAP[d.plataforma] || '#6b7280'}
                fill={`url(#grad-${d.plataforma})`}
                strokeWidth={2}
                dot={{ fill: COLOR_MAP[d.plataforma] || '#6b7280', r: 4 }}
                activeDot={{ r: 6 }}
                animationDuration={500}
              />
            ))}
          </AreaChart>
        </ChartContainer>
      </CardContent>
    </Card>
  );
}
