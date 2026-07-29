"use client";

import { useState, useEffect } from "react";
import { 
  ArrowLeftRight, 
  Wallet, 
  HelpCircle, 
  Info,
  CheckCircle2,
  AlertCircle,
  ToggleLeft,
  ToggleRight
} from "lucide-react";

/**
 * Trang Chuyển tiền (Transfer Money Page).
 * Tích hợp Toggle Switch khóa kháng lặp Idempotency Key (UUID v4 tự động),
 * bảng tóm tắt chi phí giao dịch (Transfer Summary) và đối soát số dư động.
 */
export default function TransferPage() {
  // Mock số dư ví gửi mặc định ban đầu là 120,500,000 VND
  const [balance, setBalance] = useState(120500000);
  
  const [fromWalletId, setFromWalletId] = useState("1"); // Ví chính mặc định
  const [toWalletId, setToWalletId] = useState("");
  const [amount, setAmount] = useState("");
  const [description, setDescription] = useState("");
  
  // Kháng lặp Idempotency Key
  const [useIdempotency, setUseIdempotency] = useState(true);
  const [idempotencyKey, setIdempotencyKey] = useState("");

  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState<any>(null);

  // Sinh UUID v4 ngẫu nhiên cho Idempotency Key
  const generateUUID = () => {
    return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, function (c) {
      const r = (Math.random() * 16) | 0;
      const v = c === "x" ? r : (r & 0x3) | 0x8;
      return v.toString(16);
    });
  };

  // Tự động sinh Idempotency Key khi bật Toggle hoặc load trang
  useEffect(() => {
    if (useIdempotency) {
      setIdempotencyKey(generateUUID());
    } else {
      setIdempotencyKey("");
    }
  }, [useIdempotency]);

  // Tính toán phí và số dư
  const numAmount = Number(amount) || 0;
  const transactionFee = numAmount > 0 ? 1000 : 0; // Phí giao dịch cố định 1,000 VND
  const vatTax = numAmount > 0 ? Math.round(numAmount * 0.001) : 0; // 0.1% VAT
  const totalDeduction = numAmount + transactionFee + vatTax;
  const balanceAfter = balance - totalDeduction;

  const handleTransfer = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!toWalletId) {
      setError("Vui lòng điền mã ví người nhận.");
      return;
    }
    if (numAmount <= 0) {
      setError("Số tiền chuyển khoản phải lớn hơn 0.");
      return;
    }
    if (totalDeduction > balance) {
      setError("Số dư tài khoản không đủ để thanh toán giao dịch và chi phí phát sinh.");
      return;
    }

    setIsLoading(true);
    setError("");
    setSuccess(null);

    try {
      const headers: Record<string, string> = {
        "Content-Type": "application/json",
      };

      // Đính kèm idempotency key vào header nếu bật toggle
      if (useIdempotency && idempotencyKey) {
        headers["X-Idempotency-Key"] = idempotencyKey;
      }

      // Gọi API qua cổng Gateway 8080 (downstream sang wallet-service)
      const response = await fetch("http://localhost:8080/api/v1/wallets/transfer", {
        method: "POST",
        headers,
        body: JSON.stringify({
          fromWalletId: Number(fromWalletId),
          toWalletId: Number(toWalletId),
          amount: numAmount,
          description: description || "Chuyển tiền qua cổng FinWallet Gateway",
          reference: useIdempotency ? `IDEM-${idempotencyKey.substring(0, 8)}` : "DIRECT"
        }),
      });

      const resData = await response.json();

      if (!response.ok) {
        throw new Error(resData.message || "Giao dịch chuyển tiền thất bại.");
      }

      setIsLoading(false);
      setSuccess(resData.data);
      setBalance(balanceAfter); // Cập nhật số dư động trên giao diện

      // Reset form
      setToWalletId("");
      setAmount("");
      setDescription("");
      if (useIdempotency) {
        setIdempotencyKey(generateUUID()); // Tạo key mới cho giao dịch tiếp theo
      }
    } catch (err: any) {
      setIsLoading(false);
      setError(err.message || "Giao dịch thất bại do lỗi kết nối hệ thống.");
    }
  };

  return (
    <div className="space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold text-slate-100 tracking-tight">Chuyển tiền tức thì</h1>
        <p className="text-slate-400 mt-1">Chuyển khoản trực tiếp giữa các tài khoản ví. Áp dụng bảo mật kháng lặp giao dịch.</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Left: Form Chuyển tiền */}
        <div className="lg:col-span-2 space-y-6">
          <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 shadow-sm">
            <h3 className="text-lg font-bold text-slate-100 mb-4 flex items-center space-x-2">
              <ArrowLeftRight className="w-5 h-5 text-blue-500" />
              <span>Biểu mẫu chuyển khoản</span>
            </h3>

            {/* Alert Error / Success */}
            {error && (
              <div className="bg-red-500/10 border border-red-500/20 text-red-500 rounded-lg p-4 mb-6 flex items-start space-x-2 text-sm">
                <AlertCircle className="w-5 h-5 flex-shrink-0 mt-0.5" />
                <span>{error}</span>
              </div>
            )}
            {success && (
              <div className="bg-emerald-500/10 border border-emerald-500/20 text-emerald-500 rounded-lg p-4 mb-6 flex items-start space-x-2 text-sm">
                <CheckCircle2 className="w-5 h-5 flex-shrink-0 mt-0.5" />
                <div className="space-y-1">
                  <p className="font-bold">Giao dịch đã thực hiện thành công!</p>
                  <p className="text-xs text-emerald-400">Mã giao dịch: {success.transactionId} | Trạng thái: {success.status}</p>
                </div>
              </div>
            )}

            <form onSubmit={handleTransfer} className="space-y-5">
              {/* Ví gửi & Số dư hiện tại */}
              <div className="space-y-2">
                <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Chọn ví gửi</label>
                <div className="relative">
                  <select
                    value={fromWalletId}
                    onChange={(e) => setFromWalletId(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-850 focus:border-blue-500 focus:outline-none rounded-lg pl-11 pr-4 py-3 text-sm text-slate-200 cursor-pointer appearance-none"
                  >
                    <option value="1">Ví chính (ID: 1) - VND</option>
                    <option value="2">Ví tiết kiệm (ID: 2) - VND</option>
                  </select>
                  <Wallet className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
                </div>
                <div className="flex justify-between items-center text-xs text-slate-400 pt-1 px-1">
                  <span>Số dư khả dụng:</span>
                  <span className="font-mono font-bold text-emerald-500">{balance.toLocaleString("vi-VN")} VND</span>
                </div>
              </div>

              {/* ID Ví nhận */}
              <div className="space-y-2">
                <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider">ID Ví người nhận</label>
                <input
                  type="text"
                  required
                  placeholder="Nhập ID ví (ví dụ: 2)"
                  value={toWalletId}
                  onChange={(e) => setToWalletId(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-850 focus:border-blue-500 focus:outline-none rounded-lg px-4 py-3 text-sm text-slate-200"
                />
              </div>

              {/* Số tiền chuyển */}
              <div className="space-y-2">
                <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Số tiền chuyển (VND)</label>
                <input
                  type="number"
                  required
                  placeholder="Nhập số tiền (ví dụ: 50000)"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-850 focus:border-blue-500 focus:outline-none rounded-lg px-4 py-3 text-sm text-slate-200 font-mono"
                />
              </div>

              {/* Lời nhắn */}
              <div className="space-y-2">
                <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Nội dung chuyển tiền</label>
                <textarea
                  placeholder="Nhập nội dung chuyển khoản..."
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  rows={2}
                  className="w-full bg-slate-950 border border-slate-850 focus:border-blue-500 focus:outline-none rounded-lg px-4 py-3 text-sm text-slate-200 resize-none"
                />
              </div>

              {/* Idempotency Key Configuration */}
              <div className="bg-slate-950 border border-slate-850 rounded-lg p-4 space-y-3">
                <div className="flex justify-between items-center">
                  <div className="flex items-center space-x-2">
                    <span className="text-xs font-bold text-slate-300 uppercase tracking-wider">Sử dụng Idempotency Key</span>
                    <HelpCircle className="w-3.5 h-3.5 text-slate-500 cursor-help" title="Kháng lặp giao dịch. Chặn gửi trùng lệnh chuyển tiền trong 24 giờ." />
                  </div>
                  <button
                    type="button"
                    onClick={() => setUseIdempotency(!useIdempotency)}
                    className="text-slate-400 hover:text-slate-100 transition-colors focus:outline-none"
                  >
                    {useIdempotency ? (
                      <ToggleRight className="w-9 h-9 text-blue-500" />
                    ) : (
                      <ToggleLeft className="w-9 h-9 text-slate-650" />
                    )}
                  </button>
                </div>

                {useIdempotency && (
                  <div className="space-y-1.5">
                    <label className="text-xxs font-bold text-slate-500 uppercase tracking-wider">Khóa kháng lặp sinh tự động (UUID v4)</label>
                    <input
                      type="text"
                      readOnly
                      value={idempotencyKey}
                      className="w-full bg-slate-900 border border-slate-850 focus:outline-none rounded-lg px-3 py-2 text-xxs font-mono text-slate-400 select-all cursor-default"
                    />
                  </div>
                )}
              </div>

              <button
                type="submit"
                disabled={isLoading}
                className="w-full bg-blue-600 hover:bg-blue-500 disabled:bg-slate-800 text-white rounded-lg py-3.5 font-bold transition-all text-sm shadow-md shadow-blue-600/10"
              >
                {isLoading ? "Đang xử lý giao dịch..." : "Xác nhận chuyển khoản"}
              </button>
            </form>
          </div>
        </div>

        {/* Right: Bảng tóm tắt giao dịch (Transfer Summary) */}
        <div className="space-y-6">
          <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 shadow-sm space-y-6">
            <h3 className="text-lg font-bold text-slate-100 border-b border-slate-800 pb-3 flex items-center space-x-2">
              <Info className="w-5 h-5 text-blue-500" />
              <span>Tóm tắt giao dịch</span>
            </h3>

            {/* Chi tiết chi phí */}
            <div className="space-y-3.5 text-sm text-slate-350">
              <div className="flex justify-between">
                <span>Số tiền chuyển:</span>
                <span className="font-mono font-semibold text-slate-200">{numAmount.toLocaleString("vi-VN")} VND</span>
              </div>
              <div className="flex justify-between">
                <span>Phí dịch vụ:</span>
                <span className="font-mono font-semibold text-slate-200">{transactionFee.toLocaleString("vi-VN")} VND</span>
              </div>
              <div className="flex justify-between">
                <span>Thuế giá trị gia tăng (VAT 0.1%):</span>
                <span className="font-mono font-semibold text-slate-200">{vatTax.toLocaleString("vi-VN")} VND</span>
              </div>
              <div className="border-t border-slate-800 my-4 pt-3.5 flex justify-between text-base font-bold text-slate-100">
                <span>Tổng trừ tài khoản:</span>
                <span className="font-mono text-emerald-500">{totalDeduction.toLocaleString("vi-VN")} VND</span>
              </div>
            </div>

            {/* Ước tính số dư sau giao dịch */}
            <div className="bg-slate-950 rounded-lg p-4 space-y-2 text-xs">
              <div className="flex justify-between text-slate-400">
                <span>Số dư hiện tại:</span>
                <span className="font-mono font-semibold">{balance.toLocaleString("vi-VN")} VND</span>
              </div>
              <div className="flex justify-between text-slate-450 border-t border-slate-850 pt-2 font-semibold">
                <span>Số dư dự kiến sau giao dịch:</span>
                <span className={`font-mono font-bold ${balanceAfter >= 0 ? "text-emerald-500" : "text-red-500"}`}>
                  {balanceAfter.toLocaleString("vi-VN")} VND
                </span>
              </div>
            </div>

            {/* Thời gian xử lý dự kiến */}
            <div className="text-xxs text-slate-500 space-y-1">
              <p>⏱️ **Thời gian xử lý dự kiến:** Tức thì (Real-time)</p>
              <p>🛡️ **Bảo mật:** Giao dịch được bảo toàn bởi sổ cái kép PostgreSQL và cơ chế khoá bi quan (Pessimistic lock) chống rút âm tiền.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
