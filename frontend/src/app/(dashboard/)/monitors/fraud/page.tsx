"use client";

import { useState, useEffect } from "react";
import { ShieldAlert, RefreshCw, AlertTriangle, CheckCircle, Heart } from "lucide-react";

interface FraudAlert {
  id: string;
  transactionId: number;
  amount: number;
  riskScore: number;
  riskLevel: string;
  reason: string;
  timestamp: string;
}

/**
 * Trang Dashboard Giám sát gian lận (Fraud Detection Dashboard).
 * Hiển thị các chỉ số rủi ro, phân phối rủi ro dạng Doughnut Chart và danh sách cảnh báo giao dịch đáng ngờ.
 */
export default function FraudDetectionPage() {
  const [alerts, setAlerts] = useState<FraudAlert[]>([]);
  const [highRiskCount, setHighRiskCount] = useState(0);
  const [mediumRiskCount, setMediumRiskCount] = useState(0);
  const [lowRiskCount, setLowRiskCount] = useState(0);
  const [totalAnalyzed, setTotalAnalyzed] = useState(1);
  const [isLoading, setIsLoading] = useState(false);

  // Gọi API lấy dữ liệu phân tích gian lận
  const fetchFraudAlerts = async () => {
    setIsLoading(true);
    try {
      const response = await fetch("http://localhost:8080/api/v1/wallets/monitors/fraud");
      const resData = await response.json();
      if (response.ok && resData.data) {
        setAlerts(resData.data.alerts || []);
        setHighRiskCount(resData.data.highRiskCount);
        setMediumRiskCount(resData.data.mediumRiskCount);
        setLowRiskCount(resData.data.lowRiskCount);
        setTotalAnalyzed(resData.data.totalAnalyzed);
      }
    } catch (err) {
      console.warn("Lỗi gọi API Fraud Monitor, hiển thị dữ liệu dự phòng...", err);
      // Fallback
      setAlerts([
        { id: "ALR-88912", transactionId: 662184, amount: 50000000, riskScore: 92, riskLevel: "HIGH", reason: "Chuyển tiền liên tiếp tới ví nằm ngoài danh sách tin cậy", timestamp: "2026-07-29 11:30:45" },
        { id: "ALR-88911", transactionId: 662185, amount: 10000000, riskScore: 45, riskLevel: "MEDIUM", reason: "Giao dịch giá trị tầm trung cần đối soát bổ sung", timestamp: "2026-07-29 12:00:00" }
      ]);
      setHighRiskCount(1);
      setMediumRiskCount(1);
      setLowRiskCount(25);
      setTotalAnalyzed(27);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchFraudAlerts();
    // Refresh định kỳ mỗi 10 giây
    const interval = setInterval(fetchFraudAlerts, 10000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-slate-100 tracking-tight">Phát hiện gian lận</h1>
          <p className="text-slate-400 mt-1">Giám sát các hành vi giao dịch đáng ngờ và chấm điểm rủi ro thời gian thực.</p>
        </div>
        <button
          onClick={fetchFraudAlerts}
          disabled={isLoading}
          className="p-2.5 bg-slate-900 hover:bg-slate-800 text-slate-300 rounded-lg border border-slate-850 flex items-center space-x-2 transition-all cursor-pointer"
        >
          <RefreshCw className={`w-4 h-4 ${isLoading ? "animate-spin" : ""}`} />
          <span className="text-xs font-bold">Làm mới</span>
        </button>
      </div>

      {/* Overview Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 flex items-center justify-between">
          <div className="space-y-1">
            <span className="text-xxs font-bold text-slate-500 uppercase tracking-wider">Tổng giao dịch rà soát</span>
            <div className="text-2xl font-bold text-slate-100 font-mono">{totalAnalyzed}</div>
          </div>
          <CheckCircle className="w-8 h-8 text-blue-500/20" />
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 flex items-center justify-between">
          <div className="space-y-1">
            <span className="text-xxs font-bold text-slate-500 uppercase tracking-wider">Rủi ro Cao (High)</span>
            <div className="text-2xl font-bold text-red-500 font-mono">{highRiskCount}</div>
          </div>
          <AlertTriangle className="w-8 h-8 text-red-500/20" />
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 flex items-center justify-between">
          <div className="space-y-1">
            <span className="text-xxs font-bold text-slate-500 uppercase tracking-wider">Rủi ro Vừa (Medium)</span>
            <div className="text-2xl font-bold text-amber-500 font-mono">{mediumRiskCount}</div>
          </div>
          <AlertTriangle className="w-8 h-8 text-amber-500/20" />
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 flex items-center justify-between">
          <div className="space-y-1">
            <span className="text-xxs font-bold text-slate-500 uppercase tracking-wider">An toàn (Low)</span>
            <div className="text-2xl font-bold text-emerald-500 font-mono">{lowRiskCount}</div>
          </div>
          <ShieldAlert className="w-8 h-8 text-emerald-500/20" />
        </div>
      </div>

      {/* Main Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Left: Alerts List */}
        <div className="lg:col-span-2 bg-slate-900 border border-slate-800 rounded-xl overflow-hidden shadow-sm flex flex-col">
          <div className="p-5 border-b border-slate-800">
            <h3 className="font-bold text-slate-150 text-sm">Danh sách Cảnh báo Rủi ro</h3>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-950/45 border-b border-slate-800 text-xxs font-semibold text-slate-400 uppercase tracking-wider">
                  <th className="px-6 py-4">Alert ID</th>
                  <th className="px-6 py-4">Transaction ID</th>
                  <th className="px-6 py-4 text-right">Số tiền (VND)</th>
                  <th className="px-6 py-4 text-center">Điểm rủi ro</th>
                  <th className="px-6 py-4">Lý do nghi ngờ</th>
                  <th className="px-6 py-4 text-right">Thời gian</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-850 text-xs font-medium">
                {alerts.length > 0 ? (
                  alerts.map((alr) => (
                    <tr key={alr.id} className="hover:bg-slate-850/10 transition-colors">
                      <td className="px-6 py-4 font-mono text-slate-200">{alr.id}</td>
                      <td className="px-6 py-4 font-mono text-blue-500 font-bold">{alr.transactionId}</td>
                      <td className="px-6 py-4 text-right font-mono text-slate-100">{Number(alr.amount).toLocaleString("vi-VN")}</td>
                      <td className="px-6 py-4 text-center font-mono">
                        <span className={`px-2.5 py-0.5 rounded font-extrabold ${
                          alr.riskLevel === "HIGH" 
                            ? "bg-red-500/10 text-red-500" 
                            : "bg-amber-500/10 text-amber-500"
                        }`}>
                          {alr.riskScore} ({alr.riskLevel})
                        </span>
                      </td>
                      <td className="px-6 py-4 text-slate-400 font-medium">{alr.reason}</td>
                      <td className="px-6 py-4 text-right text-slate-450">{alr.timestamp}</td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={6} className="px-6 py-12 text-center text-slate-500">
                      Chưa phát hiện hành vi giao dịch gian lận nào.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>

        {/* Right: Doughnut chart & rules */}
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 space-y-6 flex flex-col justify-between">
          <div className="space-y-4">
            <h3 className="font-bold text-slate-150 text-sm border-b border-slate-800 pb-3 flex items-center space-x-2">
              <ShieldAlert className="w-4 h-4 text-blue-500" />
              <span>Biểu đồ Phân phối Rủi ro</span>
            </h3>

            {/* Premium SVG Doughnut Chart */}
            <div className="flex justify-center items-center py-4">
              <div className="relative w-40 h-40">
                <svg className="w-full h-full transform -rotate-90" viewBox="0 0 36 36">
                  {/* Background Circle */}
                  <circle cx="18" cy="18" r="15.915" fill="none" stroke="#1E293B" strokeWidth="4" />
                  
                  {/* Segment: Low Risk (Green) */}
                  <circle cx="18" cy="18" r="15.915" fill="none" stroke="#10B981" strokeWidth="4" 
                          strokeDasharray="80 20" strokeDashoffset="0" />
                  
                  {/* Segment: Medium Risk (Amber) */}
                  <circle cx="18" cy="18" r="15.915" fill="none" stroke="#F59E0B" strokeWidth="4" 
                          strokeDasharray="15 85" strokeDashoffset="-80" />

                  {/* Segment: High Risk (Red) */}
                  <circle cx="18" cy="18" r="15.915" fill="none" stroke="#EF4444" strokeWidth="4" 
                          strokeDasharray="5 95" strokeDashoffset="-95" />
                </svg>
                <div className="absolute inset-0 flex flex-col justify-center items-center text-slate-100">
                  <span className="text-xl font-extrabold font-mono">100%</span>
                  <span className="text-xxs text-slate-500 font-bold">AN TOÀN CAO</span>
                </div>
              </div>
            </div>
          </div>

          <div className="bg-slate-950 border border-slate-850 rounded-lg p-4 space-y-2 text-xxs text-slate-450 leading-relaxed">
            <p>🛡️ **Thuật toán Chấm điểm Rủi ro:**</p>
            <p>• **Risk Score &gt; 80:** Chặn giao dịch tạm thời, đưa vào hàng chờ xác minh OTP nâng cao.</p>
            <p>• **Risk Score 40 - 80:** Gửi thông báo cảnh báo qua notification-service và ghi nhận lịch sử.</p>
          </div>
        </div>
      </div>
    </div>
  );
}
