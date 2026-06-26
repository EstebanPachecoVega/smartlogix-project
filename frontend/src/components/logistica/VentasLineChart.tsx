'use client';

import * as React from 'react';
import { memo } from 'react';
import { CartesianGrid, Line, LineChart, XAxis, YAxis } from 'recharts';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  type ChartConfig,
} from '@/components/ui/chart';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { PedidoResponse } from '@/types';
import { filterByDate, RANGES } from '@/lib/filtro';

const chartConfig = {
  mobile: { label: 'Mobile', color: '#3b82f6' },
  desktop: { label: 'Desktop', color: '#22c55e' },
} satisfies ChartConfig;

type ActiveKey = 'all' | 'mobile' | 'desktop';

const VentasLineChart = memo(function VentasLineChart({ pedidos }: { pedidos: PedidoResponse[] }) {
  const [dias, setDias] = React.useState('30');
  const [activeChart, setActiveChart] = React.useState<ActiveKey>('all');

  const total = React.useMemo(() => {
    const filtrados = filterByDate(pedidos, 'fechaPedido', dias);
    let mobile = 0;
    let desktop = 0;
    for (const p of filtrados) {
      if (p.plataforma === 'MOBILE') mobile += p.totalCompra;
      else if (p.plataforma === 'DESKTOP') desktop += p.totalCompra;
    }
    return {
      all: Math.round(mobile + desktop),
      mobile: Math.round(mobile),
      desktop: Math.round(desktop),
    };
  }, [pedidos, dias]);

  const data = React.useMemo(() => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const numDias = parseInt(dias, 10);

    const map = new Map<string, { mobile: number; desktop: number }>();
    for (let i = numDias; i >= 0; i--) {
      const d = new Date(today);
      d.setDate(d.getDate() - i);
      const key = d.toISOString().slice(0, 10);
      map.set(key, { mobile: 0, desktop: 0 });
    }

    for (const p of pedidos) {
      if (!p.fechaPedido) continue;
      const d = new Date(p.fechaPedido.split('T')[0] + 'T12:00:00');
      d.setHours(0, 0, 0, 0);
      const key = d.toISOString().slice(0, 10);
      const entry = map.get(key);
      if (!entry) continue;
      if (p.plataforma === 'MOBILE') entry.mobile += p.totalCompra;
      else if (p.plataforma === 'DESKTOP') entry.desktop += p.totalCompra;
    }

    return Array.from(map.entries()).map(([date, { mobile, desktop }]) => ({
      date,
      mobile: Math.round(mobile),
      desktop: Math.round(desktop),
    }));
  }, [pedidos, dias]);

  const empty = data.every((d) => d.mobile === 0 && d.desktop === 0);

  return (
    <Card>
      <CardHeader className="flex flex-col items-stretch border-b p-0! sm:flex-row">
        <div className="flex flex-1 flex-col justify-center gap-1 px-6 py-4">
          <div className="flex items-center justify-between">
            <CardTitle className="text-base">Tendencia de ventas</CardTitle>
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
          </div>
          <CardDescription>Comparativa Mobile vs Desktop</CardDescription>
        </div>
        <div className="flex">
          {(['all', 'mobile', 'desktop'] as const).map((key) => (
            <button
              key={key}
              data-active={activeChart === key}
              className="flex flex-1 flex-col justify-center gap-1 border-t px-4 py-3 text-left even:border-l data-[active=true]:bg-muted/50 sm:border-t-0 sm:border-l sm:px-6 sm:py-4"
              onClick={() => setActiveChart(key)}
            >
              <span className="text-xs text-muted-foreground">
                {key === 'all' ? 'Ambas' : chartConfig[key].label}
              </span>
              <span className="text-base leading-none font-bold sm:text-xl tabular-nums">
                ${total[key].toLocaleString('es-CL')}
              </span>
            </button>
          ))}
        </div>
      </CardHeader>
      <CardContent className="px-2 sm:p-6">
        {empty ? (
          <p className="text-sm text-muted-foreground py-8 text-center">No hay ventas en el período seleccionado.</p>
        ) : (
          <ChartContainer
            config={chartConfig}
            className="aspect-auto h-[250px] w-full"
          >
            <LineChart
              accessibilityLayer
              data={data}
            >
              <CartesianGrid vertical={false} />
              <XAxis
                dataKey="date"
                tickLine={false}
                axisLine={false}
                tickMargin={8}
                minTickGap={16}
                angle={-30}
                textAnchor="end"
                height={60}
                tick={{ fontSize: 10 }}
                tickFormatter={(value: string) => {
                  const date = new Date(value + 'T12:00:00');
                  return date.toLocaleDateString('es-CL', {
                    month: 'short',
                    day: 'numeric',
                  });
                }}
              />
              <YAxis
                tickLine={false}
                axisLine={false}
                tick={{ fontSize: 11 }}
                tickFormatter={(value: number) => value.toLocaleString('es-CL')}
                domain={[0, (dataMax: number) => {
                  const padded = Math.round(dataMax * 1.15);
                  return padded === 0 ? 100 : padded;
                }]}
              />
              <ChartTooltip
                content={
                  <ChartTooltipContent
                    className="w-[150px]"
                    nameKey="views"
                    labelFormatter={(value: string) => {
                      return new Date(value + 'T12:00:00').toLocaleDateString('es-CL', {
                        month: 'short',
                        day: 'numeric',
                        year: 'numeric',
                      });
                    }}
                  />
                }
              />
              {(activeChart === 'all' || activeChart === 'mobile') && (
                <Line
                  dataKey="mobile"
                  type="monotone"
                  stroke="var(--color-mobile)"
                  strokeWidth={2}
                  dot={false}
                />
              )}
              {(activeChart === 'all' || activeChart === 'desktop') && (
                <Line
                  dataKey="desktop"
                  type="monotone"
                  stroke="var(--color-desktop)"
                  strokeWidth={2}
                  dot={false}
                />
              )}
            </LineChart>
          </ChartContainer>
        )}
      </CardContent>
    </Card>
  );
});
export default VentasLineChart;
