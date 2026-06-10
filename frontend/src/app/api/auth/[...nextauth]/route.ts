import NextAuth, { NextAuthOptions } from "next-auth";
import KeycloakProvider from "next-auth/providers/keycloak";

const externalIssuer = process.env.KEYCLOAK_ISSUER!;              // http://localhost:8180/realms/smartlogix
const internalIssuer = process.env.KEYCLOAK_INTERNAL_ISSUER!;     // http://keycloak:8080/realms/smartlogix

export const authOptions: NextAuthOptions = {
    debug: false, // cámbialo a true temporalmente si necesitas logs detallados
    providers: [
        KeycloakProvider({
            clientId: process.env.KEYCLOAK_CLIENT_ID!,
            clientSecret: "", // public client
            // El issuer que usará el servidor para validaciones internas (debe ser accesible desde el contenedor)
            issuer: internalIssuer,
            // La URL de autorización debe ser accesible desde el navegador (externa)
            authorization: {
                url: `${externalIssuer}/protocol/openid-connect/auth`,
                params: { prompt: "login" },
            },
            // Los endpoints de token y userinfo también deben ser accesibles desde el contenedor (internos)
            token: `${internalIssuer}/protocol/openid-connect/token`,
            userinfo: `${internalIssuer}/protocol/openid-connect/userinfo`,
            // Opcional: forzar el well-known para evitar una llamada extra
            wellKnown: `${internalIssuer}/.well-known/openid-configuration`,
        }),
    ],
    callbacks: {
        async jwt({ token, account }) {
            // Login inicial
            if (account) {
                token.accessToken = account.access_token;
                token.refreshToken = account.refresh_token;
                token.expiresAt = account.expires_at;
                token.idToken = account.id_token;
                try {
                    const payload = JSON.parse(
                        Buffer.from(account.access_token!.split('.')[1], 'base64').toString()
                    );
                    token.roles = payload.realm_access?.roles || [];
                    token.sub = payload.sub;
                } catch (error) {
                    console.error("Error decodificando token:", error);
                    token.roles = [];
                }
                return token;
            }
            // Token aún vigente
            if (token.expiresAt && Date.now() < (token.expiresAt as number) * 1000) {
                return token;
            }
            // Token expirado → refresh
            try {
                const response = await fetch(`${internalIssuer}/protocol/openid-connect/token`, {
                    method: "POST",
                    headers: { "Content-Type": "application/x-www-form-urlencoded" },
                    body: new URLSearchParams({
                        client_id: process.env.KEYCLOAK_CLIENT_ID!,
                        grant_type: "refresh_token",
                        refresh_token: token.refreshToken as string,
                    }),
                });
                const tokens = await response.json();
                if (!response.ok) throw tokens;
                token.accessToken = tokens.access_token;
                token.refreshToken = tokens.refresh_token ?? token.refreshToken;
                token.expiresAt = Math.floor(Date.now() / 1000) + tokens.expires_in;
                return token;
            } catch (error) {
                console.error("Error refrescando token:", error);
                token.error = "RefreshAccessTokenError";
                return token;
            }
        },
        async session({ session, token }) {
            session.accessToken = token.accessToken as string;
            session.roles = token.roles as string[];
            session.sub = token.sub as string;
            session.error = token.error as string;
            return session;
        },
        async redirect({ url, baseUrl }) {
            if (url === baseUrl || url === `${baseUrl}/login` || url === '/login') {
                return `${baseUrl}/`;
            }
            if (url.startsWith('/')) return `${baseUrl}${url}`;
            if (new URL(url).origin === baseUrl) return url;
            return `${baseUrl}/`;
        },
    },
    pages: {
        signIn: "/login",
    },
};

const handler = NextAuth(authOptions);
export { handler as GET, handler as POST };