package id.ac.ui.cs.advprog.voucher.dto;

public record ValidateVoucherResponse(
    String voucherCode,
    Long subtotal,
    Long discountAmount
) {
    public static ValidateVoucherResponse from(String voucherCode, Long subtotal, Long discountAmount) {
        return new ValidateVoucherResponse(voucherCode, subtotal, discountAmount);
    }
}
