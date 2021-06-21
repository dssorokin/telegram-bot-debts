package com.telegram.debt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DebtBotConfiguration {

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}
}
