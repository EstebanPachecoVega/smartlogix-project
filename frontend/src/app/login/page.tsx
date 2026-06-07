'use client';
import { signIn, useSession } from "next-auth/react";
import { useRouter } from "next/navigation";
import { useEffect } from "react";

export default function LoginPage() {
    const { data: session, status } = useSession();
    const router = useRouter();

    useEffect(() => {
        if (status === "authenticated") {
            router.push("/dashboard/perfil");
        }
    }, [status, router]);

    if (status === "loading") return <div className="text-center py-8">Cargando...</div>;
    if (session) return null;

    return (
        <div className="min-h-screen flex items-center justify-center">
            <button
                onClick={() => signIn("keycloak")}
                className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
            >
                Iniciar sesión con Keycloak
            </button>
        </div>
    );
}