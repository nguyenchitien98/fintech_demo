"use client";

import { useState } from "react";
import { 
  History, 
  Database, 
  Search, 
  SlidersHorizontal,
  Download,
  ChevronLeft,
  ChevronRight,
  ArrowRight,
  CheckCircle2,
  Clock,
  XCircle,
  X,
  Layers,
  ArrowDownLeft,
  ArrowUpRight
} from "lucide-react";

interface Transaction {
  id: string;
  fromWallet: string;
  toWallet: string;
  amount: string;
  status: string;
  channel: string;
  description: string;
  createdAt: string;
}

/**
 * Trang Lịch sử Giao dịch và Kiểm toán Sổ cái (2 Tabs).
 * - Tab 1: Lịch sử giao dịch (Transaction History) - Nhấp vào ID để xem Sơ đồ Processing Timeline.
 * - Tab 2: Kiểm toán sổ cái (Ledger Explorer) - Đối soát Nợ/Có.
 */
export default function TransactionsHistoryPage() {
  const [activeTab, setActiveTab] = useState<"history" | "ledger">("history");
  const [searchTerm, setSearchTerm] = useState("");
  const [filterStatus, setFilterStatus] = useState("ALL");
  const [filterType, setFilterType] = useState("ALL"); 
  const [currentPage, setCurrentPage] = useState(1);
  
  // Trạng thái mở modal chi tiết giao dịch
  const [selectedTx, setSelectedTx] = useState<Transaction | null>(null);

  // 1. Mock dữ liệu Lịch sử giao dịch (Transaction History)
  const mockTransactions: Transaction[] = [
    { id: "TXN-662189", fromWallet: "Ví chính (ID: 1)", toWallet: "Ví tiết kiệm (ID: 2)", amount: "2,000,000", status: "SUCCESS", channel: "FinWallet Lõi", description: "Chuyển tiền tiết kiệm tháng 7", createdAt: "2026-07-29 14:30:22" },
    { id: "TXN-662188", fromWallet: "Cổng thanh toán (ID: 5)", toWallet: "Ví chính (ID: 1)", amount: "5,000,000", status: "SUCCESS", channel: "Bank Gateway", description: "Nạp tiền từ ngân hàng liên kết", createdAt: "2026-07-29 14:15:10" },
    { id: "TXN-662187", fromWallet: "Ví tiết kiệm (ID: 2)", toWallet: "Ví chính (ID: 1)", amount: "1,200,000", status: "SUCCESS", channel: "FinWallet Lõi", description: "Rút tiền chi tiêu khẩn cấp", createdAt: "2026-07-29 13:50:33" },
    { id: "TXN-662186", fromWallet: "Ví chính (ID: 1)", toWallet: "Hệ thống Amazon (ID: 8)", amount: "320,000", status: "SUCCESS", channel: "E-Commerce API", description: "Thanh toán hóa đơn mua sắm Amazon", createdAt: "2026-07-29 13:20:11" },
    { id: "TXN-662185", fromWallet: "Ví chính (ID: 1)", toWallet: "Ví tiết kiệm (ID: 2)", amount: "1,000,000", status: "PENDING", channel: "FinWallet Lõi", description: "Đang xử lý tích lũy tự động", createdAt: "2026-07-29 12:00:00" },
    { id: "TXN-662184", fromWallet: "Ví chính (ID: 1)", toWallet: "Ví lạ (ID: 99)", amount: "50,000,000", status: "FAILED", channel: "FinWallet Lõi", description: "Giao dịch bị chặn do nghi ngờ gian lận", createdAt: "2026-07-29 11:30:45" },
  ];

  // 2. Mock dữ liệu Kiểm toán sổ cái (Ledger Explorer)
  const mockLedgerEntries = [
    { id: "LED-772882", txId: "TXN-662189", walletName: "Ví chính (John Doe)", type: "DEBIT", debit: "2,000,000", credit: "-", balance: "118,500,000", createdAt: "2026-07-29 14:30:22" },
    { id: "LED-772881", txId: "TXN-662189", walletName: "Ví tiết kiệm (Alice)", type: "CREDIT", debit: "-", credit: "2,000,000", balance: "120,500,000", createdAt: "2026-07-29 14:30:22" },
    { id: "LED-772880", txId: "TXN-662188", walletName: "Ví ngoại tệ (John Doe)", type: "DEBIT", debit: "5,000,000", credit: "-", balance: "113,500,000", createdAt: "2026-07-29 14:15:10" },
    { id: "LED-772879", txId: "TXN-662188", walletName: "Hệ thống (Bank Gateway)", type: "CREDIT", debit: "-", credit: "5,000,000", balance: "118,500,000", createdAt: "2026-07-29 14:15:10" },
    { id: "LED-772878", txId: "TXN-662187", walletName: "Ví chính (John Doe)", type: "CREDIT", debit: "-", credit: "1,200,000", balance: "115,500,000", createdAt: "2026-07-29 13:50:33" },
    { id: "LED-772877", txId: "TXN-662187", walletName: "Ví chính (Alice)", type: "DEBIT", debit: "1,200,000", credit: "-", balance: "113,500,000", createdAt: "2026-07-29 13:50:33" },
    { id: "LED-772876", txId: "TXN-662186", walletName: "Ví chính (John Doe)", type: "DEBIT", debit: "320,000", credit: "-", balance: "113,100,000", createdAt: "2026-07-29 13:20:11" },
    { id: "LED-772875", txId: "TXN-662186", walletName: "Cổng thanh toán (Amazon)", type: "CREDIT", debit: "-", credit: "320,000", balance: "113,100,000", createdAt: "2026-07-29 13:20:11" },
  ];

  // Lọc danh sách giao dịch tổng quan (Tab 1)
  const filteredTransactions = mockTransactions.filter(tx => {
    const matchesSearch = tx.id.toLowerCase().includes(searchTerm.toLowerCase()) || 
                          tx.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
                          tx.fromWallet.toLowerCase().includes(searchTerm.toLowerCase()) ||
                          tx.toWallet.toLowerCase().includes(searchTerm.toLowerCase());
    
    const matchesStatus = filterStatus === "ALL" || tx.status === filterStatus;
    return matchesSearch && matchesStatus;
  });

  // Lọc danh sách bút toán sổ cái (Tab 2)
  const filteredLedgerEntries = mockLedgerEntries.filter(entry => {
    const matchesSearch = entry.txId.toLowerCase().includes(searchTerm.toLowerCase()) || 
                          entry.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
                          entry.walletName.toLowerCase().includes(searchTerm.toLowerCase());
    
    const matchesType = filterType === "ALL" || entry.type === filterType;
    return matchesSearch && matchesType;
  });

  // Sơ đồ Processing Timeline giả lập cho selected transaction
  const processingTimeline = [
    { title: "API Gateway", description: "Tiếp nhận request, kiểm tra JWT Blacklist & giới hạn Rate limit IP Client", time: "14:30:22.001" },
    { title: "Wallet Service (Validation)", description: "Xác minh số dư khả dụng của ví gửi và kiểm soát ví không đóng băng", time: "14:30:22.012" },
    { title: "Redis Idempotency Check", description: "Xác minh key kháng lặp X-Idempotency-Key thành công ➔ Tạo khóa tạm", time: "14:30:22.025" },
    { title: "PostgreSQL (Ledger Write)", description: "Thực thi ghi sổ bút toán kép (1 DEBIT ví gửi, 1 CREDIT ví nhận) trong DB transaction", time: "14:30:22.044" },
    { title: "Outbox Publisher", description: "Lưu bản ghi OutboxEvent trạng thái PENDING thành công trong cùng transaction", time: "14:30:22.045" },
    { title: "Kafka Broker (transaction-events)", description: "Scheduler quét OutboxPoller đẩy gói tin JSON thành công sang topic Kafka", time: "14:30:22.112" },
    { title: "Notification Service", description: "Kafka Consumer tiêu thụ message và truyền phát Server-Sent Events (SSE)", time: "14:30:22.135" },
    { title: "Completed", description: "Client nhận SSE Toast hiển thị thông báo biến động số dư ➔ Hoàn tất quy trình", time: "14:30:22.140" },
  ];

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold text-slate-100 tracking-tight">Nhật ký & Sổ cái</h1>
          <p className="text-slate-400 mt-1">Lịch sử giao dịch thời gian thực tích hợp đối soát kế toán sổ kép.</p>
        </div>
        <button className="flex items-center space-x-2 px-4 py-2.5 bg-slate-900 border border-slate-800 hover:bg-slate-800 rounded-lg text-sm font-semibold text-slate-300 hover:text-slate-100 transition-colors">
          <Download className="w-4 h-4" />
          <span>Xuất báo cáo CSV</span>
        </button>
      </div>

      {/* Tabs Switcher */}
      <div className="flex border-b border-slate-800 space-x-6">
        <button
          onClick={() => { setActiveTab("history"); setSearchTerm(""); setCurrentPage(1); }}
          className={`pb-4 text-sm font-semibold transition-all flex items-center space-x-2 border-b-2 ${
            activeTab === "history" 
              ? "border-blue-500 text-slate-100" 
              : "border-transparent text-slate-450 hover:text-slate-200"
          }`}
        >
          <History className="w-4 h-4" />
          <span>Lịch sử giao dịch</span>
        </button>
        <button
          onClick={() => { setActiveTab("ledger"); setSearchTerm(""); setCurrentPage(1); }}
          className={`pb-4 text-sm font-semibold transition-all flex items-center space-x-2 border-b-2 ${
            activeTab === "ledger" 
              ? "border-blue-500 text-slate-100" 
              : "border-transparent text-slate-450 hover:text-slate-200"
          }`}
        >
          <Database className="w-4 h-4" />
          <span>Kiểm toán Sổ cái (Ledger)</span>
        </button>
      </div>

      {/* Filters Bar */}
      <div className="bg-slate-900 border border-slate-800 p-4 rounded-xl flex flex-col md:flex-row gap-4 items-center justify-between">
        {/* Search Input */}
        <div className="relative w-full md:w-80">
          <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
          <input
            type="text"
            placeholder={activeTab === "history" ? "Tìm mã giao dịch, ví gửi..." : "Tìm mã Ledger, mã giao dịch..."}
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full bg-slate-950 border border-slate-850 rounded-lg pl-10 pr-4 py-2.5 text-sm text-slate-200 placeholder-slate-500 focus:outline-none focus:border-blue-500 transition-colors"
          />
        </div>

        {/* Action Selects */}
        <div className="flex flex-wrap gap-4 w-full md:w-auto justify-end">
          <div className="flex items-center space-x-2">
            <SlidersHorizontal className="w-4 h-4 text-slate-400" />
            <span className="text-xs text-slate-450 font-bold uppercase tracking-wider">Bộ lọc:</span>
          </div>

          {activeTab === "history" ? (
            /* Status Filter for Tab 1 */
            <select
              value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value)}
              className="bg-slate-950 border border-slate-850 rounded-lg px-3 py-2 text-xs text-slate-350 focus:outline-none focus:border-blue-500 cursor-pointer"
            >
              <option value="ALL">Tất cả trạng thái</option>
              <option value="SUCCESS">Thành công (Success)</option>
              <option value="PENDING">Chờ xử lý (Pending)</option>
              <option value="FAILED">Thất bại (Failed)</option>
            </select>
          ) : (
            /* Type Filter for Tab 2 (Ledger) */
            <select
              value={filterType}
              onChange={(e) => setFilterType(e.target.value)}
              className="bg-slate-950 border border-slate-850 rounded-lg px-3 py-2 text-xs text-slate-350 focus:outline-none focus:border-blue-500 cursor-pointer"
            >
              <option value="ALL">Tất cả bút toán</option>
              <option value="DEBIT">Nợ (Debit)</option>
              <option value="CREDIT">Có (Credit)</option>
            </select>
          )}
        </div>
      </div>

      {/* Main Table Content */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden shadow-sm">
        {activeTab === "history" ? (
          /* TAB 1: TRANSACTION HISTORY TABLE */
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-950/40 border-b border-slate-800 text-xxs font-semibold text-slate-400 uppercase tracking-wider">
                  <th className="px-6 py-4">Transaction ID</th>
                  <th className="px-6 py-4">Tài khoản gửi</th>
                  <th className="px-6 py-4"></th>
                  <th className="px-6 py-4">Tài khoản nhận</th>
                  <th className="px-6 py-4 text-right">Số tiền (VND)</th>
                  <th className="px-6 py-4 text-center">Trạng thái</th>
                  <th className="px-6 py-4">Kênh GD</th>
                  <th className="px-6 py-4 text-right">Thời gian tạo</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-850 text-sm">
                {filteredTransactions.length > 0 ? (
                  filteredTransactions.map((tx) => (
                    <tr key={tx.id} className="hover:bg-slate-850/20 transition-colors">
                      <td 
                        onClick={() => setSelectedTx(tx)}
                        className="px-6 py-4 font-mono text-xs text-blue-500 hover:underline cursor-pointer font-bold"
                      >
                        {tx.id}
                      </td>
                      <td className="px-6 py-4 text-slate-200 font-medium">{tx.fromWallet}</td>
                      <td className="px-2 py-4 text-slate-500 text-center"><ArrowRight className="w-4 h-4 inline" /></td>
                      <td className="px-6 py-4 text-slate-200 font-medium">{tx.toWallet}</td>
                      <td className="px-6 py-4 text-right font-mono text-slate-100 font-bold">{tx.amount}</td>
                      <td className="px-6 py-4 text-center">
                        {tx.status === "SUCCESS" && (
                          <span className="inline-flex items-center space-x-1 px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-500">
                            <CheckCircle2 className="w-3.5 h-3.5" />
                            <span>SUCCESS</span>
                          </span>
                        )}
                        {tx.status === "PENDING" && (
                          <span className="inline-flex items-center space-x-1 px-2.5 py-1 rounded-full text-xs font-semibold bg-amber-500/10 text-amber-500">
                            <Clock className="w-3.5 h-3.5 animate-pulse" />
                            <span>PENDING</span>
                          </span>
                        )}
                        {tx.status === "FAILED" && (
                          <span className="inline-flex items-center space-x-1 px-2.5 py-1 rounded-full text-xs font-semibold bg-red-500/10 text-red-500">
                            <XCircle className="w-3.5 h-3.5" />
                            <span>FAILED</span>
                          </span>
                        )}
                      </td>
                      <td className="px-6 py-4 text-slate-400 text-xs font-semibold">{tx.channel}</td>
                      <td className="px-6 py-4 text-right text-xs text-slate-450 font-medium">{tx.createdAt}</td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={8} className="px-6 py-12 text-center text-slate-500">
                      Không tìm thấy giao dịch nào khớp với điều kiện lọc.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        ) : (
          /* TAB 2: LEDGER EXPLORER TABLE (Sprint 2) */
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-950/40 border-b border-slate-800 text-xxs font-semibold text-slate-400 uppercase tracking-wider">
                  <th className="px-6 py-4">Ledger ID</th>
                  <th className="px-6 py-4">Transaction ID</th>
                  <th className="px-6 py-4">Tài khoản ví / Đối tác</th>
                  <th className="px-6 py-4 text-right">Nợ (Debit)</th>
                  <th className="px-6 py-4 text-right">Có (Credit)</th>
                  <th className="px-6 py-4 text-right">Số dư ví (VND)</th>
                  <th className="px-6 py-4 text-right">Thời gian ghi sổ</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-850 text-sm">
                {filteredLedgerEntries.length > 0 ? (
                  filteredLedgerEntries.map((entry) => (
                    <tr key={entry.id} className="hover:bg-slate-850/20 transition-colors">
                      <td className="px-6 py-4 font-mono text-xs text-slate-300 font-semibold">{entry.id}</td>
                      <td 
                        onClick={() => {
                          const txObj = mockTransactions.find(t => t.id === entry.txId);
                          if (txObj) setSelectedTx(txObj);
                        }}
                        className="px-6 py-4 font-mono text-xs text-blue-500 hover:underline cursor-pointer"
                      >
                        {entry.txId}
                      </td>
                      <td className="px-6 py-4 text-slate-200 font-medium">{entry.walletName}</td>
                      <td className={`px-6 py-4 text-right font-semibold ${
                        entry.debit !== "-" ? "text-red-500" : "text-slate-500"
                      }`}>
                        {entry.debit !== "-" ? `-${entry.debit}` : "-"}
                      </td>
                      <td className={`px-6 py-4 text-right font-semibold ${
                        entry.credit !== "-" ? "text-emerald-500" : "text-slate-500"
                      }`}>
                        {entry.credit !== "-" ? `+${entry.credit}` : "-"}
                      </td>
                      <td className="px-6 py-4 text-right font-mono text-slate-100 font-semibold">{entry.balance}</td>
                      <td className="px-6 py-4 text-right text-xs text-slate-450">{entry.createdAt}</td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={7} className="px-6 py-12 text-center text-slate-500">
                      Không tìm thấy bút toán sổ cái nào khớp với điều kiện lọc.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}

        {/* Pagination Section */}
        <div className="bg-slate-950/20 px-6 py-4 border-t border-slate-800 flex items-center justify-between">
          <span className="text-xs text-slate-500">
            Hiển thị <span className="font-semibold text-slate-400">
              {activeTab === "history" ? filteredTransactions.length : filteredLedgerEntries.length}
            </span> trên{" "}
            <span className="font-semibold text-slate-400">
              {activeTab === "history" ? mockTransactions.length : mockLedgerEntries.length}
            </span> dòng dữ liệu
          </span>
          <div className="flex items-center space-x-2">
            <button 
              disabled={currentPage === 1}
              onClick={() => setCurrentPage(prev => Math.max(1, prev - 1))}
              className="p-2 border border-slate-800 hover:bg-slate-850 rounded-lg text-slate-400 hover:text-slate-100 disabled:opacity-40 disabled:hover:bg-transparent transition-colors"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <span className="text-xs text-slate-350 font-semibold px-2">Trang {currentPage}</span>
            <button 
              disabled={true} 
              className="p-2 border border-slate-800 hover:bg-slate-850 rounded-lg text-slate-400 hover:text-slate-100 disabled:opacity-40 disabled:hover:bg-transparent transition-colors"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>

      {/* TRANSACTION DETAILS MODAL & PROCESSING TIMELINE */}
      {selectedTx && (
        <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto shadow-2xl animate-scale-up flex flex-col">
            {/* Modal Header */}
            <div className="p-6 border-b border-slate-800 flex justify-between items-center">
              <h3 className="text-lg font-bold text-slate-100 flex items-center space-x-2">
                <Layers className="w-5 h-5 text-blue-500" />
                <span>Chi tiết Tiến trình Giao dịch</span>
              </h3>
              <button 
                onClick={() => setSelectedTx(null)}
                className="p-1.5 hover:bg-slate-800 rounded-lg text-slate-400 hover:text-slate-150 transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Modal Body */}
            <div className="p-6 space-y-6 flex-1">
              {/* Định danh sâu (Deep Identifiers) */}
              <div className="bg-slate-950 border border-slate-850 rounded-xl p-4 grid grid-cols-1 md:grid-cols-2 gap-4 text-xs">
                <div className="space-y-1">
                  <span className="text-slate-500 font-bold uppercase tracking-wider">Transaction ID</span>
                  <span className="block font-mono text-slate-200 font-bold">{selectedTx.id}</span>
                </div>
                <div className="space-y-1">
                  <span className="text-slate-500 font-bold uppercase tracking-wider">Idempotency Key</span>
                  <span className="block font-mono text-slate-200 truncate">
                    {selectedTx.id === "TXN-662189" ? "7b539c3e-862a-4c28-97f3-e5d4c89280b1" : "c8f2b3e4-862a-4122-bc54-12948e9f80b1"}
                  </span>
                </div>
                <div className="space-y-1">
                  <span className="text-slate-500 font-bold uppercase tracking-wider">Kafka Event ID</span>
                  <span className="block font-mono text-slate-200 font-semibold">evt_9a7f3e82d5b6a1c8</span>
                </div>
                <div className="space-y-1">
                  <span className="text-slate-500 font-bold uppercase tracking-wider">Ledger ID (Debit/Credit)</span>
                  <span className="block font-mono text-slate-200">
                    LED-772882 (Nợ) / LED-772881 (Có)
                  </span>
                </div>
              </div>

              {/* Tóm tắt dòng tiền */}
              <div className="flex justify-between items-center bg-slate-950/40 border border-slate-850 p-4 rounded-xl text-sm">
                <div className="space-y-1">
                  <span className="text-xxs font-bold text-slate-500 uppercase tracking-wider">Ví gửi</span>
                  <div className="text-slate-200 font-semibold">{selectedTx.fromWallet}</div>
                </div>
                <div className="flex flex-col items-center">
                  <span className="text-xs font-extrabold text-emerald-500">{selectedTx.amount} VND</span>
                  <div className="h-0.5 w-16 bg-slate-800 relative my-1">
                    <ArrowRight className="w-3 h-3 absolute right-0 -top-1.5 text-slate-650" />
                  </div>
                </div>
                <div className="space-y-1 text-right">
                  <span className="text-xxs font-bold text-slate-500 uppercase tracking-wider">Ví nhận</span>
                  <div className="text-slate-200 font-semibold">{selectedTx.toWallet}</div>
                </div>
              </div>

              {/* Sơ đồ Processing Timeline */}
              <div className="space-y-4">
                <h4 className="text-xs font-bold text-slate-400 uppercase tracking-wider">Luồng đi của dữ liệu thời gian thực</h4>
                
                <div className="relative border-l-2 border-emerald-500/25 ml-3 pl-6 space-y-6">
                  {processingTimeline.map((step, idx) => (
                    <div key={idx} className="relative">
                      {/* Vòng tròn sáng tích xanh lá cây */}
                      <span className="absolute -left-[31px] top-0.5 flex h-4.5 w-4.5 items-center justify-center rounded-full bg-emerald-950 border border-emerald-500 shadow-[0_0_8px_rgba(16,185,129,0.3)]">
                        <CheckCircle2 className="h-3 w-3 text-emerald-500" />
                      </span>
                      <div className="flex justify-between items-start">
                        <div className="space-y-1 max-w-[80%]">
                          <span className="text-xs font-bold text-slate-100">{step.title}</span>
                          <p className="text-xxs text-slate-450 leading-relaxed">{step.description}</p>
                        </div>
                        <span className="text-xxs font-mono text-slate-500 font-semibold">{step.time}</span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
            
            {/* Modal Footer */}
            <div className="p-6 border-t border-slate-800 flex justify-end">
              <button 
                onClick={() => setSelectedTx(null)}
                className="px-5 py-2 bg-slate-800 hover:bg-slate-750 text-xs font-semibold text-slate-200 rounded-lg transition-colors"
              >
                Đóng chi tiết
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
