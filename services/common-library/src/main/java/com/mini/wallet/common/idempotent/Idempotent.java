package com.mini.wallet.common.idempotent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation đánh dấu các API yêu cầu kháng lặp giao dịch (Idempotent Annotation).
 *
 * <p><strong>Mục tiêu:</strong> Ngăn chặn việc cùng một request chuyển tiền hoặc thay đổi số dư bị gửi lặp
 * (do nhấn đúp chuột, mất kết nối mạng thử lại tự động từ client...) dẫn đến xử lý giao dịch trùng lặp.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

    /**
     * Tiền tố của key lưu trên Redis để phân biệt các nghiệp vụ kháng lặp khác nhau.
     *
     * @return String tiền tố key.
     */
    String keyPrefix() default "idempotent:";

    /**
     * Thời gian sống (Time To Live) của khóa kháng lặp trên Redis tính bằng giây.
     * Mặc định là 86400 giây (24 giờ) để chặn lặp trong vòng 1 ngày.
     *
     * @return Thời gian sống của key.
     */
    long ttlInSeconds() default 86400L;
}
