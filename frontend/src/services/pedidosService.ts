import api from './api';
import type { CrearPedidoRequest, PedidoResponse } from '@/types';

export const pedidosService = {
  async crearPedido(data: CrearPedidoRequest): Promise<PedidoResponse> {
    const response = await api.post<PedidoResponse>('/bff/pedidos', data);
    return response.data;
  },

  async obtenerPedido(id: number): Promise<PedidoResponse> {
    const response = await api.get<PedidoResponse>(`/bff/pedidos/${id}`);
    return response.data;
  },
};