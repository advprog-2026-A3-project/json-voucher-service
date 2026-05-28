package id.ac.ui.cs.advprog.voucher.functional;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.voucher.entity.Voucher;
import id.ac.ui.cs.advprog.voucher.repository.VoucherReadRepository;
import id.ac.ui.cs.advprog.voucher.repository.VoucherWriteRepository;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class VoucherConcurrencyTest {
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String VOUCHER_CODE = "LOCKJSON1";

    @LocalServerPort
    private int port;

    @Autowired
    private VoucherReadRepository voucherReadRepository;

    @Autowired
    private VoucherWriteRepository voucherWriteRepository;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        voucherReadRepository.findAll().forEach(voucherWriteRepository::delete);
        executorService = Executors.newFixedThreadPool(2);

        Voucher voucher = new Voucher(
            VOUCHER_CODE,
            LocalDateTime.now().minusMinutes(5),
            LocalDateTime.now().plusDays(1),
            1,
            10,
            0L,
            null,
            "Voucher untuk uji concurrency"
        );
        voucherWriteRepository.save(voucher);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        if (executorService != null) {
            executorService.shutdownNow();
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    void concurrentRedeemShouldAllowOnlyOneSuccessWhenQuotaIsOne() throws Exception {
        CountDownLatch startGate = new CountDownLatch(1);

        CompletableFuture<Integer> firstRequest = submitRedeem(startGate);
        CompletableFuture<Integer> secondRequest = submitRedeem(startGate);

        startGate.countDown();

        List<Integer> statuses = List.of(
            firstRequest.get(10, TimeUnit.SECONDS),
            secondRequest.get(10, TimeUnit.SECONDS)
        );

        long successCount = statuses.stream().filter(status -> status == 200).count();
        long conflictCount = statuses.stream().filter(status -> status == 409).count();

        assertEquals(1, successCount);
        assertEquals(1, conflictCount);

        Optional<Voucher> voucher = voucherReadRepository.findByVoucherCode(VOUCHER_CODE);
        assertTrue(voucher.isPresent());
        assertEquals(0, voucher.orElseThrow().getQuotaRemaining());
    }

    private CompletableFuture<Integer> submitRedeem(CountDownLatch startGate) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                startGate.await(5, TimeUnit.SECONDS);
                HttpResponse<String> response = httpClient.send(
                    jsonRequest(
                        api("/" + VOUCHER_CODE + "/redeem"),
                        objectMapper.writeValueAsString(Map.of("subtotal", 200000))
                    ),
                    HttpResponse.BodyHandlers.ofString()
                );
                return response.statusCode();
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }, executorService);
    }

    private String api(String path) {
        return "http://localhost:" + port + "/api/v1/vouchers" + path;
    }

    private HttpRequest jsonRequest(String url, String body) {
        return HttpRequest.newBuilder(URI.create(url))
            .header("Content-Type", JSON_CONTENT_TYPE)
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
    }
}
