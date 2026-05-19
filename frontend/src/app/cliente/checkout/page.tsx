'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useCarritoStore, useTotalPrecio } from '@/store/carritoStore';
import { pedidosApi } from '@/lib/api';
import { PedidoRequest } from '@/types';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';

export default function CheckoutPage() {
  const router = useRouter();
  const { items, vaciar } = useCarritoStore();
  const totalPrecio = useTotalPrecio();
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
      <div className="text-center py-12">
        <h1 className="text-2xl font-bold mb-4">No hay productos</h1>
        <Button onClick={() => router.push('/cliente')}>Ir al catálogo</Button>
      </div>
    );
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      const payload: PedidoRequest = {
        usuarioId: 1,
        ...form,
        items: items.map((i) => ({
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
      router.push(`/cliente/pedidos?exito=${pedido.id}`);
    } catch (error) {
      console.error(error);
      alert('Error al crear el pedido. Intente nuevamente.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Finalizar compra</h1>
      <div className="grid md:grid-cols-2 gap-8">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <Label>Destinatario *</Label>
            <Input
              required
              value={form.destinatario}
              onChange={(e) => setForm({ ...form, destinatario: e.target.value })}
            />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <Label>Calle *</Label>
              <Input
                required
                value={form.calle}
                onChange={(e) => setForm({ ...form, calle: e.target.value })}
              />
            </div>
            <div>
              <Label>Número *</Label>
              <Input
                required
                value={form.numero}
                onChange={(e) => setForm({ ...form, numero: e.target.value })}
              />
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <Label>Comuna *</Label>
              <Input
                required
                value={form.comuna}
                onChange={(e) => setForm({ ...form, comuna: e.target.value })}
              />
            </div>
            <div>
              <Label>Ciudad *</Label>
              <Input
                required
                value={form.ciudad}
                onChange={(e) => setForm({ ...form, ciudad: e.target.value })}
              />
            </div>
          </div>
          <div>
            <Label>Código postal</Label>
            <Input
              value={form.codigoPostal}
              onChange={(e) => setForm({ ...form, codigoPostal: e.target.value })}
            />
          </div>
          <div>
            <Label>Método de envío *</Label>
            <select
              className="w-full border rounded-md p-2"
              required
              value={form.metodoEnvio}
              onChange={(e) => setForm({ ...form, metodoEnvio: e.target.value })}
            >
              <option value="standard">Estándar (3-5 días)</option>
              <option value="express">Express (1-2 días)</option>
            </select>
          </div>
          <Button type="submit" disabled={loading} className="w-full">
            {loading ? 'Procesando...' : `Pagar $${totalPrecio.toLocaleString()}`}
          </Button>
        </form>
        <div>
          <Card>
            <CardHeader>
              <CardTitle>Resumen del pedido</CardTitle>
            </CardHeader>
            <CardContent>
              {items.map((item) => (
                <div key={item.producto.id} className="flex justify-between text-sm mb-2">
                  <span>
                    {item.producto.nombre} x{item.cantidad}
                  </span>
                  <span>${(item.producto.precio * item.cantidad).toLocaleString()}</span>
                </div>
              ))}
              <div className="border-t pt-2 mt-2 font-bold flex justify-between">
                <span>Total</span>
                <span>${totalPrecio.toLocaleString()}</span>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}