'use client';

import { use, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { categoriasApi } from '@/lib/api';
import { Categoria } from '@/types';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Switch } from '@/components/ui/switch';
import { ArrowLeft } from 'lucide-react';
import Link from 'next/link';
import Spinner from '@/components/shared/Spinner';

interface EditPageProps {
    params: Promise<{ id: string }>;
}

export default function EditarCategoriaPage({ params }: EditPageProps) {
    const { id } = use(params);
    const router = useRouter();
    const [categoria, setCategoria] = useState<Categoria | null>(null);
    const [categorias, setCategorias] = useState<Categoria[]>([]);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [notFound, setNotFound] = useState(false);
    const [form, setForm] = useState({
        nombre: '',
        slug: '',
        descripcion: '',
        padreId: undefined as number | undefined,
        ordenVisual: 0,
        activo: true,
    });

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [catData, listData] = await Promise.all([
                    categoriasApi.obtener(parseInt(id)),
                    categoriasApi.listar(),
                ]);
                setCategoria(catData);
                setCategorias(listData);
                setForm({
                    nombre: catData.nombre || '',
                    slug: catData.slug || '',
                    descripcion: catData.descripcion || '',
                    padreId: catData.padreId,
                    ordenVisual: catData.ordenVisual || 0,
                    activo: catData.activo !== undefined ? catData.activo : true,
                });
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

        if (!form.nombre.trim()) {
            setError('El nombre es obligatorio');
            return;
        }

        setSaving(true);
        try {
            const payload = {
                ...form,
                slug: form.slug.trim() || generarSlug(form.nombre),
            };
            await categoriasApi.actualizar(parseInt(id), payload);
            router.push('/logistica/categorias');
        } catch (err: any) {
            const mensaje = err?.response?.data?.detail || err?.message || 'Error al actualizar la categoría';
            setError(mensaje);
        } finally {
            setSaving(false);
        }
    };

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
                    La categoría que intentas editar no existe o fue eliminada.
                </div>
            </div>
        );
    }

    return (
        <div className="max-w-2xl mx-auto space-y-6">
            <div className="flex items-center gap-4">
                <Button variant="ghost" size="sm" asChild>
                    <Link href="/logistica/categorias">
                        <ArrowLeft className="h-4 w-4 mr-2" /> Volver
                    </Link>
                </Button>
                <h1 className="text-2xl font-bold">Editar Categoría</h1>
                {categoria && (
                    <span className="text-sm text-gray-500">{categoria.nombre}</span>
                )}
            </div>

            {error && (
                <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md text-sm">
                    {error}
                </div>
            )}

            <Card>
                <CardHeader>
                    <CardTitle>Información de la categoría</CardTitle>
                </CardHeader>
                <CardContent>
                    <form onSubmit={handleSubmit} className="space-y-4">
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
                            />
                        </div>

                        <div>
                            <Label htmlFor="slug">Slug *</Label>
                            <Input
                                id="slug"
                                required
                                value={form.slug}
                                onChange={(e) => setForm({ ...form, slug: e.target.value })}
                            />
                        </div>

                        <div>
                            <Label htmlFor="descripcion">Descripción</Label>
                            <Input
                                id="descripcion"
                                value={form.descripcion}
                                onChange={(e) => setForm({ ...form, descripcion: e.target.value })}
                            />
                        </div>

                        <div>
                            <Label htmlFor="padreId">Categoría padre</Label>
                            <select
                                id="padreId"
                                className="w-full border rounded-md p-2 bg-white"
                                value={form.padreId || ''}
                                onChange={(e) => setForm({ ...form, padreId: e.target.value ? parseInt(e.target.value) : undefined })}
                            >
                                <option value="">Ninguna (raíz)</option>
                                {categorias.filter(c => c.id !== categoria?.id).map((cat) => (
                                    <option key={cat.id} value={cat.id}>{cat.nombre}</option>
                                ))}
                            </select>
                        </div>

                        <div>
                            <Label htmlFor="ordenVisual">Orden visual</Label>
                            <Input
                                id="ordenVisual"
                                type="number"
                                min="0"
                                value={form.ordenVisual}
                                onChange={(e) => setForm({ ...form, ordenVisual: parseInt(e.target.value) || 0 })}
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

                        <div className="flex gap-3 pt-4">
                            <Button type="submit" disabled={saving} className="flex-1">
                                {saving ? 'Guardando...' : 'Actualizar categoría'}
                            </Button>
                            <Button type="button" variant="outline" asChild className="flex-1">
                                <Link href="/logistica/categorias">Cancelar</Link>
                            </Button>
                        </div>
                    </form>
                </CardContent>
            </Card>
        </div>
    );
}