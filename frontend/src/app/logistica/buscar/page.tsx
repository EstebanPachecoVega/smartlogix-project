'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';

export default function BuscarEnvioPage() {
    const router = useRouter();
    const [tracking, setTracking] = useState('');

    const handleBuscar = () => {
        if (tracking.trim()) {
            router.push(`/logistica/envios/tracking/${tracking}`);
        }
    };

    return (
        <div>
            <h1 className="text-2xl font-bold mb-6">Buscar envío por tracking</h1>
            <Card>
                <CardHeader>
                    <CardTitle>Ingrese el número de tracking</CardTitle>
                </CardHeader>
                <CardContent>
                    <div className="flex gap-4">
                        <Input
                            placeholder="Ej: TRK-ABCD1234"
                            value={tracking}
                            onChange={(e) => setTracking(e.target.value)}
                            onKeyDown={(e) => e.key === 'Enter' && handleBuscar()}
                        />
                        <Button onClick={handleBuscar}>Buscar</Button>
                    </div>
                </CardContent>
            </Card>
        </div>
    );
}