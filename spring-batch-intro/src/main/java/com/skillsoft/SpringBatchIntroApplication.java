package com.skillsoft;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@SpringBootApplication
public class SpringBatchIntroApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBatchIntroApplication.class, args);
	}


}
