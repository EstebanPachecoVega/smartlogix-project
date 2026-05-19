import axios from 'axios';
import { Producto, PedidoRequest, PedidoResponse, Envio } from '@/types';

const BFF_URL = process.env.NEXT_PUBLIC_BFF_URL || 'http://localhost:8084/bff';

const apiClient = axios.create({
  baseURL: BFF_URL,
  headers: {
    'Content-Type': 'application/json',
    Authorization: 'Bearer dev-token', // mock
  },
});

apiClient.interceptors.request.use((config) => {
  config.headers['X-Correlation-Id'] = crypto.randomUUID();
  return config;
});

// =================== PRODUCTOS (directo a inventario por ahora) ===================
export const productosApi = {
  listar: async (): Promise<Producto[]> => {
    const res = await apiClient.get('/productos');
    return res.data;
  },
};

// =================== PEDIDOS (vía BFF) ===================
export const pedidosApi = {
  crear: async (data: PedidoRequest, idempotencyKey: string): Promise<PedidoResponse> => {
    const res = await apiClient.post('/pedidos', data, {
      headers: { 'Idempotency-Key': idempotencyKey },
    });
    return res.data;
  },
  listar: async (): Promise<PedidoResponse[]> => {
    const res = await apiClient.get('/pedidos');
    return res.data;
  },
  obtener: async (id: number): Promise<PedidoResponse> => {
    const res = await apiClient.get(`/pedidos/${id}`);
    return res.data;
  },
};

// =================== ENVÍOS (vía BFF -> Gateway) ===================
export const enviosApi = {
  listar: async (): Promise<Envio[]> => {
    const res = await apiClient.get('/envios');
    return res.data;
  },
  obtener: async (id: number): Promise<Envio> => {
    const res = await apiClient.get(`/envios/${id}`);
    return res.data;
  },
  actualizarEstado: async (envioId: number, nuevoEstado: string): Promise<Envio> => {
    const res = await apiClient.patch(`/envios/${envioId}/estado?nuevoEstado=${nuevoEstado}`);
    return res.data;
  },
  obtenerPorTracking: async (tracking: string): Promise<Envio> => {
    const res = await apiClient.get(`/envios/tracking/${tracking}`);
    return res.data;
  },
  listarProblemas: async (): Promise<Envio[]> => {
    const res = await apiClient.get('/envios/problemas');
    return res.data;
  },
};