import { withAuth } from "next-auth/middleware";
import { NextResponse } from "next/server";

export default withAuth(
    function middleware(req) {
        const token = req.nextauth.token;
        const roles = (token?.roles as string[]) || [];
        const isLogistica = req.nextUrl.pathname.startsWith("/logistica");

        if (isLogistica && !roles.includes("gestor")) {
            return NextResponse.redirect(new URL("/cliente", req.url));
        }
        return NextResponse.next();
    },
    {
        callbacks: {
            authorized: ({ token }) => !!token,
        },
    }
);

export const config = { matcher: ["/cliente/:path*", "/logistica/:path*"] };