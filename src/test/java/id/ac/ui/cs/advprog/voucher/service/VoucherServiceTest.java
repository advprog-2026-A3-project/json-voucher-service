package id.ac.ui.cs.advprog.voucher.service;

import id.ac.ui.cs.advprog.voucher.entity.Voucher;
import id.ac.ui.cs.advprog.voucher.exception.InvalidVoucherPeriodException;
import id.ac.ui.cs.advprog.voucher.exception.VoucherNotFoundException;
import id.ac.ui.cs.advprog.voucher.repository.VoucherReadRepository;
import id.ac.ui.cs.advprog.voucher.repository.VoucherWriteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoucherServiceTest {

    @Mock
    private VoucherReadRepository voucherReadRepository;

    @Mock
    private VoucherWriteRepository voucherWriteRepository;

    @InjectMocks
    private VoucherService voucherService;

    @Test
    void testCreate() {
        Voucher voucher = new Voucher(
                "DISC10",
                LocalDateTime.of(2026, 3, 1, 10, 0),
                LocalDateTime.of(2026, 3, 10, 10, 0),
                10,
                "Minimal order applies"
        );

        when(voucherWriteRepository.save(any(Voucher.class))).thenReturn(voucher);

        Voucher createdVoucher = voucherService.createVoucher(
                voucher.getVoucherCode(),
                voucher.getValidFrom(),
                voucher.getValidUntil(),
                voucher.getTotalQuota(),
                voucher.getTerms()
        );

        assertEquals("DISC10", createdVoucher.getVoucherCode());
        assertEquals(voucher.getValidFrom(), createdVoucher.getValidFrom());
        assertEquals(voucher.getValidUntil(), createdVoucher.getValidUntil());
        assertEquals(10, createdVoucher.getTotalQuota());
        assertEquals(10, createdVoucher.getQuotaRemaining());
        verify(voucherWriteRepository).save(any(Voucher.class));
    }

    @Test
    void testCreateIfPeriodInvalid() {
        LocalDateTime validFrom = LocalDateTime.of(2026, 3, 10, 10, 0);
        LocalDateTime validUntil = LocalDateTime.of(2026, 3, 1, 10, 0);

        try {
            voucherService.createVoucher("DISC10", validFrom, validUntil, 10, "Minimal order applies");
            fail();
        } catch (InvalidVoucherPeriodException exception) {
            assertEquals("validUntil must be after validFrom", exception.getMessage());
        }

        verify(voucherWriteRepository, never()).save(any(Voucher.class));
    }

    @Test
    void testFindAll() {
        Voucher voucher = new Voucher(
                "DISC10",
                LocalDateTime.of(2026, 3, 1, 10, 0),
                LocalDateTime.of(2026, 3, 10, 10, 0),
                5,
                "Terms"
        );
        Voucher anotherVoucher = new Voucher(
                "DISC20",
                LocalDateTime.of(2026, 3, 2, 10, 0),
                LocalDateTime.of(2026, 3, 11, 10, 0),
                3,
                "Terms"
        );

        when(voucherReadRepository.findAllByCreatedAtDesc()).thenReturn(List.of(voucher, anotherVoucher));

        List<Voucher> vouchers = voucherService.listVouchers();
        assertEquals(2, vouchers.size());
        assertSame(voucher, vouchers.get(0));
        assertSame(anotherVoucher, vouchers.get(1));
        verify(voucherReadRepository).findAllByCreatedAtDesc();
    }

    @Test
    void testFindByCode() {
        Voucher voucher = new Voucher(
                "DISC10",
                LocalDateTime.of(2026, 3, 1, 10, 0),
                LocalDateTime.of(2026, 3, 10, 10, 0),
                5,
                "Terms"
        );

        when(voucherReadRepository.findByVoucherCode("DISC10")).thenReturn(Optional.of(voucher));

        Voucher result = voucherService.getVoucherByCode("DISC10");
        assertSame(voucher, result);
        verify(voucherReadRepository).findByVoucherCode("DISC10");
    }

    @Test
    void testFindByCodeIfVoucherNotFound() {
        when(voucherReadRepository.findByVoucherCode("MISSING")).thenReturn(Optional.empty());

        try {
            voucherService.getVoucherByCode("MISSING");
            fail();
        } catch (VoucherNotFoundException exception) {
            assertEquals("voucher not found", exception.getMessage());
        }
    }

    @Test
    void testUpdate() {
        Voucher voucher = new Voucher(
                "DISC10",
                LocalDateTime.of(2026, 3, 1, 10, 0),
                LocalDateTime.of(2026, 3, 10, 10, 0),
                10,
                "Old terms"
        );
        LocalDateTime newValidFrom = LocalDateTime.of(2026, 3, 2, 10, 0);
        LocalDateTime newValidUntil = LocalDateTime.of(2026, 3, 12, 10, 0);

        when(voucherReadRepository.findByVoucherCode("DISC10")).thenReturn(Optional.of(voucher));
        when(voucherWriteRepository.save(voucher)).thenReturn(voucher);

        Voucher updatedVoucher = voucherService.updateVoucher("DISC10", newValidFrom, newValidUntil, 15, "New terms");

        assertSame(voucher, updatedVoucher);
        assertEquals(newValidFrom, updatedVoucher.getValidFrom());
        assertEquals(newValidUntil, updatedVoucher.getValidUntil());
        assertEquals(15, updatedVoucher.getTotalQuota());
        assertEquals(15, updatedVoucher.getQuotaRemaining());
        assertEquals("New terms", updatedVoucher.getTerms());
        verify(voucherWriteRepository).save(voucher);
    }

    @Test
    void testCheckout() {
        Voucher voucher = new Voucher(
                "DISC10",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                10,
                "Terms"
        );

        when(voucherReadRepository.findByVoucherCode("DISC10")).thenReturn(Optional.of(voucher));
        when(voucherWriteRepository.save(voucher)).thenReturn(voucher);

        Voucher result = voucherService.checkoutVoucher("DISC10");

        assertEquals(9, result.getQuotaRemaining());
        verify(voucherWriteRepository).save(voucher);
    }

    @Test
    void testDeleteByCode() {
        Voucher voucher = new Voucher(
                "DISC10",
                LocalDateTime.of(2026, 3, 1, 10, 0),
                LocalDateTime.of(2026, 3, 10, 10, 0),
                10,
                "Terms"
        );

        when(voucherReadRepository.findByVoucherCode("DISC10")).thenReturn(Optional.of(voucher));

        voucherService.deleteVoucher("DISC10");

        verify(voucherWriteRepository).delete(voucher);
    }
}