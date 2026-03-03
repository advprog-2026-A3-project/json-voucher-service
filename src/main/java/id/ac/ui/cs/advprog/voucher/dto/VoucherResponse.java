package id.ac.ui.cs.advprog.voucher.dto;

import id.ac.ui.cs.advprog.voucher.entity.Voucher;
import java.time.LocalDateTime;

public record VoucherResponse(
        Long id,
        String voucherCode,
        LocalDateTime validFrom,
        LocalDateTime validUntil,
        Integer quotaTotal,
        Integer quotaRemaining,
        Integer discountPercent,
        String terms,
        Boolean active,
        LocalDateTime createdAt
) {
    public static VoucherResponse from(Voucher voucher) {
        return new VoucherResponse(
                voucher.getId(),
                voucher.getVoucherCode(),
                voucher.getValidFrom(),
                voucher.getValidUntil(),
                voucher.getQuotaTotal(),
                voucher.getQuotaRemaining(),
                voucher.getDiscountPercent(),
                voucher.getTerms(),
                voucher.getActive(),
                voucher.getCreatedAt()
        );
    }
}