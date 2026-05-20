import { Badge } from '@/components/ui/badge';
import { estadoPedidoTexto, estadoPedidoColor } from '@/lib/estados';

interface Props {
    estado: string;
}

export function EstadoPedidoBadge({ estado }: Props) {
    return (
        <Badge className={estadoPedidoColor[estado] || 'bg-gray-500'}>
            {estadoPedidoTexto[estado] || estado}
        </Badge>
    );
}