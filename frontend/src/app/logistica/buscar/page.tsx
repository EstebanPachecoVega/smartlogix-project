'use client';

import { useState } from 'react';
import { enviosApi } from '@/lib/api';
import { Envio } from '@/types';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import TablaEnvios from '@/components/logistica/TablaEnvios';

export default function BuscarPage() {
    const [tracking, setTracking] = useState('');
    const [envio, setEnvio] = useState<Envio | null>(null);
    const [error, setError] = useState('');

    const buscar = async () => {
        if (!tracking.trim()) return;
        try {
            const data = await enviosApi.obtenerPorTracking(tracking);
            setEnvio(data);
            setError('');
        } catch (err) {
            setError('No se encontró envío con ese tracking');
            setEnvio(null);
        }
    };

    return (
        <div>
            <h1 className="text-2xl font-bold mb-6">Buscar envío por tracking</h1>
            <div className="flex gap-4 mb-6">
                <Input placeholder="Número de tracking" value={tracking} onChange={e => setTracking(e.target.value)} />
                <Button onClick={buscar}>Buscar</Button>
            </div>
            {error && <p className="text-red-500">{error}</p>}
            {envio && <TablaEnvios envios={[envio]} />}
        </div>
    );
}