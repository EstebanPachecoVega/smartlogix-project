import "next-auth";

declare module "next-auth" {
    interface Session {
        accessToken?: string;
        idToken?: string;
        roles?: string[];
        sub?: string;
        error?: string;
    }
    interface JWT {
        accessToken?: string;
        idToken?: string;
        refreshToken?: string;
        expiresAt?: number;
        roles?: string[];
        sub?: string;
        error?: string;
    }
}