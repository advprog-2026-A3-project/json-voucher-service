package id.ac.ui.cs.advprog.voucher.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReedemVoucherRequest(
    @NotNull @Min(0) Long subtotal
){}
