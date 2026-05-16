import type { Metadata } from 'next';
import './globals.css';
import Navbar from '@/components/Navbar';
import Toast from '@/components/Toast';

export const metadata: Metadata = {
  title: 'SmartLogix',
  description: 'Plataforma de gestión logística',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="es">
      <body className="bg-gray-100">
        <Navbar />
        <main className="container mx-auto p-4">{children}</main>
        <Toast />
      </body>
    </html>
  );
}