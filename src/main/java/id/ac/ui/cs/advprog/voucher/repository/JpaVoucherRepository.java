package id.ac.ui.cs.advprog.voucher.repository;

import id.ac.ui.cs.advprog.voucher.entity.Voucher;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaVoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByVoucherCode(String voucherCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Voucher v where v.voucherCode = :voucherCode")
    Optional<Voucher> findByVoucherCodeForUpdate(@Param("voucherCode") String voucherCode);

    boolean existsByVoucherCode(String voucherCode);
    List<Voucher> findAllByOrderByCreatedAtDesc();
}
