package com.mini.wallet.notification.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * REST Controller cung cấp cổng kết nối Server-Sent Events (SSE) (NotificationController).
 * Cho phép thiết lập luồng đẩy thông báo thời gian thực (Real-time stream) về các client Web Next.js.
 *
 * <p><strong>Tại sao sử dụng các annotation:</strong>
 * <ul>
 *   <li>{@link RestController}: Đánh dấu Spring REST Controller trả về JSON hoặc Event Stream.</li>
 *   <li>{@link RequestMapping}: Cấu hình endpoint gốc `/api/v1/notifications`.</li>
 *   <li>{@link CrossOrigin}: Mở quyền CORS để cho phép Frontend Next.js (chạy cổng 3000) thiết lập kết nối SSE trực tiếp.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    // Danh sách thread-safe quản lý tất cả các kết nối SSE Emitter đang hoạt động
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    /**
     * Endpoint thiết lập kết nối Server-Sent Events (SSE) để truyền phát thông báo thời gian thực.
     * Trả về SseEmitter giúp duy trì kết nối HTTP lâu dài (Long-lived HTTP Connection).
     *
     * @return Đối tượng SseEmitter được đăng ký vào danh sách phát tin.
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        // Khởi tạo SseEmitter không giới hạn thời gian timeout (Long.MAX_VALUE)
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);

        // Đăng ký callback dọn dẹp khi kết nối hoàn tất hoặc timeout
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));

        try {
            // Gửi tin nhắn ping chào mừng ban đầu để xác nhận kết nối thành công
            emitter.send(SseEmitter.event().name("CONNECT").data("Đã thiết lập kết nối Server-Sent Events thành công"));
        } catch (IOException e) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    /**
     * Phát tin nhắn (Broadcast) thông báo giao dịch tới tất cả các client đang kết nối SSE.
     *
     * @param message Chuỗi JSON payload sự kiện giao dịch.
     */
    public void broadcast(String message) {
        List<SseEmitter> deadEmitters = new ArrayList<>();
        
        for (SseEmitter emitter : emitters) {
            try {
                // Đẩy gói tin nhắn (Event) về client dưới dạng dữ liệu text event stream
                emitter.send(SseEmitter.event().name("TRANSACTION").data(message));
            } catch (Exception e) {
                // Nếu kết nối của client đã chết (F5, đóng trình duyệt), đưa vào hàng chờ dọn dẹp
                deadEmitters.add(emitter);
            }
        }
        
        // Loại bỏ các kết nối lỗi khỏi danh sách phát tin
        emitters.removeAll(deadEmitters);
    }
}
