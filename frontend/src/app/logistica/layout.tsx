import Sidebar from '@/components/logistica/Sidebar';

export default function LogisticaLayout({ children }: { children: React.ReactNode }) {
    return (
        <div className="flex min-h-[calc(100vh-64px)]">
            <Sidebar />
            <main className="flex-1 p-6">{children}</main>
        </div>
    );
}