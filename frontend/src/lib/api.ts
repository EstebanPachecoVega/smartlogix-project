import axios from 'axios';
import { getSession } from 'next-auth/react';
import { Producto, PedidoRequest, PedidoResponse, Envio, Categoria } from '@/types';

const BFF_URL = process.env.NEXT_PUBLIC_BFF_URL || 'http://localhost:8084/bff';

const apiClient = axios.create({
  baseURL: BFF_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 💡 Guardaremos la promesa de la sesión aquí para evitar llamadas duplicadas en paralelo
let activeSessionPromise: Promise<any> | null = null;

apiClient.interceptors.request.use(async (config) => {

  // Si no hay una petición de sesión activa en este instante, la creamos
  if (!activeSessionPromise) {
    activeSessionPromise = getSession().then((session) => {
      // Limpiamos la promesa un segundo después para que futuras navegaciones tengan datos frescos
      setTimeout(() => { activeSessionPromise = null; }, 1000);
      return session;
    });
  }

  // Todas las peticiones de la ráfaga (Promise.all) esperarán la MISMA respuesta
  const session = await activeSessionPromise;

  console.log("Token en apiClient:", session?.accessToken);
  if (session?.accessToken) {
    config.headers.Authorization = `Bearer ${session.accessToken}`;
  }
  return config;
});

// Redirigir al login cuando el token expira
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      const { signOut } = await import('next-auth/react');
      await signOut({ callbackUrl: '/login' });
    }
    return Promise.reject(error);
  }
);

// ==================== CLIENTE PÚBLICO (sin autenticación) ====================
const GATEWAY_URL = process.env.NEXT_PUBLIC_GATEWAY_URL || 'http://localhost:8080';

const publicApi = axios.create({
  baseURL: GATEWAY_URL,
  headers: { 'Content-Type': 'application/json' },
});

export const productosPublicApi = {
  listar: async (): Promise<Producto[]> => {
    const res = await publicApi.get('/api/productos');
    return res.data;
  },
};

export const categoriasPublicApi = {
  listar: async (): Promise<Categoria[]> => {
    const res = await publicApi.get('/api/categorias');
    return res.data;
  },
};

// ==================== PRODUCTOS CRUD (vía BFF) ====================
export const productosApi = {
  listar: async (): Promise<Producto[]> => {
    const res = await apiClient.get('/logistica/productos');
    return res.data;
  },
  crear: async (data: any): Promise<Producto> => {
    const res = await apiClient.post('/logistica/productos', data);
    return res.data;
  },
  obtener: async (id: number): Promise<Producto> => {
    const res = await apiClient.get(`/logistica/productos/${id}`);
    return res.data;
  },
  actualizar: async (id: number, data: any): Promise<Producto> => {
    const res = await apiClient.put(`/logistica/productos/${id}`, data);
    return res.data;
  },
  eliminar: async (id: number): Promise<void> => {
    await apiClient.delete(`/logistica/productos/${id}`);
  },
};

// ==================== CATEGORÍAS (CRUD) ====================
export const categoriasApi = {
  listar: async (): Promise<Categoria[]> => {
    const res = await apiClient.get('/logistica/categorias');
    return res.data;
  },
  obtener: async (id: number): Promise<Categoria> => {
    const res = await apiClient.get(`/logistica/categorias/${id}`);
    return res.data;
  },
  crear: async (data: any): Promise<Categoria> => {
    const res = await apiClient.post('/logistica/categorias', data);
    return res.data;
  },
  actualizar: async (id: number, data: any): Promise<Categoria> => {
    const res = await apiClient.put(`/logistica/categorias/${id}`, data);
    return res.data;
  },
  eliminar: async (id: number): Promise<void> => {
    await apiClient.delete(`/logistica/categorias/${id}`);
  },
};

// =================== PEDIDOS (vía BFF) ===================
export const pedidosApi = {
  crear: async (data: PedidoRequest, idempotencyKey: string): Promise<PedidoResponse> => {
    const res = await apiClient.post('/pedidos', data, {
      headers: { 'Idempotency-Key': idempotencyKey }
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
  obtenerPorPedidoId: async (pedidoId: number): Promise<Envio> => {
    const res = await apiClient.get(`/envios/pedido/${pedidoId}`);
    return res.data;
  },
  obtenerPorTracking: async (tracking: string): Promise<Envio> => {
    const res = await apiClient.get(`/envios/tracking/${tracking}`);
    return res.data;
  },
  actualizarEstado: async (id: number, nuevoEstado: string): Promise<Envio> => {
    const res = await apiClient.patch(`/envios/${id}/estado?nuevoEstado=${nuevoEstado}`);
    return res.data;
  },
  listarProblemas: async (): Promise<Envio[]> => {
    const res = await apiClient.get('/envios/problemas');
    return res.data;
  },
};