package com.mini.wallet.core.controller;

import com.mini.wallet.common.dto.ApiResponse;
import com.mini.wallet.core.dto.LedgerResponseDto;
import com.mini.wallet.core.service.WalletService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller chịu trách nhiệm phơi bày các API truy vấn Sổ cái Ledger (LedgerController).
 *
 * <p><strong>Tại sao sử dụng các annotation:</strong>
 * <ul>
 *   <li>{@link RestController}: Đánh dấu lớp là Spring Controller trả về dữ liệu REST JSON.</li>
 *   <li>{@link RequestMapping}: Chỉ định URI tiền tố `/api/v1/ledger-entries` cho tài nguyên sổ cái.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/ledger-entries")
public class LedgerController {

    private final WalletService walletService;

    /**
     * Khởi tạo LedgerController thông qua Constructor Injection.
     *
     * @param walletService Service nghiệp vụ ví và sổ cái.
     */
    public LedgerController(WalletService walletService) {
        this.walletService = walletService;
    }

    /**
     * API truy vấn danh sách dòng ghi sổ cái LedgerExplorer (có phân trang và sắp xếp giảm dần theo thời gian tạo).
     * Hỗ trợ lọc theo một mã tài khoản ví nhất định.
     *
     * @param walletId Mã ví điện tử để lọc kết quả (Không bắt buộc).
     * @param page Số trang hiện tại cần lấy (mặc định trang 0).
     * @param size Kích thước của một trang dữ liệu (mặc định 20 dòng).
     * @return ApiResponse chứa Page các dòng LedgerResponseDto.
     */
    @GetMapping
    public ApiResponse<Page<LedgerResponseDto>> getLedgerEntries(
            @RequestParam(required = false) Long walletId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<LedgerResponseDto> entries = walletService.getLedgerEntries(walletId, pageable);
        return ApiResponse.success("Lấy danh sách bút toán sổ cái thành công", entries);
    }
}
