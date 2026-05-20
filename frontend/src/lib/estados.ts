// ==================== PEDIDOS ====================
export const estadoPedidoTexto: Record<string, string> = {
    PENDIENTE: 'Pendiente',
    APROBADO: 'Aprobado',
    RECHAZADO: 'Rechazado',
    EN_CAMINO: 'En camino',
    ENTREGADO: 'Entregado',
};

export const estadoPedidoColor: Record<string, string> = {
    PENDIENTE: 'bg-yellow-500',
    APROBADO: 'bg-blue-500',
    RECHAZADO: 'bg-red-500',
    EN_CAMINO: 'bg-purple-500',
    ENTREGADO: 'bg-green-500',
};

// ==================== ENVÍOS ====================
export const estadoEnvioTexto: Record<string, string> = {
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

export const estadoEnvioColor: Record<string, string> = {
    PENDIENTE: 'bg-gray-500',
    PREPARANDO: 'bg-blue-500',
    ENVIADO: 'bg-purple-500',
    EN_TRANSITO: 'bg-indigo-500',
    EN_REPARTO: 'bg-yellow-500',
    ENTREGADO: 'bg-green-500',
    INTENTO_FALLIDO: 'bg-red-500',
    RETRASADO: 'bg-orange-500',
    DEVUELTO: 'bg-rose-500',
    CANCELADO: 'bg-black',
};

// Opciones para Select (envíos)
export interface EstadoEnvioOption {
    value: string;
    label: string;
}

export const estadoEnvioOpciones: EstadoEnvioOption[] = Object.entries(estadoEnvioTexto).map(
    ([value, label]) => ({ value, label })
);