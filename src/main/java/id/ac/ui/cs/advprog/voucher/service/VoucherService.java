package id.ac.ui.cs.advprog.voucher.service;

import id.ac.ui.cs.advprog.voucher.entity.Voucher;
import id.ac.ui.cs.advprog.voucher.exception.InvalidVoucherPeriodException;
import id.ac.ui.cs.advprog.voucher.exception.VoucherNotFoundException;
import id.ac.ui.cs.advprog.voucher.repository.VoucherRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Voucher voucher = findVoucherByCode(voucherCode);
        voucher.checkout(LocalDateTime.now());
        return voucherRepository.save(voucher);
    }

    private Voucher findVoucherByCode(String voucherCode) {
        return voucherRepository.findByVoucherCode(voucherCode)
                .orElseThrow(VoucherNotFoundException::new);
    }

    private void validateVoucherPeriod(LocalDateTime validFrom, LocalDateTime validUntil) {
        if (validUntil.isBefore(validFrom)) {
            throw new InvalidVoucherPeriodException();
        }
    }
}