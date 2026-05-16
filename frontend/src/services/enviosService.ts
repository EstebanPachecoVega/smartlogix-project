import api from './api';
import type { EnvioResponse } from '@/types';

export const enviosService = {
  async obtenerEnvio(id: number): Promise<EnvioResponse> {
    const response = await api.get<EnvioResponse>(`/bff/envios/${id}`);
    return response.data;
  },
};