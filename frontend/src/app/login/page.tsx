'use client';
import { signIn } from "next-auth/react";

export default function LoginPage() {
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