package com.carlos.medicos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"package com.carlos.medicos;", "package com.carlos.commons;"})
public class MedicosMsvApplication {

	public static void main(String[] args) {
		SpringApplication.run(MedicosMsvApplication.class, args);
	}

}
