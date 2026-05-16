import api from './api';
import type { EnvioResponse } from '@/types';

export const enviosService = {
  // Obtener un envío por su ID
  async obtenerEnvio(id: number): Promise<EnvioResponse> {
    const response = await api.get<EnvioResponse>(`/bff/envios/${id}`);
    return response.data;
  },

  // Obtener la lista de envíos
  async listarEnvios(): Promise<EnvioResponse[]> {
    const response = await api.get<EnvioResponse[]>('/bff/envios');
    return response.data;
  },
};