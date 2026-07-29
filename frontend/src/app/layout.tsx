import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";

const inter = Inter({
  subsets: ["latin"],
  variable: "--font-inter",
});

export const metadata: Metadata = {
  title: "FinWallet - Hệ thống ví điện tử Fintech",
  description: "Bảng điều khiển hệ thống ví điện tử Fintech Core",
};

/**
 * Root Layout của ứng dụng Next.js.
 * Thiết lập font chữ Inter toàn cục và cấu hình ngôn ngữ.
 */
export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="vi" className={`${inter.variable} h-full antialiased`}>
      <body className="min-h-full bg-slate-950 text-slate-100 flex flex-col">
        {children}
      </body>
    </html>
  );
}
