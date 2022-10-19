package com.cens.generationService;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GenerationServiceApplicationTests {
    static {
        System.setProperty("NACIONAL_REFERENCE","bar");
		System.setProperty("NACIONAL_HOST","bar");
		System.setProperty("REGIONAL_REFERENCE","BAR");
		System.setProperty("REGIONAL_HOST","bar");
		System.setProperty("REGIONAL_CODE","11001");
    }
	@Test
	void contextLoads() {
	}

}
