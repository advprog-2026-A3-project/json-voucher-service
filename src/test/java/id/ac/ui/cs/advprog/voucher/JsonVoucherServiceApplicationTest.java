package id.ac.ui.cs.advprog.voucher;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class JsonVoucherServiceApplicationTest {

    @Test
    void mainDelegatesToSpringApplicationRun() {
        String[] args = new String[]{"--spring.main.banner-mode=off"};

        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            JsonVoucherServiceApplication.main(args);

            springApplication.verify(() -> SpringApplication.run(JsonVoucherServiceApplication.class, args));
        }
    }
}
