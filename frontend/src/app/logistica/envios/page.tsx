'use client';

import { useEffect } from 'react';
import { useEnviosStore } from '@/store/enviosStore';
import TablaEnvios from '@/components/logistica/TablaEnvios';
import FiltrosEnvios from '@/components/logistica/FiltrosEnvios';
import Spinner from '@/components/shared/Spinner';

export default function ListadoEnviosPage() {
    const { envios, loading, filtroEstado, cargarEnvios } = useEnviosStore();

    useEffect(() => {
        cargarEnvios();
    }, []);

    const filtrados = filtroEstado ? envios.filter(e => e.estadoEnvio === filtroEstado) : envios;

    if (loading) return <Spinner />;

    return (
        <div>
            <h1 className="text-2xl font-bold mb-6">Gestión de Envíos</h1>
            <FiltrosEnvios />
            <TablaEnvios envios={filtrados} />
        </div>
    );
}