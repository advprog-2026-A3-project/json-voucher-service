package id.ac.ui.cs.advprog.voucher.controller;

import id.ac.ui.cs.advprog.voucher.dto.CreateVoucherRequest;
import id.ac.ui.cs.advprog.voucher.dto.VoucherResponse;
import id.ac.ui.cs.advprog.voucher.entity.Voucher;
import id.ac.ui.cs.advprog.voucher.service.VoucherService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vouchers")
public class VoucherController {
    private final VoucherService voucherService;

    public VoucherController(VoucherService voucherService){
        this.voucherService = voucherService;
    }
    
    @PostMapping
    public ResponseEntity<VoucherResponse> createVoucher(@Valid @RequestBody CreateVoucherRequest request){
        Voucher voucherCreated = voucherService.createVoucher(
                request.voucherCode(), request.validFrom(), request.validUntil(), request.totalQuota(), request.terms()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(VoucherResponse.from(voucherCreated));
    }

    @GetMapping
    public ResponseEntity<List<VoucherResponse>> getAllVouchers(){
         List<VoucherResponse> vouchers = voucherService.listVouchers().stream().map(VoucherResponse::from).toList();
        return ResponseEntity.ok(vouchers);
    }

    @PostMapping("/{voucherCode}/checkout")
    public ResponseEntity<Map<String, Object>> checkoutVoucher(@PathVariable String voucherCode){
        Voucher updated = voucherService.checkoutVoucher(voucherCode);
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "voucherCode", updated.getVoucherCode(),
                "quotaRemaining", updated.getQuotaRemaining()
        ));
    }

    @GetMapping("/{voucherCode}")
    public ResponseEntity<VoucherResponse> getVoucherByCode(@PathVariable String voucherCode){
        Voucher voucher = voucherService.getVoucherByCode(voucherCode);
        return ResponseEntity.ok(VoucherResponse.from(voucher));
    }
}