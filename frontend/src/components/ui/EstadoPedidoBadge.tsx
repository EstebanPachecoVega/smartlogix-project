import { Badge } from '@/components/ui/badge';
import { estadoPedidoTexto, estadoPedidoColor, isEstadoPedido } from '@/lib/estados';

interface Props {
    estado: string;
}

export function EstadoPedidoBadge({ estado }: Props) {
    return (
        <Badge className={isEstadoPedido(estado) ? estadoPedidoColor[estado] : 'bg-gray-500'}>
            {isEstadoPedido(estado) ? estadoPedidoTexto[estado] : estado}
        </Badge>
    );
}