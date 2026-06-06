import { getToken } from "next-auth/jwt";
import { NextRequest, NextResponse } from "next/server";

export async function GET(req: NextRequest) {
    const token = await getToken({ req, secret: process.env.NEXTAUTH_SECRET });

    // Variables de configuración de Keycloak
    const keycloakBaseUrl = 'http://localhost:8180';
    const realm = 'smartlogix';
    const clientId = 'smartlogix-frontend';

    // La URL a la que Keycloak redirigirá después de destruir la sesión
    const baseUrl = process.env.NEXTAUTH_URL || 'http://localhost:3000';
    const postLogoutRedirectUri = encodeURIComponent(`${baseUrl}/login`);

    // Si no hay idToken en el servidor, redirigir directamente al login local
    if (!token?.idToken) {
        return NextResponse.json({ url: `${baseUrl}/login` });
    }

    // Construcción de la URL de Keycloak
    const logoutUrl = `${keycloakBaseUrl}/realms/${realm}/protocol/openid-connect/logout?id_token_hint=${token.idToken}&post_logout_redirect_uri=${postLogoutRedirectUri}&client_id=${clientId}`;

    return NextResponse.json({ url: logoutUrl });
}