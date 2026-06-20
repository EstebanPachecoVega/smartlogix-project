'use client';

import { Producto } from '@/types';
import { Bar, BarChart, CartesianGrid, XAxis } from 'recharts';
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
} from '@/components/ui/chart';
import { useMemo } from 'react';

export default function StockBajoChart({ productos }: { productos: Producto[] }) {
  const data = useMemo(() => {
    return [...productos]
      .sort((a, b) => a.cantidad - b.cantidad)
      .slice(0, 10)
      .map((p) => ({
        nombre: p.nombre.length > 25 ? p.nombre.slice(0, 22) + '...' : p.nombre,
        stock: p.cantidad,
      }));
  }, [productos]);

  const chartConfig = {
    stock: { label: 'Stock', color: '#ef4444' },
  };

  if (data.length === 0) {
    return <p className="text-sm text-muted-foreground py-8 text-center">No hay productos registrados.</p>;
  }

  return (
    <ChartContainer config={chartConfig} className="aspect-auto h-[250px] w-full">
      <BarChart data={data} layout="vertical" margin={{ left: 0, right: 0 }}>
        <CartesianGrid horizontal={false} />
        <XAxis type="number" hide />
        <ChartTooltip
          cursor={false}
          content={<ChartTooltipContent hideLabel />}
        />
        <Bar
          dataKey="stock"
          fill="var(--color-stock)"
          radius={4}
          barSize={16}
          label={false}
          animationDuration={0}
        />
      </BarChart>
    </ChartContainer>
  );
}
