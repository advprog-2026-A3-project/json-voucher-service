package id.ac.ui.cs.advprog.voucher.entity;

import id.ac.ui.cs.advprog.voucher.exception.InvalidVoucherStateException;
import id.ac.ui.cs.advprog.voucher.exception.VoucherQuotaExhaustedException;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class VoucherTest {
    @Test
    void testCheckout() {
        Voucher voucher = new Voucher(
                "DISC10",
                LocalDateTime.of(2026, 3, 1, 10, 0),
                LocalDateTime.of(2026, 3, 10, 10, 0),
                3,
                "Terms"
        );

        voucher.checkout(LocalDateTime.of(2026, 3, 5, 10, 0));
        assertEquals(2, voucher.getQuotaRemaining());
    }

    @Test
    void testCheckoutIfVoucherHasNotStarted() {
        Voucher voucher = new Voucher(
                "DISC10",
                LocalDateTime.of(2026, 3, 10, 10, 0),
                LocalDateTime.of(2026, 3, 20, 10, 0),
                3,
                "Terms"
        );

        try {
            voucher.checkout(LocalDateTime.of(2026, 3, 5, 10, 0));
            fail();
        } catch (InvalidVoucherStateException exception) {
            assertEquals("voucher is not yet valid", exception.getMessage());
        }
    }

    @Test
    void testCheckoutIfVoucherExpired() {
        Voucher voucher = new Voucher(
                "DISC10",
                LocalDateTime.of(2026, 3, 1, 10, 0),
                LocalDateTime.of(2026, 3, 10, 10, 0),
                3,
                "Terms"
        );

        try {
            voucher.checkout(LocalDateTime.of(2026, 3, 11, 10, 0));
            fail();
        } catch (InvalidVoucherStateException exception) {
            assertEquals("voucher has expired", exception.getMessage());
        }
    }

    @Test
    void testCheckoutIfQuotaExhausted() {
        Voucher voucher = new Voucher(
                "DISC10",
                LocalDateTime.of(2026, 3, 1, 10, 0),
                LocalDateTime.of(2026, 3, 10, 10, 0),
                1,
                "Terms"
        );

        voucher.checkout(LocalDateTime.of(2026, 3, 5, 10, 0));
        try {
            voucher.checkout(LocalDateTime.of(2026, 3, 5, 11, 0));
            fail();
        } catch (VoucherQuotaExhaustedException exception) {
            assertEquals("voucher quota exhausted", exception.getMessage());
        }
    }

    @Test
    void testUpdateDetails() {
        Voucher voucher = new Voucher(
                "DISC10",
                LocalDateTime.of(2026, 3, 1, 10, 0),
                LocalDateTime.of(2026, 3, 10, 10, 0),
                5,
                "Terms"
        );

        voucher.checkout(LocalDateTime.of(2026, 3, 5, 10, 0));
        voucher.updateDetails(
                LocalDateTime.of(2026, 3, 2, 10, 0),
                LocalDateTime.of(2026, 3, 12, 10, 0),
                8,
                "Updated terms"
        );
        assertEquals(8, voucher.getTotalQuota());
        assertEquals(7, voucher.getQuotaRemaining());
        assertEquals("Updated terms", voucher.getTerms());
    }

    @Test
    void testCheckoutIfVoucherInactive() {
        Voucher voucher = new Voucher(
                "DISC10",
                LocalDateTime.of(2026, 3, 1, 10, 0),
                LocalDateTime.of(2026, 3, 10, 10, 0),
                3,
                "Terms"
        );

        try {
            Field activeField = Voucher.class.getDeclaredField("active");
            activeField.setAccessible(true);
            activeField.set(voucher, false);
        } catch (ReflectiveOperationException exception) {
            fail();
        }

        try {
            voucher.checkout(LocalDateTime.of(2026, 3, 5, 10, 0));
            fail();
        } catch (InvalidVoucherStateException exception) {
            assertEquals("voucher is inactive", exception.getMessage());
        }
    }
}