package com.entropy.department;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.entropy.department", "com.entropy.department.services"})
public class ServiceDepartmentApplication {
	public static void main(String[] args) {
		SpringApplication.run(ServiceDepartmentApplication.class, args);
	}
}
