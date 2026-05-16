import { create } from 'zustand';
import { enviosService } from '@/services/enviosService';
import type { EnvioResponse } from '@/types';

interface EnvioState {
  envios: EnvioResponse[];
  currentEnvio: EnvioResponse | null;
  loading: boolean;
  error: string | null;
  listarEnvios: () => Promise<void>;
  obtenerEnvio: (id: number) => Promise<void>;
  obtenerEnvioPorPedidoId: (pedidoId: number) => Promise<EnvioResponse | null>;
  limpiarError: () => void;
}

export const useEnvioStore = create<EnvioState>((set) => ({
  envios: [],
  currentEnvio: null,
  loading: false,
  error: null,

  // Listar todos los envíos
  listarEnvios: async () => {
    set({ loading: true, error: null });
    try {
      const envios = await enviosService.listarEnvios();
      set({ envios, loading: false });
    } catch (err: any) {
      set({ error: err.message, loading: false });
    }
  },

  // Obtener un envío por su ID
  obtenerEnvio: async (id) => {
    set({ loading: true, error: null });
    try {
      const envio = await enviosService.obtenerEnvio(id);
      set({ currentEnvio: envio, loading: false });
    } catch (err: any) {
      set({ error: err.message, loading: false });
    }
  },

  // Obtener un envío por el ID del pedido
  obtenerEnvioPorPedidoId: async (pedidoId: number): Promise<EnvioResponse | null> => {
    set({ loading: true, error: null });
    try {
      const envio = await enviosService.obtenerEnvioPorPedidoId(pedidoId);
      set({ currentEnvio: envio, loading: false });
      return envio;
    } catch (err: any) {
      set({ error: err.message, loading: false });
      return null;
    }
  },


  limpiarError: () => set({ error: null }),
}));