'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import { enviosApi } from '@/lib/api';
import { Envio } from '@/types';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import Link from 'next/link';
import Spinner from '@/components/shared/Spinner';
import { EstadoPedidoBadge } from '@/components/ui/EstadoPedidoBadge';

const estadoColor: Record<string, string> = {
    PENDIENTE: 'bg-muted-foreground',
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

export default function BuscarPorTrackingPage() {
    const { tracking } = useParams();
    const [envio, setEnvio] = useState<Envio | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        enviosApi.obtenerPorTracking(decodeURIComponent(tracking as string))
            .then(setEnvio)
            .catch(() => setError('No se encontró ningún envío con ese tracking'))
            .finally(() => setLoading(false));
    }, [tracking]);

    if (loading) return <Spinner />;
    if (error) return <div className="text-red-500">{error}</div>;
    if (!envio) return <div>Envío no encontrado</div>;

    return (
        <div>
            <h1 className="text-2xl font-bold mb-6">Resultado de búsqueda</h1>
            <Card>
                <CardHeader>
                    <CardTitle>Envío {envio.numeroTracking}</CardTitle>
                </CardHeader>
                <CardContent className="space-y-2">
                    <p><strong>Tracking:</strong> {envio.numeroTracking}</p>
                    <p><strong>Destinatario:</strong> {envio.destinatario}</p>
                    <p><strong>Estado:</strong><EstadoPedidoBadge estado={envio.estadoEnvio}/></p>
                    <Link href={`/logistica/envios/${envio.id}`}>
                        <span className="text-primary hover:underline cursor-pointer">Ver detalle completo</span>
                    </Link>
                </CardContent>
            </Card>
        </div>
    );
}