package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.demo.util.SlashInterceptor;

@SpringBootApplication
public class DemoApplication implements WebMvcConfigurer {

	private final SlashInterceptor slashInterceptor;

	// slash interceptor 선언
	public DemoApplication(SlashInterceptor slashInterceptor) {
		this.slashInterceptor = slashInterceptor;
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	// add interceptors for slash
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(slashInterceptor);
	}

	@Override
	public void addViewControllers( ViewControllerRegistry registry) {
		registry.addRedirectViewController("/", "/order");
	}

}
