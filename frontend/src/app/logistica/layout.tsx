import SidebarLogistica from '@/components/logistica/Sidebar';

export default function LogisticaLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex min-h-screen">
      <SidebarLogistica />
      <main className="flex-1 p-6 overflow-auto">{children}</main>
    </div>
  );
}