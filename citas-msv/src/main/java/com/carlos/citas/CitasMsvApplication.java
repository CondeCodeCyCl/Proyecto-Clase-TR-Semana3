package com.carlos.citas;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.carlos.citas", "com.carlos.commons"})
public class CitasMsvApplication {

	public static void main(String[] args) {
		SpringApplication.run(CitasMsvApplication.class, args);
	}

}
