'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { pedidosApi } from '@/lib/api';
import { PedidoResponse, PageResponse } from '@/types';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Table, TableHeader, TableRow, TableHead, TableBody, TableCell } from '@/components/ui/table';
import { Pagination } from '@/components/ui/pagination';
import Spinner from '@/components/shared/Spinner';
import { EstadoPedidoBadge } from '@/components/ui/EstadoPedidoBadge';

const PAGE_SIZE = 10;

export default function PedidosLogisticaPage() {
  const [pedidos, setPedidos] = useState<PedidoResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  useEffect(() => {
    setLoading(true);
    pedidosApi.listar({ page, size: PAGE_SIZE })
      .then(data => {
        if (Array.isArray(data)) {
          setPedidos(data);
          setTotalPages(1);
          setTotalElements(data.length);
        } else {
          const pageData = data as PageResponse<PedidoResponse>;
          setPedidos(pageData.content);
          setTotalPages(pageData.totalPages);
          setTotalElements(pageData.totalElements);
        }
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [page]);

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
                  <TableCell className="truncate max-w-[200px]">{pedido.numeroOrden}</TableCell>
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
          <Pagination
            page={page}
            totalPages={totalPages}
            totalElements={totalElements}
            pageSize={PAGE_SIZE}
            onPageChange={setPage}
          />
        </CardContent>
      </Card>
    </div>
  );
}
