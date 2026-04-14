package id.ac.ui.cs.advprog.voucher.dto;

public record RedeemVoucherResponse(
        String voucherCode,
        Long subtotal,
        Integer quotaRemaining
){}