import { Badge } from '@/components/ui/badge';
import { estadoEnvioTexto, estadoEnvioColor } from '@/lib/estados';

interface Props {
    estado: string;
}

export function EstadoEnvioBadge({ estado }: Props) {
    return (
        <Badge className={estadoEnvioColor[estado] || 'bg-gray-500'}>
            {estadoEnvioTexto[estado] || estado}
        </Badge>
    );
}