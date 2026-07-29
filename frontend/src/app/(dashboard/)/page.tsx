"use client";

import { useState } from "react";
import { 
  TrendingUp, 
  ArrowUpRight, 
  ArrowDownLeft, 
  Wallet,
  CheckCircle,
  Clock,
  XCircle
} from "lucide-react";

/**
 * Trang Dashboard Tổng quan (Trang chủ /).
 * Hiển thị các chỉ số tài chính cơ bản và danh sách các ví của người dùng.
 */
export default function DashboardPage() {
  const [balance, setBalance] = useState("120,500,000");

  const kpis = [
    { name: "Tổng số dư", value: `${balance} VND`, change: "+3.2% hôm nay", icon: Wallet, color: "text-blue-500", bg: "bg-blue-500/10" },
    { name: "Giao dịch thành công", value: "1,245", change: "+14 giao dịch mới", icon: CheckCircle, color: "text-emerald-500", bg: "bg-emerald-500/10" },
    { name: "Giao dịch chờ xử lý", value: "25", change: "Đang xếp hàng Kafka", icon: Clock, color: "text-amber-500", bg: "bg-amber-500/10" },
    { name: "Giao dịch thất bại", value: "3", change: "Lỗi hệ thống/Hết hạn", icon: XCircle, color: "text-red-500", bg: "bg-red-500/10" },
  ];

  const mockWallets = [
    { id: 1, name: "Ví chính (Main Wallet)", balance: "120,500,000 VND", status: "ACTIVE", color: "border-emerald-500" },
    { id: 2, name: "Ví tiết kiệm (Savings Wallet)", balance: "50,000,000 VND", status: "ACTIVE", color: "border-blue-500" },
    { id: 3, name: "Ví ngoại tệ (USD Wallet)", balance: "3,250.00 USD", status: "ACTIVE", color: "border-amber-500" },
    { id: 4, name: "Ví doanh nghiệp (Business Wallet)", balance: "15,750,000 VND", status: "FROZEN", color: "border-red-550" },
  ];

  return (
    <div className="space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold text-slate-100 tracking-tight">Tổng quan hệ thống</h1>
        <p className="text-slate-400 mt-1">Chào mừng bạn quay trở lại. Dưới đây là trạng thái ví và luồng tiền hôm nay.</p>
      </div>

      {/* KPI Section */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {kpis.map((kpi, idx) => {
          const Icon = kpi.icon;
          return (
            <div key={idx} className="bg-slate-900 border border-slate-800 p-6 rounded-xl shadow-sm hover:border-slate-700 transition-all">
              <div className="flex items-center justify-between">
                <span className="text-sm font-medium text-slate-400">{kpi.name}</span>
                <div className={`w-10 h-10 rounded-lg ${kpi.bg} flex items-center justify-center`}>
                  <Icon className={`w-5 h-5 ${kpi.color}`} />
                </div>
              </div>
              <div className="mt-4">
                <h3 className="text-2xl font-bold text-slate-100">{kpi.value}</h3>
                <span className="text-xs text-slate-500 flex items-center mt-1">
                  <TrendingUp className="w-3 h-3 text-emerald-500 mr-1" />
                  {kpi.change}
                </span>
              </div>
            </div>
          );
        })}
      </div>

      {/* Wallets List Section */}
      <div className="space-y-4">
        <div className="flex justify-between items-center">
          <h2 className="text-xl font-bold text-slate-100">Ví điện tử của tôi</h2>
          <button className="px-4 py-2 bg-blue-600 hover:bg-blue-500 text-white rounded-lg text-sm font-semibold shadow-md shadow-blue-600/10 transition-colors">
            + Mở ví mới
          </button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {mockWallets.map((wallet) => (
            <div 
              key={wallet.id} 
              className={`bg-slate-900 border-l-4 ${wallet.color} border-slate-800 p-6 rounded-xl hover:border-slate-700 transition-all flex flex-col justify-between`}
            >
              <div>
                <span className="text-xs font-semibold text-slate-500 tracking-wider uppercase">VND</span>
                <h3 className="text-lg font-bold text-slate-200 mt-1">{wallet.name}</h3>
              </div>
              <div className="mt-6 flex justify-between items-end">
                <div>
                  <span className="text-xs text-slate-500">Số dư</span>
                  <p className="text-xl font-extrabold text-slate-100">{wallet.balance}</p>
                </div>
                <span className={`px-2.5 py-1 rounded-full text-xxs font-bold tracking-wider ${
                  wallet.status === "ACTIVE" 
                    ? "bg-emerald-500/10 text-emerald-500" 
                    : "bg-red-500/10 text-red-500"
                }`}>
                  {wallet.status}
                </span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
