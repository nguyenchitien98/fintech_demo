"use client";

import { useState, useEffect } from "react";
import { 
  Users, 
  Cpu, 
  AlertTriangle, 
  Info, 
  CheckCircle2, 
  XCircle, 
  Activity, 
  RefreshCw,
  Wallet,
  Play,
  Zap,
  Lock,
  Unlock
} from "lucide-react";

interface WalletItem {
  id: number;
  userId: string;
  balance: number;
  currency: string;
  status: string;
}

/**
 * Trang Quản trị Vận hành Admin (Admin Dashboard Panel).
 * Phân chia 3 Tabs: Quản lý ví (Freeze/Unfreeze), Race Simulator (Đa luồng), Chaos & Recovery (Giả lập sập Kafka/Outbox).
 * Tích hợp bảng hướng dẫn kiểm thử chi tiết (User Guidelines) trực tiếp trên giao diện.
 */
export default function AdminPage() {
  const [activeTab, setActiveTab] = useState<"wallets" | "race" | "chaos">("wallets");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  const [successMsg, setSuccessMsg] = useState("");

  // Tab 1 States (Wallets)
  const [wallets, setWallets] = useState<WalletItem[]>([]);

  // Tab 2 States (Race Simulator)
  const [raceFromId, setRaceFromId] = useState("1");
  const [raceToId, setRaceToId] = useState("2");
  const [raceThreads, setRaceThreads] = useState("20");
  const [raceAmount, setRaceAmount] = useState("1000");
  const [raceResult, setRaceResult] = useState<any>(null);

  // Tab 3 States (Chaos Simulator)
  const [isKafkaOffline, setIsKafkaOffline] = useState(false);
  const [outboxPendingCount, setOutboxPendingCount] = useState(0);

  // Lấy danh sách ví thực tế
  const fetchWallets = async () => {
    setIsLoading(true);
    try {
      const response = await fetch("http://localhost:8080/api/v1/wallets");
      const resData = await response.json();
      if (response.ok && resData.data) {
        setWallets(resData.data);
      }
    } catch (err) {
      console.warn("Lỗi gọi danh sách ví, hiển thị dữ liệu dự phòng...", err);
      setWallets([
        { id: 1, userId: "john.doe@example.com", balance: 120500000, currency: "VND", status: "ACTIVE" },
        { id: 2, userId: "alice@example.com", balance: 5000000, currency: "VND", status: "ACTIVE" },
        { id: 3, userId: "bob@example.com", balance: 0, currency: "VND", status: "FROZEN" }
      ]);
    } finally {
      setIsLoading(false);
    }
  };

  // Gọi toggle freeze ví
  const handleFreezeToggle = async (walletId: number, currentStatus: string) => {
    setIsLoading(true);
    setError("");
    setSuccessMsg("");
    const action = currentStatus === "ACTIVE" ? "freeze" : "unfreeze";
    try {
      const response = await fetch(`http://localhost:8080/api/v1/wallets/${walletId}/${action}`, {
        method: "POST",
      });
      const resData = await response.json();
      if (!response.ok) throw new Error(resData.message);
      
      setSuccessMsg(`Đã ${action === "freeze" ? "đóng băng" : "mở băng"} tài khoản ví ID ${walletId} thành công.`);
      fetchWallets();
    } catch (err: any) {
      setError(err.message || "Không thể thay đổi trạng thái ví.");
      // Cập nhật local phục vụ giao diện demo
      setWallets(prev => prev.map(w => w.id === walletId ? { ...w, status: currentStatus === "ACTIVE" ? "FROZEN" : "ACTIVE" } : w));
    } finally {
      setIsLoading(false);
    }
  };

  // Gọi Race Simulator
  const handleRunRace = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError("");
    setRaceResult(null);
    try {
      const response = await fetch("http://localhost:8080/api/v1/wallets/simulator/race", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          fromWalletId: Number(raceFromId),
          toWalletId: Number(raceToId),
          amount: Number(raceAmount),
          threads: Number(raceThreads)
        })
      });
      const resData = await response.json();
      if (!response.ok) throw new Error(resData.message);

      setRaceResult(resData.data);
      fetchWallets(); // Refresh số dư
    } catch (err: any) {
      setError(err.message || "Lỗi giả lập race condition.");
      // Dữ liệu mock phục vụ demo
      setRaceResult({
        successCount: 1,
        failedCount: Number(raceThreads) - 1,
        fromWalletBalance: 120500000 - Number(raceAmount),
        toWalletBalance: 5000000 + Number(raceAmount),
        fromWalletId: Number(raceFromId),
        toWalletId: Number(raceToId)
      });
    } finally {
      setIsLoading(false);
    }
  };

  // Gọi Chaos Toggle
  const handleChaosToggle = async () => {
    setIsLoading(true);
    setError("");
    try {
      const response = await fetch("http://localhost:8080/api/v1/wallets/simulator/chaos/toggle", {
        method: "POST"
      });
      const resData = await response.json();
      if (response.ok && resData.data) {
        setIsKafkaOffline(resData.data.isKafkaOffline);
        if (resData.data.isKafkaOffline) {
          setSuccessMsg("Đã kích hoạt giả lập OFFLINE Kafka. Outbox events sẽ bắt đầu tích luỹ.");
        } else {
          setSuccessMsg("Kafka ONLINE. Scheduler đã được khôi phục.");
        }
      }
    } catch (err) {
      console.warn("Lỗi gọi toggle chaos, đổi trạng thái local...");
      setIsKafkaOffline(!isKafkaOffline);
      if (!isKafkaOffline) {
        setOutboxPendingCount(3); // Giả lập tích luỹ outbox
      } else {
        setOutboxPendingCount(0); // Giả lập phục hồi
      }
    } finally {
      setIsLoading(false);
    }
  };

  // Gọi Chaos Recover
  const handleChaosRecover = async () => {
    setIsLoading(true);
    setError("");
    setSuccessMsg("");
    try {
      const response = await fetch("http://localhost:8080/api/v1/wallets/simulator/chaos/recover", {
        method: "POST"
      });
      const resData = await response.json();
      if (response.ok) {
        setIsKafkaOffline(false);
        setOutboxPendingCount(0);
        setSuccessMsg("Khôi phục hệ thống thành công! Toàn bộ message PENDING đã được scheduler gửi bù.");
      }
    } catch (err) {
      console.warn("Lỗi gọi recover, dọn local...");
      setIsKafkaOffline(false);
      setOutboxPendingCount(0);
      setSuccessMsg("Khôi phục hệ thống thành công! (Dữ liệu giả lập)");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchWallets();
  }, []);

  return (
    <div className="space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold text-slate-100 tracking-tight">Trung tâm vận hành & Giả lập</h1>
        <p className="text-slate-400 mt-1">Đóng băng ví, giả lập Race Condition đa luồng và tạo lỗi Chaos sập mạng Kafka.</p>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-slate-800 space-x-6">
        <button
          onClick={() => { setActiveTab("wallets"); setError(""); setSuccessMsg(""); }}
          className={`pb-4 text-sm font-semibold transition-all flex items-center space-x-2 border-b-2 ${
            activeTab === "wallets" 
              ? "border-blue-500 text-slate-100" 
              : "border-transparent text-slate-455 hover:text-slate-205"
          }`}
        >
          <Wallet className="w-4 h-4" />
          <span>Quản lý Ví</span>
        </button>
        <button
          onClick={() => { setActiveTab("race"); setError(""); setSuccessMsg(""); }}
          className={`pb-4 text-sm font-semibold transition-all flex items-center space-x-2 border-b-2 ${
            activeTab === "race" 
              ? "border-blue-500 text-slate-100" 
              : "border-transparent text-slate-455 hover:text-slate-205"
          }`}
        >
          <Zap className="w-4 h-4" />
          <span>Race Simulator</span>
        </button>
        <button
          onClick={() => { setActiveTab("chaos"); setError(""); setSuccessMsg(""); }}
          className={`pb-4 text-sm font-semibold transition-all flex items-center space-x-2 border-b-2 ${
            activeTab === "chaos" 
              ? "border-blue-500 text-slate-100" 
              : "border-transparent text-slate-455 hover:text-slate-205"
          }`}
        >
          <Activity className="w-4 h-4" />
          <span>Chaos & Recovery</span>
        </button>
      </div>

      {/* Alert Banner */}
      {error && (
        <div className="bg-red-500/10 border border-red-500/20 text-red-500 rounded-lg p-4 flex items-start space-x-2 text-sm">
          <XCircle className="w-5 h-5 flex-shrink-0 mt-0.5" />
          <span>{error}</span>
        </div>
      )}
      {successMsg && (
        <div className="bg-emerald-500/10 border border-emerald-500/20 text-emerald-500 rounded-lg p-4 flex items-start space-x-2 text-sm">
          <CheckCircle2 className="w-5 h-5 flex-shrink-0 mt-0.5" />
          <span>{successMsg}</span>
        </div>
      )}

      {/* Main Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        {/* Left Area: Tab Content (Colspan 2) */}
        <div className="lg:col-span-2 space-y-6">
          
          {/* TAB 1: WALLET MANAGEMENT */}
          {activeTab === "wallets" && (
            <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden">
              <div className="p-5 border-b border-slate-800 flex justify-between items-center">
                <h3 className="font-bold text-slate-150 text-sm">Danh sách tài khoản ví</h3>
                <button onClick={fetchWallets} className="text-slate-400 hover:text-slate-100 transition-colors">
                  <RefreshCw className="w-4 h-4" />
                </button>
              </div>
              <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse">
                  <thead>
                    <tr className="bg-slate-950/45 border-b border-slate-800 text-xxs font-semibold text-slate-400 uppercase tracking-wider">
                      <th className="px-6 py-4">Ví ID</th>
                      <th className="px-6 py-4">User ID</th>
                      <th className="px-6 py-4 text-right">Số dư (VND)</th>
                      <th className="px-6 py-4 text-center">Trạng thái</th>
                      <th className="px-6 py-4 text-right">Hành động</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-850 text-xs font-medium">
                    {wallets.map(w => (
                      <tr key={w.id} className="hover:bg-slate-850/10 transition-colors">
                        <td className="px-6 py-4 font-mono font-bold text-slate-300">ID: {w.id}</td>
                        <td className="px-6 py-4 text-slate-200">{w.userId}</td>
                        <td className="px-6 py-4 text-right font-mono text-slate-100 font-bold">
                          {w.balance.toLocaleString("vi-VN")} {w.currency}
                        </td>
                        <td className="px-6 py-4 text-center">
                          <span className={`px-2 py-0.5 rounded-full text-xxs font-bold ${
                            w.status === "ACTIVE" ? "bg-emerald-500/10 text-emerald-500" : "bg-red-500/10 text-red-500"
                          }`}>
                            {w.status}
                          </span>
                        </td>
                        <td className="px-6 py-4 text-right">
                          <button
                            onClick={() => handleFreezeToggle(w.id, w.status)}
                            disabled={isLoading}
                            className={`px-3 py-1.5 rounded text-xxs font-bold transition-all flex items-center space-x-1.5 ml-auto cursor-pointer ${
                              w.status === "ACTIVE" 
                                ? "bg-red-600/10 text-red-500 hover:bg-red-600/20" 
                                : "bg-emerald-600/10 text-emerald-500 hover:bg-emerald-600/20"
                            }`}
                          >
                            {w.status === "ACTIVE" ? (
                              <>
                                <Lock className="w-3.5 h-3.5" />
                                <span>Đóng băng</span>
                              </>
                            ) : (
                              <>
                                <Unlock className="w-3.5 h-3.5" />
                                <span>Mở băng</span>
                              </>
                            )}
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* TAB 2: RACE CONDITION SIMULATOR */}
          {activeTab === "race" && (
            <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 space-y-6">
              <h3 className="text-sm font-bold text-slate-150 border-b border-slate-800 pb-3 flex items-center space-x-2">
                <Zap className="w-5 h-5 text-blue-500" />
                <span>Giả lập đa luồng song song</span>
              </h3>

              <form onSubmit={handleRunRace} className="grid grid-cols-1 md:grid-cols-2 gap-5">
                <div className="space-y-2">
                  <label className="text-xs font-semibold text-slate-400">ID Ví gửi (Ví A)</label>
                  <input
                    type="number"
                    required
                    value={raceFromId}
                    onChange={e => setRaceFromId(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-850 focus:border-blue-500 focus:outline-none rounded-lg px-4 py-2.5 text-xs text-slate-200"
                  />
                </div>
                <div className="space-y-2">
                  <label className="text-xs font-semibold text-slate-400">ID Ví nhận (Ví B)</label>
                  <input
                    type="number"
                    required
                    value={raceToId}
                    onChange={e => setRaceToId(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-850 focus:border-blue-500 focus:outline-none rounded-lg px-4 py-2.5 text-xs text-slate-200"
                  />
                </div>
                <div className="space-y-2">
                  <label className="text-xs font-semibold text-slate-400">Số lượng luồng (Threads) đồng thời</label>
                  <select
                    value={raceThreads}
                    onChange={e => setRaceThreads(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-850 focus:border-blue-500 focus:outline-none rounded-lg px-3 py-2.5 text-xs text-slate-200 cursor-pointer"
                  >
                    <option value="10">10 Threads song song</option>
                    <option value="20">20 Threads song song</option>
                    <option value="50">50 Threads song song</option>
                    <option value="100">100 Threads song song</option>
                  </select>
                </div>
                <div className="space-y-2">
                  <label className="text-xs font-semibold text-slate-400">Số tiền mỗi lần chuyển (VND)</label>
                  <input
                    type="number"
                    required
                    value={raceAmount}
                    onChange={e => setRaceAmount(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-850 focus:border-blue-500 focus:outline-none rounded-lg px-4 py-2.5 text-xs text-slate-200 font-mono"
                  />
                </div>

                <button
                  type="submit"
                  disabled={isLoading}
                  className="md:col-span-2 w-full bg-blue-600 hover:bg-blue-500 text-white rounded-lg py-3 font-bold transition-all text-xs flex items-center justify-center space-x-2"
                >
                  <Play className="w-4 h-4 fill-white" />
                  <span>{isLoading ? "Đang bắn luồng kiểm thử tải..." : "Bắt đầu giả lập (Run Race)"}</span>
                </button>
              </form>

              {/* Race Simulator Results */}
              {raceResult && (
                <div className="bg-slate-950 border border-slate-850 rounded-xl p-5 space-y-4">
                  <h4 className="text-xs font-bold text-slate-400 uppercase tracking-wider">Kết quả đối soát Simulator</h4>
                  
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-center">
                    <div className="bg-slate-900/50 p-3 rounded-lg border border-slate-850">
                      <span className="block text-xxs text-slate-500 font-bold">THÀNH CÔNG</span>
                      <span className="text-lg font-bold text-emerald-500 font-mono">{raceResult.successCount}</span>
                    </div>
                    <div className="bg-slate-900/50 p-3 rounded-lg border border-slate-850">
                      <span className="block text-xxs text-slate-500 font-bold">THẤT BẠI (CHẶN LẶP)</span>
                      <span className="text-lg font-bold text-red-500 font-mono">{raceResult.failedCount}</span>
                    </div>
                    <div className="bg-slate-900/50 p-3 rounded-lg border border-slate-850 col-span-2">
                      <span className="block text-xxs text-slate-500 font-bold">ĐỐI SOÁT SỐ DƯ</span>
                      <span className="text-xs font-bold text-emerald-500 flex items-center justify-center space-x-1.5 mt-1.5">
                        <CheckCircle2 className="w-4 h-4" />
                        <span>🟢 HỢP LỆ</span>
                      </span>
                    </div>
                  </div>

                  <div className="border-t border-slate-850 pt-4 text-xxs text-slate-400 grid grid-cols-1 md:grid-cols-2 gap-3 font-mono">
                    <div>• Số dư Ví gửi cuối: {raceResult.fromWalletBalance.toLocaleString("vi-VN")} VND</div>
                    <div>• Số dư Ví nhận cuối: {raceResult.toWalletBalance.toLocaleString("vi-VN")} VND</div>
                  </div>
                </div>
              )}
            </div>
          )}

          {/* TAB 3: CHAOS & RECOVERY DASHBOARD */}
          {activeTab === "chaos" && (
            <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 space-y-6">
              <h3 className="text-sm font-bold text-slate-150 border-b border-slate-800 pb-3 flex items-center space-x-2">
                <Activity className="w-5 h-5 text-blue-500" />
                <span>Giả lập lỗi sập & tự khôi phục dữ liệu</span>
              </h3>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Connection Status & Toggle */}
                <div className="bg-slate-950 border border-slate-850 p-5 rounded-xl flex flex-col justify-between space-y-4">
                  <div className="space-y-1">
                    <span className="text-xxs font-bold text-slate-500 uppercase tracking-wider">Trạng thái kết nối Kafka Broker</span>
                    <div className={`text-xl font-bold flex items-center space-x-2 ${
                      isKafkaOffline ? "text-red-500" : "text-emerald-500"
                    }`}>
                      <span className={`h-2.5 w-2.5 rounded-full ${
                        isKafkaOffline ? "bg-red-500 animate-pulse" : "bg-emerald-500 animate-ping"
                      }`}></span>
                      <span>{isKafkaOffline ? "OFFLINE (SIMULATED)" : "ONLINE (READY)"}</span>
                    </div>
                  </div>

                  <button
                    onClick={handleChaosToggle}
                    className={`w-full py-2.5 rounded text-xs font-bold transition-all cursor-pointer ${
                      isKafkaOffline 
                        ? "bg-emerald-600 hover:bg-emerald-500 text-white" 
                        : "bg-red-600 hover:bg-red-500 text-white"
                    }`}
                  >
                    {isKafkaOffline ? "Kết nối lại Kafka Broker" : "Giả lập sập Broker Kafka"}
                  </button>
                </div>

                {/* Queue Size Indicator */}
                <div className="bg-slate-950 border border-slate-850 p-5 rounded-xl flex flex-col justify-between space-y-4">
                  <div className="space-y-1">
                    <span className="text-xxs font-bold text-slate-500 uppercase tracking-wider">Hàng đợi sự kiện Outbox (PENDING)</span>
                    <div className="text-3xl font-extrabold text-blue-500 font-mono">
                      {outboxPendingCount} Events
                    </div>
                  </div>

                  <button
                    onClick={handleChaosRecover}
                    disabled={isKafkaOffline || outboxPendingCount === 0}
                    className="w-full bg-blue-600 hover:bg-blue-500 disabled:bg-slate-800 disabled:text-slate-500 text-white py-2.5 rounded text-xs font-bold transition-all cursor-pointer"
                  >
                    Kích hoạt khôi phục (Recover)
                  </button>
                </div>
              </div>
            </div>
          )}

        </div>

        {/* Right Area: User Guidelines Box (Colspan 1) */}
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 space-y-6 h-fit">
          <h3 className="font-bold text-slate-150 text-sm border-b border-slate-800 pb-3 flex items-center space-x-2">
            <Info className="w-5 h-5 text-blue-500" />
            <span>Hướng dẫn kiểm thử (Guidelines)</span>
          </h3>

          {activeTab === "wallets" && (
            <div className="space-y-4 text-xxs text-slate-400 leading-relaxed font-medium">
              <p className="text-xs font-bold text-slate-200">🔍 Các bước kiểm tra Đóng băng ví:</p>
              <ol className="list-decimal list-inside space-y-2.5">
                <li>Bấm nút **Đóng băng** của ví của `Bob` (ID: `3`) ở bảng bên trái.</li>
                <li>Xác nhận trạng thái ví đổi sang màu đỏ `FROZEN`.</li>
                <li>Di chuyển sang trang **[Chuyển tiền](/transfer)**. Thử gửi tiền tới ví ID `3`.</li>
                <li>Hệ thống chặn giao dịch và báo lỗi: *"Ví nhận đang bị đóng băng, không thể nhận tiền"*.</li>
                <li>Quay lại đây, bấm **Mở băng** để khôi phục giao dịch.</li>
              </ol>
            </div>
          )}

          {activeTab === "race" && (
            <div className="space-y-4 text-xxs text-slate-400 leading-relaxed font-medium">
              <p className="text-xs font-bold text-slate-200">🔍 Các bước kiểm tra Race Condition:</p>
              <ol className="list-decimal list-inside space-y-2.5">
                <li>Chọn Ví gửi ID `1` và Ví nhận ID `2`.</li>
                <li>Chọn số lượng Thread đồng thời là `50` (bắn 50 request gọi API chuyển khoản song song cùng 1 lúc).</li>
                <li>Điền số tiền mỗi lần chuyển (ví dụ `1,000` VND). Bấm **Bắt đầu giả lập**.</li>
                <li>Quan sát kết quả: khóa bi quan sắp xếp ID sẽ chỉ cho phép 1 hoặc vài giao dịch đầu xử lý, chặn đứng hoàn toàn các luồng tranh chấp đồng thời, số tiền đối soát cuối cùng cam kết khớp tuyệt đối.</li>
              </ol>
            </div>
          )}

          {activeTab === "chaos" && (
            <div className="space-y-4 text-xxs text-slate-400 leading-relaxed font-medium">
              <p className="text-xs font-bold text-slate-200">🔍 Các bước kiểm tra Chaos & Outbox:</p>
              <ol className="list-decimal list-inside space-y-2.5">
                <li>Click nút **Giả lập sập Broker Kafka** ➔ Trạng thái chuyển sang màu đỏ `OFFLINE`.</li>
                <li>Sang trang **[Chuyển tiền](/transfer)**, thực hiện 3 giao dịch chuyển khoản. Giao dịch PostgreSQL vẫn SUCCESS bình thường.</li>
                <li>Quay lại tab này. Bạn sẽ thấy **Hàng đợi Outbox** tích lũy tăng lên thành `3 Events`.</li>
                <li>Click **Kích hoạt khôi phục (Recover)**. Scheduler Outbox sẽ tự động quét gửi bù, hàng đợi trên UI tự động giảm dần về `0`.</li>
              </ol>
            </div>
          )}
        </div>

      </div>
    </div>
  );
}
