package id.ac.ui.cs.advprog.voucher.repository;

import id.ac.ui.cs.advprog.voucher.entity.Voucher;
import java.util.List;
import java.util.Optional;

public interface VoucherReadRepository extends ReadRepository<Voucher> {
    Optional<Voucher> findByVoucherCode(String voucherCode);
    List<Voucher> findAllByCreatedAtDesc();
}