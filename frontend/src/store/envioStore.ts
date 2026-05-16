import { create } from 'zustand';
import { enviosService } from '@/services/enviosService';
import type { EnvioResponse } from '@/types';

interface EnvioState {
  currentEnvio: EnvioResponse | null;
  loading: boolean;
  error: string | null;
  obtenerEnvio: (id: number) => Promise<void>;
  limpiarError: () => void;
}

export const useEnvioStore = create<EnvioState>((set) => ({
  currentEnvio: null,
  loading: false,
  error: null,

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