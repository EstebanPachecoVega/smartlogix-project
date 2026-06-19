// ==================== PEDIDOS ====================

export type EstadoPedido =
    | 'PENDIENTE'
    | 'APROBADO'
    | 'RECHAZADO'
    | 'EN_CAMINO'
    | 'ENTREGADO';

export const estadoPedidoTexto: Record<EstadoPedido, string> = {
    PENDIENTE: 'Pendiente',
    APROBADO: 'Aprobado',
    RECHAZADO: 'Rechazado',
    EN_CAMINO: 'En camino',
    ENTREGADO: 'Entregado',
};

export const estadoPedidoColor: Record<EstadoPedido, string> = {
    PENDIENTE: 'bg-yellow-500',
    APROBADO: 'bg-blue-500',
    RECHAZADO: 'bg-red-500',
    EN_CAMINO: 'bg-purple-500',
    ENTREGADO: 'bg-green-500',
};

// ==================== ENVÍOS ====================

export type EstadoEnvio =
    | 'PENDIENTE'
    | 'PREPARANDO'
    | 'ENVIADO'
    | 'EN_TRANSITO'
    | 'EN_REPARTO'
    | 'ENTREGADO'
    | 'INTENTO_FALLIDO'
    | 'RETRASADO'
    | 'DEVUELTO'
    | 'CANCELADO';

export const estadoEnvioTexto: Record<EstadoEnvio, string> = {
    PENDIENTE: 'Pendiente',
    PREPARANDO: 'Preparando',
    ENVIADO: 'Enviado',
    EN_TRANSITO: 'En tránsito',
    EN_REPARTO: 'En reparto',
    ENTREGADO: 'Entregado',
    INTENTO_FALLIDO: 'Intento fallido',
    RETRASADO: 'Retrasado',
    DEVUELTO: 'Devuelto',
    CANCELADO: 'Cancelado',
};

export const estadoEnvioColor: Record<EstadoEnvio, string> = {
    PENDIENTE: 'bg-gray-500',
    PREPARANDO: 'bg-blue-500',
    ENVIADO: 'bg-purple-500',
    EN_TRANSITO: 'bg-indigo-500',
    EN_REPARTO: 'bg-yellow-500',
    ENTREGADO: 'bg-green-500',
    INTENTO_FALLIDO: 'bg-red-500',
    RETRASADO: 'bg-orange-500',
    DEVUELTO: 'bg-rose-500',
    CANCELADO: 'bg-zinc-900',
};

export interface EstadoEnvioOption {
    value: EstadoEnvio;
    label: string;
}

export const estadoEnvioOpciones: EstadoEnvioOption[] = (
    Object.entries(estadoEnvioTexto) as [EstadoEnvio, string][]
).map(([value, label]) => ({ value, label }));

// ==================== HEX COLORS FOR CHARTS ====================
// Mirrors estadoPedidoColor but as hex values for recharts fill
export const estadoPedidoHexColor: Record<EstadoPedido, string> = {
    PENDIENTE: '#eab308',
    APROBADO: '#3b82f6',
    RECHAZADO: '#ef4444',
    EN_CAMINO: '#a855f7',
    ENTREGADO: '#22c55e',
};

// Mirrors estadoEnvioColor but as hex values for recharts fill
export const estadoEnvioHexColor: Record<EstadoEnvio, string> = {
    PENDIENTE: '#6b7280',
    PREPARANDO: '#3b82f6',
    ENVIADO: '#a855f7',
    EN_TRANSITO: '#6366f1',
    EN_REPARTO: '#eab308',
    ENTREGADO: '#22c55e',
    INTENTO_FALLIDO: '#ef4444',
    RETRASADO: '#f97316',
    DEVUELTO: '#f43f5e',
    CANCELADO: '#18181b',
};

// Helper: castea un string a EstadoEnvio de forma segura
export function isEstadoEnvio(s: string): s is EstadoEnvio {
    return s in estadoEnvioTexto;
}

export function isEstadoPedido(s: string): s is EstadoPedido {
    return s in estadoPedidoTexto;
}