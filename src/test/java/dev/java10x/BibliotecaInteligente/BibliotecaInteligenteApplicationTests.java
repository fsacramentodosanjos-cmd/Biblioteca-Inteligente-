package dev.java10x.BibliotecaInteligente;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // usa H2 em memória (src/test/resources/application-test.properties)
class BibliotecaInteligenteApplicationTests {

	@Test
	void contextLoads() {
	}

}
