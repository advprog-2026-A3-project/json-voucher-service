package id.ac.ui.cs.advprog.voucher.dto;

public record ApiErrorResponse(
    String status,
    String message
) {
    public static final String ERROR_STATUS = "ERROR";
}
