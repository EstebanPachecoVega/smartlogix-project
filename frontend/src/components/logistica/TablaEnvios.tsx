'use client';

import { Envio } from '@/types';
import Link from 'next/link';
import { Table, TableHeader, TableRow, TableHead, TableBody, TableCell } from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';

const estadoColor: Record<string, string> = {
  PENDIENTE: 'bg-gray-500',
  PREPARANDO: 'bg-blue-500',
  ENVIADO: 'bg-purple-500',
  EN_TRANSITO: 'bg-indigo-500',
  EN_REPARTO: 'bg-yellow-500',
  ENTREGADO: 'bg-green-500',
  INTENTO_FALLIDO: 'bg-red-500',
  RETRASADO: 'bg-orange-500',
  DEVUELTO: 'bg-rose-500',
  CANCELADO: 'bg-black',
};

export default function TablaEnvios({ envios }: { envios: Envio[] }) {
  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Tracking</TableHead>
          <TableHead>Destinatario</TableHead>
          <TableHead>Estado</TableHead>
          <TableHead>Fecha creación</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {envios.map((envio) => (
          <TableRow key={envio.id}>
            <TableCell>
              <Link href={`/logistica/envios/${envio.id}`} className="text-blue-600 underline">
                {envio.numeroTracking}
              </Link>
            </TableCell>
            <TableCell>{envio.destinatario}</TableCell>
            <TableCell>
              <Badge className={estadoColor[envio.estadoEnvio] || 'bg-gray-500'}>
                {envio.estadoEnvio}
              </Badge>
            </TableCell>
            <TableCell>{new Date(envio.fechaCreacion).toLocaleDateString()}</TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
}