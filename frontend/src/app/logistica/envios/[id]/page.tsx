'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { enviosApi } from '@/lib/api';
import { Envio } from '@/types';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { EstadoEnvioBadge } from '@/components/ui/EstadoEnvioBadge';
import { Select, SelectTrigger, SelectValue, SelectContent, SelectItem } from '@/components/ui/select';
import Spinner from '@/components/shared/Spinner';
import { estadoEnvioOpciones, estadoEnvioTexto } from '@/lib/estados';

export default function DetalleEnvioPage() {
    const { id } = useParams();
    const router = useRouter();
    const [envio, setEnvio] = useState<Envio | null>(null);
    const [loading, setLoading] = useState(true);
    const [updating, setUpdating] = useState(false);
    const [nuevoEstado, setNuevoEstado] = useState('');

    const cargar = async () => {
        try {
            const data = await enviosApi.obtener(Number(id));
            setEnvio(data);
            setNuevoEstado(data.estadoEnvio);
        } catch (error) {
            console.error(error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        cargar();
    }, [id]);

    const handleActualizarEstado = async () => {
        if (!envio || nuevoEstado === envio.estadoEnvio) return;
        setUpdating(true);
        try {
            await enviosApi.actualizarEstado(envio.id, nuevoEstado);
            await cargar();
        } catch (error) {
            console.error(error);
            alert('Error al actualizar el estado');
        } finally {
            setUpdating(false);
        }
    };

    if (loading) return <Spinner />;
    if (!envio) return <div>Envío no encontrado</div>;

    return (
        <div>
            <Button
                variant="ghost"
                className="mb-4"
                onClick={() => router.push('/logistica/envios')}
            >
                ← Volver a envíos
            </Button>

            <h1 className="text-2xl font-bold mb-6">Detalle del Envío</h1>
            <div className="grid md:grid-cols-2 gap-6">
                <Card>
                    <CardHeader><CardTitle>Información del envío</CardTitle></CardHeader>
                    <CardContent className="space-y-2">
                        <p><strong>Número de tracking:</strong> {envio.numeroTracking}</p>
                        <p><strong>Pedido ID:</strong> {envio.pedidoId}</p>
                        <p><strong>Destinatario:</strong> {envio.destinatario}</p>
                        <p><strong>Dirección:</strong> {envio.calle} {envio.numero}, {envio.comuna}, {envio.ciudad}</p>
                        <p><strong>Método de envío:</strong> {envio.metodoEnvio}</p>
                        <p><strong>Empresa logística:</strong> {envio.empresaLogistica}</p>
                        <p><strong>Fecha estimada:</strong> {envio.fechaEstimadaEntrega}</p>
                        <p><strong>Fecha creación:</strong> {new Date(envio.fechaCreacion).toLocaleString()}</p>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader><CardTitle>Actualizar estado</CardTitle></CardHeader>
                    <CardContent className="space-y-4">
                        <div>
                            <p className="text-sm text-gray-500 mb-1">Estado actual:</p>
                            <EstadoEnvioBadge estado={envio.estadoEnvio} />
                        </div>
                        <div>
                            <p className="text-sm text-gray-500 mb-1">Cambiar a:</p>
                            <Select value={nuevoEstado} onValueChange={setNuevoEstado}>
                                <SelectTrigger className="w-48">
                                    <SelectValue>
                                        {estadoEnvioTexto[nuevoEstado] || nuevoEstado}
                                    </SelectValue>
                                </SelectTrigger>
                                <SelectContent>
                                    {estadoEnvioOpciones.map((opt) => (
                                        <SelectItem key={opt.value} value={opt.value}>
                                            {opt.label}
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                        </div>
                        <Button
                            onClick={handleActualizarEstado}
                            disabled={updating || nuevoEstado === envio.estadoEnvio}
                        >
                            {updating ? 'Actualizando...' : 'Actualizar estado'}
                        </Button>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}