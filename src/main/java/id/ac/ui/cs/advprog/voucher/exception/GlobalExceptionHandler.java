package id.ac.ui.cs.advprog.voucher.exception;

import id.ac.ui.cs.advprog.voucher.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String ERROR_STATUS = ApiErrorResponse.ERROR_STATUS;

    @ExceptionHandler(VoucherNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleVoucherNotFound(VoucherNotFoundException exception){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(ERROR_STATUS, exception.getMessage()));
    }

    @ExceptionHandler(InvalidVoucherStateException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidVoucherState(InvalidVoucherStateException exception){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(ERROR_STATUS, exception.getMessage()));
    }

    @ExceptionHandler(InvalidVoucherPeriodException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidVoucherPeriod(InvalidVoucherPeriodException exception){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(ERROR_STATUS, exception.getMessage()));
    }

    @ExceptionHandler(VoucherQuotaExhaustedException.class)
    public ResponseEntity<ApiErrorResponse> handleVoucherQuotaExhausted(VoucherQuotaExhaustedException exception){
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse(ERROR_STATUS, exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationFailure(MethodArgumentNotValidException exception){
        String message = exception.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(fieldError -> fieldError.getDefaultMessage())
            .orElse("validation failed");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(ERROR_STATUS, message));
    }
}
