package com.mini.wallet.core.controller;

import com.mini.wallet.common.dto.ApiResponse;
import com.mini.wallet.common.idempotent.Idempotent;
import com.mini.wallet.core.dto.TransferRequestDto;
import com.mini.wallet.core.dto.TransferResponseDto;
import com.mini.wallet.core.dto.WalletCreateDto;
import com.mini.wallet.core.dto.WalletResponseDto;
import com.mini.wallet.core.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller tiếp nhận các yêu cầu liên quan tới Ví điện tử và Giao dịch (WalletController).
 *
 * <p><strong>Tại sao sử dụng các annotation:</strong>
 * <ul>
 *   <li>{@link RestController}: Đánh dấu lớp là một Spring REST Controller để tự động bọc dữ liệu trả về thành định dạng JSON.</li>
 *   <li>{@link RequestMapping}: Chỉ định URI gốc dùng chung cho toàn bộ các endpoint tài nguyên ví.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private final WalletService walletService;

    /**
     * Khởi tạo WalletController và tiêm service xử lý nghiệp vụ ví.
     *
     * @param walletService Service nghiệp vụ ví.
     */
    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    /**
     * API thực hiện chuyển tiền trực tiếp giữa hai ví điện tử (Direct Money Transfer).
     * Áp dụng khóa bi quan chống race condition và ghi sổ cái kép.
     *
     * <p><strong>Tại sao sử dụng @Idempotent:</strong> Chặn lặp giao dịch dựa trên header X-Idempotency-Key.
     *
     * @param request DTO yêu cầu chuyển khoản (fromId, toId, amount).
     * @return ApiResponse bọc đối tượng TransferResponseDto giao dịch thành công.
     */
    @PostMapping("/transfer")
    @Idempotent(keyPrefix = "idempotent:transfer:")
    public ApiResponse<TransferResponseDto> transfer(@Valid @RequestBody TransferRequestDto request) {
        TransferResponseDto response = walletService.transferMoney(request);
        return ApiResponse.success("Giao dịch chuyển tiền thành công", response);
    }

    /**
     * API tạo ví điện tử mới cho người dùng.
     * Thường được gọi nội bộ từ auth-service khi đăng ký người dùng thành công.
     *
     * @param createDto DTO chứa thông tin userId.
     * @return ApiResponse bọc đối tượng WalletResponseDto vừa được khởi tạo thành công.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<WalletResponseDto> createWallet(@Valid @RequestBody WalletCreateDto createDto) {
        WalletResponseDto response = walletService.createWallet(createDto);
        return ApiResponse.success("Khởi tạo ví điện tử thành công", response);
    }

    /**
     * API quản trị lấy danh sách toàn bộ các ví điện tử trên hệ thống (phục vụ Admin Dashboard).
     *
     * @return ApiResponse bọc danh sách các ví điện tử.
     */
    @GetMapping
    public ApiResponse<List<WalletResponseDto>> getAllWallets() {
        List<WalletResponseDto> wallets = walletService.getAllWallets();
        return ApiResponse.success("Lấy danh sách ví điện tử thành công", wallets);
    }

    /**
     * API đóng băng ví điện tử (Freeze Wallet).
     * Ví bị đóng băng sẽ tạm thời không thể gửi/nhận tiền.
     *
     * @param id Mã định danh của tài khoản ví cần đóng băng.
     * @return ApiResponse bọc thông tin ví sau khi đóng băng.
     */
    @PatchMapping("/{id}/freeze")
    public ApiResponse<WalletResponseDto> freezeWallet(@PathVariable Long id) {
        WalletResponseDto response = walletService.freezeWallet(id);
        return ApiResponse.success("Đóng băng ví điện tử thành công", response);
    }

    /**
     * API mở băng ví điện tử (Unfreeze Wallet).
     * Ví sau khi mở băng sẽ khôi phục lại trạng thái hoạt động bình thường.
     *
     * @param id Mã định danh của tài khoản ví cần mở băng.
     * @return ApiResponse bọc thông tin ví sau khi mở băng.
     */
    @PatchMapping("/{id}/unfreeze")
    public ApiResponse<WalletResponseDto> unfreezeWallet(@PathVariable Long id) {
        WalletResponseDto response = walletService.unfreezeWallet(id);
        return ApiResponse.success("Mở băng ví điện tử thành công", response);
    }
}
