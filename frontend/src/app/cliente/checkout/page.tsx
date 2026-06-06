'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useSession } from "next-auth/react";
import { useCarritoStore, useTotalPrecio } from '@/store/carritoStore';
import { pedidosApi } from '@/lib/api';
import { PedidoRequest } from '@/types';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { toast } from "sonner";
import { getUserProfile, updateUserAddress } from '@/lib/userService';
import { getCurrentUserId } from '@/lib/userId';

const getNombreCompleto = (profile: any) => {
  const partes = [
    profile.primerNombre,
    profile.segundoNombre,
    profile.primerApellido,
    profile.segundoApellido,
  ].filter(Boolean);
  return partes.join(' ');
};

export default function CheckoutPage() {
  const router = useRouter();
  const { data: session } = useSession();
  const { items, vaciar } = useCarritoStore();
  const totalPrecio = useTotalPrecio();
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({
    destinatario: '',
    calle: '',
    numero: '',
    comuna: '',
    ciudad: '',
    codigoPostal: '',
    metodoEnvio: 'standard',
  });

  useEffect(() => {
    getUserProfile().then(profile => {
      if (profile) {
        setForm(prev => ({
          ...prev,
          destinatario: getNombreCompleto(profile),
          calle: profile.calle || '',
          numero: profile.numero || '',
          comuna: profile.comuna || '',
          ciudad: profile.ciudad || '',
          codigoPostal: profile.codigoPostal || '',
        }));
      }
    });
  }, []);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleGuardarDireccion = async () => {
    setSaving(true);
    try {
      await updateUserAddress({
        calle: form.calle,
        numero: form.numero,
        comuna: form.comuna,
        ciudad: form.ciudad,
        codigoPostal: form.codigoPostal,
      });
      toast.success("Dirección guardada", { description: "La dirección se ha guardado en tu perfil." });
    } catch (error) {
      toast.error("Error", { description: "No se pudo guardar la dirección." });
    } finally {
      setSaving(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (items.length === 0) {
      toast.error("Carrito vacío", { description: "Agrega productos antes de pagar." });
      return;
    }
    const userIdNum = await getCurrentUserId();
    if (!userIdNum) {
      toast.error("Error de usuario", { description: "No se pudo identificar al usuario." });
      return;
    }
    setLoading(true);
    try {
      const payload: PedidoRequest = {
        usuarioId: userIdNum,
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
      router.push(`/cliente/pedidos?exito=${pedido.id}`);
    } catch (error) {
      console.error(error);
      toast.error("Error al crear el pedido", { description: "Intenta nuevamente más tarde." });
    } finally {
      setLoading(false);
    }
  };

  if (items.length === 0) {
    return (
      <div className="text-center py-12">
        <h1 className="text-2xl font-bold mb-4">No hay productos</h1>
        <Button onClick={() => router.push('/cliente')}>Ir al catálogo</Button>
      </div>
    );
  }

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Finalizar compra</h1>
      <div className="grid md:grid-cols-2 gap-8">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div><Label>Destinatario</Label><Input name="destinatario" value={form.destinatario} onChange={handleChange} required /></div>
          <div className="grid grid-cols-2 gap-4"><div><Label>Calle</Label><Input name="calle" value={form.calle} onChange={handleChange} required /></div><div><Label>Número</Label><Input name="numero" value={form.numero} onChange={handleChange} required /></div></div>
          <div className="grid grid-cols-2 gap-4"><div><Label>Comuna</Label><Input name="comuna" value={form.comuna} onChange={handleChange} required /></div><div><Label>Ciudad</Label><Input name="ciudad" value={form.ciudad} onChange={handleChange} required /></div></div>
          <div><Label>Código postal</Label><Input name="codigoPostal" value={form.codigoPostal} onChange={handleChange} /></div>
          <div><Label>Método de envío</Label><select className="w-full border rounded p-2" name="metodoEnvio" value={form.metodoEnvio} onChange={handleChange} required><option value="standard">Estándar (3-5 días)</option><option value="express">Express (1-2 días)</option></select></div>
          <div className="flex gap-4"><Button type="submit" disabled={loading} className="flex-1">{loading ? 'Procesando...' : `Pagar $${totalPrecio.toLocaleString()}`}</Button><Button type="button" variant="outline" onClick={handleGuardarDireccion} disabled={saving}>{saving ? 'Guardando...' : 'Guardar dirección en mi perfil'}</Button></div>
        </form>
        <div><Card><CardHeader><CardTitle>Resumen del pedido</CardTitle></CardHeader><CardContent>{items.map(item => <div key={item.producto.id} className="flex justify-between text-sm mb-2"><span>{item.producto.nombre} x{item.cantidad}</span><span>${(item.producto.precio * item.cantidad).toLocaleString()}</span></div>)}<div className="border-t pt-2 mt-2 font-bold flex justify-between"><span>Total</span><span>${totalPrecio.toLocaleString()}</span></div></CardContent></Card></div>
      </div>
    </div>
  );
}