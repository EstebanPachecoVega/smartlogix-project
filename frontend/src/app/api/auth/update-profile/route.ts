import { NextRequest, NextResponse } from "next/server";
import { getToken } from "next-auth/jwt";
import { updateKeycloakUser } from "@/lib/keycloak-admin";
import { capitalizeName, normalizeField } from "@/lib/normalize";

export async function PUT(req: NextRequest) {
    try {
        const token = await getToken({ req, secret: process.env.NEXTAUTH_SECRET });
        if (!token?.sub) {
            return NextResponse.json({ error: "No autorizado" }, { status: 401 });
        }

        const body = await req.json();
        const { primerNombre, segundoNombre, primerApellido, segundoApellido } = body;

        if (!primerNombre || !primerApellido) {
            return NextResponse.json(
                { error: "primerNombre y primerApellido son obligatorios" },
                { status: 400 }
            );
        }

        const normalizedPrimerNombre = capitalizeName(primerNombre);
        const normalizedSegundoNombre = segundoNombre ? capitalizeName(segundoNombre) : '';
        const normalizedPrimerApellido = capitalizeName(primerApellido);
        const normalizedSegundoApellido = segundoApellido ? capitalizeName(segundoApellido) : '';

        const firstName = normalizedSegundoNombre
            ? `${normalizedPrimerNombre} ${normalizedSegundoNombre}`
            : normalizedPrimerNombre;
        const lastName = normalizedSegundoApellido
            ? `${normalizedPrimerApellido} ${normalizedSegundoApellido}`
            : normalizedPrimerApellido;

        await updateKeycloakUser(token.sub as string, firstName, lastName);

        return NextResponse.json({
            message: "Perfil actualizado en Keycloak",
            firstName,
            lastName,
        });
    } catch (error: any) {
        console.error("Error actualizando perfil en Keycloak:", error);
        return NextResponse.json(
            { error: error.message || "Error interno del servidor" },
            { status: 500 }
        );
    }
}
