package id.ac.ui.cs.advprog.voucher.repository;

import id.ac.ui.cs.advprog.voucher.entity.Voucher;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCode(String code);
    boolean existsByCode(String code);
    List<Voucher> findAllByOrderByCreatedAtDesc();
}
