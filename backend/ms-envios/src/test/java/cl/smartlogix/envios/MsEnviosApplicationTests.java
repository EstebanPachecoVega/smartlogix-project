package cl.smartlogix.envios;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Tag("docker")
class MsEnviosApplicationTests {

	@Test
	void contextLoads() {
	}

}
