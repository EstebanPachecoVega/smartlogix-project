'use client';

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Eye, EyeOff, Loader2, Check, X } from "lucide-react";
import axios from "axios";
import { validateFormSpaces, normalizeFormData } from "@/lib/normalize";

export default function RegistroPage() {
  const router = useRouter();
  const [form, setForm] = useState({
    primerNombre: "",
    segundoNombre: "",
    primerApellido: "",
    email: "",
    password: "",
    confirmPassword: "",
  });
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);

  const passwordsMatch = form.password === form.confirmPassword;
  const passwordLength = form.password.length >= 6;

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    const spaceError = validateFormSpaces(form, {
      primerNombre: "Primer nombre",
      segundoNombre: "Segundo nombre",
      primerApellido: "Primer apellido",
      email: "Email",
      password: "Contraseña",
      confirmPassword: "Confirmar contraseña",
    });
    if (spaceError) {
      setError(spaceError);
      return;
    }

    if (!form.primerNombre || !form.primerApellido || !form.email || !form.password) {
      setError("Completa todos los campos obligatorios.");
      return;
    }

    if (form.password.length < 6) {
      setError("La contraseña debe tener al menos 6 caracteres.");
      return;
    }

    if (!passwordsMatch) {
      setError("Las contraseñas no coinciden.");
      return;
    }

    setLoading(true);

    const normalized = normalizeFormData(
      {
        primerNombre: form.primerNombre,
        segundoNombre: form.segundoNombre || '',
        primerApellido: form.primerApellido,
        email: form.email,
        password: form.password,
      },
      ['primerNombre', 'segundoNombre', 'primerApellido']
    );

    try {
      await axios.post("/api/auth/register", {
        primerNombre: normalized.primerNombre,
        segundoNombre: normalized.segundoNombre || undefined,
        primerApellido: normalized.primerApellido,
        email: normalized.email,
        password: normalized.password,
      });

      setSuccess(true);
      setTimeout(() => router.push("/login"), 2000);
    } catch (err: any) {
      const msg = err?.response?.data?.error || "Error al crear la cuenta. Intenta nuevamente.";
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  if (success) {
    return (
      <div className="min-h-[80vh] flex items-center justify-center">
        <Card className="w-full max-w-md text-center">
          <CardContent className="py-8">
            <Check className="h-12 w-12 text-green-500 mx-auto mb-4" />
            <h2 className="text-xl font-bold mb-2">Cuenta creada exitosamente</h2>
            <p className="text-muted-foreground">Redirigiendo al inicio de sesión...</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-[80vh] flex items-center justify-center">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl">Crear cuenta</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-2 gap-3">
              <div>
                <Label htmlFor="primerNombre">Nombre *</Label>
                  <Input
                    id="primerNombre"
                    name="primerNombre"
                    value={form.primerNombre}
                    onChange={handleChange}
                    placeholder="Ingresa solo tu primer nombre"
                    required
                  />
                </div>
                <div>
                  <Label htmlFor="segundoNombre">Segundo nombre</Label>
                  <Input
                    id="segundoNombre"
                    name="segundoNombre"
                    value={form.segundoNombre}
                    onChange={handleChange}
                    placeholder="Segundo nombre (opcional)"
                  />
              </div>
            </div>

            <div>
              <Label htmlFor="primerApellido">Apellido *</Label>
                <Input
                  id="primerApellido"
                  name="primerApellido"
                  value={form.primerApellido}
                  onChange={handleChange}
                  placeholder="Ingresa solo tu apellido"
                  required
                />
            </div>

            <div>
              <Label htmlFor="email">Email *</Label>
              <Input
                id="email"
                name="email"
                type="email"
                value={form.email}
                onChange={handleChange}
                placeholder="correo@ejemplo.com"
                required
                autoComplete="email"
              />
            </div>

            <div>
              <Label htmlFor="password">Contraseña *</Label>
              <div className="relative">
                <Input
                  id="password"
                  name="password"
                  type={showPassword ? "text" : "password"}
                  value={form.password}
                  onChange={handleChange}
                  placeholder="Mínimo 6 caracteres"
                  required
                  autoComplete="new-password"
                  className="pr-10"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground/70 hover:text-foreground"
                  tabIndex={-1}
                >
                  {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
              {form.password.length > 0 && (
                <div className="mt-1.5 space-y-1">
                  <div className={`flex items-center gap-1.5 text-xs ${passwordLength ? 'text-green-600' : 'text-muted-foreground/70'}`}>
                    {passwordLength ? <Check className="h-3 w-3" /> : <X className="h-3 w-3" />}
                    <span>Al menos 6 caracteres</span>
                  </div>
                </div>
              )}
            </div>

            <div>
              <Label htmlFor="confirmPassword">Confirmar contraseña *</Label>
              <div className="relative">
                <Input
                  id="confirmPassword"
                  name="confirmPassword"
                  type={showConfirm ? "text" : "password"}
                  value={form.confirmPassword}
                  onChange={handleChange}
                  placeholder="Repite la contraseña"
                  required
                  autoComplete="new-password"
                  className="pr-10"
                />
                <button
                  type="button"
                  onClick={() => setShowConfirm(!showConfirm)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground/70 hover:text-foreground"
                  tabIndex={-1}
                >
                  {showConfirm ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
              {form.confirmPassword.length > 0 && (
                <div className="mt-1.5">
                  <div className={`flex items-center gap-1.5 text-xs ${passwordsMatch ? 'text-green-600' : 'text-red-500'}`}>
                    {passwordsMatch ? <Check className="h-3 w-3" /> : <X className="h-3 w-3" />}
                    <span>{passwordsMatch ? 'Las contraseñas coinciden' : 'Las contraseñas no coinciden'}</span>
                  </div>
                </div>
              )}
            </div>

            {error && (
              <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-2.5 rounded-md text-sm">
                {error}
              </div>
            )}

            <Button type="submit" disabled={loading} className="w-full">
              {loading && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
              {loading ? "Creando cuenta..." : "Crear cuenta"}
            </Button>
          </form>

          <div className="mt-6 text-center text-sm text-muted-foreground">
            ¿Ya tienes cuenta?{" "}
            <Link href="/login" className="text-primary hover:text-primary/80 font-medium">
              Iniciar sesión
            </Link>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
