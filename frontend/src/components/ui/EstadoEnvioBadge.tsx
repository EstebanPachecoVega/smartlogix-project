import { Badge } from '@/components/ui/badge';
import { estadoEnvioTexto, estadoEnvioColor, isEstadoEnvio } from '@/lib/estados';

interface Props {
    estado: string;
}

export function EstadoEnvioBadge({ estado }: Props) {
    return (
        <Badge className={isEstadoEnvio(estado) ? estadoEnvioColor[estado] : 'bg-gray-500'}>
            {isEstadoEnvio(estado) ? estadoEnvioTexto[estado] : estado}
        </Badge>
    );
}