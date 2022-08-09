package com.github.gpapadopoulos.colorcounting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ColorCountingApplication {

	public static void main(String[] args) {
		SpringApplication.run(ColorCountingApplication.class, args);
	}

}
