import { withAuth } from "next-auth/middleware";
import { NextResponse } from "next/server";

export default withAuth(
    function middleware(req) {
        const token = req.nextauth.token;
        const path = req.nextUrl.pathname;

        // Permitir acceso público a / y /login
        if (path === "/" || path === "/login") return NextResponse.next();

        // Proteger dashboard (requiere autenticación)
        if (path.startsWith("/dashboard")) {
            if (!token) return NextResponse.redirect(new URL("/login", req.url));
            return NextResponse.next();
        }

        // Proteger logística (requiere rol gestor)
        if (path.startsWith("/logistica")) {
            if (!token) return NextResponse.redirect(new URL("/login", req.url));
            const roles = token.roles as string[] || [];
            if (!roles.includes("gestor")) return NextResponse.redirect(new URL("/", req.url));
            return NextResponse.next();
        }

        return NextResponse.next();
    },
    { callbacks: { authorized: ({ token }) => true } }
);

export const config = { matcher: ["/((?!api/auth|_next/static|favicon.ico).*)"] };