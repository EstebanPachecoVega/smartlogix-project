'use client';

import { use, useEffect, useState } from 'react';
import Link from 'next/link';
import { productosApi } from '@/lib/api';
import { Producto } from '@/types';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { ArrowLeft, Edit, Eye } from 'lucide-react';
import Image from 'next/image';
import Spinner from '@/components/shared/Spinner';

interface DetailPageProps {
    params: Promise<{ id: string }>;
}

export default function DetalleProductoPage({ params }: DetailPageProps) {
    const { id } = use(params);
    const [producto, setProducto] = useState<Producto | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [notFound, setNotFound] = useState(false);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const prodData = await productosApi.obtener(parseInt(id));
                setProducto(prodData);
            } catch (err: any) {
                if (err?.response?.status === 404 || err?.response?.status === 401) {
                    setNotFound(true);
                } else {
                    setError('Error al cargar el producto');
                }
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, [id]);

    if (loading) return <Spinner />;

    if (notFound) {
        return (
            <div className="max-w-3xl mx-auto space-y-6">
                <div className="flex items-center gap-4">
                    <Button variant="ghost" size="sm" asChild>
                        <Link href="/logistica/productos">
                            <ArrowLeft className="h-4 w-4 mr-2" /> Volver
                        </Link>
                    </Button>
                    <h1 className="text-2xl font-bold">Producto no encontrado</h1>
                </div>
                <div className="bg-yellow-50 border border-yellow-200 text-yellow-700 px-4 py-3 rounded-md text-sm">
                    El producto solicitado no existe o fue eliminado.
                </div>
            </div>
        );
    }

    if (error || !producto) {
        return (
            <div className="max-w-3xl mx-auto space-y-6">
                <div className="flex items-center gap-4">
                    <Button variant="ghost" size="sm" asChild>
                        <Link href="/logistica/productos">
                            <ArrowLeft className="h-4 w-4 mr-2" /> Volver
                        </Link>
                    </Button>
                    <h1 className="text-2xl font-bold">Error</h1>
                </div>
                <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md text-sm">
                    {error || 'No se pudo cargar el producto'}
                </div>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <div className="flex items-center gap-4">
                    <Button variant="ghost" size="sm" asChild>
                        <Link href="/logistica/productos">
                            <ArrowLeft className="h-4 w-4 mr-2" /> Volver
                        </Link>
                    </Button>
                    <h1 className="text-2xl font-bold">{producto.nombre}</h1>
                    {producto.activo === false && (
                        <span className="bg-red-100 text-red-700 text-xs font-medium px-2.5 py-0.5 rounded-full">
                            Inactivo
                        </span>
                    )}
                </div>
                <Button asChild>
                    <Link href={`/logistica/productos/${producto.id}/editar`}>
                        <Edit className="h-4 w-4 mr-2" /> Editar
                    </Link>
                </Button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <Card>
                    <CardHeader>
                        <CardTitle>Información general</CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-3">
                        <div>
                            <span className="text-sm text-gray-500">SKU</span>
                            <p className="font-medium font-mono text-sm">{producto.sku}</p>
                        </div>
                        <div>
                            <span className="text-sm text-gray-500">Nombre</span>
                            <p className="font-medium">{producto.nombre}</p>
                        </div>
                        <div>
                            <span className="text-sm text-gray-500">Slug</span>
                            <p className="font-medium font-mono text-sm">{producto.slug || '—'}</p>
                        </div>
                        <div>
                            <span className="text-sm text-gray-500">Descripción</span>
                            <p className="font-medium whitespace-pre-wrap">{producto.descripcion || '—'}</p>
                        </div>
                        <div>
                            <span className="text-sm text-gray-500">Categoría</span>
                            <p className="font-medium">
                                {producto.categoriaNombre ? (
                                    <Link
                                        href={`/logistica/categorias/${producto.categoriaId}`}
                                        className="text-blue-600 hover:underline"
                                    >
                                        {producto.categoriaNombre}
                                    </Link>
                                ) : (
                                    '— (Sin categoría)'
                                )}
                            </p>
                        </div>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle>Precio y stock</CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-3">
                        <div>
                            <span className="text-sm text-gray-500">Precio</span>
                            <p className="font-medium text-lg">${producto.precio.toLocaleString()}</p>
                        </div>
                        <div>
                            <span className="text-sm text-gray-500">Stock</span>
                            <p className="font-medium">{producto.cantidad} unidades</p>
                        </div>
                    </CardContent>
                </Card>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <Card>
                    <CardHeader>
                        <CardTitle>Estado</CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-3">
                        <div className="flex items-center justify-between">
                            <span className="text-sm text-gray-500">Activo</span>
                            <span className={`text-sm font-medium ${producto.activo ? 'text-green-600' : 'text-red-600'}`}>
                                {producto.activo ? 'Sí' : 'No'}
                            </span>
                        </div>
                        <div className="flex items-center justify-between">
                            <span className="text-sm text-gray-500">Destacado</span>
                            <span className={`text-sm font-medium ${producto.destacado ? 'text-green-600' : 'text-gray-400'}`}>
                                {producto.destacado ? 'Sí' : 'No'}
                            </span>
                        </div>
                        <div className="flex items-center justify-between">
                            <span className="text-sm text-gray-500">Novedad</span>
                            <span className={`text-sm font-medium ${producto.novedad ? 'text-green-600' : 'text-gray-400'}`}>
                                {producto.novedad ? 'Sí' : 'No'}
                            </span>
                        </div>
                    </CardContent>
                </Card>

                <Card className="md:col-span-2">
                    <CardHeader>
                        <CardTitle>Imagen principal</CardTitle>
                    </CardHeader>
                    <CardContent>
                        {producto.imagenPrincipal ? (
                            <div className="relative w-full aspect-video bg-transparent rounded-md overflow-hidden">
                                <Image
                                    src={producto.imagenPrincipal}
                                    alt={producto.nombre}
                                    fill
                                    className="object-contain"
                                    sizes="(max-width: 768px) 100vw, 50vw"
                                />
                            </div>
                        ) : (
                            <div className="w-full aspect-video bg-gray-100 rounded-md flex items-center justify-center text-gray-400 text-sm">
                                Sin imagen
                            </div>
                        )}
                    </CardContent>
                </Card>
            </div>

            {producto.imagenes && producto.imagenes.length > 0 && (
                <Card>
                    <CardHeader>
                        <CardTitle>Imágenes adicionales ({producto.imagenes.length})</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                            {producto.imagenes.map((url, i) => (
                                <div key={i} className="relative aspect-video bg-transparent rounded-md overflow-hidden border">
                                    <Image
                                        src={url}
                                        alt={`${producto.nombre} - imagen ${i + 1}`}
                                        fill
                                        className="object-contain"
                                        sizes="(max-width: 768px) 50vw, 25vw"
                                    />
                                </div>
                            ))}
                        </div>
                    </CardContent>
                </Card>
            )}

            {(producto.fechaCreacion || producto.fechaActualizacion) && (
                <div className="flex gap-6 text-sm text-gray-500">
                    {producto.fechaCreacion && (
                        <span>Creado: {new Date(producto.fechaCreacion).toLocaleDateString('es-CL', {
                            year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit',
                        })}</span>
                    )}
                    {producto.fechaActualizacion && (
                        <span>Actualizado: {new Date(producto.fechaActualizacion).toLocaleDateString('es-CL', {
                            year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit',
                        })}</span>
                    )}
                </div>
            )}
        </div>
    );
}
