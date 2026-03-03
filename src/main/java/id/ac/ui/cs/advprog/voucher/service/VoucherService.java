package id.ac.ui.cs.advprog.voucher.service;

import id.ac.ui.cs.advprog.voucher.entity.Voucher;
import id.ac.ui.cs.advprog.voucher.exception.InvalidVoucherPeriodException;
import id.ac.ui.cs.advprog.voucher.exception.VoucherNotFoundException;
import id.ac.ui.cs.advprog.voucher.repository.VoucherReadRepository;
import id.ac.ui.cs.advprog.voucher.repository.VoucherWriteRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VoucherService {
    private final VoucherReadRepository voucherReadRepository;
    private final VoucherWriteRepository voucherWriteRepository;

    public VoucherService(
            VoucherReadRepository voucherReadRepository,
            VoucherWriteRepository voucherWriteRepository
    ) {
        this.voucherReadRepository = voucherReadRepository;
        this.voucherWriteRepository = voucherWriteRepository;
    }

    @Transactional
    public Voucher createVoucher(
            String voucherCode, LocalDateTime validFrom, LocalDateTime validUntil, 
            Integer totalQuota, String terms
    ){
        validateVoucherPeriod(validFrom, validUntil);
        Voucher voucher = new Voucher(voucherCode, validFrom, validUntil, totalQuota, terms);
        return voucherWriteRepository.save(voucher);
    }

    @Transactional
    public Voucher updateVoucher(
        String voucherCode, LocalDateTime newValidFrom, LocalDateTime newValidUntil, 
        Integer totalQuota, String newTerms
    ){
        validateVoucherPeriod(newValidFrom, newValidUntil);
        Voucher voucher = findVoucherByCode(voucherCode);
        voucher.updateDetails(newValidFrom, newValidUntil, totalQuota, newTerms);
        return voucherWriteRepository.save(voucher);
    }

    @Transactional(readOnly = true)
    public List<Voucher> listVouchers(){
        return voucherReadRepository.findAllByCreatedAtDesc();
    }

    @Transactional
    public Voucher checkoutVoucher(String voucherCode){
        Voucher voucher = findVoucherByCode(voucherCode);
        voucher.checkout(LocalDateTime.now());
        return voucherWriteRepository.save(voucher);
    }

    private Voucher findVoucherByCode(String voucherCode){
        return voucherReadRepository.findByVoucherCode(voucherCode)
                .orElseThrow(VoucherNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public Voucher getVoucherByCode(String voucherCode){
        return findVoucherByCode(voucherCode);
    }

    private void validateVoucherPeriod(LocalDateTime validFrom, LocalDateTime validUntil){
        if (validUntil.isBefore(validFrom)) {
            throw new InvalidVoucherPeriodException();
        }
    }
}