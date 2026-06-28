package cl.smartlogix.inventario;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Tag("docker")
class MsInventarioApplicationTests {

	@Test
	void contextLoads() {
	}

}
