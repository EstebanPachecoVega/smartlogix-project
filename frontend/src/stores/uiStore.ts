import { create } from 'zustand';

interface UIState {
  loading: boolean;
  error: string | null;
  toast: { message: string; type: 'success' | 'error' } | null;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  showToast: (message: string, type: 'success' | 'error') => void;
  hideToast: () => void;
}

export const useUIStore = create<UIState>((set) => ({
  loading: false,
  error: null,
  toast: null,
  setLoading: (loading) => set({ loading }),
  setError: (error) => set({ error }),
  showToast: (message, type) => set({ toast: { message, type } }),
  hideToast: () => set({ toast: null }),
}));