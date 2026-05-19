'use client';

import { useEffect, useState } from 'react';
import { enviosApi } from '@/lib/api';
import { Envio } from '@/types';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';

export default function DashboardLogistica() {
    const [total, setTotal] = useState(0);
    const [entregados, setEntregados] = useState(0);
    const [problemas, setProblemas] = useState(0);

    useEffect(() => {
        Promise.all([enviosApi.listar(), enviosApi.listarProblemas()]).then(([todos, problematicos]) => {
            setTotal(todos.length);
            setEntregados(todos.filter(e => e.estadoEnvio === 'ENTREGADO').length);
            setProblemas(problematicos.length);
        });
    }, []);

    return (
        <div>
            <h1 className="text-2xl font-bold mb-6">Dashboard Logística</h1>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <Card><CardHeader><CardTitle>Total envíos</CardTitle></CardHeader><CardContent className="text-3xl">{total}</CardContent></Card>
                <Card><CardHeader><CardTitle>Entregados</CardTitle></CardHeader><CardContent className="text-3xl">{entregados}</CardContent></Card>
                <Card><CardHeader><CardTitle>Con problemas</CardTitle></CardHeader><CardContent className="text-3xl">{problemas}</CardContent></Card>
            </div>
        </div>
    );
}