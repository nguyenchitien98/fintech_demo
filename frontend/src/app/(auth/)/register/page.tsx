"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";

/**
 * Trang Đăng ký tài khoản (Register Page).
 * Thiết kế tinh tế cho trải nghiệm mở tài khoản ví điện tử nhanh chóng, hỗ trợ validation.
 */
export default function RegisterPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg("");

    if (password !== confirmPassword) {
      setErrorMsg("Mật khẩu xác nhận không khớp!");
      return;
    }

    setIsLoading(true);

    // Mô phỏng gọi API Đăng ký thành công ở Sprint 1
    setTimeout(() => {
      setIsLoading(false);
      router.push("/login");
    }, 1000);
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-8 shadow-xl space-y-6">
      {/* Brand Header */}
      <div className="text-center space-y-2">
        <div className="w-12 h-12 rounded-xl bg-blue-500 flex items-center justify-center font-extrabold text-xl text-white mx-auto shadow-lg shadow-blue-500/20">
          FW
        </div>
        <h2 className="text-2xl font-bold text-slate-100">Bắt đầu miễn phí 🚀</h2>
        <p className="text-sm text-slate-400">Tự động mở ví mặc định sau khi mở tài khoản</p>
      </div>

      {/* Error Message */}
      {errorMsg && (
        <div className="bg-red-500/10 border border-red-500/20 text-red-500 text-xs py-3 px-4 rounded-lg">
          {errorMsg}
        </div>
      )}

      {/* Form */}
      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="space-y-1.5">
          <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Email</label>
          <input
            type="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="john.doe@example.com"
            className="w-full bg-slate-950 border border-slate-850 focus:border-blue-500 focus:outline-none rounded-lg px-4 py-3 text-sm text-slate-200 transition-colors"
          />
        </div>

        <div className="space-y-1.5">
          <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Mật khẩu</label>
          <input
            type="password"
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="•••••••• (Ít nhất 6 ký tự)"
            className="w-full bg-slate-950 border border-slate-850 focus:border-blue-500 focus:outline-none rounded-lg px-4 py-3 text-sm text-slate-200 transition-colors"
          />
        </div>

        <div className="space-y-1.5">
          <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Xác nhận mật khẩu</label>
          <input
            type="password"
            required
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            placeholder="••••••••"
            className="w-full bg-slate-950 border border-slate-850 focus:border-blue-500 focus:outline-none rounded-lg px-4 py-3 text-sm text-slate-200 transition-colors"
          />
        </div>

        <button
          type="submit"
          disabled={isLoading}
          className="w-full bg-blue-600 hover:bg-blue-500 disabled:bg-blue-800 text-white rounded-lg py-3 font-semibold shadow-md shadow-blue-600/10 transition-colors text-sm"
        >
          {isLoading ? "Đang tạo tài khoản..." : "Đăng ký ngay"}
        </button>
      </form>

      {/* Footer link */}
      <div className="text-center pt-2">
        <p className="text-xs text-slate-400">
          Đã có tài khoản?{" "}
          <Link href="/login" className="text-blue-500 hover:text-blue-400 font-semibold transition-colors">
            Đăng nhập
          </Link>
        </p>
      </div>
    </div>
  );
}
