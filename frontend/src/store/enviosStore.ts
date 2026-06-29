import { create } from 'zustand';
import { Envio } from '@/types';
import { enviosApi } from '@/lib/api';

interface EnviosState {
  envios: Envio[];
  loading: boolean;
  filtroEstado: string | null;
  setFiltroEstado: (estado: string | null) => void;
  cargarEnvios: () => Promise<void>;
  actualizarEstado: (id: number, nuevoEstado: string) => Promise<void>;
}

export const useEnviosStore = create<EnviosState>((set, get) => ({
  envios: [],
  loading: false,
  filtroEstado: null,
  setFiltroEstado: (estado) => set({ filtroEstado: estado }),
  cargarEnvios: async () => {
    set({ loading: true });
    try {
      const data = await enviosApi.listar();
      set({ envios: Array.isArray(data) ? data : data.content });
    } catch (error) {
      console.error(error);
    } finally {
      set({ loading: false });
    }
  },
  actualizarEstado: async (id, nuevoEstado) => {
    try {
      const updated = await enviosApi.actualizarEstado(id, nuevoEstado);
      set((state) => ({
        envios: state.envios.map((e) => (e.id === id ? updated : e)),
      }));
    } catch (error) {
      console.error(error);
      throw error;
    }
  },
}));