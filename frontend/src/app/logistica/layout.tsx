import SidebarLogistica from '@/components/logistica/Sidebar';

export default function LogisticaLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex h-screen overflow-hidden">
      <SidebarLogistica />
      <main className="flex-1 p-6 overflow-y-auto">{children}</main>
    </div>
  );
}