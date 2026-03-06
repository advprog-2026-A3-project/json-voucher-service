package id.ac.ui.cs.advprog.voucher.controller;

import id.ac.ui.cs.advprog.voucher.dto.CreateVoucherRequest;
import id.ac.ui.cs.advprog.voucher.dto.UpdateVoucherRequest;
import id.ac.ui.cs.advprog.voucher.dto.VoucherResponse;
import id.ac.ui.cs.advprog.voucher.entity.Voucher;
import id.ac.ui.cs.advprog.voucher.service.VoucherService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoucherControllerTest {

    @Mock
    private VoucherService voucherService;

    @InjectMocks
    private VoucherController controller;

    @Test
    void testCreateVoucher() {
        CreateVoucherRequest request = new CreateVoucherRequest(
                "DISC10",
                LocalDateTime.of(2026, 3, 1, 10, 0),
                LocalDateTime.of(2026, 3, 10, 10, 0),
                10,
                "Terms"
        );
        Voucher voucher = new Voucher(
                "DISC10",
                LocalDateTime.of(2026, 3, 1, 10, 0),
                LocalDateTime.of(2026, 3, 10, 10, 0),
                10,
                "Terms"
        );
        when(voucherService.createVoucher(
                request.voucherCode(),
                request.validFrom(),
                request.validUntil(),
                request.totalQuota(),
                request.terms()
        )).thenReturn(voucher);

        ResponseEntity<VoucherResponse> response = controller.createVoucher(request);

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("DISC10", response.getBody().voucherCode());
        assertEquals(10, response.getBody().quotaRemaining());
        verify(voucherService).createVoucher(
                request.voucherCode(),
                request.validFrom(),
                request.validUntil(),
                request.totalQuota(),
                request.terms()
        );
    }

    @Test
    void testGetAllVouchers() {
        Voucher voucher = new Voucher(
                "DISC10",
                LocalDateTime.of(2026, 3, 1, 10, 0),
                LocalDateTime.of(2026, 3, 10, 10, 0),
                10,
                "Terms"
        );
        when(voucherService.listVouchers()).thenReturn(List.of(voucher));

        ResponseEntity<List<VoucherResponse>> response = controller.getAllVouchers();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("DISC10", response.getBody().get(0).voucherCode());
        verify(voucherService).listVouchers();
    }

    @Test
    void testCheckoutVoucher() {
        Voucher voucher = new Voucher(
                "DISC10",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                10,
                "Terms"
        );
        voucher.checkout(LocalDateTime.now());
        when(voucherService.checkoutVoucher("DISC10")).thenReturn(voucher);

        ResponseEntity<Map<String, Object>> response = controller.checkoutVoucher("DISC10");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("SUCCESS", response.getBody().get("status"));
        assertEquals("DISC10", response.getBody().get("voucherCode"));
        assertEquals(9, response.getBody().get("quotaRemaining"));
        verify(voucherService).checkoutVoucher("DISC10");
    }

    @Test
    void testGetVoucherByCode() {
        Voucher voucher = new Voucher(
                "DISC10",
                LocalDateTime.of(2026, 3, 1, 10, 0),
                LocalDateTime.of(2026, 3, 10, 10, 0),
                10,
                "Terms"
        );
        when(voucherService.getVoucherByCode("DISC10")).thenReturn(voucher);

        ResponseEntity<VoucherResponse> response = controller.getVoucherByCode("DISC10");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("DISC10", response.getBody().voucherCode());
        verify(voucherService).getVoucherByCode("DISC10");
    }

    @Test
    void testUpdateVoucher() {
        UpdateVoucherRequest request = new UpdateVoucherRequest(
                LocalDateTime.of(2026, 3, 2, 10, 0),
                LocalDateTime.of(2026, 3, 12, 10, 0),
                15,
                "Updated terms"
        );

        Voucher voucher = new Voucher(
                "DISC10",
                LocalDateTime.of(2026, 3, 1, 10, 0),
                LocalDateTime.of(2026, 3, 10, 10, 0),
                10,
                "Terms"
        );

        voucher.updateDetails(
                request.validFrom(),
                request.validUntil(),
                request.totalQuota(),
                request.terms()
        );

        when(voucherService.updateVoucher(
                "DISC10",
                request.validFrom(),
                request.validUntil(),
                request.totalQuota(),
                request.terms()
        )).thenReturn(voucher);

        ResponseEntity<VoucherResponse> response = controller.updateVoucher("DISC10", request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(15, response.getBody().totalQuota());
        assertEquals("Updated terms", response.getBody().terms());
        verify(voucherService).updateVoucher(
                "DISC10",
                request.validFrom(),
                request.validUntil(),
                request.totalQuota(),
                request.terms()
        );
    }

    @Test
    void testDeleteVoucher() {
        ResponseEntity<Void> response = controller.deleteVoucher("DISC10");

        assertEquals(204, response.getStatusCode().value());
        verify(voucherService).deleteVoucher("DISC10");
    }
}