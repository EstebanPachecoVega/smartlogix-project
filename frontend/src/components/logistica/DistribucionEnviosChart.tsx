'use client';

import { Envio } from '@/types';
import { Pie, PieChart } from 'recharts';
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  ChartLegend,
  ChartLegendContent,
} from '@/components/ui/chart';
import { useMemo } from 'react';
import {
  estadoEnvioHexColor,
  estadoEnvioTexto,
  isEstadoEnvio,
} from '@/lib/estados';

export default function DistribucionEnviosChart({ envios }: { envios: Envio[] }) {
  const data = useMemo(() => {
    const grouped = envios.reduce<Record<string, number>>((acc, e) => {
      const estado = isEstadoEnvio(e.estadoEnvio) ? e.estadoEnvio : 'DESCONOCIDO';
      acc[estado] = (acc[estado] || 0) + 1;
      return acc;
    }, {});

    return Object.entries(grouped)
      .map(([estado, cantidad]) => ({
        estado,
        cantidad,
        fill: estadoEnvioHexColor[estado as keyof typeof estadoEnvioHexColor] || '#9ca3af',
      }))
      .sort((a, b) => b.cantidad - a.cantidad);
  }, [envios]);

  const chartConfig = useMemo(() => {
    const config: Record<string, { label: string; color: string }> = {};
    for (const { estado, fill } of data) {
      const label = isEstadoEnvio(estado) ? estadoEnvioTexto[estado] : estado;
      config[estado] = { label, color: fill };
    }
    return config;
  }, [data]);

  if (data.length === 0) {
    return <p className="text-sm text-muted-foreground py-8 text-center">No hay envíos registrados.</p>;
  }

  return (
    <ChartContainer config={chartConfig} className="min-h-[220px]">
      <PieChart>
        <ChartTooltip
          cursor={false}
          content={<ChartTooltipContent hideLabel />}
        />
        <Pie
          data={data}
          dataKey="cantidad"
          nameKey="estado"
          innerRadius={55}
          strokeWidth={2}
        />
        <ChartLegend
          content={<ChartLegendContent nameKey="estado" />}
        />
      </PieChart>
    </ChartContainer>
  );
}
