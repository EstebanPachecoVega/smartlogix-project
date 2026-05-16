import api from './api';
import type { CrearPedidoRequest, PedidoResponse } from '@/types';

export const pedidosService = {
  // Crear un nuevo pedido
  async crearPedido(data: CrearPedidoRequest): Promise<PedidoResponse> {
    const response = await api.post<PedidoResponse>('/bff/pedidos', data);
    return response.data;
  },

  // Listar todos los pedidos
  async listarPedidos(): Promise<PedidoResponse[]> {
    const response = await api.get<PedidoResponse[]>('/bff/pedidos');
    return response.data;
  },

  // Obtener un pedido por su ID
  async obtenerPedido(id: number): Promise<PedidoResponse> {
    const response = await api.get<PedidoResponse>(`/bff/pedidos/${id}`);
    return response.data;
  },
};