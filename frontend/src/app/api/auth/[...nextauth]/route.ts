import NextAuth, { NextAuthOptions } from "next-auth";
import KeycloakProvider from "next-auth/providers/keycloak";

export const authOptions: NextAuthOptions = {
    debug: false,
    providers: [
        KeycloakProvider({
            clientId: process.env.KEYCLOAK_CLIENT_ID!,
            clientSecret: "",
            issuer: process.env.KEYCLOAK_ISSUER,
            authorization: { params: { prompt: "login" } },
        }),
    ],
    callbacks: {
        async jwt({ token, account }) {
            if (account && account.access_token) {
                token.accessToken = account.access_token;
                token.idToken = account.id_token; // Se almacena exclusivamente en el servidor
                try {
                    const payload = JSON.parse(
                        Buffer.from(account.access_token.split('.')[1], 'base64').toString()
                    );
                    token.roles = payload.realm_access?.roles || [];
                    token.sub = payload.sub;
                } catch (error) {
                    console.error("Error decodificando token:", error);
                    token.roles = [];
                }
            }
            return token;
        },
        async session({ session, token }) {
            session.accessToken = token.accessToken as string;
            // ELIMINADO: session.idToken = token.idToken as string;
            session.roles = token.roles as string[];
            session.sub = token.sub as string;
            return session;
        },
        async redirect({ url, baseUrl }) {
            if (url === baseUrl || url === `${baseUrl}/login` || url === '/login') {
                return `${baseUrl}/cliente`;
            }
            if (url.startsWith('/')) return `${baseUrl}${url}`;
            if (new URL(url).origin === baseUrl) return url;
            return `${baseUrl}/cliente`;
        },
    },
    pages: {
        signIn: "/login",
    },
};

const handler = NextAuth(authOptions);
export { handler as GET, handler as POST };