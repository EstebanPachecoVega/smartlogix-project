'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useCarritoStore, useTotalPrecio } from '@/store/carritoStore';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import Breadcrumbs from '@/components/ui/breadcrumbs';
import Image from 'next/image';

export default function CarritoPage() {
    const router = useRouter();
    const { items, actualizarCantidad, eliminar } = useCarritoStore();
    const totalPrecio = useTotalPrecio();

    return (
        <div>
            <Breadcrumbs items={[{ label: 'Carrito' }]} />
            {items.length === 0 ? (
                <div className="text-center py-12">
                    <h1 className="text-2xl font-bold mb-4">Carrito vacío</h1>
                    <Button onClick={() => router.push('/')}>
                        Ir al catálogo
                    </Button>
                </div>
            ) : (
            <>
            <h1 className="text-2xl font-bold mb-6">Mi carrito</h1>
            <div className="grid md:grid-cols-3 gap-8">
                <div className="md:col-span-2 space-y-4">
                    {items.map((item) => (
                        <Card key={item.producto.id}>
                            <CardContent className="flex items-center gap-4 p-4">
                                {item.producto.imagenPrincipal && (
                                    <div className="relative w-12 h-12 shrink-0 rounded overflow-hidden border">
                                        <Image
                                            src={item.producto.imagenPrincipal}
                                            alt={item.producto.nombre}
                                            fill
                                            className="object-cover"
                                        />
                                    </div>
                                )}
                                <div className="flex-1 min-w-0">
                                    <h3 className="font-semibold truncate">{item.producto.nombre}</h3>
                                    <p className="text-sm text-muted-foreground">
                                        ${item.producto.precio.toLocaleString()} c/u
                                    </p>
                                </div>
                                <div className="flex items-center gap-3">
                                    <Input
                                        type="number"
                                        min={1}
                                        max={item.producto.cantidad}
                                        value={item.cantidad}
                                        onChange={(e) =>
                                            actualizarCantidad(item.producto.id, parseInt(e.target.value) || 1)
                                        }
                                        className="w-20 text-center"
                                    />
                                    <Button
                                        variant="destructive"
                                        size="sm"
                                        onClick={() => eliminar(item.producto.id)}
                                    >
                                        Eliminar
                                    </Button>
                                </div>
                            </CardContent>
                        </Card>
                    ))}
                </div>
                <div>
                    <Card>
                        <CardHeader>
                            <CardTitle>Resumen</CardTitle>
                        </CardHeader>
                        <CardContent>
                            <div className="space-y-2">
                                <div className="flex justify-between">
                                    <span>Subtotal</span>
                                    <span>${totalPrecio.toLocaleString()}</span>
                                </div>
                                <div className="flex justify-between font-bold text-lg border-t pt-2">
                                    <span>Total</span>
                                    <span>${totalPrecio.toLocaleString()}</span>
                                </div>
                                <Link href="/dashboard/checkout">
                                    <Button className="w-full mt-4">Proceder al pago</Button>
                                </Link>
                            </div>
                        </CardContent>
                    </Card>
                </div>
            </div>
            </>
            )}
        </div>
    );
}