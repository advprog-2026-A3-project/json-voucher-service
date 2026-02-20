package id.ac.ui.cs.advprog.voucher.controller;

import java.util.Map;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import id.ac.ui.cs.advprog.voucher.dto.CreateVoucherRequest;
import id.ac.ui.cs.advprog.voucher.entity.Voucher;
import id.ac.ui.cs.advprog.voucher.service.VoucherService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1/vouchers")
public class VoucherController {
    private VoucherService voucherService;

    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }
    
    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of("status", "ok");
    }
    
    @PostMapping
    public ResponseEntity<Voucher> createVoucher(@RequestBody CreateVoucherRequest request) {
        Voucher voucherCreated = voucherService.createVoucher(
                request.code(), request.validFrom(), request.validUntil(), request.totalQuota(), request.terms()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(voucherCreated);
    }

    @GetMapping
    public ResponseEntity<List<Voucher>> getAllVouchers() {
        return ResponseEntity.ok(voucherService.listVouchers());
    }

    @PostMapping("/{code}/checkout")
    public ResponseEntity<Map<String, Object>> checkoutVoucher(@PathVariable String code) {
        Voucher updated = voucherService.checkoutVoucher(code);
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "code", updated.getCode(),
                "quotaRemaining", updated.getQuotaRemaining()
        ));
    }
}
