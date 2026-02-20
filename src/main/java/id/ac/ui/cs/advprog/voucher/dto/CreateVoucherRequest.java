package id.ac.ui.cs.advprog.voucher.dto;

import java.time.LocalDateTime;

public record CreateVoucherRequest(
        String code, LocalDateTime validFrom, LocalDateTime validUntil, Integer totalQuota, String terms
) {}