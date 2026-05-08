package id.ac.ui.cs.advprog.voucher.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ValidateVoucherRequest(
    @NotNull @Min(0) Long subtotal
){}
