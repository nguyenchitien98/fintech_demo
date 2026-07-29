"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { 
  LayoutDashboard, 
  Wallet, 
  ArrowLeftRight, 
  History, 
  Cpu, 
  Database, 
  HeartPulse, 
  Users, 
  ShieldAlert,
  LogIn,
  UserPlus
} from "lucide-react";

/**
 * Component Sidebar điều hướng chính cho Fintech Core Dashboard (Sidebar).
 * Hiển thị menu bên trái màn hình với các mục quản lý và giám sát hệ thống ví.
 */
export default function Sidebar() {
  const pathname = usePathname();

  // Bỏ qua hiển thị Sidebar trên các màn hình Authentication (Login, Register)
  const isAuthPage = pathname === "/login" || pathname === "/register";
  if (isAuthPage) return null;

  const menuItems = [
    { name: "Tổng quan", href: "/", icon: LayoutDashboard },
    { name: "Quản lý ví", href: "/wallets", icon: Wallet },
    { name: "Chuyển tiền", href: "/transfer", icon: ArrowLeftRight },
    { name: "Lịch sử GD", href: "/transactions", icon: History },
    { name: "Giám sát Kafka", href: "/monitors/kafka", icon: Cpu },
    { name: "Redis Dashboard", href: "/monitors/redis", icon: Database },
    { name: "Sức khỏe hệ thống", href: "/monitors/health", icon: HeartPulse },
    { name: "Phát hiện gian lận", href: "/monitors/fraud", icon: ShieldAlert },
    { name: "Quản trị Admin", href: "/admin", icon: Users },
  ];

  return (
    <aside className="w-64 bg-slate-900 border-r border-slate-800 h-screen fixed left-0 top-0 flex flex-col z-20">
      {/* Brand Header */}
      <div className="p-6 border-b border-slate-800 flex items-center space-x-3">
        <div className="w-8 h-8 rounded-lg bg-blue-500 flex items-center justify-center font-bold text-white shadow-md shadow-blue-500/20">
          FW
        </div>
        <span className="font-bold text-lg text-slate-100 tracking-wide">FinWallet</span>
      </div>

      {/* Navigation Links */}
      <nav className="flex-1 p-4 space-y-1 overflow-y-auto">
        {menuItems.map((item) => {
          const Icon = item.icon;
          const isActive = pathname === item.href;
          return (
            <Link
              key={item.href}
              href={item.href}
              className={`flex items-center space-x-3 px-4 py-3 rounded-lg text-sm font-medium transition-all duration-200 ${
                isActive 
                  ? "bg-blue-600 text-white shadow-md shadow-blue-600/10" 
                  : "text-slate-400 hover:bg-slate-800 hover:text-slate-100"
              }`}
            >
              <Icon className="w-5 h-5 flex-shrink-0" />
              <span>{item.name}</span>
            </Link>
          );
        })}
      </nav>

      {/* User Session Footer (Mô phỏng ở Sprint 1) */}
      <div className="p-4 border-t border-slate-800 flex flex-col space-y-2">
        <div className="flex items-center space-x-3 px-2">
          <div className="w-9 h-9 rounded-full bg-slate-700 flex items-center justify-center text-slate-200 font-medium">
            JD
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-semibold text-slate-200 truncate">John Doe</p>
            <p className="text-xs text-slate-500 truncate">john.doe@example.com</p>
          </div>
        </div>
        <div className="flex space-x-2 pt-2">
          <Link
            href="/login"
            className="flex-1 flex items-center justify-center space-x-1 py-2 px-3 border border-slate-800 hover:bg-slate-800 rounded-lg text-xs font-medium text-slate-400 hover:text-slate-100 transition-colors"
          >
            <LogIn className="w-3.5 h-3.5" />
            <span>Đăng nhập</span>
          </Link>
          <Link
            href="/register"
            className="flex-1 flex items-center justify-center space-x-1 py-2 px-3 bg-slate-850 hover:bg-slate-800 border border-slate-800 rounded-lg text-xs font-medium text-slate-400 hover:text-slate-100 transition-colors"
          >
            <UserPlus className="w-3.5 h-3.5" />
            <span>Đăng ký</span>
          </Link>
        </div>
      </div>
    </aside>
  );
}
