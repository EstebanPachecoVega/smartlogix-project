'use client';

import { useState, useEffect } from 'react';
import { useSession } from "next-auth/react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { toast } from "sonner";
import Spinner from "@/components/shared/Spinner";
import ProfileNameFields from '@/components/cliente/ProfileNameFields';
import { getUserProfile, updateUserProfile, UserProfile } from '@/lib/userService';
import { capitalizeName, normalizeField } from '@/lib/normalize';

export default function PerfilPage() {
    const { data: session, status } = useSession();
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
            const normalized = {
                ...form,
                primerNombre: capitalizeName(form.primerNombre),
                segundoNombre: capitalizeName(form.segundoNombre),
                primerApellido: capitalizeName(form.primerApellido),
                segundoApellido: capitalizeName(form.segundoApellido),
                calle: normalizeField(form.calle),
                numero: normalizeField(form.numero),
                comuna: normalizeField(form.comuna),
                ciudad: normalizeField(form.ciudad),
                codigoPostal: normalizeField(form.codigoPostal),
            };
            await updateUserProfile(normalized);
            setForm(normalized);
            setOriginalForm(JSON.parse(JSON.stringify(normalized)));
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

    return (
        <div className="max-w-2xl mx-auto">
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-2xl font-bold">Mi perfil</h1>
                {!isEditing && <Button onClick={handleEdit} variant="outline">Editar</Button>}
            </div>
            <Card>
                <CardHeader><CardTitle>Información personal</CardTitle></CardHeader>
                <CardContent>
                    <form onSubmit={handleSubmit} className="space-y-4">
                        <ProfileNameFields
                            primerNombre={form.primerNombre}
                            segundoNombre={form.segundoNombre}
                            primerApellido={form.primerApellido}
                            segundoApellido={form.segundoApellido}
                            email={session.user?.email || ''}
                            disabled={!isEditing}
                            onChange={handleChange}
                        />
                        <div className="border-t pt-4 mt-2">
                            <h3 className="font-medium mb-3">Dirección</h3>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                <div><Label>Calle</Label><Input name="calle" value={form.calle} onChange={handleChange} disabled={!isEditing} placeholder="Ej: Av. Siempre Viva" /></div>
                                <div><Label>Número</Label><Input name="numero" value={form.numero} onChange={handleChange} disabled={!isEditing} placeholder="Ej: 123" /></div>
                                <div><Label>Comuna</Label><Input name="comuna" value={form.comuna} onChange={handleChange} disabled={!isEditing} placeholder="Ingresar comuna" /></div>
                                <div><Label>Ciudad</Label><Input name="ciudad" value={form.ciudad} onChange={handleChange} disabled={!isEditing} placeholder="Ingresar ciudad" /></div>
                                <div><Label>Código postal</Label><Input name="codigoPostal" value={form.codigoPostal} onChange={handleChange} disabled={!isEditing} placeholder="Ingresar código postal" /></div>
                            </div>
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