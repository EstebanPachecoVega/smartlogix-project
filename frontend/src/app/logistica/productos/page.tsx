'use client';

import { useEffect, useState } from 'react';
import { productosApi, categoriasApi } from '@/lib/api';
import { Producto, Categoria } from '@/types';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Table, TableHeader, TableRow, TableHead, TableBody, TableCell } from '@/components/ui/table';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Select, SelectTrigger, SelectValue, SelectContent, SelectItem } from '@/components/ui/select';
import { Switch } from '@/components/ui/switch';
import { Pencil, Trash2, Plus } from 'lucide-react';
import Spinner from '@/components/shared/Spinner';

export default function ProductosPage() {
    const [productos, setProductos] = useState<Producto[]>([]);
    const [categorias, setCategorias] = useState<Categoria[]>([]);
    const [loading, setLoading] = useState(true);
    const [open, setOpen] = useState(false);
    const [editando, setEditando] = useState<Producto | null>(null);
    const [form, setForm] = useState({
        sku: '',
        nombre: '',
        slug: '',
        descripcion: '',
        categoriaId: undefined as number | undefined,
        precio: 0,
        cantidad: 0,
        imagenPrincipal: '',
        imagenes: [] as string[],
        destacado: false,
        novedad: false,
        activo: true,
    });

    const cargarProductos = async () => {
        try {
            const data = await productosApi.listar();
            setProductos(data);
        } catch (error) {
            console.error(error);
            alert('Error al cargar productos');
        } finally {
            setLoading(false);
        }
    };

    const cargarCategorias = async () => {
        try {
            const data = await categoriasApi.listar();
            setCategorias(data);
        } catch (error) {
            console.error(error);
        }
    };

    useEffect(() => {
        Promise.all([cargarProductos(), cargarCategorias()]);
    }, []);

    const generarSlug = (nombre: string) => {
        return nombre
            .toLowerCase()
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .replace(/[^a-z0-9]+/g, '-')
            .replace(/^-|-$/g, '');
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        // Validar categoría
        if (!form.categoriaId) {
            alert('Debe seleccionar una categoría');
            return;
        }

        // Generar slug automáticamente si no se proporciona
        const slug = form.slug || generarSlug(form.nombre);

        try {
            const payload = { ...form, slug };
            if (editando) {
                await productosApi.actualizar(editando.id, payload);
            } else {
                await productosApi.crear(payload);
            }
            setOpen(false);
            setEditando(null);
            setForm({
                sku: '',
                nombre: '',
                slug: '',
                descripcion: '',
                categoriaId: undefined,
                precio: 0,
                cantidad: 0,
                imagenPrincipal: '',
                imagenes: [],
                destacado: false,
                novedad: false,
                activo: true,
            });
            cargarProductos();
        } catch (error) {
            console.error(error);
            alert('Error al guardar producto');
        }
    };

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

    const abrirEditar = (prod: Producto) => {
        setEditando(prod);
        setForm({
            sku: prod.sku,
            nombre: prod.nombre,
            slug: prod.slug || '',
            descripcion: prod.descripcion || '',
            categoriaId: prod.categoriaId,
            precio: prod.precio,
            cantidad: prod.cantidad,
            imagenPrincipal: prod.imagenPrincipal || '',
            imagenes: prod.imagenes || [],
            destacado: prod.destacado || false,
            novedad: prod.novedad || false,
            activo: prod.activo !== undefined ? prod.activo : true,
        });
        setOpen(true);
    };

    if (loading) return <Spinner />;

    return (
        <div>
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-2xl font-bold">Gestión de Productos</h1>
                <Dialog open={open} onOpenChange={setOpen}>
                    <DialogTrigger asChild>
                        <Button>
                            <Plus className="h-4 w-4 mr-2" /> Nuevo producto
                        </Button>
                    </DialogTrigger>
                    <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
                        <DialogHeader>
                            <DialogTitle>{editando ? 'Editar producto' : 'Crear producto'}</DialogTitle>
                        </DialogHeader>
                        <form onSubmit={handleSubmit} className="space-y-4">
                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <Label>SKU *</Label>
                                    <Input
                                        required
                                        value={form.sku}
                                        onChange={(e) => setForm({ ...form, sku: e.target.value })}
                                    />
                                </div>
                                <div>
                                    <Label>Nombre *</Label>
                                    <Input
                                        required
                                        value={form.nombre}
                                        onChange={(e) => {
                                            const nombre = e.target.value;
                                            setForm({ ...form, nombre, slug: generarSlug(nombre) });
                                        }}
                                    />
                                </div>
                            </div>
                            <div>
                                <Label>Slug (URL amigable)</Label>
                                <Input
                                    value={form.slug}
                                    onChange={(e) => setForm({ ...form, slug: e.target.value })}
                                    disabled
                                    className="bg-gray-100"
                                />
                            </div>
                            <div>
                                <Label>Categoría *</Label>
                                <Select
                                    value={form.categoriaId?.toString() || ''}
                                    onValueChange={(value) => setForm({ ...form, categoriaId: parseInt(value) })}
                                >
                                    <SelectTrigger>
                                        <SelectValue placeholder="Seleccione una categoría" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        {categorias.map((cat) => (
                                            <SelectItem key={cat.id} value={cat.id.toString()}>
                                                {cat.nombre}
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                            </div>
                            <div>
                                <Label>Descripción</Label>
                                <Input
                                    value={form.descripcion}
                                    onChange={(e) => setForm({ ...form, descripcion: e.target.value })}
                                />
                            </div>
                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <Label>Precio *</Label>
                                    <Input
                                        type="number"
                                        required
                                        value={form.precio}
                                        onChange={(e) => setForm({ ...form, precio: parseInt(e.target.value) })}
                                    />
                                </div>
                                <div>
                                    <Label>Stock *</Label>
                                    <Input
                                        type="number"
                                        required
                                        value={form.cantidad}
                                        onChange={(e) => setForm({ ...form, cantidad: parseInt(e.target.value) })}
                                    />
                                </div>
                            </div>
                            <div>
                                <Label>Imagen principal (URL)</Label>
                                <Input
                                    value={form.imagenPrincipal}
                                    onChange={(e) => setForm({ ...form, imagenPrincipal: e.target.value })}
                                    placeholder="https://..."
                                />
                            </div>
                            <div>
                                <Label>Imágenes adicionales (URLs separadas por coma)</Label>
                                <Input
                                    value={form.imagenes.join(', ')}
                                    onChange={(e) => setForm({ ...form, imagenes: e.target.value.split(',').map(s => s.trim()) })}
                                    placeholder="https://..., https://..."
                                />
                            </div>
                            <div className="grid grid-cols-3 gap-4">
                                <div className="flex items-center justify-between">
                                    <Label>Destacado</Label>
                                    <Switch
                                        checked={form.destacado}
                                        onCheckedChange={(checked) => setForm({ ...form, destacado: checked })}
                                    />
                                </div>
                                <div className="flex items-center justify-between">
                                    <Label>Novedad</Label>
                                    <Switch
                                        checked={form.novedad}
                                        onCheckedChange={(checked) => setForm({ ...form, novedad: checked })}
                                    />
                                </div>
                                <div className="flex items-center justify-between">
                                    <Label>Activo</Label>
                                    <Switch
                                        checked={form.activo}
                                        onCheckedChange={(checked) => setForm({ ...form, activo: checked })}
                                    />
                                </div>
                            </div>
                            <Button type="submit" className="w-full">
                                {editando ? 'Actualizar' : 'Crear'} producto
                            </Button>
                        </form>
                    </DialogContent>
                </Dialog>
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
                            {productos.map((prod) => (
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
                                            <Button variant="outline" size="sm" onClick={() => abrirEditar(prod)}>
                                                <Pencil className="h-4 w-4" />
                                            </Button>
                                            <Button variant="destructive" size="sm" onClick={() => handleEliminar(prod.id)}>
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