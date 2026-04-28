package com.logos;

import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@AutoConfigureTestDatabase // использует встроенную H2 вместо реальной БД
class LogosApplicationTests {

	@Test
	void contextLoads() {
	}

}
