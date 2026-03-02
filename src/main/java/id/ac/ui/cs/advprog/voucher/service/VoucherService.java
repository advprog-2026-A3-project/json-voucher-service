package id.ac.ui.cs.advprog.voucher.service;

import id.ac.ui.cs.advprog.voucher.entity.Voucher;
import id.ac.ui.cs.advprog.voucher.repository.VoucherRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VoucherService {
    private final VoucherRepository voucherRepository;

    public VoucherService(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }

    @Transactional
    public Voucher createVoucher(
            String voucherCode, LocalDateTime validFrom, LocalDateTime validUntil, Integer totalQuota, String terms
    ) {
        validateVoucherPeriod(validFrom, validUntil);
        Voucher voucher = new Voucher(voucherCode, validFrom, validUntil, totalQuota, terms);
        return voucherRepository.save(voucher);
    }

    @Transactional(readOnly = true)
    public List<Voucher> listVouchers() {
        return voucherRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Voucher checkoutVoucher(String voucherCode) {
        LocalDateTime now = LocalDateTime.now();
        Voucher voucher = findVoucherByCode(voucherCode);
        validateVoucherCanBeCheckedOut(voucher, now);
        voucher.setQuotaRemaining(voucher.getQuotaRemaining() - 1);
        return voucherRepository.save(voucher);
    }

    private Voucher findVoucherByCode(String voucherCode) {
        return voucherRepository.findByCode(voucherCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "voucher not found"));
    }

    private void validateVoucherPeriod(LocalDateTime validFrom, LocalDateTime validUntil) {
        if (validUntil.isBefore(validFrom)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "validUntil must be after validFrom");
        }
    }

    private void validateVoucherCanBeCheckedOut(Voucher voucher, LocalDateTime now) {
        if (Boolean.FALSE.equals(voucher.getActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "voucher is inactive");
        }

        if (now.isBefore(voucher.getValidFrom())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "voucher is not yet valid");
        }

        if (now.isAfter(voucher.getValidUntil())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "voucher has expired");
        }

        if (voucher.getQuotaRemaining() <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "voucher quota exhausted");
        }
    }
}