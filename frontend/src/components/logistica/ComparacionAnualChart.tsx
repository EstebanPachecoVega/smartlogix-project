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

export default function ComparacionAnualChart() {
  const [data, setData] = useState<DataPoint[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    estadisticasApi.comparacionAnual()
      .then((raw) => {
        const mapped = raw
          .filter((d) => d.mes >= 1 && d.mes <= 12)
          .map((d) => ({
            mes: MONTHS[d.mes - 1] || '',
            añoActual: d.añoActual ?? 0,
            añoAnterior: d.añoAnterior ?? 0,
          }));
        setData(mapped);
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Spinner />;

  if (data.length === 0) {
    return <p className="text-sm text-muted-foreground py-8 text-center">No hay datos de comparación anual.</p>;
  }

  return (
    <ChartContainer config={chartConfig} className="min-h-[220px]">
      <AreaChart data={data}>
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
        />
        <ChartLegend content={<ChartLegendContent />} />
      </AreaChart>
    </ChartContainer>
  );
}
