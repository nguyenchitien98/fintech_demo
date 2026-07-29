"use client";

import { useState, useEffect } from "react";
import Sidebar from "@/components/sidebar";
import { Info, CheckCircle2, AlertCircle, X } from "lucide-react";

interface Toast {
  id: number;
  title: string;
  message: string;
  type: "info" | "success" | "error";
}

/**
 * Layout dùng chung cho các màn hình Dashboard và Quản trị.
 * - Tự động tích hợp Sidebar bên trái.
 * - Thiết lập kết nối Server-Sent Events (SSE) thời gian thực tới notification-service (8086).
 * - Hiển thị popup Toast thông báo biến động số dư khi có sự kiện giao dịch từ Kafka.
 */
export default function DashboardLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const [toasts, setToasts] = useState<Toast[]>([]);

  // Thêm một toast thông báo mới
  const addToast = (title: string, message: string, type: "info" | "success" | "error" = "info") => {
    const id = Date.now();
    setToasts(prev => [...prev, { id, title, message, type }]);
    
    // Tự động biến mất sau 5 giây
    setTimeout(() => {
      setToasts(prev => prev.filter(t => t.id !== id));
    }, 6000);
  };

  // Kết nối Server-Sent Events (SSE) thời gian thực
  useEffect(() => {
    // Khởi tạo EventSource lắng nghe cổng 8086 của notification-service
    const eventSource = new EventSource("http://localhost:8086/api/v1/notifications/stream");

    eventSource.addEventListener("CONNECT", (event) => {
      console.log(">>> [SSE CONNECTED] ", event.data);
    });

    eventSource.addEventListener("TRANSACTION", (event) => {
      try {
        const tx = JSON.parse(event.data);
        // Hiển thị thông báo Toast biến động số dư tức thời
        addToast(
          "Biến động số dư! 💸",
          `Giao dịch ${tx.transactionId} thành công: Chuyển ${Number(tx.amount).toLocaleString("vi-VN")} VND. Nội dung: ${tx.description}`,
          "success"
        );
      } catch (err) {
        console.error("Lỗi parse dữ liệu thông báo SSE:", err);
      }
    });

    eventSource.onerror = (err) => {
      console.warn("Mất kết nối SSE tới Notification Server, đang chờ tự động kết nối lại...", err);
    };

    return () => {
      eventSource.close();
    };
  }, []);

  return (
    <div className="flex min-h-screen bg-slate-950">
      {/* Sidebar cố định bên trái */}
      <Sidebar />
      
      {/* Phần nội dung chính bên phải */}
      <main className="flex-1 pl-64 min-h-screen flex flex-col relative">
        <div className="p-8 flex-1 flex flex-col">
          {children}
        </div>

        {/* Real-time Toast Notifications Container (Góc phải màn hình) */}
        <div className="fixed bottom-6 right-6 z-50 flex flex-col gap-3 w-90 max-w-[90vw]">
          {toasts.map(toast => (
            <div
              key={toast.id}
              className="bg-slate-900 border border-slate-800 rounded-xl p-4 shadow-2xl flex items-start space-x-3 animate-slide-in relative group transition-all duration-300 hover:border-slate-700"
            >
              {/* Type Icons */}
              {toast.type === "success" && <CheckCircle2 className="w-5 h-5 text-emerald-500 mt-0.5 flex-shrink-0" />}
              {toast.type === "error" && <AlertCircle className="w-5 h-5 text-red-500 mt-0.5 flex-shrink-0" />}
              {toast.type === "info" && <Info className="w-5 h-5 text-blue-500 mt-0.5 flex-shrink-0" />}

              {/* Text content */}
              <div className="flex-1 pr-6 space-y-0.5">
                <h4 className="text-xs font-bold text-slate-100 tracking-wide uppercase">{toast.title}</h4>
                <p className="text-xs text-slate-400 leading-normal font-medium">{toast.message}</p>
              </div>

              {/* Close button */}
              <button
                onClick={() => setToasts(prev => prev.filter(t => t.id !== toast.id))}
                className="absolute right-3 top-3 text-slate-550 hover:text-slate-200 transition-colors"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
          ))}
        </div>
      </main>
    </div>
  );
}
