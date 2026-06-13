'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { enviosApi } from '@/lib/api';
import { Envio, PageResponse } from '@/types';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Table, TableHeader, TableRow, TableHead, TableBody, TableCell } from '@/components/ui/table';
import { Select, SelectTrigger, SelectValue, SelectContent, SelectItem } from '@/components/ui/select';
import { Button } from '@/components/ui/button';
import { EstadoEnvioBadge } from '@/components/ui/EstadoEnvioBadge';
import { Pagination } from '@/components/ui/pagination';
import Spinner from '@/components/shared/Spinner';
import { estadoEnvioOpciones, isEstadoEnvio, estadoEnvioTexto } from '@/lib/estados';
import { X } from 'lucide-react';

const PAGE_SIZE = 10;
const TODOS = 'TODOS';

export default function EnviosLogisticaPage() {
    const [envios, setEnvios] = useState<Envio[]>([]);
    const [filtroEstado, setFiltroEstado] = useState<string>(TODOS);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);

    useEffect(() => {
        setLoading(true);
        enviosApi.listar({ page, size: PAGE_SIZE })
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

    const filtrados = filtroEstado === TODOS
        ? envios
        : envios.filter(e => e.estadoEnvio === filtroEstado);

    if (loading) return <Spinner />;

    return (
        <div className="space-y-6 pb-8">
            <h1 className="text-2xl font-bold">Gestión de Envíos</h1>

            {/* Filtro */}
            <div className="flex flex-wrap items-center gap-3">
                <label className="text-sm font-medium text-muted-foreground whitespace-nowrap">
                    Filtrar por estado:
                </label>
                <Select value={filtroEstado} onValueChange={setFiltroEstado}>
                    <SelectTrigger className="w-48">
                        <SelectValue placeholder="Todos los estados" />
                    </SelectTrigger>
                    <SelectContent>
                        <SelectItem value={TODOS}>Todos los estados</SelectItem>
                        {estadoEnvioOpciones.map(opt => (
                            <SelectItem key={opt.value} value={opt.value}>
                                {opt.label}
                            </SelectItem>
                        ))}
                    </SelectContent>
                </Select>

                {filtroEstado !== TODOS && (
                    <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => setFiltroEstado(TODOS)}
                        className="text-muted-foreground hover:text-foreground gap-1.5"
                    >
                        <X className="h-3.5 w-3.5" />
                        Limpiar
                    </Button>
                )}
            </div>

            {/* Tabla */}
            <Card>
                <CardHeader>
                    <CardTitle className="text-base">
                        Listado de envíos
                        {filtroEstado !== TODOS && (
                            <span className="ml-2 text-sm font-normal text-muted-foreground">
                                — {filtrados.length} resultado{filtrados.length !== 1 ? 's' : ''}
                            </span>
                        )}
                    </CardTitle>
                </CardHeader>
                <CardContent>
                    <div className="w-full overflow-x-auto">
                        <Table className="min-w-[600px]">
                            <TableHeader>
                                <TableRow>
                                    <TableHead className="w-[180px]">Tracking</TableHead>
                                    <TableHead>Destinatario</TableHead>
                                    <TableHead className="w-[160px]">Estado</TableHead>
                                    <TableHead className="w-[130px]">Fecha creación</TableHead>
                                    <TableHead className="w-[100px]">Acciones</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {filtrados.length === 0 ? (
                                    <TableRow>
                                        <TableCell colSpan={5} className="text-center py-12 text-muted-foreground text-sm">
                                            No hay envíos que mostrar.
                                        </TableCell>
                                    </TableRow>
                                ) : (
                                    filtrados.map((envio) => (
                                        <TableRow key={envio.id}>
                                            <TableCell className="font-medium text-sm">
                                                {envio.numeroTracking}
                                            </TableCell>
                                            <TableCell className="truncate max-w-[200px] text-sm">
                                                {envio.destinatario}
                                            </TableCell>
                                            <TableCell>
                                                <EstadoEnvioBadge estado={envio.estadoEnvio} />
                                            </TableCell>
                                            <TableCell className="text-sm text-muted-foreground whitespace-nowrap">
                                                {new Date(envio.fechaCreacion).toLocaleDateString('es-CL')}
                                            </TableCell>
                                            <TableCell>
                                                <Link href={`/logistica/envios/${envio.id}`}>
                                                    <Button variant="ghost" size="sm">
                                                        Ver detalle
                                                    </Button>
                                                </Link>
                                            </TableCell>
                                        </TableRow>
                                    ))
                                )}
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
                </CardContent>
            </Card>
        </div>
    );
}