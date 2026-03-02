package id.ac.ui.cs.advprog.voucher.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class VoucherNotFoundException extends RuntimeException {
    public VoucherNotFoundException() {
        super("voucher not found");
    }
}