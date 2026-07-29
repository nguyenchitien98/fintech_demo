"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";

/**
 * Trang Đăng nhập (Login Page).
 * Thiết kế theo phong cách Stripe/Revolut tối giản với Dark Mode và các nút xác thực mạng xã hội.
 */
export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const [error, setError] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError("");

    try {
      const response = await fetch("http://localhost:8081/api/v1/auth/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email, password }),
      });

      const resData = await response.json();

      if (!response.ok) {
        throw new Error(resData.message || "Đăng nhập thất bại. Vui lòng kiểm tra lại.");
      }

      setIsLoading(false);
      
      if (resData.data?.requires2fa) {
        // Chuyển hướng sang trang OTP 2FA kèm email query param
        router.push(`/otp?email=${encodeURIComponent(email)}`);
      } else {
        // Đăng nhập trực tiếp (nếu không bật 2FA, tuy nhiên hệ thống mặc định yêu cầu 2FA)
        localStorage.setItem("accessToken", resData.data.accessToken);
        localStorage.setItem("refreshToken", resData.data.refreshToken);
        router.push("/");
      }
    } catch (err: any) {
      setIsLoading(false);
      setError(err.message || "Không thể kết nối đến máy chủ xác thực.");
    }
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-8 shadow-xl space-y-6">
      {/* Brand Header */}
      <div className="text-center space-y-2">
        <div className="w-12 h-12 rounded-xl bg-blue-500 flex items-center justify-center font-extrabold text-xl text-white mx-auto shadow-lg shadow-blue-500/20">
          FW
        </div>
        <h2 className="text-2xl font-bold text-slate-100">Chào mừng quay trở lại 👋</h2>
        <p className="text-sm text-slate-400">Đăng nhập tài khoản FinWallet của bạn để tiếp tục</p>
      </div>

      {/* Form */}
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && (
          <div className="bg-red-500/10 border border-red-500/20 text-red-500 rounded-lg p-3 text-xs font-semibold text-center">
            {error}
          </div>
        )}
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
          <div className="flex justify-between items-center">
            <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Mật khẩu</label>
            <a href="#" className="text-xs text-blue-500 hover:text-blue-400 transition-colors">Quên mật khẩu?</a>
          </div>
          <input
            type="password"
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="••••••••"
            className="w-full bg-slate-950 border border-slate-850 focus:border-blue-500 focus:outline-none rounded-lg px-4 py-3 text-sm text-slate-200 transition-colors"
          />
        </div>

        <button
          type="submit"
          disabled={isLoading}
          className="w-full bg-blue-600 hover:bg-blue-500 disabled:bg-blue-800 text-white rounded-lg py-3 font-semibold shadow-md shadow-blue-600/10 transition-colors text-sm"
        >
          {isLoading ? "Đang xử lý..." : "Đăng nhập"}
        </button>
      </form>

      {/* Divider */}
      <div className="flex items-center my-4">
        <div className="flex-1 border-t border-slate-800"></div>
        <span className="text-xxs text-slate-500 px-3 uppercase tracking-wider">Hoặc tiếp tục với</span>
        <div className="flex-1 border-t border-slate-800"></div>
      </div>

      {/* OAuth Buttons */}
      <div className="grid grid-cols-2 gap-4">
        <button className="flex items-center justify-center space-x-2 py-2.5 border border-slate-800 hover:bg-slate-850 rounded-lg text-xs font-semibold text-slate-300 hover:text-slate-100 transition-colors">
          <span>Google</span>
        </button>
        <button className="flex items-center justify-center space-x-2 py-2.5 border border-slate-800 hover:bg-slate-850 rounded-lg text-xs font-semibold text-slate-300 hover:text-slate-100 transition-colors">
          <span>GitHub</span>
        </button>
      </div>

      {/* Footer link */}
      <div className="text-center pt-2">
        <p className="text-xs text-slate-400">
          Chưa có tài khoản?{" "}
          <Link href="/register" className="text-blue-500 hover:text-blue-400 font-semibold transition-colors">
            Đăng ký ngay
          </Link>
        </p>
      </div>
    </div>
  );
}
