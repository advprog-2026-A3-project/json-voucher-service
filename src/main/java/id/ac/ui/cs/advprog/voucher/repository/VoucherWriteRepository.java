package id.ac.ui.cs.advprog.voucher.repository;

import id.ac.ui.cs.advprog.voucher.entity.Voucher;

public interface VoucherWriteRepository extends WriteRepository<Voucher> {
    boolean existsByVoucherCode(String voucherCode);
}