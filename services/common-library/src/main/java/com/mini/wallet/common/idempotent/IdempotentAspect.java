package com.mini.wallet.common.idempotent;

import com.mini.wallet.common.exception.BusinessException;
import com.mini.wallet.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * Aspect lập trình hướng khía cạnh (AOP) kiểm soát xử lý kháng lặp giao dịch (Idempotent Aspect).
 *
 * <p><strong>Tại sao sử dụng @Aspect & @Component:</strong>
 * Annotation này biến lớp này thành một Spring Component Bean chịu trách nhiệm chặn các lời gọi API
 * có gắn annotation `@Idempotent` (Around Advice), thực thi kiểm tra khóa trùng lặp trước khi cho phép
 * đi vào logic nghiệp vụ chính của Service.
 */
@Aspect
@Component
public class IdempotentAspect {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Khởi tạo IdempotentAspect và tiêm RedisTemplate phục vụ tra cứu kiểm tra khóa kháng lặp.
     *
     * @param redisTemplate Cache lưu trữ các khóa giao dịch đang xử lý hoặc hoàn thành.
     */
    public IdempotentAspect(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Điểm chặn thực thi bao quanh (Around) các phương thức có gắn annotation @Idempotent.
     *
     * @param joinPoint Đối tượng ProceedingJoinPoint cung cấp ngữ cảnh của phương thức gốc đang bị chặn.
     * @param idempotentAnnotation Annotation @Idempotent gắn trên phương thức.
     * @return Kết quả trả về của phương thức gốc sau khi thực thi thành công.
     * @throws Throwable Bất kỳ lỗi runtime hoặc business exception nào của luồng xử lý.
     */
    @Around("@annotation(idempotentAnnotation)")
    public Object handleIdempotent(ProceedingJoinPoint joinPoint, Idempotent idempotentAnnotation) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();

        // 1. Trích xuất X-Idempotency-Key từ HTTP Header
        String idempotencyKey = request.getHeader("X-Idempotency-Key");
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Thiếu header bắt buộc X-Idempotency-Key để thực thi giao dịch");
        }

        String redisKey = idempotentAnnotation.keyPrefix() + idempotencyKey;
        long ttl = idempotentAnnotation.ttlInSeconds();

        // 2. Tra cứu trạng thái của khóa trên Redis
        String status = (String) redisTemplate.opsForValue().get(redisKey);

        if (status != null) {
            if ("PROCESSING".equals(status)) {
                // Khóa đang được xử lý bởi một thread khác
                throw new BusinessException(
                    ErrorCode.IDEMPOTENT_KEY_CONFLICT, 
                    "Yêu cầu giao dịch trùng lặp đang được xử lý hệ thống. Vui lòng chờ."
                );
            } else if ("COMPLETED".equals(status)) {
                // Khóa đã được xử lý hoàn tất thành công trước đó
                throw new BusinessException(
                    ErrorCode.IDEMPOTENT_KEY_CONFLICT,
                    "Yêu cầu giao dịch này đã được hệ thống thực hiện thành công trước đó. Không thể gửi lặp lại."
                );
            }
        }

        // 3. Nếu chưa tồn tại, lưu tạm trạng thái PROCESSING lên Redis
        redisTemplate.opsForValue().set(redisKey, "PROCESSING", Duration.ofSeconds(ttl));

        try {
            // 4. Cho phép thực thi logic của phương thức gốc
            Object result = joinPoint.proceed();

            // 5. Nếu thực thi thành công, cập nhật trạng thái thành COMPLETED
            redisTemplate.opsForValue().set(redisKey, "COMPLETED", Duration.ofSeconds(ttl));
            return result;
        } catch (Throwable throwable) {
            // 6. Nếu xảy ra lỗi nghiệp vụ, xóa khóa khỏi Redis để cho phép người dùng thử lại
            redisTemplate.delete(redisKey);
            throw throwable;
        }
    }
}
