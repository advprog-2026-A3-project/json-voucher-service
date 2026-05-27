package id.ac.ui.cs.advprog.voucher.functional;

import id.ac.ui.cs.advprog.voucher.dto.ApiErrorResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.voucher.repository.VoucherReadRepository;
import id.ac.ui.cs.advprog.voucher.repository.VoucherWriteRepository;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
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
class VoucherApiFlowTest {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String JSON_CONTENT_TYPE = "application/json";

    @LocalServerPort
    private int port;

    @Autowired
    private VoucherReadRepository voucherReadRepository;

    @Autowired
    private VoucherWriteRepository voucherWriteRepository;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void resetDatabase() {
        voucherReadRepository.findAll().forEach(voucherWriteRepository::delete);
    }

    private String api(String path) {
        return "http://localhost:" + port + "/api/v1/vouchers" + path;
    }

    @Test
    void voucherLifecycleShouldWorkThroughHttpApi() throws Exception {
        String voucherCode = "FLOWTEST20";
        String createPayload = objectMapper.writeValueAsString(Map.of(
            "voucherCode", voucherCode,
            "validFrom", FORMATTER.format(LocalDateTime.now().minusDays(1)),
            "validUntil", FORMATTER.format(LocalDateTime.now().plusDays(7)),
            "totalQuota", 5,
            "discountPercent", 20,
            "minimumPurchaseAmount", 100000,
            "maxDiscountAmount", 25000,
            "terms", "Diskon 20% untuk minimum pembelian Rp100.000. Maksimal potongan Rp25.000."
        ));

        HttpRequest createRequest = jsonRequest("POST", api(""), createPayload);
        HttpResponse<String> createResponse = httpClient.send(
            createRequest,
            HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(201, createResponse.statusCode());
        JsonNode created = objectMapper.readTree(createResponse.body());
        assertEquals(voucherCode, created.get("voucherCode").asText());
        assertEquals(5, created.get("quotaRemaining").asInt());
        assertTrue(created.get("active").asBoolean());

        HttpResponse<String> listResponse = httpClient.send(
            HttpRequest.newBuilder(URI.create(api(""))).GET().build(),
            HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, listResponse.statusCode());
        JsonNode listed = objectMapper.readTree(listResponse.body());
        assertEquals(voucherCode, listed.get(0).get("voucherCode").asText());

        HttpResponse<String> getResponse = httpClient.send(
            HttpRequest.newBuilder(URI.create(api("/" + voucherCode))).GET().build(),
            HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, getResponse.statusCode());
        JsonNode fetched = objectMapper.readTree(getResponse.body());
        assertEquals(voucherCode, fetched.get("voucherCode").asText());
        assertEquals(20, fetched.get("discountPercent").asInt());

        String validatePayload = objectMapper.writeValueAsString(Map.of("subtotal", 200000));
        HttpResponse<String> validateResponse = httpClient.send(
            jsonRequest("POST", api("/" + voucherCode + "/validate"), validatePayload),
            HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, validateResponse.statusCode());
        JsonNode validated = objectMapper.readTree(validateResponse.body());
        assertEquals(25000, validated.get("discountAmount").asLong());

        String redeemPayload = objectMapper.writeValueAsString(Map.of("subtotal", 200000));
        HttpResponse<String> redeemResponse = httpClient.send(
            jsonRequest("POST", api("/" + voucherCode + "/redeem"), redeemPayload),
            HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, redeemResponse.statusCode());
        JsonNode redeemed = objectMapper.readTree(redeemResponse.body());
        assertEquals(voucherCode, redeemed.get("voucherCode").asText());
        assertEquals(200000, redeemed.get("subtotal").asLong());
        assertEquals(4, redeemed.get("quotaRemaining").asInt());

        String updatePayload = objectMapper.writeValueAsString(Map.of(
            "validFrom", FORMATTER.format(LocalDateTime.now().minusDays(2)),
            "validUntil", FORMATTER.format(LocalDateTime.now().plusDays(10)),
            "totalQuota", 8,
            "discountPercent", 15,
            "minimumPurchaseAmount", 120000,
            "maxDiscountAmount", 30000,
            "terms", "Diskon 15% untuk minimum pembelian Rp120.000. Maksimal potongan Rp30.000."
        ));
        HttpResponse<String> updateResponse = httpClient.send(
            jsonRequest("PUT", api("/" + voucherCode), updatePayload),
            HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, updateResponse.statusCode());
        JsonNode updated = objectMapper.readTree(updateResponse.body());
        assertEquals(8, updated.get("totalQuota").asInt());
        assertEquals(7, updated.get("quotaRemaining").asInt());
        assertEquals(15, updated.get("discountPercent").asInt());

        HttpResponse<String> deactivateResponse = httpClient.send(
            HttpRequest.newBuilder(URI.create(api("/" + voucherCode + "/deactivate")))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build(),
            HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, deactivateResponse.statusCode());
        JsonNode deactivated = objectMapper.readTree(deactivateResponse.body());
        assertEquals(false, deactivated.get("active").asBoolean());

        HttpResponse<String> invalidValidateResponse = httpClient.send(
            jsonRequest("POST", api("/" + voucherCode + "/validate"), validatePayload),
            HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(400, invalidValidateResponse.statusCode());
        JsonNode invalidValidate = objectMapper.readTree(invalidValidateResponse.body());
        assertEquals("voucher is inactive", invalidValidate.get("message").asText());

        HttpResponse<String> deleteResponse = httpClient.send(
            HttpRequest.newBuilder(URI.create(api("/" + voucherCode)))
                .DELETE()
                .build(),
            HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(204, deleteResponse.statusCode());

        HttpResponse<String> deletedGetResponse = httpClient.send(
            HttpRequest.newBuilder(URI.create(api("/" + voucherCode))).GET().build(),
            HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(404, deletedGetResponse.statusCode());
        JsonNode deleted = objectMapper.readTree(deletedGetResponse.body());
        assertEquals("voucher not found", deleted.get("message").asText());
    }

    @Test
    void createVoucherShouldRejectBlankTermsAndExposeValidationMessage() throws Exception {
        String invalidPayload = objectMapper.writeValueAsString(Map.of(
            "voucherCode", "BADVOUCHER",
            "validFrom", FORMATTER.format(LocalDateTime.now().minusDays(1)),
            "validUntil", FORMATTER.format(LocalDateTime.now().plusDays(1)),
            "totalQuota", 5,
            "discountPercent", 10,
            "minimumPurchaseAmount", 0,
            "maxDiscountAmount", 10000,
            "terms", " "
        ));

        HttpResponse<String> response = httpClient.send(
            jsonRequest("POST", api(""), invalidPayload),
            HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(400, response.statusCode());
        JsonNode json = objectMapper.readTree(response.body());
        assertEquals(ApiErrorResponse.ERROR_STATUS, json.get("status").asText());
        assertTrue(json.get("message").asText().toLowerCase().contains("must not be blank"));
    }

    private HttpRequest jsonRequest(String method, String url, String body) {
        return HttpRequest.newBuilder(URI.create(url))
            .header("Content-Type", JSON_CONTENT_TYPE)
            .method(method, HttpRequest.BodyPublishers.ofString(body))
            .build();
    }
}

