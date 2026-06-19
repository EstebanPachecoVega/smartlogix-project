'use client';

import { use, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { categoriasApi } from '@/lib/api';
import { Categoria } from '@/types';
import { buildTree, CategoriaNode, getDescendantIds, getNextOrden } from '@/lib/categoryTree';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Switch } from '@/components/ui/switch';
import { ArrowLeft, ChevronRight, ChevronDown, X } from 'lucide-react';
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
        activo: true,
    });
    const [treeExpanded, setTreeExpanded] = useState<Set<number>>(new Set());

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
                    activo: catData.activo !== undefined ? catData.activo : true,
                });
                const parents = listData.filter((c) => listData.some((d) => d.padreId === c.id));
                setTreeExpanded(new Set(parents.map((c) => c.id)));
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

    const excludeIds = categoria ? getDescendantIds(categorias, categoria.id) : [];
    const filteredCategorias = categorias.filter((c) => !excludeIds.includes(c.id));
    const tree = buildTree(filteredCategorias);

    const generarSlug = (nombre: string) => {
        return nombre
            .toLowerCase()
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .replace(/[^a-z0-9]+/g, '-')
            .replace(/^-|-$/g, '');
    };

    const selectPadre = (padreId?: number) => {
        setForm({ ...form, padreId });
    };

    const toggleTreeExpand = (id: number) => {
        setTreeExpanded((prev) => {
            const next = new Set(prev);
            if (next.has(id)) next.delete(id);
            else next.add(id);
            return next;
        });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);

        if (!form.nombre.trim()) {
            setError('El nombre es obligatorio');
            return;
        }

        const ordenVisual = form.padreId !== categoria?.padreId
            ? getNextOrden(filteredCategorias, form.padreId)
            : (categoria?.ordenVisual ?? 1);

        setSaving(true);
        try {
            const payload = {
                ...form,
                slug: form.slug.trim() || generarSlug(form.nombre),
                ordenVisual,
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

    const renderTreeNode = (node: CategoriaNode): React.ReactNode[] => {
        const hasChildren = node.children.length > 0;
        const expanded = treeExpanded.has(node.id);
        const selected = form.padreId === node.id;

        const nodes: React.ReactNode[] = [];

        nodes.push(
            <button
                key={node.id}
                type="button"
                onClick={() => selectPadre(node.id)}
                className={`w-full flex items-center gap-1 px-2 py-1.5 rounded-md text-left text-sm transition-colors
                    ${selected ? 'bg-blue-100 text-blue-700 font-medium' : 'hover:bg-gray-100'}`}
                style={{ paddingLeft: `${node.nivel * 1.5 + 0.5}rem` }}
            >
                {hasChildren ? (
                    <span
                        onClick={(e) => { e.stopPropagation(); toggleTreeExpand(node.id); }}
                        className="shrink-0 text-gray-500 hover:text-gray-700"
                    >
                        {expanded ? <ChevronDown className="h-3.5 w-3.5" /> : <ChevronRight className="h-3.5 w-3.5" />}
                    </span>
                ) : (
                    <span className="w-3.5 shrink-0" />
                )}
                <span className="truncate">{node.nombre}</span>
                {selected && <span className="ml-auto text-xs">Seleccionado</span>}
            </button>
        );

        if (hasChildren && expanded) {
            for (const child of node.children) {
                nodes.push(...renderTreeNode(child));
            }
        }

        return nodes;
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

    const selectedPadreNombre = form.padreId
        ? categorias.find((c) => c.id === form.padreId)?.nombre
        : null;

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

            <form onSubmit={handleSubmit} className="space-y-6">
                <Card>
                    <CardHeader>
                        <CardTitle>Información básica</CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-4">
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
                            <Label htmlFor="slug">Slug</Label>
                            <Input
                                id="slug"
                                value={form.slug}
                                onChange={(e) => setForm({ ...form, slug: e.target.value })}
                            />
                            <p className="text-xs text-gray-500 mt-1">Rara vez necesitas cambiarlo manualmente.</p>
                        </div>

                        <div>
                            <Label htmlFor="descripcion">Descripción</Label>
                            <Input
                                id="descripcion"
                                value={form.descripcion}
                                onChange={(e) => setForm({ ...form, descripcion: e.target.value })}
                            />
                        </div>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle>Jerarquía</CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-4">
                        <div>
                            <div className="flex items-center justify-between mb-2">
                                <Label>Categoría padre</Label>
                                {form.padreId && (
                                    <Button
                                        type="button"
                                        variant="ghost"
                                        size="sm"
                                        onClick={() => selectPadre(undefined)}
                                        className="text-xs h-7"
                                    >
                                        <X className="h-3 w-3 mr-1" /> Quitar padre
                                    </Button>
                                )}
                            </div>

                            {selectedPadreNombre && (
                                <div className="mb-2 text-sm text-blue-600 bg-blue-50 border border-blue-200 rounded-md px-3 py-1.5">
                                    Padre seleccionado: <strong>{selectedPadreNombre}</strong>
                                </div>
                            )}

                            <div className="border rounded-md max-h-60 overflow-y-auto p-1 bg-white">
                                <button
                                    type="button"
                                    onClick={() => selectPadre(undefined)}
                                    className={`w-full flex items-center px-2 py-1.5 rounded-md text-left text-sm transition-colors
                                        ${!form.padreId ? 'bg-blue-100 text-blue-700 font-medium' : 'hover:bg-gray-100'}`}
                                >
                                    <span className="text-gray-500 mr-1">—</span>
                                    <span>Ninguna (raíz)</span>
                                    {!form.padreId && <span className="ml-auto text-xs">Seleccionado</span>}
                                </button>
                                {tree.length > 0 && (
                                    <div className="border-t mt-1 pt-1">
                                        {tree.flatMap((node) => renderTreeNode(node))}
                                    </div>
                                )}
                            </div>
                            <p className="text-xs text-gray-500 mt-1">
                                Selecciona la categoría padre. La categoría actual y sus descendientes no aparecen en la lista.
                            </p>
                        </div>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle>Estado</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <div className="flex items-center justify-between rounded-md border p-3">
                            <div>
                                <Label htmlFor="activo" className="text-sm font-medium">Activo</Label>
                                <p className="text-xs text-gray-500">Las categorías inactivas no se muestran en el sitio público</p>
                            </div>
                            <Switch
                                id="activo"
                                checked={form.activo}
                                onCheckedChange={(checked) => setForm({ ...form, activo: checked })}
                            />
                        </div>
                    </CardContent>
                </Card>

                <div className="flex gap-3">
                    <Button type="submit" disabled={saving} className="flex-1">
                        {saving ? 'Guardando...' : 'Actualizar categoría'}
                    </Button>
                    <Button type="button" variant="outline" asChild className="flex-1">
                        <Link href="/logistica/categorias">Cancelar</Link>
                    </Button>
                </div>
            </form>
        </div>
    );
}