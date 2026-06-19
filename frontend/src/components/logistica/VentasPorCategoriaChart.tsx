'use client';

import { useEffect, useState } from 'react';
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
import { estadisticasApi } from '@/lib/api';
import Spinner from '@/components/shared/Spinner';

interface DataPoint {
  categoria: string;
  cantidad: number;
}

export default function VentasPorCategoriaChart() {
  const [data, setData] = useState<DataPoint[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    estadisticasApi.ventasPorCategoria()
      .then(setData)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const chartConfig = {
    cantidad: { label: 'Unidades vendidas', color: '#3b82f6' },
  };

  if (loading) return <Spinner />;

  const empty = data.length === 0 || data.every((d) => d.cantidad === 0);

  return (
    <Card>
      <CardHeader className="py-4">
        <CardTitle className="text-base">Unidades vendidas por categoría</CardTitle>
      </CardHeader>
      <CardContent>
        {empty ? (
          <p className="text-sm text-muted-foreground py-8 text-center">No hay ventas registradas.</p>
        ) : (
          <ChartContainer config={chartConfig} className="min-h-[350px] w-full">
            <BarChart data={data} layout="vertical" margin={{ left: 0, right: 16 }}>
              <CartesianGrid horizontal={false} />
              <XAxis type="number" tickLine={false} axisLine={false} tick={{ fontSize: 11 }} />
              <YAxis
                dataKey="categoria"
                type="category"
                tickLine={false}
                axisLine={false}
                tickMargin={8}
                tick={{ fontSize: 12 }}
                width={140}
              />
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
                radius={[0, 4, 4, 0]}
                barSize={20}
              />
            </BarChart>
          </ChartContainer>
        )}
      </CardContent>
    </Card>
  );
}
