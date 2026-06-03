import "next-auth";

declare module "next-auth" {
    interface Session {
        accessToken?: string;
        idToken?: string;
        roles?: string[];
    }
    interface JWT {
        accessToken?: string;
        idToken?: string;
        roles?: string[];
    }
}