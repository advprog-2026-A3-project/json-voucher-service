package id.ac.ui.cs.advprog.voucher.service;

import java.time.LocalDateTime;

public record CreateVoucherCommand(
    String voucherCode,
    LocalDateTime validFrom,
    LocalDateTime validUntil,
    Integer totalQuota,
    Integer discountPercent,
    Long minimumPurchaseAmount,
    Long maxDiscountAmount,
    String terms
) {}
