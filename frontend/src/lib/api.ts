import axios from 'axios';
import { getSession } from 'next-auth/react';
import { Producto, PedidoRequest, PedidoResponse, Envio, Categoria, PageResponse } from '@/types';

const BFF_URL = process.env.NEXT_PUBLIC_BFF_URL || 'http://localhost:8084/bff';

const apiClient = axios.create({
  baseURL: BFF_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Cache de sesión para evitar llamadas duplicadas en paralelo
let inFlightPromise: Promise<any> | null = null;
let sessionCacheValue: { session: any; timestamp: number } | null = null;
const SESSION_CACHE_TTL = 2000; // 2 segundos

async function getSessionWithCache(): Promise<any> {
  const now = Date.now();

  if (sessionCacheValue && (now - sessionCacheValue.timestamp) < SESSION_CACHE_TTL) {
    return sessionCacheValue.session;
  }

  if (!inFlightPromise) {
    inFlightPromise = (async () => {
      try {
        let session = await getSession().catch(() => null);
        if (!session) {
          await new Promise(r => setTimeout(r, 300));
          session = await getSession().catch(() => null);
        }
        if (session) {
          sessionCacheValue = { session, timestamp: now };
        }
        return session;
      } finally {
        inFlightPromise = null;
      }
    })();
  }

  return inFlightPromise;
}

apiClient.interceptors.request.use(async (config) => {
  const session = await getSessionWithCache();
  console.log("Token en apiClient:", session?.accessToken ? "presente" : "NO HAY TOKEN");
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

export interface ProductoFilterParams {
  nombre?: string;
  categoriaId?: number;
  precioMin?: number;
  precioMax?: number;
  destacado?: boolean;
  novedad?: boolean;
}

export const productosPublicApi = {
  listar: async (params?: ProductoFilterParams): Promise<Producto[]> => {
    const res = await publicApi.get('/api/productos', { params });
    const data = res.data;
    return Array.isArray(data) ? data : data.content || [];
  },
  obtenerPorSlug: async (slug: string): Promise<Producto> => {
    const res = await publicApi.get(`/api/productos/slug/${slug}`);
    return res.data;
  },
};

export const categoriasPublicApi = {
  listar: async (): Promise<Categoria[]> => {
    const res = await publicApi.get('/api/categorias');
    const data = res.data;
    return Array.isArray(data) ? data : data.content || [];
  },
  obtenerPorId: async (id: number): Promise<Categoria> => {
    const res = await publicApi.get(`/api/categorias/${id}`);
    return res.data;
  },
  obtenerPorSlug: async (slug: string): Promise<Categoria> => {
    const res = await publicApi.get(`/api/categorias/slug/${slug}`);
    return res.data;
  },
};

// ==================== PRODUCTOS CRUD (vía BFF) ====================
export const productosApi = {
  listar: async (params?: { page?: number; size?: number }): Promise<Producto[] | PageResponse<Producto>> => {
    const res = await apiClient.get('/logistica/productos', { params });
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
    const data = res.data;
    return Array.isArray(data) ? data : data.content || [];
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
  reordenar: async (ordenes: { id: number; ordenVisual: number }[]): Promise<void> => {
    await apiClient.patch('/logistica/categorias/reordenar', ordenes);
  },
};

// =================== PEDIDOS (vía BFF) ===================
// =================== ESTADÍSTICAS (vía BFF) ===================
export const estadisticasApi = {
  ventasPlataforma: async (): Promise<{ plataforma: string; total: number }[]> => {
    const res = await apiClient.get('/estadisticas/ventas-plataforma');
    return res.data;
  },
  comparacionAnual: async (): Promise<{ mes: number; añoActual: number; añoAnterior: number }[]> => {
    const res = await apiClient.get('/estadisticas/comparacion-anual');
    return res.data;
  },
  ventasPorCategoria: async (): Promise<{ categoria: string; cantidad: number }[]> => {
    const res = await apiClient.get('/estadisticas/ventas-por-categoria');
    return res.data;
  },
};

export const pedidosApi = {
  crear: async (data: PedidoRequest, idempotencyKey: string): Promise<PedidoResponse> => {
    const res = await apiClient.post('/pedidos', data, {
      headers: { 'Idempotency-Key': idempotencyKey }
    });
    return res.data;
  },
  listar: async (params?: { page?: number; size?: number }): Promise<PedidoResponse[] | PageResponse<PedidoResponse>> => {
    const res = await apiClient.get('/pedidos', { params });
    return res.data;
  },
  obtener: async (id: number): Promise<PedidoResponse> => {
    const res = await apiClient.get(`/pedidos/${id}`);
    return res.data;
  },
};

// =================== ENVÍOS (vía BFF -> Gateway) ===================
export const enviosApi = {
  listar: async (params?: { page?: number; size?: number }): Promise<Envio[] | PageResponse<Envio>> => {
    const res = await apiClient.get('/envios', { params });
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