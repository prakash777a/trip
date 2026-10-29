package com.tripcraft;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TripcraftApplication { public static void main(String[] args) { SpringApplication.run(TripcraftApplication.class, args); } }
