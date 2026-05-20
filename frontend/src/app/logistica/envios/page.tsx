'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { enviosApi } from '@/lib/api';
import { Envio } from '@/types';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Table, TableHeader, TableRow, TableHead, TableBody, TableCell } from '@/components/ui/table';
import { EstadoEnvioBadge } from '@/components/ui/EstadoEnvioBadge';
import Spinner from '@/components/shared/Spinner';
import { estadoEnvioTexto } from '@/lib/estados';

export default function EnviosLogisticaPage() {
    const [envios, setEnvios] = useState<Envio[]>([]);
    const [filtrados, setFiltrados] = useState<Envio[]>([]);
    const [filtroEstado, setFiltroEstado] = useState('');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        enviosApi.listar()
            .then(data => {
                setEnvios(data);
                setFiltrados(data);
            })
            .catch(console.error)
            .finally(() => setLoading(false));
    }, []);

    useEffect(() => {
        if (filtroEstado) {
            setFiltrados(envios.filter(e => e.estadoEnvio === filtroEstado));
        } else {
            setFiltrados(envios);
        }
    }, [filtroEstado, envios]);

    if (loading) return <Spinner />;

    const estadosUnicos = [...new Set(envios.map(e => e.estadoEnvio))];

    return (
        <div>
            <h1 className="text-2xl font-bold mb-6">Gestión de Envíos</h1>
            <div className="mb-4 flex gap-4">
                <select value={filtroEstado} onChange={(e) => setFiltroEstado(e.target.value)}>
                    <option value="">Todos los estados</option>
                    {estadosUnicos.map(est => (
                        <option key={est} value={est}>
                            {estadoEnvioTexto[est] || est}
                        </option>
                    ))}
                </select>

            </div>
            <Card>
                <CardHeader>
                    <CardTitle>Listado de envíos</CardTitle>
                </CardHeader>
                <CardContent>
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
                            {filtrados.map((envio) => (
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
                </CardContent>
            </Card>
        </div>
    );
}