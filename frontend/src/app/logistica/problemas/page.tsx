'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { enviosApi } from '@/lib/api';
import { Envio, PageResponse } from '@/types';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Table, TableHeader, TableRow, TableHead, TableBody, TableCell } from '@/components/ui/table';
import { Pagination } from '@/components/ui/pagination';
import { EstadoEnvioBadge } from '@/components/ui/EstadoEnvioBadge';
import Spinner from '@/components/shared/Spinner';

const PAGE_SIZE = 10;
const ESTADOS_PROBLEMA = 'INTENTO_FALLIDO,RETRASADO,DEVUELTO';

export default function ProblemasPage() {
    const [envios, setEnvios] = useState<Envio[]>([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);

    useEffect(() => {
        setLoading(true);
        enviosApi.listar({ page, size: PAGE_SIZE, estadoEnvio: ESTADOS_PROBLEMA })
            .then(data => {
                if (Array.isArray(data)) {
                    setEnvios(data);
                    setTotalPages(1);
                    setTotalElements(data.length);
                } else {
                    const pageData = data as PageResponse<Envio>;
                    setEnvios(pageData.content);
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
            <h1 className="text-2xl font-bold mb-6">Envíos con problemas</h1>
            <Card>
                <CardHeader>
                    <CardTitle>Listado de envíos problemáticos</CardTitle>
                </CardHeader>
                <CardContent>
                    {envios.length === 0 ? (
                        <p className="text-muted-foreground">No hay envíos con problemas</p>
                    ) : (
                        <>
                            <div className="w-full overflow-x-auto">
                                <Table className="min-w-[600px]">
                                    <TableHeader>
                                        <TableRow>
                                            <TableHead>Tracking</TableHead>
                                            <TableHead>Destinatario</TableHead>
                                            <TableHead>Estado</TableHead>
                                            <TableHead>Fecha creación</TableHead>
                                            <TableHead>Acciones</TableHead>
                                        </TableRow>
                                    </TableHeader>
                                    <TableBody>
                                        {envios.map((envio) => (
                                            <TableRow key={envio.id}>
                                                <TableCell className="font-medium text-sm">{envio.numeroTracking}</TableCell>
                                                <TableCell className="truncate max-w-[200px] text-sm">{envio.destinatario}</TableCell>
                                                <TableCell><EstadoEnvioBadge estado={envio.estadoEnvio} /></TableCell>
                                                <TableCell className="text-sm text-muted-foreground">{new Date(envio.fechaCreacion).toLocaleDateString()}</TableCell>
                                                <TableCell>
                                                    <Link href={`/logistica/envios/${envio.id}`}>
                                                        <span className="text-primary hover:underline cursor-pointer">Ver detalle</span>
                                                    </Link>
                                                </TableCell>
                                            </TableRow>
                                        ))}
                                    </TableBody>
                                </Table>
                            </div>
                            <Pagination
                                page={page}
                                totalPages={totalPages}
                                totalElements={totalElements}
                                pageSize={PAGE_SIZE}
                                onPageChange={setPage}
                            />
                        </>
                    )}
                </CardContent>
            </Card>
        </div>
    );
}
