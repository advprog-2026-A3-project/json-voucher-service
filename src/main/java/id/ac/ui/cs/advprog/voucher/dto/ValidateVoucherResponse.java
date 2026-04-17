package id.ac.ui.cs.advprog.voucher.dto;

public record ValidateVoucherResponse(
    String voucherCode,
    Long subtotal,
    Long discountAmount
){}
