package id.ac.ui.cs.advprog.voucher.controller;

import id.ac.ui.cs.advprog.voucher.dto.ApiErrorResponse;
import id.ac.ui.cs.advprog.voucher.exception.GlobalExceptionHandler;
import id.ac.ui.cs.advprog.voucher.exception.InvalidVoucherPeriodException;
import id.ac.ui.cs.advprog.voucher.exception.InvalidVoucherStateException;
import id.ac.ui.cs.advprog.voucher.exception.VoucherNotFoundException;
import id.ac.ui.cs.advprog.voucher.exception.VoucherQuotaExhaustedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleVoucherNotFound(){
        ResponseEntity<ApiErrorResponse> response =
            handler.handleVoucherNotFound(new VoucherNotFoundException());

        assertEquals(404, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(ApiErrorResponse.ERROR_STATUS, response.getBody().status());
        assertEquals("voucher not found", response.getBody().message());
    }

    @Test
    void testHandleInvalidVoucherState(){
        ResponseEntity<ApiErrorResponse> response =
            handler.handleInvalidVoucherState(new InvalidVoucherStateException("voucher is inactive"));

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(ApiErrorResponse.ERROR_STATUS, response.getBody().status());
        assertEquals("voucher is inactive", response.getBody().message());
    }

    @Test
    void testHandleInvalidVoucherPeriod(){
        ResponseEntity<ApiErrorResponse> response =
            handler.handleInvalidVoucherPeriod(new InvalidVoucherPeriodException());

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(ApiErrorResponse.ERROR_STATUS, response.getBody().status());
        assertEquals("validUntil must be after validFrom", response.getBody().message());
    }

    @Test
    void testHandleVoucherQuotaExhausted(){
        ResponseEntity<ApiErrorResponse> response =
            handler.handleVoucherQuotaExhausted(new VoucherQuotaExhaustedException());

        assertEquals(409, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(ApiErrorResponse.ERROR_STATUS, response.getBody().status());
        assertEquals("voucher quota exhausted", response.getBody().message());
    }

    @Test
    void testHandleValidationFailure(){
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(
            new MutablePropertyValues(),
            "request"
        );
        MethodParameter methodParameter = mock(MethodParameter.class);

        bindingResult.addError(new FieldError("request", "voucherCode", "must not be blank"));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
            methodParameter,
            bindingResult
        );
        ResponseEntity<ApiErrorResponse> response = handler.handleValidationFailure(exception);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(ApiErrorResponse.ERROR_STATUS, response.getBody().status());
        assertEquals("must not be blank", response.getBody().message());
    }
}
