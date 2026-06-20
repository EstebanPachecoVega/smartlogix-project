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
import { estadoEnvioOpciones, estadoEnvioTexto, isEstadoEnvio } from '@/lib/estados';
import { toast } from 'sonner';
import { Loader2 } from 'lucide-react';

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
            const label = isEstadoEnvio(nuevoEstado) ? estadoEnvioTexto[nuevoEstado] : nuevoEstado;
            toast.success('Estado actualizado', {
                description: `El envío pasó a "${label}".`,
            });
        } catch (error) {
            console.error(error);
            toast.error('Error al actualizar el estado', {
                description: 'Intenta de nuevo.',
            });
        } finally {
            setUpdating(false);
        }
    };

    if (loading) return <Spinner />;
    if (!envio) return <div className="p-6 text-muted-foreground">Envío no encontrado.</div>;

    return (
        <div className="space-y-6 pb-8">
            <Button
                variant="ghost"
                className="mb-2 -ml-2"
                onClick={() => router.push('/logistica/envios')}
            >
                ← Volver a envíos
            </Button>

            <h1 className="text-2xl font-bold">Detalle del Envío</h1>

            <div className="grid md:grid-cols-2 gap-6">
                <Card>
                    <CardHeader>
                        <CardTitle className="text-base">Información del envío</CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-2 text-sm">
                        <p><span className="font-medium">Tracking:</span> {envio.numeroTracking}</p>
                        <p><span className="font-medium">Destinatario:</span> {envio.destinatario}</p>
                        <p><span className="font-medium">Dirección:</span> {envio.calle} {envio.numero}, {envio.comuna}, {envio.ciudad}</p>
                        <p><span className="font-medium">Método de envío:</span> {envio.metodoEnvio}</p>
                        <p><span className="font-medium">Empresa logística:</span> {envio.empresaLogistica}</p>
                        <p><span className="font-medium">Fecha estimada:</span> {envio.fechaEstimadaEntrega}</p>
                        <p><span className="font-medium">Fecha creación:</span> {new Date(envio.fechaCreacion).toLocaleString('es-CL')}</p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle className="text-base">Actualizar estado</CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-4">
                        <div>
                            <p className="text-sm text-muted-foreground mb-1.5">Estado actual:</p>
                            <EstadoEnvioBadge estado={envio.estadoEnvio} />
                        </div>
                        <div>
                            <p className="text-sm text-muted-foreground mb-1.5">Cambiar a:</p>
                            <Select value={nuevoEstado} onValueChange={(v: string | null) => setNuevoEstado(v ?? '')}>
                                <SelectTrigger className="w-52">
                                    <SelectValue>
                                        {isEstadoEnvio(nuevoEstado)
                                            ? estadoEnvioTexto[nuevoEstado]
                                            : nuevoEstado}
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
                            {updating && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                            {updating ? 'Actualizando…' : 'Actualizar estado'}
                        </Button>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}