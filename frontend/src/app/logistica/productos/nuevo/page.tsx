'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { productosApi, categoriasApi } from '@/lib/api';
import { Producto, Categoria } from '@/types';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Select, SelectTrigger, SelectValue, SelectContent, SelectItem } from '@/components/ui/select';
import { Switch } from '@/components/ui/switch';
import { ArrowLeft } from 'lucide-react';
import Link from 'next/link';
import Spinner from '@/components/shared/Spinner';

export default function NuevoProductoPage() {
    const router = useRouter();
    const [categorias, setCategorias] = useState<Categoria[]>([]);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState<string | null>(null);
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

    useEffect(() => {
        categoriasApi.listar()
            .then(setCategorias)
            .catch(() => setError('Error al cargar categorías'))
            .finally(() => setLoading(false));
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
        setError(null);

        if (!form.categoriaId) {
            setError('Debe seleccionar una categoría');
            return;
        }
        if (form.precio < 0 || form.cantidad < 0) {
            setError('Precio y stock deben ser valores positivos');
            return;
        }

        setSaving(true);
        try {
            const payload = {
                ...form,
                slug: form.slug || generarSlug(form.nombre),
            };
            await productosApi.crear(payload);
            router.push('/logistica/productos');
        } catch (err: any) {
            const mensaje = err?.response?.data?.detail || err?.message || 'Error al crear el producto';
            setError(mensaje);
        } finally {
            setSaving(false);
        }
    };

    if (loading) return <Spinner />;

    return (
        <div className="max-w-3xl mx-auto space-y-6">
            <div className="flex items-center gap-4">
                <Button variant="ghost" size="sm" asChild>
                    <Link href="/logistica/productos">
                        <ArrowLeft className="h-4 w-4 mr-2" /> Volver
                    </Link>
                </Button>
                <h1 className="text-2xl font-bold">Nuevo Producto</h1>
            </div>

            {error && (
                <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md text-sm">
                    {error}
                </div>
            )}

            <Card>
                <CardHeader>
                    <CardTitle>Información del producto</CardTitle>
                </CardHeader>
                <CardContent>
                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <Label htmlFor="sku">SKU *</Label>
                                <Input
                                    id="sku"
                                    required
                                    value={form.sku}
                                    onChange={(e) => setForm({ ...form, sku: e.target.value })}
                                    placeholder="SKU-001"
                                />
                            </div>
                            <div>
                                <Label htmlFor="nombre">Nombre *</Label>
                                <Input
                                    id="nombre"
                                    required
                                    value={form.nombre}
                                    onChange={(e) => {
                                        const nombre = e.target.value;
                                        setForm({ ...form, nombre, slug: generarSlug(nombre) });
                                    }}
                                    placeholder="Nombre del producto"
                                />
                            </div>
                        </div>

                        <div>
                            <Label htmlFor="slug">Slug (URL amigable)</Label>
                            <Input
                                id="slug"
                                value={form.slug}
                                disabled
                                className="bg-gray-100"
                            />
                            <p className="text-xs text-gray-500 mt-1">Se genera automáticamente desde el nombre</p>
                        </div>

                        <div>
                            <Label htmlFor="categoria">Categoría *</Label>
                            <Select
                                value={form.categoriaId?.toString() || ''}
                                onValueChange={(value) => setForm({ ...form, categoriaId: parseInt(value) })}
                            >
                                <SelectTrigger id="categoria">
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
                            <Label htmlFor="descripcion">Descripción</Label>
                            <Input
                                id="descripcion"
                                value={form.descripcion}
                                onChange={(e) => setForm({ ...form, descripcion: e.target.value })}
                                placeholder="Descripción del producto"
                            />
                        </div>

                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <Label htmlFor="precio">Precio *</Label>
                                <Input
                                    id="precio"
                                    type="number"
                                    required
                                    min="0"
                                    value={form.precio}
                                    onChange={(e) => setForm({ ...form, precio: parseInt(e.target.value) || 0 })}
                                />
                            </div>
                            <div>
                                <Label htmlFor="cantidad">Stock *</Label>
                                <Input
                                    id="cantidad"
                                    type="number"
                                    required
                                    min="0"
                                    value={form.cantidad}
                                    onChange={(e) => setForm({ ...form, cantidad: parseInt(e.target.value) || 0 })}
                                />
                            </div>
                        </div>

                        <div>
                            <Label htmlFor="imagenPrincipal">Imagen principal (URL)</Label>
                            <Input
                                id="imagenPrincipal"
                                value={form.imagenPrincipal}
                                onChange={(e) => setForm({ ...form, imagenPrincipal: e.target.value })}
                                placeholder="https://..."
                            />
                        </div>

                        <div>
                            <Label htmlFor="imagenes">Imágenes adicionales (URLs separadas por coma)</Label>
                            <Input
                                id="imagenes"
                                value={form.imagenes.join(', ')}
                                onChange={(e) => setForm({ ...form, imagenes: e.target.value.split(',').map(s => s.trim()).filter(Boolean) })}
                                placeholder="https://..., https://..."
                            />
                        </div>

                        <div className="grid grid-cols-3 gap-4 pt-2">
                            <div className="flex items-center justify-between rounded-md border p-3">
                                <Label htmlFor="destacado" className="text-sm">Destacado</Label>
                                <Switch
                                    id="destacado"
                                    checked={form.destacado}
                                    onCheckedChange={(checked) => setForm({ ...form, destacado: checked })}
                                />
                            </div>
                            <div className="flex items-center justify-between rounded-md border p-3">
                                <Label htmlFor="novedad" className="text-sm">Novedad</Label>
                                <Switch
                                    id="novedad"
                                    checked={form.novedad}
                                    onCheckedChange={(checked) => setForm({ ...form, novedad: checked })}
                                />
                            </div>
                            <div className="flex items-center justify-between rounded-md border p-3">
                                <Label htmlFor="activo" className="text-sm">Activo</Label>
                                <Switch
                                    id="activo"
                                    checked={form.activo}
                                    onCheckedChange={(checked) => setForm({ ...form, activo: checked })}
                                />
                            </div>
                        </div>

                        <div className="flex gap-3 pt-4">
                            <Button type="submit" disabled={saving} className="flex-1">
                                {saving ? 'Guardando...' : 'Crear producto'}
                            </Button>
                            <Button type="button" variant="outline" asChild className="flex-1">
                                <Link href="/logistica/productos">Cancelar</Link>
                            </Button>
                        </div>
                    </form>
                </CardContent>
            </Card>
        </div>
    );
}