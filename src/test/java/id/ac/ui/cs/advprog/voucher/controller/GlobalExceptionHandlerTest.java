package id.ac.ui.cs.advprog.voucher.controller;

import id.ac.ui.cs.advprog.voucher.dto.ApiErrorResponse;
import id.ac.ui.cs.advprog.voucher.exception.GlobalExceptionHandler;
import id.ac.ui.cs.advprog.voucher.exception.InvalidVoucherPeriodException;
import id.ac.ui.cs.advprog.voucher.exception.InvalidVoucherStateException;
import id.ac.ui.cs.advprog.voucher.exception.VoucherNotFoundException;
import id.ac.ui.cs.advprog.voucher.exception.VoucherQuotaExhaustedException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleVoucherNotFound(){
        ResponseEntity<ApiErrorResponse> response =
                handler.handleVoucherNotFound(new VoucherNotFoundException());

        assertEquals(404, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("ERROR", response.getBody().status());
        assertEquals("voucher not found", response.getBody().message());
    }

    @Test
    void testHandleInvalidVoucherState(){
        ResponseEntity<ApiErrorResponse> response =
                handler.handleInvalidVoucherState(new InvalidVoucherStateException("voucher is inactive"));

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("ERROR", response.getBody().status());
        assertEquals("voucher is inactive", response.getBody().message());
    }

    @Test
    void testHandleInvalidVoucherPeriod(){
        ResponseEntity<ApiErrorResponse> response =
                handler.handleInvalidVoucherPeriod(new InvalidVoucherPeriodException());

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("ERROR", response.getBody().status());
        assertEquals("validUntil must be after validFrom", response.getBody().message());
    }

    @Test
    void testHandleVoucherQuotaExhausted(){
        ResponseEntity<ApiErrorResponse> response =
                handler.handleVoucherQuotaExhausted(new VoucherQuotaExhaustedException());

        assertEquals(409, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("ERROR", response.getBody().status());
        assertEquals("voucher quota exhausted", response.getBody().message());
    }
}