package id.ac.ui.cs.advprog.voucher.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class VoucherQuotaExhaustedException extends RuntimeException {
    public VoucherQuotaExhaustedException() {
        super("voucher quota exhausted");
    }
}