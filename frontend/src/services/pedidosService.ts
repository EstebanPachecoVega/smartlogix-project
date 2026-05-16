import { apiGet, apiPost } from './api';
import { CrearPedidoRequest, PedidoResponse } from '@/types';

export const pedidosService = {
  crear: (data: CrearPedidoRequest) => apiPost<PedidoResponse>('/bff/pedidos', data),
  obtener: (id: number) => apiGet<PedidoResponse>(`/bff/pedidos/${id}`),
};