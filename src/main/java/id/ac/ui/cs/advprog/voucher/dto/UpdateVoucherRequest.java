package id.ac.ui.cs.advprog.voucher.dto;

import id.ac.ui.cs.advprog.voucher.service.UpdateVoucherCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record UpdateVoucherRequest(
    @NotNull LocalDateTime validFrom,
    @NotNull LocalDateTime validUntil,
    @NotNull @Min(1) Integer totalQuota,
    @NotNull @Min(1) Integer discountPercent,
    @NotNull @Min(0) Long minimumPurchaseAmount,
    @Min(0) Long maxDiscountAmount,
    @NotBlank String terms
) {
    public UpdateVoucherCommand toCommand() {
        return new UpdateVoucherCommand(
            validFrom,
            validUntil,
            totalQuota,
            discountPercent,
            minimumPurchaseAmount,
            maxDiscountAmount,
            terms
        );
    }
}
