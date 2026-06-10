'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { productosApi } from '@/lib/api';
import { Producto } from '@/types';
import { Button } from '@/components/ui/button';
import { Table, TableHeader, TableRow, TableHead, TableBody, TableCell } from '@/components/ui/table';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Pencil, Trash2, Plus } from 'lucide-react';
import Link from 'next/link';
import Spinner from '@/components/shared/Spinner';

export default function ProductosPage() {
    const router = useRouter();
    const [productos, setProductos] = useState<Producto[]>([]);
    const [loading, setLoading] = useState(true);

    const cargarProductos = async () => {
        try {
            const data = await productosApi.listar();
            setProductos(data);
        } catch (error) {
            console.error(error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        cargarProductos();
    }, []);

    const handleEliminar = async (id: number) => {
        if (!confirm('¿Eliminar este producto?')) return;
        try {
            await productosApi.eliminar(id);
            cargarProductos();
        } catch (error) {
            console.error(error);
            alert('Error al eliminar producto');
        }
    };

    if (loading) return <Spinner />;

    return (
        <div>
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-2xl font-bold">Gestión de Productos</h1>
                <Button asChild>
                    <Link href="/logistica/productos/nuevo">
                        <Plus className="h-4 w-4 mr-2" /> Nuevo producto
                    </Link>
                </Button>
            </div>

            <Card>
                <CardHeader>
                    <CardTitle>Listado de productos</CardTitle>
                </CardHeader>
                <CardContent>
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead>ID</TableHead>
                                <TableHead>SKU</TableHead>
                                <TableHead>Nombre</TableHead>
                                <TableHead>Categoría</TableHead>
                                <TableHead>Precio</TableHead>
                                <TableHead>Stock</TableHead>
                                <TableHead>Activo</TableHead>
                                <TableHead>Acciones</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {productos.length === 0 ? (
                                <TableRow>
                                    <TableCell colSpan={8} className="text-center text-gray-500 py-8">
                                        No hay productos registrados.
                                    </TableCell>
                                </TableRow>
                            ) : (
                                productos.map((prod) => (
                                    <TableRow key={prod.id}>
                                        <TableCell>{prod.id}</TableCell>
                                        <TableCell>{prod.sku}</TableCell>
                                        <TableCell>{prod.nombre}</TableCell>
                                        <TableCell>{prod.categoriaNombre || '-'}</TableCell>
                                        <TableCell>${prod.precio.toLocaleString()}</TableCell>
                                        <TableCell>{prod.cantidad}</TableCell>
                                        <TableCell>{prod.activo ? 'Sí' : 'No'}</TableCell>
                                        <TableCell>
                                            <div className="flex gap-2">
                                                <Button variant="outline" size="sm" asChild>
                                                    <Link href={`/logistica/productos/${prod.id}/editar`}>
                                                        <Pencil className="h-4 w-4" />
                                                    </Link>
                                                </Button>
                                                <Button variant="destructive" size="sm" onClick={() => handleEliminar(prod.id)}>
                                                    <Trash2 className="h-4 w-4" />
                                                </Button>
                                            </div>
                                        </TableCell>
                                    </TableRow>
                                ))
                            )}
                        </TableBody>
                    </Table>
                </CardContent>
            </Card>
        </div>
    );
}
