'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import { enviosApi } from '@/lib/api';
import { Envio } from '@/types';
import ActualizarEstadoForm from '@/components/logistica/ActualizarEstadoForm';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import Spinner from '@/components/shared/Spinner';

export default function DetalleEnvioPage() {
    const { id } = useParams();
    const [envio, setEnvio] = useState<Envio | null>(null);
    const [loading, setLoading] = useState(true);

    const cargar = async () => {
        try {
            const data = await enviosApi.obtener(Number(id));
            setEnvio(data);
        } catch (error) {
            console.error(error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        cargar();
    }, [id]);

    if (loading) return <Spinner />;
    if (!envio) return <div>Envío no encontrado</div>;

    return (
        <div>
            <h1 className="text-2xl font-bold mb-6">Detalle del envío</h1>
            <Card>
                <CardHeader><CardTitle>Tracking: {envio.numeroTracking}</CardTitle></CardHeader>
                <CardContent className="space-y-2">
                    <p><strong>Pedido ID:</strong> {envio.pedidoId}</p>
                    <p><strong>Destinatario:</strong> {envio.destinatario}</p>
                    <p><strong>Dirección:</strong> {envio.calle} {envio.numero}, {envio.comuna}, {envio.ciudad}</p>
                    <p><strong>Método envío:</strong> {envio.metodoEnvio}</p>
                    <p><strong>Empresa logística:</strong> {envio.empresaLogistica}</p>
                    <p><strong>Fecha estimada:</strong> {envio.fechaEstimadaEntrega}</p>
                    <p><strong>Fecha creación:</strong> {new Date(envio.fechaCreacion).toLocaleString()}</p>
                    <div className="flex items-center gap-4">
                        <strong>Estado actual:</strong> {envio.estadoEnvio}
                        <ActualizarEstadoForm envioId={envio.id} estadoActual={envio.estadoEnvio} onSuccess={cargar} />
                    </div>
                </CardContent>
            </Card>
        </div>
    );
}