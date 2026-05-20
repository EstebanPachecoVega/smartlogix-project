'use client';

import { useEffect, useState } from 'react';
import { categoriasApi } from '@/lib/api';
import { Categoria } from '@/types';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Table, TableHeader, TableRow, TableHead, TableBody, TableCell } from '@/components/ui/table';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Switch } from '@/components/ui/switch';
import { Pencil, Trash2, Plus } from 'lucide-react';
import Spinner from '@/components/shared/Spinner';

export default function CategoriasPage() {
    const [categorias, setCategorias] = useState<Categoria[]>([]);
    const [loading, setLoading] = useState(true);
    const [open, setOpen] = useState(false);
    const [editando, setEditando] = useState<Categoria | null>(null);
    const [form, setForm] = useState({
        nombre: '',
        slug: '',
        descripcion: '',
        padreId: undefined as number | undefined,
        ordenVisual: 0,
        activo: true,
    });

    const cargarCategorias = async () => {
        try {
            const data = await categoriasApi.listar();
            setCategorias(data);
        } catch (error) {
            console.error(error);
            alert('Error al cargar categorías');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        cargarCategorias();
    }, []);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            if (editando) {
                await categoriasApi.actualizar(editando.id, form);
            } else {
                await categoriasApi.crear(form);
            }
            setOpen(false);
            setEditando(null);
            setForm({ nombre: '', slug: '', descripcion: '', padreId: undefined, ordenVisual: 0, activo: true });
            cargarCategorias();
        } catch (error) {
            console.error(error);
            alert('Error al guardar categoría');
        }
    };

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

    const abrirEditar = (cat: Categoria) => {
        setEditando(cat);
        setForm({
            nombre: cat.nombre,
            slug: cat.slug,
            descripcion: cat.descripcion || '',
            padreId: cat.padreId,
            ordenVisual: cat.ordenVisual || 0,
            activo: cat.activo,
        });
        setOpen(true);
    };

    if (loading) return <Spinner />;

    return (
        <div>
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-2xl font-bold">Gestión de Categorías</h1>
                <Dialog open={open} onOpenChange={setOpen}>
                    <DialogTrigger asChild>
                        <Button>
                            <Plus className="h-4 w-4 mr-2" /> Nueva categoría
                        </Button>
                    </DialogTrigger>
                    <DialogContent>
                        <DialogHeader>
                            <DialogTitle>{editando ? 'Editar categoría' : 'Crear categoría'}</DialogTitle>
                        </DialogHeader>
                        <form onSubmit={handleSubmit} className="space-y-4">
                            <div>
                                <Label>Nombre *</Label>
                                <Input
                                    required
                                    value={form.nombre}
                                    onChange={(e) => setForm({ ...form, nombre: e.target.value })}
                                />
                            </div>
                            <div>
                                <Label>Slug *</Label>
                                <Input
                                    required
                                    value={form.slug}
                                    onChange={(e) => setForm({ ...form, slug: e.target.value })}
                                />
                            </div>
                            <div>
                                <Label>Descripción</Label>
                                <Input
                                    value={form.descripcion}
                                    onChange={(e) => setForm({ ...form, descripcion: e.target.value })}
                                />
                            </div>
                            <div>
                                <Label>Categoría padre</Label>
                                <select
                                    className="w-full border rounded-md p-2"
                                    value={form.padreId || ''}
                                    onChange={(e) => setForm({ ...form, padreId: e.target.value ? parseInt(e.target.value) : undefined })}
                                >
                                    <option value="">Ninguna (raíz)</option>
                                    {categorias.filter(c => c.id !== editando?.id).map((cat) => (
                                        <option key={cat.id} value={cat.id}>{cat.nombre}</option>
                                    ))}
                                </select>
                            </div>
                            <div>
                                <Label>Orden visual</Label>
                                <Input
                                    type="number"
                                    value={form.ordenVisual}
                                    onChange={(e) => setForm({ ...form, ordenVisual: parseInt(e.target.value) || 0 })}
                                />
                            </div>
                            <div className="flex items-center justify-between">
                                <Label>Activo</Label>
                                <Switch
                                    checked={form.activo}
                                    onCheckedChange={(checked) => setForm({ ...form, activo: checked })}
                                />
                            </div>
                            <Button type="submit" className="w-full">Guardar</Button>
                        </form>
                    </DialogContent>
                </Dialog>
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
                                            <Button variant="outline" size="sm" onClick={() => abrirEditar(cat)}>
                                                <Pencil className="h-4 w-4" />
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