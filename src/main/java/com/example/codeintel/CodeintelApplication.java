package com.example.codeintel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;

@Async
@SpringBootApplication
public class CodeintelApplication {

	public static void main(String[] args) {
		SpringApplication.run(CodeintelApplication.class, args);
	}

}
