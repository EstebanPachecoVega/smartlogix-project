'use client';

import * as React from 'react';
import { Envio } from '@/types';
import { Pie, PieChart } from 'recharts';
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
import { useMemo } from 'react';
import {
  estadoEnvioHexColor,
  estadoEnvioTexto,
  isEstadoEnvio,
} from '@/lib/estados';
import { filterByDate, RANGES } from '@/lib/filtro';

export default function DistribucionEnviosChart({ envios }: { envios: Envio[] }) {
  const [dias, setDias] = React.useState('all');

  const data = useMemo(() => {
    const filtrados = filterByDate(envios, 'fechaCreacion', dias);
    const grouped = filtrados.reduce<Record<string, number>>((acc, e) => {
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
  }, [envios, dias]);

  const chartConfig = useMemo(() => {
    const config: Record<string, { label: string; color: string }> = {};
    for (const { estado, fill } of data) {
      const label = isEstadoEnvio(estado) ? estadoEnvioTexto[estado] : estado;
      config[estado] = { label, color: fill };
    }
    return config;
  }, [data]);

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between py-4">
        <CardTitle className="text-base">Envíos por estado</CardTitle>
        <Select value={dias} onValueChange={(v: string | null) => setDias(v ?? '')}>
          <SelectTrigger className="w-[150px] h-8 text-xs" aria-label="Seleccionar rango">
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
        {data.length === 0 ? (
          <p className="text-sm text-muted-foreground py-8 text-center">No hay envíos registrados.</p>
        ) : (
          <ChartContainer
            config={chartConfig}
            className="aspect-auto h-[250px] w-full"
          >
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
                animationDuration={0}
              />
              <ChartLegend
                content={<ChartLegendContent nameKey="estado" />}
              />
            </PieChart>
          </ChartContainer>
        )}
      </CardContent>
    </Card>
  );
}
