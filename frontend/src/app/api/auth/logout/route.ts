import { getToken } from "next-auth/jwt";
import { NextRequest, NextResponse } from "next/server";

export async function GET(req: NextRequest) {
    const token = await getToken({ req, secret: process.env.NEXTAUTH_SECRET });

    // Variables de configuración de Keycloak
    const issuer = process.env.KEYCLOAK_ISSUER || 'http://localhost:8180/realms/smartlogix';
    const keycloakBaseUrl = issuer.replace(/\/realms\/.*$/, '');
    const realm = issuer.split('/realms/')[1] || 'smartlogix';
    const clientId = process.env.KEYCLOAK_CLIENT_ID || 'smartlogix-frontend';

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