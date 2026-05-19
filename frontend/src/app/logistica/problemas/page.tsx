'use client';

import { useEffect, useState } from 'react';
import { enviosApi } from '@/lib/api';
import { Envio } from '@/types';
import TablaEnvios from '@/components/logistica/TablaEnvios';
import Spinner from '@/components/shared/Spinner';

export default function ProblemasPage() {
    const [envios, setEnvios] = useState<Envio[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        enviosApi.listarProblemas().then(setEnvios).catch(console.error).finally(() => setLoading(false));
    }, []);

    if (loading) return <Spinner />;

    return (
        <div>
            <h1 className="text-2xl font-bold mb-6">Envíos con problemas</h1>
            <TablaEnvios envios={envios} />
        </div>
    );
}