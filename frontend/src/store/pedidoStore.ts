import { create } from 'zustand';
import { pedidosService } from '@/services/pedidosService';
import type { PedidoResponse, CrearPedidoRequest } from '@/types';

interface PedidoState {
  pedidos: PedidoResponse[];
  currentPedido: PedidoResponse | null;
  loading: boolean;
  error: string | null;
  crearPedido: (data: CrearPedidoRequest) => Promise<void>;
  obtenerPedido: (id: number) => Promise<void>;
  limpiarError: () => void;
}

export const usePedidoStore = create<PedidoState>((set) => ({
  pedidos: [],
  currentPedido: null,
  loading: false,
  error: null,

  crearPedido: async (data) => {
    set({ loading: true, error: null });
    try {
      const nuevo = await pedidosService.crearPedido(data);
      set((state) => ({
        pedidos: [nuevo, ...state.pedidos],
        currentPedido: nuevo,
        loading: false,
      }));
    } catch (err: any) {
      set({ error: err.message, loading: false });
    }
  },

  obtenerPedido: async (id) => {
    set({ loading: true, error: null });
    try {
      const pedido = await pedidosService.obtenerPedido(id);
      set({ currentPedido: pedido, loading: false });
    } catch (err: any) {
      set({ error: err.message, loading: false });
    }
  },

  limpiarError: () => set({ error: null }),
}));