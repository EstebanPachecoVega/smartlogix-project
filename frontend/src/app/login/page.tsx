'use client';

import { useState } from "react";
import { signIn, useSession } from "next-auth/react";
import { useRouter } from "next/navigation";
import { useEffect } from "react";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Eye, EyeOff, Loader2 } from "lucide-react";
import { validateFormSpaces, normalizeField } from "@/lib/normalize";

export default function LoginPage() {
  const { data: session, status } = useSession();
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (status === "authenticated") {
      router.push("/");
    }
  }, [status, router]);

  if (status === "loading") return <div className="text-center py-8">Cargando...</div>;
  if (session) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    const spaceError = validateFormSpaces(
      { email, password },
      { email: "Email", password: "Contraseña" }
    );
    if (spaceError) {
      setError(spaceError);
      return;
    }

    setLoading(true);

    const trimmedEmail = normalizeField(email);

    try {
      const result = await signIn("credentials", {
        email: trimmedEmail,
        password,
        redirect: false,
      });

      if (result?.error) {
        setError("Credenciales incorrectas. Verifica tu email y contraseña.");
      } else {
        router.push("/");
      }
    } catch {
      setError("Error al iniciar sesión. Intenta nuevamente.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[80vh] flex items-center justify-center">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl">Iniciar sesión</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="correo@ejemplo.com"
                required
                autoComplete="email"
              />
            </div>

            <div>
              <Label htmlFor="password">Contraseña</Label>
              <div className="relative">
                <Input
                  id="password"
                  type={showPassword ? "text" : "password"}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  required
                  autoComplete="current-password"
                  className="pr-10"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                  tabIndex={-1}
                >
                  {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
            </div>

            {error && (
              <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-2.5 rounded-md text-sm">
                {error}
              </div>
            )}

            <Button type="submit" disabled={loading} className="w-full">
              {loading && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
              {loading ? "Iniciando sesión..." : "Iniciar sesión"}
            </Button>
          </form>

          <div className="mt-6 text-center text-sm text-gray-500">
            ¿No tienes cuenta?{" "}
            <Link href="/registro" className="text-blue-600 hover:text-blue-700 font-medium">
              Registrarse
            </Link>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
