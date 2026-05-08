package id.ac.ui.cs.advprog.voucher.dto;

import id.ac.ui.cs.advprog.voucher.entity.Voucher;

public record RedeemVoucherResponse(
    String voucherCode,
    Long subtotal,
    Integer quotaRemaining
) {
    public static RedeemVoucherResponse from(Voucher voucher, Long subtotal) {
        return new RedeemVoucherResponse(
            voucher.getVoucherCode(),
            subtotal,
            voucher.getQuotaRemaining()
        );
    }
}
