import Sidebar from "@/components/sidebar";

/**
 * Layout dùng chung cho các màn hình Dashboard và Quản trị.
 * Tự động tích hợp Sidebar điều hướng bên trái và căn lề cho phần nội dung chính.
 */
export default function DashboardLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <div className="flex min-h-screen bg-slate-950">
      {/* Sidebar cố định bên trái */}
      <Sidebar />
      
      {/* Phần nội dung chính bên phải */}
      <main className="flex-1 pl-64 min-h-screen flex flex-col">
        <div className="p-8 flex-1 flex flex-col">
          {children}
        </div>
      </main>
    </div>
  );
}
