package com.beko.DemoBank_v1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DemoBankV1Application {

	public static void main(String[] args) {
		SpringApplication.run(DemoBankV1Application.class, args);
	}

}
