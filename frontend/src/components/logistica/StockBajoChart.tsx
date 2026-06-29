'use client';

import { Producto } from '@/types';
import { Bar, BarChart, CartesianGrid, XAxis, YAxis, Cell } from 'recharts';
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
} from '@/components/ui/chart';
import { useMemo, memo } from 'react';

const chartConfig = {
  stock: { label: 'Stock', color: '#ef4444' },
};

function getStockColor(stock: number): string {
  if (stock <= 3) return '#ef4444';
  if (stock <= 6) return '#f97316';
  if (stock <= 10) return '#eab308';
  return '#64748b';
}

const StockBajoChart = memo(function StockBajoChart({ productos }: { productos: Producto[] }) {
  const data = useMemo(() => {
    return [...productos]
      .sort((a, b) => a.cantidad - b.cantidad)
      .slice(0, 10)
      .map((p) => ({
        nombre: p.nombre.length > 30 ? p.nombre.slice(0, 27) + '...' : p.nombre,
        stock: p.cantidad,
      }));
  }, [productos]);

  if (data.length === 0) {
    return <p className="text-sm text-muted-foreground py-8 text-center">No hay productos registrados.</p>;
  }

  return (
    <ChartContainer
      config={chartConfig}
      className="w-full"
      style={{ height: Math.max(200, data.length * 36) }}
    >
      <BarChart data={data} layout="vertical" margin={{ left: 0, right: 32 }}>
        <CartesianGrid horizontal={false} />
        <XAxis
          type="number"
          tickLine={false}
          axisLine={false}
          tick={{ fontSize: 11 }}
          tickFormatter={(value: number) => value.toString()}
        />
        <YAxis
          type="category"
          dataKey="nombre"
          tickLine={false}
          axisLine={false}
          tick={{ fontSize: 11 }}
          width={180}
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
        <Bar dataKey="stock" radius={[0, 4, 4, 0]} barSize={16}>
          {data.map((entry, index) => (
            <Cell key={`cell-${index}`} fill={getStockColor(entry.stock)} />
          ))}
        </Bar>
      </BarChart>
    </ChartContainer>
  );
});
export default StockBajoChart;
