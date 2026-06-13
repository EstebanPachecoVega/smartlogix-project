'use client';

import { Envio } from '@/types';
import Link from 'next/link';
import {
  Table,
  TableHeader,
  TableRow,
  TableHead,
  TableBody,
  TableCell,
} from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { estadoEnvioTexto, estadoEnvioColor, isEstadoEnvio } from '@/lib/estados';

export default function TablaEnvios({ envios }: { envios: Envio[] }) {
  if (envios.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-16 text-muted-foreground">
        <p className="text-sm">No hay envíos que mostrar.</p>
      </div>
    );
  }

  return (
    <div className="w-full overflow-x-auto rounded-lg border">
      <Table className="min-w-[600px]">
        <TableHeader>
          <TableRow>
            <TableHead className="w-[180px]">Tracking</TableHead>
            <TableHead>Destinatario</TableHead>
            <TableHead className="w-[160px]">Estado</TableHead>
            <TableHead className="w-[130px]">Fecha creación</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {envios.map((envio) => {
            // El backend devuelve string — validamos antes de indexar el Record<EstadoEnvio, string>
            const estadoKey = isEstadoEnvio(envio.estadoEnvio) ? envio.estadoEnvio : null;
            const colorClass = estadoKey ? estadoEnvioColor[estadoKey] : 'bg-gray-400';
            const texto = estadoKey ? estadoEnvioTexto[estadoKey] : envio.estadoEnvio;

            return (
              <TableRow key={envio.id}>
                <TableCell>
                  <Link
                    href={`/logistica/envios/${envio.id}`}
                    className="text-primary underline underline-offset-2 hover:text-primary/80 font-medium text-sm"
                  >
                    {envio.numeroTracking}
                  </Link>
                </TableCell>
                <TableCell className="max-w-[220px] truncate text-sm">
                  {envio.destinatario}
                </TableCell>
                <TableCell>
                  <Badge className={`${colorClass} text-white text-xs`}>
                    {texto}
                  </Badge>
                </TableCell>
                <TableCell className="text-sm text-muted-foreground whitespace-nowrap">
                  {new Date(envio.fechaCreacion).toLocaleDateString('es-CL', {
                    day: '2-digit',
                    month: '2-digit',
                    year: 'numeric',
                  })}
                </TableCell>
              </TableRow>
            );
          })}
        </TableBody>
      </Table>
    </div>
  );
}