'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { productosApi, pedidosApi, enviosApi } from '@/lib/api';
import { Producto, PedidoResponse, Envio } from '@/types';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { EstadoPedidoBadge } from '@/components/ui/EstadoPedidoBadge';
import { EstadoEnvioBadge } from '@/components/ui/EstadoEnvioBadge';
import {
    Package, ShoppingCart, Truck,
    TrendingUp,
} from 'lucide-react';
import Spinner from '@/components/shared/Spinner';
import DistribucionEnviosChart from '@/components/logistica/DistribucionEnviosChart';
import DistribucionPedidosChart from '@/components/logistica/DistribucionPedidosChart';
import StockBajoChart from '@/components/logistica/StockBajoChart';
import VentasUltimosDiasChart from '@/components/logistica/VentasUltimosDiasChart';
import VentasPlataformaChart from '@/components/logistica/VentasPlataformaChart';
import ComparacionAnualChart from '@/components/logistica/ComparacionAnualChart';
import VentasPorCategoriaChart from '@/components/logistica/VentasPorCategoriaChart';
import VentasLineChart from '@/components/logistica/VentasLineChart';

type Filtro = 'semana' | 'mes' | 'año';

export default function DashboardPage() {
    const [loading, setLoading] = useState(true);
    const [productos, setProductos] = useState<Producto[]>([]);
    const [pedidos, setPedidos] = useState<PedidoResponse[]>([]);
    const [envios, setEnvios] = useState<Envio[]>([]);
    const [filtro, setFiltro] = useState<Filtro>('semana');

    useEffect(() => {
        Promise.all([
            productosApi.listar(),
            pedidosApi.listar(),
            enviosApi.listar(),
        ])
            .then(([prod, ped, env]) => {
                setProductos(Array.isArray(prod) ? prod : prod.content);
                setPedidos(Array.isArray(ped) ? ped : ped.content);
                setEnvios(Array.isArray(env) ? env : env.content);
            })
            .catch(console.error)
            .finally(() => setLoading(false));
    }, []);

    if (loading) return <Spinner />;

    // Métricas
    const totalProductos = productos.length;
    const totalVentas = pedidos.reduce((s, p) => s + p.totalCompra, 0);
    const totalPedidos = pedidos.length;
    const totalEnvios = envios.length;
    const pedidosEntregados = pedidos.filter((p) => p.estado === 'ENTREGADO').length;
    const enviosEntregados = envios.filter((e) => e.estadoEnvio === 'ENTREGADO').length;
    const enviosProblemas = envios.filter((e) =>
        ['INTENTO_FALLIDO', 'RETRASADO', 'DEVUELTO'].includes(e.estadoEnvio),
    ).length;

    const enviosPorEstado = envios.reduce<Record<string, number>>((acc, e) => {
        acc[e.estadoEnvio] = (acc[e.estadoEnvio] || 0) + 1;
        return acc;
    }, {});

    const ultimosPedidos = [...pedidos]
        .sort((a, b) => {
            if (a.fechaPedido && b.fechaPedido)
                return new Date(b.fechaPedido).getTime() - new Date(a.fechaPedido).getTime();
            return b.id - a.id;
        })
        .slice(0, 5);

    const enviosConProblemas = envios
        .filter((e) => ['INTENTO_FALLIDO', 'RETRASADO', 'DEVUELTO'].includes(e.estadoEnvio))
        .slice(0, 5);

    // Render 
    return (
        <div className="space-y-8 pb-8">
            <h1 className="text-2xl font-bold">Dashboard Logística</h1>

            {/* Tarjetas de métricas */}
            <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
                <MetricCard
                    title="Productos"
                    value={totalProductos}
                    subtitle="Activos en catálogo"
                    icon={<Package className="h-4 w-4 text-muted-foreground" />}
                />
                <MetricCard
                    title="Ventas"
                    value={`$${totalVentas.toLocaleString('es-CL')}`}
                    subtitle="Total facturado"
                    icon={<TrendingUp className="h-4 w-4 text-muted-foreground" />}
                />
                <MetricCard
                    title="Pedidos"
                    value={totalPedidos}
                    subtitle={`${pedidosEntregados} entregados`}
                    icon={<ShoppingCart className="h-4 w-4 text-muted-foreground" />}
                />
                <MetricCard
                    title="Envíos"
                    value={totalEnvios}
                    subtitle={`${enviosEntregados} entregados`}
                    icon={<Truck className="h-4 w-4 text-muted-foreground" />}
                />
            </div>

            {/* Filtros */}
            <div className="flex gap-2">
              {(['semana', 'mes', 'año'] as Filtro[]).map((f) => (
                <button
                  key={f}
                  onClick={() => setFiltro(f)}
                  className={`px-3 py-1.5 rounded text-sm font-medium transition-colors ${
                    filtro === f
                      ? 'bg-primary text-primary-foreground'
                      : 'bg-muted text-muted-foreground hover:bg-muted/80'
                  }`}
                >
                  {f === 'semana' ? 'Semana' : f === 'mes' ? 'Mes' : 'Año'}
                </button>
              ))}
            </div>

            {/* Distribución de envíos + Distribución de pedidos */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <Card>
                    <CardHeader>
                        <CardTitle className="text-base">Envíos por estado</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <DistribucionEnviosChart envios={envios} />
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle className="text-base">Pedidos por estado</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <DistribucionPedidosChart pedidos={pedidos} />
                    </CardContent>
                </Card>
            </div>

            {/* Stock bajo + Ventas últimos días */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <Card>
                    <CardHeader>
                        <CardTitle className="text-base">Productos con menor stock</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <StockBajoChart productos={productos} />
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle className="text-base">Ventas últimos 7 días</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <VentasUltimosDiasChart pedidos={pedidos} />
                    </CardContent>
                </Card>
            </div>

            {/* Ventas por plataforma + Comparación anual */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <Card>
                <CardHeader>
                  <CardTitle className="text-base">Ventas por plataforma</CardTitle>
                </CardHeader>
                <CardContent>
                  <VentasPlataformaChart />
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle className="text-base">Comparación anual</CardTitle>
                </CardHeader>
                <CardContent>
                  <ComparacionAnualChart />
                </CardContent>
              </Card>
            </div>

            {/* Ventas por categoría + Línea de ventas */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <Card>
                <CardHeader>
                  <CardTitle className="text-base">Ventas por categoría</CardTitle>
                </CardHeader>
                <CardContent>
                  <VentasPorCategoriaChart />
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle className="text-base">Ventas últimos 30 días</CardTitle>
                </CardHeader>
                <CardContent>
                  <VentasLineChart pedidos={pedidos} />
                </CardContent>
              </Card>
            </div>

            {/* Últimos pedidos */}
            <Card>
                <CardHeader className="flex flex-row items-center justify-between">
                    <CardTitle className="text-base">Últimos pedidos</CardTitle>
                    <Link href="/logistica/pedidos">
                        <Button variant="outline" size="sm">Ver todos</Button>
                    </Link>
                </CardHeader>
                <CardContent>
                    {ultimosPedidos.length === 0 ? (
                        <p className="text-sm text-muted-foreground">No hay pedidos registrados.</p>
                    ) : (
                        <ul className="divide-y">
                            {ultimosPedidos.map((pedido) => (
                                <li
                                    key={pedido.id}
                                    className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 py-3"
                                >
                                    <div className="min-w-0">
                                        <p className="text-sm font-medium truncate">{pedido.numeroOrden}</p>
                                        <p className="text-xs text-muted-foreground">
                                            {pedido.fechaPedido
                                                ? new Date(pedido.fechaPedido).toLocaleDateString('es-CL')
                                                : 'Fecha no disponible'}
                                        </p>
                                    </div>
                                    <div className="flex items-center gap-3 shrink-0">
                                        <EstadoPedidoBadge estado={pedido.estado} />
                                        <span className="text-sm font-semibold tabular-nums">
                                            ${pedido.totalCompra.toLocaleString('es-CL')}
                                        </span>
                                        <Link href={`/logistica/pedidos/${pedido.id}`}>
                                            <Button variant="ghost" size="sm">Ver</Button>
                                        </Link>
                                    </div>
                                </li>
                            ))}
                        </ul>
                    )}
                </CardContent>
            </Card>

            {/* Envíos con problemas */}
            <Card>
                <CardHeader className="flex flex-row items-center justify-between">
                    <CardTitle className="text-base">⚠️ Envíos con problemas</CardTitle>
                    <Link href="/logistica/envios">
                        <Button variant="outline" size="sm">Ver todos</Button>
                    </Link>
                </CardHeader>
                <CardContent>
                    {enviosConProblemas.length === 0 ? (
                        <p className="text-sm text-muted-foreground">No hay envíos con problemas.</p>
                    ) : (
                        <ul className="divide-y">
                            {enviosConProblemas.map((envio) => (
                                <li
                                    key={envio.id}
                                    className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 py-3"
                                >
                                    <div className="min-w-0">
                                        <p className="text-sm font-medium">{envio.numeroTracking}</p>
                                        <p className="text-xs text-muted-foreground truncate max-w-xs">
                                            {envio.destinatario}
                                        </p>
                                    </div>
                                    <div className="flex items-center gap-3 shrink-0">
                                        <EstadoEnvioBadge estado={envio.estadoEnvio} />
                                        <Link href={`/logistica/envios/${envio.id}`}>
                                            <Button variant="ghost" size="sm">Ver</Button>
                                        </Link>
                                    </div>
                                </li>
                            ))}
                        </ul>
                    )}
                </CardContent>
            </Card>
        </div>
    );
}

// Subcomponentes 

function MetricCard({
    title,
    value,
    subtitle,
    icon,
}: {
    title: string;
    value: string | number;
    subtitle: string;
    icon: React.ReactNode;
}) {
    return (
        <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
                <CardTitle className="text-xs sm:text-sm font-medium">{title}</CardTitle>
                {icon}
            </CardHeader>
            <CardContent>
                <div className="text-xl sm:text-2xl font-bold tabular-nums">{value}</div>
                <p className="text-xs text-muted-foreground mt-0.5">{subtitle}</p>
            </CardContent>
        </Card>
    );
}

function SummaryRow({
    icon,
    label,
    value,
}: {
    icon: React.ReactNode;
    label: string;
    value: number;
}) {
    return (
        <div className="flex items-center justify-between">
            <span className="flex items-center gap-2 text-sm">{icon}{label}</span>
            <span className="text-sm font-semibold tabular-nums">{value}</span>
        </div>
    );
}