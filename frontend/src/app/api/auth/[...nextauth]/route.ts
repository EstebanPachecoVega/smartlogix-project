import NextAuth, { NextAuthOptions } from "next-auth";
import KeycloakProvider from "next-auth/providers/keycloak";
import CredentialsProvider from "next-auth/providers/credentials";

const externalIssuer = process.env.KEYCLOAK_ISSUER!;
const internalIssuer = process.env.KEYCLOAK_INTERNAL_ISSUER!;

export const authOptions: NextAuthOptions = {
    debug: false,
    providers: [
        KeycloakProvider({
            clientId: process.env.KEYCLOAK_CLIENT_ID!,
            clientSecret: "",
            issuer: internalIssuer,
            authorization: {
                url: `${externalIssuer}/protocol/openid-connect/auth`,
                params: { prompt: "login" },
            },
            token: `${internalIssuer}/protocol/openid-connect/token`,
            userinfo: `${internalIssuer}/protocol/openid-connect/userinfo`,
            wellKnown: `${internalIssuer}/.well-known/openid-configuration`,
        }),
        CredentialsProvider({
            name: "credentials",
            credentials: {
                email: { label: "Email", type: "email" },
                password: { label: "Contraseña", type: "password" },
            },
            async authorize(credentials) {
                if (!credentials?.email || !credentials?.password) return null;

                const res = await fetch(`${internalIssuer}/protocol/openid-connect/token`, {
                    method: "POST",
                    headers: { "Content-Type": "application/x-www-form-urlencoded" },
                    body: new URLSearchParams({
                        client_id: process.env.KEYCLOAK_CLIENT_ID!,
                        grant_type: "password",
                        username: credentials.email,
                        password: credentials.password,
                        scope: "openid profile email",
                    }),
                });

                if (!res.ok) {
                    const error = await res.json().catch(() => ({}));
                    throw new Error(error.error_description || "Credenciales incorrectas");
                }

                const tokens = await res.json();
                const payload = JSON.parse(
                    Buffer.from(tokens.access_token.split('.')[1], 'base64').toString()
                );

                const extractRoles = (p: any): string[] => {
                    const realmRoles: string[] = p.realm_access?.roles || [];
                    const clientRoles: string[] = p.resource_access?.[process.env.KEYCLOAK_CLIENT_ID!]?.roles || [];
                    return realmRoles.length > 0 ? realmRoles : clientRoles;
                };

                return {
                    id: payload.sub,
                    email: payload.email || credentials.email,
                    name: payload.name || payload.preferred_username || credentials.email,
                    accessToken: tokens.access_token,
                    refreshToken: tokens.refresh_token,
                    expiresAt: Math.floor(Date.now() / 1000) + tokens.expires_in,
                    idToken: tokens.id_token,
                    roles: extractRoles(payload),
                };
            },
        }),
    ],
    callbacks: {
        async jwt({ token, account, user }) {
            const extractRoles = (p: any): string[] => {
                const realmRoles: string[] = p.realm_access?.roles || [];
                const clientRoles: string[] = p.resource_access?.[process.env.KEYCLOAK_CLIENT_ID!]?.roles || [];
                const all = realmRoles.length > 0 ? realmRoles : clientRoles;
                return all.map((r: string) => r.toLowerCase());
            };

            const sessionToken = account?.access_token ? account : user;
            if (sessionToken) {
                const tok = sessionToken as any;
                token.accessToken = tok.access_token || tok.accessToken;
                token.refreshToken = tok.refresh_token || tok.refreshToken;
                token.expiresAt = tok.expires_at || tok.expiresAt;
                token.idToken = tok.id_token || tok.idToken;
                try {
                    const at = token.accessToken as string;
                    const payload = JSON.parse(
                        Buffer.from(at.split('.')[1], 'base64').toString()
                    );
                    const rawRoles: string[] = extractRoles(payload);
                    token.roles = rawRoles;
                    token.sub = payload.sub;
                } catch {
                    console.warn("[NextAuth] Error decodificando token, roles vacíos");
                    token.roles = [];
                }
                return token;
            }
            if (token.expiresAt && Date.now() < (token.expiresAt as number) * 1000) {
                return token;
            }
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
                // Re-extractar roles del nuevo access token
                try {
                    const payload = JSON.parse(
                        Buffer.from(tokens.access_token.split('.')[1], 'base64').toString()
                    );
                    const refreshedRoles: string[] = extractRoles(payload);
                    console.log("[NextAuth] Roles re-extraídos tras refresh:", refreshedRoles);
                    token.roles = refreshedRoles;
                } catch {
                    console.warn("[NextAuth] Error decodificando token refrescado");
                }
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
