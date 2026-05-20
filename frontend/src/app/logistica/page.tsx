'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { productosApi, categoriasApi, pedidosApi, enviosApi } from '@/lib/api';
import { Producto, Categoria, PedidoResponse, Envio } from '@/types';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { EstadoPedidoBadge } from '@/components/ui/EstadoPedidoBadge';
import { EstadoEnvioBadge } from '@/components/ui/EstadoEnvioBadge';
import { Package, ShoppingCart, Truck, CheckCircle, AlertCircle, Clock, TrendingUp } from 'lucide-react';
import Spinner from '@/components/shared/Spinner';

export default function DashboardPage() {
    const [loading, setLoading] = useState(true);
    const [productos, setProductos] = useState<Producto[]>([]);
    const [categorias, setCategorias] = useState<Categoria[]>([]);
    const [pedidos, setPedidos] = useState<PedidoResponse[]>([]);
    const [envios, setEnvios] = useState<Envio[]>([]);

    useEffect(() => {
        Promise.all([
            productosApi.listar(),
            categoriasApi.listar(),
            pedidosApi.listar(),
            enviosApi.listar(),
        ])
            .then(([prod, cat, ped, env]) => {
                setProductos(prod);
                setCategorias(cat);
                setPedidos(ped);
                setEnvios(env);
            })
            .catch(console.error)
            .finally(() => setLoading(false));
    }, []);

    if (loading) return <Spinner />;

    // Calcular métricas
    const totalProductos = productos.length;
    const totalVentas = pedidos.reduce((total, pedido) => total + pedido.totalCompra, 0);
    const totalPedidos = pedidos.length;
    const totalEnvios = envios.length;
    const pedidosEntregados = pedidos.filter(p => p.estado === 'ENTREGADO').length;
    const enviosEntregados = envios.filter(e => e.estadoEnvio === 'ENTREGADO').length;
    const enviosProblemas = envios.filter(e =>
        e.estadoEnvio === 'INTENTO_FALLIDO' ||
        e.estadoEnvio === 'RETRASADO' ||
        e.estadoEnvio === 'DEVUELTO'
    ).length;

    // Envíos por estado (conteo)
    const enviosPorEstado = envios.reduce((acc, e) => {
        acc[e.estadoEnvio] = (acc[e.estadoEnvio] || 0) + 1;
        return acc;
    }, {} as Record<string, number>);

    // Últimos 5 pedidos
    const ultimosPedidos = [...pedidos].sort((a, b) => {
        if (a.fechaPedido && b.fechaPedido) {
            return new Date(b.fechaPedido).getTime() - new Date(a.fechaPedido).getTime();
        }
        return b.id - a.id;
    }).slice(0, 5);

    // Envíos con problemas (hasta 5)
    const enviosConProblemas = envios.filter(e =>
        e.estadoEnvio === 'INTENTO_FALLIDO' ||
        e.estadoEnvio === 'RETRASADO' ||
        e.estadoEnvio === 'DEVUELTO'
    ).slice(0, 5);

    return (
        <div>
            <h1 className="text-2xl font-bold mb-6">Dashboard Logística</h1>

            {/* Tarjetas de métricas principales */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between pb-2">
                        <CardTitle className="text-sm font-medium">Productos</CardTitle>
                        <Package className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{totalProductos}</div>
                        <p className="text-xs text-muted-foreground">Activos en catálogo</p>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between pb-2">
                        <CardTitle className="text-sm font-medium">Ventas</CardTitle>
                        <TrendingUp className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">${totalVentas.toLocaleString()}</div>
                        <p className="text-xs text-muted-foreground">Total facturado</p>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between pb-2">
                        <CardTitle className="text-sm font-medium">Pedidos</CardTitle>
                        <ShoppingCart className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{totalPedidos}</div>
                        <p className="text-xs text-muted-foreground">{pedidosEntregados} entregados</p>
                    </CardContent>
                </Card>
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between pb-2">
                        <CardTitle className="text-sm font-medium">Envíos</CardTitle>
                        <Truck className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{totalEnvios}</div>
                        <p className="text-xs text-muted-foreground">{enviosEntregados} entregados</p>
                    </CardContent>
                </Card>
            </div>

            {/* Envíos por estado (resumen) */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
                <Card>
                    <CardHeader>
                        <CardTitle>Envíos por estado</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <div className="space-y-3">
                            {Object.entries(enviosPorEstado).map(([estado, cantidad]) => (
                                <div key={estado} className="flex items-center justify-between">
                                    <div className="flex items-center gap-2">
                                        <EstadoEnvioBadge estado={estado} />
                                    </div>
                                    <span className="font-semibold">{cantidad}</span>
                                </div>
                            ))}
                            {Object.keys(enviosPorEstado).length === 0 && (
                                <p className="text-gray-500">No hay envíos registrados</p>
                            )}
                        </div>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle>Resumen rápido</CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-3">
                        <div className="flex justify-between items-center">
                            <span className="flex items-center gap-2"><CheckCircle className="h-4 w-4 text-green-500" /> Pedidos completados</span>
                            <span className="font-semibold">{pedidosEntregados}</span>
                        </div>
                        <div className="flex justify-between items-center">
                            <span className="flex items-center gap-2"><Clock className="h-4 w-4 text-yellow-500" /> Pendientes / En curso</span>
                            <span className="font-semibold">{totalPedidos - pedidosEntregados}</span>
                        </div>
                        <div className="flex justify-between items-center">
                            <span className="flex items-center gap-2"><AlertCircle className="h-4 w-4 text-red-500" /> Envíos con problemas</span>
                            <span className="font-semibold">{enviosProblemas}</span>
                        </div>
                    </CardContent>
                </Card>
            </div>

            {/* Últimos pedidos */}
            <Card className="mb-8">
                <CardHeader className="flex flex-row items-center justify-between">
                    <CardTitle>Últimos pedidos</CardTitle>
                    <Link href="/logistica/pedidos">
                        <Button variant="outline" size="sm">Ver todos</Button>
                    </Link>
                </CardHeader>
                <CardContent>
                    {ultimosPedidos.length === 0 ? (
                        <p className="text-gray-500">No hay pedidos registrados</p>
                    ) : (
                        <div className="space-y-3">
                            {ultimosPedidos.map((pedido) => (
                                <div key={pedido.id} className="flex items-center justify-between border-b pb-2">
                                    <div>
                                        <p className="font-medium">{pedido.numeroOrden}</p>
                                        <p className="text-sm text-gray-500">
                                            {pedido.fechaPedido ? new Date(pedido.fechaPedido).toLocaleDateString() : 'Fecha no disponible'}
                                        </p>
                                    </div>
                                    <div className="flex items-center gap-3">
                                        <EstadoPedidoBadge estado={pedido.estado} />
                                        <span className="font-semibold">${pedido.totalCompra.toLocaleString()}</span>
                                        <Link href={`/logistica/pedidos/${pedido.id}`}>
                                            <Button variant="ghost" size="sm">Ver</Button>
                                        </Link>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </CardContent>
            </Card>

            {/* Envíos con problemas */}
            <Card>
                <CardHeader className="flex flex-row items-center justify-between">
                    <CardTitle>⚠️ Envíos con problemas</CardTitle>
                    <Link href="/logistica/envios">
                        <Button variant="outline" size="sm">Ver todos</Button>
                    </Link>
                </CardHeader>
                <CardContent>
                    {enviosConProblemas.length === 0 ? (
                        <p className="text-gray-500">No hay envíos con problemas</p>
                    ) : (
                        <div className="space-y-3">
                            {enviosConProblemas.map((envio) => (
                                <div key={envio.id} className="flex items-center justify-between border-b pb-2">
                                    <div>
                                        <p className="font-medium">{envio.numeroTracking}</p>
                                        <p className="text-sm text-gray-500">Pedido #{envio.pedidoId} - {envio.destinatario}</p>
                                    </div>
                                    <div className="flex items-center gap-3">
                                        <EstadoEnvioBadge estado={envio.estadoEnvio} />
                                        <Link href={`/logistica/envios/${envio.id}`}>
                                            <Button variant="ghost" size="sm">Ver</Button>
                                        </Link>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </CardContent>
            </Card>
        </div>
    );
}