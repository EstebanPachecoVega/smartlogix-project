'use client';

import { useEffect, useState } from 'react';
import { Area, AreaChart, CartesianGrid, XAxis } from 'recharts';
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  ChartLegend,
  ChartLegendContent,
} from '@/components/ui/chart';
import { estadisticasApi } from '@/lib/api';
import Spinner from '@/components/shared/Spinner';

interface DataPoint {
  categoria: string;
  totalVentas: number;
}

const PALETTE = [
  '#3b82f6', '#22c55e', '#a855f7', '#f97316',
  '#ef4444', '#06b6d4', '#eab308', '#ec4899',
];

export default function VentasPorCategoriaChart() {
  const [data, setData] = useState<DataPoint[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    estadisticasApi.ventasPorCategoria()
      .then(setData)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const chartConfig: Record<string, { label: string; color: string }> = {};
  data.forEach((d, i) => {
    chartConfig[d.categoria] = {
      label: d.categoria,
      color: PALETTE[i % PALETTE.length],
    };
  });

  if (loading) return <Spinner />;

  if (data.length === 0) {
    return <p className="text-sm text-muted-foreground py-8 text-center">No hay datos de ventas por categoría.</p>;
  }

  return (
    <ChartContainer config={chartConfig} className="min-h-[220px]">
      <AreaChart data={data}>
        <defs>
          {data.map((d, i) => (
            <linearGradient key={d.categoria} id={`grad-cat-${d.categoria}`} x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor={PALETTE[i % PALETTE.length]} stopOpacity={0.3} />
              <stop offset="95%" stopColor={PALETTE[i % PALETTE.length]} stopOpacity={0} />
            </linearGradient>
          ))}
        </defs>
        <CartesianGrid vertical={false} />
        <XAxis
          dataKey="categoria"
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
        {data.map((d, i) => (
          <Area
            key={d.categoria}
            type="monotone"
            dataKey="totalVentas"
            data={[d]}
            name={d.categoria}
            stroke={PALETTE[i % PALETTE.length]}
            fill={`url(#grad-cat-${d.categoria})`}
            strokeWidth={2}
            dot={{ fill: PALETTE[i % PALETTE.length], r: 3 }}
            activeDot={{ r: 5 }}
          />
        ))}
        <ChartLegend content={<ChartLegendContent />} />
      </AreaChart>
    </ChartContainer>
  );
}
