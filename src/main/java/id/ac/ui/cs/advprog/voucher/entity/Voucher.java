package id.ac.ui.cs.advprog.voucher.entity;

import id.ac.ui.cs.advprog.voucher.exception.InvalidVoucherStateException;
import id.ac.ui.cs.advprog.voucher.exception.VoucherQuotaExhaustedException;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "vouchers")
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true)
    private String voucherCode;

    @Column(nullable = false)
    private LocalDateTime validFrom;

    @Column(nullable = false)
    private LocalDateTime validUntil;

    @Column(name = "quota_total", nullable = false)
    private Integer totalQuota;

    @Column(nullable = false)
    private Integer quotaRemaining;

    @Column(nullable = false)
    private Integer discountPercent = 0;

    @Column(nullable = false, columnDefinition = "text")
    private String terms;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Voucher(
            String voucherCode,
            LocalDateTime validFrom,
            LocalDateTime validUntil,
            Integer totalQuota,
            String terms
    ) {
        this.voucherCode = voucherCode;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.totalQuota = totalQuota;
        this.quotaRemaining = totalQuota;
        this.terms = terms;
    }

    public void updateDetails(LocalDateTime validFrom, LocalDateTime validUntil, Integer totalQuota, String terms){
        int usedQuota = this.totalQuota - this.quotaRemaining;
        if (totalQuota < usedQuota){
            throw new InvalidVoucherStateException("total quota can't be less than used quota");
        }

        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.totalQuota = totalQuota;
        this.quotaRemaining = totalQuota - usedQuota;
        this.terms = terms;
    }
    
    public void checkout(LocalDateTime now){
        validateCanBeCheckedOutAt(now);
        this.quotaRemaining -= 1;
    }

    private void validateCanBeCheckedOutAt(LocalDateTime now){
        validateVoucherIsActive();
        validateVoucherHasStarted(now);
        validateVoucherNotExpired(now);
        validateVoucherQuotaAvailable();
    }

    private void validateVoucherIsActive(){
        if (Boolean.FALSE.equals(this.active)){
            throw new InvalidVoucherStateException("voucher is inactive");
        }
    }

    private void validateVoucherHasStarted(LocalDateTime now){
        if (now.isBefore(this.validFrom)){
            throw new InvalidVoucherStateException("voucher is not yet valid");
        }
    }

    private void validateVoucherNotExpired(LocalDateTime now){
        if (now.isAfter(this.validUntil)){
            throw new InvalidVoucherStateException("voucher has expired");
        }
    }

    private void validateVoucherQuotaAvailable(){
        if (this.quotaRemaining <= 0){
            throw new VoucherQuotaExhaustedException();
        }
    }
}
