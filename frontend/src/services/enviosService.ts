import { apiGet } from './api';
import { EnvioResponse } from '@/types';

export const enviosService = {
  obtener: (id: number) => apiGet<EnvioResponse>(`/bff/envios/${id}`),
};