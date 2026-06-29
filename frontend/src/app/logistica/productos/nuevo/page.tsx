'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { productosApi, categoriasApi } from '@/lib/api';
import { Categoria } from '@/types';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Switch } from '@/components/ui/switch';
import ImageUploader from '@/components/ui/ImageUploader';
import { ArrowLeft } from 'lucide-react';
import Link from 'next/link';
import Spinner from '@/components/shared/Spinner';
import { CategoryCombobox } from '@/components/ui/Categorycombobox';

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

    const generarSlug = (nombre: string) =>
        nombre
            .toLowerCase()
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .replace(/[^a-z0-9]+/g, '-')
            .replace(/^-|-$/g, '');

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);

        if (form.precio < 0 || form.cantidad < 0) {
            setError('Precio y stock deben ser valores positivos');
            return;
        }

        setSaving(true);
        try {
            await productosApi.crear({
                ...form,
                slug: form.slug || generarSlug(form.nombre),
            });
            router.push('/logistica/productos');
        } catch (err: any) {
            setError(
                err?.response?.data?.detail ??
                err?.message ??
                'Error al crear el producto',
            );
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
                        {/* SKU + Nombre */}
                        <div className="grid grid-cols-2 gap-4">
                            <div className="space-y-1.5">
                                <Label htmlFor="sku">SKU *</Label>
                                <Input
                                    id="sku"
                                    required
                                    value={form.sku}
                                    onChange={(e) => setForm({ ...form, sku: e.target.value })}
                                    placeholder="SKU-001"
                                />
                            </div>
                            <div className="space-y-1.5">
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

                        {/* Slug (read-only) */}
                        <div className="space-y-1.5">
                            <Label htmlFor="slug">Slug (URL amigable)</Label>
                            <Input
                                id="slug"
                                value={form.slug}
                                disabled
                                className="bg-muted text-muted-foreground"
                            />
                            <p className="text-xs text-muted-foreground">
                                Se genera automáticamente desde el nombre
                            </p>
                        </div>

                        {/* Categoría — Base UI Combobox, sin bloqueo de scroll */}
                        <div className="space-y-1.5">
                            <Label>Categoría</Label>
                            <CategoryCombobox
                                categorias={categorias}
                                value={form.categoriaId}
                                onChange={(id) => setForm({ ...form, categoriaId: id })}
                            />
                            {categorias.length > 0 && (
                                <p className="text-xs text-muted-foreground">
                                    Escribe para filtrar entre las {categorias.length} categorías.
                                </p>
                            )}
                        </div>

                        {/* Descripción */}
                        <div className="space-y-1.5">
                            <Label htmlFor="descripcion">Descripción</Label>
                            <Input
                                id="descripcion"
                                value={form.descripcion}
                                onChange={(e) => setForm({ ...form, descripcion: e.target.value })}
                                placeholder="Descripción del producto"
                            />
                        </div>

                        {/* Precio + Stock */}
                        <div className="grid grid-cols-2 gap-4">
                            <div className="space-y-1.5">
                                <Label htmlFor="precio">Precio *</Label>
                                <Input
                                    id="precio"
                                    type="number"
                                    required
                                    min="0"
                                    value={form.precio}
                                    onChange={(e) =>
                                        setForm({ ...form, precio: parseInt(e.target.value) || 0 })
                                    }
                                />
                            </div>
                            <div className="space-y-1.5">
                                <Label htmlFor="cantidad">Stock *</Label>
                                <Input
                                    id="cantidad"
                                    type="number"
                                    required
                                    min="0"
                                    value={form.cantidad}
                                    onChange={(e) =>
                                        setForm({ ...form, cantidad: parseInt(e.target.value) || 0 })
                                    }
                                />
                            </div>
                        </div>

                        {/* Imágenes */}
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <ImageUploader
                                mode="single"
                                value={form.imagenPrincipal}
                                onChange={(val) =>
                                    setForm({ ...form, imagenPrincipal: val as string })
                                }
                                label="Imagen principal"
                            />
                            <ImageUploader
                                mode="multiple"
                                value={form.imagenes}
                                onChange={(val) =>
                                    setForm({ ...form, imagenes: val as string[] })
                                }
                                label="Imágenes adicionales"
                            />
                        </div>

                        {/* Toggles: Destacado / Novedad / Activo */}
                        <div className="grid grid-cols-3 gap-4 pt-2">
                            {(
                                [
                                    { id: 'destacado', label: 'Destacado' },
                                    { id: 'novedad', label: 'Novedad' },
                                    { id: 'activo', label: 'Activo' },
                                ] as const
                            ).map(({ id, label }) => (
                                <div
                                    key={id}
                                    className="flex items-center justify-between rounded-md border p-3"
                                >
                                    <Label htmlFor={id} className="text-sm">
                                        {label}
                                    </Label>
                                    <Switch
                                        id={id}
                                        checked={form[id]}
                                        onCheckedChange={(checked) =>
                                            setForm({ ...form, [id]: checked })
                                        }
                                    />
                                </div>
                            ))}
                        </div>

                        {/* Acciones */}
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