'use client';
import { useEffect } from 'react';
import { useUIStore } from '@/stores/uiStore';

export default function Toast() {
  const { toast, hideToast } = useUIStore();

  useEffect(() => {
    if (toast) {
      const timer = setTimeout(() => hideToast(), 5000);
      return () => clearTimeout(timer);
    }
  }, [toast, hideToast]);

  if (!toast) return null;

  return (
    <div className={`fixed bottom-4 right-4 p-3 rounded shadow-lg text-white ${toast.type === 'error' ? 'bg-red-500' : 'bg-green-500'}`}>
      {toast.message}
    </div>
  );
}