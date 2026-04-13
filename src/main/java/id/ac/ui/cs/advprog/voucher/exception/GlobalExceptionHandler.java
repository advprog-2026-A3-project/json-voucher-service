package id.ac.ui.cs.advprog.voucher.exception;

import id.ac.ui.cs.advprog.voucher.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(VoucherNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleVoucherNotFound(VoucherNotFoundException exception){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse("ERROR", exception.getMessage()));
    }

    @ExceptionHandler(InvalidVoucherStateException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidVoucherState(InvalidVoucherStateException exception){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse("ERROR", exception.getMessage()));
    }

    @ExceptionHandler(InvalidVoucherPeriodException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidVoucherPeriod(InvalidVoucherPeriodException exception){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse("ERROR", exception.getMessage()));
    }

    @ExceptionHandler(VoucherQuotaExhaustedException.class)
    public ResponseEntity<ApiErrorResponse> handleVoucherQuotaExhausted(VoucherQuotaExhaustedException exception){
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse("ERROR", exception.getMessage()));
    }
}
