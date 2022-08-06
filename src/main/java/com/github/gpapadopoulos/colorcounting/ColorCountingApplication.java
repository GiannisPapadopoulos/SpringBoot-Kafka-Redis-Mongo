package com.github.gpapadopoulos.colorcounting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
public class ColorCountingApplication {

	public static void main(String[] args) {
		SpringApplication.run(ColorCountingApplication.class, args);
	}

}
