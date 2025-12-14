package com.indra.asistencia;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class Application implements CommandLineRunner {

	@Autowired
	private PasswordEncoder passwordEncoder;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		
		String password = "12345";
		for (int i = 0; i < 3 ; i++) {
			String passwordEncoded = passwordEncoder.encode(password);
			System.out.println("Password encoded: " + passwordEncoded);
			
		}

		
	}



}
