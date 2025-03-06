package com.ecommerce.users;

import com.ecommerce.users.config.WebSecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@SpringBootApplication
@EntityScan("com.ecommerce.users.model")
@EnableJpaRepositories("com.ecommerce.users.repositories")
@ConfigurationPropertiesScan("com.ecommerce.users.config")
@Import(WebSecurityConfig.class)
public class RestCodeApplication {
	private static final Logger logger = LoggerFactory.getLogger(RestCodeApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(RestCodeApplication.class, args);
		logger.info("Application started successfully!");
	}
}
