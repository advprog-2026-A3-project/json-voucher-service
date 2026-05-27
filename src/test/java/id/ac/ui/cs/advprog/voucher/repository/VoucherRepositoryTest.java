package id.ac.ui.cs.advprog.voucher.repository;

import id.ac.ui.cs.advprog.voucher.entity.Voucher;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoucherRepositoryTest {

    @Mock
    private JpaVoucherRepository jpaVoucherRepository;

    @InjectMocks
    private VoucherRepository voucherRepository;

    @Test
    void findAllDelegatesToJpaRepository() {
        Voucher voucher = createVoucher("WELCOMEJSON10");
        when(jpaVoucherRepository.findAll()).thenReturn(List.of(voucher));

        List<Voucher> vouchers = voucherRepository.findAll();

        assertEquals(1, vouchers.size());
        assertSame(voucher, vouchers.getFirst());
        verify(jpaVoucherRepository).findAll();
    }

    @Test
    void findByVoucherCodeDelegatesToJpaRepository() {
        Voucher voucher = createVoucher("WELCOMEJSON10");
        when(jpaVoucherRepository.findByVoucherCode("WELCOMEJSON10")).thenReturn(Optional.of(voucher));

        Optional<Voucher> foundVoucher = voucherRepository.findByVoucherCode("WELCOMEJSON10");

        assertTrue(foundVoucher.isPresent());
        assertSame(voucher, foundVoucher.orElseThrow());
        verify(jpaVoucherRepository).findByVoucherCode("WELCOMEJSON10");
    }

    @Test
    void findByVoucherCodeForUpdateDelegatesToJpaRepository() {
        Voucher voucher = createVoucher("WELCOMEJSON10");
        when(jpaVoucherRepository.findByVoucherCodeForUpdate("WELCOMEJSON10")).thenReturn(Optional.of(voucher));

        Optional<Voucher> foundVoucher = voucherRepository.findByVoucherCodeForUpdate("WELCOMEJSON10");

        assertTrue(foundVoucher.isPresent());
        assertSame(voucher, foundVoucher.orElseThrow());
        verify(jpaVoucherRepository).findByVoucherCodeForUpdate("WELCOMEJSON10");
    }

    @Test
    void findAllByCreatedAtDescDelegatesToJpaRepository() {
        Voucher voucher = createVoucher("WELCOMEJSON10");
        when(jpaVoucherRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(voucher));

        List<Voucher> vouchers = voucherRepository.findAllByCreatedAtDesc();

        assertEquals(1, vouchers.size());
        assertSame(voucher, vouchers.getFirst());
        verify(jpaVoucherRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void saveDelegatesToJpaRepository() {
        Voucher voucher = createVoucher("WELCOMEJSON10");
        when(jpaVoucherRepository.save(voucher)).thenReturn(voucher);

        Voucher savedVoucher = voucherRepository.save(voucher);

        assertSame(voucher, savedVoucher);
        verify(jpaVoucherRepository).save(voucher);
    }

    @Test
    void deleteDelegatesToJpaRepository() {
        Voucher voucher = createVoucher("WELCOMEJSON10");

        voucherRepository.delete(voucher);

        verify(jpaVoucherRepository).delete(voucher);
    }

    @Test
    void existsByVoucherCodeDelegatesToJpaRepository() {
        when(jpaVoucherRepository.existsByVoucherCode("WELCOMEJSON10")).thenReturn(true);

        boolean exists = voucherRepository.existsByVoucherCode("WELCOMEJSON10");

        assertTrue(exists);
        verify(jpaVoucherRepository).existsByVoucherCode("WELCOMEJSON10");
    }

    private Voucher createVoucher(String code) {
        return new Voucher(
            code,
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(30),
            25,
            10,
            100_000L,
            20_000L,
            "Diskon 10% untuk minimum pembelian Rp100.000."
        );
    }
}
