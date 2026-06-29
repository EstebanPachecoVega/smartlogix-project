import '@/styles/globals.css';
import type { Metadata } from 'next';
import Providers from './providers';
import Navbar from '@/components/layout/Navbar';
import PublicCategoryNav from '@/components/layout/PublicCategoryNav';
import Footer from '@/components/layout/Footer';
import { Toaster } from '@/components/ui/sonner';
import JsonLdOrganization from '@/components/seo/JsonLdOrganization';
import JsonLdWebSite from '@/components/seo/JsonLdWebSite';

export const metadata: Metadata = {
  title: {
    default: 'SmartLogix | Tecnología para el hogar y gaming',
    template: '%s | SmartLogix',
  },
  description: 'SmartLogix — tienda de tecnología con productos gaming, computación, smartphones y más. Envíos a todo Chile.',
  openGraph: {
    title: 'SmartLogix | Tecnología para el hogar y gaming',
    description: 'SmartLogix — tienda de tecnología con productos gaming, computación, smartphones y más.',
    url: 'https://smartlogix.cl',
    siteName: 'SmartLogix',
    locale: 'es_CL',
    type: 'website',
  },
  twitter: {
    card: 'summary_large_image',
    title: 'SmartLogix | Tecnología para el hogar y gaming',
    description: 'SmartLogix — tienda de tecnología con productos gaming, computación, smartphones y más.',
  },
  robots: {
    index: true,
    follow: true,
  },
  alternates: {
    canonical: 'https://smartlogix.cl',
  },
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="es" suppressHydrationWarning>
      <body>
        <Providers>
          <JsonLdOrganization />
          <JsonLdWebSite />
          <Navbar />
          <div id="app-container" className="app-container flex flex-col min-h-screen pt-16">
            <Toaster position="top-right"/>
            <PublicCategoryNav />
            <main className="container mx-auto px-4 sm:px-6 pb-8 flex-1 pt-4">{children}</main>
            <Footer />
          </div>
        </Providers>
      </body>
    </html>
  );
}