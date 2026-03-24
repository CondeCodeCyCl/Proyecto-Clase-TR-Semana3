package com.carlos.pacientes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.carlos.pacientes", "com.carlos.commons"})
public class PacientesMsvApplication {

	public static void main(String[] args) {
		SpringApplication.run(PacientesMsvApplication.class, args);
	}

}
