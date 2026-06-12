'use client';

import { use, useEffect, useState } from 'react';
import Link from 'next/link';
import { categoriasApi } from '@/lib/api';
import { Categoria } from '@/types';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Table, TableHeader, TableRow, TableHead, TableBody, TableCell } from '@/components/ui/table';
import { ArrowLeft, Edit, Eye, Package, Pencil } from 'lucide-react';
import Spinner from '@/components/shared/Spinner';

interface DetailPageProps {
    params: Promise<{ id: string }>;
}

export default function DetalleCategoriaPage({ params }: DetailPageProps) {
    const { id } = use(params);
    const [categoria, setCategoria] = useState<Categoria | null>(null);
    const [subcategorias, setSubcategorias] = useState<Categoria[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [notFound, setNotFound] = useState(false);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [catData, listData] = await Promise.all([
                    categoriasApi.obtener(parseInt(id)),
                    categoriasApi.listar(),
                ]);
                setCategoria(catData);
                setSubcategorias(listData.filter((c) => c.padreId === catData.id));
            } catch (err: any) {
                if (err?.response?.status === 404 || err?.response?.status === 401) {
                    setNotFound(true);
                } else {
                    setError('Error al cargar la categoría');
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
            <div className="max-w-2xl mx-auto space-y-6">
                <div className="flex items-center gap-4">
                    <Button variant="ghost" size="sm" asChild>
                        <Link href="/logistica/categorias">
                            <ArrowLeft className="h-4 w-4 mr-2" /> Volver
                        </Link>
                    </Button>
                    <h1 className="text-2xl font-bold">Categoría no encontrada</h1>
                </div>
                <div className="bg-yellow-50 border border-yellow-200 text-yellow-700 px-4 py-3 rounded-md text-sm">
                    La categoría solicitada no existe o fue eliminada.
                </div>
            </div>
        );
    }

    if (error || !categoria) {
        return (
            <div className="max-w-2xl mx-auto space-y-6">
                <div className="flex items-center gap-4">
                    <Button variant="ghost" size="sm" asChild>
                        <Link href="/logistica/categorias">
                            <ArrowLeft className="h-4 w-4 mr-2" /> Volver
                        </Link>
                    </Button>
                    <h1 className="text-2xl font-bold">Error</h1>
                </div>
                <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md text-sm">
                    {error || 'No se pudo cargar la categoría'}
                </div>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <div className="flex items-center gap-4">
                    <Button variant="ghost" size="sm" asChild>
                        <Link href="/logistica/categorias">
                            <ArrowLeft className="h-4 w-4 mr-2" /> Volver
                        </Link>
                    </Button>
                    <h1 className="text-2xl font-bold">{categoria.nombre}</h1>
                    {!categoria.activo && (
                        <span className="bg-red-100 text-red-700 text-xs font-medium px-2.5 py-0.5 rounded-full">
                            Inactiva
                        </span>
                    )}
                </div>
                <Button asChild>
                    <Link href={`/logistica/categorias/${categoria.id}/editar`}>
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
                            <span className="text-sm text-gray-500">Nombre</span>
                            <p className="font-medium">{categoria.nombre}</p>
                        </div>
                        <div>
                            <span className="text-sm text-gray-500">Slug</span>
                            <p className="font-medium font-mono text-sm">{categoria.slug}</p>
                        </div>
                        <div>
                            <span className="text-sm text-gray-500">Descripción</span>
                            <p className="font-medium">{categoria.descripcion || '—'}</p>
                        </div>
                        <div>
                            <span className="text-sm text-gray-500">Orden visual</span>
                            <p className="font-medium">{categoria.ordenVisual ?? '—'}</p>
                        </div>
                        <div>
                            <span className="text-sm text-gray-500">Estado</span>
                            <p className="font-medium">{categoria.activo ? 'Activa' : 'Inactiva'}</p>
                        </div>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle>Jerarquía</CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-3">
                        <div>
                            <span className="text-sm text-gray-500">Categoría padre</span>
                            <p className="font-medium">
                                {categoria.padreNombre ? (
                                    <Link
                                        href={`/logistica/categorias/${categoria.padreId}`}
                                        className="text-blue-600 hover:underline"
                                    >
                                        {categoria.padreNombre}
                                    </Link>
                                ) : (
                                    '— (Raíz)'
                                )}
                            </p>
                        </div>
                        <div>
                            <span className="text-sm text-gray-500">Subcategorías</span>
                            <p className="font-medium">{subcategorias.length}</p>
                        </div>
                    </CardContent>
                </Card>
            </div>

            <div>
                <div className="flex items-center justify-between mb-4">
                    <h2 className="text-xl font-semibold">Subcategorías</h2>
                    <Button variant="outline" size="sm" asChild>
                        <Link href={`/logistica/categorias/nuevo`}>
                            <Package className="h-4 w-4 mr-2" /> Nueva subcategoría
                        </Link>
                    </Button>
                </div>

                {subcategorias.length === 0 ? (
                    <Card>
                        <CardContent className="py-8 text-center text-gray-500">
                            No tiene subcategorías.
                        </CardContent>
                    </Card>
                ) : (
                    <Card>
                        <CardContent className="p-0">
                            <Table>
                                <TableHeader>
                                    <TableRow>
                                        <TableHead>Nombre</TableHead>
                                        <TableHead>Slug</TableHead>
                                        <TableHead>Activo</TableHead>
                                        <TableHead>Acciones</TableHead>
                                    </TableRow>
                                </TableHeader>
                                <TableBody>
                                    {subcategorias.map((sub) => (
                                        <TableRow key={sub.id}>
                                            <TableCell>{sub.nombre}</TableCell>
                                            <TableCell className="font-mono text-sm">{sub.slug}</TableCell>
                                            <TableCell>{sub.activo ? 'Sí' : 'No'}</TableCell>
                                            <TableCell>
                                                <div className="flex gap-2">
                                                    <Button variant="outline" size="sm" asChild>
                                                        <Link href={`/logistica/categorias/${sub.id}`}>
                                                            <Eye className="h-4 w-4" />
                                                        </Link>
                                                    </Button>
                                                    <Button variant="outline" size="sm" asChild>
                                                        <Link href={`/logistica/categorias/${sub.id}/editar`}>
                                                            <Pencil className="h-4 w-4" />
                                                        </Link>
                                                    </Button>
                                                </div>
                                            </TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        </CardContent>
                    </Card>
                )}
            </div>

            {categoria.fechaCreacion && (
                <div className="flex gap-6 text-sm text-gray-500">
                    <span>Creada: {new Date(categoria.fechaCreacion).toLocaleDateString('es-CL', {
                        year: 'numeric', month: 'long', day: 'numeric',
                    })}</span>
                    {categoria.fechaActualizacion && (
                        <span>Actualizada: {new Date(categoria.fechaActualizacion).toLocaleDateString('es-CL', {
                            year: 'numeric', month: 'long', day: 'numeric',
                        })}</span>
                    )}
                </div>
            )}
        </div>
    );
}
