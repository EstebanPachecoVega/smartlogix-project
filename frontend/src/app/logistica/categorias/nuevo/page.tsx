'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { categoriasApi } from '@/lib/api';
import { Categoria } from '@/types';
import { buildTree, CategoriaNode, getNextOrden } from '@/lib/categoryTree';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Switch } from '@/components/ui/switch';
import { ArrowLeft, ChevronRight, ChevronDown, X } from 'lucide-react';
import Link from 'next/link';
import Spinner from '@/components/shared/Spinner';

export default function NuevaCategoriaPage() {
    const router = useRouter();
    const [categorias, setCategorias] = useState<Categoria[]>([]);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [form, setForm] = useState({
        nombre: '',
        slug: '',
        descripcion: '',
        padreId: undefined as number | undefined,
        activo: true,
    });
    const [treeExpanded, setTreeExpanded] = useState<Set<number>>(new Set());

    useEffect(() => {
        categoriasApi.listar()
            .then((data) => {
                setCategorias(data);
                const parents = data.filter((c) => data.some((d) => d.padreId === c.id));
                setTreeExpanded(new Set(parents.map((c) => c.id)));
            })
            .catch(() => setError('Error al cargar categorías'))
            .finally(() => setLoading(false));
    }, []);

    const tree = buildTree(categorias);

    const generarSlug = (nombre: string) => {
        return nombre
            .toLowerCase()
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .replace(/[^a-z0-9]+/g, '-')
            .replace(/^-|-$/g, '');
    };

    const selectPadre = (id?: number) => {
        setForm({ ...form, padreId: id });
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

        const ordenVisual = getNextOrden(categorias, form.padreId);

        setSaving(true);
        try {
            const payload = {
                ...form,
                slug: form.slug.trim() || generarSlug(form.nombre),
                ordenVisual,
            };
            await categoriasApi.crear(payload);
            router.push('/logistica/categorias');
        } catch (err: any) {
            const mensaje = err?.response?.data?.detail || err?.message || 'Error al crear la categoría';
            setError(mensaje);
        } finally {
            setSaving(false);
        }
    };

    const renderTreeNode = (node: CategoriaNode) => {
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
                    ${selected ? 'bg-primary/15 text-primary font-medium' : 'hover:bg-accent'}`}
                style={{ paddingLeft: `${node.nivel * 1.5 + 0.5}rem` }}
            >
                {hasChildren ? (
                    <span
                        onClick={(e) => { e.stopPropagation(); toggleTreeExpand(node.id); }}
                        className="shrink-0 text-muted-foreground hover:text-foreground"
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
                <h1 className="text-2xl font-bold">Nueva Categoría</h1>
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
                                placeholder="Ej: Electrónicos, Ropa, Hogar"
                            />
                        </div>

                        <div>
                            <Label htmlFor="slug">Slug</Label>
                            <Input
                                id="slug"
                                value={form.slug}
                                onChange={(e) => setForm({ ...form, slug: e.target.value })}
                                placeholder="categoria-ejemplo"
                            />
                            <p className="text-xs text-muted-foreground mt-1">Se genera automáticamente desde el nombre. Cámbialo solo si es necesario.</p>
                        </div>

                        <div>
                            <Label htmlFor="descripcion">Descripción</Label>
                            <Input
                                id="descripcion"
                                value={form.descripcion}
                                onChange={(e) => setForm({ ...form, descripcion: e.target.value })}
                                placeholder="Descripción breve de la categoría"
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
                                <div className="mb-2 text-sm text-primary bg-primary/10 border border-primary/20 rounded-md px-3 py-1.5">
                                    Padre seleccionado: <strong>{selectedPadreNombre}</strong>
                                </div>
                            )}

                            <div className="border rounded-md max-h-60 overflow-y-auto p-1 bg-card">
                                <button
                                    type="button"
                                    onClick={() => selectPadre(undefined)}
                                    className={`w-full flex items-center px-2 py-1.5 rounded-md text-left text-sm transition-colors
                                        ${!form.padreId ? 'bg-primary/15 text-primary font-medium' : 'hover:bg-accent'}`}
                                >
                                    <span className="text-muted-foreground mr-1">—</span>
                                    <span>Ninguna (raíz)</span>
                                    {!form.padreId && <span className="ml-auto text-xs">Seleccionado</span>}
                                </button>
                                {tree.length > 0 && (
                                    <div className="border-t mt-1 pt-1">
                                        {tree.flatMap((node) => renderTreeNode(node))}
                                    </div>
                                )}
                            </div>
                            <p className="text-xs text-muted-foreground mt-1">Haz clic en una categoría para asignarla como padre. La nueva categoría aparecerá como subcategoría de la seleccionada.</p>
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
                                <p className="text-xs text-muted-foreground">Las categorías inactivas no se muestran en el sitio público</p>
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
                        {saving ? 'Guardando...' : 'Crear categoría'}
                    </Button>
                    <Button type="button" variant="outline" asChild className="flex-1">
                        <Link href="/logistica/categorias">Cancelar</Link>
                    </Button>
                </div>
            </form>
        </div>
    );
}