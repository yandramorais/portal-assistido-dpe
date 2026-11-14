package br.gov.go.defensoria.portal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class PortalApplicationTests {

    @Test
    void contextLoads() {
    }
}
