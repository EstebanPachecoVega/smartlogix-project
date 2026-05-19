import NavbarCliente from '@/components/cliente/Navbar';

export default function ClienteLayout({ children }: { children: React.ReactNode }) {
  return (
    <>
      <NavbarCliente />
      <main className="container mx-auto px-4 py-8">{children}</main>
    </>
  );
}