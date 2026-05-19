'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useCarritoStore } from '@/store/carritoStore';
import { pedidosApi } from '@/lib/api';
import { PedidoRequest } from '@/types';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

export default function CheckoutPage() {
    const router = useRouter();
    const { items, totalPrecio, vaciar } = useCarritoStore();
    const [loading, setLoading] = useState(false);
    const [form, setForm] = useState({
        destinatario: '',
        calle: '',
        numero: '',
        comuna: '',
        ciudad: '',
        codigoPostal: '',
        metodoEnvio: 'standard',
    });

    if (items.length === 0) {
        return (
            <div className="text-center py-8">
                <p>Carrito vacío</p>
                <Button onClick={() => router.push('/')} className="mt-4">Ir al catálogo</Button>
            </div>
        );
    }

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        try {
            const payload: PedidoRequest = {
                usuarioId: 1, // mock
                ...form,
                items: items.map(i => ({
                    productoId: i.producto.id,
                    sku: i.producto.sku,
                    nombreProducto: i.producto.nombre,
                    precioUnitario: i.producto.precio,
                    cantidad: i.cantidad,
                })),
            };
            const idempotencyKey = crypto.randomUUID();
            const pedido = await pedidosApi.crear(payload, idempotencyKey);
            vaciar();
            router.push(`/pedidos?exito=${pedido.id}`);
        } catch (err) {
            alert('Error al crear pedido');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div>
            <h1 className="text-2xl font-bold mb-6">Checkout</h1>
            <div className="grid md:grid-cols-2 gap-8">
                <form onSubmit={handleSubmit} className="space-y-4">
                    <div><Label>Destinatario</Label><Input required value={form.destinatario} onChange={e => setForm({ ...form, destinatario: e.target.value })} /></div>
                    <div className="grid grid-cols-2 gap-4">
                        <div><Label>Calle</Label><Input required value={form.calle} onChange={e => setForm({ ...form, calle: e.target.value })} /></div>
                        <div><Label>Número</Label><Input required value={form.numero} onChange={e => setForm({ ...form, numero: e.target.value })} /></div>
                    </div>
                    <div className="grid grid-cols-2 gap-4">
                        <div><Label>Comuna</Label><Input required value={form.comuna} onChange={e => setForm({ ...form, comuna: e.target.value })} /></div>
                        <div><Label>Ciudad</Label><Input required value={form.ciudad} onChange={e => setForm({ ...form, ciudad: e.target.value })} /></div>
                    </div>
                    <div><Label>Código postal</Label><Input value={form.codigoPostal} onChange={e => setForm({ ...form, codigoPostal: e.target.value })} /></div>
                    <div><Label>Método de envío</Label>
                        <select className="w-full border rounded p-2" value={form.metodoEnvio} onChange={e => setForm({ ...form, metodoEnvio: e.target.value })}>
                            <option value="standard">Estándar</option>
                            <option value="express">Express</option>
                        </select>
                    </div>
                    <Button type="submit" disabled={loading} className="w-full">{loading ? 'Procesando...' : `Pagar $${totalPrecio.toLocaleString()}`}</Button>
                </form>
                <div>
                    <h2 className="text-xl font-semibold mb-4">Resumen</h2>
                    {items.map(i => (
                        <div key={i.producto.id} className="flex justify-between text-sm mb-2">
                            <span>{i.producto.nombre} x{i.cantidad}</span>
                            <span>${(i.producto.precio * i.cantidad).toLocaleString()}</span>
                        </div>
                    ))}
                    <div className="border-t pt-2 mt-2 font-bold flex justify-between">
                        <span>Total</span><span>${totalPrecio.toLocaleString()}</span>
                    </div>
                </div>
            </div>
        </div>
    );
}