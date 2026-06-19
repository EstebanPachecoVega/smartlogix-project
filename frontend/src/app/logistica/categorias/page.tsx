'use client';

import { useCallback, useEffect, useState } from 'react';
import Link from 'next/link';
import { categoriasApi } from '@/lib/api';
import { Categoria } from '@/types';
import { buildTree, CategoriaNode, flattenTree, getSiblings } from '@/lib/categoryTree';
import { Button } from '@/components/ui/button';
import { Table, TableHeader, TableRow, TableHead, TableBody, TableCell } from '@/components/ui/table';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { ChevronRight, ChevronDown, Eye, Pencil, Trash2, Plus, GripVertical } from 'lucide-react';
import Spinner from '@/components/shared/Spinner';

export default function CategoriasPage() {
    const [categorias, setCategorias] = useState<Categoria[]>([]);
    const [loading, setLoading] = useState(true);
    const [expanded, setExpanded] = useState<Set<number>>(new Set());
    const [dragId, setDragId] = useState<number | null>(null);
    const [dropId, setDropId] = useState<number | null>(null);

    const cargarCategorias = useCallback(async () => {
        try {
            const data = await categoriasApi.listar();
            setCategorias(data);
            const parents = data.filter((c) => data.some((d) => d.padreId === c.id));
            setExpanded(new Set(parents.map((c) => c.id)));
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

    const tree = buildTree(categorias);

    const toggleExpand = (id: number) => {
        setExpanded((prev) => {
            const next = new Set(prev);
            if (next.has(id)) next.delete(id);
            else next.add(id);
            return next;
        });
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

    const handleDragStart = (e: React.DragEvent, id: number) => {
        e.dataTransfer.effectAllowed = 'move';
        setDragId(id);
    };

    const handleDragOver = (e: React.DragEvent, id: number) => {
        e.preventDefault();
        e.dataTransfer.dropEffect = 'move';
        setDropId(id);
    };

    const handleDrop = () => {
        if (dragId === null || dropId === null || dragId === dropId) {
            setDragId(null);
            setDropId(null);
            return;
        }

        const dragged = categorias.find((c) => c.id === dragId);
        const target = categorias.find((c) => c.id === dropId);
        if (!dragged || !target || dragged.padreId !== target.padreId) {
            setDragId(null);
            setDropId(null);
            return;
        }

        const siblings = getSiblings(categorias, dragged.padreId);
        const sortedIds = siblings.map((c) => c.id);
        const fromIdx = sortedIds.indexOf(dragId);
        const toIdx = sortedIds.indexOf(dropId);
        if (fromIdx === -1 || toIdx === -1) {
            setDragId(null);
            setDropId(null);
            return;
        }

        sortedIds.splice(fromIdx, 1);
        sortedIds.splice(toIdx, 0, dragId);

        const ordenes = sortedIds.map((id, i) => ({ id, ordenVisual: i + 1 }));

        categoriasApi.reordenar(ordenes).then(() => {
            cargarCategorias();
        }).catch(() => {
            alert('Error al reordenar categorías');
        });

        setDragId(null);
        setDropId(null);
    };

    const handleDragEnd = () => {
        setDragId(null);
        setDropId(null);
    };

    const renderNode = (node: CategoriaNode) => {
        const hasChildren = node.children.length > 0;
        const isExpanded = expanded.has(node.id);
        const visible = true;

        const rows: React.ReactNode[] = [];

        rows.push(
            <TableRow
                key={node.id}
                draggable
                onDragStart={(e) => handleDragStart(e, node.id)}
                onDragOver={(e) => handleDragOver(e, node.id)}
                onDrop={handleDrop}
                onDragEnd={handleDragEnd}
                className={`transition-all ${dragId === node.id ? 'opacity-50' : ''} ${dropId === node.id && dragId !== node.id ? 'border-t-2 border-blue-500' : ''}`}
            >
                <TableCell>
                    <div className="flex items-center gap-1" style={{ paddingLeft: `${node.nivel * 1.5}rem` }}>
                        <span className="text-gray-400 cursor-grab active:cursor-grabbing shrink-0">
                            <GripVertical className="h-4 w-4" />
                        </span>
                        {hasChildren ? (
                            <button
                                type="button"
                                onClick={() => toggleExpand(node.id)}
                                className="shrink-0 text-gray-500 hover:text-gray-700"
                            >
                                {isExpanded ? <ChevronDown className="h-4 w-4" /> : <ChevronRight className="h-4 w-4" />}
                            </button>
                        ) : (
                            <span className="w-4 shrink-0" />
                        )}
                        <span className="font-medium truncate">{node.nombre}</span>
                        {!node.activo && (
                            <span className="bg-red-100 text-red-700 text-xs px-1.5 py-0.5 rounded-full shrink-0">Inactiva</span>
                        )}
                    </div>
                </TableCell>
                <TableCell className="font-mono text-sm">{node.slug}</TableCell>
                <TableCell>{node.padreNombre || '—'}</TableCell>
                <TableCell>{node.ordenVisual ?? '—'}</TableCell>
                <TableCell>{node.activo ? 'Sí' : 'No'}</TableCell>
                <TableCell>
                    <div className="flex gap-2">
                        <Button variant="outline" size="sm" asChild>
                            <Link href={`/logistica/categorias/${node.id}`} aria-label="Ver categoría">
                                <Eye className="h-4 w-4" />
                            </Link>
                        </Button>
                        <Button variant="outline" size="sm" asChild>
                            <Link href={`/logistica/categorias/${node.id}/editar`} aria-label="Editar categoría">
                                <Pencil className="h-4 w-4" />
                            </Link>
                        </Button>
                        <Button variant="destructive" size="sm" onClick={() => handleEliminar(node.id)}>
                            <Trash2 className="h-4 w-4" />
                        </Button>
                    </div>
                </TableCell>
            </TableRow>
        );

        if (hasChildren && isExpanded) {
            for (const child of node.children) {
                rows.push(...renderChildRows(child));
            }
        }

        return rows;
    };

    const renderChildRows = (node: CategoriaNode): React.ReactNode[] => {
        const hasChildren = node.children.length > 0;
        const isExpanded = expanded.has(node.id);
        const rows: React.ReactNode[] = [];

        rows.push(
            <TableRow
                key={node.id}
                draggable
                onDragStart={(e) => handleDragStart(e, node.id)}
                onDragOver={(e) => handleDragOver(e, node.id)}
                onDrop={handleDrop}
                onDragEnd={handleDragEnd}
                className={`transition-all ${dragId === node.id ? 'opacity-50' : ''} ${dropId === node.id && dragId !== node.id ? 'border-t-2 border-blue-500' : ''}`}
            >
                <TableCell>
                    <div className="flex items-center gap-1" style={{ paddingLeft: `${node.nivel * 1.5}rem` }}>
                        <span className="text-gray-400 cursor-grab active:cursor-grabbing shrink-0">
                            <GripVertical className="h-4 w-4" />
                        </span>
                        {hasChildren ? (
                            <button
                                type="button"
                                onClick={() => toggleExpand(node.id)}
                                className="shrink-0 text-gray-500 hover:text-gray-700"
                            >
                                {isExpanded ? <ChevronDown className="h-4 w-4" /> : <ChevronRight className="h-4 w-4" />}
                            </button>
                        ) : (
                            <span className="w-4 shrink-0" />
                        )}
                        <span className="truncate">{node.nombre}</span>
                        {!node.activo && (
                            <span className="bg-red-100 text-red-700 text-xs px-1.5 py-0.5 rounded-full shrink-0">Inactiva</span>
                        )}
                    </div>
                </TableCell>
                <TableCell className="font-mono text-sm">{node.slug}</TableCell>
                <TableCell>{node.padreNombre || '—'}</TableCell>
                <TableCell>{node.ordenVisual ?? '—'}</TableCell>
                <TableCell>{node.activo ? 'Sí' : 'No'}</TableCell>
                <TableCell>
                    <div className="flex gap-2">
                        <Button variant="outline" size="sm" asChild>
                            <Link href={`/logistica/categorias/${node.id}`} aria-label="Ver categoría">
                                <Eye className="h-4 w-4" />
                            </Link>
                        </Button>
                        <Button variant="outline" size="sm" asChild>
                            <Link href={`/logistica/categorias/${node.id}/editar`} aria-label="Editar categoría">
                                <Pencil className="h-4 w-4" />
                            </Link>
                        </Button>
                        <Button variant="destructive" size="sm" onClick={() => handleEliminar(node.id)}>
                            <Trash2 className="h-4 w-4" />
                        </Button>
                    </div>
                </TableCell>
            </TableRow>
        );

        if (hasChildren && isExpanded) {
            for (const child of node.children) {
                rows.push(...renderChildRows(child));
            }
        }

        return rows;
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
                <CardContent className="p-0">
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead className="pl-8">Nombre</TableHead>
                                <TableHead>Slug</TableHead>
                                <TableHead>Padre</TableHead>
                                <TableHead>Orden</TableHead>
                                <TableHead>Activo</TableHead>
                                <TableHead>Acciones</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {tree.length === 0 ? (
                                <TableRow>
                                    <TableCell colSpan={6} className="text-center text-gray-500 py-8">
                                        No hay categorías. Crea la primera.
                                    </TableCell>
                                </TableRow>
                            ) : (
                                tree.flatMap((node) => renderNode(node))
                            )}
                        </TableBody>
                    </Table>
                </CardContent>
            </Card>
        </div>
    );
}