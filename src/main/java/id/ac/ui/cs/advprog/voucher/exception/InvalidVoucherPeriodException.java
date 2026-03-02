package id.ac.ui.cs.advprog.voucher.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidVoucherPeriodException extends RuntimeException {
    public InvalidVoucherPeriodException() {
        super("validUntil must be after validFrom");
    }
}