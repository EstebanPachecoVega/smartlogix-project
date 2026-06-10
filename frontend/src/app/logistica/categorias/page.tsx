'use client';

import { useCallback, useEffect, useState } from 'react';
import Link from 'next/link';
import { categoriasApi } from '@/lib/api';
import { Categoria } from '@/types';
import { Button } from '@/components/ui/button';
import { Table, TableHeader, TableRow, TableHead, TableBody, TableCell } from '@/components/ui/table';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Eye, Pencil, Trash2, Plus } from 'lucide-react';
import Spinner from '@/components/shared/Spinner';

export default function CategoriasPage() {
    const [categorias, setCategorias] = useState<Categoria[]>([]);
    const [loading, setLoading] = useState(true);

    const cargarCategorias = useCallback(async () => {
        try {
            const data = await categoriasApi.listar();
            setCategorias(data);
        } catch (error) {
            console.error(error);
            alert('Error al cargar categorías');
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        cargarCategorias();
    }, [cargarCategorias]);

    const handleEliminar = async (id: number) => {
        if (!confirm('¿Eliminar esta categoría? Se eliminarán también las subcategorías vacías.')) return;
        try {
            await categoriasApi.eliminar(id);
            cargarCategorias();
        } catch (error: any) {
            console.error(error);
            alert(error.response?.data?.detail || 'Error al eliminar categoría');
        }
    };

    if (loading) return <Spinner />;

    return (
        <div>
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-2xl font-bold">Gestión de Categorías</h1>
                <Button asChild>
                    <Link href="/logistica/categorias/nuevo">
                        <Plus className="h-4 w-4 mr-2" /> Nueva categoría
                    </Link>
                </Button>
            </div>

            <Card>
                <CardHeader>
                    <CardTitle>Listado de categorías</CardTitle>
                </CardHeader>
                <CardContent>
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead>ID</TableHead>
                                <TableHead>Nombre</TableHead>
                                <TableHead>Slug</TableHead>
                                <TableHead>Categoría padre</TableHead>
                                <TableHead>Activo</TableHead>
                                <TableHead>Acciones</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {categorias.map((cat) => (
                                <TableRow key={cat.id}>
                                    <TableCell>{cat.id}</TableCell>
                                    <TableCell>{cat.nombre}</TableCell>
                                    <TableCell>{cat.slug}</TableCell>
                                    <TableCell>{cat.padreNombre || '-'}</TableCell>
                                    <TableCell>{cat.activo ? 'Sí' : 'No'}</TableCell>
                                    <TableCell>
                                        <div className="flex gap-2">
                                            <Button variant="outline" size="sm" asChild>
                                                <Link href={`/logistica/categorias/${cat.id}`}>
                                                    <Eye className="h-4 w-4" />
                                                </Link>
                                            </Button>
                                            <Button variant="outline" size="sm" asChild>
                                                <Link href={`/logistica/categorias/${cat.id}/editar`}>
                                                    <Pencil className="h-4 w-4" />
                                                </Link>
                                            </Button>
                                            <Button variant="destructive" size="sm" onClick={() => handleEliminar(cat.id)}>
                                                <Trash2 className="h-4 w-4" />
                                            </Button>
                                        </div>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </CardContent>
            </Card>
        </div>
    );
}