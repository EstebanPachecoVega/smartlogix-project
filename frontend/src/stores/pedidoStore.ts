import { create } from 'zustand';
import { PedidoResponse } from '@/types';
import { pedidosService } from '@/services/pedidosService';
import { useUIStore } from './uiStore';

interface PedidoState {
  ultimoPedido: PedidoResponse | null;
  pedidos: PedidoResponse[];
  crearPedido: (productoId: number, cantidad: number) => Promise<void>;
  limpiar: () => void;
}

export const usePedidoStore = create<PedidoState>((set) => ({
  ultimoPedido: null,
  pedidos: [],
  crearPedido: async (productoId, cantidad) => {
    const uiStore = useUIStore.getState();
    uiStore.setLoading(true);
    uiStore.setError(null);
    try {
      const nuevo = await pedidosService.crear({ productoId, cantidad });
      set((state) => ({
        ultimoPedido: nuevo,
        pedidos: [nuevo, ...state.pedidos],
      }));
      uiStore.showToast(`Pedido #${nuevo.id} creado con éxito`, 'success');
    } catch (err: any) {
      const detail = err.detail || err.message || 'Error desconocido';
      uiStore.setError(detail);
      uiStore.showToast(detail, 'error');
      throw err;
    } finally {
      uiStore.setLoading(false);
    }
  },
  limpiar: () => set({ ultimoPedido: null, pedidos: [] }),
}));