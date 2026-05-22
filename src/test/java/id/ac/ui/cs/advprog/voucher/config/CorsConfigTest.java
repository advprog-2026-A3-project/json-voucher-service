package id.ac.ui.cs.advprog.voucher.config;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class CorsConfigTest {

    @Test
    void parseAllowedOriginsRemovesBlankEntriesAndWhitespace() throws Exception {
        CorsConfig corsConfig = new CorsConfig(" http://localhost:3000, ,https://json.example.com ,,http://localhost:3001 ");

        Method parseAllowedOrigins = CorsConfig.class.getDeclaredMethod("parseAllowedOrigins");
        parseAllowedOrigins.setAccessible(true);

        String[] origins = (String[]) parseAllowedOrigins.invoke(corsConfig);

        assertArrayEquals(
            new String[]{"http://localhost:3000", "https://json.example.com", "http://localhost:3001"},
            origins
        );
    }
}
