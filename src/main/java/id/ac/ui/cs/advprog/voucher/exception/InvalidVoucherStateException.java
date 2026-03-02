package id.ac.ui.cs.advprog.voucher.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidVoucherStateException extends RuntimeException {
    public InvalidVoucherStateException(String message) {
        super(message);
    }
}