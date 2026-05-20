'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { enviosApi } from '@/lib/api';
import { Envio } from '@/types';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Table, TableHeader, TableRow, TableHead, TableBody, TableCell } from '@/components/ui/table';
import { EstadoEnvioBadge } from '@/components/ui/EstadoEnvioBadge';
import Spinner from '@/components/shared/Spinner';

export default function ProblemasPage() {
    const [envios, setEnvios] = useState<Envio[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        enviosApi.listar()
            .then(data => {
                const problemas = data.filter(e =>
                    e.estadoEnvio === 'INTENTO_FALLIDO' ||
                    e.estadoEnvio === 'RETRASADO' ||
                    e.estadoEnvio === 'DEVUELTO'
                );
                setEnvios(problemas);
            })
            .catch(console.error)
            .finally(() => setLoading(false));
    }, []);

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
                        <p className="text-gray-500">No hay envíos con problemas</p>
                    ) : (
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead>Tracking</TableHead>
                                    <TableHead>Pedido ID</TableHead>
                                    <TableHead>Destinatario</TableHead>
                                    <TableHead>Estado</TableHead>
                                    <TableHead>Fecha creación</TableHead>
                                    <TableHead>Acciones</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {envios.map((envio) => (
                                    <TableRow key={envio.id}>
                                        <TableCell>{envio.numeroTracking}</TableCell>
                                        <TableCell>{envio.pedidoId}</TableCell>
                                        <TableCell>{envio.destinatario}</TableCell>
                                        <TableCell>
                                            <EstadoEnvioBadge estado={envio.estadoEnvio} />
                                        </TableCell>
                                        <TableCell>{new Date(envio.fechaCreacion).toLocaleDateString()}</TableCell>
                                        <TableCell>
                                            <Link href={`/logistica/envios/${envio.id}`}>
                                                <span className="text-blue-600 hover:underline cursor-pointer">Ver detalle</span>
                                            </Link>
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    )}
                </CardContent>
            </Card>
        </div>
    );
}