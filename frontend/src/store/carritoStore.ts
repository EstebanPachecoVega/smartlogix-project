import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import { Producto, ItemCarrito } from '@/types';

interface CarritoState {
    items: ItemCarrito[];
    agregar: (producto: Producto, cantidad: number) => void;
    eliminar: (productoId: number) => void;
    actualizarCantidad: (productoId: number, cantidad: number) => void;
    vaciar: () => void;
}

export const useCarritoStore = create<CarritoState>()(
    persist(
        (set, get) => ({
            items: [],
            agregar: (producto, cantidad) => {
                const items = get().items;
                const existente = items.find((i) => i.producto.id === producto.id);
                if (existente) {
                    set({
                        items: items.map((i) =>
                            i.producto.id === producto.id
                                ? { ...i, cantidad: i.cantidad + cantidad }
                                : i
                        ),
                    });
                } else {
                    set({ items: [...items, { producto, cantidad }] });
                }
            },
            eliminar: (productoId) => {
                set({ items: get().items.filter((i) => i.producto.id !== productoId) });
            },
            actualizarCantidad: (productoId, cantidad) => {
                if (cantidad <= 0) {
                    get().eliminar(productoId);
                    return;
                }
                set({
                    items: get().items.map((i) =>
                        i.producto.id === productoId ? { ...i, cantidad } : i
                    ),
                });
            },
            vaciar: () => set({ items: [] }),
        }),
        {
            name: 'carrito-storage',
            storage: createJSONStorage(() => localStorage),
        }
    )
);

// Selectores para totales
export const useTotalItems = () =>
    useCarritoStore((state) => state.items.reduce((acc, i) => acc + i.cantidad, 0));

export const useTotalPrecio = () =>
    useCarritoStore((state) =>
        state.items.reduce((acc, i) => acc + i.producto.precio * i.cantidad, 0)
    );