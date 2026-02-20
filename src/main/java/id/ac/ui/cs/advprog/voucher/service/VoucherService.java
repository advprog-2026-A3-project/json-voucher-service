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
    private VoucherRepository voucherRepository;
    
    public VoucherService(VoucherRepository voucherRepository){
        this.voucherRepository = voucherRepository;
    }
    
    @Transactional
    public Voucher createVoucher(
            String code, LocalDateTime validFrom, LocalDateTime validUntil, Integer totalQuota, String terms
    ){
        if (validUntil.isBefore(validFrom)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "validUntil must be after validFrom");
        }
        
        if (voucherRepository.existsByCode(code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "code already exists");
        }
        
        Voucher voucher = new Voucher(code, validFrom, validUntil, totalQuota, terms);
        return voucherRepository.save(voucher);
    }
    
    @Transactional(readOnly = true)
    public List<Voucher> listVouchers() {
        return voucherRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Voucher checkoutVoucher(String code) {
        LocalDateTime now = LocalDateTime.now();

        Voucher voucher = voucherRepository.findByCode(code).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "voucher not found"));

        if (Boolean.FALSE.equals(voucher.getActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "voucher is inactive");
        }
        if (now.isBefore(voucher.getValidFrom())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "voucher is not yet valid");
        }
        if (now.isAfter(voucher.getValidUntil())) {
            voucher.setActive(false);
            voucherRepository.save(voucher);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "voucher has expired");
        }
        if (voucher.getQuotaRemaining() <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "voucher quota exhausted");
        }

        voucher.setQuotaRemaining(voucher.getQuotaRemaining() - 1);
        return voucherRepository.save(voucher);
    }
}
