export interface CrearPedidoRequest {
  productoId: number;
  cantidad: number;
}

export interface PedidoResponse {
  id: number;
  estado: 'PENDIENTE' | 'APROBADO' | 'RECHAZADO';
}

export interface EnvioResponse {
  id: number;
  pedidoId: number;
  estado: 'CREADO' | 'EN_CURSO' | 'ENTREGADO';
}

export interface ErrorResponse {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance: string;
  errors?: Record<string, string>;
}