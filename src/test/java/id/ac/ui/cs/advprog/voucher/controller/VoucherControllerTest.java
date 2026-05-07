package id.ac.ui.cs.advprog.voucher.controller;

import id.ac.ui.cs.advprog.voucher.dto.CreateVoucherRequest;
import id.ac.ui.cs.advprog.voucher.dto.RedeemVoucherRequest;
import id.ac.ui.cs.advprog.voucher.dto.RedeemVoucherResponse;
import id.ac.ui.cs.advprog.voucher.dto.UpdateVoucherRequest;
import id.ac.ui.cs.advprog.voucher.dto.ValidateVoucherRequest;
import id.ac.ui.cs.advprog.voucher.dto.ValidateVoucherResponse;
import id.ac.ui.cs.advprog.voucher.dto.VoucherResponse;
import id.ac.ui.cs.advprog.voucher.entity.Voucher;
import id.ac.ui.cs.advprog.voucher.service.CreateVoucherCommand;
import id.ac.ui.cs.advprog.voucher.service.UpdateVoucherCommand;
import id.ac.ui.cs.advprog.voucher.service.VoucherService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoucherControllerTest {
    private static final int DISCOUNT_PERCENT = 10;
    private static final long MINIMUM_PURCHASE_AMOUNT = 0;

    @Mock
    private VoucherService voucherService;

    @InjectMocks
    private VoucherController controller;

    @Test
    void testCreateVoucher(){
        CreateVoucherRequest request = new CreateVoucherRequest(
            "DISC10",
            LocalDateTime.of(2026, 3, 1, 10, 0),
            LocalDateTime.of(2026, 3, 10, 10, 0),
            10,
            DISCOUNT_PERCENT,
            MINIMUM_PURCHASE_AMOUNT,
            null,
            "Terms"
        );
        Voucher voucher = new Voucher(
            "DISC10",
            LocalDateTime.of(2026, 3, 1, 10, 0),
            LocalDateTime.of(2026, 3, 10, 10, 0),
            10,
            DISCOUNT_PERCENT,
            MINIMUM_PURCHASE_AMOUNT,
            null,
            "Terms"
        );
        when(voucherService.createVoucher(request.toCommand())).thenReturn(voucher);

        ResponseEntity<VoucherResponse> response = controller.createVoucher(request);

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("DISC10", response.getBody().voucherCode());
        assertEquals(10, response.getBody().quotaRemaining());
        verify(voucherService).createVoucher(new CreateVoucherCommand(
            request.voucherCode(),
            request.validFrom(),
            request.validUntil(),
            request.totalQuota(),
            request.discountPercent(),
            request.minimumPurchaseAmount(),
            request.maxDiscountAmount(),
            request.terms()
        ));
    }

    @Test
    void testGetAllVouchers(){
        Voucher voucher = new Voucher(
            "DISC10",
            LocalDateTime.of(2026, 3, 1, 10, 0),
            LocalDateTime.of(2026, 3, 10, 10, 0),
            10,
            DISCOUNT_PERCENT,
            MINIMUM_PURCHASE_AMOUNT,
            null,
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
    void testGetVoucherByCode(){
        Voucher voucher = new Voucher(
            "DISC10",
            LocalDateTime.of(2026, 3, 1, 10, 0),
            LocalDateTime.of(2026, 3, 10, 10, 0),
            10,
            DISCOUNT_PERCENT,
            MINIMUM_PURCHASE_AMOUNT,
            null,
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
    void testUpdateVoucher(){
        UpdateVoucherRequest request = new UpdateVoucherRequest(
            LocalDateTime.of(2026, 3, 2, 10, 0),
            LocalDateTime.of(2026, 3, 12, 10, 0),
            15,
            DISCOUNT_PERCENT,
            MINIMUM_PURCHASE_AMOUNT,
            null,
            "Updated terms"
        );

        Voucher voucher = new Voucher(
            "DISC10",
            LocalDateTime.of(2026, 3, 1, 10, 0),
            LocalDateTime.of(2026, 3, 10, 10, 0),
            10,
            DISCOUNT_PERCENT,
            MINIMUM_PURCHASE_AMOUNT,
            null,
            "Terms"
        );

        voucher.updateDetails(
            request.validFrom(),
            request.validUntil(),
            request.totalQuota(),
            voucher.getDiscountPercent(),
            voucher.getMinimumPurchaseAmount(),
            voucher.getMaxDiscountAmount(),
            request.terms()
        );

        when(voucherService.updateVoucher("DISC10", request.toCommand())).thenReturn(voucher);

        ResponseEntity<VoucherResponse> response = controller.updateVoucher("DISC10", request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(15, response.getBody().totalQuota());
        assertEquals("Updated terms", response.getBody().terms());
        verify(voucherService).updateVoucher("DISC10", new UpdateVoucherCommand(
            request.validFrom(),
            request.validUntil(),
            request.totalQuota(),
            request.discountPercent(),
            request.minimumPurchaseAmount(),
            request.maxDiscountAmount(),
            request.terms()
        ));
    }

    @Test
    void testDeleteVoucher(){
        ResponseEntity<Void> response = controller.deleteVoucher("DISC10");

        assertEquals(204, response.getStatusCode().value());
        verify(voucherService).deleteVoucher("DISC10");
    }

    @Test
    void testValidateVoucher(){
        ValidateVoucherRequest request = new ValidateVoucherRequest(
            Long.valueOf(200000)
        );

        when(voucherService.previewVoucherDiscount("DISC10", 200000)).thenReturn(20000L);

        ResponseEntity<ValidateVoucherResponse> response = controller.validateVoucher("DISC10", request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("DISC10", response.getBody().voucherCode());
        assertEquals(200000, response.getBody().subtotal());
        assertEquals(20000, response.getBody().discountAmount());
        verify(voucherService).previewVoucherDiscount("DISC10", 200000);
    }

    @Test
    void testDeactivateVoucher(){
        Voucher voucher = new Voucher(
            "DISC10",
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(1),
            5,
            DISCOUNT_PERCENT,
            MINIMUM_PURCHASE_AMOUNT,
            null,
            "Terms"
        );
        voucher.deactivate();
        when(voucherService.deactivateVoucher("DISC10")).thenReturn(voucher);

        ResponseEntity<VoucherResponse> response = controller.deactivateVoucher("DISC10");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().active());
        verify(voucherService).deactivateVoucher("DISC10");
    }

    @Test
    void testRedeemVoucher(){
        RedeemVoucherRequest request = new RedeemVoucherRequest(Long.valueOf(200000));

        Voucher voucher = new Voucher(
            "DISC10",
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(1),
            5,
            10,
            Long.valueOf(100000),
            null,
            "Terms"
        );
        voucher.redeem(LocalDateTime.now(), 200000);

        when(voucherService.redeemVoucher("DISC10", 200000)).thenReturn(voucher);

        ResponseEntity<RedeemVoucherResponse> response = controller.redeemVoucher("DISC10", request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("DISC10", response.getBody().voucherCode());
        assertEquals(200000, response.getBody().subtotal());
        assertEquals(4, response.getBody().quotaRemaining());
        verify(voucherService).redeemVoucher("DISC10", 200000);
    }
}
