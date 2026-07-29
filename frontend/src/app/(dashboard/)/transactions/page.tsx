"use client";

import { useState } from "react";
import { 
  ArrowDownLeft, 
  ArrowUpRight, 
  Search, 
  SlidersHorizontal,
  Download,
  ChevronLeft,
  ChevronRight
} from "lucide-react";

/**
 * Trang Ledger Explorer (Sổ cái kế toán kép).
 * Hiển thị bảng chi tiết các bút toán Nợ (Debit - Đỏ) và Có (Credit - Xanh lá),
 * giúp kiểm toán tính toàn vẹn của dòng tiền và chứng minh nguyên tắc bảo toàn số dư.
 */
export default function LedgerExplorerPage() {
  const [searchTerm, setSearchTerm] = useState("");
  const [filterType, setFilterType] = useState("ALL");
  const [filterWallet, setFilterWallet] = useState("ALL");
  const [currentPage, setCurrentPage] = useState(1);

  // Mock dữ liệu Ledger Entries chất lượng cao khớp với hình ảnh thiết kế
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

  // Lọc dữ liệu
  const filteredEntries = mockLedgerEntries.filter(entry => {
    const matchesSearch = entry.txId.toLowerCase().includes(searchTerm.toLowerCase()) || 
                          entry.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
                          entry.walletName.toLowerCase().includes(searchTerm.toLowerCase());
    
    const matchesType = filterType === "ALL" || entry.type === filterType;
    
    const matchesWallet = filterWallet === "ALL" || 
                          (filterWallet === "MAIN" && entry.walletName.includes("Ví chính")) ||
                          (filterWallet === "SAVINGS" && entry.walletName.includes("Ví tiết kiệm"));

    return matchesSearch && matchesType && matchesWallet;
  });

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold text-slate-100 tracking-tight">Sổ cái kế toán kép</h1>
          <p className="text-slate-400 mt-1">Khám phá kiểm toán chi tiết (Ledger Entries). Ghi nhận Nợ/Có cân bằng bảo toàn dòng tiền.</p>
        </div>
        <button className="flex items-center space-x-2 px-4 py-2.5 bg-slate-900 border border-slate-800 hover:bg-slate-800 rounded-lg text-sm font-semibold text-slate-300 hover:text-slate-100 transition-colors">
          <Download className="w-4 h-4" />
          <span>Xuất báo cáo CSV</span>
        </button>
      </div>

      {/* Filters Bar */}
      <div className="bg-slate-900 border border-slate-800 p-4 rounded-xl flex flex-col md:flex-row gap-4 items-center justify-between">
        {/* Search */}
        <div className="relative w-full md:w-80">
          <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
          <input
            type="text"
            placeholder="Tìm kiếm Ledger ID, Tx ID..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full bg-slate-950 border border-slate-850 rounded-lg pl-10 pr-4 py-2.5 text-sm text-slate-200 placeholder-slate-500 focus:outline-none focus:border-blue-500 transition-colors"
          />
        </div>

        {/* Action Selects */}
        <div className="flex flex-wrap gap-4 w-full md:w-auto justify-end">
          <div className="flex items-center space-x-2">
            <SlidersHorizontal className="w-4 h-4 text-slate-400" />
            <span className="text-xs text-slate-400 font-semibold uppercase tracking-wider">Lọc:</span>
          </div>

          {/* Type Filter */}
          <select
            value={filterType}
            onChange={(e) => setFilterType(e.target.value)}
            className="bg-slate-950 border border-slate-850 rounded-lg px-3 py-2 text-xs text-slate-350 focus:outline-none focus:border-blue-500 cursor-pointer"
          >
            <option value="ALL">Tất cả bút toán</option>
            <option value="DEBIT">Nợ (Debit)</option>
            <option value="CREDIT">Có (Credit)</option>
          </select>

          {/* Wallet Filter */}
          <select
            value={filterWallet}
            onChange={(e) => setFilterWallet(e.target.value)}
            className="bg-slate-950 border border-slate-850 rounded-lg px-3 py-2 text-xs text-slate-350 focus:outline-none focus:border-blue-500 cursor-pointer"
          >
            <option value="ALL">Tất cả tài khoản ví</option>
            <option value="MAIN">Ví chính (Main)</option>
            <option value="SAVINGS">Ví tiết kiệm (Savings)</option>
          </select>
        </div>
      </div>

      {/* Ledger Table */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden shadow-sm">
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
              {filteredEntries.length > 0 ? (
                filteredEntries.map((entry) => (
                  <tr key={entry.id} className="hover:bg-slate-850/20 transition-colors">
                    <td className="px-6 py-4 font-mono text-xs text-slate-300 font-semibold">{entry.id}</td>
                    <td className="px-6 py-4 font-mono text-xs text-blue-500 hover:underline cursor-pointer">{entry.txId}</td>
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

        {/* Pagination */}
        <div className="bg-slate-950/20 px-6 py-4 border-t border-slate-800 flex items-center justify-between">
          <span className="text-xs text-slate-500">
            Hiển thị <span className="font-semibold text-slate-400">{filteredEntries.length}</span> trên{" "}
            <span className="font-semibold text-slate-400">{mockLedgerEntries.length}</span> dòng sổ cái
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
              disabled={true} // Chỉ có 1 trang mock
              className="p-2 border border-slate-800 hover:bg-slate-850 rounded-lg text-slate-400 hover:text-slate-100 disabled:opacity-40 disabled:hover:bg-transparent transition-colors"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
