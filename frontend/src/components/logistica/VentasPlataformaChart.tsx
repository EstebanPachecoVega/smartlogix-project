'use client';

import { useEffect, useState } from 'react';
import { Area, AreaChart, CartesianGrid, XAxis } from 'recharts';
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
} from '@/components/ui/chart';
import { estadisticasApi } from '@/lib/api';
import Spinner from '@/components/shared/Spinner';

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

export default function VentasPlataformaChart() {
  const [data, setData] = useState<DataPoint[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    estadisticasApi.ventasPlataforma()
      .then(setData)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const chartConfig: Record<string, { label: string; color: string }> = {};
  for (const d of data) {
    chartConfig[d.plataforma] = {
      label: formatLabel(d.plataforma),
      color: COLOR_MAP[d.plataforma] || '#6b7280',
    };
  }

  if (loading) return <Spinner />;

  if (data.length === 0) {
    return <p className="text-sm text-muted-foreground py-8 text-center">No hay datos de ventas por plataforma.</p>;
  }

  return (
    <ChartContainer config={chartConfig} className="min-h-[220px]">
      <AreaChart data={data}>
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
          />
        ))}
      </AreaChart>
    </ChartContainer>
  );
}
