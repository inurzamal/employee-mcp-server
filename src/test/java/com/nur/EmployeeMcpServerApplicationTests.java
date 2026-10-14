package com.nur;

import com.nur.configuration.TestVectorStoreConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestVectorStoreConfiguration.class)
class EmployeeMcpServerApplicationTests {

	@Test
	void contextLoads() {
	}

}
