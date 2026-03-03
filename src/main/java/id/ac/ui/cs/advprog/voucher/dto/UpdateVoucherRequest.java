package id.ac.ui.cs.advprog.voucher.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record UpdateVoucherRequest(
        @NotNull LocalDateTime validFrom,
        @NotNull LocalDateTime validUntil,
        @NotNull @Min(1) Integer totalQuota,
        @NotBlank String terms
) {}