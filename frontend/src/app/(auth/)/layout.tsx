/**
 * Layout cho các trang Authentication (Đăng ký, Đăng nhập, OTP).
 * Đảm bảo căn giữa nội dung biểu mẫu ở chính giữa màn hình và không hiển thị Sidebar.
 */
export default function AuthLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <div className="min-h-screen bg-slate-950 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {children}
      </div>
    </div>
  );
}
