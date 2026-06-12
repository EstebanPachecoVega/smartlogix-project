'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { pedidosApi } from '@/lib/api';
import { PedidoResponse } from '@/types';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Table, TableHeader, TableRow, TableHead, TableBody, TableCell } from '@/components/ui/table';
import Spinner from '@/components/shared/Spinner';
import { EstadoPedidoBadge } from '@/components/ui/EstadoPedidoBadge';

const estadoColor: Record<string, string> = {
  PENDIENTE: 'bg-yellow-500',
  APROBADO: 'bg-blue-500',
  RECHAZADO: 'bg-red-500',
  EN_CAMINO: 'bg-purple-500',
  ENTREGADO: 'bg-green-500',
};

export default function PedidosLogisticaPage() {
  const [pedidos, setPedidos] = useState<PedidoResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    pedidosApi.listar()
      .then(setPedidos)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Spinner />;

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Gestión de Pedidos</h1>
      <Card>
        <CardHeader>
          <CardTitle>Todos los pedidos</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Nº Orden</TableHead>
                <TableHead>Estado</TableHead>
                <TableHead>Total</TableHead>
                <TableHead>Acciones</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {pedidos.map((pedido) => (
                <TableRow key={pedido.id}>
                  <TableCell>{pedido.numeroOrden}</TableCell>
                  <TableCell><EstadoPedidoBadge estado={pedido.estado}/></TableCell>
                  <TableCell>${pedido.totalCompra.toLocaleString()}</TableCell>
                  <TableCell>
                    <Link href={`/logistica/pedidos/${pedido.id}`}>
                      <span className="text-blue-600 hover:underline cursor-pointer">Ver detalle</span>
                    </Link>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
}