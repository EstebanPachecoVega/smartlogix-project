'use client';

import { useState, useEffect } from 'react';
import { useSession } from "next-auth/react";
import { useRouter } from 'next/navigation';
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { toast } from "sonner";
import Spinner from "@/components/shared/Spinner";
import { getUserProfile, updateUserProfile, UserProfile } from '@/lib/userService';
import { censorEmail } from '@/lib/emailUtils';

export default function PerfilGestorPage() {
    const { data: session, status } = useSession();
    const router = useRouter();
    const [loading, setLoading] = useState(false);
    const [isEditing, setIsEditing] = useState(false);
    const [form, setForm] = useState<UserProfile | null>(null);
    const [originalForm, setOriginalForm] = useState<UserProfile | null>(null);

    useEffect(() => {
        if (status === "authenticated") {
            getUserProfile().then(profile => {
                setForm(profile);
                setOriginalForm(JSON.parse(JSON.stringify(profile)));
            });
        }
    }, [status]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (!form) return;
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleEdit = () => {
        setOriginalForm(JSON.parse(JSON.stringify(form)));
        setIsEditing(true);
    };

    const handleCancel = () => {
        setForm(JSON.parse(JSON.stringify(originalForm)));
        setIsEditing(false);
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!form) return;
        setLoading(true);
        try {
            await updateUserProfile(form);
            setOriginalForm(JSON.parse(JSON.stringify(form)));
            toast.success("Perfil actualizado", { description: "Los cambios se han guardado correctamente." });
            setIsEditing(false);
        } catch (error) {
            toast.error("Error", { description: "No se pudo guardar el perfil." });
        } finally {
            setLoading(false);
        }
    };

    if (status === "loading") return <Spinner />;
    if (!session || !form) return <div>Debes iniciar sesión</div>;

    const isGestor = session.roles?.includes('gestor');
    if (!isGestor) {
        router.push('/cliente/perfil');
        return null;
    }

    const rolTexto = 'Gestor Logístico';
    const censoredEmail = censorEmail(session.user?.email || '');

    return (
        <div className="max-w-2xl mx-auto">
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-2xl font-bold">Mi perfil (Gestor)</h1>
                {!isEditing && <Button onClick={handleEdit} variant="outline">Editar</Button>}
            </div>
            <Card>
                <CardHeader><CardTitle>Información personal</CardTitle></CardHeader>
                <CardContent>
                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div>
                                <Label>Primer nombre *</Label>
                                <Input name="primerNombre" value={form.primerNombre} onChange={handleChange} required disabled={!isEditing} placeholder="Ingresar primer nombre" />
                            </div>
                            <div>
                                <Label>Segundo nombre (opcional)</Label>
                                <Input name="segundoNombre" value={form.segundoNombre} onChange={handleChange} disabled={!isEditing} placeholder="Ingresar segundo nombre (opcional)" />
                            </div>
                            <div>
                                <Label>Primer apellido *</Label>
                                <Input name="primerApellido" value={form.primerApellido} onChange={handleChange} required disabled={!isEditing} placeholder="Ingresar primer apellido" />
                            </div>
                            <div>
                                <Label>Segundo apellido (opcional)</Label>
                                <Input name="segundoApellido" value={form.segundoApellido} onChange={handleChange} disabled={!isEditing} placeholder="Ingresar segundo apellido (opcional)" />
                            </div>
                        </div>
                        <div>
                            <Label>Correo electrónico</Label>
                            <p className="text-sm text-gray-500 mt-1">{censoredEmail}</p>
                            <p className="text-xs text-gray-400">El correo no puede ser modificado directamente.</p>
                        </div>
                        <div>
                            <Label>Rol</Label>
                            <p className="text-sm font-medium text-blue-600">{rolTexto}</p>
                        </div>
                        {isEditing && (
                            <div className="flex gap-4 pt-2">
                                <Button type="submit" disabled={loading}>{loading ? "Guardando..." : "Guardar cambios"}</Button>
                                <Button type="button" variant="outline" onClick={handleCancel} disabled={loading}>Cancelar</Button>
                            </div>
                        )}
                    </form>
                </CardContent>
            </Card>
        </div>
    );
}