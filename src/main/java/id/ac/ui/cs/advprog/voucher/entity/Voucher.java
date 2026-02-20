package id.ac.ui.cs.advprog.voucher.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vouchers")
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private LocalDateTime validFrom;

    @Column(nullable = false)
    private LocalDateTime validUntil;

    @Column(nullable = false)
    private Integer quotaTotal;

    @Column(nullable = false)
    private Integer quotaRemaining;

    @Column(nullable = false)
    private Integer discountPercent = 0;

    @Lob
    @Column(nullable = false)
    private String terms;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Voucher() {

    }

    public Voucher(
            String code,
            LocalDateTime validFrom,
            LocalDateTime validUntil,
            Integer quotaTotal,
            String terms
    ) {
        this.code = code;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.quotaTotal = quotaTotal;
        this.quotaRemaining = quotaTotal;
        this.discountPercent = 0;
        this.terms = terms;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public Integer getQuotaTotal() {
        return quotaTotal;
    }

    public Integer getQuotaRemaining() {
        return quotaRemaining;
    }

    public String getTerms() {
        return terms;
    }

    public Integer getDiscountPercent() {
        return discountPercent;
    }

    public Boolean getActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setValidFrom(LocalDateTime validFrom) {
        this.validFrom = validFrom;
    }

    public void setValidUntil(LocalDateTime validUntil) {
        this.validUntil = validUntil;
    }

    public void setQuotaTotal(Integer quotaTotal) {
        this.quotaTotal = quotaTotal;
    }

    public void setQuotaRemaining(Integer quotaRemaining) {
        this.quotaRemaining = quotaRemaining;
    }

    public void setTerms(String terms) {
        this.terms = terms;
    }

    public void setDiscountPercent(Integer discountPercent) {
        this.discountPercent = discountPercent;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
