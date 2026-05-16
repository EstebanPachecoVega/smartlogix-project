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
  limpiarError: () => void;
}

export const useEnvioStore = create<EnvioState>((set) => ({
  envios: [],
  currentEnvio: null,
  loading: false,
  error: null,

  listarEnvios: async () => {
    set({ loading: true, error: null });
    try {
      const envios = await enviosService.listarEnvios();
      set({ envios, loading: false });
    } catch (err: any) {
      set({ error: err.message, loading: false });
    }
  },

  obtenerEnvio: async (id) => {
    set({ loading: true, error: null });
    try {
      const envio = await enviosService.obtenerEnvio(id);
      set({ currentEnvio: envio, loading: false });
    } catch (err: any) {
      set({ error: err.message, loading: false });
    }
  },

  limpiarError: () => set({ error: null }),
}));