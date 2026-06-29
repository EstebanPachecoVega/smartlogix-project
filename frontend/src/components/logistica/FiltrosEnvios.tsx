'use client';

import { useEnviosStore } from '@/store/enviosStore';
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '@/components/ui/select';
import { Button } from '@/components/ui/button';
import { X } from 'lucide-react';
import { estadoEnvioOpciones } from '@/lib/estados';

const TODOS = 'TODOS';

export default function FiltrosEnvios() {
    const { filtroEstado, setFiltroEstado } = useEnviosStore();

    return (
        <div className="flex flex-wrap items-center gap-3 mb-6">
            <label className="text-sm font-medium text-muted-foreground whitespace-nowrap">
                Filtrar por estado:
            </label>

            <Select
                value={filtroEstado ?? TODOS}
                onValueChange={(value: string | null) => setFiltroEstado(value === TODOS ? null : value)}
            >
                <SelectTrigger className="w-48">
                    <SelectValue placeholder="Todos los estados" />
                </SelectTrigger>
                <SelectContent>
                    <SelectItem value={TODOS}>Todos los estados</SelectItem>
                    {estadoEnvioOpciones.map((e) => (
                        <SelectItem key={e.value} value={e.value}>
                            {e.label}
                        </SelectItem>
                    ))}
                </SelectContent>
            </Select>

            {filtroEstado !== null && (
                <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => setFiltroEstado(null)}
                    className="text-muted-foreground hover:text-foreground gap-1.5"
                >
                    <X className="h-3.5 w-3.5" />
                    Limpiar
                </Button>
            )}
        </div>
    );
}